package com.mastfrog.webapi;

import com.mastfrog.webapi.builtin.Parameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.mastfrog.acteur.util.BasicCredentials;
import com.mastfrog.acteur.util.Headers;
import com.mastfrog.giulius.Dependencies;
import com.mastfrog.guicy.scope.ReentrantScope;
import com.mastfrog.netty.http.client.HttpClient;
import com.mastfrog.netty.http.client.HttpClientBuilder;
import com.mastfrog.netty.http.client.HttpRequestBuilder;
import com.mastfrog.netty.http.client.ResponseFuture;
import com.mastfrog.netty.http.client.State;
import com.mastfrog.url.Path;
import com.mastfrog.url.URL;
import com.mastfrog.url.URLBuilder;
import com.mastfrog.util.Exceptions;
import com.mastfrog.util.thread.Receiver;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Invokes web api calls
 *
 * @author Tim Boudreau
 */
public class Invoker<T extends Enum<T> & WebCallEnum> {

    private final HttpClient client;
    private final Dependencies deps;
    private final URL base;
    private final ObjectMapper mapper;

    @Inject
    public Invoker(HttpClient client, Dependencies deps, URL base, ObjectMapper mapper) {
        this.client = client;
        this.deps = deps;
        this.base = base;
        this.mapper = mapper;
    }

    private void replace(String token, StringBuilder in, Object with) {
        if (with == null) {
            return;
        }
        token = "{{" + token + "}}";
        int ix = in.indexOf(token);
        if (ix >= 0) {
            try {
                String val = URLEncoder.encode(with.toString(), "UTF-8");
                in.replace(ix, ix + token.length(), val);
            } catch (UnsupportedEncodingException ex) {
                Exceptions.chuck(ex);
            }
        }
    }

    /**
     * Create a standalone Web API invoker for the passed base URL and Web API
     * enum type. This method creates all of the Guice plumbing used to drive
     * the invoker under the hood. Use this for deployment in simple apps which
     * have no other need for dependency injection; otherwise, you may want to
     * use
     * <a href="WebApiModule.class"><code>WebApiModule</code></a> and set up
     * injection explicitly (in which case you need to bind
     * <code>com.mastfrog.url.URL</code> and
     * <code>com.mastfrog.netty.HttpClient</code> and Jackson's
     * <code>ObjectMapper</code>.
     *
     * @param <T> The type
     * @param baseUrl The base URL for api calls
     * @param webApi An enum whose constants implement WebCall
     * @return An invoker
     * @throws IOException
     */
    public static <T extends Enum<T> & WebCallEnum> Invoker<T> create(URL baseUrl, Class<T> webApi) throws IOException {
        Dependencies deps = Dependencies.builder().add(new StandaloneModule(baseUrl))
                .add(new WebApiModule(webApi)).build();
        return deps.getInstance(Invoker.class);
    }

    private static class StandaloneModule extends AbstractModule {

        private final URL url;
        private final HttpClientBuilder builder = HttpClient.builder().followRedirects();

        StandaloneModule(URL url) {
            this.url = url;
        }

        @Override
        protected void configure() {
            bind(URL.class).toInstance(url);
            bind(HttpClient.class).toInstance(builder.build());
        }
    }

    private <T> boolean interpolate(StringBuilder sb, Class<T> type, Dependencies deps, WebCall call) {
        Class<? extends Interpolator<T>> pi = call.interpolator(type);
        if (pi != null) {
            return interpolate(sb, type, deps, call, pi);
        }
        return false;
    }

    private <T, R extends Interpolator<T>> boolean interpolate(StringBuilder sb, Class<T> type, Dependencies deps, WebCall call, Class<R> pi) {
        R r = deps.getInstance(pi);
        T obj = deps.getInstance(type);
        r.interpolate(call, sb, obj, type);
        return false;
    }

    private Object[] combine(Object[] a, Object... with) {
        List<Object> all = new ArrayList<>(a.length + with.length);
        all.addAll(Arrays.asList(a));
        all.addAll(Arrays.asList(with));
        return all.toArray(new Object[a.length + with.length]);
    }

    public <T> ResponseFuture call(final WebCallEnum call, final Callback<T> callback, final Object... args) throws Exception {
        return call(call, null, callback, args);
    }

    public <T> ResponseFuture call(final WebCallEnum call, Receiver<State<?>> listener, final Callback<T> callback, final Object... args) throws Exception {
        final ReentrantScope scope = deps.getInstance(Key.get(ReentrantScope.class, Names.named("webapi")));
        try (AutoCloseable cl = scope.enter(args)) {
            final WebCall wc = call.get();
            HttpRequestBuilder reqb = toRequest(wc, deps);
            if (listener != null) {
                reqb.onEvent(listener);
            }
            if (wc.authenticationRequired()) {
                BasicCredentials bc = deps.getInstance(BasicCredentials.class);
                if (bc != null) {
                    reqb.addHeader(Headers.AUTHORIZATION, bc);
                }
            }
            reqb.on(State.Error.class, new Receiver<Throwable>() {
                @Override
                public void receive(Throwable object) {
                    callback.error(object);
                }
            });
            reqb.on(State.HeadersReceived.class, new Receiver<HttpResponse>() {
                @Override
                public void receive(HttpResponse object) {
                    callback.responseReceived(object.getStatus(), object.headers());
                    if (HttpResponseStatus.NOT_MODIFIED.equals(object.getStatus())) {
                        callback.notModified(object.headers());
                    }
                }
            });
            reqb.on(State.Finished.class, new Receiver<FullHttpResponse>() {

                @Override
                public void receive(FullHttpResponse resp) {
                    try (AutoCloseable ac = scope.enter(combine(args, wc, call))) {
                        if (resp.getStatus().code() < 299 && resp.getStatus().code() > 199) {
                            Interpreter inter = wc.interpreter(deps);
                            if (inter == null) {
                                inter = new DefaultResponseInterceptor(mapper);
                            }
                            try {
                                T obj = inter.interpret(resp.getStatus(), resp.headers(), resp.content(), callback.type());
                                callback.success(obj);
                            } catch (Exception ex) {
                                callback.error(ex);
                                Exceptions.printStackTrace(ex);
                            }
                        } else {
                            callback.fail(resp.getStatus(), resp.content());
                        }
                    } catch (Exception e) {
                        callback.error(e);
                    }
                }
            });
            return reqb.execute();
        }
    }

    public HttpRequestBuilder toRequest(WebCall call, Dependencies deps) throws IOException {
        StringBuilder b = new StringBuilder(call.urlTemplate());
        for (Class<?> type : call.requiredTypes()) {
            if (!interpolate(b, type, deps, call)) {
                Object o = deps.getInstance(type);
                replace(type.getSimpleName().toLowerCase(), b, o);
            }
        }

        Parameters params = deps.getInstance(Parameters.class);
        URLBuilder bld = URL.builder(base);
        Path p = Path.parse(b.toString());
        Path pth = Path.merge(base.getPath() == null ? Path.parse("/") : base.getPath(), p);
        bld.setPath(pth);

        HttpRequestBuilder builder = client.request(call.method()).setURL(bld.create());

        if (params != null) {
            params.populate(builder);
        }
        for (Class<?> type : call.requiredTypes()) {
            decorate(builder, type, deps, call);
        }
        return builder;
    }

    private <T, R extends Decorator<T>> boolean decorate(Class<T> type, HttpRequestBuilder builder, T obj, Dependencies deps, WebCall call, Class<R> rt) throws IOException {
        R r = deps.getInstance(rt);
        r.decorate(call, builder, obj, type);
        return true;
    }

    private <T> boolean decorate(HttpRequestBuilder builder, Class<T> type, Dependencies deps, WebCall call) throws IOException {
        Class<? extends Decorator<T>> x = call.decorator(type);
        T obj = deps.getInstance(type);
        if (x != null && obj != null) {
            return decorate(type, builder, obj, deps, call, x);
        }
        return false;
    }
}

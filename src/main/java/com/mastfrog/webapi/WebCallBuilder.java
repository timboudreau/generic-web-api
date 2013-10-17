package com.mastfrog.webapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mastfrog.acteur.util.Method;
import com.mastfrog.giulius.Dependencies;
import com.mastfrog.util.ConfigurationError;
import java.util.HashSet;
import java.util.Set;

/**
 * Builds one element of a web api
 *
 * @author Tim Boudreau
 */
public class WebCallBuilder {

    private Enum<?> id;
    private Method method = Method.GET;
    private String path;
    private final Set<Class<?>> requiredTypes = new HashSet<>();
    private boolean authenticationRequired;
    private Decorators decorators = new Decorators();
    private Interpolators interpolators = new Interpolators();
    private boolean stayOpen;
    private Class<?> type = Void.class;
    private boolean hasBody = true;
    private Class<? extends Interpreter> interpreter;

    public WebCallBuilder() {
    }

    public WebCallBuilder(Enum<?> id) {
        this.id = id;
    }

    /**
     * <b>Required</b> - set the ID of this call
     *
     * @param id The id
     * @return this
     */
    public WebCallBuilder id(Enum<?> id) {
        this.id = id;
        return this;
    }

    /**
     * Set the HTTP method of this call (by default it is GET).
     *
     * @param method The method
     * @return this
     */
    public WebCallBuilder method(Method method) {
        this.method = method;
        return this;
    }

    /**
     * <b>Required</b> - Set the <i>path template</i> for this call. To do
     * simple substitution, you can delimit templated items with {{ }} and use
     * simple types whose toString() value is substituted in. So if you have a
     * class called
     * <code>UserId</code> whose
     * <code>toString()</code> method returns a useful representation of the ID,
     * then you can have a template that looks like, say,
     * <code>/users/{{userid}}/dostuff</code> and if a UserId is available when
     * the call is invoked, {{userid}} will be replaced. The templated text
     * should be the <i>lower-cased</li> type name of the type.
     *
     * @param path The path
     * @return This
     */
    public WebCallBuilder path(String path) {
        this.path = path;
        return this;
    }

    /**
     * Add a type which is required by something that will construct the call -
     * this might be a constructor argument to a Decorator subclass, or
     * something similer. Use this to ensure types that will be needed when
     * processing calls are bound correctly.
     *
     * @param type
     * @return
     */
    public WebCallBuilder addRequiredType(Class<?> type) {
        requiredTypes.add(type);
        return this;
    }

    public WebCallBuilder addRequiredTypes(Class<?>... types) {
        for (Class<?> type : types) {
            addRequiredType(type);
        }
        return this;
    }

    /**
     * Mark this call as requiring HTTP basic authentication. To use Basic
     * authentication, do this and include a
     * <code>BasicCredentials</code> in the call context.
     *
     * @return
     */
    public WebCallBuilder authenticationRequired() {
        this.authenticationRequired = true;
        return this;
    }

    public WebCallBuilder authenticationRequired(boolean required) {
        this.authenticationRequired = required;
        return this;
    }
    
    /**
     * Add an interpolator which will replace some contents of the API call path
     * based on some object type. The actual interpolator instance will be
     * created on-demand.
     *
     * @param <T> The object type
     * @param <R> The interpolator type
     * @param type The object type
     * @param dec The iterpolator type
     * @return This
     */
    public <T, R extends Interpolator<T>> WebCallBuilder withInterpolator(Class<T> type, Class<R> dec) {
        addRequiredType(type);
        interpolators.add(type, dec);
        return this;
    }

    /**
     * Add a decorator which will somehow modify an HTTP request based on some
     * object passed into the call context.
     *
     * @param <T> The object type
     * @param <R> The decorator type
     * @param type The object type
     * @param dec The decorator type
     * @return this
     */
    public <T, R extends Decorator<T>> WebCallBuilder withDecorator(Class<T> type, Class<R> dec) {
        addRequiredType(type);
        decorators.add(type, dec);
        return this;
    }

    /**
     * This call utilizes an open HTTP connection - don't close the channel when
     * the request is completed.
     *
     * @return this
     */
    public WebCallBuilder stayOpen() {
        stayOpen = true;
        return this;
    }

    /**
     * The response type. May be superseded by the type on the Callback passed
     * to the web call
     *
     * @param type A type
     * @return
     * @deprecated
     */
    public WebCallBuilder responseType(Class<?> type) {
        this.type = type;
        return this;
    }

    /**
     * Determine whether to expect a response body. If false, the system may opt
     * to close the connection and call the callback as soon as headers have
     * been received.
     *
     * @param val
     * @return
     */
    public WebCallBuilder hasBody(boolean val) {
        this.hasBody = val;
        return this;
    }

    /**
     * Thing which interprets the response
     *
     * @param interp An interpreter
     * @return this
     */
    public WebCallBuilder interpreter(Class<? extends Interpreter> interp) {
        this.interpreter = interp;
        return this;
    }

    /**
     * Build a web call
     *
     * @return A web call.
     * @throws ConfigurationError if the id, method or path are not set
     */
    public WebCall build() {
        if (id == null) {
            throw new ConfigurationError("Enum id not set");
        }
        if (method == null) {
            throw new ConfigurationError("Method not set");
        }
        if (path == null) {
            throw new ConfigurationError("Path not set");
        }
        return new WebCallImpl(id, method, path, requiredTypes,
                authenticationRequired, decorators, interpolators, stayOpen,
                type, hasBody, interpreter);
    }

    private static class WebCallImpl implements WebCall {

        private final Enum<?> id;
        private final Method method;
        private final String path;
        private final Set<Class<?>> requiredTypes;
        private final boolean authenticationRequired;
        private final Decorators decorators;
        private final Interpolators interpolators;
        private final boolean stayOpen;
        private final Class<?> type;
        private final boolean hasBody;
        private final Class<? extends Interpreter> interpreter;

        public WebCallImpl(Enum<?> id, Method method, String path, Set<Class<?>> requiredTypes, boolean authenticationRequired, Decorators decorators, Interpolators interpolators, boolean stayOpen, Class<?> type, boolean hasBody, Class<? extends Interpreter> interpreter) {
            this.id = id;
            this.method = method;
            this.path = path;
            this.requiredTypes = requiredTypes;
            this.authenticationRequired = authenticationRequired;
            this.decorators = decorators;
            this.interpolators = interpolators;
            this.stayOpen = stayOpen;
            this.interpreter = interpreter;
            this.type = type;
            this.hasBody = hasBody;
        }

        @Override
        public String name() {
            return id.toString();
        }

        @Override
        public Enum<?> id() {
            return id;
        }

        @Override
        public Method method() {
            return method;
        }

        @Override
        public String urlTemplate() {
            return path;
        }

        @Override
        public Class<?>[] requiredTypes() {
            return requiredTypes.toArray(new Class<?>[requiredTypes.size()]);
        }

        @Override
        public boolean authenticationRequired() {
            return authenticationRequired;
        }

        @Override
        public boolean expectResponseBody() {
            return hasBody;
        }

        @Override
        public boolean isStayOpen() {
            return stayOpen;
        }

        @Override
        public Class<?> responseType() {
            return type;
        }

        @Override
        public <T, R extends Decorator<T>> Class<R> decorator(Class<T> type) {
            return decorators.get(type);
        }

        @Override
        public <T, R extends Interpolator<T>> Class<R> interpolator(Class<T> type) {
            return interpolators.get(type);
        }

        @Override
        public Interpreter interpreter(Dependencies deps) {
            Interpreter result;
            if (interpreter == null || interpreter.equals(DefaultResponseInterceptor.class)) {
                return new DefaultResponseInterceptor(deps.getInstance(ObjectMapper.class));
            } else {
                result = deps.getInstance(interpreter);
            }
            return result;
        }
    }
}

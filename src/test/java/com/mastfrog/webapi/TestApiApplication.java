package com.mastfrog.webapi;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.mastfrog.acteur.Acteur;
import com.mastfrog.acteur.ActeurFactory;
import com.mastfrog.acteur.Application;
import com.mastfrog.acteur.Event;
import com.mastfrog.acteur.HttpEvent;
import com.mastfrog.acteur.Page;
import com.mastfrog.acteur.server.ServerModule;
import com.mastfrog.acteur.util.Method;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import java.util.concurrent.CountDownLatch;

/**
 *
 * @author tim
 */
class TestApiApplication extends Application {

    public TestApiApplication() {
        add(HelloPage.class);
        add(EchoPage.class);
        add(FooPage.class);
        System.out.println("Created application");
    }

    static class FooPage extends Page {

        @Inject
        FooPage(ActeurFactory af) {
            System.out.println("Create a foo page");
            add(af.matchMethods(Method.GET));
            add(af.matchPath("^foo$"));
            add(A.class);
        }

        private static class A extends Acteur {

            A() {
                setState(new RespondWith(200, "Hujus hujus"));
            }
        }
    }

    @Override
    public CountDownLatch onEvent(Event<?> event, Channel channel) {
        System.out.println("ON EVENT " + ((HttpEvent)event).getMethod() + " path '" + ((HttpEvent)event).getPath() + "'");
        System.out.println("URI: " + ((FullHttpRequest) event.getRequest()).getUri());

        return super.onEvent(event, channel); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected HttpResponse createNotFoundResponse(Event<?> event) {
        System.out.println("RESPONSE " + 404 + " for " + ((HttpEvent)event).getPath());
        return super.createNotFoundResponse(event); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected HttpResponse decorateResponse(Event<?> event, Page page, Acteur action, HttpResponse response) {
        System.out.println("RESPONSE " + response.getStatus() + " for " + ((HttpEvent)event).getPath());
        return super.decorateResponse(event, page, action, response); //To change body of generated methods, choose Tools | Templates.
    }

    public static class Module extends AbstractModule {

        Module() {
            System.out.println("Created a module");
        }

        @Override
        protected void configure() {
            install(new ServerModule(TestApiApplication.class, 5, 5, 5));
        }
    }
}

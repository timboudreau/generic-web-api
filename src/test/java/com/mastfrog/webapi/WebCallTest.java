package com.mastfrog.webapi;

import com.google.inject.AbstractModule;
import com.mastfrog.acteur.util.Server;
import com.mastfrog.giulius.Dependencies;
import com.mastfrog.giulius.tests.GuiceRunner;
import com.mastfrog.giulius.tests.TestWith;
import com.mastfrog.netty.http.client.HttpClient;
import com.mastfrog.netty.http.client.ResponseFuture;
import com.mastfrog.url.URL;
import com.mastfrog.webapi.WebCallTest.M;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author tim
 */
@RunWith(GuiceRunner.class)
@TestWith(value = {TestApiApplication.Module.class})
public class WebCallTest {

    @Test
    public void test(Server server) throws IOException, Exception, Throwable {
        Dependencies deps = new Dependencies(new M());
        HttpClient cl = deps.getInstance(HttpClient.class);
        Invoker invoker = deps.getInstance(Invoker.class);

        server.start(3729);

        CB cb = new CB();
        ResponseFuture f = invoker.call(TestAPI.HELLO_WORLD, cb, new DisplayName("Tim Boudreau"), new UserId("tim"), new Integer(23), new Short((short) 5), Boolean.TRUE);

        f.await(3, TimeUnit.SECONDS);
        f.throwIfError();
        assertNotNull(cb.obj);
        assertEquals("tim", cb.obj.get("name"));
        assertEquals("Hello Tim Boudreau", cb.obj.get("message"));

        EchoCB echo = new EchoCB();

        f = invoker.call(TestAPI.ECHO, echo, "hot ", new UserId("tim"));
//        Thread.sleep(9000000);
        f.await(3, TimeUnit.SECONDS);
        f.throwIfError();
        assertNotNull(echo);
        assertEquals("hot hot hot ", echo.result);

        assertEquals(OK, echo.status);
        cl.shutdown();
    }

    private static class EchoCB extends Callback<String> {

        volatile HttpResponseStatus status;
        private String result;

        EchoCB() {
            super(String.class);
        }

        @Override
        public void success(String object) {
            System.out.println("ECHOCB SUCCESS " + object);
            result = object;
        }

        @Override
        public void fail(HttpResponseStatus status, ByteBuf bytes) {
            System.out.println("FAIL " + status);
            this.status = status;
        }

        @Override
        public void error(Throwable err) {
            err.printStackTrace();
        }

        @Override
        public void responseReceived(HttpResponseStatus status, HttpHeaders headers) {
            System.out.println("RESPONSE RECEIVED " + status);
            this.status = status;
        }
    }

    private static class CB extends Callback<Map> {

        CB() {
            super(Map.class);
        }

        private Map obj;

        @Override
        public void success(Map object) {
            System.out.println("Success " + object);
            obj = object;
        }

        @Override
        public void fail(HttpResponseStatus status, ByteBuf bytes) {
            System.out.println("FAIL " + status + " " + bytes);
            Thread.dumpStack();
        }

    }

    static class M extends AbstractModule {

        @Override
        protected void configure() {
            install(new WebApiModule(TestAPI.class));
            bind(URL.class).toInstance(URL.parse("http://localhost:3729"));
//            bind(URL.class).toInstance(URL.parse("http://localhost:9333"));
            bind(HttpClient.class).toInstance(HttpClient.builder().followRedirects().build());
        }
    }
}

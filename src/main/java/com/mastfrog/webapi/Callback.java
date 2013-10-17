package com.mastfrog.webapi;

import com.mastfrog.util.Parameterized;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Thing which is called back with the result of a Web API call.
 *
 * @author Tim Boudreau
 */
public abstract class Callback<T> implements Parameterized<T> {

    private final Class<T> type;

    public Callback(Class<T> type) {
        this.type = type;
    }

    /**
     * Called when the call has succeeded, with the body (or some other
     * representation) of the response translated into an object by the
     * ResponseInterpreter of the call (or the default one which handles
     * a number of basic types and JSON).
     * @param object 
     */
    public abstract void success(T object);

    /**
     * Called if there is a response code > 399, with the raw response bytes
     * @param status The status
     * @param bytes The bytes
     */
    public abstract void fail(HttpResponseStatus status, ByteBuf bytes);

    /**
     * Called if an exception is thrown at any point in processing the request
     * @param err The exception
     */
    public void error(Throwable err) {
        err.printStackTrace();
    }

    /**
     * Called in the event of a 304 Not Modified response.  The caller is
     * responsible for setting the outbound request's headers in such a way
     * as to provoke this response.
     * @param headers The headers
     */
    public void notModified(HttpHeaders headers) {
    }

    /**
     * Called when the response headers are received, before the body arrives
     * @param status The status
     * @param headers The headers
     */
    public void responseReceived(HttpResponseStatus status, HttpHeaders headers) {
    }

    @Override
    public final Class<T> type() {
        return type;
    }
}

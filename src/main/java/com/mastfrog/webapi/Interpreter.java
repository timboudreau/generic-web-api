package com.mastfrog.webapi;

import com.google.inject.ImplementedBy;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Takes an HTTP response and translates it into some sort of object.
 * The default implementation handles JSON and string.
 *
 * @author Tim Boudreau
 */
@ImplementedBy(DefaultResponseInterceptor.class)
public abstract class Interpreter {

    public abstract <T> T interpret(HttpResponseStatus status,
            HttpHeaders headers, ByteBuf contents, Class<T> as) throws Exception;

}

package com.mastfrog.webapi;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Takes an HTTP response and translates it into some sort of object.
 * The default implementation handles JSON and string.
 *
 * @author Tim Boudreau
 */
public abstract class Interpreter {

    protected abstract <T> T interpret(HttpResponseStatus status,
            HttpHeaders headers, ByteBuf contents, Class<T> as) throws Exception;

}

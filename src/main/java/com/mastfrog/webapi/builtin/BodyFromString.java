package com.mastfrog.webapi.builtin;

import com.google.common.net.MediaType;
import com.mastfrog.netty.http.client.HttpRequestBuilder;
import com.mastfrog.webapi.Decorator;
import com.mastfrog.webapi.WebCall;
import java.io.IOException;

/**
 * A request decorator which takes a String from the context and attaches
 * it as the body of a request.
 *
 * @author Tim Boudreau
 */
public final class BodyFromString implements Decorator<String> {

    @Override
    public void decorate(WebCall call, HttpRequestBuilder builder, String obj, Class<String> type) throws IOException {
        builder.setBody(obj, MediaType.PLAIN_TEXT_UTF_8);
    }
}

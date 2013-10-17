package com.mastfrog.webapi;

import com.mastfrog.netty.http.client.HttpRequestBuilder;
import java.io.IOException;

/**
 * Somehow modifies a to-be-sent HTTP request, based on an object passed
 * into the call context.
 *
 * @author Tim Boudreau
 */
public interface Decorator<T> {

    /**
     * Decorate the request by modifying contents of the HttpRequestBuilder
     * 
     * @param call The call being processed
     * @param builder A request builder
     * @param obj An object
     * @param type The canonical type of the object (the actual object may be a 
     * subclass)
     */
    void decorate(WebCall call, HttpRequestBuilder builder, T obj, Class<T> type) throws IOException;
    
}

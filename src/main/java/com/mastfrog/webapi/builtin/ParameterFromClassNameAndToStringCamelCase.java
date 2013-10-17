package com.mastfrog.webapi.builtin;

import com.mastfrog.netty.http.client.HttpRequestBuilder;
import com.mastfrog.webapi.Decorator;
import com.mastfrog.webapi.WebCall;

/**
 * Sets a parameter with the class name to the value of toString() on the value.
 * So if you have, say a DisplayName object in scope and its toString() value is
 * "Joe Blow", you get a parameter
 * <code>?displayname=Joe%20Blow</code>
 *
 * @author Tim Boudreau
 */
public final class ParameterFromClassNameAndToStringCamelCase<T> implements Decorator<T> {

    @Override
    public void decorate(WebCall call, HttpRequestBuilder builder, T obj, Class<T> type) {
        StringBuilder key = new StringBuilder(type.getSimpleName());
        key.setCharAt(0, Character.toLowerCase(key.charAt(0)));
        String val = obj.toString();
        builder.addQueryPair(key.toString(), val);
    }
}

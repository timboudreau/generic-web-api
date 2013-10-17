package com.mastfrog.webapi.builtin;

import com.google.common.collect.Maps;
import com.mastfrog.netty.http.client.HttpRequestBuilder;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tim Boudreau
 */
public class Parameters {

    private final Map<Enum<?>, String> pairs = Maps.newHashMap();
    private final Map<String, String> adhoc = Maps.newHashMap();

    public static Parameters create(String name, String value) {
        return new Parameters().add(name, value);
    }

    public static Parameters create(String name, long value) {
        return new Parameters().add(name, value + "");
    }

    public static Parameters create(Enum<?> name, long value) {
        return new Parameters().add(name, value);
    }

    public static Parameters create(Enum<?> name, String value) {
        return new Parameters().add(name, value);
    }

    public Parameters add(String name, String value) {
        adhoc.put(name, value);
        return this;
    }

    public Parameters add(Enum<?> name, long value) {
        pairs.put(name, Long.toString(value));
        return this;
    }

    public Parameters add(Enum<?> name, String value) {
        pairs.put(name, value);
        return this;
    }

    public void populate(HttpRequestBuilder b) {
        for (Map.Entry<Enum<?>, String> e : pairs.entrySet()) {
            b.addQueryPair(e.getKey().toString(), e.getValue());
        }
        for (Map.Entry<String, String> e : adhoc.entrySet()) {
            b.addQueryPair(e.getKey().toString(), e.getValue());
        }
    }

    public void populate(StringBuilder sb) {
        boolean first = sb.indexOf("?") < 0;
        try {
            for (Map.Entry<Enum<?>, String> e : pairs.entrySet()) {
                sb.append(first ? '?' : '&');
                first = false;
                sb.append(e.getKey()).append('=').append(URLEncoder.encode(e.getValue(), "UTF-8"));
            }
            for (Map.Entry<String, String> e : adhoc.entrySet()) {
                sb.append(first ? '?' : '&');
                first = false;
                sb.append(e.getKey()).append('=').append(URLEncoder.encode(e.getValue(), "UTF-8"));
            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Parameters.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

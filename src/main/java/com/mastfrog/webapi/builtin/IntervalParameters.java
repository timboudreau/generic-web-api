package com.mastfrog.webapi.builtin;

import com.mastfrog.netty.http.client.HttpRequestBuilder;
import com.mastfrog.util.time.Interval;
import com.mastfrog.webapi.Decorator;
import com.mastfrog.webapi.WebCall;

/**
 * Example
 *
 * @author Tim Boudreau
 */
public class IntervalParameters implements Decorator<Interval> {

    @Override
    public void decorate(WebCall call, HttpRequestBuilder builder, Interval i, Class<Interval> type) {
        builder.addQueryPair("start", i.getStartMillis() + "");
        builder.addQueryPair("end", i.getEndMillis() + "");
    }

}

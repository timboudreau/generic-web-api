package com.mastfrog.webapi;

import java.util.LinkedList;
import java.util.List;

/**
 * Registry of interpolators by type
 *
 * @author Tim Boudreau
 */
final class Interpolators {

    private final List<Entry<?, ?>> all = new LinkedList<>();

    @SuppressWarnings("unchecked")
    <T, R extends Interpolator<T>> Class<R> get(Class<T> type) {
        for (Entry<?, ?> e : all) {
            if (type == e.parameterType) {
                return (Class<R>) e.interpolatorType;
            }
        }
        return null;
    }

    public <T, R extends Interpolator<T>> void add(Class<T> type, Class<R> dec) {
        all.add(new Entry(type, dec));
    }

    private static final class Entry<T, R extends Interpolator<T>> {

        private final Class<T> parameterType;
        private final Class<R> interpolatorType;

        public Entry(Class<T> parameterType, Class<R> interpolatorType) {
            this.parameterType = parameterType;
            this.interpolatorType = interpolatorType;
        }
    }
}

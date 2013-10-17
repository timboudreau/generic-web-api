package com.mastfrog.webapi;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Tim Boudreau
 */
final class Decorators {

    private final List<Entry<?, ?>> all = new LinkedList<>();

    @SuppressWarnings("unchecked")
    <T, R extends Decorator<T>> Class<R> get(Class<T> type) {
        for (Entry<?,?> e : all) {
            if (type == e.parameterType) {
                return (Class<R>) e.decoratorType;
            }
        }
        for (Entry<?,?> e : all) {
            if (type.isAssignableFrom(e.parameterType)) {
                return (Class<R>) e.decoratorType;
            }
        }
        return null;
    }

    public <T, R extends Decorator<T>> void add(Class<T> type, Class<R> dec) {
        all.add(new Entry(type, dec));
    }

    private static final class Entry<T, R extends Decorator<T>> {

        private final Class<T> parameterType;
        private final Class<R> decoratorType;

        public Entry(Class<T> parameterType, Class<R> decoratorType) {
            this.parameterType = parameterType;
            this.decoratorType = decoratorType;
        }
    }
}

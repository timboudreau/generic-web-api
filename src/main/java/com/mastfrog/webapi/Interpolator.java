package com.mastfrog.webapi;

/**
 * Thing which can modify the path template of a 
 * <code><a href="http://WebCall.html">WebCall</a></code> with a value derived
 * somehow from an object.
 *
 * @author Tim Boudreau
 */
public interface Interpolator<T> {

    /**
     * Update the path template in the passed string builder based on the passed
     * arguments.
     * 
     * @param call The call in question
     * @param path The path template, which may or may not already have been altered
     * by other interpolators
     * @param obj The object
     * @param type The type of the object (the object may be a subclass, and there
     * may be semantics to the type name)
     * @return True if the StringBuilder was altered
     */
    boolean interpolate(WebCall call, StringBuilder path, T obj, Class<T> type);
    
}

package com.mastfrog.webapi;

import com.mastfrog.acteur.util.Method;
import com.mastfrog.giulius.Dependencies;

/**
 * Implemented on enum constants which represents web calls
 *
 * @author Tim Boudreau
 */
public interface WebCall {

    /**
     * The human-readable name of this call, typically <code>id().toString()</code>
     * @return a name
     */
    String name();
    /**
     * The enum ID of this call
     * @return an id
     */
    Enum<?> id();
    /**
     * The HTTP method of this call;  the default is GET
     * @return 
     */
    Method method();
    /**
     * The path portion of the URL to call;  will be appended to the 
     * <a href="Invoker.html"><code>Invoker</code></a>'s base URL.  Simple
     * string substitution can be done by putting lower-case simple class names
     * into the path, delimited by &lcub;&lcub; &rcub;&rcub;.  So, for example,
     * <code>/users/&lcub;&lcub;userid&rcub;&rcub</code> would mean to look for
     * a class whose simple name, lower-cased, is <code>userid</code>, call
     * <code>toString()</code> on it, and replace the templated portion with that.
     * <p/>
     * For more complex transformations of the path, implement
     * <code><a href="Decorator.html">Decorator</a></code>.
     * @return A url
     */
    String urlTemplate();

    /**
     * The set of types which may, or are expected to, be passed in when making
     * a web call
     * @return 
     */
    Class<?>[] requiredTypes();

    /**
     * If true, look for a set of <code>BasicCredentialss</code> in the call
     * context, and if present, include an Authorization header based on them
     * to authenticate.
     * @return 
     */
    boolean authenticationRequired();

    /**
     * Get the decorator, if any, associated with some required type
     * @param <T> The object type
     * @param <R> The type of the decorator
     * @param type The object type
     * @return The type of the decorator
     */
    <T, R extends Decorator<T>> Class<R> decorator(Class<T> type);

    /**
     * If true, a response body is expected and should be waited for before
     * calling back the callback.  Simple calls may just need acknowledgement.
     * @return whether or not a body is required
     */
    boolean expectResponseBody();

    /**
     * If true, do not close the connection - the caller will hold on to
     * the <code>ResponseFuture</code>
     * @return 
     */
    boolean isStayOpen();

    Class<?> responseType();
    
    <T, R extends Interpolator<T>> Class<R> interpolator(Class<T> type);
    
    Interpreter interpreter(Dependencies deps);
}

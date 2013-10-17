package com.mastfrog.webapi;

import com.mastfrog.webapi.builtin.Parameters;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.mastfrog.acteur.util.BasicCredentials;
import com.mastfrog.guicy.scope.ReentrantScope;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Tim Boudreau
 */
public final class WebApiModule<T extends Enum<T> & WebCallEnum> extends AbstractModule {
    private final Class<T> type;
    public WebApiModule(Class<T> type) {
        this.type = type;
    }

    @Override
    protected void configure() {
        ReentrantScope scope = new ReentrantScope();
        bind(ReentrantScope.class).annotatedWith(Names.named("webapi")).toInstance(scope);
        scope.bindTypes(binder(), WebCall.class, WebCallEnum.class);
        scope.bindTypesAllowingNulls(binder(), Parameters.class, BasicCredentials.class);
        
        Set<Class<?>> types = new HashSet<>();
        for (T obj : type.getEnumConstants()) {
            WebCallEnum e = obj;
            WebCall call = e.get();
            types.addAll(Arrays.asList(call.requiredTypes()));
        }
        scope.bindTypesAllowingNulls(binder(), types.toArray(new Class<?>[types.size()]));
    }
}

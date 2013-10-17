package com.mastfrog.webapi;

import com.mastfrog.webapi.builtin.BodyFromString;
import com.mastfrog.acteur.util.Method;
import com.mastfrog.webapi.builtin.ParameterFromClassNameAndToStringCamelCase;
import java.util.Map;

/**
 *
 * @author tim
 */
public enum TestAPI implements WebCallEnum {

    HELLO_WORLD(new WebCallBuilder()
                        .method(Method.GET)
                        .addRequiredType(UserId.class)
                        .withDecorator(DisplayName.class, ParameterFromClassNameAndToStringCamelCase.class)
                        .path("/users/{{userid}}/hello").responseType(Map.class)),
    ECHO(new WebCallBuilder()
                        .method(Method.POST)
                        .addRequiredType(String.class)
                        .addRequiredType(UserId.class)
                        .withDecorator(String.class, BodyFromString.class)
                        .path("/users/{{userid}}/echo").responseType(String.class));

    private final WebCall call;
    TestAPI(WebCallBuilder bldr) {
        call = bldr.id(this).build();
    }

    @Override
    public WebCall get() {
        return call;
    }
}

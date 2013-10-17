package com.mastfrog.webapi;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.mastfrog.acteur.Acteur;
import com.mastfrog.acteur.ActeurFactory;
import com.mastfrog.acteur.HttpEvent;
import com.mastfrog.acteur.Page;
import com.mastfrog.acteur.util.Method;
import java.util.Map;

/**
 *
 * @author tim
 */
public class HelloPage extends Page {
    
    @Inject
    HelloPage(ActeurFactory af) {
        add(af.matchPath("^users/.*?/hello$"));
        add(af.requireParameters("displayName"));
        add(af.matchMethods(Method.GET));
        System.out.println("Create a hello page");
        add(HelloActeur.class);
    }
    
    private static final class HelloActeur extends Acteur {
        @Inject
        HelloActeur(HttpEvent evt) {
            System.out.println("Create a hello acteur");
            String dn = evt.getParameter("displayName");
            String un = evt.getPath().getElement(1).toString();
            Map<?,?> m = ImmutableMap.builder().put("name", un).put("displayName", dn).put("message", "Hello " + dn).build();
            setState(new RespondWith(200, m));
        }
    }
}

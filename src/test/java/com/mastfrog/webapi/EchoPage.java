package com.mastfrog.webapi;

import com.google.common.net.MediaType;
import com.google.inject.Inject;
import com.mastfrog.acteur.Acteur;
import com.mastfrog.acteur.ActeurFactory;
import com.mastfrog.acteur.ContentConverter;
import com.mastfrog.acteur.Event;
import com.mastfrog.acteur.HttpEvent;
import com.mastfrog.acteur.Page;
import com.mastfrog.acteur.ResponseWriter;
import com.mastfrog.acteur.headers.Method;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import java.io.IOException;

/**
 *
 * @author tim
 */
public class EchoPage extends Page {

    @Inject
    EchoPage(ActeurFactory af) {
        add(af.matchPath("^users/.*?/echo"));
        add(af.matchMethods(Method.POST));
        add(EchoActeur.class);
    }

    private static class EchoActeur extends Acteur {

        @Inject
        EchoActeur(HttpEvent evt) throws IOException {
//            String body = evt.jsonContent(String.class);
//            System.out.println("GOT BODY: " + body);
//            setState(new RespondWith(OK, body + body + body));
            setState(new RespondWith(OK));
            setResponseWriter(EchoWriter.class);
        }
    }

    private static class EchoWriter extends ResponseWriter {

        private final String body;

        @Inject
        EchoWriter(HttpEvent evt, ContentConverter cvt) throws IOException {
            body = cvt.toObject(evt.content(), MediaType.PLAIN_TEXT_UTF_8, String.class);
            System.out.println("GOT BODY: " + body);
        }

        @Override
        public Status write(Event<?> evt, Output out, int iteration) throws Exception {
            System.out.println("CONTENT: " + body);
            out.write(body);
            return iteration == 2 ? Status.DONE : Status.NOT_DONE;
        }
    }
}

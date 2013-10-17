package com.mastfrog.webapi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author Tim Boudreau
 */
public class UserId {

    public final String id;

    @JsonCreator
    public UserId(@JsonProperty("id") String id) {
        this.id = id;
    }

    public String toString() {
        return id;
    }
}

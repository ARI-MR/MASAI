/*
 * Copyright (C) 2015 Orange
 *
 * This software is distributed under the terms and conditions of the 'GNU GENERAL PUBLIC LICENSE
 * Version 2' license which can be found in the file 'LICENSE.txt' in this package distribution or
 * at 'http://www.gnu.org/licenses/gpl-2.0-standalone.html'.
 */

package com.orange.cepheus.cep.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import java.util.*;

public class EventType {

    @NotEmpty(message = "All events must have an id")
    private String id;

    @NotEmpty(message = "All events must have a type")
    private String type;

    private boolean isPattern;

    @Valid
    @NotEmpty(message = "All events must define a list of attributes")
    private Set<Attribute> attributes; // using a set to handle equals on unordered attributes

    public EventType() {
    }

    public EventType(String id, String type, boolean isPattern) {
        this.id = id;
        this.type = type;
        this.isPattern = isPattern;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public Set<Attribute> getAttributes() {
        if (attributes == null) {
            return Collections.emptySet();
        }
        return attributes;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("isPattern")
    public boolean isPattern() {
        return isPattern;
    }

    public void setIsPattern(Boolean isPattern) {
        if (isPattern != null) {
            this.isPattern = isPattern;
        }
    }

    @JsonProperty("attributes")
    public void setAttributes(Set<Attribute> attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(Attribute attribute) {
        if (attributes == null) {
            attributes = new HashSet<>();
        }
        attributes.add(attribute);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof EventType))
            return false;

        EventType eventType = (EventType) o;

        if (isPattern != eventType.isPattern)
            return false;
        if (id != null ? !id.equals(eventType.id) : eventType.id != null)
            return false;
        if (type != null ? !type.equals(eventType.type) : eventType.type != null)
            return false;
        return !(attributes != null ? !attributes.equals(eventType.attributes) : eventType.attributes != null);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (isPattern ? 1 : 0);
        result = 31 * result + (attributes != null ? attributes.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EventType{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", isPattern=" + isPattern +
                ", attributes=" + attributes +
                '}';
    }
}

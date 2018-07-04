/*
 * Copyright (C) 2015 Orange
 *
 * This software is distributed under the terms and conditions of the 'GNU GENERAL PUBLIC LICENSE
 * Version 2' license which can be found in the file 'LICENSE.txt' in this package distribution or
 * at 'http://www.gnu.org/licenses/gpl-2.0-standalone.html'.
 */

package com.orange.cepheus.cep.model;

import org.hibernate.validator.constraints.NotEmpty;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Attribute definition for EventType
 */
public class Attribute {

    @NotEmpty(message = "All attributes must have a name")
    private String name;

    @NotEmpty(message = "All attributes must have a type")
    private String type;

    private Set<Metadata> metadata = Collections.emptySet();

    private String jsonpath;

    public Attribute() {
    }

    public Attribute(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Set<Metadata> getMetadata() {
        return metadata;
    }

    public void setMetadata(Set<Metadata> metadata) {
        if (metadata == null) {
            metadata = Collections.emptySet();
        }
        this.metadata = metadata;
    }

    public String getJsonpath() {
        return jsonpath;
    }

    public void setJsonpath(String jsonpath) {
        this.jsonpath = jsonpath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Attribute))
            return false;

        Attribute attribute = (Attribute) o;

        if (name != null ? !name.equals(attribute.name) : attribute.name != null)
            return false;
        if (type != null ? !type.equals(attribute.type) : attribute.type != null)
            return false;
        if (metadata != null ? !metadata.equals(attribute.metadata) : attribute.metadata != null)
            return false;
        return !(jsonpath != null ? !jsonpath.equals(attribute.jsonpath) : attribute.jsonpath != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (metadata != null ? metadata.hashCode() : 0);
        result = 31 * result + (jsonpath != null ? jsonpath.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Attribute{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", metadata=" + metadata +
                ", jsonpath='" + jsonpath + '\'' +
                '}';
    }
}

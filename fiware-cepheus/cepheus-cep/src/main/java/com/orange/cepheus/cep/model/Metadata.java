/*
 * Copyright (C) 2015 Orange
 *
 * This software is distributed under the terms and conditions of the 'GNU GENERAL PUBLIC LICENSE
 * Version 2' license which can be found in the file 'LICENSE.txt' in this package distribution or
 * at 'http://www.gnu.org/licenses/gpl-2.0-standalone.html'.
 */

package com.orange.cepheus.cep.model;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * Metadata definition for Attributes
 */
public class Metadata {

    @NotEmpty(message = "All metadata must have a name")
    private String name;

    @NotEmpty(message = "All metadata must have a type")
    private String type;

    private String jsonpath;

    public Metadata() {
    }

    public Metadata(String name, String type) {
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
        if (!(o instanceof Metadata))
            return false;

        Metadata metadata = (Metadata) o;

        if (name != null ? !name.equals(metadata.name) : metadata.name != null)
            return false;
        if (type != null ? !type.equals(metadata.type) : metadata.type != null)
            return false;
        return !(jsonpath != null ? !jsonpath.equals(metadata.jsonpath) : metadata.jsonpath != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (jsonpath != null ? jsonpath.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "Metadata{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", jsonpath='" + jsonpath + '\'' +
                '}';
    }
}

package com.logate.openapi2asciidoc.swagger2markup.core.model;

/**
 * Parameter wrapper for Schema parameter model
 */
public class Parameter {
    private String name;
    private String in;

    public Parameter(String name, String in) {
        this.name = name;
        this.in = in;
    }

    public String getName() {
        return name;
    }

    public String getIn() {
        return in;
    }
}

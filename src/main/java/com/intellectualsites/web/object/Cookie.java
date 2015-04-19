package com.intellectualsites.web.object;

/**
 * Created 2015-04-19 for IntellectualServer
 *
 * @author Citymonstret
 */
public class Cookie {

    private String name, value;

    public Cookie(final String name, final String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    public String toString() {
        return name + "=" + value;
    }
}

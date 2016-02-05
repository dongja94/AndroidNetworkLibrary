package com.begentgroup.simplenetwork;

/**
 * Created by dongja94 on 2015-11-27.
 */
public class BasicHeader implements Header, Cloneable {

    private final String name;

    private final String value;

    public BasicHeader(final String name, final String value) {
        super();
        if (name == null) {
            throw new IllegalArgumentException("Name may not be null");
        }
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }


    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}

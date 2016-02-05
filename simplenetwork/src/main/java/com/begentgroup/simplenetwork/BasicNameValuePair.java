package com.begentgroup.simplenetwork;

/**
 * Created by dongja94 on 2015-11-27.
 */
public class BasicNameValuePair implements NameValuePair {
    String name;
    String value;
    public BasicNameValuePair(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

}

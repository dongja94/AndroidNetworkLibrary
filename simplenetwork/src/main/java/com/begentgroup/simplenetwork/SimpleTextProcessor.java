package com.begentgroup.simplenetwork;

import com.squareup.okhttp.ResponseBody;

import java.io.IOException;

/**
 * Created by dongja94 on 2015-11-30.
 */
public abstract class SimpleTextProcessor<T> extends SimpleProcessor<T> {
    @Override
    protected T process(ResponseBody body) throws IOException {
        return process(body.string());
    }

    abstract protected T process(String message) throws IOException;
}

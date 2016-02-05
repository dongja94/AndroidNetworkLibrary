package com.begentgroup.simplenetwork;

import com.squareup.okhttp.ResponseBody;

import java.io.IOException;

/**
 * Created by dongja94 on 2015-11-30.
 */
public abstract class TextProcessor<T,E> extends Processor<T,E> {
    @Override
    protected T process(ResponseBody body) throws IOException {
        return process(body.string());
    }

    @Override
    protected E errorProcess(ResponseBody body) throws IOException {
        return errorProcess(body.string());
    }

    abstract protected T process(String message) throws IOException;
    abstract protected E errorProcess(String message) throws IOException;
}

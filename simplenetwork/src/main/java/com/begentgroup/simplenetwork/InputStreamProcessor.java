package com.begentgroup.simplenetwork;

import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by dongja94 on 2015-11-30.
 */
public abstract class InputStreamProcessor<T,E> extends Processor<T,E> {
    @Override
    protected T process(ResponseBody body) throws IOException {
        return process(body.byteStream());
    }

    @Override
    protected E errorProcess(ResponseBody body) throws IOException {
        return errorProcess(body.byteStream());
    }

    abstract protected  T process(InputStream is) throws IOException;
    abstract protected E errorProcess(InputStream is) throws IOException;
}

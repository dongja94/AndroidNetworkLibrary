package com.begentgroup.simplenetwork;

import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by dongja94 on 2015-11-30.
 */
public abstract class SimpleInputStreamProcessor<T> extends SimpleProcessor<T> {
    @Override
    protected T process(ResponseBody body) throws IOException {
        return process(body.byteStream());
    }

    abstract protected T process(InputStream is) throws IOException;
}

package com.begentgroup.simplenetwork;

import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.io.Reader;

/**
 * Created by dongja94 on 2015-11-30.
 */
public abstract class SimpleReaderProcessor<T> extends SimpleProcessor<T> {
    @Override
    protected T process(ResponseBody body) throws IOException {
        return process(body.charStream());
    }
    abstract protected T process(Reader reader) throws IOException;
}

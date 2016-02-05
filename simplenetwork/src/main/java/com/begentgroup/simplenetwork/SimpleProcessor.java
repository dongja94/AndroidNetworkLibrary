package com.begentgroup.simplenetwork;

import com.squareup.okhttp.ResponseBody;

import java.io.IOException;

/**
 * Created by dongja94 on 2015-11-30.
 */
public abstract class SimpleProcessor<T> extends Processor<T, String> {

    @Override
    protected String errorProcess(ResponseBody body) throws IOException {
        return body.string();
    }
}

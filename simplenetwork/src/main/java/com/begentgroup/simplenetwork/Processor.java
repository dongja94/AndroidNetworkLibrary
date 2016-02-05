package com.begentgroup.simplenetwork;

import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by dongja94 on 2015-11-27.
 */
public abstract class Processor<T, E> {
    MainHandler mainHandler;
    HttpRequest mRequest;
    OnNetworkResultListener<T, E> mListener;
    T mResult;
    E mError;
    int mCode;
    String mMessage;
    Throwable mExecption;

    public Processor() {

    }

    public void setHandler(MainHandler handler) {
        mainHandler = handler;
    }

    void processResponse(Response response) {
        mCode = response.code();
        ResponseBody body = response.body();
        mMessage = response.message();
        InputStream is = null;
        try {
            if (mCode < 300) {
                mResult = process(body);
                if (mainHandler != null) {
                    mainHandler.sendSuccess(this);
                }
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            mExecption = e;
        }
        if (mExecption == null) {
            try {
                mError = errorProcess(body);
            } catch (IOException e) {
                e.printStackTrace();
                mExecption = e;
            }
        }
        if (mainHandler != null) {
            mainHandler.sendFail(this);
        }
    }

    abstract protected T process(ResponseBody body) throws IOException;

    abstract protected E errorProcess(ResponseBody body) throws IOException;

    void sendSuccess() {
        if (mListener != null && !mRequest.isCancel) {
            mListener.onSuccess(mRequest, mResult);
        }
    }

    void sendFail() {
        if (mListener != null && !mRequest.isCancel) {
            mListener.onFail(mRequest, mCode, mMessage, mError, mExecption);
        }
    }

    void sendProgress() {

    }
}

package com.begentgroup.simplenetwork;

/**
 * Created by dongja94 on 2015-11-27.
 */
public abstract class SimpleResultListener<T> implements OnNetworkResultListener<T,String> {
    @Override
    abstract public void onSuccess(HttpRequest request, T result);

    @Override
    public void onProgress(HttpRequest request, int progress, int total) {

    }

    @Override
    public void onFail(HttpRequest request, int code, String message, String error, Throwable exception) {

    }
}

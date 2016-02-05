package com.begentgroup.simplenetwork;

/**
 * Created by dongja94 on 2015-11-27.
 */
public interface OnNetworkResultListener<T,E> {
    public void onSuccess(HttpRequest request, T result);
    public void onProgress(HttpRequest request, int progress, int total);
    public void onFail(HttpRequest request, int code, String message, E error, Throwable exception);
}

package com.example.dongja94.androidnetworklibrary;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Created by dongja94 on 2015-12-09.
 */
public class NetworkManager {
    private static NetworkManager instance;
    public static NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }
    OkHttpClient mClient;

    private NetworkManager() {
        mClient = new OkHttpClient();
    }

    public interface OnResultListener<T> {
        public void onSuccess(Request request, T result);
        public void onFail(Request request, IOException exception);
    }

    private static final int MESSAGE_SUCCESS = 1;
    private static final int MESSAGE_FAIL = 2;

    private static class Result<T> {
        OnResultListener<T> listener;
        Request request;
        T result;
        IOException exception;
    }

    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            Result result = (Result)msg.obj;
            switch (msg.what) {
                case MESSAGE_SUCCESS :
                    result.listener.onSuccess(result.request, result.result);
                    break;
                case MESSAGE_FAIL :
                    result.listener.onFail(result.request, result.exception);
                    break;
            }
        }
    };

    public Request getNaverMovie(String keyword, int start, int display, OnResultListener<String> listener) {
        Request.Builder builder = new Request.Builder();
        builder.url("...");
        Request request = builder.build();
        final Result<String> result = new Result<String>();
        result.listener = listener;
        result.request = request;
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                result.exception = e;
                mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_FAIL, result));
            }

            @Override
            public void onResponse(Response response) throws IOException {
                result.result = response.body().string();
                mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_SUCCESS, result));
            }
        });
        return request;
    }
}

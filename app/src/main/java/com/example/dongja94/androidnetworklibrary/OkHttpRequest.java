package com.example.dongja94.androidnetworklibrary;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okio.BufferedSink;
import okio.Okio;

/**
 * Created by dongja94 on 2015-12-09.
 */
public abstract class OkHttpRequest<T> extends Request<T> {
    Response.Listener<T> mListener;
    com.squareup.okhttp.Request mRequest;
    public OkHttpRequest(com.squareup.okhttp.Request request, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(Method.DEPRECATED_GET_OR_POST, request.urlString(), errorListener);
        mListener = listener;
        mRequest = request;
    }

    public com.squareup.okhttp.Request getRequest() {
        return mRequest;
    }

    @Override
    protected void deliverResponse(T response) {
        mListener.onResponse(response);
    }

    @Override
    public int getMethod() {
        return convertStringToIntMethod(mRequest.method());
    }

    @Override
    public String getBodyContentType() {
        if (mRequest.body() != null) {
            return mRequest.body().contentType().toString();
        }
        return super.getBodyContentType();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        if (mRequest.body() != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BufferedSink bufferedSink = Okio.buffer(Okio.sink(baos));
            try {
                mRequest.body().writeTo(bufferedSink);
                return baos.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return super.getBody();
    }

    public int convertStringToIntMethod(String method) {
        if ("GET".equals(method)) {
            return Method.GET;
        } else if ("POST".equals(method)) {
            return Method.POST;
        } else if ("PUT".equals(method)) {
            return Method.PUT;
        } else if ("DELETE".equals(method)) {
            return Method.DELETE;
        } else if ("HEAD".equals(method)) {
            return Method.HEAD;
        } else if ("OPTIONS".equals(method)) {
            return Method.OPTIONS;
        } else if ("TRACE".equals(method)) {
            return Method.TRACE;
        } else if ("PATCH".equals(method)) {
            return Method.PATCH;
        }
        return Method.DEPRECATED_GET_OR_POST;
    }

    public String convertIntToStringMethod(int method) {
        switch(method) {
            case Method.GET : return "GET";
            case Method.POST : return "POST";
            case Method.PUT: return "PUT";
            case Method.DELETE : return "DELETE";
            case Method.HEAD : return "HEAD";
            case Method.PATCH : return "PATCH";
            case Method.TRACE : return "TRACE";
            case Method.OPTIONS : return "OPTIONS";
        }
        return "GET";
    }
}

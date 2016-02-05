package com.example.dongja94.androidnetworklibrary;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.io.UnsupportedEncodingException;

/**
 * Created by dongja94 on 2015-12-09.
 */
public class PostRequest extends StringRequest {
    String contentType;
    String body;
    public PostRequest(String url, String cotentType, String body, Response.Listener<String> listener,
                       Response.ErrorListener errorListener) {
        super(Method.POST, url, listener, errorListener);
        this.contentType = contentType;
        this.body = body;
    }

    @Override
    public String getBodyContentType() {
        return contentType + "; charset=" + getParamsEncoding();
    }

    @Override
    public byte[] getBody()  throws AuthFailureError {
        try {
            return body.getBytes(getParamsEncoding());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}

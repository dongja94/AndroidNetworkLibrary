package com.example.dongja94.androidnetworklibrary;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dongja94 on 2015-12-05.
 */
public class HeaderRequest extends StringRequest {
    public HeaderRequest(int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    public HeaderRequest(String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(url, listener, errorListener);
    }

    Map<String,String> headers = new HashMap<String,String>();
    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }
}

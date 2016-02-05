package com.example.dongja94.androidnetworklibrary;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dongja94 on 2015-12-05.
 */
public class PostFormRequest extends StringRequest {
    public PostFormRequest(String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(Method.POST, url, listener, errorListener);
    }

    Map<String,String> params = new HashMap<String,String>();

    public void putParam(String key,String value) {
        params.put(key, value);
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }
}

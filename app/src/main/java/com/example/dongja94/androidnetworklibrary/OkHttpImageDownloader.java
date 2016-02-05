package com.example.dongja94.androidnetworklibrary;

import android.content.Context;

import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by dongja94 on 2015-12-02.
 */
public class OkHttpImageDownloader extends BaseImageDownloader {
    OkHttpClient client;
    public OkHttpImageDownloader(Context context) {
        this(context, new OkHttpClient());
    }
    public OkHttpImageDownloader(Context context, OkHttpClient client) {
        super(context);
        this.client = client;
    }

    @Override
    protected InputStream getStreamFromNetwork(String imageUri, Object extra) throws IOException {
        Request request = new Request.Builder().url(imageUri).build();
        Response response = client.newCall(request).execute();
        return response.body().byteStream();
    }
}

package com.begentgroup.simplenetwork;

import android.net.Uri;
import android.text.TextUtils;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by dongja94 on 2015-11-27.
 */
public class HttpRequest<T,E> {
    Request mRequest;
    Processor<T,E> mProcessor;
    OnNetworkResultListener<T,E> mListener;
    Call mCall;
    URL mUrl;
    boolean isCancel = false;
    public boolean isCancel() {
        if (mCall != null) {
            return mCall.isCanceled();
        }
        return isCancel;
    }

    public void cancel() {
        if (isCancel) return;
        isCancel = true;
        if (mCall != null) {
            mCall.cancel();
        }
    }

    public URL getURL() {
        return mUrl;
    }

    public Request getRequest() {
        return mRequest;
    }

    public Request buildGetRequest(String url) throws MalformedURLException {
        return buildGetRequest(url, null, null, null);
    }
    public Request buildGetRequest(String url, Object tag) throws MalformedURLException {
        return buildGetRequest(url, null, null, tag);
    }

    public Request buildGetRequest(String url, List<Header> headers, Object tag) throws  MalformedURLException {
        return buildGetRequest(url, headers, null, tag);
    }

    public Request buildGetRequest(String url, List<Header> headers, RequestParams params, Object tag) throws MalformedURLException {
        if (params != null) {
            url = getUrlWithQueryString(true, url, params);
        }
        Request.Builder builder = makeRequestBuilder(url, headers, tag);
        mRequest = builder.build();
        return mRequest;
    }

    public Request buildPostRequest(String url, RequestParams params) throws MalformedURLException {
        return buildPostRequest(url, null, params, null);
    }

    public Request buildPostRequest(String url, RequestParams params, Object tag) throws MalformedURLException {
        return buildPostRequest(url, null, params, tag);
    }

    public Request buildPostRequest(String url, List<Header> headers, RequestParams params) throws MalformedURLException {
        return buildPostRequest(url, headers, params, null);
    }
    public Request buildPostRequest(String url, List<Header> headers, RequestParams params, Object tag) throws MalformedURLException {
        url = getUrlApplyPathParameter(url, params);
        return buildPostRequest(url, headers, paramsToRequestBody(params), tag);
    }

    public Request buildPostRequest(String url, List<Header> headers, String contentType, String body, Object tag) throws MalformedURLException {
        RequestBody requestBody = RequestBody.create(MediaType.parse(contentType), body);
        return buildPostRequest(url, headers, requestBody, tag);
    }

    public Request buildPostRequest(String url, List<Header> headers, RequestBody requestBody, Object tag) throws MalformedURLException {
        Request.Builder builder = makeRequestBuilder(url, headers, tag);
        builder.post(requestBody);
        mRequest = builder.build();
        return mRequest;
    }

    public Request buildPutRequest(String url, List<Header> headers, RequestParams params, Object tag) throws MalformedURLException {
        url = getUrlApplyPathParameter(url, params);
        return buildPutRequest(url, headers, paramsToRequestBody(params), tag);
    }

    public Request buildPutRequest(String url, List<Header> headers, String contentType, String body, Object tag) throws MalformedURLException {
        RequestBody requestBody = RequestBody.create(MediaType.parse(contentType), body);
        return buildPutRequest(url, headers, requestBody, tag);
    }

    public Request buildPutRequest(String url, List<Header> headers, RequestBody requestBody, Object tag) throws MalformedURLException {
        Request.Builder builder = makeRequestBuilder(url, headers, tag);
        builder.put(requestBody);
        mRequest = builder.build();
        return mRequest;
    }

    public Request buildDeleteRequest(String url, Object tag) throws MalformedURLException {
        return buildDeleteRequest(url, null, null, tag);
    }

    public Request buildDeleteRequest(String url, RequestParams params, Object tag) throws MalformedURLException {
        return buildDeleteRequest(url, null, params, tag);
    }

    public Request buildDeleteRequest(String url, List<Header> headers, RequestParams params, Object tag) throws MalformedURLException {
        if (params != null) {
            url = getUrlWithQueryString(true, url, params);
        }
        Request.Builder builder = makeRequestBuilder(url, headers, tag);
        mRequest = builder.build();
        return mRequest;
    }

    private Request.Builder makeRequestBuilder(String url, List<Header> headers, Object tag) throws MalformedURLException {
        mUrl = new URL(url);
        Request.Builder builder = new Request.Builder();
        builder.url(mUrl);
        if (headers != null) {
            for (Header header : headers) {
                builder.header(header.getName(), header.getValue());
            }
        }
        if (tag != null) {
            builder.tag(tag);
        }
        return builder;
    }


    private static RequestBody paramsToRequestBody(RequestParams params) {
        RequestBody body = null;
        try {
            if (params != null) {
                body = params.getRequestBody();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return body;
    }

    private static String getUrlApplyPathParameter(String url, RequestParams params) {
        Uri uri = Uri.parse(url);
        List<String> pathSegmentList = uri.getPathSegments();
        StringBuilder sb = new StringBuilder();
        sb.append(uri.getScheme()).append("://").append(uri.getAuthority());

        if (params != null) {
            for (int i = 0; i < pathSegmentList.size(); i++) {
                String path = pathSegmentList.get(i);
                String value = path;
                if (path.startsWith(":")) {
                    String name = path.substring(1);
                    value = params.getValue(name);
                    if (value == null)
                        throw new IllegalArgumentException("Not Found value of " + name + " from RequestParams");
                    params.remove(name);
                }
                sb.append("/").append(value);
            }
        }
        if (!TextUtils.isEmpty(uri.getQuery())) {
            sb.append("?").append(uri.getQuery());
        }
        return sb.toString();
    }

    private static String getUrlWithQueryString(boolean shouldEncodeUrl, String url, RequestParams params) {
        if (shouldEncodeUrl)
            url = url.replace(" ", "%20");

        url = getUrlApplyPathParameter(url, params);

        if (params != null) {
            String paramString = params.getParamString().trim();
            if (!paramString.equals("") && !paramString.equals("?")) {
                url += url.contains("?") ? "&" : "?";
                url += paramString;
            }
        }

        return url;
    }
}

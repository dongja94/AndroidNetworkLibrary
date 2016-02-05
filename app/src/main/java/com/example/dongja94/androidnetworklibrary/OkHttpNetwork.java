package com.example.dongja94.androidnetworklibrary;

import android.os.SystemClock;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;

import org.apache.http.conn.ConnectTimeoutException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

/**
 * Created by dongja94 on 2015-12-09.
 */
public class OkHttpNetwork implements Network {
    OkHttpClient mClient;

    public OkHttpNetwork() {
        this(new OkHttpClient());
    }

    public OkHttpNetwork(OkHttpClient client) {
        if (client == null) {
            client = new OkHttpClient();
        }
        mClient = client;
    }

    public OkHttpClient getOkHttpClient() {
        return mClient;
    }

    @Override
    public NetworkResponse performRequest(Request<?> request) throws VolleyError {
        long requestStart = SystemClock.elapsedRealtime();
        com.squareup.okhttp.Response response = null;

        while (true) {
            byte[] responseContents = null;
            Map<String, String> responseHeaders = Collections.emptyMap();
            try {
                Map<String, String> headers = new HashMap<String, String>();
                addCacheHeaders(headers, request.getCacheEntry());

                com.squareup.okhttp.Request okRequest;
                if (request instanceof OkHttpRequest) {
                    okRequest = ((OkHttpRequest) request).getRequest();
                } else {
                    okRequest = convertRequest(request, headers);
                }

                response = mClient.newCall(okRequest).execute();

                int statusCode = response.code();

                responseHeaders = convertHeaders(response.headers());

                if (statusCode == HttpURLConnection.HTTP_NOT_MODIFIED) {

                    Cache.Entry entry = request.getCacheEntry();
                    if (entry == null) {
                        return new NetworkResponse(HttpURLConnection.HTTP_NOT_MODIFIED, null,
                                responseHeaders, true,
                                SystemClock.elapsedRealtime() - requestStart);
                    }
                    entry.responseHeaders.putAll(responseHeaders);
                    return new NetworkResponse(HttpURLConnection.HTTP_NOT_MODIFIED, entry.data,
                            entry.responseHeaders, true,
                            SystemClock.elapsedRealtime() - requestStart);
                }

                if (response.body() != null) {
                    responseContents = response.body().bytes();
                } else {
                    responseContents = new byte[0];
                }
                if (statusCode < 200 || statusCode > 299) {
                    throw new IOException();
                }
                return new NetworkResponse(statusCode, responseContents, responseHeaders, false,
                        SystemClock.elapsedRealtime() - requestStart);
            } catch (SocketTimeoutException e) {
                attemptRetryOnException(request, new TimeoutError());
            } catch (ConnectTimeoutException e) {
                attemptRetryOnException(request, new TimeoutError());
            } catch (MalformedURLException e) {
                throw new RuntimeException("Bad URL " + request.getUrl(), e);
            } catch (IOException e) {
                int statusCode = 0;
                NetworkResponse networkResponse = null;
                if (response != null) {
                    statusCode = response.code();
                } else {
                    throw new NoConnectionError(e);
                }
                VolleyLog.e("Unexpected response code %d for %s", statusCode, request.getUrl());
                if (responseContents != null) {
                    networkResponse = new NetworkResponse(statusCode, responseContents,
                            responseHeaders, false, SystemClock.elapsedRealtime() - requestStart);
                    if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED ||
                            statusCode == HttpURLConnection.HTTP_FORBIDDEN) {
                        attemptRetryOnException(request, new AuthFailureError(networkResponse));
                    } else {
                        throw new ServerError(networkResponse);
                    }
                } else {
                    throw new NetworkError(networkResponse);
                }
            }

        }
    }

    protected static Map<String, String> convertHeaders(Headers headers) {
        Map<String, String> result = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
        for (int i = 0; i < headers.size(); i++) {
            result.put(headers.name(i), headers.value(i));
        }
        return result;
    }

    private static void attemptRetryOnException(Request<?> request,
                                                VolleyError exception) throws VolleyError {
        RetryPolicy retryPolicy = request.getRetryPolicy();
        try {
            retryPolicy.retry(exception);
        } catch (VolleyError e) {
            throw e;
        }
    }

    private com.squareup.okhttp.Request convertRequest(Request<?> request, Map<String, String> headers) throws AuthFailureError {
        com.squareup.okhttp.Request.Builder builder = new com.squareup.okhttp.Request.Builder();
        builder.url(request.getUrl());
        HashMap<String, String> map = new HashMap<String, String>();
        map.putAll(request.getHeaders());
        map.putAll(headers);
        for (String key : map.keySet()) {
            builder.addHeader(key, map.get(key));
        }

        switch (request.getMethod()) {
            case Request.Method.GET:
                builder.get();
                break;
            case Request.Method.POST:
                builder.post(RequestBody.create(MediaType.parse(request.getBodyContentType()), request.getBody()));
                break;
            case Request.Method.PUT:
                builder.put(RequestBody.create(MediaType.parse(request.getBodyContentType()), request.getBody()));
                break;
            case Request.Method.DELETE:
                builder.delete();
                break;
            case Request.Method.PATCH:
                builder.patch(RequestBody.create(MediaType.parse(request.getBodyContentType()), request.getBody()));
                break;
            case Request.Method.HEAD:
                builder.head();
                break;
            case Request.Method.OPTIONS:
                builder.method("OPTIONS", null);
                break;
            case Request.Method.TRACE:
                builder.method("TRACE", RequestBody.create(MediaType.parse(request.getBodyContentType()), request.getBody()));
                break;
            default:
                byte[] body = request.getBody();
                if (body == null) {
                    builder.get();
                } else {
                    builder.post(RequestBody.create(MediaType.parse(request.getBodyContentType()), request.getBody()));
                }
                break;
        }
        return builder.build();
    }

    private void addCacheHeaders(Map<String, String> headers, Cache.Entry entry) {
        if (entry == null) {
            return;
        }
        if (entry.etag != null) {
            headers.put("If-None-Match", entry.etag);
        }
        if (entry.lastModified > 0) {
            Date refTime = new Date(entry.lastModified);
            headers.put("If-Modified-Since", formatDate(refTime));
        }
    }

    public static final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";
    SimpleDateFormat formatter;

    private String formatDate(Date date) {
        if (formatter == null) {
            formatter = new SimpleDateFormat(PATTERN_RFC1123, Locale.US);
            formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        }
        return formatter.format(date);
    }

}

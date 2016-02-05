package com.begentgroup.simplenetwork;

import android.content.Context;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.MalformedURLException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by dongja94 on 2015-11-27.
 */
public class HttpClient {
    private final OkHttpClient client = new OkHttpClient();
    private static final String DEFALUT_CACHE_DIRECTORY = "cache_directory";
    private static final int DEFAULT_CACHE_SIZE = 10 * 1024 * 1024;
    private MainHandler mainHandler = new MainHandler();

    public HttpClient() {
        this(null, false, false, false);
    }

    public HttpClient(Context context) {
        this(context, true, true, false);
    }

    public HttpClient(Context context, boolean enableCookie) {
        this(context, enableCookie, true, false);
    }

    public HttpClient(Context context, boolean enableCookie, boolean enableCache) {
        this(context, enableCookie, enableCache, false);
    }

    public HttpClient(boolean disableCertificateValidation) {
        this(null, false, false, true);
    }

    public HttpClient(Context context, boolean enableCookie, boolean enableCache, boolean disableCertificateValidation) {
        if (enableCookie) {
            if (context != null) {
                client.setCookieHandler(new CookieManager(new PersistentCookieStore(context), CookiePolicy.ACCEPT_ALL));
            } else {
                client.setCookieHandler(new CookieManager());
            }
        }
        if (context != null && enableCache) {
            File dir = new File(context.getCacheDir(), DEFALUT_CACHE_DIRECTORY);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            Cache cache = new Cache(dir, DEFAULT_CACHE_SIZE);
            client.setCache(cache);
        }
        if (disableCertificateValidation) {
            disableCertificateValidation(client);
        }
    }

    static void disableCertificateValidation(OkHttpClient client) {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }};

        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            client.setSslSocketFactory(sc.getSocketFactory());
            client.setHostnameVerifier(hv);
        } catch (Exception e) {
        }
    }

    public <T,E> HttpRequest<T,E> get(String url, Processor<T,E> processor, OnNetworkResultListener<T,E> listener) throws MalformedURLException {
        return get(null, url, null, null, processor, listener);
    }

    public <T,E> HttpRequest<T,E> get(Context context, String url, Processor<T,E> processor, OnNetworkResultListener<T,E> listener) throws MalformedURLException {
        return get(context, url, null, null, processor, listener);
    }

    public <T,E> HttpRequest<T,E> get(Context context, String url, RequestParams params, Processor<T,E> processor, OnNetworkResultListener<T,E> listener) throws MalformedURLException {
        return get(context, url, null, params, processor, listener);
    }

    public <T,E> HttpRequest<T,E> get(Context context, String url, List<Header> headers, RequestParams params, Processor<T,E> processor, OnNetworkResultListener<T,E> listener) throws MalformedURLException {
        HttpRequest<T,E> httpRequest = new HttpRequest<T,E>();
        httpRequest.buildGetRequest(url, headers, params, context);
        return callAsync(httpRequest, processor, listener);
    }

    public <T,E> HttpRequest<T,E> callAsync(Request request, Processor<T,E> processor, OnNetworkResultListener<T,E> listener) {
        final HttpRequest<T,E> httpRequest = new HttpRequest<T,E>();
        httpRequest.mRequest = request;
        return callAsync(httpRequest, processor, listener);
    }

    public <T,E> HttpRequest<T,E> callAsync(final HttpRequest<T,E> httpRequest, Processor<T,E> processor, OnNetworkResultListener<T,E> listener) {
        httpRequest.mProcessor = processor;
        httpRequest.mListener = listener;
        Call call = client.newCall(httpRequest.getRequest());
        httpRequest.mCall = call;
        processor.mRequest = httpRequest;
        processor.mListener = listener;
        processor.mainHandler = mainHandler;
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                httpRequest.mProcessor.mExecption = e;
                mainHandler.sendFail(httpRequest.mProcessor);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                httpRequest.mProcessor.processResponse(response);
            }
        });
        return httpRequest;
    }

    public <T,E> T callSync(Request request, Processor<T,E> processor) throws  Throwable {
        Call call = client.newCall(request);
        Response resonse = call.execute();
        processor.processResponse(resonse);
        if (processor.mExecption != null) {
            throw processor.mExecption;
        }
        if (processor.mError != null) {
            throw new IOException(processor.mMessage);
        }
        return processor.mResult;
    }
}

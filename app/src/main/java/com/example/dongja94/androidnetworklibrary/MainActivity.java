package com.example.dongja94.androidnetworklibrary;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.util.LruCache;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Network;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.begentgroup.simplenetwork.HttpClient;
import com.begentgroup.simplenetwork.HttpRequest;
import com.begentgroup.simplenetwork.RequestParams;
import com.begentgroup.simplenetwork.SimpleResultListener;
import com.begentgroup.simplenetwork.SimpleTextProcessor;
import com.squareup.okhttp.OkHttpClient;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.http.Field;
import retrofit.http.POST;

public class MainActivity extends AppCompatActivity {

    HttpClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mClient = new HttpClient();
        Button btn = (Button)findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestParams params = new RequestParams();
                params.put("search", "keyword");
                params.put("count", 10);
                try {
                    mClient.get(MainActivity.this, "http://www.google.com/:search", params, new SimpleTextProcessor<String>() {
                        @Override
                        protected String process(String message) throws IOException {
                            return message;
                        }
                    }, new SimpleResultListener<String>() {
                        @Override
                        public void onSuccess(HttpRequest request, String result) {
                            Toast.makeText(MainActivity.this, "message : " + result, Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        });
        mRequestQueue = Volley.newRequestQueue(this);
    }

    RequestQueue mRequestQueue;
    private void useVolley() {
        mRequestQueue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, "http://www.google.com", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        mRequestQueue.add(request);
    }
    private static final String DEFAULT_CACHE_DIR = "volley";

    public void useVolleyOnOkHttp() {
        File cacheDir = new File(getCacheDir(), DEFAULT_CACHE_DIR);
        Network network = new OkHttpNetwork();

        RequestQueue queue = new RequestQueue(new DiskBasedCache(cacheDir), network);
        queue.start();

        com.squareup.okhttp.Request.Builder builder = new com.squareup.okhttp.Request.Builder();
        builder.url("http://www.google.com");
        queue.add(new OkHttpRequest<String>(builder.build(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String parsed;
                try {
                    parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                } catch (UnsupportedEncodingException e) {
                    parsed = new String(response.data);
                }
                return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
            }
        });
        mRequestQueue = queue;
    }

    ImageLoader mImageLoader;

    public void useImageLoader() {
        mImageLoader = new ImageLoader(mRequestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<String, Bitmap>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });

        String IMAGE_URL = "";
        ImageView mImageView = null;
        mImageLoader.get(IMAGE_URL, ImageLoader.getImageListener(mImageView,
                R.mipmap.ic_launcher, R.mipmap.ic_launcher));

        NetworkImageView networkImageView = null;
        networkImageView.setImageUrl(IMAGE_URL, mImageLoader);
    }

    public interface UserManager {
        @POST("/user/modify")
        Call<String> modifyUser(@Field(value = "name",encoded = true) String name);
    }
    private void useRetrofit() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl("http://www.google.com").client(new OkHttpClient()).addConverterFactory(GsonConverterFactory.create()).build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

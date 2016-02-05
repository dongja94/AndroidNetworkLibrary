package com.example.dongja94.androidnetworklibrary;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.ExternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;

/**
 * Created by dongja94 on 2015-12-02.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        setupDefaultPicasso(this);
    }

    private static final void setupDefaultPicasso(Context context) {
        Picasso.with(context);
    }

    private static final void setupCustomPicasso(Context context) {
        Picasso.Builder builder = new Picasso.Builder(context);
        builder.defaultBitmapConfig(Bitmap.Config.RGB_565);
        builder.downloader(new OkHttpDownloader(new OkHttpClient()));
        builder.requestTransformer(new Picasso.RequestTransformer() {
            @Override
            public Request transformRequest(Request request) {
                Request.Builder builder = request.buildUpon();
                builder.centerCrop();
                return builder.build();
            }
        });
        builder.memoryCache(new LruCache(context));
        builder.loggingEnabled(true);
        Picasso.setSingletonInstance(builder.build());
    }

    private static final int DEFAULT_DISK_CACHE_SIZE = 250 * 1024 * 1024;
    private static final int DEFAULT_MEMORY_CACHE_SIZE = 10 * 1024 * 1024;
    private static final int DEFAULT_BITMAP_POOL_SIZE = 10 * 1024 * 1024;
    private static final String DEFAULT_DISK_CACHE_DIR = "image_manager_disk_cache";
    private static final boolean isInternalCache = true;
    private static final void setupCustomGlide(Context context) {
        GlideBuilder builder = new GlideBuilder(context);
        if (isInternalCache) {
            builder.setDiskCache(new InternalCacheDiskCacheFactory(context, DEFAULT_DISK_CACHE_DIR, DEFAULT_DISK_CACHE_SIZE));
        } else {
            builder.setDiskCache(new ExternalCacheDiskCacheFactory(context, DEFAULT_DISK_CACHE_DIR, DEFAULT_DISK_CACHE_SIZE));
        }
        builder.setMemoryCache(new LruResourceCache(DEFAULT_MEMORY_CACHE_SIZE));
        builder.setBitmapPool(new LruBitmapPool(DEFAULT_BITMAP_POOL_SIZE));
        builder.setDecodeFormat(DecodeFormat.ALWAYS_ARGB_8888);
        Glide.setup(builder);
    }
}

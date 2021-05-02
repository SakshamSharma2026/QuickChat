package com.codewithshadow.quickchat.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.codewithshadow.quickchat.R;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class UniversalImageLoderClass {
    private static final int defaultImage = R.color.gray3;
    private Context mcontext;

    public UniversalImageLoderClass(Context context)
    {
        mcontext = context;
    }

    public ImageLoaderConfiguration getConfig()
    {
        DisplayImageOptions displayImageOptions = new Builder()
                .showImageOnLoading(defaultImage)
                .showImageForEmptyUri(defaultImage)
                .showImageOnFail(defaultImage)
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .resetViewBeforeLoading(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300))
                .build();


        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(mcontext)
                .defaultDisplayImageOptions(displayImageOptions)
                .memoryCache(new WeakMemoryCache())
                .diskCacheSize(100 * 1024 * 1024).build();


        return configuration;

    }


    public static void  setImage(String imgUrl, ImageView imageView,final ProgressBar mProgressBar)
    {
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage( imgUrl, imageView, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                if (mProgressBar!=null)
                {
                    mProgressBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                if (mProgressBar!=null)
                {
                    mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if (mProgressBar!=null)
                {
                    mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                if (mProgressBar!=null)
                {
                    mProgressBar.setVisibility(View.GONE);
                }
            }
        });
    }
}

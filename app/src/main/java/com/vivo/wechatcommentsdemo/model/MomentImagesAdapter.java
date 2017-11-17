package com.vivo.wechatcommentsdemo.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.vivo.wechatcommentsdemo.R;

import java.util.List;

/**
 * Created by Administrator on 2017/11/17.
 */

public class MomentImagesAdapter extends BaseAdapter {

    private static final String TAG = "MomentImagesAdapter";

    private List<Tweet.Url> mUrlsList;
    private LayoutInflater mInflater;
    private ImageLoader mLoader;

    public MomentImagesAdapter(Context context, List<Tweet.Url> urlsList) {
        mInflater = LayoutInflater.from(context);
        mUrlsList = urlsList;
        mLoader = ImageLoader.getInstance();
    }

    @Override
    public int getCount() {
        return mUrlsList == null ? 0 : mUrlsList.size();
    }

    @Override
    public String getItem(int i) {
        return mUrlsList.get(i).getUrl();
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        GridViewHolder holder;
        if (view == null) {
            holder = new GridViewHolder();
            view = mInflater.inflate(R.layout.moments_images_grid_view, viewGroup, false);
            holder.imageViewTweetImage = (ImageView) view
                    .findViewById(R.id.imageViewTweetImage);
            view.setTag(holder);
        } else {
            holder = (GridViewHolder) view.getTag();
        }
        mLoader.displayImage(getItem(i), holder.imageViewTweetImage, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                Log.d(TAG, "load fail, uri " + imageUri + ", reason " + failReason.getType() + "/" + failReason.getCause());
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                Log.d(TAG, "load complete uri " + imageUri);
            }
        });

        return view;
    }

    private static class GridViewHolder {
        ImageView imageViewTweetImage;
    }
}

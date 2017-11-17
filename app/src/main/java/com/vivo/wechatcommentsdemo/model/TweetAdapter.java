package com.vivo.wechatcommentsdemo.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.vivo.wechatcommentsdemo.R;
import com.nostra13.universalimageloader.core.*;
import com.vivo.wechatcommentsdemo.view.NoScrollGridView;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by Administrator on 2017/11/9.
 */

public class TweetAdapter extends BaseAdapter {

    private static final String TAG = "TweetAdapter";

    private Context mContext;
    private List<Tweet> mTweetList;
    private LayoutInflater mInflater;
    private com.nostra13.universalimageloader.core.ImageLoader mLoader;

    public TweetAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mLoader = ImageLoader.getInstance();
    }

    public void setTweetData(List<Tweet> tweetList) {
        mTweetList = tweetList;
    }

    @Override
    public int getCount() {
        return mTweetList == null ? 0 : mTweetList.size();
    }

    @Override
    public Tweet getItem(int i) {
        return mTweetList == null ? null : mTweetList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = mInflater.inflate(R.layout.list_item_layout, null);
            holder.imageButtonSender = (ImageButton) view.findViewById(R.id.imageButtonSender);
            holder.textViewSenderUserName = (TextView)view.findViewById(R.id.textViewSenderUserName);
            holder.textViewSenderContent = (TextView) view.findViewById(R.id.textViewSenderContent);
            holder.gridView = (NoScrollGridView) view.findViewById(R.id.gridViewImages);
            holder.linearLayoutCommentContent = (LinearLayout)view.findViewById(R.id.linearLayoutCommentContent);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        // begin to fill tweet data
        Tweet tweet = getItem(i);
        holder.imageButtonSender.setImageResource(R.mipmap.ic_launcher_round);
        holder.textViewSenderUserName.setText(tweet.getSender().getUsername());
        String senderContent = tweet.getContent();
        List<Tweet.Url> imagesList = tweet.getImages();
        if (senderContent == null) {
            holder.textViewSenderContent.setVisibility(View.GONE);
        } else {
            holder.textViewSenderContent.setVisibility(View.VISIBLE);
            holder.textViewSenderContent.setText(senderContent);
        }

        if (tweet.getComments() == null || tweet.getComments().size() == 0) {
            holder.linearLayoutCommentContent.setVisibility(View.GONE);
        } else {
            holder.linearLayoutCommentContent.setVisibility(View.VISIBLE);
            holder.linearLayoutCommentContent.removeAllViews();

            LinearLayout commentLayout;
            TextView textViewCommentUserName, textViewCommentContent;
            List<Tweet.Comment> commentList = tweet.getComments();
            for (int index = 0; index < commentList.size(); index++) {
                commentLayout = new LinearLayout(mContext);
                commentLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                commentLayout.setOrientation(LinearLayout.HORIZONTAL);
                commentLayout.setPadding(10, 5, 10, 5);

                textViewCommentUserName = new TextView(mContext);
                textViewCommentUserName.setText(commentList.get(index).getSender().getUsername());
                textViewCommentUserName.setTextColor(mContext.getResources().getColor(R.color.color_comment_username_text));
                textViewCommentUserName.setTextSize(14);
                commentLayout.addView(textViewCommentUserName, 0);

                textViewCommentContent = new TextView(mContext);
                textViewCommentContent.setText(String.format(mContext.getResources().getString(R.string.tweet_default_comment_content),
                                                commentList.get(index).getContent()));
                textViewCommentContent.setTextColor(mContext.getResources().getColor(R.color.colorGray));
                textViewCommentContent.setTextSize(14);
                commentLayout.addView(textViewCommentContent, 1);
                holder.linearLayoutCommentContent.addView(commentLayout);
                /*Log.d(TAG, "Sender " + mTweetList.get(i).getSender().getUsername() + ", Comment Sender name "
                    + commentList.get(index).getSender().getUsername() + ", content is "
                    + commentList.get(index).getContent()); */
            }
        }

        mLoader.displayImage(tweet.getSender().getAvatar(), holder.imageButtonSender); /*, new SimpleImageLoadingListener() {

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        Log.d(TAG, "onLoadingComplete imageUri " + imageUri);
                        if (view instanceof ImageView) {
                            ((ImageView)view).setImageBitmap(loadedImage);
                        }
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        if (view instanceof ImageView) {
                            ((ImageView)view).setImageResource(R.mipmap.ic_launcher);
                        }
                    }
                }); */

        if (imagesList == null || imagesList.size() == 0) {
            holder.gridView.setVisibility(View.GONE);
        } else {
            holder.gridView.setVisibility(View.VISIBLE);
            holder.gridView.setAdapter(new MomentImagesAdapter(mContext, tweet.getImages()));
        }

        return view;
    }

    private static class ViewHolder {
        ImageButton imageButtonSender;
        TextView textViewSenderUserName;
        TextView textViewSenderContent;
        NoScrollGridView gridView;
        LinearLayout linearLayoutCommentContent;
    }

}

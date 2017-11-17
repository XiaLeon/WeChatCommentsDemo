package com.vivo.wechatcommentsdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.vivo.wechatcommentsdemo.model.Constants;
import com.vivo.wechatcommentsdemo.model.Tweet;
import com.vivo.wechatcommentsdemo.model.TweetAdapter;
import com.vivo.wechatcommentsdemo.model.UserInfo;
import com.vivo.wechatcommentsdemo.utils.DiskCacheUtils;
import com.vivo.wechatcommentsdemo.utils.JsonParseUtils;
import com.vivo.wechatcommentsdemo.utils.NetworkUtils;
import com.vivo.wechatcommentsdemo.view.MomentsListView;

import java.util.ArrayList;
import java.util.List;

public class CommentsActivity extends Activity {

    private static final String TAG = "CommentsActivity";

    private MomentsListView mListView;
    private View mEmptyView;
    private ProgressBar mProgressBarLoading;

    private RequestQueue mQueue;
    private StringRequest mUserInfoRequest, mTweetRequest;
    private TweetAdapter mAdapter;

    private AlertDialog.Builder mDialogBuilder;
    private Gson mGson;

    private List<Tweet> mTweetList, mFirstFiveTweetsList;
    private String mTweetResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.app_name);

        mGson = new Gson();
        mQueue = Volley.newRequestQueue(CommentsActivity.this);
        Cache.Entry userInfoEntry = mQueue.getCache().get(Constants.URL_USER_INFO);
        Cache.Entry tweetEntry = mQueue.getCache().get(Constants.URL_TWEETS_CONTENT);
        Log.d(TAG, "tweetEntry " + tweetEntry + ", userInfoEntry " + userInfoEntry
                + ", network connected ? " + NetworkUtils.isNetworkConnected(this));

        if (!NetworkUtils.isNetworkConnected(this) && userInfoEntry == null && tweetEntry == null) {
            setContentView(R.layout.empty_view);
            showNoNetDialog();
        } else {
            setContentView(R.layout.main_activity_layout);
            initView();

            if (userInfoEntry == null && tweetEntry == null) {
                // This is the first time we enter main page
                mListView.setVisibility(View.INVISIBLE);
                mProgressBarLoading.setVisibility(View.VISIBLE);

                requestUserInfo();
                requestTweetInfo();
            } else {
                mProgressBarLoading.setVisibility(View.INVISIBLE);
                if (userInfoEntry != null) {
                    String userInfoResponse = new String(userInfoEntry.data);
                    UserInfo userInfo = mGson.fromJson(userInfoResponse, UserInfo.class);
                    Log.d(TAG, "userInfo is " + userInfo);
                    mListView.updateUserInfo(userInfo);
                }

                if (tweetEntry != null) {
                    mTweetResponse = new String(tweetEntry.data);
                    JsonParseUtils.parseTweetResponse(mTweetResponse, CommentsActivity.this, mListView, mGson, mTweetList);
                    mAdapter.setTweetData(mTweetList);
                    mListView.setAdapter(mAdapter);
                }
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        DiskCacheUtils.getInstance(this).setTweetNextReadPos(this, 0);
    }

    private void initView() {
        mListView = (MomentsListView)findViewById(R.id.momentsListView);
        mProgressBarLoading = (ProgressBar)findViewById(R.id.progressBarLoading);
        mEmptyView = View.inflate(CommentsActivity.this, R.layout.empty_view, null);
        mListView.setOnRefreshListener(new MomentsListView.OnRefreshListener() {

            @Override
            public void onRefresh() {
                Log.d(TAG, "get the first 5 items");
                mFirstFiveTweetsList = JsonParseUtils.parseFirstFiveTweets(mTweetResponse, mGson);
                mTweetList.addAll(0, mFirstFiveTweetsList);
                Log.d(TAG, "new size " + mTweetList.size());
                mAdapter.notifyDataSetChanged();
                //mListView.hideLoadingView();
            }

            @Override
            public void onLoadMore() {
                Log.d(TAG, "onLoadMore, mList size " + mTweetList.size());
                JsonParseUtils.parseTweetResponse(mTweetResponse, CommentsActivity.this, mListView, mGson, mTweetList);
                mAdapter.notifyDataSetChanged();
            }
        });
        if (mTweetList == null) {
            mTweetList = new ArrayList<Tweet>();
        }
        mAdapter = new TweetAdapter(this);
    }

    private void requestUserInfo() {
        mUserInfoRequest = new StringRequest(Request.Method.GET, Constants.URL_USER_INFO,
                new Response.Listener<String>() { // runs on main thread
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "userInfo response is " + response);
                        UserInfo userInfo = mGson.fromJson(response, UserInfo.class);
                        mListView.updateUserInfo(userInfo);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("TAG", error.getMessage(), error);
                        mListView.setEmptyView(mEmptyView);
                    }
                }
        );
        mQueue.add(mUserInfoRequest);
    }

    private void requestTweetInfo() {
        mTweetRequest = new StringRequest(Request.Method.GET, Constants.URL_TWEETS_CONTENT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) { // runs on main thread
                        Log.d(TAG, "onResponse tweet data");
                        mTweetResponse = response;
                        mProgressBarLoading.setVisibility(View.INVISIBLE);
                        mListView.setVisibility(View.VISIBLE);
                        //parseTweetResponse(response);
                        JsonParseUtils.parseTweetResponse(response, CommentsActivity.this, mListView, mGson, mTweetList);
                        mAdapter.setTweetData(mTweetList);
                        mListView.setAdapter(mAdapter);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("TAG", error.getMessage(), error);
                        mListView.setEmptyView(mEmptyView);
                    }
                });

        mQueue.add(mTweetRequest);
    }

    private void showNoNetDialog() {
        if (mDialogBuilder == null) {
            mDialogBuilder = new AlertDialog.Builder(this);
            mDialogBuilder.setTitle(R.string.dialog_title_no_network)
                    .setMessage(R.string.dialog_message_no_network)
                    .setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .setCancelable(false);
        }
        AlertDialog dialog = mDialogBuilder.create();
        dialog.show();
    }

}

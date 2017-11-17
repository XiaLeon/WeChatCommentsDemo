package com.vivo.wechatcommentsdemo.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.MemoryCache;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.vivo.wechatcommentsdemo.R;
import com.vivo.wechatcommentsdemo.model.UserInfo;
import com.vivo.wechatcommentsdemo.utils.DiskCacheUtils;

import java.io.File;


/**
 * Created by Administrator on 2017/11/10.
 */

public class MomentsListView extends ListView implements AbsListView.OnScrollListener{

    private static final String TAG = "MomentsListView";

    private static final int MSG_REFRESH_FINISHED = 0;

    private View mHeaderView, mFooterView;
    private ImageView mLoadingView, mBgImageView, mProfileImageView;
    private ProgressBar mProgressBar;
    private TextView mTextViewUserName, mTextViewDefaultCover;

    private int mMeasuredHeight;
    private float mStartY;

    private final int REFRESH_PULL = 0;
    private final int REFRESH_RELEASE = 1;
    private final int REFRESHING = 2;

    private ObjectAnimator mRotationAnimator;
    private float mLoadViewRotation;
    private boolean isRefresh = false;

    private OnRefreshListener listener;
    private LoadingViewHandler mMainHandler;
    private DiskCacheUtils mDiskCacheUtil;

    public MomentsListView(Context context) {
        this(context,null);
    }

    public MomentsListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MomentsListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        isRefresh = false;
        mDiskCacheUtil = DiskCacheUtils.getInstance(getContext());

        initHeaderView();
        initFooterView();
        initRotateAnimation();
        setOnScrollListener(this);

        mMainHandler = new LoadingViewHandler();
    }

    private void initHeaderView() {
        mHeaderView = View.inflate(getContext(), R.layout.list_header_view_layout, null);
        RelativeLayout relativeLayout = (RelativeLayout) mHeaderView.findViewById(R.id.layoutListHeaderView);
        mBgImageView = (ImageView)mHeaderView.findViewById(R.id.imageViewMomentsBg);
        mProfileImageView = (ImageView)mHeaderView.findViewById(R.id.imageViewUserProfile);
        mLoadingView = (ImageView)mHeaderView.findViewById(R.id.imageViewLoading);
        mTextViewUserName = (TextView)mHeaderView.findViewById(R.id.textViewUserName);
        mTextViewDefaultCover = (TextView)mHeaderView.findViewById(R.id.textViewDefaltCover);

        mHeaderView.setPadding(0, -100, 0, 0);
        addHeaderView(mHeaderView);
        mMeasuredHeight = mHeaderView.getMeasuredHeight();
        //Log.d(TAG, "mMeasuredHeight " + mMeasuredHeight);
    }

    private void initFooterView() {
        mFooterView = View.inflate(getContext(), R.layout.list_footer_view_layout, null);
        mProgressBar = (ProgressBar)mFooterView.findViewById(R.id.progressBarFooterView);
        addFooterView(mFooterView);
    }

    public void hideFooterView() {
        if (mFooterView != null) {
            mFooterView.setVisibility(View.GONE);
        }
    }

    private void initRotateAnimation() {
        mRotationAnimator = ObjectAnimator.ofFloat(mLoadingView,"Rotation",/*mLoadViewRotation/360*/0, 720).setDuration(500);
        mRotationAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mRotationAnimator.setCurrentPlayTime(3000);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float dy;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                dy = event.getRawY() - mStartY;
                //Log.d(TAG, "move Y " + event.getRawY());
                if (dy > 10 && getFirstVisiblePosition() == 0) {
                    int padding = (int) (dy - mMeasuredHeight);
                    mHeaderView.setPadding(0, padding / 2, 0, 0);
                    if (padding >= 100) {
                        mLoadingView.setY(50);
                    } else {
                        mLoadingView.setY(padding / 2);
                    }
                    mLoadingView.setVisibility(View.VISIBLE);
                    if (!mRotationAnimator.isStarted()) {
                        mRotationAnimator.start();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                dy = event.getRawY() - mStartY;
                if (getFirstVisiblePosition() == 0) {
                    mHeaderView.setPadding(0, -100, 0, 0);
                }

                Log.d(TAG, "dy is " + dy + ", firstItem " + getFirstVisiblePosition() + ", running ? "
                        + mRotationAnimator.isRunning() + ", isStarted ? ");

                if (dy >= 350 && !mRotationAnimator.isRunning() && getFirstVisiblePosition() == 0) {
                    mLoadingView.setVisibility(View.VISIBLE);
                    mRotationAnimator.start();
                    isRefresh = true;
                } else {
                    if (dy < 350 && mRotationAnimator.isRunning()) {
                        mRotationAnimator.end();
                        mLoadingView.setVisibility(View.GONE);
                        isRefresh = false;
                    }
                }
                if (mRotationAnimator.isRunning()) {
                    isRefresh = true;
                    if (listener != null) {
                        listener.onRefresh();
                    }
                }
                Log.d(TAG, "isRefresh " + isRefresh);
                if (isRefresh) {
                    mMainHandler.sendEmptyMessageDelayed(MSG_REFRESH_FINISHED, 3000);
                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    public void updateUserInfo(UserInfo userInfo) {
        mTextViewUserName.setText(userInfo.getUserName());
        mDiskCacheUtil.loadUserImage(userInfo, new DiskCacheUtils.UserImagesLoadListener() {
            @Override
            public void onUserImagesLoad(Bitmap profileBmp, Bitmap avatarBmp) {
                Log.d(TAG, "profileBmp " + profileBmp + ", avatarBmp " + avatarBmp);

                if (mTextViewDefaultCover != null) {
                    mTextViewDefaultCover.setVisibility(View.GONE);
                }

                if (profileBmp == null && avatarBmp != null) {
                    mProfileImageView.setImageBitmap(avatarBmp);
                }
                if (avatarBmp == null && profileBmp != null) {
                    mBgImageView.setImageBitmap(profileBmp);
                }

                if (mBgImageView != null && avatarBmp != null) {
                    mBgImageView.setImageBitmap(avatarBmp);
                }
                if (mProfileImageView != null && profileBmp != null) {
                    mProfileImageView.setImageBitmap(profileBmp);
                }
            }
        });
    }

    public void hideLoadingView() {
        if (mRotationAnimator.isRunning()) {
            mRotationAnimator.end();
        }
        mLoadingView.setVisibility(View.GONE);
    }

    public void setOnRefreshListener(OnRefreshListener listener){
        this.listener = listener;
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int scrollState) {
        if ((scrollState == SCROLL_STATE_IDLE || scrollState == SCROLL_STATE_FLING)
                && (getLastVisiblePosition() == getCount() -1)) {
            setSelection(getCount());
            if (listener != null){
                Log.d(TAG, "begin to load more");
                if (mFooterView != null) {
                    mFooterView.setVisibility(View.VISIBLE);
                }
                listener.onLoadMore();
            }
        }
    }

    @Override
    public void onScroll(AbsListView absListView, int i, int i1, int i2) {

    }

    public interface OnRefreshListener{
        void onRefresh();
        void onLoadMore();
    }

    class LoadingViewHandler extends Handler {
        public LoadingViewHandler() {
            super();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFRESH_FINISHED:
                    Log.d(TAG, "isRefresh ? " + isRefresh);
                    if (isRefresh) {
                        mRotationAnimator.end();
                        mLoadingView.setVisibility(View.GONE);
                        isRefresh = false;
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    }
}

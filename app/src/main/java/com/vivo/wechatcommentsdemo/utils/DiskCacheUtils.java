package com.vivo.wechatcommentsdemo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;

import com.vivo.wechatcommentsdemo.model.Constants;
import com.vivo.wechatcommentsdemo.model.UserInfo;

import org.apache.http.conn.ConnectTimeoutException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2017/11/13.
 */

public class DiskCacheUtils {

    private static final String TAG = "DiskCacheUtils";

    private static Context mContext;
    private static DiskCacheUtils mDiskCacheUtil;
    private DownloadUserImageTask mDownloadUserImageTask;

    private DiskCacheUtils() {
    }

    /*private static class InnerDiskCacheUtils {
        static final DiskCacheUtils sDiskCacheUtils = new DiskCacheUtils();
    } */

    public static DiskCacheUtils getInstance(Context context) {
        if (mDiskCacheUtil == null) {
            mContext = context;
            mDiskCacheUtil = new DiskCacheUtils();
        }
        return mDiskCacheUtil;
    }

    public int getTweetNextReadPos(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Constants.SP_TWEET_NEXT_READ_POS_FILE, Context.MODE_PRIVATE);
        return sp.getInt(Constants.KEY_TWEET_NEXT_READ_POS, 0);
    }

    public void setTweetNextReadPos(Context context, int nextPos) {
        SharedPreferences sp = context.getSharedPreferences(Constants.SP_TWEET_NEXT_READ_POS_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(Constants.KEY_TWEET_NEXT_READ_POS, nextPos);
        editor.apply();
    }

    public void loadUserImage(UserInfo userInfo, UserImagesLoadListener listener) {
        String profileImageUrl = userInfo.getProfileImageUrl();
        String avatarImageUrl = userInfo.getAvatarUrl();
        HashMap<String, Bitmap> imageMap = getUserBmpsFromDisk(profileImageUrl, avatarImageUrl);
        Log.d(TAG, "imageMap " + imageMap);
        if (imageMap != null) {
            listener.onUserImagesLoad(imageMap.get(profileImageUrl), imageMap.get(avatarImageUrl));
        } else {
            // download from cached json
            new DownloadUserImageTask(listener, profileImageUrl, avatarImageUrl).execute(profileImageUrl, avatarImageUrl);
        }
    }

    private HashMap<String, Bitmap> getUserBmpsFromDisk(String profileImageUrl, String avatarUrl) {
        HashMap<String, Bitmap> result = new HashMap<String, Bitmap>(2);
        String profileImageName = MD5Encode.encode(profileImageUrl);
        String avatarImageName = MD5Encode.encode(avatarUrl);
        File profileImageFile = new File(mContext.getCacheDir(), profileImageName);
        File avatarImageFile = new File(mContext.getCacheDir(), avatarImageName);

        Bitmap profileImage = readBmpFromDisk(profileImageFile.getPath());
        Bitmap avatarImage = readBmpFromDisk(avatarImageFile.getPath());
        Log.d(TAG, "profileImageFile path " + profileImageFile.getPath() + ", profileImage " + profileImage
            + "avatarFile path " + avatarImageFile.getPath() + ", avatarImage " + avatarImage);
        if (profileImage == null && avatarImage == null) {
            Log.d(TAG, "we haven't cached user images on disk");
            return null;
        }
        result.put(profileImageUrl, profileImage);
        result.put(avatarUrl, avatarImage);
        return result;
    }

    private Bitmap readBmpFromDisk(String path) {
        Bitmap bitmap = null;
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(
                    new FileInputStream(path));
            bitmap = BitmapFactory.decodeStream(bis);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }

    private Bitmap downloadImage(String urlString) {
        URL url = null;
        InputStream is = null;
        Bitmap bitmap = null;
        try {
            url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                is = conn.getInputStream();
                bitmap = BitmapFactory.decodeStream(is);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }

    private void saveBitmapToDisk(Bitmap bitmap, String urlString) {
        Log.d(TAG, "bitmap " + bitmap + ", urlString " + urlString);
        String fileName = MD5Encode.encode(urlString);
        File destFile = new File(mContext.getCacheDir(), fileName);
        Log.d(TAG, "destFile path " + destFile.getPath());
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(destFile.getPath()));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
            bos.flush();
        } catch (Exception e) {
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public interface UserImagesLoadListener {
        void onUserImagesLoad(Bitmap profileBmp, Bitmap avatarBmp);
    }

    private class DownloadUserImageTask extends AsyncTask<String, Void, HashMap<String, Bitmap>> {

        private UserImagesLoadListener mListener;
        private HashMap<String, Bitmap> mMap;
        private String mProfileImageUrl, mAvatarImageUrl;

        public DownloadUserImageTask(UserImagesLoadListener listener, String profileImageUrl, String avatarImageUrl) {
            mListener = listener;
            mProfileImageUrl = profileImageUrl;
            mAvatarImageUrl = avatarImageUrl;
            mMap = new HashMap<>();
        }

        @Override
        protected HashMap<String, Bitmap> doInBackground(String... urls) {
            Log.d(TAG, "url size " + urls.length);
            Bitmap bitmap;
            for (String url : urls) {
                bitmap = downloadImage(url);
                Log.d(TAG, "url " + url + ", bitmap " + bitmap);
                mMap.put(url, bitmap);
                if (bitmap != null) {
                    saveBitmapToDisk(bitmap, url);
                }
            }
            return mMap;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(HashMap<String, Bitmap> stringBitmapHashMap) {
            Log.d(TAG, "onPostExecute map " + stringBitmapHashMap);
            if (stringBitmapHashMap != null) {
                mListener.onUserImagesLoad(mMap.get(mProfileImageUrl), mMap.get(mAvatarImageUrl));
            }
        }
    }
}

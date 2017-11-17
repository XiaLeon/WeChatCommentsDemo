package com.vivo.wechatcommentsdemo.model;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.Glide;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/11/8.
 */

public class UserInfo {

    @SerializedName("username")
    String mUserName;

    @SerializedName("nick")
    String mNickName;

    @SerializedName("profile-image")
    String mProfileImageUrl;

    @SerializedName("avatar")
    String mAvatarUrl;

    public UserInfo(String userName, String nickName, String profileImageUrl, String avatarUrl) {
        mUserName = userName;
        mNickName = nickName;
        mProfileImageUrl = profileImageUrl;
        mAvatarUrl = avatarUrl;
    }

    public String getUserName() {
        return mUserName;
    }

    public String getNickName() {
        return mNickName;
    }

    public String getProfileImageUrl() {
        return mProfileImageUrl;
    }

    public String getAvatarUrl() {
        return mAvatarUrl;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("UserName: ");
        sb.append(mUserName + "; ");
        sb.append("NickName: ");
        sb.append(mNickName + "; ");
        sb.append("ProfileImageUrl: ");
        sb.append(mProfileImageUrl + "; ");
        sb.append("AvatarUrl: ");
        sb.append(mAvatarUrl);
        return sb.toString();
    }
}

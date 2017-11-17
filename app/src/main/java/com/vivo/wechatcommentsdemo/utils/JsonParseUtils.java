package com.vivo.wechatcommentsdemo.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.vivo.wechatcommentsdemo.model.Tweet;
import com.vivo.wechatcommentsdemo.view.MomentsListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/11/17.
 */

public class JsonParseUtils {

    public static List<Tweet> parseFirstFiveTweets(String response, Gson gson) {
        List<Tweet> firstFiveTweetsList = new ArrayList<>(5);
        JsonParser parser = new JsonParser();
        JsonArray jsonArray = parser.parse(response).getAsJsonArray();
        for (JsonElement element : jsonArray) {
            Tweet tweet = gson.fromJson(element, Tweet.class);
            if (TextUtils.isEmpty(tweet.getContent())
                    && (tweet.getImages() == null || tweet.getImages().size() == 0)) {
                continue;
            }
            firstFiveTweetsList.add(tweet);
            if (firstFiveTweetsList.size() == 5) {
                break;
            }
        }
        return firstFiveTweetsList;
    }

    public static void parseTweetResponse(String response, Context context, MomentsListView listView, Gson gson, List<Tweet> tweetList) {
        JsonParser parser = new JsonParser();
        JsonArray jsonArray = parser.parse(response).getAsJsonArray();
        int nextPos = DiskCacheUtils.getInstance(context).getTweetNextReadPos(context);
        //Log.d(TAG, "nextPos " + nextPos + ", json size " + jsonArray.size());
        if (nextPos == jsonArray.size()) {
            listView.hideFooterView();
            return;
        }
        int i = 0;
        for (int index = nextPos; index < jsonArray.size(); index++) {
            nextPos++;
            JsonElement element = jsonArray.get(index);
            Tweet tweet = gson.fromJson(element, Tweet.class);
            if (TextUtils.isEmpty(tweet.getContent())
                    && (tweet.getImages() == null || tweet.getImages().size() == 0)) {
                continue;
            }
            tweetList.add(tweet);
            i++;
            if (i == 5) {
                break;
            }
        }
        DiskCacheUtils.getInstance(context).setTweetNextReadPos(context, nextPos);
    }

}

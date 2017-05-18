package com.shu.wyf.wyfgraduationproject;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;

/**
 * Created by info_kerwin on 2017/4/23.
 */

public class ParseFromJsonClass {

    public String getUrl() {
        return url;
    }
    public String getIntro() {
        return intro;
    }
    private String url;
    private String intro;
    public void parseItemInfo(String jsonData) {
        try {
            JsonParser jsonParser = new JsonParser();
            JsonObject object = (JsonObject) jsonParser.parse(jsonData);
            Log.d("Gson", object.get("url").getAsString());
            Log.d("Gson", object.get("intro").getAsString());

            url = object.get("url").getAsString();
            intro = object.get("intro").getAsString();
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
    }


}
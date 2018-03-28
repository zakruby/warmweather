package com.example.hp.warmweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by HP on 2018/3/28.
 */

public class HttpUtil {
    public static void sendOkhttpRequest(String adress,okhttp3.Callback callback){
        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder().url(adress).build();
        client.newCall(request).enqueue(callback);
    }
}

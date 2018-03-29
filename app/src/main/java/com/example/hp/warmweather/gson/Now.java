package com.example.hp.warmweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by HP on 2018/3/29.
 */

public class Now {
    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;
    public class More{
        @SerializedName("txt")
        public String info;
    }
}

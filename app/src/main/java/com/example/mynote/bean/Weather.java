package com.example.mynote.bean;


import com.google.gson.annotations.SerializedName;

public class Weather {

    public String status;

    public Now now;
    public class Now{
        @SerializedName("tmp")
        public String temperature;

        @SerializedName("cond")
        public More more;

        public class More{
            @SerializedName("txt")
            public String info;
        }
    }
}

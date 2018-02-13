package com.ustech.bloodhope.Utils;

import android.support.multidex.MultiDexApplication;

/**
 * Created by Fahad on 2/6/2018.
 */

public class MyApplication extends MultiDexApplication {
    private String user_Name = "";

    public String getUser_Name() {
        return user_Name;
    }

    public void setUser_Name(String user_Name) {
        this.user_Name = user_Name;
    }
    private String email = "";

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    private String token = "";

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    private String lat = "";

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }
    private String lon = "";

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }
}

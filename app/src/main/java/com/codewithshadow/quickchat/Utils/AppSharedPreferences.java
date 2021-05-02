package com.codewithshadow.quickchat.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class AppSharedPreferences {
    private SharedPreferences sharedPreference;

    private static final String flag="logout";

    private  SharedPreferences.Editor editor;


    private static final String username="username";
    private  static  final  String imgUrl ="imgUrl";
    private  static  final  String hint ="hint";


    public AppSharedPreferences(Context context) {
        String Pref_Name = "Login_ID";
        sharedPreference = context.getSharedPreferences(Pref_Name, Context.MODE_PRIVATE);
        editor = sharedPreference.edit();
        editor.apply();
    }
    public  void setUsername(String username)
    {
        editor.putString(this.username,username);
        editor.apply();
    }
    public String getUserName() {
        return sharedPreference.getString(username,null);
    }

    public  void setImgUrl(String imgUrl)
    {
        editor.putString(this.imgUrl,imgUrl);
        editor.apply();
    }
    public String getImgUrl() {
        return sharedPreference.getString(imgUrl,null);
    }




    public  void setHint(String hint)
    {
        editor.putString(this.hint,hint);
        editor.apply();
    }
    public String getHint() {
        return sharedPreference.getString(hint,null);
    }





}

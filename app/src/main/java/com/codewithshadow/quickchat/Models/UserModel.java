package com.codewithshadow.quickchat.Models;

public class UserModel {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    public UserModel()
    {}


    public String getImgurl() {
        return imgurl;
    }

    public void setImgurl(String imgurl) {
        this.imgurl = imgurl;
    }

    private String name;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public UserModel(String name, String imgurl, String token, String userid,String status) {
        this.name = name;
        this.imgurl = imgurl;
        this.token = token;
        this.userid = userid;
        this.status=status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private String imgurl;
    private String token;
    private String userid;
    private String status;
}

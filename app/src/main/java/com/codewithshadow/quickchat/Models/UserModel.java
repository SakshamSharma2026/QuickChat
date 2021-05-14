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


    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
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

    public UserModel(String name, String imgUrl, String token, String userid, String status) {
        this.name = name;
        this.imgUrl = imgUrl;
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

    private String imgUrl;
    private String token;
    private String userid;
    private String status;
}

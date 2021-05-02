package com.codewithshadow.quickchat.Models;

public class StoryModel {
    private String storyimg;
    private long timestart;

    public String getStoryid() {
        return storyid;
    }

    public void setStoryid(String storyid) {
        this.storyid = storyid;
    }

    private String storyid;


    public StoryModel ()
    {

    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    private String userId;

    public String getStoryimg() {
        return storyimg;
    }

    public void setStoryimg(String storyimg) {
        this.storyimg = storyimg;
    }

    public long  getTimestart() {
        return timestart;
    }

    public void setTimestart(long  timestart) {
        this.timestart = timestart;
    }

    public long  getTimeend() {
        return timeend;
    }

    public void setTimeend(long  timeend) {
        this.timeend = timeend;
    }

    public StoryModel( String storyimg, long  timestart, long  timeend, String userId
    ,String storyid,String timeupload) {
        this.storyimg = storyimg;
        this.timestart = timestart;
        this.timeend = timeend;
        this.userId = userId;
        this.storyid = storyid;
        this.timeupload = timeupload;
    }

    public StoryModel( String storyimg,String storyid,String msg) {
        this.storyimg = storyimg;
        this.storyid = storyid;
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    private String msg;

    private long  timeend;


    public String getTimeupload() {
        return timeupload;
    }

    public void setTimeupload(String timeupload) {
        this.timeupload = timeupload;
    }

    private String timeupload;

}

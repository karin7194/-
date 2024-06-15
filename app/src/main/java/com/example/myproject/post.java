package com.example.myproject;

import java.io.Serializable;

public class post implements Serializable {
    private String postimage;
    private String postinfo;
    private String userimage;
    private String username;
    private String email;
    private long timestamp;
    public post(String postimage, String username, String userimage,String postinfo,String email,long timestamp) {
        this.postimage= postimage;
        this.username = username;
        this.userimage = userimage;
        this.postinfo=postinfo;
        this.email=email;
        this.timestamp=timestamp;
    }
public post(){}

    public String getEmail() {
        return email;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPostimage() {
        return postimage;
    }

    public void setPostimage(String postimage) {
        this.postimage = postimage;
    }

    public String getPostinfo() {
        return postinfo;
    }

    public void setPostinfo(String postinfo) {
        this.postinfo = postinfo;
    }

    public String getUserimage() {
        return userimage;
    }

    public void setUserimage(String userimage) {
        this.userimage = userimage;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}


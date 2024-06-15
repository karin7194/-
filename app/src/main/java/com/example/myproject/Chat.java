package com.example.myproject;

import java.util.HashMap;
import java.util.Map;

public class Chat {
    private String currentuseremail;
    private String postowneremail;
    private String message;
    private long timestamp;
    private String currentprofile;
    private String postprofile;

    public Chat(String currentuseremail, String postowneremail, String message, long timestamp, String currentprofile, String postprofile) {
        this.currentuseremail = currentuseremail;
        this.postowneremail = postowneremail;
        this.message = message;
        this.timestamp = timestamp;
        this.currentprofile = currentprofile;
        this.postprofile = postprofile;
    }

    public Chat(){}

    public String getCurrentprofile() {
        return currentprofile;
    }

    public void setCurrentprofile(String currentprofile) {
        this.currentprofile = currentprofile;
    }

    public String getPostprofile() {
        return postprofile;
    }

    public void setPostprofile(String postprofile) {
        this.postprofile = postprofile;
    }



    public String getCurrentuseremail() {
        return currentuseremail;
    }

    public void setCurrentuseremail(String currentuseremail) {
        this.currentuseremail = currentuseremail;
    }

    public String getPostowneremail() {
        return postowneremail;
    }

    public void setPostowneremail(String postowneremail) {
        this.postowneremail = postowneremail;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timeswap) {
        this.timestamp = timeswap;
    }
}

package com.example.myproject;

import java.io.Serializable;

public class User implements Serializable {
    private String name;
    private String pass;
    private String email;
    private String bio;
    private String profilepic;
public User(){}
    public User(String name, String pass, String email,String bio,String profilepic) {
        this.name = name;
        this.pass = pass;
        this.email = email;
        this.bio=bio;
        this.profilepic=profilepic;
    }

    public String getProfilepic() {
        return profilepic;
    }

    public void setProfilepic(String profilepic) {
        this.profilepic = profilepic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", pass='" + pass + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}


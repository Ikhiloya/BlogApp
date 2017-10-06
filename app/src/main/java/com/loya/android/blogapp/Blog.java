package com.loya.android.blogapp;

/**
 * Created by user on 9/29/2017.
 */

public class Blog {
    private String title;
    private String desc_value;
    private String image;
    private String profile_image;
    private String username;
    private String post_time;


    public Blog() {

    }

    public Blog(String title, String desc_value, String image, String profile_image, String username, String post_time) {
        this.setTitle(title);
        this.setDesc_value(desc_value);
        this.setImage(image);
        this.setProfile_image(profile_image);
        this.setUsername(username);
        this.setPost_time(post_time);
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc_value() {
        return desc_value;
    }

    public void setDesc_value(String desc_value) {
        this.desc_value = desc_value;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPost_time() {
        return post_time;
    }

    public void setPost_time(String post_time) {
        this.post_time = post_time;
    }
}
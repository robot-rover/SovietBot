package com.github.adamint.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Owner {

    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("discriminator")
    @Expose
    private String discriminator;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("avatar")
    @Expose
    private String avatar;

    public Owner() {
    }

    public Owner(String username, String discriminator, String id, String avatar) {
        super();
        this.username = username;
        this.discriminator = discriminator;
        this.id = id;
        this.avatar = avatar;
    }

    public String getUsername() {
        return username;
    }

    public String getDiscriminator() {
        return discriminator;
    }

    public String getId() {
        return id;
    }

    public String getAvatar() {
        return avatar;
    }

}
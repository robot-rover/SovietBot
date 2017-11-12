package com.github.adamint.Responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Email {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("discriminator")
    @Expose
    private String discriminator;
    @SerializedName("avatar")
    @Expose
    private String avatar;
    @SerializedName("verified")
    @Expose
    private Boolean verified;
    @SerializedName("email")
    @Expose
    private String email;

    public Email() {
    }

    public Email(String id, String username, String discriminator, String avatar, Boolean verified, String email) {
        super();
        this.id = id;
        this.username = username;
        this.discriminator = discriminator;
        this.avatar = avatar;
        this.verified = verified;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getDiscriminator() {
        return discriminator;
    }

    public String getAvatar() {
        return avatar;
    }

    public Boolean getVerified() {
        return verified;
    }

    public String getEmail() {
        return email;
    }
}
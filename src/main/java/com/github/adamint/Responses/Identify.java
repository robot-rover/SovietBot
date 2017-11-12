package com.github.adamint.Responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Identify {

    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("verified")
    @Expose
    private Boolean verified;
    @SerializedName("mfa_enabled")
    @Expose
    private Boolean mfaEnabled;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("avatar")
    @Expose
    private String avatar;
    @SerializedName("discriminator")
    @Expose
    private String discriminator;

    public Identify() {
    }

    public Identify(String username, Boolean verified, Boolean mfaEnabled, String id, String avatar, String discriminator) {
        super();
        this.username = username;
        this.verified = verified;
        this.mfaEnabled = mfaEnabled;
        this.id = id;
        this.avatar = avatar;
        this.discriminator = discriminator;
    }

    public String getUsername() {
        return username;
    }

    public Boolean isVerified() {
        return verified;
    }

    public Boolean isMfaEnabled() {
        return mfaEnabled;
    }

    public String getId() {
        return id;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getDiscriminator() {
        return discriminator;
    }
}

package com.github.adamint.Responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Guild {

    @SerializedName("owner")
    @Expose
    private Boolean owner;
    @SerializedName("permissions")
    @Expose
    private Integer permissions;
    @SerializedName("icon")
    @Expose
    private String icon;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("name")
    @Expose
    private String name;

    public Guild() {
    }

    public Guild(Boolean owner, Integer permissions, String icon, String id, String name) {
        super();
        this.owner = owner;
        this.permissions = permissions;
        this.icon = icon;
        this.id = id;
        this.name = name;
    }

    public Boolean getOwner() {
        return owner;
    }

    public Integer getPermissions() {
        return permissions;
    }

    public String getIcon() {
        return icon;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
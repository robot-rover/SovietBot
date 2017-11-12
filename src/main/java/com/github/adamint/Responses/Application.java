
package com.github.adamint.Responses;

import com.github.adamint.Models.Owner;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Application {

    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("icon")
    @Expose
    private String icon;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("rpc_origins")
    @Expose
    private List<Object> rpcOrigins = null;
    @SerializedName("flags")
    @Expose
    private Integer flags;
    @SerializedName("owner")
    @Expose
    private Owner owner;


    public Application() {
    }

    public Application(String description, String icon, String id, String name, List<Object> rpcOrigins, Integer flags, Owner owner) {
        super();
        this.description = description;
        this.icon = icon;
        this.id = id;
        this.name = name;
        this.rpcOrigins = rpcOrigins;
        this.flags = flags;
        this.owner = owner;
    }

    public String getDescription() {
        return description;
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

    public List<Object> getRpcOrigins() {
        return rpcOrigins;
    }

    public Integer getFlags() {
        return flags;
    }


    public Owner getOwner() {
        return owner;
    }

}
package com.github.adamint.Responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Connection {

    @SerializedName("visibility")
    @Expose
    private Integer visibility;
    @SerializedName("friend_sync")
    @Expose
    private Boolean friendSync;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("name")
    @Expose
    private String name;

    public Connection() {
    }

    public Connection(Integer visibility, Boolean friendSync, String type, String id, String name) {
        super();
        this.visibility = visibility;
        this.friendSync = friendSync;
        this.type = type;
        this.id = id;
        this.name = name;
    }

    public boolean isVisible() {
        return visibility == 1;
    }


    public boolean getFriendSync() {
        return friendSync;
    }

    public Type getType() {
        if (type.equalsIgnoreCase("youtube")) return Type.YOUTUBE;
        else if (type.equalsIgnoreCase("twitch")) return Type.TWITCH;
        else if (type.equalsIgnoreCase("battlenet")) return Type.BATTLENET;
        else if (type.equalsIgnoreCase("steam")) return Type.STEAM;
        else if (type.equalsIgnoreCase("skype")) return Type.SKYPE;
        else if (type.equalsIgnoreCase("leagueoflegends")) return Type.LEAGUEOFLEGENDS;
        else return null;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public enum Type {
        TWITCH,
        YOUTUBE,
        SKYPE,
        BATTLENET,
        STEAM,
        LEAGUEOFLEGENDS;
    }

}
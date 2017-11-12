package com.github.adamint.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Token {
    @SerializedName("access_token")
    @Expose
    private String accessToken;
    @SerializedName("token_type")
    @Expose
    private String tokenType;
    @SerializedName("expires_in")
    @Expose
    private Integer expiresIn;
    @SerializedName("refresh_token")
    @Expose
    private String refreshToken;
    @SerializedName("scope")
    @Expose
    private String scope;

    public Token() {
    }

    public Token(String accessToken, String tokenType, Integer expiresIn, String refreshToken, String scope) {
        super();
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.refreshToken = refreshToken;
        this.scope = scope;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public ArrayList<Scope.Routes> getScopes() {
        ArrayList<Scope.Routes> scopes = new ArrayList<>();
        String[] args = scope.split(" ");
        for (String arg : args) {
            if (arg.equalsIgnoreCase("connections")) scopes.add(Scope.Routes.CONNECTIONS);
            else if (arg.equalsIgnoreCase("identify")) scopes.add(Scope.Routes.IDENTIFY);
            else if (arg.equalsIgnoreCase("email")) scopes.add(Scope.Routes.EMAIL);
            else if (arg.equalsIgnoreCase("guilds")) scopes.add(Scope.Routes.GUILDS);
        }
        return scopes;
    }
}
package com.github.adamint.Main;


import com.github.adamint.Exceptions.OAuthException;
import com.github.adamint.Models.Scope;
import com.github.adamint.Models.Token;
import com.github.adamint.Responses.*;
import com.github.adamint.Settings.OAuthSettings;
import com.github.adamint.Settings.BotSettings;
import com.github.adamint.Utils.Pair;
import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class OAuthManager {
    private Gson gson = new Gson();
    private OAuthSettings OAuthSettings;
    protected BotSettings botSettings;

    public OAuthManager(BotSettings botSettings) {
        OAuthSettings = new OAuthSettings();
        this.botSettings = botSettings;
    }

    public OAuthManager(OAuthSettings OAuthSettings, BotSettings botSettings) {
        this.OAuthSettings = OAuthSettings;
        this.botSettings = botSettings;
    }

    public Application getApplicationInfo() throws OAuthException {
        Pair<Scope.Routes, String> pair = getObjectThroughBotToken(Scope.Routes.BOT_INFORMATION, botSettings.getBotToken());
        String json = pair.getV();
        return gson.fromJson(json, Application.class);
    }

    public Email getUserWithEmail(String code) throws OAuthException, JSONException, UnirestException {
        Token token = getTokenResponse(code);
        return emailHelper(token);
    }

    public Email getUserWithEmail(Token token) throws OAuthException {
        return emailHelper(token);
    }

    private Email emailHelper(Token token) throws OAuthException {
        if (token.getScopes().contains(Scope.Routes.EMAIL)) {
            Pair<Scope.Routes, String> pair = getObject(Scope.Routes.EMAIL, token);
            String json = pair.getV();
            return gson.fromJson(json, Email.class);
        }
        else throw new OAuthException("The token doesn't have access to /users/@me (Scope: email)", 403);

    }

    public Identify getUser(String code) throws OAuthException, JSONException, UnirestException {
        Token token = getTokenResponse(code);
        return userHelper(token);
    }

    public Identify getUser(Token token) throws OAuthException {
        return userHelper(token);
    }

    private Identify userHelper(Token token) throws OAuthException {
        if (token.getScopes().contains(Scope.Routes.IDENTIFY)) {
            Pair<Scope.Routes, String> pair = getObject(Scope.Routes.IDENTIFY, token);
            String json = pair.getV();
            return gson.fromJson(json, Identify.class);
        }
        else throw new OAuthException("The token doesn't have access to /users/@me (Scope: identify)", 403);
    }

    public List<Guild> getUserGuilds(String code) throws OAuthException, JSONException, UnirestException {
        Token token = getTokenResponse(code);
        return userGuildsHelper(token);
    }

    public List<Guild> getUserGuilds(Token token) throws OAuthException {
        return userGuildsHelper(token);
    }

    private List<Guild> userGuildsHelper(Token token) throws OAuthException {
        if (token.getScopes().contains(Scope.Routes.IDENTIFY)) {
            Pair<Scope.Routes, String> pair = getObject(Scope.Routes.GUILDS, token);
            String json = pair.getV();
            Guild[] guildsArray = gson.fromJson(json, Guild[].class);
            return Arrays.asList(guildsArray);
        }
        else throw new OAuthException("The token doesn't have access to /users/@me/guilds (Scope: guilds)", 403);
    }

    public List<Connection> getUserConnections(String code) throws OAuthException, JSONException, UnirestException {
        Token token = getTokenResponse(code);
        return userConnectionsHelper(token);
    }

    public List<Connection> getUserConnections(Token token) throws OAuthException {
        return userConnectionsHelper(token);
    }

    private List<Connection> userConnectionsHelper(Token token) throws OAuthException {
        if (token.getScopes().contains(Scope.Routes.CONNECTIONS)) {
            Pair<Scope.Routes, String> pair = getObject(Scope.Routes.CONNECTIONS, token);
            String json = pair.getV();
            Connection[] connections = gson.fromJson(json, Connection[].class);
            return Arrays.asList(connections);
        }
        else throw new OAuthException("The token doesn't have access to /users/@me/connections (Scope: connections)", 403);
    }


    private Pair<Scope.Routes, String> getObject(Scope.Routes route, Token token) throws OAuthException {
        try {
            HttpResponse<String> scopeResponse = Unirest.get("https://discordapp.com/api" + route.getRouteURL())
                    .header("authorization", "Bearer " + token.getAccessToken())
                    .header("cache-control", "no-cache")
                    .asString();
            if (scopeResponse.getStatus() == 200) return new Pair<>(route, scopeResponse.getBody());
            else if (scopeResponse.getStatus() == 401)
                throw new OAuthException("Unauthorized. Make sure that the provided code is valid, then try again.", 401);
            else
                throw new OAuthException("Error. HTTP Response Code: " + scopeResponse.getStatus() + ": " + scopeResponse.getStatusText(), scopeResponse.getStatus());
        }
        catch (UnirestException e) {
            throw new OAuthException("Unirest Exception: Invalid URL or arguments", 0);
        }
    }

    private Pair<Scope.Routes, String> getObjectThroughBotToken(Scope.Routes route, String botToken) throws OAuthException {
        try {
            HttpResponse<String> scopeResponse = Unirest.get("https://discordapp.com/api" + route.getRouteURL())
                    .header("authorization", "Bot " + botToken)
                    .header("cache-control", "no-cache")
                    .asString();
            if (scopeResponse.getStatus() == 200) return new Pair<>(route, scopeResponse.getBody());
            else if (scopeResponse.getStatus() == 401)
                throw new OAuthException("Unauthorized. Make sure that the provided code is valid, then try again.", 401);
            else
                throw new OAuthException("Error. HTTP Response Code: " + scopeResponse.getStatus() + ": " + scopeResponse.getStatusText(), scopeResponse.getStatus());
        }
        catch (UnirestException e) {
            throw new OAuthException("Unirest Exception: Invalid URL or arguments", 0);
        }
    }

    private Token getTokenResponse(String code) throws OAuthException, UnirestException, JSONException {
            JSONObject obj = new JSONObject();
            obj.put("client_id", botSettings.getClientId())
                    .put("client_secret", botSettings.getClientSecret())
                    .put("grant_type", "authorization_code")
                    .put("redirect_uri", OAuthSettings.getRedirectUrl())
                    .put("code", code);
            HttpResponse<String> tokenHttpResponse = Unirest.post("https://discordapp.com/api/oauth2/token")
                    .header("content-type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
                    .header("authorization", "Bearer " + code)
                    .header("cache-control", "no-cache")
                    .body("------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"client_id\"\r\n\r\n" + botSettings.getClientId() + "" +
                            "\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"client_secret\"\r\n\r\n" + botSettings.getClientSecret() + "" +
                            "\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"grant_type\"\r\n\r\nauthorization_code" +
                            "\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"redirect_uri\"\r\n\r\n" + OAuthSettings.getRedirectUrl() + "" +
                            "\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"code\"\r\n\r\n" + code + "\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW--")
                    .asString();
            if (tokenHttpResponse.getStatus() == 200) {
                return gson.fromJson(tokenHttpResponse.getBody(), Token.class);
            }
            else if (tokenHttpResponse.getStatus() == 401) {
                throw new OAuthException("Unauthorized. Make sure that the provided code is valid, then try again." + tokenHttpResponse.getBody(), 401);
            }
            else {
                throw new OAuthException("Error. HTTP Response Code: " + tokenHttpResponse.getStatus() + ": " + tokenHttpResponse.getStatusText(), tokenHttpResponse.getStatus());
            }
    }

    public Token getToken(String code) throws OAuthException, JSONException, UnirestException {
        return getTokenResponse(code);
    }
}

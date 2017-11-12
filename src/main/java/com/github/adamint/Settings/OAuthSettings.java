package com.github.adamint.Settings;

import com.mashape.unirest.http.Unirest;

public class OAuthSettings {
    private String botName;
    private String websiteUrl;
    private String redirectUrl;
    private String version;

    public OAuthSettings() {
        this.botName = "Bot";
        this.websiteUrl = "http://ardentbot.tk";
        this.redirectUrl = "https://www.discordapp.com";
        this.version = "1.0";
        update();
    }

    public OAuthSettings(String botName, String websiteUrl, String redirectUrl, String version) {
        this.botName = botName;
        this.websiteUrl = websiteUrl;
        this.redirectUrl = redirectUrl;
        this.version = version;
        update();
    }

    public String getBotName() {
        return botName;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getVersion() {
        return version;
    }

    public OAuthSettings setBotName(String botName) {
        this.botName = botName;
        return this;
    }

    public OAuthSettings setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
        return this;
    }

    public OAuthSettings setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
        return this;
    }

    public OAuthSettings setVersion(String version) {
        this.version = version;
        return this;
    }

    public void update() {
        Unirest.setDefaultHeader("User-Agent", botName + " (" + websiteUrl + ", " + version + ")");
    }
}

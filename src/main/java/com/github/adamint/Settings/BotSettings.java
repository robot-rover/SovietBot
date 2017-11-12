package com.github.adamint.Settings;

public class BotSettings {
    private final String clientId;
    private final String clientSecret;
    private final String botToken;

    public BotSettings(String clientId, String clientSecret, String botToken) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.botToken = botToken;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getBotToken() {
        return botToken;
    }
}

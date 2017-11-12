package rr.industries;

public class Configuration {
    public String commChar;
    public String url;
    public String webhookSecret;
    public String[] operators;
    public String owmKey;
    public String googleKey;
    public String dictKey;
    public String discordSecret;

    public Configuration(String commChar, String url, String secret, String[] operators, String owmKey, String googleKey, String dictKey, String discordSecret) {
        this.commChar = commChar;
        this.webhookSecret = secret;
        this.url = url;
        this.operators = (operators == null ? new String[0] : operators);
        this.owmKey = owmKey;
        this.googleKey = googleKey;
        this.dictKey = dictKey;
        this.discordSecret = discordSecret;
    }

    public Configuration() {
        this(">", "", "", new String[0], "", "", "", "");
    }

    @Override
    public String toString() {
        return "Bot Configuration";
    }
}

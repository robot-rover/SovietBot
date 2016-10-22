package rr.industries;

public class Configuration {
    public String botName;
    public String botAvatar;
    public String commChar;
    public String token;
    public String secret;
    public int webhooksPort;
    public String[] operators;
    public String owmKey;
    public String googleKey;
    public String dictKey;

    public Configuration(String botName, String botAvatar, String commChar, String token, String secret, int webhooksPort, String[] operators, String owmKey, String googleKey, String dictKey) {
        this.botName = botName;
        this.botAvatar = botAvatar;
        this.commChar = commChar;
        this.token = token;
        this.secret = secret;
        this.webhooksPort = webhooksPort;
        this.operators = (operators == null ? new String[0] : operators);
        this.owmKey = owmKey;
        this.googleKey = googleKey;
        this.dictKey = dictKey;
    }

    @Override
    public String toString() {
        return botName.concat(" Configuration");
    }
}

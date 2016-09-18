package rr.industries.util;

/**
 * @author Sam
 * @project sovietBot
 * @created 9/10/2016
 */
public enum Arguments {
    NUMBER("<#>"), MENTION("@<\u200BUser>"), TIMEZONE("GMT<+or-><#>"), LINK("<http://www.xxx.xxx>"),
    TEXTCHANNEL("#<Channel>"), VOICECHANNEL("<VoiceChannel>"), TEXT("<Some_Words>"), COMMAND("<Command>"), DND("<X>d<Y>"),
    MENTIONROLE("<@\u200BRole>"), CITY("<City>"), COUNTRY("<Country>");
    public final String text;

    Arguments(String text) {
        this.text = " " + text + " ";
    }
}

package rr.industries.util;

import rr.industries.CommandList;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Predicate;

/**
 * @author Sam
 * @project sovietBot
 * @created 9/10/2016
 */
public enum Arguments {
    NUMBER("<#>", (v) -> BotUtils.tryInt(v)), MENTION("@<\u200BUser>", (v) -> v.matches("^<@!?[0-9]{18}>$")),
    TIMEZONE("GMT<+or-><#>", (v) -> v.matches("^GMT(?:/+|-)[0-9]+$")),
    LINK("<http://www.xxx.xxx>", (v) -> {
        try {
            new URL(v);
        } catch (MalformedURLException ex) {
            return false;
        }
        return true;
    }),
    TEXTCHANNEL("#<Channel>", (v) -> v.matches("^<#[0-9]{18}>$")), VOICECHANNEL("<VoiceChannel>", (v) -> v.matches("^.+$")),
    TEXT("<Some_Words>", (v) -> v.matches("^.+$")), COMMAND("<Command>", (v) -> CommandList.isCommand(v)),
    DND("<X>d<Y>", (v) -> v.matches("^[0-9]+d[0-9]+$")), MENTIONROLE("@<\u200BRole>", (v) -> v.matches("^<@&[0-9]{18}>$")),
    CITY("<City>", (v) -> v.matches("^.+$")), COUNTRY("<Country>", (v) -> v.matches("^.+$"));
    public final String text;
    public final Predicate<String> isValid;

    Arguments(String text, Predicate<String> predicate) {
        this.text = " " + text;
        this.isValid = predicate;
    }
}

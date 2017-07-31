package rr.industries.util;

import rr.industries.CommandList;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.function.Predicate;

/**
 * @author robot_rover
 */
public enum Validate {
    NUMBER("#", BotUtils::tryInt), MENTION("@\u200BUser", (v) -> v.matches("^<@!?[0-9]{18}>$")),
    TIMEZONE("Timezone", (v) -> {
        try {
            ZoneId.of(v);
        } catch (DateTimeException ex) {
            return false;
        }
        return true;
    }),
    LINK("http://www.xxx.xxx", (v) -> {
        try {
            new URL(v);
        } catch (MalformedURLException ex) {
            return false;
        }
        return true;
    }),
    TEXTCHANNEL("#Channel", (v) -> v.matches("^<#[0-9]{18}>$")), VOICECHANNEL("Voice Channel", (v) -> v.matches("^.+$")),
    TEXT("Text", (v) -> v.length() > 0), COMMAND("Command", CommandList::isCommand),
    DND("#d#", (v) -> v.matches("^[0-9]+d[0-9]+$")), MENTIONROLE("@\u200BRole", (v) -> v.matches("^<@&[0-9]{18}>$")),
    LOCATION("Location", (v) -> v.matches("^.+$")), LONGTEXT("Long Text", (v) -> v.length() > 0),
    BOOLEAN("Boolean", (v) -> v.equals("true") || v.equals("false"));
    public final Predicate<String> isValid;

    public final String defaultLabel;

    Validate(String defaultLabel, Predicate<String> predicate) {
        this.isValid = predicate;
        this.defaultLabel = defaultLabel;
    }
}

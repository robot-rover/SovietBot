package rr.industries.commands;

import rr.industries.util.*;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.MissingPermissionsException;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@CommandInfo(
        commandName = "connect",
        helpText = "Connects and disconnects the bot from voice channels."
)
public class Connect implements Command {
    @SubCommand(name = "/", Syntax = {@Syntax(helpText = "Disconnects the bot from all voice channels", args = {})})
    public void disconnect(CommContext cont) {
        cont.getActions().channels().disconnectFromChannel(cont.getMessage().getGuild(), cont.getClient().getConnectedVoiceChannels());
    }

    @SubCommand(name = "", Syntax = {
            @Syntax(helpText = "Connects the bot to the Voice Channel provided", args = {Arguments.VOICECHANNEL}),
            @Syntax(helpText = "Connects the bot to the voice channel you are connected too", args = {})
    })
    public void execute(CommContext cont) {

        if (cont.getArgs().size() >= 2) {
            try {
                String channelName;
                Matcher m = Pattern.compile("\"(.+)\"").matcher(cont.getMessage().getContent());
                if (m.find()) {
                    channelName = m.group();
                } else {
                    channelName = cont.getArgs().get(1);
                }
                List<IVoiceChannel> next = cont.getMessage().getGuild().getVoiceChannelsByName(channelName);
                if (next.size() > 0) {
                    if (!next.get(0).isConnected())
                        next.get(0).join();
                } else {
                    cont.getActions().channels().notFound(cont.getMessage(), "connect", "Voice Channel", channelName, LOG);
                }
            } catch (MissingPermissionsException ex) {
                cont.getActions().channels().missingPermissions(cont.getMessage().getChannel(), ex);
            }
        } else {
            List<IVoiceChannel> next = cont.getMessage().getAuthor().getConnectedVoiceChannels();
            if (next.size() >= 1) {
                cont.getActions().channels().connectToChannel(next.get(0), cont.getClient().getConnectedVoiceChannels());
            }
        }
    }

    @Override
    public Predicate<List<String>> getValiddityOverride() {
        return (v) -> v.size() == 2 || v.size() == 1 || v.stream().collect(Collectors.joining(" ")).matches("\".+\"");
    }
}

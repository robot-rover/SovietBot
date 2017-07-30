package rr.industries.commands;

import rr.industries.exceptions.BotException;
import rr.industries.exceptions.NotFoundException;
import rr.industries.util.*;
import sx.blah.discord.handle.obj.IVoiceChannel;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CommandInfo(
        commandName = "connect",
        helpText = "Connects and disconnects the bot from voice channels."
)
public class Connect implements Command {
    @SubCommand(name = "/", Syntax = {@Syntax(helpText = "Disconnects the bot from all voice channels", args = {})})
    public void disconnect(CommContext cont) {
        cont.getActions().channels().disconnectFromChannel(cont.getMessage().getGuild());
    }

    @SubCommand(name = "", Syntax = {
            @Syntax(helpText = "Connects the bot to the Voice Channel provided", args = {Arguments.VOICECHANNEL}),
            @Syntax(helpText = "If the voice channel is more than one word, put it in quotes", args = {Arguments.LONGTEXT}),
            @Syntax(helpText = "Connects the bot to the voice channel you are connected too", args = {})
    })
    public void execute(CommContext cont) throws BotException {

        if (cont.getArgs().size() >= 2) {
            Matcher m = Pattern.compile("\"(.+)\"").matcher(cont.getMessage().getContent());
            String channelName = (m.find() ? m.group() : cont.getArgs().get(1));
            List<IVoiceChannel> next = cont.getMessage().getGuild().getVoiceChannelsByName(channelName);
            if (next.size() > 0) {
                cont.getActions().channels().connectToChannel(next.get(0));
            } else {
                throw new NotFoundException("Voice Channel", channelName);
            }
        } else {
            IVoiceChannel channel = cont.getMessage().getAuthor().getVoiceStateForGuild(cont.getMessage().getGuild()).getChannel();
            if (channel != null) {
                cont.getActions().channels().connectToChannel(channel);
            }
        }
    }
}

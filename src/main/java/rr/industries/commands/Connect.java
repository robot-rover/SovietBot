package rr.industries.commands;

import rr.industries.exceptions.BotException;
import rr.industries.exceptions.NotFoundException;
import rr.industries.util.*;
import sx.blah.discord.handle.obj.IVoiceChannel;

import java.util.List;

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
            @Syntax(helpText = "Connects to the named voice channel.", args = {@Argument(description = "\"Channel Name\"", value = Validate.LONGTEXT)}),
            @Syntax(helpText = "Connects the bot to the voice channel you are connected too", args = {})
    })
    public void execute(CommContext cont) throws BotException {

        if (cont.getArgs().size() >= 2) {
            String channelName = cont.getConcatArgs(1);
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
            } else {
                cont.getActions().channels().sendMessage(cont.builder().appendContent("You aren't connected to a voice channel!"));
            }
        }
    }
}

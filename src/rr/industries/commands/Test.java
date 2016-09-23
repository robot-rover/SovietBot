package rr.industries.commands;

import rr.industries.util.*;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.util.List;
import java.util.function.Predicate;

@CommandInfo(
        commandName = "test",
        helpText = "Temporary command for testing new features",
        permLevel = Permissions.BOTOPERATOR
)
public class Test implements Command {
    @SubCommand(name = "tester", Syntax = {@Syntax(helpText = "Test the tester test", args = {})})
    public void testSub(CommContext cont) {
        try {
            cont.getMessage().reply("The test worked");
        } catch (MissingPermissionsException e) {
            e.printStackTrace();
        } catch (RateLimitException e) {
            e.printStackTrace();
        } catch (DiscordException e) {
            e.printStackTrace();
        }
    }

    @SubCommand(name = "repeat", Syntax = {})
    public void repeat(CommContext cont) {
        cont.getActions().sendMessage(new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getChannel())
                .withContent("```" + cont.getMessage().getContent() + "```"));
    }

    @Override
    public Predicate<List<String>> getValiddityOverride() {
        return (v) -> true;
    }
}

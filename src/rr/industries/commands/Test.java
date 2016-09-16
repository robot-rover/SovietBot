package rr.industries.commands;

import rr.industries.CommandList;
import rr.industries.util.*;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

@CommandInfo(
        commandName = "test",
        helpText = "Temporary command for testing new features",
        permLevel = Permissions.BOTOPERATOR
)
public class Test implements Command {
    static {
        CommandList.defaultCommandList.add(Test.class);
    }
    @SubCommand(name = "tester", Syntax = {@Syntax(helpText = "Test the tester test", args = {})})
    public void testSub(CommContext cont) {
        try {
            cont.getMessage().getMessage().reply("The test worked");
        } catch (MissingPermissionsException e) {
            e.printStackTrace();
        } catch (RateLimitException e) {
            e.printStackTrace();
        } catch (DiscordException e) {
            e.printStackTrace();
        }
    }
}

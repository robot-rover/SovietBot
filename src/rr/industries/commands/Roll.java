package rr.industries.commands;

import rr.industries.util.*;
import sx.blah.discord.util.MessageBuilder;

@CommandInfo(
        commandName = "roll",
        helpText = "Rolls a random number in a variety of ways"
)
public class Roll implements Command {
    @SubCommand(name = "", Syntax = {
            @Syntax(helpText = "Rolls a number 1-100", args = {}),
            @Syntax(helpText = "Rolls a number 1-#", args = {Arguments.NUMBER}),
            @Syntax(helpText = "Rolls the dice RP style. Rolls a number 1-Y, X times", args = {Arguments.DND})
    })
    public void execute(CommContext cont) {
        int roll;
        if (cont.getArgs().size() >= 2 && cont.getArgs().get(1).contains("d")) {
            int d = cont.getArgs().get(1).indexOf("d");
            int reps = Integer.parseInt(cont.getArgs().get(1).substring(0, d));
            int value = Integer.parseInt(cont.getArgs().get(1).substring(d + 1));
            int total = 0;
            String message = "**Rolling: **" + cont.getArgs().get(1) + "\n";
            for (int i = 0; i < reps; i++) {
                roll = (rn.nextInt(value) + 1);
                message = message + roll + ", ";
                total += roll;
            }
            message = message.substring(0, message.length() - 2);
            message = message + "\n**Total: **" + total;
            cont.getActions().sendMessage(new MessageBuilder(cont.getClient()).withContent(message).withChannel(cont.getMessage().getChannel()));

        } else if (cont.getArgs().size() >= 2) {

            roll = Integer.parseInt(cont.getArgs().get(1));
            if (roll < 1) {
                cont.getActions().sendMessage(new MessageBuilder(cont.getClient()).withContent("Rolling 0 to 0: 0").withChannel(cont.getMessage().getChannel()));

                return;
            }
            String message = "Rolling 1 to " + Integer.toString(roll) + ": " + (rn.nextInt(roll) + 1);
            cont.getActions().sendMessage(new MessageBuilder(cont.getClient()).withContent(message).withChannel(cont.getMessage().getChannel()));
        } else {
            cont.getActions().sendMessage(new MessageBuilder(cont.getClient()).withContent("Rolling 1 to 100: " + (rn.nextInt(100) + 1)).withChannel(cont.getMessage().getChannel()));

        }
    }
}

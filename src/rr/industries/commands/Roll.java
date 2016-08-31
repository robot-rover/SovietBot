package rr.industries.commands;

import rr.industries.util.BotActions;
import rr.industries.util.CommContext;
import rr.industries.util.Parsable;
import sx.blah.discord.util.MessageBuilder;

/**
 * Created by Sam on 8/28/2016.
 */
public class Roll extends Command {
    public Roll() {
        commandName = "roll";
        helpText = "Rolls a random number in a variety of ways.";
    }

    @Override
    public void execute(CommContext cont) {
        int roll;
        boolean dnd;
        int d = 0;
        try {
            d = cont.getArgs().get(1).indexOf("d");
            dnd = cont.getArgs().get(1).contains("d") && Parsable.tryInt(cont.getArgs().get(1).substring(0, d)) && Parsable.tryInt(cont.getArgs().get(1).substring(d + 1));
        } catch (IndexOutOfBoundsException ex) {
            dnd = false;
        }
        if (cont.getArgs().size() >= 2 && Parsable.tryInt(cont.getArgs().get(1))) {
            roll = Integer.parseInt(cont.getArgs().get(1));
            if (roll < 1) {
                BotActions.sendMessage(new MessageBuilder(cont.getClient()).withContent("Rolling 0 to 0: 0").withChannel(cont.getMessage().getMessage().getChannel()));

                return;
            }
            String message = "Rolling 1 to " + Integer.toString(roll) + ": " + (rn.nextInt(roll) + 1);
            BotActions.sendMessage(new MessageBuilder(cont.getClient()).withContent(message).withChannel(cont.getMessage().getMessage().getChannel()));
        } else if (cont.getArgs().size() >= 2 && dnd) {
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
            BotActions.sendMessage(new MessageBuilder(cont.getClient()).withContent(message).withChannel(cont.getMessage().getMessage().getChannel()));

        } else {
            BotActions.sendMessage(new MessageBuilder(cont.getClient()).withContent("Rolling 1 to 100: " + (rn.nextInt(100) + 1)).withChannel(cont.getMessage().getMessage().getChannel()));

        }
    }
}

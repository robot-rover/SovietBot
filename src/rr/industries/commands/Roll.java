package rr.industries.commands;

import rr.industries.util.*;
import sx.blah.discord.util.MessageBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        MessageBuilder message = cont.builder();
        int roll;
        if (cont.getArgs().size() >= 2) {
            Matcher dnd = Pattern.compile("(\\d+)d(\\d+)").matcher(cont.getArgs().get(1));
            if (dnd.find()) {
                int reps = Integer.parseInt(dnd.group(1));
                int value = Integer.parseInt(dnd.group(2));
                List<Integer> rolls = new ArrayList<>();
                message.withContent("**Rolling: **" + cont.getArgs().get(1) + "\n");
                for (int i = 0; i < reps; i++) {
                    rolls.add(rn.nextInt(value) + 1);
                }
                message.appendContent(rolls.stream().map(v -> v.toString()).collect(Collectors.joining(", ")));
                message.appendContent("\n**Total: **" + rolls.stream().collect(Collectors.summingInt(v -> v)));

            } else {
                roll = Integer.parseInt(cont.getArgs().get(1));
                message.withContent("Rolling 1 to " + Integer.toString(roll) + ": " + (rn.nextInt(roll) + Math.signum(roll)));
            }
        } else {
            message.withContent("Rolling 1 to 100: " + (rn.nextInt(100) + 1));
        }
        cont.getActions().channels().sendMessage(message);
    }
}

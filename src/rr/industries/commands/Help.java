package rr.industries.commands;

import rr.industries.SovietBot;
import rr.industries.exceptions.BotException;
import rr.industries.exceptions.NotFoundException;
import rr.industries.util.*;
import rr.industries.util.sql.PermTable;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

//todo: add options
@CommandInfo(
        commandName = "help",
        helpText = "Displays this help message"
)
public class Help implements Command {

    @SubCommand(name = "", Syntax = {
            @Syntax(helpText = "Displays all possible commands", args = {}),
            @Syntax(helpText = "Displays the selected command in greater detail", args = {Arguments.COMMAND})
    })
    public void execute(CommContext cont) throws BotException {
        MessageBuilder message = cont.builder();
        if (cont.getArgs().size() >= 2) {
            String name = cont.getArgs().get(1);
            Command command = cont.getActions().getCommands().getCommandList().stream().filter(v -> v.getClass().getAnnotation(CommandInfo.class).commandName().equals(name)).findAny().orElse(null);
            if (command != null) {
                CommandInfo commandInfo = command.getClass().getDeclaredAnnotation(CommandInfo.class);
                message.appendContent("**" + cont.getCommChar() + commandInfo.commandName() + " - " + commandInfo.helpText() + "**\n");
                message.appendContent("`<>` means replace with your own value. `[]` means you can give more than one value.\n");
                message.appendContent("For more help, visit <**" + SovietBot.website + "commands/" + commandInfo.commandName() + ".html**>\n");
                SubCommand mainSubCommand = null;
                List<SubCommand> subCommands = new ArrayList<>();
                for (Method method : command.getClass().getDeclaredMethods()) {
                    SubCommand sub = method.getAnnotation(SubCommand.class);
                    if (sub != null) {
                        if (sub.name().equals("")) {
                            mainSubCommand = sub;
                        } else {
                            if (sub.permLevel() == Permissions.BOTOPERATOR)
                                continue;
                            subCommands.add(sub);
                        }
                    }
                }
                if (mainSubCommand != null)
                    subCommands.add(0, mainSubCommand);
                for (SubCommand subCom : subCommands) {
                    for (Syntax syntax : subCom.Syntax()) {
                        message.appendContent("[" + cont.getCommChar() + commandInfo.commandName() + (subCom.name().equals("") ? "" : " ") + subCom.name() +
                                Arrays.stream(syntax.args()).map(v -> v.text).collect(Collectors.joining(" ", " ", " ")) + "]: ");
                        if (subCom.permLevel().level > commandInfo.permLevel().level) {
                            message.appendContent("<*" + subCom.permLevel().title + "*> ");
                        }
                        message.appendContent(syntax.helpText() + "\n");
                        if (syntax.options().length > 0) {
                            message.appendContent(Arrays.stream(syntax.options()).collect(Collectors.joining(" | ", "( ", " )"))).appendContent("\n");
                        }
                    }
                }

            } else {
                throw new NotFoundException("Command", cont.getArgs().get(1));
            }
            cont.getActions().channels().sendMessage(message);
        } else {
            BotUtils.bufferRequest(() -> {
                try {
                    MessageBuilder message2 = new MessageBuilder(cont.getClient()).withChannel(cont.getClient().getOrCreatePMChannel(cont.getMessage().getAuthor()));
                    message2.appendContent("```markdown\n# " + SovietBot.botName + " - \"" + cont.getCommChar() + "\" #\n");
                    message2.appendContent("For more help type >help <command>\n");
                    message2.appendContent("Or visit <" + SovietBot.website + ">\n");
                    boolean userIsOp = cont.getActions().getTable(PermTable.class).getPerms(cont.getMessage().getAuthor(), cont.getMessage().getGuild()).equals(Permissions.BOTOPERATOR);
                    for (Permissions perm : Permissions.values()) {
                        if (perm.equals(Permissions.BOTOPERATOR) && !userIsOp) {
                            continue;
                        }
                        message2.appendContent("[Permission]: " + perm.title + "\n");
                        cont.getActions().getCommands().getCommandList().stream().filter(comm -> comm.getClass().getDeclaredAnnotation(CommandInfo.class).permLevel().equals(perm))
                                .map(v -> v.getClass().getDeclaredAnnotation(CommandInfo.class)).forEach(comm -> message2.appendContent("\t" + cont.getCommChar() + comm.commandName() + " - " + comm.helpText() + "\n"));
                    }
                    cont.getActions().channels().sendMessage(message2.appendContent("```"));
                    cont.getActions().channels().sendMessage(message.withContent(cont.getMessage().getAuthor().mention() + ", Check your PMs!"));
                } catch (DiscordException ex) {
                    throw BotException.returnException(ex);
                }
            });
        }
    }
}

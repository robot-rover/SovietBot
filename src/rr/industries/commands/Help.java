package rr.industries.commands;

import rr.industries.SovietBot;
import rr.industries.util.*;
import rr.industries.util.sql.PermTable;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.RequestBuffer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

//todo: add subcommand specific permissions
@CommandInfo(
        commandName = "help",
        helpText = "Displays this help message"
)
public class Help implements Command {

    @SubCommand(name = "", Syntax = {
            @Syntax(helpText = "Displays all possible commands", args = {}),
            @Syntax(helpText = "Displays the selected command in greater detail", args = {Arguments.COMMAND})
    })
    public void execute(CommContext cont) {
        MessageBuilder message = new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getChannel());
        if (cont.getArgs().size() >= 2) {
            Command command = null;
            String name = cont.getArgs().get(1);
            for (Command comm : cont.getActions().getCommands().getCommandList()) {
                CommandInfo commandInfo = comm.getClass().getDeclaredAnnotation(CommandInfo.class);
                if (name.equals(commandInfo.commandName())) {
                    command = comm;
                }
            }
            if (command != null) {
                CommandInfo commandInfo = command.getClass().getDeclaredAnnotation(CommandInfo.class);
                message.appendContent("**" + cont.getActions().getConfig().commChar + commandInfo.commandName() + " - " + commandInfo.helpText() + "**\n");
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
                            subCommands.add(sub);
                        }
                    }
                }
                if (mainSubCommand != null) {
                    for (Syntax syntax : mainSubCommand.Syntax()) {
                        String args = "";
                        for (Arguments arg : syntax.args())
                            args = args.concat(arg.text);
                        message.appendContent("`[" + cont.getActions().getConfig().commChar + commandInfo.commandName() + args + "]:` ");
                        message.appendContent(syntax.helpText() + "\n");
                    }
                }
                for (SubCommand subCom : subCommands) {
                    for (Syntax syntax : subCom.Syntax()) {
                        String args = "";
                        for (Arguments arg : syntax.args())
                            args = args.concat(arg.text);
                        message.appendContent("`[" + cont.getActions().getConfig().commChar + commandInfo.commandName() + " " + subCom.name() + args + "]:` ");
                        if (subCom.permLevel().level > commandInfo.permLevel().level) {
                            message.appendContent("<*" + subCom.permLevel().title + "*> ");
                        }
                        message.appendContent(syntax.helpText() + "\n");
                    }
                }

            } else {
                message.withContent("Command " + cont.getArgs().get(1) + " not found...");
            }
            cont.getActions().channels().sendMessage(message);


        } else {
            RequestBuffer.request(() -> {
                try {
                    MessageBuilder message2 = new MessageBuilder(cont.getClient()).withChannel(cont.getClient().getOrCreatePMChannel(cont.getMessage().getAuthor()));
                    message2.appendContent("```markdown\n# " + SovietBot.botName + " - \"" + cont.getCommChar() + "\" #\n");
                    message2.appendContent("For more help type >help <command>...\n");
                    message2.appendContent("Or visit <" + SovietBot.website + ">\n");
                    for (Permissions perm : Permissions.values()) {
                        if (perm.equals(Permissions.BOTOPERATOR) && !cont.getActions().getTable(PermTable.class).getPerms(cont.getMessage().getAuthor(), cont.getMessage().getGuild()).equals(Permissions.BOTOPERATOR)) {
                            continue;
                        }
                        message2.appendContent("[Permission]: " + perm.title + "\n");
                        cont.getActions().getCommands().getCommandList().stream().filter(comm -> comm.getClass().getDeclaredAnnotation(CommandInfo.class).permLevel().equals(perm)).forEach(comm -> {
                            CommandInfo info = comm.getClass().getDeclaredAnnotation(CommandInfo.class);
                            message2.appendContent("\t" + cont.getCommChar() + info.commandName() + " - " + info.helpText() + "\n");
                        });
                    }
                    cont.getActions().channels().sendMessage(message2.appendContent("```"));
                    cont.getActions().channels().sendMessage(message.withContent(cont.getMessage().getAuthor().mention() + ", Check your PMs!"));
                } catch (DiscordException ex) {
                    cont.getActions().channels().customException("Help", ex.getErrorMessage(), ex, LOG, true);
                }
            });
        }
    }
}

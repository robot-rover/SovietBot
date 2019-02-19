package rr.industries.commands;

import discord4j.core.spec.EmbedCreateSpec;
import reactor.core.publisher.Mono;
import rr.industries.CommandList;
import rr.industries.Information;
import rr.industries.exceptions.BotException;
import rr.industries.exceptions.NotFoundException;
import rr.industries.util.*;
import rr.industries.util.sql.PermTable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

//todo: add options
@CommandInfo(
        commandName = "help",
        helpText = "Displays this help message",
        pmSafe = true
)
public class Help implements Command {

    @SubCommand(name = "", Syntax = {
            @Syntax(helpText = "Displays all possible commands", args = {}),
            @Syntax(helpText = "Displays the selected command in greater detail", args = {@Argument(value = Validate.COMMAND)})
    })
    public Mono<Void> execute(CommContext cont) throws BotException {
        if (cont.getArgs().size() >= 2) {
            String name = cont.getArgs().get(1);
            Command command = CommandList.getCommandList().stream().filter(v -> v.getClass().getAnnotation(CommandInfo.class).commandName().equals(name)).findAny().orElse(null);
            CommandInfo commandInfo = command == null ? null : command.getClass().getDeclaredAnnotation(CommandInfo.class);
            if (command != null && (!commandInfo.permLevel().equals(Permissions.BOTOPERATOR) || cont.getCallerPerms().equals(Permissions.BOTOPERATOR))) {
                return cont.getMessage().getMessage().getChannel().flatMap(channel -> channel.createMessage(messageSpec -> messageSpec.setEmbed(embedSpec -> createSpecificHelp(embedSpec, cont, commandInfo, command)))).then();
            } else {
                throw new NotFoundException("Command", cont.getArgs().get(1));
            }
        } else {
            Mono<Boolean> isUserOpMono = Mono.justOrEmpty(cont.getMessage().getMessage().getAuthor()).flatMap(user -> cont.getActions().getTable(PermTable.class).getPerms(user, cont.getMessage().getMessage())).map(v -> v.equals(Permissions.BOTOPERATOR)).defaultIfEmpty(false);
            return isUserOpMono.flatMap(isUserOp -> cont.getChannel().createMessage(messageSpec -> messageSpec.setEmbed(embedSpec -> createGeneralHelp(embedSpec, isUserOp, cont)))).then();
        }
    }

    private void createSpecificHelp(EmbedCreateSpec embedSpec, CommContext cont, CommandInfo commandInfo, Command command) {
                embedSpec.setTitle(cont.getCommChar() + commandInfo.commandName() + " - " + commandInfo.helpText() + (commandInfo.pmSafe() ? " - `|PM|`" : ""));
        embedSpec.setDescription("`<>` means replace with your own value. `[]` means you can give more than one value.\n" +
                "For more help, visit <" + cont.getActions().getConfig().url + "commands/" + commandInfo.commandName() + ".html>\n");
        SubCommand mainSubCommand = null;
        List<SubCommand> subCommands = new ArrayList<>();
        for (Method method : command.getClass().getDeclaredMethods()) {
            SubCommand sub = method.getAnnotation(SubCommand.class);
            if (sub != null) {
                if (sub.name().equals("")) {
                    mainSubCommand = sub;
                } else {
                    if (sub.permLevel() == Permissions.BOTOPERATOR && cont.getCallerPerms() != Permissions.BOTOPERATOR)
                        continue;
                    subCommands.add(sub);
                }
            }
        }
        if (mainSubCommand != null)
            subCommands.add(0, mainSubCommand);
        for (SubCommand subCom : subCommands) {
            for (Syntax syntax : subCom.Syntax()) {
                String title = cont.getCommChar() + commandInfo.commandName() + (subCom.name().equals("") ? "" : " ") + subCom.name() +
                        Arrays.stream(syntax.args()).map(v -> {
                            if (v.description().equals("")) {
                                return v.value().defaultLabel;
                            } else {
                                return v.description();
                            }
                        }).map(v -> "<" + v + ">").collect(Collectors.joining(" ", " ", " "));
                        /*if (subCom.permLevel().level > commandInfo.permLevel().level) {
                            title += " - Requires " + subCom.permLevel().title;
                        }*/

                if (!subCom.pmSafe() && commandInfo.pmSafe())
                    title += " `|NO PM|`";
                StringBuilder content = new StringBuilder(syntax.helpText());
                Arrays.stream(syntax.args()).filter(v -> v.options().length > 0).forEach(v -> content.append("\n\t" + v.description() + " -> ").append(Arrays.stream(v.options()).collect(Collectors.joining(" | ", "( ", " )"))));
                embedSpec.addField(title, content.toString(), false);
            }
        }
    }

    private void createGeneralHelp(EmbedCreateSpec embedSpec, boolean userIsOp, CommContext cont) {
        embedSpec.setAuthor(Information.botName + " - \"" + cont.getCommChar() + "\"", cont.getActions().getConfig().url, cont.getActions().getConfig().url + "/avatar.png");
        embedSpec.setTitle("For more help type >help <command>");
        embedSpec.setDescription("Or visit " + cont.getActions().getConfig().url);

        for (Permissions perm : Permissions.values()) {
            if (perm.equals(Permissions.BOTOPERATOR) && !userIsOp) {
                continue;
            }
            String title = "[Permission]: " + perm.title;
            StringBuilder content = new StringBuilder();
            CommandList.getCommandList().stream().filter(comm -> comm.getClass().getDeclaredAnnotation(CommandInfo.class).permLevel().equals(perm))
                    .map(u -> u.getClass().getDeclaredAnnotation(CommandInfo.class)).forEach(comm -> content.append(cont.getCommChar()).append(comm.commandName()).append(" - ").append(comm.helpText()).append("\n"));
            if (content.length() > 0)
                embedSpec.addField(title, content.toString(), false);
        }
    }
}

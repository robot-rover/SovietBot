package rr.industries.commands;

import rr.industries.CommandList;
import rr.industries.Information;
import rr.industries.exceptions.BotException;
import rr.industries.exceptions.NotFoundException;
import rr.industries.util.*;
import rr.industries.util.sql.PermTable;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MessageBuilder;

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
    /*public void execute(CommContext cont) throws BotException {
        MessageBuilder message = cont.builder();
        if (cont.getArgs().size() >= 2) {
            String name = cont.getArgs().get(1);
            Command command = cont.getActions().getCommands().getCommandList().stream().filter(v -> v.getClass().getAnnotation(CommandInfo.class).commandName().equals(name)).findAny().orElse(null);
            if (command != null) {
                CommandInfo commandInfo = command.getClass().getDeclaredAnnotation(CommandInfo.class);
                message.appendContent("**" + cont.getCommChar() + commandInfo.commandName() + " - " + commandInfo.helpText() + "**\n" + (commandInfo.pmSafe() ? "`|PM|`" : ""));
                message.appendContent("`<>` means replace with your own value. `[]` means you can give more than one value.\n");
                if (commandInfo.permLevel() != Permissions.BOTOPERATOR)
                    message.appendContent("For more help, visit <**" + cont.getActions().channels().getInfo().website + "commands/" + commandInfo.commandName() + ".html**>\n");
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
                                Arrays.stream(syntax.args()).map(v -> {if(v.description().equals("")){return v.value().defaultLabel;}else{return v.description();}}).map(v -> "<" + v + ">").collect(Collectors.joining(" ", " ", " ")) + "]: ");
                        if (subCom.permLevel().level > commandInfo.permLevel().level) {
                            message.appendContent("<*" + subCom.permLevel().title + "*> ");
                        }
                        message.appendContent(syntax.helpText());
                        if (!subCom.pmSafe())
                            message.appendContent(" `|NO PM|`");
                        message.appendContent("\n");
                        Arrays.stream(syntax.args()).filter(v -> v.options().length > 0).forEach(v -> message.appendContent("\t" + v.description() + " -> ").appendContent(Arrays.stream(v.options()).collect(Collectors.joining(" | ", "( ", " )"))).appendContent("\n"));
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
                    message2.appendContent("```markdown\n# " + cont.getActions().channels().getInfo().botName + " - \"" + cont.getCommChar() + "\" #\n");
                    message2.appendContent("For more help type >help <command>\n");
                    message2.appendContent("Or visit <" + cont.getActions().channels().getInfo().website + ">\n");
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
                    if (!cont.getMessage().getChannel().isPrivate())
                    cont.getActions().channels().sendMessage(message.withContent(cont.getMessage().getAuthor().mention() + ", Check your PMs!"));
                } catch (DiscordException ex) {
                    throw BotException.returnException(ex);
                }
            });
        }
    }*/

    public void execute(CommContext cont) throws BotException {
        EmbedBuilder embed = new EmbedBuilder();
        if (cont.getArgs().size() >= 2) {
            String name = cont.getArgs().get(1);
            Command command = CommandList.getCommandList().stream().filter(v -> v.getClass().getAnnotation(CommandInfo.class).commandName().equals(name)).findAny().orElse(null);
            if (command != null) {
                CommandInfo commandInfo = command.getClass().getDeclaredAnnotation(CommandInfo.class);
                embed.withTitle(cont.getCommChar() + commandInfo.commandName() + " - " + commandInfo.helpText() + (commandInfo.pmSafe() ? " - `|PM|`" : ""));
                embed.appendDescription("`<>` means replace with your own value. `[]` means you can give more than one value.\n");
                embed.appendDescription("For more help, visit <" + cont.getActions().getConfig().url + "commands/" + commandInfo.commandName() + ".html>\n");
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
                        embed.appendField(title, content.toString(), false);
                    }
                }

            } else {
                throw new NotFoundException("Command", cont.getArgs().get(1));
            }
            cont.getActions().channels().sendMessage(cont.builder().withEmbed(embed.build()));
        } else {
            BotUtils.bufferRequest(() -> {
                try {
                    MessageBuilder message2 = new MessageBuilder(cont.getClient()).withChannel(cont.getClient().getOrCreatePMChannel(cont.getMessage().getAuthor()));
                    embed.withAuthorName(Information.botName + " - \"" + cont.getCommChar() + "\"");
                    embed.withAuthorIcon(cont.getActions().getConfig().url + "/avatar.png");
                    embed.withTitle("For more help type >help <command>");
                    embed.withDescription("Or visit " + cont.getActions().getConfig().url);
                    boolean userIsOp = cont.getActions().getTable(PermTable.class).getPerms(cont.getMessage().getAuthor(), cont.getMessage()).equals(Permissions.BOTOPERATOR);
                    for (Permissions perm : Permissions.values()) {
                        if (perm.equals(Permissions.BOTOPERATOR) && !userIsOp) {
                            continue;
                        }
                        String title = "[Permission]: " + perm.title;
                        StringBuilder content = new StringBuilder();
                        CommandList.getCommandList().stream().filter(comm -> comm.getClass().getDeclaredAnnotation(CommandInfo.class).permLevel().equals(perm))
                                .map(v -> v.getClass().getDeclaredAnnotation(CommandInfo.class)).forEach(comm -> content.append(cont.getCommChar()).append(comm.commandName()).append(" - ").append(comm.helpText()).append("\n"));
                        if (content.length() > 0)
                            embed.appendField(title, content.toString(), false);
                    }
                    LOG.info("is Embed too big? {}", embed.doesExceedCharacterLimit());
                    cont.getActions().channels().sendMessage(message2.withEmbed(embed.build()));
                    if (!cont.getMessage().getChannel().isPrivate())
                        cont.getActions().channels().sendMessage(cont.builder().withContent(cont.getMessage().getAuthor().mention() + ", Check your PMs!"));
                } catch (DiscordException ex) {
                    throw BotException.returnException(ex);
                }
            });
        }
    }
}

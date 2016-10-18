package rr.industries.commands;

import rr.industries.exceptions.BotException;
import rr.industries.util.*;
import rr.industries.util.sql.TagTable;
import sx.blah.discord.util.MessageBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Sam
 */
@CommandInfo(commandName = "tag", helpText = "Makes custom commands")
public class Tag implements Command {
    @SubCommand(name = "add", Syntax = {@Syntax(helpText = "Adds the Tag <Text> with the Content <Lots_of_Text>", args = {Arguments.TEXT, Arguments.LONGTEXT})}, permLevel = Permissions.REGULAR)
    public void add(CommContext cont) throws BotException {
        MessageBuilder message = cont.builder();
        String name = cont.getArgs().get(2);
        String content = cont.getConcatArgs(3);
        if (Arrays.stream(this.getClass().getMethods()).filter(v -> v.getAnnotation(SubCommand.class) != null && v.getAnnotation(SubCommand.class).name().equals(name)).findAny().isPresent()) {
            message.withContent("`" + name + "` is a protected name");
        } else {
            cont.getActions().getTable(TagTable.class).makeTag(cont.getMessage().getGuild(), name, content, false, cont.getCallerPerms());
            message.withContent("Successfully Created Tag `" + name + "`");
        }
        cont.getActions().channels().sendMessage(message);
    }

    @SubCommand(name = "remove", Syntax = {@Syntax(helpText = "Removes the command <Text>", args = {Arguments.TEXT})}, permLevel = Permissions.MOD)
    public void remove(CommContext cont) throws BotException {
        MessageBuilder message = new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getChannel());
        if (cont.getActions().getTable(TagTable.class).deleteTag(cont.getMessage().getGuild(), cont.getArgs().get(2), cont.getCallerPerms()).isPresent())
            message.withContent("Successfully removed tag `" + cont.getArgs().get(2) + "`");
        else
            message.withContent("Could not find tag `" + cont.getArgs().get(2) + "`");
        cont.getActions().channels().sendMessage(message);
    }

    @SubCommand(name = "global", Syntax = {@Syntax(helpText = "Sets the Tag <Text> as global or not", args = {Arguments.TEXT, Arguments.BOOLEAN})}, permLevel = Permissions.BOTOPERATOR)
    public void global(CommContext cont) throws BotException {
        String name = cont.getArgs().get(2);
        boolean global = Boolean.parseBoolean(cont.getArgs().get(3));
        MessageBuilder message = cont.builder();
        if (cont.getActions().getTable(TagTable.class).setGlobal(cont.getMessage().getGuild(), name, global, cont.getCallerPerms()).isPresent()) {
            message.withContent("Set the tag `" + name + "` as " + (global ? "" : "not ") + "global");
        } else {
            message.withContent("Could not find the Tag `" + name + "`");
        }
        cont.getActions().channels().sendMessage(message);
    }

    @SubCommand(name = "perm", Syntax = {@Syntax(helpText = "Sets the Tag <Text> as permanent or not", args = {Arguments.TEXT, Arguments.BOOLEAN})}, permLevel = Permissions.ADMIN)
    public void permanent(CommContext cont) throws BotException {
        String name = cont.getArgs().get(2);
        boolean permanent = Boolean.parseBoolean(cont.getArgs().get(3));
        MessageBuilder message = cont.builder();
        if (cont.getActions().getTable(TagTable.class).setPermanent(cont.getMessage().getGuild(), name, permanent, cont.getCallerPerms()).isPresent()) {
            message.withContent("Set the tag `" + name + "` as " + (permanent ? "" : "not ") + "permanent");
        } else {
            message.withContent("Could not find the Tag `" + name + "`");
        }
        cont.getActions().channels().sendMessage(message);
    }

    @SubCommand(name = "", Syntax = {
            @Syntax(helpText = "Tells you all of the tags for your Server", args = {}),
            @Syntax(helpText = "Displays the Tag Called <Text>", args = {Arguments.TEXT})})
    public void execute(CommContext cont) throws BotException {
        MessageBuilder message = new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getChannel());
        if (cont.getArgs().size() >= 2) {
            Optional<TagData> tag = cont.getActions().getTable(TagTable.class).getTag(cont.getMessage().getGuild(), cont.getArgs().get(1));
            if (tag.isPresent()) {
                message.withContent(tag.get().getContent());
            } else {
                message.withContent("Could not find a tag called `" + cont.getArgs().get(1) + "`");
            }
        } else {
            List<TagData> tags = cont.getActions().getTable(TagTable.class).getAllTags(cont.getMessage().getGuild());
            List<TagData> globalTags = cont.getActions().getTable(TagTable.class).getGlobalTags();
            message.withContent("```markdown\n# Tags ------ #\n").appendContent((tags.size() == 0 ? "No tags yet..." : tags.stream().map(v -> (v.isPermanent() ? "<" + v.getName() + ">" : v.getName())).collect(Collectors.joining(", "))));
            if (globalTags.size() > 0)
                message.appendContent("\n# Global Tags #\n" + globalTags.stream().map(TagData::getName).collect(Collectors.joining(", ")));
            message.appendContent("\n```");
        }
        cont.getActions().channels().sendMessage(message);
    }
}

package rr.industries.commands;

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
@CommandInfo(commandName = "tag", helpText = "Makes custom commands", permLevel = Permissions.REGULAR)
public class Tag implements Command {
    @SubCommand(name = "add", Syntax = {@Syntax(helpText = "Adds the Tag <Text> with the Content <Lots_of_Text>", args = {Arguments.TEXT, Arguments.LONGTEXT})})
    public void add(CommContext cont) {
        MessageBuilder message = cont.builder();
        String name = cont.getArgs().get(2);
        if (Arrays.asList(this.getClass().getMethods()).stream().filter(v -> v.getAnnotation(SubCommand.class) != null && v.getAnnotation(SubCommand.class).name().equals(name)).findAny().isPresent()) {
            message.withContent("`" + name + "` is a protected name...");
        } else {
            String content = cont.getConcatArgs(3);
            cont.getActions().getTable(TagTable.class).makeTag(cont.getMessage().getGuild(), name, content);
            message.withContent("Successfully created tag `" + name + "`");
        }
        cont.getActions().channels().sendMessage(message);
    }

    @SubCommand(name = "remove", Syntax = {@Syntax(helpText = "Removes the command <Text>", args = {Arguments.TEXT})})
    public void remove(CommContext cont) {
        MessageBuilder message = new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getChannel());
        boolean removed = cont.getActions().getTable(TagTable.class).deleteTag(cont.getMessage().getGuild(), cont.getArgs().get(2));
        if (removed)
            message.withContent("Successfully removed tag `" + cont.getArgs().get(2) + "`");
        else
            message.withContent("Could not find tag `" + cont.getArgs().get(2) + "`");
        cont.getActions().channels().sendMessage(message);
    }

    @SubCommand(name = "", Syntax = {
            @Syntax(helpText = "Tells you all of the tags for your Server", args = {}),
            @Syntax(helpText = "Displays the Tag Called <Text>", args = {Arguments.TEXT})})
    public void execute(CommContext cont) {
        MessageBuilder message = new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getChannel());
        if (cont.getArgs().size() >= 2) {
            Optional<String> tag = cont.getActions().getTable(TagTable.class).getTag(cont.getMessage().getGuild(), cont.getArgs().get(1));
            if (tag.isPresent()) {
                message.withContent(tag.get());
            } else {
                message.withContent("Could not find a tag called `" + cont.getArgs().get(1) + "`");
            }
        } else {
            List<String> tags = cont.getActions().getTable(TagTable.class).getAllTags(cont.getMessage().getGuild());
            message.withContent("**Tags -----**\n").appendCode("", (tags.size() == 0 ? "No tags yet..." : tags.stream().collect(Collectors.joining(", "))));
        }
        cont.getActions().channels().sendMessage(message);
    }
}

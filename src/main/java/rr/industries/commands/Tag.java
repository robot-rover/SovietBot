package rr.industries.commands;

import discord4j.core.object.entity.Channel;
import reactor.core.publisher.Mono;
import rr.industries.exceptions.BotException;
import rr.industries.exceptions.PMNotSupportedException;
import rr.industries.util.*;
import rr.industries.util.sql.TagTable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Sam
 */
@CommandInfo(commandName = "tag", helpText = "Makes custom commands", pmSafe = true)
public class Tag implements Command {
    @SubCommand(name = "add", Syntax = {@Syntax(helpText = "Creates a new Tag", args = {
            @Argument(description = "Tag Name", value = Validate.TEXT),
            @Argument(description = "", value = Validate.LONGTEXT)})
    },
            permLevel = Permissions.REGULAR, pmSafe = false)
    public Mono<Void> add(CommContext cont) throws BotException {
        StringBuilder message = new StringBuilder();
        String name = cont.getArgs().get(2);
        String content = cont.getConcatArgs(3);
        if (Arrays.stream(this.getClass().getMethods()).anyMatch(v -> v.getAnnotation(SubCommand.class) != null && v.getAnnotation(SubCommand.class).name().equals(name))) {
            message.append("`" + name + "` is a protected name");
        } else {
            cont.getActions().getTable(TagTable.class).makeTag(cont.getMessage().getGuildId().orElseThrow(PMNotSupportedException::new), name, content,  cont.getCallerPerms());
            message.append("Successfully Created Tag `" + name + "`");
        }
        return cont.getChannel().createMessage(message.toString()).then();
    }

    @SubCommand(name = "remove", Syntax = {@Syntax(helpText = "Removes the Tag", args = {@Argument(description = "Command Name", value = Validate.TEXT)})}, permLevel = Permissions.MOD, pmSafe = false)
    public Mono<Void> remove(CommContext cont) throws BotException {
        StringBuilder message = new StringBuilder();
        if (cont.getActions().getTable(TagTable.class).deleteTag(cont.getMessage().getGuildId().orElseThrow(PMNotSupportedException::new), cont.getArgs().get(2), cont.getCallerPerms()) != null)
            message.append("Successfully removed tag `" + cont.getArgs().get(2) + "`");
        else
            message.append("Could not find tag `" + cont.getArgs().get(2) + "`");
        return cont.getChannel().createMessage(message.toString()).then();
    }

    @SubCommand(name = "global", Syntax = {@Syntax(helpText = "Sets the Tag as global or not", args = {
            @Argument(description = "Tag Name", value = Validate.TEXT),
            @Argument(description = "Global?", value = Validate.BOOLEAN, options = {"True", "False"})
    })}, permLevel = Permissions.BOTOPERATOR, pmSafe = false)
    public Mono<Void> global(CommContext cont) throws BotException {
        String name = cont.getArgs().get(2);
        boolean global = Boolean.parseBoolean(cont.getArgs().get(3));
        StringBuilder message = new StringBuilder();
        if (cont.getActions().getTable(TagTable.class).setGlobal(cont.getMessage().getGuildId().orElseThrow(PMNotSupportedException::new), name, global, cont.getCallerPerms())) {
            message.append("Set the tag `" + name + "` as " + (global ? "" : "not ") + "global");
        } else {
            message.append("Could not find the Tag `" + name + "`");
        }
        return cont.getChannel().createMessage(message.toString()).then();
    }

    @SubCommand(name = "perm", Syntax = {@Syntax(helpText = "Sets the Tag as permanent or not", args = {
            @Argument(description = "Tag Name", value = Validate.TEXT),
            @Argument(description = "Permanent?", value = Validate.BOOLEAN, options = {"True", "False"})})}, permLevel = Permissions.ADMIN, pmSafe = false)
    public Mono<Void> permanent(CommContext cont) throws BotException {
        String name = cont.getArgs().get(2);
        boolean permanent = Boolean.parseBoolean(cont.getArgs().get(3));
        StringBuilder message = new StringBuilder();
        if (cont.getActions().getTable(TagTable.class).setPermanent(cont.getMessage().getGuildId().orElseThrow(PMNotSupportedException::new), name, permanent, cont.getCallerPerms())) {
            message.append("Set the tag `" + name + "` as " + (permanent ? "" : "not ") + "permanent");
        } else {
            message.append("Could not find the Tag `" + name + "`");
        }
        return cont.getChannel().createMessage(message.toString()).then();
    }

    @SubCommand(name = "", Syntax = {
            @Syntax(helpText = "Tells you all of the tags for your Server", args = {}),
            @Syntax(helpText = "Displays the Tag Called <Text>", args = {@Argument(description = "Tag Name", value = Validate.TEXT)})})
    public Mono<Void> execute(CommContext cont) throws BotException {
        StringBuilder message = new StringBuilder();
        Mono exec = Mono.empty();
        if (cont.getArgs().size() >= 2) {
            TagData tag = cont.getActions().getTable(TagTable.class).getTag(cont.getMessage().getGuildId().orElseThrow(PMNotSupportedException::new), cont.getArgs().get(1));
            if (tag != null) {
                message.append(tag.getContent());
            } else {
                message.append("Could not find a tag called `" + cont.getArgs().get(1) + "`");
            }
        } else {
            message.append("```markdown\n");
            if (cont.getChannel().getType().equals(Channel.Type.GUILD_TEXT)) {
                List<TagData> tags = cont.getActions().getTable(TagTable.class).getAllTags(cont.getMessage().getGuildId().orElseThrow(PMNotSupportedException::new));
                message.append("# Tags ------ #\n").append((tags.size() == 0 ? "No tags yet..." : tags.stream().map(v -> (v.isPermanent() ? "<" + v.getName() + ">" : v.getName())).collect(Collectors.joining(", "))));
            }
            List<TagData> globalTags = cont.getActions().getTable(TagTable.class).getGlobalTags();
            if (globalTags.size() > 0)
                message.append("\n# Global Tags #\n" + globalTags.stream().map(TagData::getName).collect(Collectors.joining(", ")));
            message.append("\n```");
        }
        return cont.getChannel().createMessage(message.toString()).then();
    }
}

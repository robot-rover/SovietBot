package rr.industries.commands;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;
import rr.industries.exceptions.PMNotSupportedException;
import rr.industries.modules.SwearFilter;
import rr.industries.util.*;
import rr.industries.util.sql.GreetingTable;

@CommandInfo(commandName = "filter", helpText = "Toggles the profanity filter", permLevel = Permissions.ADMIN)
public class Filter implements Command {

    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Toggles the filter", args = {}), @Syntax(helpText = "Sets the filter on or off", args = @Argument(Validate.BOOLEAN))})
    public Mono<Void> execute(CommContext cont) throws PMNotSupportedException {
        GreetingTable table = cont.getActions().getTable(GreetingTable.class);
        if (cont.getArgs().size() < 2) {
            return cont.getMessage().getGuild().map(Guild::getId).map(table::shouldFilter).map(Filter::booleanToWord).flatMap(v -> cont.getChannel().createMessage("Profanity filter is " + v)).then();
        }

        boolean setFilter = Boolean.parseBoolean(cont.getArgs().get(1));
        Snowflake guildId = cont.getMessage().getGuildId().orElseThrow(PMNotSupportedException::new);
        table.setFilter(guildId, setFilter);

        return cont.getMessage().getMessage().getChannel()
        .flatMap(v -> v.createMessage("Setting profanity filter " + booleanToWord(setFilter)))
        .then();
    }

    private static String booleanToWord(boolean state) {
        return (state ? "On" : "Off");
    }

    @SubCommand(name = "reload", Syntax = {@Syntax(helpText = "Toggles the filter", args = {}), @Syntax(helpText = "Sets the filter on or off", args = @Argument(Validate.BOOLEAN))}, permLevel = Permissions.BOTOPERATOR)
    public Mono<Void> reload(CommContext cont) {
        cont.getActions().getModule(SwearFilter.class).loadConfig();
        return cont.getMessage().getMessage().getChannel().flatMap(v -> v.createMessage("Reloaded Config")).then();
    }
}

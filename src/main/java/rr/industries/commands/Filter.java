package rr.industries.commands;

import rr.industries.modules.SwearFilter;
import rr.industries.util.*;
import rr.industries.util.sql.FilterTable;

@CommandInfo(commandName = "filter", helpText = "Toggles the profanity filter", permLevel = Permissions.ADMIN)
public class Filter implements Command {

    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Toggles the filter", args = {}), @Syntax(helpText = "Sets the filter on or off", args = @Argument(Validate.BOOLEAN))})
    public void execute(CommContext cont){
        FilterTable table = cont.getActions().getTable(FilterTable.class);
        boolean setFilter;
        if(cont.getArgs().size() < 2){
            setFilter = !table.shouldFilter(cont.getMessage().getGuild().getLongID());
        } else {
            setFilter = Boolean.parseBoolean(cont.getArgs().get(1));
        }

        table.setFilter(cont.getMessage().getGuild().getLongID(), setFilter);
        cont.getActions().channels().sendMessage(cont.builder().withContent("Setting profanity filter " + (setFilter ? "On" : "Off")));
    }

    @SubCommand(name = "reload", Syntax = {@Syntax(helpText = "Toggles the filter", args = {}), @Syntax(helpText = "Sets the filter on or off", args = @Argument(Validate.BOOLEAN))}, permLevel = Permissions.BOTOPERATOR)
    public void reload(CommContext cont){
        cont.getActions().getModule(SwearFilter.class).loadConfig();
        cont.getActions().channels().sendMessage(cont.builder().withContent("Reloaded config!"));
    }
}

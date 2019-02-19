package rr.industries.commands;

import reactor.core.publisher.Mono;
import rr.industries.Information;
import rr.industries.exceptions.BotException;
import rr.industries.util.CommContext;
import rr.industries.util.CommandInfo;
import rr.industries.util.SubCommand;
import rr.industries.util.Syntax;
import rr.industries.util.sql.GreetingTable;

@CommandInfo(
        commandName = "info",
        helpText = "Displays basic bot info."
)
public class Info implements Command {
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Shows you interesting things such as the bots author and invite link", args = {})})
    public Mono<Void> execute(CommContext cont) throws BotException {
        return cont.getActions().getTable(GreetingTable.class).getPrefix(cont.getMessage().getMessage()).flatMap(prefix -> cont.getChannel().createMessage(messageSpec -> messageSpec.setEmbed(embedSpec -> {
            embedSpec.setTitle("Info");
            embedSpec.setAuthor(Information.botName, cont.getActions().getConfig().url, cont.getActions().getConfig().url + "avatar.png");
            embedSpec.addField("Created with", Information.frameName, false);
            embedSpec.addField("For help type", "`" + prefix + Information.helpCommand + "`", false);
            embedSpec.addField("Created By", "**@" + Information.author + "**", false);
            embedSpec.addField("Website", cont.getActions().getConfig().url, false);
            embedSpec.addField("Invite Link", Information.invite, false);
        }))).then();
    }
}
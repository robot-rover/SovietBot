/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rr.industries.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.util.sql.PermTable;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CommContext {
    private static final Logger LOG = LoggerFactory.getLogger(CommContext.class);
    private final List<String> args = new ArrayList<>();
    private final MessageReceivedEvent e;
    private Permissions callerPerms;
    private final BotActions actions;

    public CommContext(MessageReceivedEvent e, BotActions actions) {
        this.actions = actions;
        this.e = e;
        if (e.getMessage().getChannel().isPrivate()) {
            callerPerms = Permissions.NORMAL;
        } else {
            callerPerms = actions.getTable(PermTable.class).getPerms(e.getMessage().getAuthor(), e.getMessage().getGuild());
        }
        boolean next = true;
        Scanner parser = new Scanner(e.getMessage().getContent());
        while (parser.hasNext()) {
            args.add(parser.next());
        }
        if (args.get(0).startsWith(actions.getConfig().commChar)) {
            args.set(0, args.get(0).substring(actions.getConfig().commChar.length()));
        }
    }

    public String getConcatArgs(int first) {
        StringBuilder concatArgs = new StringBuilder("");
        for (int i = first; i < args.size(); i++) {
            concatArgs.append(args.get(i));
            if (i + 1 < args.size())
                concatArgs.append(" ");
        }
        return concatArgs.toString();
    }

    public BotActions getActions() {
        return actions;
    }

    public Permissions getCallerPerms() {
        return callerPerms;
    }

    public List<String> getArgs() {
        return args;
    }

    public MessageReceivedEvent getMessage() {
        return e;
    }

    public IDiscordClient getClient() {
        return actions.getClient();
    }

    public String getCommChar() {
        return actions.getConfig().commChar;
    }
}

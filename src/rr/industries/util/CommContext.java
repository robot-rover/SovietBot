/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rr.industries.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.Configuration;
import rr.industries.util.sql.SQLUtils;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CommContext {
    private static final Logger LOG = LoggerFactory.getLogger(CommContext.class);
    private final List<String> args = new ArrayList<>();
    private final MessageReceivedEvent e;
    private Permissions callerPerms;
    private final BotActions actions;
    boolean messageErased;

    public CommContext(MessageReceivedEvent e, BotActions actions) {
        messageErased = false;
        this.actions = actions;
        this.e = e;
        Statement sql = actions.getSQL();
        if (e.getMessage().getChannel().isPrivate()) {
            callerPerms = Permissions.NORMAL;
        } else {
            callerPerms = SQLUtils.getPerms(e.getMessage().getAuthor().getID(), e.getMessage().getGuild().getID(), sql, actions);
        }
        if (e.getMessage().getAuthor().getID().equals("141981833951838208")) {
            callerPerms = Permissions.BOTOPERATOR;
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

    public String getConcatArgs() {
        String concatArgs = "";
        for (int i = 1; i < args.size(); i++) {
            concatArgs = concatArgs.concat(args.get(i) + " ");
        }
        return concatArgs.substring(0, concatArgs.length() > 0 ? concatArgs.length() - 1 : 0);
    }

    public BotActions getActions() {
        return actions;
    }

    public Configuration getConfig() {
        return actions.getConfig();
    }

    public Permissions getCallerPerms() {
        return callerPerms;
    }

    public List<String> getArgs() {
        return args;
    }

    public MessageReceivedEvent getMessage() {
        if (messageErased) {
            LOG.error("Tried to access deleted message!!!", new IllegalAccessException("This message has been deleted. No access for u!"));
            return null;
        }
        return e;
    }

    public IDiscordClient getClient() {
        return actions.getClient();
    }

    public String getCommChar() {
        return actions.getConfig().commChar;
    }

    public void eraseMessage() {
        messageErased = true;
    }
}

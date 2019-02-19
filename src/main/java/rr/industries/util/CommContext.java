/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rr.industries.util;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.util.Snowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import rr.industries.exceptions.PMNotSupportedException;
import rr.industries.util.sql.GreetingTable;
import rr.industries.util.sql.PermTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CommContext {
    private static final Logger LOG = LoggerFactory.getLogger(CommContext.class);
    private final List<String> args;
    private String commChar;
    private Permissions callerPerms;
    private final BotActions actions;
    private final MessageCreateEvent event;
    private final MessageChannel channel;

    public CommContext(List<String> args, String commChar, Permissions callerPerms, BotActions actions, MessageCreateEvent event, MessageChannel channel) {
        this.args = args;
        this.commChar = commChar;
        this.callerPerms = callerPerms;
        this.actions = actions;
        this.event = event;
        this.channel = channel;
    }

    public String getConcatArgs(int first) {
        if (first >= args.size()) {
            return "";
        }
        List<String> concat = args;
        for (int i = 0; i < first && args.size() > 0; i++)
            concat.remove(0);
        return String.join(" ", concat);
    }

    public static Mono<CommContext> getCommContext(MessageCreateEvent e, BotActions actions) {
        Mono<String> prefix = actions.getTable(GreetingTable.class).getPrefix(e.getMessage());
        Mono<Permissions> callerPerm = e.getMessage().getAuthor()
                .map(user -> actions.getTable(PermTable.class).getPerms(user, e.getMessage()))
                .orElse(Mono.just(Permissions.NORMAL));
        Mono<List<String>> argList = prefix.map(v -> processArgs(v, e.getMessage().getContent().orElse("")));
        Mono<MessageChannel> channel = e.getMessage().getChannel();
        return Mono.zip(prefix, callerPerm, argList, channel).map(v -> new CommContext(v.getT3(), v.getT1(), v.getT2(), actions, e, v.getT4()));
    }

    private static List<String> processArgs(String commChar, String content) {
        Scanner parser = new Scanner(content);
        List<String> argList = new ArrayList<>();
        while (parser.hasNext()) {
            argList.add(parser.next());
        }
        if (argList.get(0).startsWith(commChar)) {
            argList.set(0, argList.get(0).substring(commChar.length()));
        }
        return argList;
    }

    public Snowflake getGuildId() throws PMNotSupportedException {
        return event.getGuildId().orElseThrow(PMNotSupportedException::new);
    }

    public Member getMember() throws PMNotSupportedException {
        return event.getMember().orElseThrow(PMNotSupportedException::new);
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

    public MessageCreateEvent getMessage() {
        return event;
    }

    public DiscordClient getClient() {
        return actions.getClient();
    }

    public String getCommChar() {
        return commChar;
    }

    public MessageChannel getChannel() {
        return channel;
    }
}

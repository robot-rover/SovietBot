/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rr.industries.util;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class CommContext {
    private final List<String> args = new ArrayList<>();
    private MessageReceivedEvent e;
    private IDiscordClient client;
    private String commChar;

    public CommContext(MessageReceivedEvent e, IDiscordClient client, String commChar) {
        this.e = e;
        this.commChar = commChar;
        this.client = client;
        boolean next = true;
        Scanner parser = new Scanner(e.getMessage().getContent());
        while (next) {
            try {
                args.add(parser.next());
            } catch (NoSuchElementException | NullPointerException ex) {
                next = false;
            }
        }
        if (args.get(0).startsWith(commChar)) {
            args.set(0, args.get(0).substring(commChar.length()));
        }
    }

    public List<String> getArgs() {
        return args;
    }

    public MessageReceivedEvent getMessage() {
        return e;
    }

    public IDiscordClient getClient() {
        return client;
    }

    public String getCommChar() {
        return commChar;
    }
}

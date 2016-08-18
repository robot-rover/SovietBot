/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Main;

import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * @author Sam
 */
class CommContext {
    private List<String> args = new ArrayList<>();
    private MessageReceivedEvent e;
    private String message;
    private IChannel channel;

    CommContext(MessageReceivedEvent e, String commChar) {
        this.e = e;
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

    CommContext(IChannel channel, String message) {
        this.channel = channel;
        this.message = message;
    }

    List<String> getArgs() {
        return args;
    }

    MessageReceivedEvent getMessage() {
        return e;
    }

    String getReturnMessage() {
        return message;
    }

    IChannel getChannel() {
        if (e == null) {
            return channel;
        } else {
            return e.getMessage().getChannel();
        }
    }
}

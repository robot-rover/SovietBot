/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * @author Sam
 */
public class commContext {
    private List<String> args = new ArrayList<String>();
    private MessageReceivedEvent e;

    public commContext(MessageReceivedEvent e, String commChar) {
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

    List<String> getArgs() {
        return args;
    }

    MessageReceivedEvent getMessage() {
        return e;
    }

}

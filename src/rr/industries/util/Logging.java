package rr.industries.util;

import org.slf4j.Logger;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Sam on 8/28/2016
 */
public class Logging {
    public static void threadInterrupted(InterruptedException ex, String methodName, Logger log) {
        log.debug("The Method " + methodName + "'s Sleep was interrupted - ", ex);
    }

    public static void notFound(MessageReceivedEvent e, String methodName, String type, String name, Logger log) {
        log.info(methodName + " failed to find " + type + ": \"" + name + "\" in Server: \"" + e.getMessage().getGuild().getName() + "\"");
    }

    public static void customException(MessageReceivedEvent e, String methodName, String message, Exception ex, Logger log) {
        log.info(methodName + " - Server: \"" + e.getMessage().getGuild().getName() + "\": " + message);
        if (ex != null) {
            log.debug("Full Stack Trace - ", ex);
        }
    }

    public static void missingPermissions(IChannel channel, String methodName, MissingPermissionsException ex, Logger log) {
        log.info(methodName + ": " + ex.getErrorMessage() + " on Server: \"" + channel.getGuild().getName() + "\" in channel: " + channel.getName());
    }

    public static void missingArgs(MessageReceivedEvent e, String methodName, List<String> args, Logger log) {
        log.info(methodName + " called without enough arguments: " + args.toString() + " in Server: \"" + e.getMessage().getGuild().getName() + "\"");
    }

    public static void wrongArgs(MessageReceivedEvent e, String methodName, List<String> args, Logger log) {
        log.info(methodName + " called with incomplete arguments: " + args.toString() + " in Server: \"" + e.getMessage().getGuild().getName() + "\"");
    }

    public static void error(IGuild guild, String methodName, Exception ex, Logger log) {
        log.error(methodName + " in the Server: \"" + guild.getName() + "\" returned error: " + ex.getMessage());
        log.debug("Full Stack Trace - ", ex);
    }

    public static void rateLimit(RateLimitException ex, Consumer<CommContext> method, CommContext args, Logger log) {
        log.debug("Rate Limited - ", ex);
        try {
            Thread.sleep(ex.getRetryDelay());
        } catch (InterruptedException e) {
            log.debug("RateLimit Sleep Interrupted, Cancelling retry.");
            return;
        }
        method.accept(args);
    }

}

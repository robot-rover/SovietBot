package rr.industries.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.SovietBot;
import rr.industries.util.CommContext;
import rr.industries.util.Permissions;

import java.util.Random;

/**
 * Created by Sam on 8/28/2016.
 */
public abstract class Command {
    static final Random rn = new Random();
    static final Logger LOG = LoggerFactory.getLogger(Command.class);
    static final ClassLoader resourceLoader = SovietBot.resourceLoader;
    public Permissions permLevel = Permissions.NORMAL;
    public String commandName = "notConfigured";
    public String helpText = "notConfigured";
    public boolean deleteMessage = true;

    public abstract void execute(CommContext cont);
}

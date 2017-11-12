package rr.industries.modules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.SovietBot;
import rr.industries.exceptions.BotException;
import rr.industries.util.BotActions;
import rr.industries.util.ChannelActions;

import java.util.Scanner;

/**
 * @author robot_rover
 */
public class Console implements Module {
    private static Logger LOG = LoggerFactory.getLogger(Console.class);
    boolean isEnabled;
    Thread listner;
    BotActions actions;
    Scanner input;

    public Console() {
        isEnabled = false;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public Module enable(BotActions actions) {
        isEnabled = true;
        this.actions = actions;
        input = new Scanner(System.in);
        listner = new Thread(() -> {

            while (isEnabled) {
                try {
                    switch (input.nextLine()) {
                        case "stop":
                            System.out.println("Shutting Down...");
                            try {
                                actions.channels().terminate(false);
                            } catch (BotException e) {
                                LOG.error("Could not Shutdown", e);
                            }
                            break;
                        case "spark stop":
                            System.out.println("Stopping Spark...");
                            SovietBot.getBotActions().getModule(Webserver.class).disable();
                            break;
                        case "spark start":
                            System.out.println("Starting Spark...");
                            SovietBot.getBotActions().getModule(Webserver.class).enable(actions);
                            break;
                    }
                } catch (IllegalStateException ex) {
                    break;
                }
            }

        });
        listner.start();
        return this;
    }

    @Override
    public Module disable() {
        isEnabled = false;
        System.out.println();
        listner = null;
        return this;
    }
}

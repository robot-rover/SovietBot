package rr.industries.modules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.exceptions.BotException;
import rr.industries.util.ChannelActions;

import java.util.Scanner;

/**
 * @author robot_rover
 */
public class Console implements Module {
    private static Logger LOG = LoggerFactory.getLogger(Console.class);
    boolean isEnabled;
    Thread listner;
    ChannelActions actions;
    Scanner input;

    public Console(ChannelActions actions) {
        this.actions = actions;
        isEnabled = false;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public Module enable() {
        isEnabled = true;
        input = new Scanner(System.in);
        listner = new Thread() {
            @Override
            public void run() {

                while (isEnabled) {
                    try {
                        switch (input.nextLine()) {
                            case "stop":
                                System.out.println("Shutting Down...");
                                try {
                                    actions.terminate(false);
                                } catch (BotException e) {
                                    LOG.error("Could not Shutdown", e);
                                }
                                break;
                        }
                    } catch (IllegalStateException ex) {
                        break;
                    }
                }

            }
        };
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

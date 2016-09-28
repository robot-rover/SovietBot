package rr.industries.modules;

import rr.industries.util.ChannelActions;

import java.util.Scanner;

/**
 * @author Sam
 * @project sovietBot
 * @created 9/16/2016
 */
public class Console implements Module {
    boolean isEnabled;
    Thread listner;
    ChannelActions actions;

    public Console(ChannelActions actions) {
        this.actions = actions;
        isEnabled = false;
        listner = new Thread() {
            @Override
            public void run() {
                Scanner input = new Scanner(System.in);
                while (isEnabled) {
                    switch (input.nextLine()) {
                        case "stop":
                            System.out.println("Shutting Down...");
                            actions.terminate(false);
                            break;
                        default:
                            System.out.println("Unrecognized Command...");
                    }
                }

            }
        };
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public Module enable() {
        isEnabled = true;
        listner.start();
        return this;
    }

    @Override
    public Module disable() {
        isEnabled = false;
        return this;
    }
}

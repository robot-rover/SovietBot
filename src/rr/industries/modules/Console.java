package rr.industries.modules;

import rr.industries.util.BotActions;

import java.util.Scanner;

/**
 * @author Sam
 * @project sovietBot
 * @created 9/16/2016
 */
public class Console implements Module {
    boolean isEnabled;
    Thread listner;
    BotActions actions;

    public Console(BotActions actions) {
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
    public void enable() {
        isEnabled = true;
        listner.start();
    }

    @Override
    public void disable() {
        isEnabled = false;
    }
}

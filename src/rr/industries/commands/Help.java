package rr.industries.commands;

import rr.industries.util.CommContext;

/**
 * Created by Sam on 8/28/2016.
 */
public class Help extends Command {
    public Help() {
        commandName = "help";
        helpText = "Displays this help message";
    }

    @Override
    public void execute(CommContext cont) {
        //todo: make help work
    }
}

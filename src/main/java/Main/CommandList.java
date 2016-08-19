package Main;

import java.util.NoSuchElementException;

class CommandList {
    String commChar;
    Command[] commands;

    Command getCommand(String name) throws NoSuchElementException {
        Command command = null;
        for (Command com : commands) {
            if (com.commandName.equals(name)) {
                command = com;
            }
        }
        if (command == null) {
            throw new NoSuchElementException("No command named " + name + " found.");
        }
        return command;
    }

    class Command {
        String commandName;
        String helpText;
        boolean delete;



        public Command() {
        }

        @Override
        public String toString() {
            return commandName + " - " + helpText;
        }

    }

    @Override
    public String toString() {
        String printOut = "";
        for (Command com : commands) {
            printOut = printOut + "\n" + commChar + com.commandName + " - " + com.helpText;
        }
        printOut = "Command Character - \"" + commChar + "\"" + printOut;
        return printOut;
    }
}

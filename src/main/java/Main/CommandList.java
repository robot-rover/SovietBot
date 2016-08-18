package Main;

class CommandList {
    String commChar;
    Command[] commands;

    class Command {
        String commandName;
        String helpText;

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

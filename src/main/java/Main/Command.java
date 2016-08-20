package Main;

/**
 * Created by Sam on 8/19/2016.
 */
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

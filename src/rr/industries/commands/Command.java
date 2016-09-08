package rr.industries.commands;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.SovietBot;
import rr.industries.util.CommContext;
import rr.industries.util.CommandInfo;

import java.util.Random;

@CommandInfo(commandName = "help", helpText = "derpydoodahday")
public interface Command {
    Random rn = new Random();
    Logger LOG = LoggerFactory.getLogger(Command.class);
    ClassLoader resourceLoader = SovietBot.resourceLoader;
    Gson gson = new Gson();

    void execute(CommContext cont);
}

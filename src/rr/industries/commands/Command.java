package rr.industries.commands;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.SovietBot;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public interface Command {
    Random rn = new Random();
    Logger LOG = LoggerFactory.getLogger(Command.class);
    Gson gson = SovietBot.gson;

    default Predicate<List<String>> getValiddityOverride() {
        return null;
    }
}

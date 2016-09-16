package rr.industries.commands;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.Instance;
import rr.industries.SovietBot;

import java.util.Random;

public interface Command {
    Random rn = new Random();
    Logger LOG = LoggerFactory.getLogger(Command.class);
    ClassLoader resourceLoader = SovietBot.resourceLoader;
    Gson gson = Instance.gson;
}

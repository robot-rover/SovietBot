package rr.industries.commands;

import net.aksingh.owmjapis.CurrentWeather;
import net.aksingh.owmjapis.OpenWeatherMap;
import rr.industries.util.*;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

@CommandInfo(
        commandName = "test",
        helpText = "Temporary command for testing new features",
        permLevel = Permissions.BOTOPERATOR
)
public class Test implements Command {
    @SubCommand(name = "tester", Syntax = {@Syntax(helpText = "Test the tester test", args = {})})
    public void testSub(CommContext cont) {
        try {
            cont.getMessage().getMessage().reply("The test worked");
        } catch (MissingPermissionsException e) {
            e.printStackTrace();
        } catch (RateLimitException e) {
            e.printStackTrace();
        } catch (DiscordException e) {
            e.printStackTrace();
        }
    }

    @SubCommand(name = "weather", Syntax = {})
    public void testWeather(CommContext cont) {
        OpenWeatherMap map = new OpenWeatherMap(cont.getActions().getConfig().owmKey);
        CurrentWeather current = map.currentWeatherByCityCode(Long.parseLong(cont.getArgs().get(2)));
        LOG.info("Weather:\nCity Name: {}\nBase Station: {}\nTemperature: {}{}", current.getCityName(), current.getBaseStation(), current.getMainInstance().getTemperature(), "\u00B0F");
    }
}

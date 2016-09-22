package rr.industries.commands;

import net.aksingh.owmjapis.CurrentWeather;
import net.aksingh.owmjapis.OpenWeatherMap;
import org.apache.commons.io.IOUtils;
import rr.industries.Instance;
import rr.industries.geoCoding.GeoCoding;
import rr.industries.geoCoding.Result;
import rr.industries.util.*;
import sx.blah.discord.util.MessageBuilder;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;


@CommandInfo(
        commandName = "weather",
        helpText = "[Coming Soon] Interface for getting the weather in your area.",
        permLevel = Permissions.BOTOPERATOR
)
public class Weather implements Command {
    private static String deg = "\u00B0F";

    @SubCommand(name = "", Syntax = {
            @Syntax(helpText = "Displays the current weather in your area", args = {Arguments.CITY, Arguments.COUNTRY})/*,
            @Syntax(helpText = "Finds your area with just a city", args = {Arguments.CITY})*/
    })
    public void execute(CommContext cont) {
        try {
            MessageBuilder message = new MessageBuilder(cont.getClient()).withChannel(cont.getMessage().getChannel());
            URL url = new URL("https://maps.googleapis.com/maps/api/geocode/json?address=" + cont.getArgs().get(1) + ",+" + cont.getArgs().get(2));
            URLConnection con = url.openConnection();
            GeoCoding response = Instance.gson.fromJson(IOUtils.toString(con.getInputStream()), GeoCoding.class);
            if (response.results.size() == 0) {
                cont.getActions().sendMessage(message.withContent("Location was not Found..."));
                return;
            }
            Result location = response.results.get(0);
            CurrentWeather weather = new OpenWeatherMap(OpenWeatherMap.Units.IMPERIAL, cont.getActions().getConfig().owmKey).currentWeatherByCoordinates((float) location.geometry.location.lat, (float) location.geometry.location.lng);
            message.appendContent("Weather in **" + weather.getCityName() + "**\n");
            if (weather.hasMainInstance()) {
                CurrentWeather.Main set = weather.getMainInstance();
                message.appendContent(Math.round(set.getTemperature()) + deg + " (" + Math.round(set.getMaxTemperature()) + deg + "/" + Math.round(set.getMinTemperature()) + deg + ")\n");
                message.appendContent(Math.round(set.getHumidity()) + "% Humidity\n");
            }
            if (weather.hasRainInstance()) {
                CurrentWeather.Rain set = weather.getRainInstance();
                message.appendContent(Integer.toString(Math.round(set.getRain() * 100) / 100) + "mm of rain in last 3h\n");
            }
            cont.getActions().sendMessage(message);
        } catch (IOException ex) {
            cont.getActions().customException("Weather", ex.getMessage(), ex, LOG, true);
        }
    }

    /*@SubCommand(name = "set", Syntax = {@Syntax(helpText = "Sets your zip code", args = {Arguments.NUMBER})})
    public void set(CommContext cont) {
        OpenWeatherMap map = new OpenWeatherMap(OpenWeatherMap.Units.IMPERIAL, cont.getActions().getConfig().owmKey);
    }*/
}

package org.bitpipeline.lib.owm;

import org.json.JSONException;

import java.io.IOException;

/**
 * A demo to show how to use this API.
 *
 * @author moretgio https://github.com/moretgio
 * @author Ayutac https://github.com/Ayutac
 */
public class DEMO {

    public static void main(String[] args) throws IOException, JSONException {
        OwmClient client = new OwmClient(OwmClient.Units.METRIC);
        client.setAPPID("c4cb05905b0c1017d58221beda81460d");

        WeatherStatusResponse status = client.currentWeatherAtCity("Zurich");
        System.out.println(status.getWeatherStatus().get(0).getMain().getTemp());
        System.out.println(status.getWeatherStatus().get(0).getMain().getTempMax());
        System.out.println(status.getWeatherStatus().get(0).getMain().getTempMin());


        WeatherForecastResponse r = client.forecastWeatherAtCity("Zurich");
        System.out.println(r.getForecasts().get(0).getMain().getTemp());
        System.out.println(r.getForecasts().get(0).getMain().getTempMax());
        System.out.println(r.getForecasts().get(0).getMain().getTempMin());

        WeatherForecast16Response r16 = client.dailyForecastWeatherAtCity("Zurich");
        System.out.println(r16.getForecasts().get(1).getTemperature().getTempDay());

    }

}

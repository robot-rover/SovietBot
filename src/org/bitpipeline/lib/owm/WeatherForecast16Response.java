package org.bitpipeline.lib.owm;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WeatherForecast16Response extends AbstractOwmResponse {

    private final List<Forecast16WeatherData> forecasts;

    public WeatherForecast16Response(JSONObject json) {
        super(json);
        JSONArray jsonForecasts = json
                .optJSONArray(AbstractOwmResponse.JSON_LIST);
        if (jsonForecasts != null) {
            this.forecasts = new ArrayList<Forecast16WeatherData>(
                    jsonForecasts.length());

            for (int i = 0; i < jsonForecasts.length(); i++) {
                JSONObject jsonForecast = jsonForecasts.optJSONObject(i);
                this.forecasts.add(new Forecast16WeatherData(jsonForecast));
            }
        } else {
            this.forecasts = Collections.emptyList();
        }
    }

    public boolean hasForecasts() {
        return this.forecasts != null && !this.forecasts.isEmpty();
    }

    public List<Forecast16WeatherData> getForecasts() {
        return this.forecasts;
    }

}

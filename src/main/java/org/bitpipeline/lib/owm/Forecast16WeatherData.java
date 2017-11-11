package org.bitpipeline.lib.owm;

import org.json.JSONObject;

public class Forecast16WeatherData extends LocalizedWeatherData {

    public static final String JSON_WIND = "speed";
    public static final String JSON_DATETIME = "dt";
    public static final String JSON_PRESSURE = "pressure";
    public static final String JSON_HUMIDITY = "humidity";

    private Temperature temp;
    private float pressure;
    private float humidity;
    private float windSpeed;
    private long dateTime;

    public Forecast16WeatherData(JSONObject json) {
        super(json);
        this.pressure = (float) json.optDouble(JSON_PRESSURE);
        this.humidity = (float) json.optDouble(JSON_HUMIDITY);
        this.windSpeed = (float) json.optDouble(JSON_WIND);
        this.dateTime = (long) json.optDouble(JSON_DATETIME);
        JSONObject jsonTemp = json.optJSONObject("temp");
        if (jsonTemp != null) {
            this.temp = new Temperature(jsonTemp);
        }
    }

    public boolean hasTemperature() {
        return this.temp != null;
    }

    public Temperature getTemperature() {
        return this.temp;
    }

    public float getPressure() {
        return pressure;
    }

    public float getHumidity() {
        return humidity;
    }

    public float getWindSpeed() {
        return windSpeed;
    }

    public long getDateTime() {
        return dateTime;
    }

}

package org.bitpipeline.lib.owm;

import org.json.JSONObject;

public class Temperature {

    public static final String JSON_TEMP = "temp";
    public static final String JSON_TEMP_MIN = "temp_min";
    public static final String JSON_TEMP_MAX = "temp_max";
    public static final String JSON_F16_TEMP = "temp";
    public static final String JSON_F16_TEMP_DAY = "day";
    public static final String JSON_F16_TEMP_MIN = "min";
    public static final String JSON_F16_TEMP_MAX = "max";
    public static final String JSON_F16_TEMP_EVE = "night";
    public static final String JSON_F16_TEMP_NIGHT = "eve";
    public static final String JSON_F16_TEMP_MORNING = "morn";

    private final float temp;
    private final float tempMin;
    private final float tempMax;
    private final float tempDay;
    private final float tempDayMin;
    private final float tempDayMax;
    private final float tempNight;
    private final float tempEve;
    private final float tempMorn;

    public Temperature(JSONObject json) {
        this.temp = (float) json.optDouble(JSON_TEMP);
        this.tempMin = (float) json.optDouble(JSON_TEMP_MIN);
        this.tempMax = (float) json.optDouble(JSON_TEMP_MAX);
        this.tempDay = (float) json.optDouble(JSON_F16_TEMP_DAY);
        this.tempDayMin = (float) json.optDouble(JSON_F16_TEMP_MIN);
        this.tempDayMax = (float) json.optDouble(JSON_F16_TEMP_MAX);
        this.tempNight = (float) json.optDouble(JSON_F16_TEMP_NIGHT);
        this.tempEve = (float) json.optDouble(JSON_F16_TEMP_EVE);
        this.tempMorn = (float) json.optDouble(JSON_F16_TEMP_MORNING);
    }

    public float getTemp() {
        return temp;
    }

    public float getTempMin() {
        return tempMin;
    }

    public float getTempMax() {
        return tempMax;
    }

    public float getTempDay() {
        return tempDay;
    }

    public float getTempDayMin() {
        return tempDayMin;
    }

    public float getTempDayMax() {
        return tempDayMax;
    }

    public float getTempNight() {
        return tempNight;
    }

    public float getTempEve() {
        return tempEve;
    }

    public float getTempMorn() {
        return tempMorn;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return Temperature.class.getSimpleName() + " [temp=" + temp + ", tempMin=" + tempMin + ", tempMax=" + tempMax + ", tempDay=" + tempDay
                + ", tempDayMin=" + tempDayMin + ", tempDayMax=" + tempDayMax + ", tempNight=" + tempNight + ", tempEve="
                + tempEve + ", tempMorn=" + tempMorn + "]";
    }

}

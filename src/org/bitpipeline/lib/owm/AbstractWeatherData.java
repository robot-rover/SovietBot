/**
 * Copyright 2013 J. Miguel P. Tavares
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/
package org.bitpipeline.lib.owm;

import org.bitpipeline.lib.owm.OwmClient.Units;
import org.json.JSONObject;

/**
 *
 * @author mtavares */
abstract public class AbstractWeatherData {
    protected static final String JSON_DATE_TIME = "dt";
    protected static final String JSON_MAIN = "main";
    protected static final String JSON_WIND = "wind";
    protected static final String JSON_RAIN = "rain";
    protected static final String JSON_SNOW = "snow";


    static abstract public class Main {
        protected static final String JSON_HUMIDITY = "humidity";
        protected static final String JSON_PRESSURE = "pressure";

        /**
         * Returns the temperature in one of Celsius (if {@link Units#METRIC})
         * or Fahrenheit (if {@link Units#IMPERIAL}).
         *
         * @return the temperature in a preselected unit or Nan if no
         *         temperature could be found
         */
        abstract public float getTemp();

        /**
         * Returns the minimal temperature in one of Celsius (if
         * {@link Units#METRIC}) or Fahrenheit (if {@link Units#IMPERIAL}).
         *
         * @return the minimal temperature in a preselected unit or Nan if no
         *         temperature could be found
         */
        abstract public float getTempMin();

        /**
         * Returns the maximal temperature in one of Celsius (if
         * {@link Units#METRIC}) or Fahrenheit (if {@link Units#IMPERIAL}).
         *
         * @return the maximal temperature in a preselected unit or Nan if no
         *         temperature could be found
         */
        abstract public float getTempMax();

        /**
         * Returns the pressure in hPa.
         *
         * @return the pressure in hPa or NaN if no humidity could be found.
         */
        abstract public float getPressure();

        /**
         * Returns the humidity in percent.
         *
         * @return the humidity in % or NaN if no humidity could be found.
         */
        abstract public float getHumidity();

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return Main.class.getSimpleName() + "[getTemp()=" + getTemp() + ", getTempMin()=" + getTempMin() + ", getTempMax()=" + getTempMax()
                    + ", getPressure()=" + getPressure() + ", getHumidity()=" + getHumidity() + "]";
        }
    }

    static abstract public class Wind {
        protected static final String JSON_SPEED = "speed";
        protected static final String JSON_DEG = "deg";
        protected static final String JSON_GUST = "gust";
        protected static final String JSON_VAR_BEG = "var_beg";
        protected static final String JSON_VAR_END = "var_end";

        /**
         * Returns the wind speed in in one of meter per second (if
         * {@link Units#METRIC}) or miles per hour (if {@link Units#IMPERIAL}).
         *
         * @return the wind speed in a preselected unit or NaN if no wind speed
         *         could be found
         */
        abstract public float getSpeed();

        /**
         * Returns the wind direction in degrees.
         *
         * @return the wind direction in degrees or {@link Integer#MIN_VALUE} if
         *         no wind direction could be found.
         */
        abstract public int getDeg();

        abstract public float getGust();

        abstract public int getVarBeg();

        abstract public int getVarEnd();

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return Wind.class.getSimpleName() + " [getSpeed()=" + getSpeed() + ", getDeg()=" + getDeg() + ", getGust()=" + getGust()
                    + ", getVarBeg()=" + getVarBeg() + ", getVarEnd()=" + getVarEnd() + "]";
        }
    }

    private final long dateTime;

    public AbstractWeatherData(JSONObject json) {
        this.dateTime = json.optLong(WeatherData.JSON_DATE_TIME, Long.MIN_VALUE);
    }

    public long getDateTime() {
        return this.dateTime;
    }

    /** Get the temperature of this weather report
     * @return <code>null</code> if the report doesn't have temperature data; */
    abstract public Temperature getTemperature();

    /** Get the humidity of this weather report
     * @return <code>Float.NaN</code> if the report doesn't have humidity data; the value of the humidity in percentage to condensation point otherwise. */
    abstract public float getHumidity();

    /** Get the atmospheric pressure of this weather report
     * @return <code>Float.NaN</code> if the report doesn't have pressure data; the value of the pressure in hectopascal otherwise. */
    abstract public float getPressure();

    /** Get the average wind speed of this weather report
     * @return <code>Float.NaN</code> if the report doesn't have wind speed data; the value of the wind speed in metre per second otherwise. */
    abstract public float getWindSpeed();

    /** Get the wind gust speed of this weather report
     * @return <code>Float.NaN</code> if the report doesn't have wind gust speed data; the value of the wind gust speed in metre per second otherwise. */
    abstract public float getWindGust();

    /** Get the average wind direction of this weather report
     * @return <code>Integer.MIN_VALUE</code> if the report doesn't have wind direction data; the degree of the wind direction otherwise. */
    abstract public int getWindDeg();

    /** Get the rain amount of this weather report
     * @return <code>Integer.MIN_VALUE</code> if the report doesn't have rain data; the amount of rain in mm per hour otherwise. */
    abstract public int getRain();

    /** Get the snow amount of this weather report
     * @return <code>Integer.MIN_VALUE</code> if the report doesn't have snow data; the amount of snow in mm per hour otherwise. */
    abstract public int getSnow();

    /** Get the amount of precipitation in this weather report
     * @return <code>Integer.MIN_VALUE</code> if the report doesn't have precipitation data; the amount of precipitation in mm per hour otherwise. */
    abstract public int getPrecipitation();
}

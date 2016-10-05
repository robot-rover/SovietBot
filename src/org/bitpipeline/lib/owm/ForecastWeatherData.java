/*
  Copyright 2013 J. Miguel P. Tavares
  <p>
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  <p>
  http://www.apache.org/licenses/LICENSE-2.0
  <p>
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package org.bitpipeline.lib.owm;

import org.json.JSONObject;

/**
 * @author mtavares */
public class ForecastWeatherData extends LocalizedWeatherData {
    static private final String DATETIME_KEY_NAME = "dt";

    private long calcDateTime = Long.MIN_VALUE;

    /**
     * @param json json container with the forecast data
     * */
    public ForecastWeatherData(JSONObject json) {
        super(json);
        this.calcDateTime = json.optLong(ForecastWeatherData.DATETIME_KEY_NAME, Long.MIN_VALUE);
    }

    /**
     * Returns the time point the forecast is made for in seconds since the
     * epoch.
     *
     * @return the time point the forecast is made for in seconds since the
     *         epoch
     */
    public long getCalcDateTime() {
        return this.calcDateTime;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String s = super.toString();
        s = ForecastWeatherData.class.getSimpleName() + s.substring(s.indexOf(' '));
        return s.substring(0, s.length() - 1) + ", calcDateTime=" + calcDateTime + "]";
    }
}

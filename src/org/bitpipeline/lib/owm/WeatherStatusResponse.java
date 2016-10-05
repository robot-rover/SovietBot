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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mtavares */
public class WeatherStatusResponse extends AbstractOwmResponse {
    private final List<StatusWeatherData> status;

    /** A parser for a weather status query response
     * @param json The JSON obejct built from the OWM response */
    public WeatherStatusResponse(JSONObject json) {
        super(json);
        JSONArray jsonWeatherStatus = json.optJSONArray(AbstractOwmResponse.JSON_LIST);
        if (jsonWeatherStatus == null) {
            this.status = new ArrayList<>();
            this.status.add(new StatusWeatherData(json));
        } else {
            this.status = new ArrayList<>(jsonWeatherStatus.length());
            for (int i = 0; i < jsonWeatherStatus.length(); i++) {
                JSONObject jsonStatus = jsonWeatherStatus.optJSONObject(i);
                if (jsonStatus != null) {
                    this.status.add(new StatusWeatherData(jsonStatus));
                }
            }
        }
    }

    public boolean hasWeatherStatus() {
        return this.status != null && !this.status.isEmpty();
    }

    public List<StatusWeatherData> getWeatherStatus() {
        return this.status;
    }
}

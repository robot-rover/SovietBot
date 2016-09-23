/**
 * Copyright 2013 J. Miguel P. Tavares
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/
package org.bitpipeline.lib.owm;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.Locale;

/**
 * Implements a synchronous HTTP client to the Open Weather Map service
 * described in http://openweathermap.org/wiki/API/JSON_API
 *
 * @author mtavares
 * @author ondrejvanek
 * @author Ayutac
 */
public class OwmClient {
    static private final String APPID_HEADER = "x-api-key";

    /**
     * the attribute name for the response code
     */
    private static final String JSON_CODE = "cod";

    /**
     * the error code for JSON objects obtained from OWM
     *
     * @see #JSON_CODE
     */
    private static final int JSON_ERR = 404;

    public enum HistoryType {
        UNKNOWN, TICK, HOUR, DAY
    }

    /**
     * An enumeration of the possible units to use.
     */
    public enum Units {
        /**
         * metric units
         */
        METRIC,

        /**
         * imperial units
         */
        IMPERIAL;

        /**
         * Returns the default units to use.
         * @return the default units to use
         */
        static Units getDefault() {
            return IMPERIAL;
        }
    }

    /**
     * the units to use
     */
    private Units units = Units.IMPERIAL;

    /**
     * the base URL for Open Weather Map
     */
    private String baseOwmUrl = "http://api.openweathermap.org/data/2.5/";
    private String owmAPPID = null;

    private HttpClient httpClient;

    public OwmClient() {
        this(new DefaultHttpClient(), Units.getDefault());
    }

    public OwmClient(Units units) {
        this(new DefaultHttpClient(), units);
    }

    public OwmClient(HttpClient httpClient) {
        this(httpClient, Units.getDefault());
    }

    public OwmClient(HttpClient httpClient, Units units) {
        if (httpClient == null)
            throw new IllegalArgumentException(
                    "Can't construct a OwmClient with a null HttpClient");
        this.httpClient = httpClient;
        this.units = units;
    }

    /**
     * @param appid
     *            The APP ID provided by OpenWeatherMap
     */
    public void setAPPID(String appid) {
        this.owmAPPID = appid;
    }

    /**
     * Find current weather around a geographic point
     *
     * @param lat
     *            is the latitude of the geographic point of interest
     *            (North/South coordinate)
     * @param lon
     *            is the longitude of the geographic point of interest
     *            (East/West coordinate)
     * @param cnt
     *            is the requested number of weather stations to retrieve (the
     *            actual answer might be less than the requested).
     * @return the WeaherStatusResponse received
     * @throws JSONException
     *             if the response from the OWM server can't be parsed
     * @throws IOException
     *             if there's some network error or the OWM server replies with
     *             a error.
     */
    public WeatherStatusResponse currentWeatherAroundPoint(float lat, float lon,
                                                           int cnt) throws IOException, JSONException { // , boolean cluster,
        // OwmClient.Lang lang)
        // {
        String subUrl = String.format(Locale.ROOT,
                "weather?lat=%f&lon=%f&cnt=%d&cluster=yes&units=%s",
                Float.valueOf(lat), Float.valueOf(lon), Integer.valueOf(cnt),
                units.toString().toLowerCase());
        JSONObject response = doQuery(subUrl);
        if (isError(response)) {
            return null;
        }
        return new WeatherStatusResponse(response);
    }

    /**
     * Find current weather around a city coordinates
     *
     * @param lat
     *            is the latitude of the geographic point of interest
     *            (North/South coordinate)
     * @param lon
     *            is the longitude of the geographic point of interest
     *            (East/West coordinate)
     * @param cnt
     *            is the requested number of weather stations to retrieve (the
     *            actual answer might be less than the requested).
     * @return the WeaherStatusResponse received
     * @throws JSONException
     *             if the response from the OWM server can't be parsed
     * @throws IOException
     *             if there's some network error or the OWM server replies with
     *             a error.
     */
    public WeatherStatusResponse currentWeatherAtCity(float lat, float lon,
                                                      int cnt) throws IOException, JSONException { // , boolean cluster,
        // OwmClient.Lang lang)
        // {
        String subUrl = String.format(Locale.ROOT,
                "weather?lat=%f&lon=%f&cnt=%d&cluster=yes&units=%s",
                Float.valueOf(lat), Float.valueOf(lon), Integer.valueOf(cnt),
                units.toString().toLowerCase());
        JSONObject response = doQuery(subUrl);
        if (isError(response)) {
            return null;
        }
        return new WeatherStatusResponse(response);
    }

    /**
     * Find current weather within a bounding box
     *
     * @param northLat
     *            is the latitude of the geographic top left point of the
     *            bounding box
     * @param westLon
     *            is the longitude of the geographic top left point of the
     *            bounding box
     * @param southLat
     *            is the latitude of the geographic bottom right point of the
     *            bounding box
     * @param eastLon
     *            is the longitude of the geographic bottom right point of the
     *            bounding box
     * @return the WeaherStatusResponse received
     * @throws JSONException
     *             if the response from the OWM server can't be parsed
     * @throws IOException
     *             if there's some network error or the OWM server replies with
     *             a error.
     */
    public WeatherStatusResponse currentWeatherInBoundingBox(float northLat,
                                                             float westLon, float southLat, float eastLon)
            throws IOException, JSONException { // , boolean cluster,
        // OwmClient.Lang lang) {
        String subUrl = String.format(Locale.ROOT,
                "box/city?bbox=%f,%f,%f,%f&cluster=yes&units=%s",
                Float.valueOf(northLat), Float.valueOf(westLon),
                Float.valueOf(southLat), Float.valueOf(eastLon),
                units.toString().toLowerCase());
        JSONObject response = doQuery(subUrl);
        if (isError(response)) {
            return null;
        }
        return new WeatherStatusResponse(response);
    }

    /**
     * Find current city weather within a bounding box
     *
     * @param northLat
     *            is the latitude of the geographic top left point of the
     *            bounding box
     * @param westLon
     *            is the longitude of the geographic top left point of the
     *            bounding box
     * @param southLat
     *            is the latitude of the geographic bottom right point of the
     *            bounding box
     * @param eastLon
     *            is the longitude of the geographic bottom right point of the
     *            bounding box
     * @return the WeaherStatusResponse received
     * @throws JSONException
     *             if the response from the OWM server can't be parsed
     * @throws IOException
     *             if there's some network error or the OWM server replies with
     *             a error.
     */
    public WeatherStatusResponse currentWeatherAtCityBoundingBox(float northLat,
                                                                 float westLon, float southLat, float eastLon)
            throws IOException, JSONException { // , boolean cluster,
        // OwmClient.Lang lang) {
        String subUrl = String.format(Locale.ROOT,
                "box/city?bbox=%f,%f,%f,%f&cluster=yes&units=%s",
                Float.valueOf(northLat), Float.valueOf(westLon),
                Float.valueOf(southLat), Float.valueOf(eastLon),
                units.toString().toLowerCase());
        JSONObject response = doQuery(subUrl);
        if (isError(response)) {
            return null;
        }
        return new WeatherStatusResponse(response);
    }

    /**
     * Find current weather within a circle
     *
     * @param lat
     *            is the latitude of the geographic center of the circle
     *            (North/South coordinate)
     * @param lon
     *            is the longitude of the geographic center of the circle
     *            (East/West coordinate)
     * @param radius
     *            is the radius of the circle (in kilometres)
     * @return the WeaherStatusResponse received
     * @throws JSONException
     *             if the response from the OWM server can't be parsed
     * @throws IOException
     *             if there's some network error or the OWM server replies with
     *             a error.
     */
    public WeatherStatusResponse currentWeatherInCircle(float lat, float lon,
                                                        float radius) throws IOException, JSONException { // , boolean
        // cluster,
        // OwmClient.Lang
        // lang) {
        String subUrl = String.format(Locale.ROOT,
                "find?lat=%f&lon=%f&cluster=yes&units=%s", Float.valueOf(lat),
                Float.valueOf(lon), Float.valueOf(radius),
                units.toString().toLowerCase());
        JSONObject response = doQuery(subUrl);
        if (isError(response)) {
            return null;
        }
        return new WeatherStatusResponse(response);
    }

    /**
     * Find current city weather within a circle
     *
     * @param lat
     *            is the latitude of the geographic center of the circle
     *            (North/South coordinate)
     * @param lon
     *            is the longitude of the geographic center of the circle
     *            (East/West coordinate)
     * @param radius
     *            is the radius of the circle (in kilometres)
     * @return the WeaherStatusResponse received
     * @throws JSONException
     *             if the response from the OWM server can't be parsed
     * @throws IOException
     *             if there's some network error or the OWM server replies with
     *             a error.
     */
    public WeatherStatusResponse currentWeatherAtCityCircle(float lat,
                                                            float lon, float radius) throws IOException, JSONException {
        String subUrl = String.format(Locale.ROOT,
                "find?lat=%f&lon=%f&cluster=yes&units=%s", Float.valueOf(lat),
                Float.valueOf(lon), Float.valueOf(radius),
                units.toString().toLowerCase());
        JSONObject response = doQuery(subUrl);
        if (isError(response)) {
            return null;
        }
        return new WeatherStatusResponse(response);
    }

    /**
     * Find current city weather
     *
     * @param cityId
     *            is the ID of the city
     * @return the StatusWeatherData received
     * @throws JSONException
     *             if the response from the OWM server can't be parsed
     * @throws IOException
     *             if there's some network error or the OWM server replies with
     *             a error.
     */
    public StatusWeatherData currentWeatherAtCity(int cityId)
            throws IOException, JSONException {
        String subUrl = String.format(Locale.ROOT,
                "weather?id=%d&type=json&units=%s", Integer.valueOf(cityId),
                units.toString().toLowerCase());
        JSONObject response = doQuery(subUrl);
        if (isError(response)) {
            return null;
        }
        return new StatusWeatherData(response);
    }

    /**
     * Find current weather of several cities.
     *
     * @param cityIds
     *            are the IDs of the cities.
     * @return The WeatherStatusResponse received . <code>null</code> will be
     *         returned if OWM responds with code {@value #JSON_ERR}.
     * @throws NullPointerException
     *             if <code>cityIds</code> refers to <code>null</code>
     * @throws JSONException
     *             if the response from the OWM server can't be parsed
     * @throws IOException
     *             if there's some network error or the OWM server replies with
     *             an error.
     */
    public WeatherStatusResponse currentWeatherAtCities(int[] cityIds)
            throws IOException, JSONException {
        if (cityIds == null)
            throw new NullPointerException("City ID array must be specified!");

        StringBuilder s = new StringBuilder();
        if (cityIds.length > 0) {
            for (int i = 0; i < cityIds.length - 1; i++) {
                s.append(cityIds[i]);
                s.append(',');
            }
            s.append(cityIds[cityIds.length - 1]);
        }

        String subUrl = String.format(Locale.ROOT,
                "group?id=%s&type=json&units=%s", s.toString(),
                units.toString().toLowerCase());
        JSONObject response = doQuery(subUrl);
        if (isError(response)) {
            return null;
        }

        return new WeatherStatusResponse(response);
    }

    /**
     * Find current station weather report
     *
     * @param stationId
     *            is the ID of the station
     * @return the StatusWeatherData received
     * @throws JSONException
     *             if the response from the OWM server can't be parsed
     * @throws IOException
     *             if there's some network error or the OWM server replies with
     *             a error.
     */
    public StatusWeatherData currentWeatherAtStation(int stationId)
            throws IOException, JSONException {
        String subUrl = String.format(Locale.ROOT,
                "station?id=%d&type=json&units=%s", Integer.valueOf(stationId),
                units.toString().toLowerCase());
        JSONObject response = doQuery(subUrl);
        if (isError(response)) {
            return null;
        }
        return new StatusWeatherData(response);
    }

    /**
     * Find current city weather
     *
     * @param cityName
     *            is the name of the city
     * @return the WeatherStatusResponse received
     * @throws JSONException
     *             if the response from the OWM server can't be parsed
     * @throws IOException
     *             if there's some network error or the OWM server replies with
     *             a error.
     */
    public WeatherStatusResponse currentWeatherAtCity(String cityName)
            throws IOException, JSONException {
        String subUrl = String.format(Locale.ROOT, "weather?q=%s&units=%s",
                cityName, units.toString().toLowerCase());
        JSONObject response = doQuery(subUrl);
        if (isError(response)) {
            return null;
        }
        return new WeatherStatusResponse(response);
    }

    /**
     * Find current city weather
     *
     * @param cityName
     *            is the name of the city
     * @param countryCode
     *            is the two letter country code
     * @return the StatusWeatherData received
     * @throws JSONException
     *             if the response from the OWM server can't be parsed
     * @throws IOException
     *             if there's some network error or the OWM server replies with
     *             a error.
     */
    public WeatherStatusResponse currentWeatherAtCity(String cityName,
                                                      String countryCode) throws IOException, JSONException {
        String subUrl = String.format(Locale.ROOT, "weather?q=%s,%s&units=%s",
                cityName, countryCode.toUpperCase(),
                units.toString().toLowerCase());
        JSONObject response = doQuery(subUrl);
        if (isError(response)) {
            return null;
        }
        return new WeatherStatusResponse(response);
    }

    /**
     * Get the weather forecast for a city
     *
     * @param cityId
     *            is the ID of the city
     * @return the WeatherForecasteResponse received or <code>null</code> if the
     *         {@link #JSON_CODE} attribute of the specified JSON object holds
     *         the error value {@value #JSON_ERR}.
     * @throws JSONException
     *             if the response from the OWM server can't be parsed
     * @throws IOException
     *             if there's some network error or the OWM server replies with
     *             a error.
     */
    public WeatherForecastResponse forecastWeatherAtCity(int cityId)
            throws JSONException, IOException {
        String subUrl = String.format(Locale.ROOT,
                "forecast?id=%d&type=json&units=%s", cityId,
                units.toString().toLowerCase());
        JSONObject response = doQuery(subUrl);
        if (isError(response)) {
            return null;
        }
        return new WeatherForecastResponse(response);
    }

    /**
     * Get the weather forecast for a city
     *
     * @param cityName
     *            is the Name of the city
     * @return the WeatherForecasteResponse received
     * @throws JSONException
     *             if the response from the OWM server can't be parsed
     * @throws IOException
     *             if there's some network error or the OWM server replies with
     *             a error.
     */
    public WeatherForecastResponse forecastWeatherAtCity(String cityName)
            throws JSONException, IOException {
        String subUrl = String.format(Locale.ROOT,
                "forecast?q=%s&type=json&units=%s", cityName,
                units.toString().toLowerCase());
        JSONObject response = doQuery(subUrl);
        if (isError(response)) {
            return null;
        }
        return new WeatherForecastResponse(response);
    }

    /**
     * Get the daily weather forecast for a city in the next 7 days. (Just one
     * Forecast a day).
     *
     * @param cityName the name of the city
     * @return the WeatherForecasteResponse received
     * @throws JSONException
     *             if the response from the OWM server can't be parsed
     * @throws IOException
     *             if there's some network error or the OWM server replies with
     *             a error.
     */
    public WeatherForecast16Response dailyForecastWeatherAtCity(String cityName)
            throws JSONException, IOException {
        String subUrl = String.format(Locale.ROOT,
                "forecast/daily?q=%s&type=json&units=%s&cnt=7", cityName,
                units.toString().toLowerCase());
        JSONObject response = doQuery(subUrl);
        return new WeatherForecast16Response(response);
    }

    /**
     * Get the weather history of a city.
     *
     * @param cityId
     *            is the OWM city ID
     * @param type
     *            is the history type (frequency) to use.
     * @return the WeatherHistoricCityResponse received
     * @throws JSONException
     *             if the response from the OWM server can't be parsed
     * @throws IOException
     *             if there's some network error or the OWM server replies with
     *             a error.
     */
    public WeatherHistoryCityResponse historyWeatherAtCity(int cityId,
                                                           HistoryType type) throws JSONException, IOException {
        if (type == HistoryType.UNKNOWN)
            throw new IllegalArgumentException(
                    "Can't do a historic request for unknown type of history.");
        String subUrl = String.format(Locale.ROOT,
                "history/city?id=%d&type=%s&units=%s", cityId, type,
                units.toString().toLowerCase());
        JSONObject response = doQuery(subUrl);
        if (isError(response)) {
            return null;
        }
        return new WeatherHistoryCityResponse(response);
    }

    /**
     * Get the weather history of a city.
     *
     * @param city
     *            name of the city
     * @param type
     *            is the history type (frequency) to use.
     * @return the WeatherHistoricCityResponse received
     * @throws JSONException
     *             if the response from the OWM server can't be parsed
     * @throws IOException
     *             if there's some network error or the OWM server replies with
     *             a error.
     */
    public WeatherHistoryCityResponse historyWeatherAtCity(String city,
                                                           HistoryType type) throws JSONException, IOException {
        if (type == HistoryType.UNKNOWN)
            throw new IllegalArgumentException(
                    "Can't do a historic request for unknown type of history.");
        String subUrl = String.format(Locale.ROOT,
                "history/city?q=%s&type=%s&units=%s", city, type,
                units.toString().toLowerCase());
        JSONObject response = doQuery(subUrl);
        if (isError(response)) {
            return null;
        }
        return new WeatherHistoryCityResponse(response);
    }

    /**
     * Get the weather history of a city.
     *
     * @param stationId
     *            is the OWM station ID
     * @param type
     *            is the history type (frequency) to use.
     * @return the WeatherHistoryStationResponse received
     * @throws JSONException
     *             if the response from the OWM server can't be parsed
     * @throws IOException
     *             if there's some network error or the OWM server replies with
     *             a error.
     */
    public WeatherHistoryStationResponse historyWeatherAtStation(int stationId,
                                                                 HistoryType type) throws JSONException, IOException {
        if (type == HistoryType.UNKNOWN)
            throw new IllegalArgumentException(
                    "Can't do a historic request for unknown type of history.");
        String subUrl = String.format(Locale.ROOT,
                "history/station?id=%d&type=%s&units=%s", stationId, type,
                units.toString().toLowerCase());
        JSONObject response = doQuery(subUrl);
        if (isError(response)) {
            return null;
        }
        return new WeatherHistoryStationResponse(response);
    }

    /**
     * Tells if the {@value #JSON_CODE} attribute of the specified JSON object
     * holds the error value {@value #JSON_ERR}.
     *
     * @param json
     *            the JSON object to check
     * @return <code>true</code> if the attribute is present and its value
     *         equals the error value {@value #JSON_ERR}, else
     *         <code>false</code>.
     * @throws JSONException
     *             If the {@value #JSON_CODE} attribute is present but doesn't
     *             hold an integer.
     */
    private boolean isError(JSONObject json) throws JSONException {
        return json.has(JSON_CODE) && json.getInt(JSON_CODE) == JSON_ERR;
    }

    private JSONObject doQuery(String subUrl)
            throws JSONException, IOException {
        String responseBody = null;
        if (this.owmAPPID != null) {
            subUrl += "&APPID=" + owmAPPID;
        }
        HttpGet httpget = new HttpGet(this.baseOwmUrl + subUrl);
        if (this.owmAPPID != null) {
            httpget.addHeader(OwmClient.APPID_HEADER, this.owmAPPID);
        }

        HttpResponse response = this.httpClient.execute(httpget);
        InputStream contentStream = null;
        try {
            StatusLine statusLine = response.getStatusLine();
            if (statusLine == null) {
                throw new IOException(String
                        .format("Unable to get a response from OWM server"));
            }
            int statusCode = statusLine.getStatusCode();
            if (statusCode < 200 || statusCode >= 300) {
                throw new IOException(String.format(
                        "OWM server responded with status code %d: %s",
                        statusCode, statusLine));
            }
            /* Read the response content */
            HttpEntity responseEntity = response.getEntity();
            contentStream = responseEntity.getContent();
            Reader isReader = new InputStreamReader(contentStream);
            int contentSize = (int) responseEntity.getContentLength();
            if (contentSize < 0)
                contentSize = 8 * 1024;
            StringWriter strWriter = new StringWriter(contentSize);
            char[] buffer = new char[8 * 1024];
            int n = 0;
            while ((n = isReader.read(buffer)) != -1) {
                strWriter.write(buffer, 0, n);
            }
            responseBody = strWriter.toString();
            contentStream.close();
        } catch (IOException e) {
            throw e;
        } catch (RuntimeException re) {
            httpget.abort();
            throw re;
        } finally {
            if (contentStream != null)
                contentStream.close();
        }
        return new JSONObject(responseBody);
    }
}

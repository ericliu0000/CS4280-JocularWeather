package org.nothing.jocularweather;

import java.util.List;

/**
 * Parent class for JSON object from weather report. Follows
 * {@link <a href="https://openweathermap.org/current#current_JSON">docs</a>}
 *
 * @param coord      location of station
 * @param weather    qualitative overview of weather
 * @param base       internal parameter
 * @param main       quantitative weather containing temp, pressure, humidity
 * @param visibility measured visibility in meters, capped at 10km
 * @param wind       wind data at station
 * @param clouds     cloud coverage at station
 * @param dt         current time formatted as Unix timestamp (UTC)
 * @param sys        static positional and solar data of station
 * @param timezone   time correction from UTC in seconds
 * @param id         identification number of city
 * @param name       name of town
 * @param cod        internal parameter
 */
public record Report(Coordinates coord, List<Weather> weather, String base, Details main, int visibility, Wind wind,
                     Clouds clouds, Rain rain, Snow snow, int dt, WxSystem sys, int timezone, int id, String name,
                     int cod) {

    @Override
    public String toString() {
        return String.format("Weather report for %s", name);
    }
}

/**
 * Latitude and longitude coordinates (WGS-84?)
 *
 * @param lon longitude of city
 * @param lat latitude of city
 */
record Coordinates(double lon, double lat) {
    @Override
    public String toString() {
        return String.format("(%s, %s)", lon, lat);
    }
}

/**
 * Primary weather report object
 *
 * @param id          weather behavior identification number
 * @param main        broad qualitative description of weather
 * @param description precise qualitative evaluation of weather
 * @param icon        suffix to identify weather icon
 */
record Weather(int id, String main, String description, String icon) {
    @Override
    public String toString() {
        String content = """
                Condition id %s, %s concise
                %s verbose, icon %s
                """;
        return String.format(content, id, main, description, icon);
    }
}

/**
 * Further details about weather. Temperature units determined by API call
 *
 * @param temp       temperature
 * @param feels_like temperature accounting for human factors
 * @param temp_min   minimum temperature within city
 * @param temp_max   maximum temperature within city
 * @param pressure   barometric sea-level equivalent pressure in hPa, if
 *                   sea_level/grnd_level fields aren't provided
 * @param humidity   relative humidity
 * @param sea_level  barometric pressure at sea level in hPa
 * @param grnd_level barometric pressure at ground level in hPa
 */
record Details(double temp, double feels_like, double temp_min, double temp_max, int pressure, int humidity,
               int sea_level, int grnd_level) {
    @Override
    public String toString() {
        String content = """
                Temperature %s, feels like %s, mintemp %s, maxtemp %s
                Sea level press. %s or sea level %s and ground level %s
                Relative humidity %s%
                """;
        return String.format(content, temp, feels_like, temp_min, temp_max, pressure, sea_level, grnd_level, humidity);
    }
}

/**
 * Wind information at weather station. Velocity specified by API call.
 *
 * @param speed velocity of wind
 * @param deg   meterological degree from which the wind is coming
 * @param gust  velocity of recent wind gust
 */
record Wind(double speed, int deg, double gust) {
    @Override
    public String toString() {
        return String.format("Wind %s at %s gust %gust", deg, speed, gust);
    }
}

/**
 * Cloud coverage information.
 *
 * @param all percentage of sky covered by clouds
 */
record Clouds(int all) {
    @Override
    public String toString() {
        return String.format("%s percent", all);
    }
}

/**
 * Rainfall records.
 *
 * @param _1h height of previous 1 hour of rain in mm
 * @param _3h height of previous 3 hours of rain in mm
 */
record Rain(double _1h, double _3h) {
    @Override
    public String toString() {
        return String.format("Rainfall: %s 1h, %s 3h", _1h, _3h);
    }
}

/**
 * Snowfall records.
 *
 * @param _1h height of previous 1 hour of snow in mm
 * @param _3h height of previous 3 hours of snow in mm
 */
record Snow(double _1h, double _3h) {
    @Override
    public String toString() {
        return String.format("%s 1h, %s 3h", _1h, _3h);
    }
}

/**
 * Additional information about static weather station values.
 *
 * @param type    internal parameter
 * @param id      internal parameter
 * @param country country code in which the weather station is
 * @param sunrise Unix timestamp (UTC) of sunrise
 * @param sunset  Unix timestamp (UTC) of sunset
 */
record WxSystem(int type, int id, String country, int sunrise, int sunset) {
    @Override
    public String toString() {
        return String.format("%s %s %s %s %s", type, id, country, sunrise, sunset);
    }
}
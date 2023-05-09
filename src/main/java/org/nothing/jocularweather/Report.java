package org.nothing.jocularweather;

import java.util.List;

/**
 * Parent class for JSON object from weather report
 * @param coord location of station
 * @param weather qualitative overview of weather
 * @param base type of station
 * @param main quantitative weather containing temp, pressure, humidity
 * @param visibility measured visibility in meters
 * @param wind wind data at station
 * @param clouds cloud coverage at station
 * @param dt current time formatted as Unix timestamp (UTC)
 * @param sys static positional and solar data of station
 * @param timezone time correction from UTC in seconds
 * @param id
 * @param name name of town
 * @param cod
 */
public record Report(Coordinates coord, List<Weather> weather, String base, Details main, int visibility, Wind wind,
                     Clouds clouds, int dt, WxSystem sys, int timezone, int id, String name, int cod) {
}

/**
 * Latitude and longitude coordinates (WGS-84?)
 * @param lon
 * @param lat
 */
record Coordinates(double lon, double lat) {
}

/**
 * Primary weather report object
 * @param id weather behavior identification number
 * @param main broad qualitative description of weather
 * @param description precise qualitative evaluation of weather
 * @param icon suffix to identify weather icon
 */
record Weather(int id, String main, String description, String icon) {
}

/**
 * Further deatils about weather. Units determined before API call
 * @param temp temperature
 * @param feels_like temperature accounting for human factors
 * @param temp_min predicted minimum temperature
 * @param temp_max predicted maximum temperature
 * @param pressure atmospheric pressure in hPa
 * @param humidity relative humidity
 */
record Details(double temp, double feels_like, double temp_min, double temp_max, int pressure, int humidity) {
}

/**
 * Wind information at weather station.
 * @param speed velocity of wind
 * @param deg compass degree from which the wind is coming
 */
record Wind(double speed, int deg) {
}

/**
 * Cloud coverage information.
 * @param all percentage of sky covered by clouds
 */
record Clouds(int all) {
}

/**
 * Additional information about static weather station values.
 * @param type type of weather station
 * @param id weather station identification number
 * @param country country in which the weather station is
 * @param sunrise Unix timestamp (UTC) of sunrise
 * @param sunset Unix timestamp (UTC) of sunset
 */
record WxSystem(int type, int id, String country, int sunrise, int sunset) {
}
package org.nothing.jocularweather;

/**
 * Enumeration for type of report response. Assigned to {@link org.nothing.jocularweather.Report} and {@link org.nothing.jocularweather.MalformedReport} depending on weather system API output.
 *
 * @author Eric Liu
 * @author Ganning Xu
 */
public enum ReportType {
    /**
     * Normal report; fully filled by API response consummate with weather conditions.
     */
    OKAY,
    /**
     * Empty report, corresponding to an unavailable location.
     */
    LOCATION_NOT_FOUND,
    /**
     * Empty report, corresponding to a connection or other API error.
     */
    API_ERROR,
    /**
     * Catch-all other report faults
     */
    NOT_OKAY
}

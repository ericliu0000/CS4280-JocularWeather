package org.nothing.jocularweather;

/**
 * Enumeration for type of report response.
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
     * Catch-all other report fault
     */
    NOT_OKAY
}

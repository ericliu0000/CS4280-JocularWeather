package org.nothing.jocularweather;

/**
 * Interface which handles normally formed and incomplete reports.
 */
public interface ReportBase {
    /**
     * Returns type of report to match {@link ReportType} enum
     * @return type of report
     */
    ReportType type();
}


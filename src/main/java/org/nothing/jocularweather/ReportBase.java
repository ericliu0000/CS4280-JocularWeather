package org.nothing.jocularweather;

/**
 * Interface which handles normally formed and incomplete reports.
 *
 * @author Eric Liu
 * @author Ganning Xu
 */
public interface ReportBase {
    /**
     * Returns type of report to match {@link org.nothing.jocularweather.ReportType} enum.
     *
     * @return type of report
     */
    ReportType type();
}

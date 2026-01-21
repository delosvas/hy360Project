package util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/*
 * DateUtils
 *
 * Utility class providing methods for formatting LocalDate objects into readable strings
 * The formats it supports are:
 * 1. dd-MM-yyyy (e.g. 21-01-2026)
 * 2. yyyy-MM-dd (e.g. 2026-01-21)
 */
public class DateUtils {
    private static final DateTimeFormatter DD_MM_YYYY = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /*
     * Formats a LocalDate into a dd-MM-yyyy string
     * 
     * @param date LocalDate to format
     * @return formatted string or empty string if date is null
     */
    public static String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DD_MM_YYYY);
    }
    
    /*
     * Parses a date string into a LocalDate
     * 
     * @param dateStr the input date string
     * @return LocalDate parsed from the string
     * @throws DateTimeParseException if the format is invalid
     */
    public static LocalDate parseDate(String dateStr) throws DateTimeParseException {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new DateTimeParseException("Date string is empty", dateStr, 0);
        }
        dateStr = dateStr.trim();
        try { // Try dd-MM-yyyy 
            return LocalDate.parse(dateStr, DD_MM_YYYY);
        } catch (DateTimeParseException e) {
            try { // Try yyyy-MM-dd as fallback
                return LocalDate.parse(dateStr, YYYY_MM_DD);
            } catch (DateTimeParseException e2) {
                throw new DateTimeParseException("Invalid date format. Use DD-MM-YYYY", dateStr, 0);
            }
        }
    }
}

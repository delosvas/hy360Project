package util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateUtils {
    private static final DateTimeFormatter DD_MM_YYYY = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public static String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DD_MM_YYYY);
    }
    
    public static LocalDate parseDate(String dateStr) throws DateTimeParseException {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new DateTimeParseException("Date string is empty", dateStr, 0);
        }
        dateStr = dateStr.trim();
        try {
            return LocalDate.parse(dateStr, DD_MM_YYYY);
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(dateStr, YYYY_MM_DD);
            } catch (DateTimeParseException e2) {
                throw new DateTimeParseException("Invalid date format. Use DD-MM-YYYY", dateStr, 0);
            }
        }
    }
}

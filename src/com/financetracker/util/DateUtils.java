package com.financetracker.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/**
 * Small utility class for date parsing and formatting used across the app.
 */
public final class DateUtils {

    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    private DateUtils() {}

    public static Optional<LocalDate> parseDate(String input) {
        try {
            return Optional.of(LocalDate.parse(input.trim(), INPUT_FORMAT));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    public static String format(LocalDate date) {
        return date.format(DISPLAY_FORMAT);
    }

    public static YearMonth currentYearMonth() {
        return YearMonth.now();
    }

    public static boolean isInMonth(LocalDate date, YearMonth yearMonth) {
        return YearMonth.from(date).equals(yearMonth);
    }
}

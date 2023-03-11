package de.undercouch.citeproc.helper.time;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Parses strings containing dates in almost any known international format.
 * @author Michel Kraemer
 */
public class AnyDateParser {
    /**
     * Pattern contains long year group
     */
    private static final int FLY = 1;

    /**
     * Pattern contains short year group
     */
    private static final int FSY = 2;

    /**
     * Pattern contains month name group
     */
    private static final int FMN = 4;

    /**
     * Pattern contains month digit group
     */
    private static final int FM = 8;

    /**
     * Pattern contains day group
     */
    private static final int FD = 16;

    /**
     * Pattern contains day and month that might be switched if locale demands it
     */
    private static final int FS = 32;

    private static final String GROUPNAME_DAY = "day";
    private static final String GROUPNAME_MONTHNAME = "monthname";
    private static final String GROUPNAME_MONTH = "month";
    private static final String GROUPNAME_LONGYEAR = "longyear";
    private static final String GROUPNAME_SHORTYEAR = "shortyear";

    private static final List<CompiledPattern> englishPatterns;
    private static final Map<String, Long> englishMonthIndexes;

    static {
        englishPatterns = makePatterns(Locale.ENGLISH);
        englishMonthIndexes = makeMonthIndexes(Locale.ENGLISH);
    }

    private static class CompiledPattern {
        /**
         * The pattern
         */
        final Pattern p;

        /**
         * A bitmask specifying which group names are used in the pattern
         */
        final int groupNames;

        CompiledPattern(Pattern p, int groupNames) {
            this.p = p;
            this.groupNames = groupNames;
        }
    }

    /**
     * Creates regular expression patterns for the given locale
     * @param locale the locale
     * @return the patterns
     */
    private static List<CompiledPattern> makePatterns(Locale locale) {
        DateFormatSymbols dfs = new DateFormatSymbols(locale);
        String shortMonthsRegex = Arrays.stream(dfs.getShortMonths())
                .filter(s -> !s.isEmpty())
                .map(Pattern::quote)
                .map(s -> s + "\\.?")
                .collect(Collectors.joining("|"));
        String longMonthsRegex = Arrays.stream(dfs.getMonths())
                .filter(s -> !s.isEmpty())
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));
        String allMonthsRegex = longMonthsRegex + "|" + shortMonthsRegex;

        String dayRegex = "(?<" + GROUPNAME_DAY + ">[0-9]{1,2})";
        String monthNameRegex = "(?<" + GROUPNAME_MONTHNAME + ">" + allMonthsRegex + ")";
        String monthRegex = "(?<" + GROUPNAME_MONTH + ">[0-9]{1,2})";
        String longYearRegex = "(?<" + GROUPNAME_LONGYEAR + ">[0-9]{4,})";
        String yearRegex = "(" + longYearRegex + "|" +
                "'?(?<" + GROUPNAME_SHORTYEAR + ">[0-9]{2}))";

        return Arrays.asList(
                // Feb. 27, 2023 / February 27th, 2023
                new CompiledPattern(
                        Pattern.compile(
                                monthNameRegex + "\\s+" + dayRegex +
                                        "(th|st|nd|rd)?(\\s*,)?\\s+" + yearRegex,
                                Pattern.CASE_INSENSITIVE),
                        FMN | FD | FSY | FLY
                ),

                // 27 February 2023
                new CompiledPattern(
                        Pattern.compile(
                                "(" + dayRegex + "\\.?\\s+)?" + monthNameRegex +
                                        "\\s+" + yearRegex,
                                Pattern.CASE_INSENSITIVE),
                        FMN | FD | FSY | FLY
                ),

                // yyyy-mm-dd,
                new CompiledPattern(
                        Pattern.compile(
                                longYearRegex + "-" + monthRegex +
                                        "(-" + dayRegex + ")?",
                                Pattern.CASE_INSENSITIVE),
                        FM | FD | FLY
                ),

                // yyyy.mm.dd,
                new CompiledPattern(
                        Pattern.compile(
                                longYearRegex + "\\." + monthRegex +
                                        "(\\." + dayRegex + ")?",
                                Pattern.CASE_INSENSITIVE),
                        FM | FD | FLY | FS
                ),

                // 2023-Feb-27,
                new CompiledPattern(
                        Pattern.compile(
                                longYearRegex + "-" + monthNameRegex +
                                        "(-" + dayRegex + ")?",
                                Pattern.CASE_INSENSITIVE),
                        FMN | FD | FLY
                ),

                // 27-Feb-2023
                new CompiledPattern(
                        Pattern.compile(
                                "(" + dayRegex + "-)?" + monthNameRegex +
                                        "-" + yearRegex,
                                Pattern.CASE_INSENSITIVE),
                        FMN | FD | FSY | FLY
                ),

                // yyyy/mm/dd,
                new CompiledPattern(
                        Pattern.compile(
                                longYearRegex + "/" + monthRegex +
                                        "(/" + dayRegex + ")?",
                                Pattern.CASE_INSENSITIVE),
                        FM | FD | FLY
                ),

                // mm/dd/yy
                new CompiledPattern(
                        Pattern.compile(
                                monthRegex + "/" + dayRegex + "/" + yearRegex,
                                Pattern.CASE_INSENSITIVE),
                        FM | FD | FSY | FLY | FS
                ),

                // 27/Feb/2023
                new CompiledPattern(
                        Pattern.compile(
                                dayRegex + "/" + monthNameRegex + "/" + yearRegex,
                                Pattern.CASE_INSENSITIVE),
                        FMN | FD | FSY | FLY
                ),

                // dd.mm.yyyy
                new CompiledPattern(
                        Pattern.compile(
                                dayRegex + "\\.\\s*" + monthRegex + "\\.\\s*" + yearRegex,
                                Pattern.CASE_INSENSITIVE),
                        FM | FD | FSY | FLY | FS
                ),

                // yyyy
                new CompiledPattern(
                        Pattern.compile(yearRegex, Pattern.CASE_INSENSITIVE),
                        FLY
                )
        );
    }

    /**
     * Create a map that maps short and long month names from a given locale
     * to their ordinal
     * @param locale the locale
     * @return the map
     */
    private static Map<String, Long> makeMonthIndexes(Locale locale) {
        DateFormatSymbols dfs = new DateFormatSymbols(locale);
        Map<String, Long> monthIndexes = new HashMap<>();
        String[] longMonths = dfs.getMonths();
        for (int i = 0; i < longMonths.length; i++) {
            String m = longMonths[i].toLowerCase(locale);
            monthIndexes.put(m, i + 1L);
        }
        String[] shortMonths = dfs.getShortMonths();
        for (int i = 0; i < shortMonths.length; i++) {
            String m = shortMonths[i].toLowerCase(locale);
            monthIndexes.put(m, i + 1L);
        }
        return monthIndexes;
    }

    /**
     * Returns {@code true} if the given locale prefers that the day should be
     * given before the month
     * @param locale the locale
     * @return {@code true} if the day should be given before the month,
     * {@code false} otherwise
     */
    private static boolean dayBeforeMonth(Locale locale) {
        SimpleDateFormat fmt = ((SimpleDateFormat)DateFormat.getDateInstance(
                DateFormat.SHORT, locale));
        String p = fmt.toPattern().toLowerCase(locale);
        int di = p.indexOf('d');
        int mi = p.indexOf('m');
        if (di == -1 || mi == -1) {
            return false;
        }
        return di < mi;
    }

    /**
     * Parse a string containing a date in almost any known international format.
     * Parses at most year, month, and date and returns them in a
     * {@link TemporalAccessor} object. This object may not contain all fields
     * if the string does not contain them as well. For example, if the string
     * is "2014-10", the object will only contain a year and a month but not
     * a day.
     * @param date the string containing the date
     * @return the parsed date
     * @throws IllegalArgumentException if the date could not be parsed
     */
    public static TemporalAccessor parse(String date) throws IllegalArgumentException {
        return parse(date, Locale.ENGLISH);
    }

    /**
     * Parse a string containing a date in almost any known international format.
     * Parses at most year, month, and date and returns them in a
     * {@link TemporalAccessor} object. This object may not contain all fields
     * if the string does not contain them as well. For example, if the string
     * is "2014-10", the object will only contain a year and a month but not
     * a day.
     * @param date the string containing the date
     * @param locale the locale to use for parsing (used to parse month names
     * and to determine if the locale prefers the month to be given before the
     * day in the string to parse or the other way around)
     * @return the parsed date
     * @throws IllegalArgumentException if the date could not be parsed
     */
    public static TemporalAccessor parse(String date, Locale locale) throws IllegalArgumentException {
        List<CompiledPattern> patterns;
        Map<String, Long> monthIndexes;
        boolean dbm;
        if (locale.equals(Locale.ENGLISH) || locale.equals(Locale.US)) {
            patterns = englishPatterns;
            monthIndexes = englishMonthIndexes;
            dbm = false;
        } else {
            patterns = makePatterns(locale);
            monthIndexes = makeMonthIndexes(locale);
            dbm = dayBeforeMonth(locale);
        }

        String trimmedDate = date.trim();
        for (CompiledPattern cp : patterns) {
            Matcher m = cp.p.matcher(trimmedDate);
            if (m.find()) {
                Long year = null;
                Long month = null;
                Long day = null;

                if ((cp.groupNames & FLY) == FLY) {
                    String ys = m.group(GROUPNAME_LONGYEAR);
                    if (ys == null && (cp.groupNames & FSY) == FSY) {
                        ys = m.group(GROUPNAME_SHORTYEAR);
                    }

                    if (ys != null) {
                        year = Long.parseLong(ys);
                        if (year < 100) {
                            year += 2000;
                        }
                    }
                }

                if ((cp.groupNames & FMN) == FMN) {
                    String mm = m.group(GROUPNAME_MONTHNAME).toLowerCase(locale);
                    if (mm.endsWith(".")) {
                        mm = mm.substring(0, mm.length() - 1);
                    }
                    month = monthIndexes.get(mm);
                }

                if ((cp.groupNames & FM) == FM) {
                    String mm = m.group(GROUPNAME_MONTH);
                    month = Long.parseLong(mm);
                }

                if ((cp.groupNames & FD) == FD) {
                    String dd = m.group(GROUPNAME_DAY);
                    if (dd != null) {
                        day = Long.parseLong(dd);
                    }
                }

                if (dbm && (cp.groupNames & FS) == FS) {
                    // switch day and month if locale demands it
                    Long t = month;
                    month = day;
                    day = t;
                }

                // auto-switch day and month, if month is clearly out of range
                if (month != null && month > 12 && (day == null || day <= 12)) {
                    Long t = month;
                    month = day;
                    day = t;
                }

                return ParsedDate.of(year, month, day);
            }
        }
        throw new IllegalArgumentException("Could not parse input string '" +
                date + "' to valid date");
    }
}

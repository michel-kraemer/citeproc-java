package de.undercouch.citeproc.helper.time;

import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.util.HashMap;
import java.util.Map;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.YEAR;

class ParsedDate implements TemporalAccessor {
    private final Map<TemporalField, Long> values;

    public static ParsedDate of(Long year, Long monthOfYear, Long dayOfMonth) {
        Map<TemporalField, Long> values = new HashMap<>();
        if (year != null) {
            values.put(YEAR, year);
        }
        if (monthOfYear != null) {
            values.put(MONTH_OF_YEAR, monthOfYear);
        }
        if (dayOfMonth != null) {
            values.put(DAY_OF_MONTH, dayOfMonth);
        }
        return new ParsedDate(values);
    }

    private ParsedDate(Map<TemporalField, Long> values) {
        this.values = values;
    }

    @Override
    public boolean isSupported(TemporalField field) {
        return values.containsKey(field);
    }

    @Override
    public long getLong(TemporalField field) {
        return values.get(field);
    }
}

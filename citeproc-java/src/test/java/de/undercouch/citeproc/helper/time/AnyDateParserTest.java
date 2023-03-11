package de.undercouch.citeproc.helper.time;

import org.junit.Test;

import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

import static de.undercouch.citeproc.helper.time.AnyDateParser.parse;
import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link AnyDateParser}
 * @author Michel Kraemer
 */
public class AnyDateParserTest {
    private void assertDateEquals(String str, Integer year, Integer month, Integer day) {
        assertDateEquals(str, year, month, day, Locale.ENGLISH);
    }

    private void assertDateEquals(String str, Integer year, Integer month,
            Integer day, Locale locale) {
        TemporalAccessor ta = parse(str, locale);
        if (year != null) {
            assertEquals(year.intValue(), ta.get(ChronoField.YEAR));
        }
        if (month != null) {
            assertEquals(month.intValue(), ta.get(ChronoField.MONTH_OF_YEAR));
        }
        if (day != null) {
            assertEquals(day.intValue(), ta.get(ChronoField.DAY_OF_MONTH));
        }
    }

    @Test
    public void allTestCases() {
        // These patterns are very, very loosely based on the unit tests from
        // the Golang library 'dateparse' released under the MIT license by
        // Aaron Raddon (https://github.com/araddon/dateparse)
        assertDateEquals("feb 27, 2023", 2023, 2, 27);
        assertDateEquals("feb 27, '23", 2023, 2, 27);
        assertDateEquals("Feb 27, '23", 2023, 2, 27);
        assertDateEquals("Feb. 27, '23", 2023, 2, 27);
        assertDateEquals("feb. 27, '23", 2023, 2, 27);
        assertDateEquals("feb 27, 23", 2023, 2, 27);
        assertDateEquals("Feb 27, 23", 2023, 2, 27);
        assertDateEquals("Feb. 27, 23", 2023, 2, 27);
        assertDateEquals("feb. 27, 23", 2023, 2, 27);
        assertDateEquals("feb. 27, 2023", 2023, 2, 27);
        assertDateEquals("Feb. 27, 1970", 1970, 2, 27);
        assertDateEquals("feb. 27, 1970", 1970, 2, 27);
        assertDateEquals("february 27, 1970", 1970, 2, 27);
        assertDateEquals("February 27, 2023", 2023, 2, 27);
        assertDateEquals("27 February 2023", 2023, 2, 27);
        assertDateEquals("27 Feb. 2023", 2023, 2, 27);
        assertDateEquals("27 Feb 2023", 2023, 2, 27);
        assertDateEquals("27 Feb 23", 2023, 2, 27);
        assertDateEquals("27. February 23", 2023, 2, 27);

        assertDateEquals("Feb '23", 2023, 2, null);
        assertDateEquals("Feb 23", 2023, 2, null);
        assertDateEquals("Feb 2023", 2023, 2, null);
        assertDateEquals("February 23", 2023, 2, null);
        assertDateEquals("February 2023", 2023, 2, null);

        assertDateEquals("February 27 2023", 2023, 2, 27);
        assertDateEquals("February 27th 2023", 2023, 2, 27);
        assertDateEquals("February 27th, 2023", 2023, 2, 27);
        assertDateEquals("February 1st 2023", 2023, 2, 1);
        assertDateEquals("February 2nd 2023", 2023, 2, 2);
        assertDateEquals("February 3rd 2023", 2023, 2, 3);

        assertDateEquals("Mon 20 Feb 2023 07:49:10 PM UTC", 2023, 2, 20);
        assertDateEquals("September 17, 2012 at 5:00pm UTC-05", 2012, 9, 17);
        assertDateEquals("September 17 2012, 10:10:09", 2012, 9, 17);
        assertDateEquals("September 17 2012 10:10:09", 2012, 9, 17);
        assertDateEquals("Fri, 03 Jul 2015 08:08:08 MST", 2015, 7, 3);

        assertDateEquals("28-Feb-02", 2002, 2, 28);
        assertDateEquals("28-February-02", 2002, 2, 28);
        assertDateEquals("15-Jan-18", 2018, 1, 15);
        assertDateEquals("15-Jan-2017", 2017, 1, 15);

        assertDateEquals("Feb-23", 2023, 2, null);
        assertDateEquals("Feb-2023", 2023, 2, null);
        assertDateEquals("February-23", 2023, 2, null);
        assertDateEquals("February-2023", 2023, 2, null);

        assertDateEquals("Fri, 03-Jul-15 08:08:08 MST", 2015, 7, 3);
        assertDateEquals("Wednesday, 07-May-09 08:00:43 MST", 2009, 5, 7);
        assertDateEquals("Wednesday, 28-Feb-18 09:01:00 MST", 2018, 2, 28);

        assertDateEquals("07-Feb-2004 09:07:07 +0100", 2004, 2, 7);
        assertDateEquals("07-Feb-04 09:07:07 +0100", 2004, 2, 7);

        assertDateEquals("2013-Feb-03", 2013, 2, 3);
        assertDateEquals("2013-February-03", 2013, 2, 3);
        assertDateEquals("2013-Feb", 2013, 2, null);
        assertDateEquals("2013-February", 2013, 2, null);

        assertDateEquals("03/31/2014", 2014, 3, 31);
        assertDateEquals("3/31/2014", 2014, 3, 31);
        assertDateEquals("3/5/2014", 2014, 3, 5);

        assertDateEquals("08/05/23", 2023, 8, 5);
        assertDateEquals("8/5/23", 2023, 8, 5);

        assertDateEquals("04/02/2014 04:08:09", 2014, 4, 2);
        assertDateEquals("4/2/2014 04:08:09", 2014, 4, 2);

        assertDateEquals("08/05/23", 2023, 8, 5, Locale.US);
        assertDateEquals("08/05/23", 2023, 5, 8, Locale.UK);
        assertDateEquals("08/31/23", 2023, 8, 31, Locale.US);
        assertDateEquals("08/31/23", 2023, 8, 31, Locale.UK);

        assertDateEquals("2014/04/02", 2014, 4, 2);
        assertDateEquals("2014/03/31", 2014, 3, 31);
        assertDateEquals("2014/4/2", 2014, 4, 2);

        assertDateEquals("2014/4", 2014, 4, null);

        assertDateEquals("2014/04/02 04:08", 2014, 4, 2);
        assertDateEquals("2014/03/31 04:08", 2014, 3, 31);
        assertDateEquals("2014/4/2 04:08", 2014, 4, 2);

        assertDateEquals("2014/04/02", 2014, 4, 2, Locale.US);
        assertDateEquals("2014/04/02", 2014, 4, 2, Locale.UK);

        assertDateEquals("06/May/2008", 2008, 5, 6);
        assertDateEquals("30/May/2008", 2008, 5, 30);

        assertDateEquals("06/May/2008 08:11:17", 2008, 5, 6);
        assertDateEquals("30/May/2008:08:11:17 -0700", 2008, 5, 30);

        assertDateEquals("2014-04-02", 2014, 4, 2);
        assertDateEquals("2014-03-31", 2014, 3, 31);
        assertDateEquals("2014-4-2", 2014, 4, 2);
        assertDateEquals("2020-07-20 08:00", 2020, 7, 20);
        assertDateEquals("2020-07-20+08:00", 2020, 7, 20);

        assertDateEquals("2009-08-12T22:15:09", 2009, 8, 12);

        assertDateEquals("2014-04", 2014, 4, null);

        assertDateEquals("2014.05", 2014, 5, null);
        assertDateEquals("2018.09.30", 2018, 9, 30);

        assertDateEquals("31.3.2014", 2014, 3, 31);
        assertDateEquals("3.3.2014", 2014, 3, 3);

        assertDateEquals("3.31.2014", 2014, 3, 31);
        assertDateEquals("3. 31. 2014", 2014, 3, 31);

        assertDateEquals("3. Oktober 2023", 2023, 10, 3, Locale.GERMAN);
        assertDateEquals("Oktober 2023", 2023, 10, null, Locale.GERMAN);

        assertDateEquals("2023", 2023, null, null);

        assertDateEquals("It's 2023! Happy new year!", 2023, null, null);
    }
}

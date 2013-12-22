// Copyright 2013 Michel Kraemer
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package de.undercouch.citeproc.bibtex;

import java.text.DateFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import de.undercouch.citeproc.csl.CSLDate;
import de.undercouch.citeproc.csl.CSLDateBuilder;

/**
 * Parses dates
 * @author Michel Kraemer
 */
public class DateParser {
	/**
	 * A cache for month names
	 * @see #getMonthNames(Locale)
	 */
	private static Map<Locale, Map<String, Integer>> MONTH_NAMES_CACHE =
			new ConcurrentHashMap<Locale, Map<String,Integer>>();
	
	/**
	 * Converts a given date string to a {@link CSLDate} object. Does
	 * not parse the string but saves it in the CSLDate's raw field.
	 * @param dateString the string
	 * @return the {@link CSLDate} object
	 */
	public static CSLDate toDate(String dateString) {
		return new CSLDateBuilder().raw(dateString).build();
	}
	
	/**
	 * Parses the given year and month to a {@link CSLDate} object. Handles
	 * date ranges such as <code>xx-xx</code> or <code>xx/xx</code> and even
	 * <code>xx-xx/yy-yy</code>.
	 * @param year the year to parse. Should be a four-digit number or a String
	 * whose last four characters are digits.
	 * @param month the month to parse. May be a number (<code>1-12</code>),
	 * a short month name (<code>Jan</code> to <code>Dec</code>), or a
	 * long month name (<code>January</code> to </code>December</code>). This
	 * method is also able to recognize month names in several locales.
	 * @return the {@link CSLDate} object or null if both, the year and the
	 * month, could not be parsed
	 */
	public static CSLDate toDate(String year, String month) {
		//check if there are several dates, parse each of them
		//individually and merge them afterwards
		String[] ms = null;
		if (month != null) {
			ms = month.split("/");
		}
		String[] ys = null;
		if (year != null) {
			ys = year.split("/");
		}
		
		if (ys != null && ys.length > 1) {
			//even if there is a month parse year only to avoid ambiguities
			CSLDate d1 = toDateRange(ys[0], null);
			CSLDate d2 = toDateRange(ys[ys.length - 1], null);
			
			//only merge if the difference between the years is not greater than 1
			if (d1.getDateParts() != null && d2.getDateParts() != null &&
				d1.getDateParts().length > 0 && d2.getDateParts().length > 0 &&
				d1.getDateParts()[0].length > 0 && d2.getDateParts()[d2.getDateParts().length - 1].length > 0 &&
				Math.abs(d2.getDateParts()[0][0] - d1.getDateParts()[d2.getDateParts().length - 1][0]) <= 1) {
				return merge(d1, d2);
			}
		} else if (ms != null && ms.length > 1) {
			CSLDate d1 = toDateRange(year, ms[0]);
			CSLDate d2 = toDateRange(year, ms[1]);
			
			//only merge if the difference between the months is not greater than 1
			if (d1.getDateParts() != null && d2.getDateParts() != null &&
				d1.getDateParts().length > 0 && d2.getDateParts().length > 0 &&
				d1.getDateParts()[0].length > 1 && d2.getDateParts()[d2.getDateParts().length - 1].length > 1 &&
				Math.abs(d2.getDateParts()[0][1] - d1.getDateParts()[d2.getDateParts().length - 1][1]) <= 1) {
				return merge(d1, d2);
			}
		}
		
		return toDateRange(year, month);
	}
	
	/**
	 * Parses the given year and month to a {@link CSLDate} object. Handles
	 * date ranges such as <code>xx-xx</code>.
	 * @param year the year to parse. Should be a four-digit number or a String
	 * whose last four characters are digits.
	 * @param month the month to parse. May be a number (<code>1-12</code>),
	 * a short month name (<code>Jan</code> to <code>Dec</code>), or a
	 * long month name (<code>January</code> to </code>December</code>). This
	 * method is also able to recognize month names in several locales.
	 * @return the {@link CSLDate} object or null if both, the year and the
	 * month, could not be parsed
	 */
	public static CSLDate toDateRange(String year, String month) {
		//check if there's a date range, parse elements
		//individually and merge them afterwards
		String[] ms = null;
		if (month != null) {
			ms = month.split("-+|\u2013+");
		}
		String[] ys = null;
		if (year != null) {
			ys = year.split("-+|\u2013+");
		}
		
		if (ys != null && ys.length > 1) {
			//even if there is a month parse year only to avoid ambiguities
			CSLDate d1 = toDateSingle(ys[0], null);
			CSLDate d2 = toDateSingle(ys[ys.length - 1], null);
			return merge(d1, d2);
		} else if (ms != null && ms.length > 1) {
			CSLDate d1 = toDateSingle(year, ms[0]);
			CSLDate d2 = toDateSingle(year, ms[1]);
			return merge(d1, d2);
		}
		
		return toDateSingle(year, month);
	}
	
	/**
	 * Parses the given year and month to a {@link CSLDate} object. Does not
	 * handle ranges.
	 * @param year the year to parse. Should be a four-digit number or a String
	 * whose last four characters are digits.
	 * @param month the month to parse. May be a number (<code>1-12</code>),
	 * a short month name (<code>Jan</code> to <code>Dec</code>), or a
	 * long month name (<code>January</code> to </code>December</code>). This
	 * method is also able to recognize month names in several locales.
	 * @return the {@link CSLDate} object or null if both, the year and the
	 * month, could not be parsed
	 */
	public static CSLDate toDateSingle(String year, String month) {
		int m = toMonth(month);
		
		//parse year
		int y = -1;
		Boolean circa = null;
		if (year != null && year.length() >= 4) {
			if (StringUtils.isNumeric(year)) {
				y = Integer.parseInt(year);
			} else {
				String fourDigit = year.substring(year.length() - 4);
				if (StringUtils.isNumeric(fourDigit)) {
					y = Integer.parseInt(fourDigit);
					if (year.length() > 4) {
						circa = Boolean.TRUE;
					}
				}
			}
		}
		
		//create result
		CSLDateBuilder builder = new CSLDateBuilder();
		if (y < 0) {
			return null;
		}
		if (m < 0) {
			return builder.dateParts(y).circa(circa).build();
		}
		return builder.dateParts(y, m).circa(circa).build();
	}
	
	/**
	 * Merges two dates
	 * @param d1 the first date
	 * @param d2 the second date
	 * @return the merged date
	 */
	private static CSLDate merge(CSLDate d1, CSLDate d2) {
		if (d1 == null) {
			return d2;
		} else if (d2 == null) {
			return d1;
		}
		
		CSLDateBuilder builder = new CSLDateBuilder();
		
		//handle date parts
		builder.dateParts(d1.getDateParts()[0], d2.getDateParts()[d2.getDateParts().length - 1]);
		
		//handle circa
		if (d1.getCirca() != null) {
			builder.circa(d1.getCirca());
		}
		if (d2.getCirca() != null && (d1.getCirca() == null || d2.getCirca().booleanValue())) {
			builder.circa(d2.getCirca());
		}
		
		//handle literal strings
		if (d1.getLiteral() != null) {
			builder.literal(d1.getLiteral());
		}
		if (d2.getLiteral() != null) {
			if (d1.getLiteral() != null) {
				builder.literal(d1.getLiteral() + "-" + d2.getLiteral());
			} else {
				builder.literal(d2.getLiteral());
			}
		}
		
		//handle seasons
		if (d1.getSeason() != null) {
			builder.season(d1.getSeason());
		}
		if (d2.getSeason() != null) {
			if (d1.getSeason() != null) {
				builder.season(d1.getSeason() + "-" + d2.getSeason());
			} else {
				builder.season(d2.getSeason());
			}
		}
		
		//handle raw strings
		if (d1.getRaw() != null) {
			builder.raw(d1.getRaw());
		}
		if (d2.getRaw() != null) {
			if (d1.getRaw() != null) {
				builder.raw(d1.getRaw() + "-" + d2.getRaw());
			} else {
				builder.raw(d2.getRaw());
			}
		}
		
		return builder.build();
	}
	
	/**
	 * Parses the given month string
	 * @param month the month to parse. May be a number (<code>1-12</code>),
	 * a short month name (<code>Jan</code> to <code>Dec</code>), or a
	 * long month name (<code>January</code> to </code>December</code>). This
	 * method is also able to recognize month names in several locales.
	 * @return the month's number (<code>1-12</code>) or <code>-1</code> if
	 * the string could not be parsed
	 */
	public static int toMonth(String month) {
		int m = -1;
		if (month != null && !month.isEmpty()) {
			if (StringUtils.isNumeric(month)) {
				m = Integer.parseInt(month);
			} else {
				m = tryParseMonth(month, Locale.ENGLISH);
				if (m <= 0) {
					m = tryParseMonth(month, Locale.getDefault());
					if (m <= 0) {
						for (Locale l : Locale.getAvailableLocales()) {
							m = tryParseMonth(month, l);
							if (m > 0) {
								break;
							}
						}
					}
				}
			}
		}
		return m;
	}
	
	/**
	 * Retrieves and caches a list of month names for a given locale
	 * @param locale the locale
	 * @return the list of month names (short and long). All names are
	 * converted to upper case
	 */
	private static Map<String, Integer> getMonthNames(Locale locale) {
		Map<String, Integer> r = MONTH_NAMES_CACHE.get(locale);
		if (r == null) {
			DateFormatSymbols symbols = DateFormatSymbols.getInstance(locale);
			r = new HashMap<String, Integer>(24);
			
			//insert long month names
			String[] months = symbols.getMonths();
			for (int i = 0; i < months.length; ++i) {
				String m = months[i];
				if (!m.isEmpty()) {
					r.put(m.toUpperCase(), i + 1);
				}
			}
			
			//insert short month names
			String[] shortMonths = symbols.getShortMonths();
			for (int i = 0; i < shortMonths.length; ++i) {
				String m = shortMonths[i];
				if (!m.isEmpty()) {
					r.put(m.toUpperCase(), i + 1);
				}
			}
			MONTH_NAMES_CACHE.put(locale, r);
		}
		
		return r;
	}
	
	/**
	 * Tries to parse the given month string using the month names
	 * of the given locale
	 * @param month the month string
	 * @param locale the locale
	 * @return the month's number (<code>1-12</code>) or <code>-1</code> if
	 * the string could not be parsed
	 */
	private static int tryParseMonth(String month, Locale locale) {
		Map<String, Integer> names = getMonthNames(locale);
		Integer r = names.get(month.toUpperCase());
		if (r != null) {
			return r;
		}
		return -1;
	}
}

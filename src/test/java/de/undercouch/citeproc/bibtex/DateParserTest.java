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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.undercouch.citeproc.csl.CSLDate;

/**
 * Tests the date parser
 * @author Michel Kraemer
 */
public class DateParserTest {
	/**
	 * Tests if a single date can be converted
	 */
	@Test
	public void toSingleDate() {
		CSLDate d = DateParser.toDateSingle("2013", "Aug");
		assertArrayEquals(new int[][] { new int[] { 2013, 8 } }, d.getDateParts());
		d = DateParser.toDateSingle("2013", "march");
		assertArrayEquals(new int[][] { new int[] { 2013, 3 } }, d.getDateParts());
		d = DateParser.toDateSingle("2000", "1");
		assertArrayEquals(new int[][] { new int[] { 2000, 1 } }, d.getDateParts());
		d = DateParser.toDateSingle("2013", "Januar");
		assertArrayEquals(new int[][] { new int[] { 2013, 1 } }, d.getDateParts());
		
		d = DateParser.toDateSingle("2013", null);
		assertArrayEquals(new int[][] { new int[] { 2013 } }, d.getDateParts());
		
		d = DateParser.toDateSingle("2013", "nothing");
		assertArrayEquals(new int[][] { new int[] { 2013 } }, d.getDateParts());
		
		d = DateParser.toDateSingle("in 2013", null);
		assertArrayEquals(new int[][] { new int[] { 2013 } }, d.getDateParts());
		assertTrue(d.getCirca());
		
		d = DateParser.toDateSingle(null, null);
		assertNull(d);
	}
	
	/**
	 * Tests if a date range can be converted
	 */
	@Test
	public void toDate() {
		CSLDate d = DateParser.toDate("2013", "Aug");
		assertArrayEquals(new int[][] { new int[] { 2013, 8 } }, d.getDateParts());
		
		d = DateParser.toDate("2013", "Mar-Aug");
		assertArrayEquals(new int[][] { new int[] { 2013, 3 }, new int[] { 2013, 8 } }, d.getDateParts());
		
		d = DateParser.toDate("2013", "March-Aug");
		assertArrayEquals(new int[][] { new int[] { 2013, 3 }, new int[] { 2013, 8 } }, d.getDateParts());
		
		d = DateParser.toDate("2013", "Jan--Aug");
		assertArrayEquals(new int[][] { new int[] { 2013, 1 }, new int[] { 2013, 8 } }, d.getDateParts());
		
		d = DateParser.toDate("2000-2013", null);
		assertArrayEquals(new int[][] { new int[] { 2000 }, new int[] { 2013 } }, d.getDateParts());
		
		d = DateParser.toDate("2000-2013", "Jan");
		assertArrayEquals(new int[][] { new int[] { 2000 }, new int[] { 2013 } }, d.getDateParts());
		
		d = DateParser.toDate("2000-bla", "Jan");
		assertArrayEquals(new int[][] { new int[] { 2000 } }, d.getDateParts());
	}
	
	/**
	 * Tests if a date range containg a slash (two consecutive dates) can be converted
	 */
	@Test
	public void toDateSlash() {
		CSLDate d = DateParser.toDate("2013", "Jul/Aug");
		assertArrayEquals(new int[][] { new int[] { 2013, 7 }, new int[] { 2013, 8 } }, d.getDateParts());
		
		d = DateParser.toDate("2013", "Jan/Aug");
		assertArrayEquals(new int[][] { new int[] { 2013 } }, d.getDateParts());
		
		d = DateParser.toDate("2013/2014", "Jan/Aug");
		assertArrayEquals(new int[][] { new int[] { 2013 }, new int[] { 2014 } }, d.getDateParts());
	}
}

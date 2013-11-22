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

package de.undercouch.citeproc;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

import de.undercouch.citeproc.csl.CSLDate;

/**
 * Tests {@link CSLDateParser}
 * @author Michel Kraemer
 */
public class CSLDateParserTest {
	/**
	 * Tests if simple dates can be parsed correctly
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void simple() throws Exception {
		CSLDateParser parser = new CSLDateParser();
		CSLDate date = parser.parse("2013-11-17");
		assertArrayEquals(new int[][] { new int[] { 2013, 11, 17 } }, date.getDateParts());
		
		date = parser.parse("2013-11");
		assertArrayEquals(new int[][] { new int[] { 2013, 11 } }, date.getDateParts());
		
		date = parser.parse("2013");
		assertArrayEquals(new int[][] { new int[] { 2013 } }, date.getDateParts());
		
		date = parser.parse("");
		assertArrayEquals(new int[][] { new int[0] }, date.getDateParts());
	}
	
	/**
	 * Tests if dates with literal month names can be parsed correctly
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void alpha() throws Exception {
		CSLDateParser parser = new CSLDateParser();
		CSLDate date = parser.parse("November 2013");
		assertArrayEquals(new int[][] { new int[] { 2013, 11 } }, date.getDateParts());
	}
	
	/**
	 * Tests if dates with a slash can be parsed correctly
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void slash() throws Exception {
		CSLDateParser parser = new CSLDateParser();
		CSLDate date = parser.parse("2013/11/17");
		assertArrayEquals(new int[][] { new int[] { 2013, 11, 17 } }, date.getDateParts());
		
		date = parser.parse("11/17/2013");
		assertArrayEquals(new int[][] { new int[] { 2013 } }, date.getDateParts());
	}
}

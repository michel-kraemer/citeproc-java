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

package de.undercouch.citeproc.endnote;

import static org.junit.Assert.assertEquals;

import java.io.Reader;
import java.io.StringReader;

import org.junit.Test;

/**
 * Tests {@link EndNoteParser}
 * @author Michel Kraemer
 */
public class EndNoteParserTest {
	/**
	 * Tests if a single entry can be converted correctly
	 * @throws Exception if anything goes wrong
	 */
	@Test
	public void singleEntry() throws Exception {
		String e =
			"%0 Report\n" + 
			"%T The Programming Language B\n" + 
			"%A Johnson, S. C.\n" + 
			"%A Kernighan, B. W.\n" + 
			"%D 1973\n" + 
			"%N 8\n" + 
			"%I Bell Laboratories,\n" + 
			"%C Murray Hill, NJ, USA\n" + 
			"%F Johnson:1973:PLB";
		
		Reader r = new StringReader(e);
		EndNoteParser parser = new EndNoteParser();
		EndNoteLibrary l = parser.parse(r);
		assertEquals(1, l.getReferences().size());
		
		EndNoteReference ref = l.getReferences().get(0);
		assertEquals(EndNoteType.REPORT, ref.getType());
		assertEquals("The Programming Language B", ref.getTitle());
		assertEquals(Integer.valueOf(1973), ref.getYear());
		assertEquals("8", ref.getNumberOrIssue());
		assertEquals("Bell Laboratories,", ref.getPublisher());
		assertEquals("Murray Hill, NJ, USA", ref.getPlace());
		assertEquals("Johnson:1973:PLB", ref.getLabel());
		assertEquals(2, ref.getAuthors().length);
		assertEquals("Johnson, S. C.", ref.getAuthors()[0]);
		assertEquals("Kernighan, B. W.", ref.getAuthors()[1]);
	}
}

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

import org.junit.Test;

import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CSLType;

/**
 * Tests {@link EndNoteConverter}
 * @author Michel Kraemer
 */
public class EndNoteConverterTest {
	/**
	 * Tests if a single entry can be converted correctly
	 * @throws Exception if anything goes wrong
	 */
	@Test
	public void singleEntry() throws Exception {
		EndNoteReference ref = new EndNoteReferenceBuilder()
			.type(EndNoteType.REPORT)
			.title("The Programming Language B")
			.authors("Johnson, S. C.", "Kernighan, B. W.")
			.year("1973")
			.numberOrIssue("8")
			.publisher("Bell Laboratories,")
			.place("Murray Hill, NJ, USA")
			.label("Johnson:1973:PLB")
			.build();
		
		EndNoteConverter conv = new EndNoteConverter();
		CSLItemData item = conv.toItemData(ref);

		assertEquals(CSLType.REPORT, item.getType());
		assertEquals("The Programming Language B", item.getTitle());
		assertEquals("1973", item.getIssued().getRaw());
		assertEquals("8", item.getNumber());
		assertEquals("8", item.getIssue());
		assertEquals("Bell Laboratories,", item.getPublisher());
		assertEquals("Murray Hill, NJ, USA", item.getEventPlace());
		assertEquals("Murray Hill, NJ, USA", item.getPublisherPlace());
		assertEquals("Johnson:1973:PLB", item.getId());
		assertEquals(2, item.getAuthor().length);
		assertEquals("S. C.", item.getAuthor()[0].getGiven());
		assertEquals("Johnson", item.getAuthor()[0].getFamily());
		assertEquals("B. W.", item.getAuthor()[1].getGiven());
		assertEquals("Kernighan", item.getAuthor()[1].getFamily());
	}
}

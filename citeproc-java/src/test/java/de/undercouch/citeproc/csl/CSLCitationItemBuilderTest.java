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

package de.undercouch.citeproc.csl;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import org.junit.Test;

import de.undercouch.citeproc.helper.json.JsonLexer;
import de.undercouch.citeproc.helper.json.JsonParser;
import de.undercouch.citeproc.helper.json.StringJsonBuilderFactory;

/**
 * Tests the {@link CSLCitationItemBuilder}
 * @author Michel Kraemer
 */
public class CSLCitationItemBuilderTest {
	/**
	 * Constructs a CSLCitationItem that contains as much different
	 * Java types as possible. Serializes this object and deserializes it
	 * again to check if all attributes are still the same.
	 * @throws IOException if the object could not be serialized
	 */
	@Test
	public void fromJson() throws IOException {
		CSLItemData data = new CSLItemDataBuilder()
			.id("DATA_ID")
			.type(CSLType.REPORT)
			.abstrct("Abstract")
			.accessed(2013, 10, 3)
			.author(
					new CSLNameBuilder().given("Given").family("Family").parseNames(true).build(),
					new CSLNameBuilder().given("Given2").family("Family2").build()
			)
			.issue(5)
			.categories("Cat1", "Cat2")
			.build();
		
		CSLCitationItem item = new CSLCitationItemBuilder("ID")
			.authorOnly(true)
			.itemData(data)
			.label(CSLLabel.BOOK)
			.suppressAuthor(false)
			.build();
		
		String json = (String)item.toJson(new StringJsonBuilderFactory().createJsonBuilder());
		
		JsonParser parser = new JsonParser(new JsonLexer(new StringReader(json)));
		Map<String, Object> obj = parser.parseObject();
		
		CSLCitationItem item2 = CSLCitationItem.fromJson(obj);
		
		assertEquals("ID", item2.getId());
		assertTrue(item2.getAuthorOnly());
		assertEquals(CSLLabel.BOOK, item2.getLabel());
		assertFalse(item2.getSuppressAuthor());
		
		CSLItemData data2 = item2.getItemData();
		assertEquals("DATA_ID", data2.getId());
		assertEquals("Abstract", data2.getAbstrct());
		assertArrayEquals(new int[][] { new int[] { 2013, 10, 3 } },
				data2.getAccessed().getDateParts());
		assertEquals("5", data2.getIssue());
		assertArrayEquals(new String[] { "Cat1", "Cat2" }, data2.getCategories());
		
		CSLName[] authors2 = data2.getAuthor();
		assertEquals(2, authors2.length);
		assertEquals("Given", authors2[0].getGiven());
		assertEquals("Family", authors2[0].getFamily());
		assertEquals("Given2", authors2[1].getGiven());
		assertEquals("Family2", authors2[1].getFamily());
	}
}

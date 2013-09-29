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

package de.undercouch.citeproc.mendeley;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.junit.Test;

import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CSLType;
import de.undercouch.citeproc.helper.json.JsonLexer;
import de.undercouch.citeproc.helper.json.JsonParser;

/**
 * Tests the {@link MendeleyConverter}
 * @author Michel Kraemer
 */
public class MendeleyConverterTest {
	/**
	 * Tests if a simple document can be converted
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void simple() throws Exception {
		Map<String, Object> doc = loadDoc("mendeleyconvertertestdoc.json");
		
		CSLItemData item = MendeleyConverter.convert("DOC-ID", doc);
		assertEquals("Brown2011", item.getId());
		assertNotNull(item.getAbstrct());
		assertEquals(doc.get("abstract"), item.getAbstrct());
		assertEquals(CSLType.PAPER_CONFERENCE, item.getType());
		assertEquals(2011, item.getIssued().getDateParts()[0][0]);
		assertEquals(10, item.getIssued().getDateParts()[0][1]);
		assertNotNull(item.getDOI());
		assertEquals(doc.get("doi"), item.getDOI());
		assertNotNull(item.getISBN());
		assertEquals(doc.get("isbn"), item.getISBN());
		assertNotNull(item.getPage());
		assertEquals(doc.get("pages"), item.getPage());
		assertNotNull(item.getPublisher());
		assertEquals(doc.get("publisher"), item.getPublisher());
		assertNotNull(item.getTitle());
		assertEquals(doc.get("title"), item.getTitle());
		assertNotNull(item.getContainerTitle());
		assertEquals(doc.get("published_in"), item.getContainerTitle());
		assertEquals("Brown", item.getAuthor()[0].getFamily());
		assertEquals("Kevin J.", item.getAuthor()[0].getGiven());
		assertEquals(7, item.getAuthor().length);
	}

	private Map<String, Object> loadDoc(String name) throws IOException {
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(name);
		Map<String, Object> doc;
		try {
			JsonParser p = new JsonParser(new JsonLexer(new InputStreamReader(is)));
			doc = p.parseObject();
		} finally {
			is.close();
		}
		return doc;
	}
}

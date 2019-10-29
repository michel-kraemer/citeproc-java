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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.Key;
import org.jbibtex.StringValue;
import org.junit.Test;

import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CSLType;

/**
 * Tests the BibTeX converter
 * @author Michel Kraemer
 */
public class BibTeXConverterTest extends AbstractBibTeXTest {
	/**
	 * Tests if a single bibliography entry can be converted
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void singleEntry() throws Exception {
		BibTeXDatabase db = loadUnixDatabase();
		
		BibTeXEntry e = db.resolveEntry(new Key("Ritchie:1974:UTS"));
		
		BibTeXConverter conv = new BibTeXConverter();
		CSLItemData cid = conv.toItemData(e);
		assertEquals("Ritchie:1974:UTS", cid.getId());
		assertEquals(CSLType.ARTICLE_JOURNAL, cid.getType());
		assertEquals(2, cid.getAuthor().length);
		assertEquals("Ritchie", cid.getAuthor()[0].getFamily());
		assertEquals("Dennis W.", cid.getAuthor()[0].getGiven());
		assertEquals("Thompson", cid.getAuthor()[1].getFamily());
		assertEquals("Ken", cid.getAuthor()[1].getGiven());
		assertEquals("Communications of the Association for Computing Machinery", cid.getCollectionTitle());
		assertEquals("Communications of the Association for Computing Machinery", cid.getContainerTitle());
		assertEquals("17", cid.getVolume());
		assertEquals("7", cid.getIssue());
		assertEquals("7", cid.getNumber());
		assertEquals("11", cid.getNumberOfPages());
		assertEquals("365-375", cid.getPage());
		assertEquals("365", cid.getPageFirst());
		assertEquals("The UNIX Time-Sharing System", cid.getTitle());
		assertArrayEquals(new int[][] { new int[] { 1974, 7 } }, cid.getIssued().getDateParts());
	}
	
	/**
	 * Tests if a bibliography entry with a date range can be converted
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void singleEntryWithDateRange() throws Exception {
		BibTeXDatabase db = loadUnixDatabase();
		
		BibTeXEntry e = db.resolveEntry(new Key("Lycklama:1978:UTSb"));
		
		BibTeXConverter conv = new BibTeXConverter();
		CSLItemData cid = conv.toItemData(e);
		assertEquals("Lycklama:1978:UTSb", cid.getId());
		assertEquals(CSLType.ARTICLE_JOURNAL, cid.getType());
		assertEquals(1, cid.getAuthor().length);
		assertEquals("Lycklama", cid.getAuthor()[0].getFamily());
		assertEquals("H.", cid.getAuthor()[0].getGiven());
		assertEquals("The Bell System Technical Journal", cid.getCollectionTitle());
		assertEquals("The Bell System Technical Journal", cid.getContainerTitle());
		assertEquals("57", cid.getVolume());
		assertEquals("6", cid.getIssue());
		assertEquals("6", cid.getNumber());
		assertEquals("15", cid.getNumberOfPages());
		assertEquals("2087-2101", cid.getPage());
		assertEquals("2087", cid.getPageFirst());
		assertEquals("UNIX Time-Sharing System: UNIX on a Microprocessor", cid.getTitle());
		assertArrayEquals(new int[][] { new int[] { 1978, 7 }, new int[] { 1978, 8 } }, cid.getIssued().getDateParts());
	}
	
	/**
	 * Tests if a while bibliography database can be converted. Loads the
	 * database and then checks a sample item.
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void allEntries() throws Exception {
		BibTeXDatabase db = loadUnixDatabase();
		BibTeXConverter conv = new BibTeXConverter();
		Map<String, CSLItemData> cids = conv.toItemData(db);
		
		CSLItemData cid = cids.get("Ritchie:1974:UTS");
		assertEquals("Ritchie:1974:UTS", cid.getId());
		assertEquals(CSLType.ARTICLE_JOURNAL, cid.getType());
		assertEquals(2, cid.getAuthor().length);
		assertEquals("Ritchie", cid.getAuthor()[0].getFamily());
		assertEquals("Dennis W.", cid.getAuthor()[0].getGiven());
		assertEquals("Thompson", cid.getAuthor()[1].getFamily());
		assertEquals("Ken", cid.getAuthor()[1].getGiven());
		assertEquals("Communications of the Association for Computing Machinery", cid.getCollectionTitle());
		assertEquals("Communications of the Association for Computing Machinery", cid.getContainerTitle());
		assertEquals("17", cid.getVolume());
		assertEquals("7", cid.getIssue());
		assertEquals("7", cid.getNumber());
		assertEquals("11", cid.getNumberOfPages());
		assertEquals("365-375", cid.getPage());
		assertEquals("365", cid.getPageFirst());
		assertEquals("The UNIX Time-Sharing System", cid.getTitle());
		assertArrayEquals(new int[][] { new int[] { 1974, 7 } }, cid.getIssued().getDateParts());
	}

	/**
	 * Tests if order of items in the BibTeX file is preserved when converting
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void preserveOrder() throws Exception {
		BibTeXDatabase db = loadUnixDatabase();
		BibTeXConverter conv = new BibTeXConverter();
		Map<String, CSLItemData> cids = conv.toItemData(db);

		Iterator<Key> ik1 = db.getEntries().keySet().iterator();
		Iterator<String> ik2 = cids.keySet().iterator();
		while (ik1.hasNext() && ik2.hasNext()) {
			assertEquals(ik1.next().getValue(), ik2.next());
		}
		assertFalse(ik1.hasNext());
		assertFalse(ik2.hasNext());
	}
	
	/**
	 * Test if a BibTeX entry whose title contains a CR character (\r) can
	 * be converted correctly.
	 */
	@Test
	public void carriageReturnInTitle() {
		BibTeXEntry e = new BibTeXEntry(new Key("article"), new Key("a"));
		e.addField(new Key("title"), new StringValue(
				"syst\\`emes\r\ndiff\\'erentiels", StringValue.Style.QUOTED));
		BibTeXConverter conv = new BibTeXConverter();
		CSLItemData i = conv.toItemData(e);
		assertEquals("systèmes différentiels", i.getTitle());
	}
}

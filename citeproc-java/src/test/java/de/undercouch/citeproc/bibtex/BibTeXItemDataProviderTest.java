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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.jbibtex.BibTeXDatabase;
import org.jbibtex.Key;
import org.junit.BeforeClass;
import org.junit.Test;

import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.output.Bibliography;
import de.undercouch.citeproc.output.Citation;

/**
 * Tests the BibTeX citation item provider
 * @author Michel Kraemer
 */
public class BibTeXItemDataProviderTest extends AbstractBibTeXTest {
	private static BibTeXDatabase db;
	private static BibTeXItemDataProvider sys = new BibTeXItemDataProvider();
	
	/**
	 * Set up this test
	 * @throws Exception if something goes wrong
	 */
	@BeforeClass
	public static void setUp() throws Exception {
		db = loadUnixDatabase();
		sys.addDatabase(db);
	}
	
	/**
	 * Tests if a valid bibliography can be generated through the item provider
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void bibliography() throws Exception {
		CSL citeproc = new CSL(sys, "ieee");
		citeproc.setOutputFormat("text");
		
		String id0 = "Johnson:1973:PLB";
		String id1 = "Ritchie:1973:UTS";
		String id2 = "Ritchie:1974:UTS";
		String id3 = "Lycklama:1978:UTSb";
		List<Citation> a = citeproc.makeCitation(id0);
		assertEquals(0, a.get(0).getIndex());
		assertEquals("[1]", a.get(0).getText());
		
		a = citeproc.makeCitation(id1);
		assertEquals(1, a.get(0).getIndex());
		assertEquals("[2]", a.get(0).getText());
		
		a = citeproc.makeCitation(id0, id1);
		assertEquals(2, a.get(0).getIndex());
		assertEquals("[1], [2]", a.get(0).getText());
		
		a = citeproc.makeCitation(id2, id0);
		assertEquals(3, a.get(0).getIndex());
		assertEquals("[1], [3]", a.get(0).getText());
		
		a = citeproc.makeCitation(id3);
		assertEquals(4, a.get(0).getIndex());
		assertEquals("[4]", a.get(0).getText());
		
		Bibliography b = citeproc.makeBibliography();
		assertEquals(4, b.getEntries().length);
		assertEquals("[1]S. C. Johnson and B. W. Kernighan, \u201cThe Programming Language B,\u201d "
				+ "Bell Laboratories,, Murray Hill, NJ, USA, 8, 1973.\n", b.getEntries()[0]);
		assertEquals("[2]D. M. Ritchie and K. Thompson, \u201cThe UNIX time-sharing system,\u201d "
				+ "Operating Systems Review, vol. 7, no. 4, p. 27, Oct. 1973.\n", b.getEntries()[1]);
		assertEquals("[3]D. W. Ritchie and K. Thompson, \u201cThe UNIX Time-Sharing System,\u201d "
				+ "Communications of the Association for Computing Machinery, vol. 17, no. 7, pp. 365\u2013375, "
				+ "Jul. 1974.\n", b.getEntries()[2]);
		assertEquals("[4]H. Lycklama, \u201cUNIX Time-Sharing System: UNIX on a Microprocessor,\u201d "
				+ "The Bell System Technical Journal, vol. 57, no. 6, pp. 2087\u20132101, "
				+ "Jul.\u2013Aug. 1978.\n", b.getEntries()[3]);
	}
	
	/**
	 * Tests if a valid bibliography can be generated through the item provider
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void numericAlphabetical() throws Exception {
		CSL citeproc = new CSL(sys, "din-1505-2-numeric-alphabetical");
		citeproc.setOutputFormat("text");
		
		List<Key> keys = new ArrayList<Key>(db.getEntries().keySet());
		List<String> result = new ArrayList<String>();
		List<Integer> rnds = new ArrayList<Integer>();
		for (int i = 0; i < 10; ++i) {
			int j = (int)(Math.random() * keys.size());
			rnds.add(j);
			Key k = keys.get(j);
			List<Citation> cs = citeproc.makeCitation(k.getValue());
			for (Citation c : cs) {
				while (result.size() <= c.getIndex()) {
					result.add("");
				}
				result.set(c.getIndex(), c.getText());
			}
		}
		
		int c = 0;
		for (Integer r : rnds) {
			Key k = keys.get(r);
			List<Citation> cs = citeproc.makeCitation(k.getValue());
			assertEquals(1, cs.size());
			String nc = cs.get(0).getText();
			String pc = result.get(c);
			assertEquals(nc, pc);
			++c;
		}
	}
}

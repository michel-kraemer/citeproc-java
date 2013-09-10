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

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import de.undercouch.citeproc.csl.CSLDateBuilder;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CSLItemDataBuilder;
import de.undercouch.citeproc.csl.CSLNameBuilder;
import de.undercouch.citeproc.csl.CSLType;
import de.undercouch.citeproc.output.Bibliography;
import de.undercouch.citeproc.output.Citation;

/**
 * Tests the CSL processor {@link CSL}
 * @author Michel Kraemer
 */
public class CSLTest {
	/**
	 * Example citation items
	 */
	private static CSLItemData[] items = new CSLItemData[] {
		new CSLItemDataBuilder("Johnson:1973:PLB", CSLType.REPORT)
			.title("The Programming Language B")
			.author(
					new CSLNameBuilder().given("S. C. ").family("Johnson").build(),
					new CSLNameBuilder().given("B. W. ").family("Kernighan").build()
			)
			.number(8)
			.publisher("Bell Laboratories")
			.publisherPlace("Murray Hill, NJ, USA")
			.issued(1973)
			.build(),
		
		new CSLItemDataBuilder("Ritchie:1973:UTS", CSLType.ARTICLE_JOURNAL)
			.title("The UNIX time-sharing system")
			.author(
					new CSLNameBuilder().given("Dennis M.").family("Ritchie").build(),
					new CSLNameBuilder().given("Ken").family("Thompson").build()
			)
			.volume(7)
			.issue(4)
			.page(27)
			.pageFirst(27)
			.containerTitle("Operating Systems Review")
			.issued(1973, 10)
			.ISSN("0163-5980 (print), 1943-586X (electronic)")
			.build(),
		
		new CSLItemDataBuilder("Ritchie:1974:UTS", CSLType.ARTICLE_JOURNAL)
			.title("The UNIX Time-Sharing System")
			.author(
					new CSLNameBuilder().given("Dennis M.").family("Ritchie").build(),
					new CSLNameBuilder().given("Ken").family("Thompson").build()
			)
			.volume(17)
			.issue(7)
			.page("365-375")
			.pageFirst(365)
			.containerTitle("Communications of the Association for Computing Machinery")
			.issued(1974, 7)
			.ISSN("0001-0782 (print), 1557-7317 (electronic)")
			.build(),
		
		new CSLItemDataBuilder("Lycklama:1978:UTSb", CSLType.ARTICLE_JOURNAL)
			.title("UNIX Time-Sharing System: UNIX on a Microprocessor")
			.author("H.", "Lycklama")
			.volume(57)
			.issue(6)
			.page("2087-2101")
			.issued(new CSLDateBuilder().dateParts(new int[] { 1978, 7 }, new int[] { 1978, 8 }).build())
			.containerTitle("The Bell System Technical Journal")
			.ISSN("0005-8580")
			.URL("http://bstj.bell-labs.com/BSTJ/images/Vol57/bstj57-6-2087.pdf")
			.build()
	};
	
	/**
	 * Retrieves citation items from {@link CSLTest#items}
	 */
	private static class TestItemDataProvider implements ItemDataProvider {
		@Override
		public CSLItemData retrieveItem(String id) {
			for (CSLItemData i : items) {
				if (i.getId().equals(id)) {
					return i;
				}
			}
			return null;
		}
	}
	
	/**
	 * Tests if a valid bibliography can be generated
	 * @throws Exception if anything goes wrong
	 */
	@Test
	public void bibliography() throws Exception {
		CSL citeproc = new CSL(new TestItemDataProvider(), "ieee");
		citeproc.setOutputFormat("text");
		
		List<Citation> a = citeproc.makeCitation(items[0].getId());
		assertEquals(0, a.get(0).getIndex());
		assertEquals("[1]", a.get(0).getText());
		
		a = citeproc.makeCitation(items[1].getId());
		assertEquals(1, a.get(0).getIndex());
		assertEquals("[2]", a.get(0).getText());
		
		a = citeproc.makeCitation(items[0].getId(), items[1].getId());
		assertEquals(2, a.get(0).getIndex());
		assertEquals("[1], [2]", a.get(0).getText());
		
		a = citeproc.makeCitation(items[2].getId(), items[0].getId());
		assertEquals(3, a.get(0).getIndex());
		assertEquals("[1], [3]", a.get(0).getText());
		
		a = citeproc.makeCitation(items[3].getId());
		assertEquals(4, a.get(0).getIndex());
		assertEquals("[4]", a.get(0).getText());
		
		Bibliography b = citeproc.makeBibliography();
		assertEquals(4, b.getEntries().length);
		assertEquals("[1]S. C. Johnson and B. W. Kernighan, \u201cThe Programming Language B,\u201d "
				+ "Bell Laboratories, Murray Hill, NJ, USA, 8, 1973.\n", b.getEntries()[0]);
		assertEquals("[2]D. M. Ritchie and K. Thompson, \u201cThe UNIX time-sharing system,\u201d "
				+ "Operating Systems Review, vol. 7, no. 4, p. 27, Oct. 1973.\n", b.getEntries()[1]);
		assertEquals("[3]D. M. Ritchie and K. Thompson, \u201cThe UNIX Time-Sharing System,\u201d "
				+ "Communications of the Association for Computing Machinery, vol. 17, no. 7, pp. 365\u2013375, "
				+ "Jul. 1974.\n", b.getEntries()[2]);
		assertEquals("[4]H. Lycklama, \u201cUNIX Time-Sharing System: UNIX on a Microprocessor,\u201d "
				+ "The Bell System Technical Journal, vol. 57, no. 6, pp. 2087\u20132101, "
				+ "Jul.\u2013Aug. 1978.\n", b.getEntries()[3]);
	}
	
	/**
	 * Tests if an ad hoc bibliography can be created
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void makeAdhocBibliography() throws Exception {
		CSLItemData item = new CSLItemDataBuilder("citeproc-java", CSLType.WEBPAGE)
			.title("citeproc-java: A Citation Style Language (CSL) processor for Java")
			.author("Michel", "Kr\u00E4mer")
			.issued(2013, 9, 7)
			.URL("http://michel-kraemer.github.io/citeproc-java/")
			.accessed(2013, 12, 6)
			.build();
		
		String bibl = CSL.makeAdhocBibliography("ieee", "text", item).makeString();
		assertEquals("[1]M. Kr\u00E4mer, \u201cciteproc-java: A Citation Style "
				+ "Language (CSL) processor for Java,\u201d 07-Sep-2013. [Online]. "
				+ "Available: http://michel-kraemer.github.io/citeproc-java/. [Accessed: 06-Dec-2013].\n", bibl);
	}
}

package de.undercouch.citeproc;

import de.undercouch.citeproc.csl.CSLAbbreviationList;
import de.undercouch.citeproc.csl.CSLAbbreviationListBuilder;
import de.undercouch.citeproc.csl.CSLCitation;
import de.undercouch.citeproc.csl.CSLCitationItem;
import de.undercouch.citeproc.csl.CSLDateBuilder;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CSLItemDataBuilder;
import de.undercouch.citeproc.csl.CSLNameBuilder;
import de.undercouch.citeproc.csl.CSLPropertiesBuilder;
import de.undercouch.citeproc.csl.CSLType;
import de.undercouch.citeproc.csl.CitationIDIndexPair;
import de.undercouch.citeproc.output.Bibliography;
import de.undercouch.citeproc.output.Citation;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the CSL processor {@link CSL}
 * @author Michel Kraemer
 */
public class CSLTest {
    /**
     * Example citation items
     */
    private static CSLItemData[] items = new CSLItemData[] {
            new CSLItemDataBuilder()
                    .id("Johnson:1973:PLB")
                    .type(CSLType.REPORT)
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

            new CSLItemDataBuilder()
                    .id("Ritchie:1973:UTS")
                    .type(CSLType.ARTICLE_JOURNAL)
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

            new CSLItemDataBuilder()
                    .id("Ritchie:1974:UTS")
                    .type(CSLType.ARTICLE_JOURNAL)
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

            new CSLItemDataBuilder()
                    .id("Lycklama:1978:UTSb")
                    .type(CSLType.ARTICLE_JOURNAL)
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
     * Tests if a valid bibliography can be generated
     * @throws Exception if anything goes wrong
     */
    @Test
    public void bibliography() throws Exception {
        try (CSL citeproc = new CSL(new ListItemDataProvider(items), "ieee")) {
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
    }

    /**
     * Tests if a valid bibliography can be generated with a selection
     * @throws Exception if anything goes wrong
     */
    @Test
    public void bibliographySelection() throws Exception {
        try (CSL citeproc = new CSL(new ListItemDataProvider(items), "ieee")) {
            citeproc.setOutputFormat("text");

            List<Citation> a = citeproc.makeCitation(items[0].getId());
            assertEquals(0, a.get(0).getIndex());
            assertEquals("[1]", a.get(0).getText());

            a = citeproc.makeCitation(items[1].getId());
            assertEquals(1, a.get(0).getIndex());
            assertEquals("[2]", a.get(0).getText());

            Bibliography b = citeproc.makeBibliography(SelectionMode.SELECT,
                    new CSLItemDataBuilder().title("The Programming Language B").build());
            assertEquals(1, b.getEntries().length);
            assertTrue(b.getEntries()[0].startsWith("[1]S. C. Johnson"));
        }
    }

    /**
     * Tests if an ad hoc bibliography can be created
     * @throws Exception if something goes wrong
     */
    @Test
    public void makeAdhocBibliography() throws Exception {
        CSLItemData item = new CSLItemDataBuilder()
                .id("citeproc-java")
                .type(CSLType.WEBPAGE)
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

    /**
     * Tests if the processor throws an {@link IllegalArgumentException} if
     * a citation item does not exist
     * @throws Exception if something else goes wrong
     */
    @Test(expected = IllegalArgumentException.class)
    public void missingItem() throws Exception {
        try (CSL citeproc = new CSL(new ListItemDataProvider(items), "ieee")) {
            citeproc.makeCitation("foobar");
        }
    }

    /**
     * Tests if the processor correctly produces links for URLs
     * @throws Exception if something goes wrong
     */
    @Test
    public void links() throws Exception {
        CSLItemData item = new CSLItemDataBuilder()
                .id("citeproc-java")
                .type(CSLType.WEBPAGE)
                .title("citeproc-java: A Citation Style Language (CSL) processor for Java")
                .author("Michel", "Kr\u00E4mer")
                .issued(2013, 9, 9)
                .URL("http://michel-kraemer.github.io/citeproc-java/")
                .accessed(2013, 9, 11)
                .build();

        try (CSL citeproc = new CSL(new ListItemDataProvider(item), "ieee")) {
            citeproc.setOutputFormat("html");
            citeproc.setConvertLinks(true);

            List<Citation> a = citeproc.makeCitation("citeproc-java");
            assertEquals(0, a.get(0).getIndex());
            assertEquals("[1]", a.get(0).getText());

            Bibliography b = citeproc.makeBibliography();
            assertEquals(1, b.getEntries().length);
            assertEquals("  <div class=\"csl-entry\">\n"
                    + "    <div class=\"csl-left-margin\">[1]</div><div class=\"csl-right-inline\">"
                    + "M. Kr\u00E4mer, \u201cciteproc-java: A Citation Style Language (CSL) processor for Java,\u201d"
                    + " 09-Sep-2013. [Online]. Available: "
                    + "<a href=\"http://michel-kraemer.github.io/citeproc-java/\">http://michel-kraemer.github.io/citeproc-java/</a>. "
                    + "[Accessed: 11-Sep-2013].</div>\n"
                    + "  </div>\n", b.getEntries()[0]);
        }
    }

    /**
     * Tests the AsciiDoc output format
     * @throws Exception if something goes wrong
     */
    @Test
    public void asciiDocFormat() throws Exception {
        CSLItemData item = new CSLItemDataBuilder()
                .id("citeproc-java")
                .type(CSLType.WEBPAGE)
                .title("citeproc-java: A Citation Style Language (CSL) processor for Java")
                .author("Michel", "Kr\u00E4mer")
                .issued(2013, 9, 9)
                .URL("http://michel-kraemer.github.io/citeproc-java/")
                .accessed(2013, 9, 11)
                .build();

        try (CSL citeproc = new CSL(new ListItemDataProvider(item), "ieee")) {
            citeproc.setOutputFormat("asciidoc");
            citeproc.makeCitation("citeproc-java");

            Bibliography b = citeproc.makeBibliography();

            assertEquals(1, b.getEntries().length);
            assertEquals("[1] M. Kr\u00E4mer, ``citeproc-java: A Citation Style "
                    + "Language (CSL) processor for Java,'' 09-Sep-2013. [Online]. "
                    + "Available: http://michel-kraemer.github.io/citeproc-java/. "
                    + "[Accessed: 11-Sep-2013].\n", b.getEntries()[0]);
        }
    }

    /**
     * Tests the FO output format
     * @throws Exception if something goes wrong
     */
    @Test
    public void asciiFOFormat() throws Exception {
        try (CSL citeproc = new CSL(new ListItemDataProvider(items), "ieee")) {
            citeproc.setOutputFormat("fo");
            citeproc.makeCitation(items[0].getId());

            Bibliography b = citeproc.makeBibliography();

            assertEquals(1, b.getEntries().length);
            assertEquals("<fo:block id=\"Johnson:1973:PLB\">\n"
                    + "  <fo:table table-layout=\"fixed\" width=\"100%\">\n"
                    + "    <fo:table-column column-number=\"1\" column-width=\"2.5em\"/>\n"
                    + "    <fo:table-column column-number=\"2\" column-width=\"proportional-column-width(1)\"/>\n"
                    + "    <fo:table-body>\n"
                    + "      <fo:table-row>\n"
                    + "        <fo:table-cell>\n"
                    + "          <fo:block>[1]</fo:block>\n"
                    + "        </fo:table-cell>\n"
                    + "        <fo:table-cell>\n"
                    + "          <fo:block>S. C. Johnson and B. W. Kernighan, "
                    + "\u201cThe Programming Language B,\u201d Bell Laboratories, "
                    + "Murray Hill, NJ, USA, 8, 1973.</fo:block>\n"
                    + "        </fo:table-cell>\n"
                    + "      </fo:table-row>\n"
                    + "    </fo:table-body>\n"
                    + "  </fo:table>\n"
                    + "</fo:block>\n", b.getEntries()[0]);
        }
    }

    /**
     * Tests if the processor's state can be reset
     * @throws Exception if something goes wrong
     */
    @Test
    public void reset() throws Exception {
        try (CSL citeproc = new CSL(new ListItemDataProvider(items), "ieee")) {
            citeproc.setOutputFormat("text");

            List<Citation> a = citeproc.makeCitation(items[0].getId());
            assertEquals(0, a.get(0).getIndex());
            assertEquals("[1]", a.get(0).getText());

            Bibliography b = citeproc.makeBibliography();
            assertEquals(1, b.getEntries().length);

            citeproc.reset();

            b = citeproc.makeBibliography();
            assertEquals(0, b.getEntries().length);
        }
    }

    /**
     * Checks if event-place is considered
     * @throws Exception if something goes wrong
     */
    @Test
    public void eventPlace() throws Exception {
        CSLItemData item = new CSLItemDataBuilder()
                .type(CSLType.PAPER_CONFERENCE)
                .title("The Paper")
                .author("The", "Author")
                .event("Conference")
                .eventPlace("The Place")
                .build();
        String bib = CSL.makeAdhocBibliography("ieee", "text", item).makeString();
        assertEquals("[1]T. Author, \u201cThe Paper,\u201d presented at the Conference, The Place.\n", bib);
    }

    /**
     * Tests the {@link CSL#makeCitation(CSLCitation, List, List)} method
     * @throws Exception if something goes wrong
     */
    @Test
    public void makeCitationPrePost() throws Exception {
        try (CSL citeproc = new CSL(new ListItemDataProvider(items), "ieee")) {
            citeproc.setOutputFormat("text");

            CSLCitation cit1 = new CSLCitation(new CSLCitationItem[] {
                    new CSLCitationItem(items[0].getId()) }, "CITATION-1",
                    new CSLPropertiesBuilder().noteIndex(1).build());
            CSLCitation cit2 = new CSLCitation(new CSLCitationItem[] {
                    new CSLCitationItem(items[2].getId()) }, "CITATION-2",
                    new CSLPropertiesBuilder().noteIndex(2).build());
            CSLCitation cit3 = new CSLCitation(new CSLCitationItem[] {
                    new CSLCitationItem(items[3].getId()) }, "CITATION-3",
                    new CSLPropertiesBuilder().noteIndex(3).build());
            CSLCitation cit4 = new CSLCitation(new CSLCitationItem[] {
                    new CSLCitationItem(items[2].getId()) }, "CITATION-4",
                    new CSLPropertiesBuilder().noteIndex(2).build());

            List<Citation> a1 = citeproc.makeCitation(cit1,
                    Collections.emptyList(),
                    Collections.emptyList());
            List<Citation> a2 = citeproc.makeCitation(cit2,
                    Collections.singletonList(new CitationIDIndexPair(cit1)),
                    Collections.emptyList());
            List<Citation> a3 = citeproc.makeCitation(cit3,
                    Collections.singletonList(new CitationIDIndexPair(cit1)),
                    Collections.singletonList(new CitationIDIndexPair(cit2)));
            List<Citation> a4 = citeproc.makeCitation(cit4,
                    Collections.singletonList(new CitationIDIndexPair(cit1)),
                    Arrays.asList(new CitationIDIndexPair(cit2), new CitationIDIndexPair(cit3)));

            assertEquals(1, a1.size());
            assertEquals("[1]", a1.get(0).getText());
            assertEquals(0, a1.get(0).getIndex());

            assertEquals(1, a2.size());
            assertEquals("[2]", a2.get(0).getText());
            assertEquals(1, a2.get(0).getIndex());

            assertEquals(2, a3.size());
            assertEquals("[2]", a3.get(0).getText());
            assertEquals(1, a3.get(0).getIndex());
            assertEquals("[3]", a3.get(1).getText());
            assertEquals(2, a3.get(1).getIndex());

            // we should now have two items with ID [2], but different indexes
            assertEquals(3, a4.size());
            assertEquals("[2]", a4.get(0).getText());
            assertEquals(1, a4.get(0).getIndex());
            assertEquals("[2]", a4.get(1).getText());
            assertEquals(2, a4.get(1).getIndex());
            assertEquals("[3]", a4.get(2).getText());
            assertEquals(3, a4.get(2).getIndex());
        }
    }

    /**
     * Tests if abbreviations can be used
     * @throws Exception if something goes wrong
     */
    @Test
    public void abbreviations() throws Exception {
        Map<String, String> titleAbbreviations = new HashMap<>();
        titleAbbreviations.put("The Programming Language B", "B");

        CSLAbbreviationList abbrevs = new CSLAbbreviationListBuilder()
                .title(titleAbbreviations)
                .build();

        DefaultAbbreviationProvider prov = new DefaultAbbreviationProvider();
        prov.add(AbbreviationProvider.DEFAULT_LIST_NAME, abbrevs);

        try (CSL citeproc = new CSL(new ListItemDataProvider(items), prov, "chicago-note-bibliography")) {
            citeproc.setOutputFormat("text");
            citeproc.setAbbreviations(AbbreviationProvider.DEFAULT_LIST_NAME);

            List<Citation> a = citeproc.makeCitation(items[0].getId());
            assertEquals(0, a.get(0).getIndex());
            assertEquals("Johnson and Kernighan, \u201cB.\u201d", a.get(0).getText());
        }
    }

    /**
     * Tests if output can be decorated
     * @throws Exception if something goes wrong
     */
    @Test
    public void variableWrapper() throws Exception {
        VariableWrapper wrapper = (params, prePunct, str, postPunct) -> {
            if (params.getContext() == Context.CITATION &&
                    Arrays.asList(params.getVariableNames()).contains("author")) {
                return prePunct + "<strong>" + str + "</strong>" + postPunct;
            }
            return prePunct + str + postPunct;
        };

        try (CSL citeproc = new CSL(new ListItemDataProvider(items),
                new DefaultLocaleProvider(), new DefaultAbbreviationProvider(),
                wrapper, "chicago-note-bibliography", "en-US", false)) {
            List<Citation> a = citeproc.makeCitation(items[0].getId());
            assertEquals(0, a.get(0).getIndex());
            assertEquals("<strong>Johnson and Kernighan</strong>, "
                            + "\u201cThe Programming Language B.\u201d",
                    a.get(0).getText());
        }
    }

    /**
     * Tests if citation items can be registered unsorted
     * @throws Exception if something goes wrong
     */
    @Test
    public void registerUnsorted() throws Exception {
        try (CSL citeproc = new CSL(new ListItemDataProvider(items), "chicago-note-bibliography")) {
            citeproc.setOutputFormat("text");

            String[] ids = new String[] { items[0].getId(), items[1].getId(), items[3].getId() };
            citeproc.registerCitationItems(ids);

            Bibliography b = citeproc.makeBibliography();
            assertEquals(3, b.getEntries().length);
            assertTrue(b.getEntries()[0].startsWith("Johnson"));
            assertTrue(b.getEntries()[1].startsWith("Lycklama"));
            assertTrue(b.getEntries()[2].startsWith("Ritchie"));

            citeproc.registerCitationItems(ids, true);
            b = citeproc.makeBibliography();
            assertEquals(3, b.getEntries().length);
            assertTrue(b.getEntries()[0].startsWith("Johnson"));
            assertTrue(b.getEntries()[1].startsWith("Ritchie"));
            assertTrue(b.getEntries()[2].startsWith("Lycklama"));
        }
    }

    /**
     * Checks if the supported output formats are calculated correctly
     * @throws Exception if something goes wrong
     */
    @Test
    public void getSupportedFormats() throws Exception {
        List<String> sf = CSL.getSupportedOutputFormats();
        assertEquals(6, sf.size());
        assertTrue(sf.contains("html"));
        assertTrue(sf.contains("text"));
        assertTrue(sf.contains("latex"));
        assertTrue(sf.contains("asciidoc"));
        assertTrue(sf.contains("rtf"));
        assertTrue(sf.contains("fo"));
    }

    /**
     * Checks if the supported styles are calculated correctly
     * @throws Exception if something goes wrong
     */
    @Test
    public void getSupportedStyles() throws Exception {
        Set<String> ss = CSL.getSupportedStyles();
        assertTrue(ss.size() > 5000);
        assertTrue(ss.contains("ieee"));
        assertTrue(ss.contains("apa"));
        assertTrue(ss.contains("vancouver"));
    }

    /**
     * Makes sure some styles are supported
     */
    @Test
    public void supportsStyle() {
        assertTrue(CSL.supportsStyle("ieee"));
        assertTrue(CSL.supportsStyle("apa"));
        assertFalse(CSL.supportsStyle("jkseghg"));
    }

    /**
     * Checks if the supported locales are calculated correctly
     * @throws Exception if something goes wrong
     */
    @Test
    public void getSupportedLocales() throws Exception {
        Set<String> ss = CSL.getSupportedLocales();
        assertTrue(ss.size() > 40);
        assertTrue(ss.contains("de-DE"));
        assertTrue(ss.contains("en-US"));
        assertTrue(ss.contains("en-GB"));
        assertTrue(ss.contains("de"));
        assertTrue(ss.contains("en"));
    }

    /**
     * Test if a dependent style can be loaded
     * @throws Exception if something goes wrong
     */
    @Test
    public void dependentStyle() throws Exception {
        String bibl = CSL.makeAdhocBibliography("dependent/proceedings-of-the-ieee.csl",
                "text", items[0]).makeString();
        assertEquals("[1]S. C. Johnson and B. W. Kernighan, \u201cThe Programming Language B,\u201d "
                + "Bell Laboratories, Murray Hill, NJ, USA, 8, 1973.\n", bibl);
    }
}

package de.undercouch.citeproc.bibtex;

import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CSLName;
import de.undercouch.citeproc.csl.CSLNameBuilder;
import de.undercouch.citeproc.csl.CSLType;
import org.jbibtex.*;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.*;

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
        assertEquals("Communications of the Association for Computing Machinery", cid.getContainerTitle());
        assertEquals("17", cid.getVolume());
        assertEquals("7", cid.getNumber());
        assertEquals("11", cid.getNumberOfPages());
        assertEquals("365-375", cid.getPage());
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
        assertEquals("The Bell System Technical Journal", cid.getContainerTitle());
        assertEquals("57", cid.getVolume());
        assertEquals("6", cid.getNumber());
        assertEquals("15", cid.getNumberOfPages());
        assertEquals("2087-2101", cid.getPage());
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
        assertEquals("Communications of the Association for Computing Machinery", cid.getContainerTitle());
        assertEquals("17", cid.getVolume());
        assertEquals("7", cid.getNumber());
        assertEquals("11", cid.getNumberOfPages());
        assertEquals("365-375", cid.getPage());
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

    /**
     * Test if the field {@code language} of BibTeX entry is read.
     */
    @Test
    public void shouldReadTheLanguageFieldOfASingleEntry() throws IOException, ParseException {
        BibTeXDatabase db = loadUnixDatabase();
        BibTeXEntry e = db.resolveEntry(new Key("Bach:1986:UTS"));
        BibTeXConverter conv = new BibTeXConverter();
        CSLItemData cid = conv.toItemData(e);
        assertEquals("Bach:1986:UTS", cid.getId());
        assertEquals("German", cid.getLanguage());
    }

    /**
     * Test if the field {@code genre} of the CSL entry is set for theses.
     */
    @Test
    public void shouldSetGenreFromTypeForTheses() throws IOException, ParseException {
        BibTeXDatabase db = loadUnixDatabase();
        BibTeXEntry e = db.resolveEntry(new Key("Wang:2002:DIR"));
        BibTeXConverter conv = new BibTeXConverter();
        CSLItemData cid = conv.toItemData(e);
        assertEquals("CSL 'type' not set", CSLType.THESIS, cid.getType());
        assertEquals("CSL 'genre' not set", "Thesis (Ph.D.)", cid.getGenre());
    }

    /**
     * Test if a chapter in a book series is converted correctly
     * @throws ParseException if the BibTeX entry could not be parsed
     */
    @Test
    public void inBookSeries() throws ParseException {
        String entry = "@incollection{kraemer-2021,\n" +
                "  author    = {Michel Krämer},\n" +
                "  editor    = {Hammoudi, Slimane and Quix, Christoph and Bernardino, Jorge},\n" +
                "  title     = {Efficient Scheduling of Scientific Workflow Actions in the\n" +
                "    Cloud Based on Required Capabilities},\n" +
                "  booktitle = {Data Management Technologies and Applications},\n" +
                "  year      = {2021},\n" +
                "  publisher = {Springer International Publishing},\n" +
                "  address   = {Cham},\n" +
                "  volume    = {1446},\n" +
                "  series    = {Communications in Computer and Information Science},\n" +
                "  pages     = {32--55},\n" +
                "  isbn      = {978-3-030-83014-4},\n" +
                "  doi       = {10.1007/978-3-030-83014-4_2}\n" +
        "}";
        BibTeXDatabase db = new BibTeXParser().parse(new StringReader(entry));
        BibTeXConverter converter = new BibTeXConverter();
        Map<String, CSLItemData> items = converter.toItemData(db);
        CSLItemData item = items.get("kraemer-2021");
        assertEquals(CSLType.CHAPTER, item.getType());
        assertEquals(1, item.getAuthor().length);
        assertEquals(3, item.getEditor().length);
        assertEquals("Efficient Scheduling of Scientific Workflow Actions " +
                "in the Cloud Based on Required Capabilities", item.getTitle());
        assertEquals("Data Management Technologies and Applications",
                item.getContainerTitle());
        assertEquals("Communications in Computer and Information Science",
                item.getCollectionTitle());
        assertEquals("1446", item.getVolume());
        assertEquals(2021, item.getIssued().getDateParts()[0][0]);
        assertEquals("Springer International Publishing", item.getPublisher());
        assertEquals("Cham", item.getPublisherPlace());
        assertEquals("978-3-030-83014-4", item.getISBN());
        assertEquals("10.1007/978-3-030-83014-4_2", item.getDOI());
    }

    /**
     * Check if the parser correctly falls back to a literal string if the
     * 'pages' field contains an illegal number.
     * See <a href="https://github.com/michel-kraemer/citeproc-java/issues/114">issue 114</a>
     * @throws ParseException if the BibTeX entry could not be parsed
     */
    @Test
    public void invalidPageNumber() throws ParseException {
        String entry = "@Article{baks-2021,\n" +
                "  author           = {Sandipan Baksi},\n" +
                "  date             = {2021},\n" +
                "  journaltitle     = {The Indian Economic {\\&} Social History Review},\n" +
                "  pages            = {001946462110645},\n" +
                "  title            = {Science journalism in Hindi in pre-independence India: A study of Hindi periodicals},\n" +
                "  doi              = {10.1177/00194646211064586},\n" +
                "  creationdate     = {2022-01-03T11:59:38},\n" +
                "  modificationdate = {2022-01-03T12:01:49},\n" +
                "  publisher        = {{SAGE} Publications},\n" +
                "}";
        BibTeXDatabase db = new BibTeXParser().parse(new StringReader(entry));
        BibTeXConverter converter = new BibTeXConverter();
        Map<String, CSLItemData> items = converter.toItemData(db);
        CSLItemData item = items.get("baks-2021");
        assertEquals("001946462110645", item.getPage());
    }

    /**
     * Check if the 'urldate' of a webpage is correctly mapped to the CSL
     * accessed field.
     * See <a href="https://github.com/michel-kraemer/citeproc-java/issues/115">issue 115</a>
     * @throws ParseException if the BibTeX entry could not be parsed
     */
    @Test
    public void urldate() throws ParseException {
        String entry = "@online{testcitationkey,\n" +
                "title = {Title of the test entry},\n" +
                "url = {https://test.com},\n" +
                "author = {Testname, Test},\n" +
                "urldate = {2020-01-02},\n" +
                "date = {1984},\n" +
                "}";
        BibTeXDatabase db = new BibTeXParser().parse(new StringReader(entry));
        BibTeXConverter converter = new BibTeXConverter();
        Map<String, CSLItemData> items = converter.toItemData(db);
        CSLItemData item = items.get("testcitationkey");
        assertEquals("2020-01-02", item.getAccessed().getRaw());
        assertEquals("1984", item.getIssued().getRaw());
    }

    @Test
    public void curlyBracesAreKept() throws ParseException {
        String entry = "@online{testcitationkey,\n" +
                "  author = {{The PGF/TikZ Team} and others},\n" +
                "  journal = {TUGBoat},\n" +
                "  title = {pgf – Create PostScript and PDF graphics in TeX},\n" +
                "  year = {2013},\n" +
                "  _jabref_shared = {sharedId: -1, version: 1}\n" +
                "}";

        BibTeXDatabase db = new BibTeXParser().parse(new StringReader(entry));
        BibTeXConverter converter = new BibTeXConverter();
        Map<String, CSLItemData> items = converter.toItemData(db);
        CSLItemData item = items.get("testcitationkey");

        CSLName name = new CSLNameBuilder()
                .literal("The PGF/TikZ Team")
                .build();

        assertEquals(name.getLiteral(), item.getAuthor()[0].getLiteral());
    }

    @Test
    public void curlyNestedBracesAreKept() throws ParseException {
        String entry = "@online{testcitationkey,\n" +
                "  author = {{The PGF/TikZ Team} and {JabRef e.V}},\n" +
                "  journal = {TUGBoat},\n" +
                "  title = {pgf – Create PostScript and PDF graphics in TeX},\n" +
                "  year = {2013},\n" +
                "  _jabref_shared = {sharedId: -1, version: 1}\n" +
                "}";

        BibTeXDatabase db = new BibTeXParser().parse(new StringReader(entry));
        BibTeXConverter converter = new BibTeXConverter();
        Map<String, CSLItemData> items = converter.toItemData(db);
        CSLItemData item = items.get("testcitationkey");

        CSLName name = new CSLNameBuilder()
                .literal("The PGF/TikZ Team")
                .build();

        CSLName jabrefName = new CSLNameBuilder()
                .literal("JabRef e.V")
                .build();

        assertEquals(name.getLiteral(), item.getAuthor()[0].getLiteral());
        assertEquals(jabrefName.getLiteral(), item.getAuthor()[1].getLiteral());
    }

    @Test
    public void editorCurlyBracesAreKept() throws ParseException {
        String entry = "@online{testcitationkey,\n" +
                "  editor = {{The PGF/TikZ Team} and others},\n" +
                "  title = {pgf – Create PostScript and PDF graphics in TeX},\n" +
                "  year = {2013}\n" +
                "}";

        BibTeXDatabase db = new BibTeXParser().parse(new StringReader(entry));
        BibTeXConverter converter = new BibTeXConverter();
        Map<String, CSLItemData> items = converter.toItemData(db);
        CSLItemData item = items.get("testcitationkey");

        CSLName name = new CSLNameBuilder()
                .literal("The PGF/TikZ Team")
                .build();

        assertEquals(2, item.getEditor().length);
        assertEquals(name.getLiteral(), item.getEditor()[0].getLiteral());
    }

    @Test
    public void editorMixedLiteralAndPerson() throws ParseException {
        String entry = "@book{mixededitors,\n" +
                "  editor = {{ACME Inc} and Doe, John},\n" +
                "  title = {Some Book},\n" +
                "  year = {2020}\n" +
                "}";

        BibTeXDatabase db = new BibTeXParser().parse(new StringReader(entry));
        BibTeXConverter converter = new BibTeXConverter();
        Map<String, CSLItemData> items = converter.toItemData(db);
        CSLItemData item = items.get("mixededitors");

        assertEquals(2, item.getEditor().length);
        assertEquals("ACME Inc", item.getEditor()[0].getLiteral());
        assertNull("First editor should be literal; no family name", item.getEditor()[0].getFamily());
        assertEquals("Doe", item.getEditor()[1].getFamily());
        assertEquals("John", item.getEditor()[1].getGiven());
    }

    @Test
    public void titleBracesNotReadded() throws ParseException {
        String entry = "@article{titlebraces,\n" +
                "  author = {Test, Author},\n" +
                "  title = {An {E}xample with {B}races in {T}itle},\n" +
                "  year = {2022}\n" +
                "}";

        BibTeXDatabase db = new BibTeXParser().parse(new StringReader(entry));
        BibTeXConverter converter = new BibTeXConverter();
        Map<String, CSLItemData> items = converter.toItemData(db);
        CSLItemData item = items.get("titlebraces");

        String title = item.getTitle();
        assertNotNull(title);
        assertFalse("Title should not contain curly braces after conversion", title.contains("{"));
        assertFalse("Title should not contain curly braces after conversion", title.contains("}"));
        assertEquals("An Example with Braces in Title", title);
    }

}

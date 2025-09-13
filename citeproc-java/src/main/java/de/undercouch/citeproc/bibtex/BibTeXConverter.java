package de.undercouch.citeproc.bibtex;

import de.undercouch.citeproc.csl.CSLDate;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CSLItemDataBuilder;
import de.undercouch.citeproc.csl.CSLType;
import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.BibTeXParser;
import org.jbibtex.BibTeXString;
import org.jbibtex.Key;
import org.jbibtex.LaTeXObject;
import org.jbibtex.LaTeXParser;
import org.jbibtex.LaTeXPrinter;
import org.jbibtex.ParseException;
import org.jbibtex.TokenMgrException;
import org.jbibtex.Value;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * <p>Converts BibTeX items to CSL citation items</p>
 * <p>The class maps BibTeX attributes to CSL attributes. The mapping is
 * based on the one used in <a href="http://www.docear.org">Docear</a> as
 * <a href="http://www.docear.org/2012/08/08/docear4word-mapping-bibtex-fields-and-types-with-the-citation-style-language/">presented
 * by Joeran Beel</a>.</p>
 * <p>Docear is released under the GPLv2 but its code may also be reused in
 * projects licensed under Apache License 2.0 (see
 * <a href="http://www.docear.org/software/licence/">http://www.docear.org/software/licence/</a>,
 * last visited 2013-09-06). The mapping here is released under the
 * Apache License 2.0 by permission of Joaran Beel, Docear.</p>
 * @author Joaran Beel
 * @author Michel Kraemer
 */
public class BibTeXConverter {
    private static final String FIELD_ABSTRACT = "abstract";
    private static final String FIELD_ACCESSED = "accessed";
    private static final String FIELD_ADDRESS = "address";
    private static final String FIELD_ANNOTE = "annote";
    private static final String FIELD_AUTHOR = "author";
    private static final String FIELD_BOOKTITLE = "booktitle";
    private static final String FIELD_CHAPTER = "chapter";
    private static final String FIELD_DATE = "date";
    private static final String FIELD_DOI = "doi";
    private static final String FIELD_EDITION = "edition";
    private static final String FIELD_EDITOR = "editor";
    private static final String FIELD_INSTITUTION = "institution";
    private static final String FIELD_ISBN = "isbn";
    private static final String FIELD_ISSN = "issn";
    private static final String FIELD_ISSUE = "issue";
    private static final String FIELD_JOURNAL = "journal";
    private static final String FIELD_JOURNALTITLE = "journaltitle";
    private static final String FIELD_KEYWORDS = "keywords";
    private static final String FIELD_LANGUAGE = "language";
    private static final String FIELD_LOCATION = "location";
    private static final String FIELD_MONTH = "month";
    private static final String FIELD_NOTE = "note";
    private static final String FIELD_NUMBER = "number";
    private static final String FIELD_ORGANIZATION = "organization";
    private static final String FIELD_PAGES = "pages";
    private static final String FIELD_PUBLISHER = "publisher";
    private static final String FIELD_REVISION = "revision";
    private static final String FIELD_SCHOOL = "school";
    private static final String FIELD_SERIES = "series";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_TITLE = "title";
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_URL = "url";
    private static final String FIELD_URLDATE = "urldate";
    private static final String FIELD_VOLUME = "volume";
    private static final String FIELD_YEAR = "year";

    private static final String TYPE_ARTICLE = "article";
    private static final String TYPE_BOOK = "book";
    private static final String TYPE_BOOKLET = "booklet";
    private static final String TYPE_CONFERENCE = "conference";
    private static final String TYPE_ELECTRONIC = "electronic";
    private static final String TYPE_INBOOK = "inbook";
    private static final String TYPE_INCOLLECTION = "incollection";
    private static final String TYPE_INPROCEEDINGS = "inproceedings";
    private static final String TYPE_MANUAL = "manual";
    private static final String TYPE_MASTERSTHESIS = "mastersthesis";
    private static final String TYPE_ONLINE = "online";
    private static final String TYPE_PATENT = "patent";
    private static final String TYPE_PERIODICAL = "periodical";
    private static final String TYPE_PHDTHESIS = "phdthesis";
    private static final String TYPE_PROCEEDINGS = "proceedings";
    private static final String TYPE_STANDARD = "standard";
    private static final String TYPE_TECHREPORT = "techreport";
    private static final String TYPE_UNPUBLISHED = "unpublished";
    private static final String TYPE_WWW = "www";

    private final LaTeXParser latexParser;
    private final LaTeXPrinter latexPrinter;

    /**
     * Default constructor
     */
    public BibTeXConverter() {
        try {
            latexParser = new LaTeXParser();
        } catch (ParseException e) {
            // can actually never happen because the default constructor
            // of LaTeXParser doesn't throw
            throw new RuntimeException(e);
        }
        latexPrinter = new LaTeXPrinter();
    }

    /**
     * Re-add curly braces around parts of a name string that were enclosed in curly
     * braces in the original BibTeX field. This is necessary because the LaTeX conversion
     * removes braces, but NameParser uses them to detect literal names.
     * Only applies to author/editor fields.
     * @param original the original field value (possibly containing braces and LaTeX)
     * @param converted the field value after LaTeX conversion
     * @return the converted string with curly braces re-added where appropriate
     */
    private String reAddCurlyBracesInNames(String original, String converted) {
        if (original == null || converted == null || original.indexOf('{') < 0) {
            return converted;
        }

        int len = original.length();
        // Collect all top-level brace segments (and nested ones), but skip one that
        // wraps the whole string
        List<int[]> segments = new ArrayList<>();
        int depth = 0;
        int segStart = -1;
        for (int i = 0; i < len; ++i) {
            char c = original.charAt(i);
            if (c == '{') {
                depth++;
                if (depth == 1) {
                    segStart = i;
                }
            } else if (c == '}') {
                if (depth == 1) {
                    // candidate segment from segStart to i
                    // skip if it covers the whole string
                    if (!(segStart == 0 && i == len - 1)) {
                        segments.add(new int[] { segStart, i });
                    }
                    segStart = -1;
                }
                if (depth > 0) {
                    depth--;
                }
            }
        }

        if (segments.isEmpty()) {
            return converted;
        }

        String result = converted;
        for (int[] seg : segments) {
            int start = seg[0] + 1; // content inside braces
            int end = seg[1];
            if (start >= end) {
                continue;
            }
            // Convert the inner LaTeX to the same plain text to find in converted
            String innerConverted = original.substring(start, end);
            try {
                java.util.List<LaTeXObject> objs = latexParser.parse(new StringReader(innerConverted));
                innerConverted = latexPrinter.print(objs).replaceAll("\\n", " ").replaceAll("\\r", "").trim();
            } catch (Exception ex) {
                // ignore parse errors; fall back to raw inner
            }
            if (innerConverted.isEmpty()) {
                continue;
            }
            result = wrapWithBracesIfFound(result, innerConverted);
        }
        return result;
    }

    private String wrapWithBracesIfFound(String text, String needle) {
        int fromIndex = 0;
        StringBuilder sb = new StringBuilder();
        while (fromIndex < text.length()) {
            int idx = text.indexOf(needle, fromIndex);
            if (idx < 0) {
                sb.append(text, fromIndex, text.length());
                break;
            }
            // Check if already wrapped with braces (ignoring spaces around)
            int left = idx - 1;
            while (left >= fromIndex && Character.isWhitespace(text.charAt(left))) {
                left--;
            }
            int right = idx + needle.length();
            int r = right;
            while (r < text.length() && Character.isWhitespace(text.charAt(r))) {
                r++;
            }
            boolean alreadyWrapped = left >= 0 && text.charAt(left) == '{' && r < text.length() && text.charAt(r) == '}';
            sb.append(text, fromIndex, idx);
            if (alreadyWrapped) {
                // keep as-is
                sb.append(text, idx, right);
            } else {
                sb.append('{').append(needle).append('}');
            }
            fromIndex = right;
        }
        return sb.toString();
    }

    /**
     * <p>Loads a BibTeX database from a stream.</p>
     * <p>This method does not close the given stream. The caller is
     * responsible for closing it.</p>
     * @param is the input stream to read from
     * @return the BibTeX database
     * @throws ParseException if the database is invalid
     */
    public BibTeXDatabase loadDatabase(InputStream is) throws ParseException {
        Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
        BibTeXParser parser = new BibTeXParser() {
            @Override
            public void checkStringResolution(Key key, BibTeXString string) {
                // ignore
            }
        };
        try {
            return parser.parse(reader);
        } catch (TokenMgrException err) {
            throw new ParseException("Could not parse BibTeX library: " +
                    err.getMessage());
        }
    }

    /**
     * Converts the given database to a map of CSL citation items
     * @param db the database
     * @return a map consisting of citation keys and citation items
     */
    public Map<String, CSLItemData> toItemData(BibTeXDatabase db) {
        Map<String, CSLItemData> result = new LinkedHashMap<>();
        for (Map.Entry<Key, BibTeXEntry> e : db.getEntries().entrySet()) {
            result.put(e.getKey().getValue(), toItemData(e.getValue()));
        }
        return result;
    }

    /**
     * Converts a BibTeX entry to a citation item
     * @param e the BibTeX entry to convert
     * @return the citation item
     */
    public CSLItemData toItemData(BibTeXEntry e) {
        // get all fields from the BibTeX entry
        Map<String, String> entries = new HashMap<>();
        for (Map.Entry<Key, Value> field : e.getFields().entrySet()) {
            String original = field.getValue().toUserString().replaceAll("\\r", "");
            String us = original;

            // convert LaTeX string to normal text
            try {
                List<LaTeXObject> objs = latexParser.parse(new StringReader(us));
                us = latexPrinter.print(objs).replaceAll("\\n", " ").replaceAll("\\r", "").trim();
            } catch (ParseException | TokenMgrException ex) {
                // ignore
            }

            String keyLower = field.getKey().getValue().toLowerCase();
            if (FIELD_AUTHOR.equals(keyLower) || FIELD_EDITOR.equals(keyLower)) {
                us = reAddCurlyBracesInNames(original, us);
            }

            entries.put(keyLower, us);
        }

        // map type
        CSLType type = toType(e.getType());

        CSLItemDataBuilder builder = new CSLItemDataBuilder()
                .id(e.getKey().getValue()).type(type);

        // map address
        if (entries.containsKey(FIELD_LOCATION)) {
            builder.eventPlace(entries.get(FIELD_LOCATION));
            builder.publisherPlace(entries.get(FIELD_LOCATION));
        } else {
            builder.eventPlace(entries.get(FIELD_ADDRESS));
            builder.publisherPlace(entries.get(FIELD_ADDRESS));
        }

        // map author
        if (entries.containsKey(FIELD_AUTHOR)) {
            builder.author(NameParser.parse(entries.get(FIELD_AUTHOR)));
        }

        // map editor
        if (entries.containsKey(FIELD_EDITOR)) {
            builder.editor(NameParser.parse(entries.get(FIELD_EDITOR)));
            builder.collectionEditor(NameParser.parse(entries.get(FIELD_EDITOR)));
        }

        // map date‚
        CSLDate date;
        if (entries.containsKey(FIELD_DATE)) {
            date = DateParser.toDate(entries.get(FIELD_DATE));
        } else {
            date = DateParser.toDate(entries.get(FIELD_YEAR), entries.get(FIELD_MONTH));
        }
        builder.issued(date);
        builder.eventDate(date);

        // 'urldate' is the access date in biblatex as defined in
        // https://ctan.kako-dev.de/macros/latex/contrib/biblatex/doc/biblatex.pdf
        if (entries.containsKey(FIELD_URLDATE)) {
            CSLDate urlDate = DateParser.toDate(entries.get(FIELD_URLDATE));
            builder.accessed(urlDate);
        }

        // map journal/journaltitle, booktitle, series
        if (entries.containsKey(FIELD_JOURNAL)) {
            builder.containerTitle(entries.get(FIELD_JOURNAL));
        } else if (entries.containsKey(FIELD_JOURNALTITLE)) {
            builder.containerTitle(entries.get(FIELD_JOURNALTITLE));
        } else if (entries.containsKey(FIELD_BOOKTITLE)) {
            builder.containerTitle(entries.get(FIELD_BOOKTITLE));
        } else {
            builder.collectionTitle(entries.get(FIELD_SERIES));
        }
        if (entries.containsKey(FIELD_SERIES)) {
            if (entries.containsKey(FIELD_JOURNAL)) {
                builder.containerTitle(entries.get(FIELD_JOURNAL));
                builder.collectionTitle(entries.get(FIELD_SERIES));
            } else if (entries.containsKey(FIELD_JOURNALTITLE)) {
                builder.containerTitle(entries.get(FIELD_JOURNALTITLE));
                builder.collectionTitle(entries.get(FIELD_SERIES));
            } else if (entries.containsKey(FIELD_BOOKTITLE)) {
                builder.containerTitle(entries.get(FIELD_BOOKTITLE));
                builder.collectionTitle(entries.get(FIELD_SERIES));
            }
        }

        // map number and issue
        builder.number(entries.get(FIELD_NUMBER));
        builder.issue(entries.get(FIELD_ISSUE));

        // map publisher, institution, school, organisation
        if (type == CSLType.REPORT) {
            if (entries.containsKey(FIELD_PUBLISHER)) {
                builder.publisher(entries.get(FIELD_PUBLISHER));
            } else if (entries.containsKey(FIELD_INSTITUTION)) {
                builder.publisher(entries.get(FIELD_INSTITUTION));
            } else if (entries.containsKey(FIELD_SCHOOL)) {
                builder.publisher(entries.get(FIELD_SCHOOL));
            } else {
                builder.publisher(entries.get(FIELD_ORGANIZATION));
            }
        } else if (type == CSLType.THESIS) {
            if (entries.containsKey(FIELD_PUBLISHER)) {
                builder.publisher(entries.get(FIELD_PUBLISHER));
            } else if (entries.containsKey(FIELD_SCHOOL)) {
                builder.publisher(entries.get(FIELD_SCHOOL));
            } else if (entries.containsKey(FIELD_INSTITUTION)) {
                builder.publisher(entries.get(FIELD_INSTITUTION));
            } else {
                builder.publisher(entries.get(FIELD_ORGANIZATION));
            }
        } else {
            if (entries.containsKey(FIELD_PUBLISHER)) {
                builder.publisher(entries.get(FIELD_PUBLISHER));
            } else if (entries.containsKey(FIELD_ORGANIZATION)) {
                builder.publisher(entries.get(FIELD_ORGANIZATION));
            } else if (entries.containsKey(FIELD_INSTITUTION)) {
                builder.publisher(entries.get(FIELD_INSTITUTION));
            } else {
                builder.publisher(entries.get(FIELD_SCHOOL));
            }
        }

        // map title or chapter
        if (entries.containsKey(FIELD_TITLE)) {
            builder.title(entries.get(FIELD_TITLE));
        } else {
            builder.title(entries.get(FIELD_CHAPTER));
        }

        // map pages
        String pages = entries.get(FIELD_PAGES);
        if (pages != null) {
            PageRanges ranges = PageParser.parse(pages);
            builder.page(ranges.getLiteral());
            Integer numberOfPages = ranges.getNumberOfPages();
            if (numberOfPages != null) {
                builder.numberOfPages(String.valueOf(numberOfPages));
            }
        }

        // map last accessed date
        if (entries.containsKey(FIELD_ACCESSED)) {
            builder.accessed(DateParser.toDate(entries.get(FIELD_ACCESSED)));
        }

        // map genre as per https://aurimasv.github.io/z2csl/typeMap.xml#map-thesis
        switch (type) {
            case BOOK:
            case MANUSCRIPT:
            case MAP:
            case MOTION_PICTURE:
            case PERSONAL_COMMUNICATION:
            case POST:
            case POST_WEBLOG:
            case REPORT:
            case SPEECH:
            case THESIS:
            case WEBPAGE:
                if (entries.containsKey(FIELD_TYPE)) {
                    builder.genre(entries.get(FIELD_TYPE));
                }
                break;
            default:
                // ignore genre
                break;
        }

        // map language
        if (entries.containsKey(FIELD_LANGUAGE)) {
            builder.language(entries.get(FIELD_LANGUAGE));
        }

        // map other attributes
        builder.volume(entries.get(FIELD_VOLUME));
        builder.keyword(entries.get(FIELD_KEYWORDS));
        builder.URL(entries.get(FIELD_URL));
        builder.status(entries.get(FIELD_STATUS));
        builder.ISSN(entries.get(FIELD_ISSN));
        builder.ISBN(entries.get(FIELD_ISBN));
        builder.version(entries.get(FIELD_REVISION));
        builder.annote(entries.get(FIELD_ANNOTE));
        builder.edition(entries.get(FIELD_EDITION));
        builder.abstrct(entries.get(FIELD_ABSTRACT));
        builder.DOI(entries.get(FIELD_DOI));
        builder.note(entries.get(FIELD_NOTE));

        // create citation item
        return builder.build();
    }

    /**
     * Converts a BibTeX type to a CSL type
     * @param type the type to convert
     * @return the converted type (never null, falls back to {@link CSLType#ARTICLE})
     */
    public CSLType toType(Key type) {
        String s = type.getValue();
        if (s.equalsIgnoreCase(TYPE_ARTICLE)) {
            return CSLType.ARTICLE_JOURNAL;
        } else if (s.equalsIgnoreCase(TYPE_PROCEEDINGS)) {
            return CSLType.BOOK;
        } else if (s.equalsIgnoreCase(TYPE_MANUAL)) {
            return CSLType.BOOK;
        } else if (s.equalsIgnoreCase(TYPE_BOOK)) {
            return CSLType.BOOK;
        } else if (s.equalsIgnoreCase(TYPE_PERIODICAL)) {
            return CSLType.BOOK;
        } else if (s.equalsIgnoreCase(TYPE_BOOKLET)) {
            return CSLType.PAMPHLET;
        } else if (s.equalsIgnoreCase(TYPE_INBOOK)) {
            return CSLType.CHAPTER;
        } else if (s.equalsIgnoreCase(TYPE_INCOLLECTION)) {
            return CSLType.CHAPTER;
        } else if (s.equalsIgnoreCase(TYPE_INPROCEEDINGS)) {
            return CSLType.PAPER_CONFERENCE;
        } else if (s.equalsIgnoreCase(TYPE_CONFERENCE)) {
            return CSLType.PAPER_CONFERENCE;
        } else if (s.equalsIgnoreCase(TYPE_MASTERSTHESIS)) {
            return CSLType.THESIS;
        } else if (s.equalsIgnoreCase(TYPE_PHDTHESIS)) {
            return CSLType.THESIS;
        } else if (s.equalsIgnoreCase(TYPE_TECHREPORT)) {
            return CSLType.REPORT;
        } else if (s.equalsIgnoreCase(TYPE_PATENT)) {
            return CSLType.PATENT;
        } else if (s.equalsIgnoreCase(TYPE_ELECTRONIC)) {
            return CSLType.WEBPAGE;
        } else if (s.equalsIgnoreCase(TYPE_ONLINE)) {
            return CSLType.WEBPAGE;
        } else if (s.equalsIgnoreCase(TYPE_WWW)) {
            return CSLType.WEBPAGE;
        } else if (s.equalsIgnoreCase(TYPE_STANDARD)) {
            return CSLType.LEGISLATION;
        } else if (s.equalsIgnoreCase(TYPE_UNPUBLISHED)) {
            return CSLType.MANUSCRIPT;
        }
        return CSLType.ARTICLE;
    }
}

package de.undercouch.citeproc;

import de.undercouch.citeproc.csl.CSLAbbreviationList;
import de.undercouch.citeproc.csl.CSLCitation;
import de.undercouch.citeproc.csl.CSLCitationItem;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CitationIDIndexPair;
import de.undercouch.citeproc.helper.CSLUtils;
import de.undercouch.citeproc.helper.json.JsonLexer;
import de.undercouch.citeproc.helper.json.JsonParser;
import de.undercouch.citeproc.output.Bibliography;
import de.undercouch.citeproc.output.Citation;
import de.undercouch.citeproc.script.ScriptRunner;
import de.undercouch.citeproc.script.ScriptRunnerException;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * Runs all tests from the CSL test suite (<a href="https://github.com/citation-style-language/test-suite">https://github.com/citation-style-language/test-suite</a>)
 * @author Michel Kraemer
 */
@RunWith(Parameterized.class)
public class TestSuiteTest {
    private static final String HUMANS_DIR = "/test-suite/processor-tests/humans";
    private static final String elementRegex = "(?sm)^(.*>>=[^\\n]*%1$s[^\\n]+)(.*)(\\n<<=.*%1$s.*)";

    /**
     * The current test to run
     */
    private File testFile;

    /**
     * Get all the human-readable test files
     */
    @Parameters(name = "{0}")
    public static Iterable<String> data() {
        URL humansUrl = CSL.class.getResource(HUMANS_DIR);
        File humansDir = new File(humansUrl.getPath());

        // noinspection ConstantConditions
        return Arrays.stream(humansDir.listFiles((dir, name) -> name.endsWith(".txt")))
                .map(File::getName)
                .collect(Collectors.toList());
    }

    /**
     * Create a new test
     * @param name the name of the test file
     */
    public TestSuiteTest(String name) {
        URL humansUrl = CSL.class.getResource(HUMANS_DIR);
        File humansDir = new File(humansUrl.getPath());
        testFile = new File(humansDir, name);
    }

    /**
     * Run a test from the test suite
     * @throws IOException if an I/O error occurred
     */
    @Test
    @SuppressWarnings("unchecked")
    public void run() throws IOException {
        // load file
        Map<String, Object> conf = readFile(testFile);

        // get configuration
        String mode = (String)conf.get("mode");
        String result = (String)conf.get("result");
        String style = (String)conf.get("csl");
        Collection<Map<String, Object>> input =
                (Collection<Map<String, Object>>)conf.get("input");
        Collection<List<Object>> rawCitations =
                (Collection<List<Object>>)conf.get("citations");
        Collection<List<Map<String, Object>>> rawCitationItems =
                (Collection<List<Map<String, Object>>>)conf.get("citation_items");
        Map<String, Map<String, Object>> abbreviations =
                (Map<String, Map<String, Object>>)conf.get("abbreviations");
        Collection<List<String>> bibentries =
                (Collection<List<String>>)conf.get("bibentries");
        Map<String, Collection<Map<String, Object>>> rawBibsection =
                (Map<String, Collection<Map<String, Object>>>)conf.get("bibsection");

        if (rawCitationItems != null && rawCitations != null) {
            // Only one of the two can be tested. Prefer rawCitations.
            rawCitationItems = null;
        }

        // parse mode
        String[] modes = mode.split("-");
        mode = modes[0];
        Set<String> submodes = new HashSet<>(Arrays.asList(modes).subList(1, modes.length));

        // convert item data
        int i = 0;
        CSLItemData[] items = new CSLItemData[input.size()];
        for (Map<String, Object> m : input) {
            items[i++] = CSLItemData.fromJson(m);
        }

        // convert citations
        List<List<Object>> citations = null;
        if (rawCitations != null) {
            citations = new ArrayList<>();
            for (List<Object> m : rawCitations) {
                List<Object> cits = new ArrayList<>();
                cits.add(CSLCitation.fromJson((Map<String, Object>)m.get(0)));

                Collection<List<Object>> coll1 = (Collection<List<Object>>)m.get(1);
                Collection<List<Object>> coll2 = (Collection<List<Object>>)m.get(2);
                List<CitationIDIndexPair> citsPre = new ArrayList<>();
                List<CitationIDIndexPair> citsPost = new ArrayList<>();
                for (List<Object> c1m : coll1) {
                    citsPre.add(CitationIDIndexPair.fromJson(c1m));
                }
                for (List<Object> c2m : coll2) {
                    citsPost.add(CitationIDIndexPair.fromJson(c2m));
                }

                cits.add(citsPre);
                cits.add(citsPost);
                citations.add(cits);
            }
        }

        // convert citation items
        List<List<CSLCitationItem>> citationItems = null;
        if (rawCitationItems != null) {
            citationItems = new ArrayList<>();
            for (List<Map<String, Object>> l : rawCitationItems) {
                List<CSLCitationItem> cits = new ArrayList<>();
                for (Map<String, Object> m : l) {
                    cits.add(CSLCitationItem.fromJson(m));
                }
                citationItems.add(cits);
            }
        }

        // convert abbreviations
        DefaultAbbreviationProvider abbreviationProvider = new DefaultAbbreviationProvider();
        if (abbreviations != null) {
            for (Map.Entry<String, Map<String, Object>> e : abbreviations.entrySet()) {
                CSLAbbreviationList al = CSLAbbreviationList.fromJson(e.getValue());
                abbreviationProvider.add(e.getKey(), al);
            }
        }

        // convert the 'bibsection' configuration
        SelectionMode bibSectionMode = null;
        CSLItemData[] bibSection = null;
        CSLItemData[] bibSectionQuash = null;
        if (rawBibsection != null) {
            for (Map.Entry<String, Collection<Map<String, Object>>> e : rawBibsection.entrySet()) {
                CSLItemData[] r = convertBibSection(e.getValue());
                if (e.getKey().equals("quash")) {
                    bibSectionQuash = r;
                } else {
                    bibSection = r;
                    bibSectionMode = SelectionMode.fromString(e.getKey());
                }
            }
        }

        // create CSL processor
        ListItemDataProvider itemDataProvider = new ListItemDataProvider(items);
        TestSuiteCSL citeproc = new TestSuiteCSL(itemDataProvider, abbreviationProvider, style);

        // set output format
        if (submodes.contains("rtf")) {
            citeproc.setOutputFormat("rtf");
        }

        // set development options
        Map<String, Object> options = (Map<String, Object>)conf.get("options");
        if (options != null) {
            for (Map.Entry<String, Object> e : options.entrySet()) {
                if (e.getKey().equals("variableWrapper")) {
                    continue;
                }
                citeproc.setDevelopmentExtension(e.getKey(), e.getValue());
            }
        }

        // register citation items
        boolean nosort = submodes.contains("nosort");
        if (bibentries != null) {
            for (List<String> be : bibentries) {
                citeproc.registerCitationItems(be.toArray(new String[0]), nosort);
            }
        } else if (citations == null) {
            citeproc.registerCitationItems(itemDataProvider.getIds(), nosort);
        }

        // set default citation items
        if (citations == null && citationItems == null) {
            citationItems = new ArrayList<>();
            citationItems.add(citeproc.getRegistryReflist());
        }

        // make citations
        StringBuilder citationResult = new StringBuilder();
        if (citationItems != null) {
            for (List<CSLCitationItem> cits : citationItems) {
                if (citationResult.length() > 0) {
                    citationResult.append("\n");
                }
                citationResult.append(citeproc.makeCitationCluster(
                        cits.toArray(new CSLCitationItem[0])));
            }
        } else if (!citations.isEmpty()) {
            List<List<Object>> slice = citations.subList(0, citations.size() - 1);
            for (List<Object> cit : slice) {
                citeproc.makeCitation((CSLCitation)cit.get(0),
                        (List<CitationIDIndexPair>)cit.get(1),
                        (List<CitationIDIndexPair>)cit.get(2));
            }

            List<Object> citation = citations.get(citations.size() - 1);
            List<Citation> r = citeproc.makeCitation((CSLCitation)citation.get(0),
                    (List<CitationIDIndexPair>)citation.get(1),
                    (List<CitationIDIndexPair>)citation.get(2));

            Map<Integer, Integer> indexMap = new HashMap<>();
            int pos = 0;
            for (Citation c : r) {
                indexMap.put(c.getIndex(), pos);
                ++pos;
            }

            List<String> resultCitations = new ArrayList<>();
            for (int cpos = 0; cpos < citeproc.getCitationsByIndex().size(); ++cpos) {
                if (indexMap.containsKey(cpos)) {
                    resultCitations.add(">>[" + cpos + "] " + r.get(indexMap.get(cpos)).getText());
                } else {
                    resultCitations.add("..[" + cpos + "] " + citeproc.callProcessCitationCluster(cpos));
                }
            }
            citationResult = new StringBuilder(StringUtils.join(resultCitations, "\n"));
        }

        // make bibliography
        if (mode.equals("bibliography") && !submodes.contains("header")) {
            if (bibSection != null || bibSectionQuash != null) {
                citationResult = new StringBuilder(citeproc.makeBibliography(bibSectionMode,
                        bibSection, bibSectionQuash).makeString());
            } else {
                citationResult = new StringBuilder(citeproc.makeBibliography().makeString());
            }
        } else if (submodes.contains("header")) {
            Bibliography p = citeproc.makeBibliography();
            citationResult = new StringBuilder();
            citationResult.append("bibend: ").append(p.getBibEnd()).append("\n");
            citationResult.append("bibliography_errors: \n"); // TODO not implemented yet
            citationResult.append("bibstart: ").append(p.getBibStart()).append("\n");
            citationResult.append("done: ").append(p.getDone()).append("\n");
            citationResult.append("entry_ids: ").append(StringUtils.join(p.getEntryIds(), ",")).append("\n");
            citationResult.append("entryspacing: ").append(p.getEntrySpacing()).append("\n");
            citationResult.append("linespacing: ").append(p.getLineSpacing()).append("\n");
            citationResult.append("maxoffset: ").append(p.getMaxOffset()).append("\n");
            citationResult.append("second-field-align: ").append(p.getSecondFieldAlign());
        }

        // compare result
        assertEquals(result, citationResult.toString());
    }

    /**
     * Reads a test file
     * @param f the test file
     * @return the configuration read from the file
     * @throws IOException if the file could not be read
     */
    private static Map<String, Object> readFile(File f) throws IOException {
        String text;
        try (FileInputStream is = new FileInputStream(f)) {
            text = CSLUtils.readStreamToString(is, "UTF-8");
        }

        Map<String, Object> result = new HashMap<>();
        extract(text, "MODE", true, false, false, result);
        extract(text, "CSL", true, false, false, result);
        extract(text, "RESULT", true, false, false, result);
        extract(text, "INPUT", true, true, false, result);
        extract(text, "CITATION-ITEMS", false, true, false, result);
        extract(text, "CITATIONS", false, true, false, result);
        extract(text, "BIBENTRIES", false, true, false, result);
        extract(text, "BIBSECTION", false, true, false, result);
        extract(text, "ABBREVIATIONS", false, false, true, result);

        return result;
    }

    /**
     * Extract an element from a human-readable test file
     * @param text the test file contents
     * @param tag the name of the element to extract
     * @param required {@code true} if the element is required
     * @param jsonArray {@code true} if the element contains a JSON array
     * @param jsonObject {@code true} if the element contains a JSON object
     * @param result the parsed element
     * @throws IOException if the element could not be parsed
     */
    private static void extract(String text, String tag, boolean required,
            boolean jsonArray, boolean jsonObject, Map<String, Object> result) throws IOException {
        Pattern pattern = Pattern.compile(String.format(elementRegex, tag));
        Matcher matcher = pattern.matcher(text);

        if (matcher.matches()) {
            String s = matcher.group(2).trim();
            Object v = s;

            if (jsonArray) {
                JsonLexer jsonLexer = new JsonLexer(new StringReader(s));
                JsonParser jsonParser = new JsonParser(jsonLexer);
                v = jsonParser.parseArray();
            } else if (jsonObject) {
                JsonLexer jsonLexer = new JsonLexer(new StringReader(s));
                JsonParser jsonParser = new JsonParser(jsonLexer);
                v = jsonParser.parseObject();
            }

            String name = tag.toLowerCase().replace('-', '_');
            result.put(name, v);
        } else if (required) {
            throw new IllegalStateException("Missing required element: " + tag);
        }
    }

    /**
     * Convert a 'bibsection' configuration to a list of item data objects
     * @param bs the configuration
     * @return the item data objects
     */
    private static CSLItemData[] convertBibSection(Collection<Map<String, Object>> bs) {
        CSLItemData[] r = new CSLItemData[bs.size()];
        int i = 0;
        for (Map<String, Object> s : bs) {
            String f = (String)s.get("field");
            Object v = s.get("value");
            if (f.equals("issued") && ((String)v).isEmpty()) {
                v = new HashMap<String, Object>();
            } else if (f.equals("categories")) {
                v = Collections.singletonList(v);
            }
            Map<String, Object> m = new HashMap<>();
            m.put(f, v);
            r[i++] = CSLItemData.fromJson(m);
        }
        return r;
    }

    /**
     * A special citation processor that allows access to the internal API
     */
    private static class TestSuiteCSL extends CSL {
        private static class TestSuiteLocaleProvider extends DefaultLocaleProvider {
            @Override
            public String retrieveLocale(String lang) {
                try {
                    return super.retrieveLocale(lang);
                } catch (IllegalArgumentException e) {
                    // fall back to empty locale definition for invalid lang tags
                    return "[]";
                }
            }
        }

        public TestSuiteCSL(ItemDataProvider itemDataProvider,
                AbbreviationProvider abbreviationProvider, String style)
                throws IOException {
            super(itemDataProvider, new TestSuiteLocaleProvider(),
                    abbreviationProvider, style, "en-US", false);

            ScriptRunner sr = getScriptRunner();
            try {
                sr.eval(new StringReader(
                        "function __getCitationByIndex(engine) { "
                                + "return engine.registry.citationreg.citationByIndex; }"

                                + "function __callProcessCitationCluster(engine, cpos) { "
                                + "return engine.process_CitationCluster("
                                + "engine.registry.citationreg.citationByIndex[cpos].sortedItems); }"

                                + "function __getRefList(engine) {"
                                + "return engine.registry.reflist; }"

                                + "function __setDevelopmentExtension(engine, key, value) {"
                                + "engine.opt.development_extensions[key] = value; }"
                ));
            } catch (ScriptRunnerException e) {
                throw new IOException("Could not evaluate inline scripts", e);
            }
        }

        public List<CSLCitation> getCitationsByIndex() {
            List<?> r;
            try {
                r = getScriptRunner().callMethod("__getCitationByIndex",
                        List.class, getEngine());
            } catch (ScriptRunnerException e) {
                throw new IllegalArgumentException("Could not get registered citations", e);
            }

            List<CSLCitation> result = new ArrayList<>();
            for (Object o : r) {
                @SuppressWarnings("unchecked")
                Map<String, Object> m = (Map<String, Object>)o;
                result.add(CSLCitation.fromJson(m));
            }
            return result;
        }

        public String callProcessCitationCluster(int cpos) {
            try {
                return getScriptRunner().callMethod("__callProcessCitationCluster",
                        String.class, getEngine(), cpos);
            } catch (ScriptRunnerException e) {
                throw new IllegalArgumentException("Could not get registered citations", e);
            }
        }

        public List<CSLCitationItem> getRegistryReflist() {
            List<?> r;
            try {
                r = getScriptRunner().callMethod("__getRefList", List.class, getEngine());
            } catch (ScriptRunnerException e) {
                throw new IllegalArgumentException("Could not get registered citation items", e);
            }

            List<CSLCitationItem> result = new ArrayList<>();
            for (Object o : r) {
                @SuppressWarnings("unchecked")
                Map<String, Object> m = (Map<String, Object>)o;
                result.add(CSLCitationItem.fromJson(m));
            }
            return result;
        }

        public String makeCitationCluster(CSLCitationItem... citation) {
            try {
                return getScriptRunner().callMethod(getEngine(),
                        "makeCitationCluster", String.class, (Object)citation);
            } catch (ScriptRunnerException e) {
                throw new IllegalArgumentException("Could not make citation custer", e);
            }
        }

        public void setDevelopmentExtension(String key, Object value) {
            try {
                getScriptRunner().callMethod("__setDevelopmentExtension", getEngine(), key, value);
            } catch (ScriptRunnerException e) {
                throw new IllegalArgumentException("Could not set development extension", e);
            }
        }
    }
}

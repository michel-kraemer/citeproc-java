package de.undercouch.citeproc;

import de.undercouch.citeproc.bibtex.BibTeXConverter;
import de.undercouch.citeproc.bibtex.BibTeXItemDataProvider;
import de.undercouch.citeproc.csl.CSLCitation;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.helper.json.JsonLexer;
import de.undercouch.citeproc.helper.json.JsonParser;
import de.undercouch.citeproc.output.Bibliography;
import de.undercouch.citeproc.output.Citation;
import org.apache.commons.io.FileUtils;
import org.jbibtex.BibTeXDatabase;
import org.jbibtex.ParseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class FixturesTest {
    private static final String FIXTURES_DIR = "/fixtures";
    private static final String TEST_SUITE_DIR = "/test-suite/processor-tests/humans";
    private static final String TEST_SUITE_OVERRIDES_DIR = "/test-suite-overrides";
    private static final Map<String, ItemDataProvider> bibliographyFileCache = new HashMap<>();

    /**
     * {@code true} if the test should be run in experimental mode
     */
    private final boolean experimentalMode;

    /**
     * The output format to generate
     */
    private final String outputFormat;

    /**
     * The expected rendered result
     */
    private final String expectedResult;

    /**
     * The test data
     */
    private final Map<String, Object> data;

    /**
     * Get a map of expected results from test fixture data
     * @param data the data
     * @param propertyName the name of the property holding the expected results
     * @return the expected results
     */
    @SuppressWarnings("unchecked")
    private static Map<String, String> readExpectedResults(Map<String, Object> data,
            String propertyName) {
        Object expectedResultObj = data.get(propertyName);
        if (expectedResultObj instanceof String) {
            String str = (String)expectedResultObj;
            Map<String, String> map = new HashMap<>();
            map.put("text", str);
            expectedResultObj = map;
        }
        return (Map<String, String>)expectedResultObj;
    }

    /**
     * Read a file from the CSL test suite and convert it to the same format
     * as our test fixtures
     * @param f the file to read
     * @return the parsed data object
     * @throws IOException if the file could not be read
     */
    private static Map<String, Object> cslTestSuiteFileToData(File f)
            throws IOException {
        Map<String, Object> result = new HashMap<>();
        Pattern startPattern = Pattern.compile("^\\s*>>=+\\s*(.*?)\\s*=+>>\\s*$");
        Pattern endPattern = Pattern.compile("^\\s*<<=+\\s*(.*?)\\s*=+<<\\s*$");
        String currentKey = null;
        StringBuilder currentValue = null;
        try (BufferedReader br = Files.newBufferedReader(f.toPath())) {
            String line;
            while ((line = br.readLine()) != null) {
                if (currentKey == null) {
                    Matcher m = startPattern.matcher(line);
                    if (m.matches()) {
                        currentKey = m.group(1);
                        currentValue = new StringBuilder();
                    }
                } else {
                    Matcher m = endPattern.matcher(line);
                    if (m.matches()) {
                        String value = currentValue.toString().trim();
                        switch (currentKey.toLowerCase()) {
                            case "mode":
                                result.put("mode", value);
                                break;

                            case "result":
                                result.put("result", value);
                                break;

                            case "csl":
                                result.put("style", value);
                                break;

                            case "input": {
                                JsonParser parser = new JsonParser(
                                        new JsonLexer(new StringReader(value)));
                                List<Object> items = parser.parseArray();
                                result.put("items", items);
                                break;
                            }

                            case "citation-items": {
                                JsonParser parser = new JsonParser(
                                        new JsonLexer(new StringReader(value)));
                                List<Object> citationItems = parser.parseArray();
                                List<Map<String, Object>> citations = new ArrayList<>();
                                for (Object citationItem : citationItems) {
                                    Map<String, Object> citation = new HashMap<>();
                                    citation.put("citationItems", citationItem);
                                    citations.add(citation);
                                }
                                result.put("citations", citations);
                                break;
                            }
                        }
                        currentKey = null;
                    } else {
                        currentValue.append(line).append("\n");
                    }
                }
            }
        }
        return result;
    }

    /**
     * Get all test files
     */
    @Parameterized.Parameters(name = "{0}, {1}, {2}")
    @SuppressWarnings("unchecked")
    public static Iterable<Object[]> data() {
        URL fixturesUrl = CSL.class.getResource(FIXTURES_DIR);
        URL testSuiteUrl = CSL.class.getResource(TEST_SUITE_DIR);
        URL testSuiteOverridesUrl = CSL.class.getResource(TEST_SUITE_OVERRIDES_DIR);
        File fixturesDir = new File(fixturesUrl.getPath());
        File testSuiteDir = new File(testSuiteUrl.getPath());
        File testSuiteOverridesDir = new File(testSuiteOverridesUrl.getPath());

        // read test fixtures
        Stream<Map<String, Object>> fixturesStream = FileUtils.listFiles(fixturesDir, new String[]{"yaml"}, true)
                .stream()
                .map(f -> {
                    Map<String, Object> data;
                    Yaml yaml = new Yaml();
                    try (FileInputStream is = new FileInputStream(f)) {
                        data = yaml.loadAs(is, Map.class);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    data.put("__name", f.getName().substring(0, f.getName().length() - 5));
                    return data;
                });

        // read fixtures from CSL test suite
        List<String> testSuiteFiles = new ArrayList<>();
        testSuiteFiles.add("affix_CommaAfterQuote");
        testSuiteFiles.add("affix_InterveningEmpty");
        testSuiteFiles.add("affix_MovingPunctuation");
        testSuiteFiles.add("affix_PrefixFullCitationTextOnly");
        // testSuiteFiles.add("affix_PrefixWithDecorations");
        testSuiteFiles.add("affix_SpaceWithQuotes");
        testSuiteFiles.add("affix_TextNodeWithMacro");
        // testSuiteFiles.add("affix_WithCommas");
        testSuiteFiles.add("affix_WordProcessorAffixNoSpace");

        Stream<Map<String, Object>> testSuiteStream = testSuiteFiles
                .stream()
                .map(name -> {
                    // read test suite file
                    Map<String, Object> data;
                    try {
                        data = cslTestSuiteFileToData(new File(testSuiteDir, name + ".txt"));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    // override attributes if there is an override file
                    File overridesFiles = new File(testSuiteOverridesDir, name + ".yaml");
                    if (overridesFiles.exists()) {
                        Map<String, Object> overrides;
                        Yaml yaml = new Yaml();
                        try (FileInputStream is = new FileInputStream(overridesFiles)) {
                            overrides = yaml.loadAs(is, Map.class);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        // rename "result" to "resultLegacy" and handle HTML output
                        Object overridesResult = overrides.get("result");
                        if (overridesResult != null) {
                            data.put("resultLegacy", data.get("result"));
                            if (overridesResult instanceof Map) {
                                Map<String, Object> overridesResultMap =
                                        (Map<String, Object>)overridesResult;
                                if (overridesResultMap.get("html") != null) {
                                    Map<String, Object> resultLegacyMap =
                                            new HashMap<>();
                                    resultLegacyMap.put("html", data.get("resultLegacy"));
                                    data.put("resultLegacy", resultLegacyMap);
                                }
                            }
                        }
                        data.putAll(overrides);
                    }

                    data.put("__name", name);
                    return data;
                });

        // convert test fixtures to parameters
        Stream<Map<String, Object>> dataStream = Stream.concat(testSuiteStream, fixturesStream);

        return dataStream.flatMap(data -> {
            Map<String, String> expectedResults = readExpectedResults(data, "result");
            Map<String, String> expectedResultsLegacy;
            if (data.containsKey("resultLegacy")) {
                expectedResultsLegacy = readExpectedResults(data, "resultLegacy");
            } else {
                expectedResultsLegacy = expectedResults;
            }

            String strExperimentalMode = (String)data.get("experimentalMode");
            boolean experimentalOnly = "only".equals(strExperimentalMode);

            Stream<Boolean> s;
            if (experimentalOnly) {
                s = Stream.of(true);
            } else {
                s = Stream.of(true, false);
            }

            return s.flatMap(experimentalMode -> {
                    Map<String, String> er = expectedResults;
                    if (!experimentalMode) {
                        er = expectedResultsLegacy;
                    }
                    return er.entrySet().stream().map(expectedResult ->
                            new Object[] {
                                    data.get("__name"),
                                    experimentalMode,
                                    expectedResult.getKey(),
                                    expectedResult.getValue(),
                                    data
                            }
                    );
            });
        }).collect(Collectors.toList());
    }

    /**
     * Create a new test
     * @param name the name of the test file
     * @param experimentalMode {@code true} if the test should be run in
     * experimental mode
     * @param outputFormat the output format to generate
     * @param expectedResult the expected rendered result
     * @param data the test data
     */
    public FixturesTest(@SuppressWarnings("unused") String name, boolean experimentalMode,
            String outputFormat, String expectedResult, Map<String, Object> data) {
        this.experimentalMode = experimentalMode;
        this.outputFormat = outputFormat;
        this.expectedResult = expectedResult;
        this.data = data;
    }

    private static ItemDataProvider loadBibliographyFile(String filename) throws IOException {
        ItemDataProvider result = bibliographyFileCache.get(filename);
        if (result == null) {
            BibTeXDatabase db;
            try (InputStream is = FixturesTest.class.getResourceAsStream(filename);
                 BufferedInputStream bis = new BufferedInputStream(is)) {
                InputStream tis = bis;
                if (filename.endsWith(".gz")) {
                    tis = new GZIPInputStream(bis);
                }
                db = new BibTeXConverter().loadDatabase(tis);
            } catch (ParseException e) {
                throw new IOException(e);
            }

            BibTeXItemDataProvider r = new BibTeXItemDataProvider();
            r.addDatabase(db);
            result = r;
            bibliographyFileCache.put(filename, result);
        }
        return result;
    }

    /**
     * Run a test from the test suite
     * @throws IOException if an I/O error occurred
     */
    @Test
    @SuppressWarnings("unchecked")
    public void run() throws IOException {
        String mode = (String)data.get("mode");
        String style = (String)data.get("style");

        // get bibliography file
        ItemDataProvider itemDataProvider = null;
        String bibliographyFile = (String)data.get("bibliographyFile");
        if (bibliographyFile != null) {
            itemDataProvider = loadBibliographyFile(bibliographyFile);
        }

        // get item data
        List<Map<String, Object>> rawItems = (List<Map<String, Object>>)data.get("items");
        if (rawItems != null && bibliographyFile != null) {
            throw new IllegalStateException("Found both `bibliographyFile' " +
                    "and `items'. Define only one of them.");
        }

        // convert item data
        if (rawItems != null) {
            CSLItemData[] items = new CSLItemData[rawItems.size()];
            for (int i = 0; i < items.length; ++i) {
                items[i] = CSLItemData.fromJson(rawItems.get(i));
            }
            itemDataProvider = new ListItemDataProvider(items);
        }

        if (itemDataProvider == null) {
            throw new IllegalStateException("Either `bibliographyFile' or " +
                    "`items' must be specified.");
        }

        // get the item IDs to test against
        String[][] itemIds;
        List<Object> itemIdsListObj = (List<Object>)data.get("itemIds");
        if (itemIdsListObj != null && !itemIdsListObj.isEmpty() &&
                itemIdsListObj.get(0) instanceof String) {
            itemIds = new String[1][];
            itemIds[0] = new String[itemIdsListObj.size()];
            for (int i = 0; i < itemIdsListObj.size(); i++) {
                Object o = itemIdsListObj.get(i);
                itemIds[0][i] = (String)o;
            }
        } else if (itemIdsListObj != null) {
            itemIds = new String[itemIdsListObj.size()][];
            for (int i = 0; i < itemIdsListObj.size(); i++) {
                List<String> l = (List<String>)itemIdsListObj.get(i);
                itemIds[i] = l.toArray(new String[0]);
            }
        } else {
            itemIds = new String[1][];
            itemIds[0] = itemDataProvider.getIds();
        }

        // get the raw citations
        List<Map<String, Object>> rawCitations = (List<Map<String, Object>>)data.get("citations");
        if (rawCitations != null && !"citation".equals(mode)) {
            throw new IllegalStateException("`citations' can only be defined " +
                    "if `mode' equals `citation'.");
        }
        if (rawCitations != null && itemIdsListObj != null) {
            throw new IllegalStateException("Found both `itemIds' and " +
                    "`citations'. Define only one of them.");
        }

        // converts citations
        List<CSLCitation> citations = null;
        if (rawCitations != null) {
            citations = new ArrayList<>();
            for (Map<String, Object> raw : rawCitations) {
                citations.add(CSLCitation.fromJson(raw));
            }
        }

        // create CSL processor
        CSL citeproc = new CSL(itemDataProvider, style, experimentalMode);
        citeproc.setOutputFormat(outputFormat);
        citeproc.setConvertLinks(true);

        // register citation items
        for (String[] ii : itemIds) {
            citeproc.registerCitationItems(ii);
        }

        String actualResult;
        if ("bibliography".equals(mode)) {
            Bibliography bibl = citeproc.makeBibliography();
            actualResult = bibl.makeString();
        } else if ("citation".equals(mode)) {
            List<Citation> generatedCitations = new ArrayList<>();
            if (citations != null) {
                for (CSLCitation c : citations) {
                    generatedCitations.addAll(citeproc.makeCitation(c));
                }
            } else {
                String[] ii = citeproc.getRegisteredItems().stream()
                        .map(CSLItemData::getId)
                        .toArray(String[]::new);
                generatedCitations.addAll(citeproc.makeCitation(ii));
            }
            actualResult = generatedCitations.stream()
                    .map(Citation::getText)
                    .collect(Collectors.joining("\n"));
        } else {
            throw new IllegalStateException("Unknown mode: " + mode);
        }

        // compare result
        assertEquals(expectedResult, actualResult);
    }
}

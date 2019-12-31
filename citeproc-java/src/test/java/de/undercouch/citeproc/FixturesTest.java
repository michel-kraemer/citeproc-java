package de.undercouch.citeproc;

import de.undercouch.citeproc.bibtex.BibTeXConverter;
import de.undercouch.citeproc.bibtex.BibTeXItemDataProvider;
import de.undercouch.citeproc.csl.CSLCitation;
import de.undercouch.citeproc.csl.CSLItemData;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class FixturesTest {
    private static final String FIXTURES_DIR = "/fixtures";
    private static final Map<String, ItemDataProvider> bibliographyFileCache = new HashMap<>();

    /**
     * {@code true} if the test should be run in experimental mode
     */
    private boolean experimentalMode;

    /**
     * The output format to generate
     */
    private String outputFormat;

    /**
     * The expected rendered result
     */
    private String expectedResult;

    /**
     * The test data
     */
    private Map<String, Object> data;

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
     * Get all test files
     */
    @Parameterized.Parameters(name = "{0}, {1}, {2}")
    @SuppressWarnings("unchecked")
    public static Iterable<Object[]> data() {
        URL fixturesUrl = CSL.class.getResource(FIXTURES_DIR);
        File fixturesDir = new File(fixturesUrl.getPath());

        return FileUtils.listFiles(fixturesDir, new String[] {"yaml"}, true).stream()
                .flatMap(f -> {
                    Map<String, Object> data;
                    Yaml yaml = new Yaml();
                    try (FileInputStream is = new FileInputStream(f)) {
                        data = yaml.loadAs(is, Map.class);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

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
                                            f.getName().substring(0, f.getName().length() - 5),
                                            experimentalMode,
                                            expectedResult.getKey(),
                                            expectedResult.getValue(),
                                            data
                                    }
                            );
                    });
                })
                .collect(Collectors.toList());
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
        List<String> itemIdsList = (List<String>)data.get("itemIds");
        String[] itemIds;
        if (itemIdsList == null) {
            itemIds = itemDataProvider.getIds();
        } else {
            itemIds = itemIdsList.toArray(new String[0]);
        }

        // get the raw citations
        List<Map<String, Object>> rawCitations = (List<Map<String, Object>>)data.get("citations");
        if (rawCitations != null && !"citation".equals(mode)) {
            throw new IllegalStateException("`citations' can only be defined " +
                    "if `mode' equals `citation'.");
        }
        if (rawCitations != null && itemIdsList != null) {
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
        citeproc.registerCitationItems(itemIds);

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
                generatedCitations.addAll(citeproc.makeCitation(itemIds));
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

package de.undercouch.citeproc;

import de.undercouch.citeproc.bibtex.BibTeXConverter;
import de.undercouch.citeproc.bibtex.BibTeXItemDataProvider;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.output.Bibliography;
import de.undercouch.citeproc.output.Citation;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeFalse;

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
     * Get all test files
     */
    @Parameterized.Parameters(name = "{0}, {1}, {2}")
    @SuppressWarnings("unchecked")
    public static Iterable<Object[]> data() {
        URL fixturesUrl = CSL.class.getResource(FIXTURES_DIR);
        File fixturesDir = new File(fixturesUrl.getPath());

        // noinspection ConstantConditions
        return Arrays.stream(fixturesDir.listFiles((dir, name) -> name.endsWith(".yaml")))
                .flatMap(f -> {
                    Map<String, Object> data;
                    Yaml yaml = new Yaml();
                    try (FileInputStream is = new FileInputStream(f)) {
                        data = yaml.loadAs(is, Map.class);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    Object expectedResultObj = data.get("result");
                    if (expectedResultObj instanceof String) {
                        String str = (String)expectedResultObj;
                        Map<String, String> map = new HashMap<>();
                        map.put("text", str);
                        expectedResultObj = map;
                    }
                    Map<String, String> expectedResults = (Map<String, String>)expectedResultObj;

                    return Stream.of(true, false).flatMap(experimentalMode ->
                            expectedResults.entrySet().stream().map(expectedResult ->
                                    new Object[] {
                                            f.getName().substring(0, f.getName().length() - 5),
                                            experimentalMode,
                                            expectedResult.getKey(),
                                            expectedResult.getValue(),
                                            data
                                    }
                            )
                    );
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

        String experimentalModeEnabled = (String)data.get("experimentalMode");
        assumeFalse("only".equals(experimentalModeEnabled) && !experimentalMode);

        ItemDataProvider itemDataProvider;
        String bibliographyFile = (String)data.get("bibliographyFile");
        if (bibliographyFile != null) {
            itemDataProvider = loadBibliographyFile(bibliographyFile);
        } else {
            // convert item data
            List<Map<String, Object>> rawItems = (List<Map<String, Object>>)data.get("items");
            CSLItemData[] items = new CSLItemData[rawItems.size()];
            for (int i = 0; i < items.length; ++i) {
                items[i] = CSLItemData.fromJson(rawItems.get(i));
            }
            itemDataProvider = new ListItemDataProvider(items);
        }

        List<String> itemIdsList = (List<String>)data.get("itemIds");
        String[] itemIds;
        if (itemIdsList == null) {
            itemIds = itemDataProvider.getIds();
        } else {
            itemIds = itemIdsList.toArray(new String[0]);
        }

        // create CSL processor
        CSL citeproc = new CSL(itemDataProvider, style, experimentalMode);
        citeproc.setOutputFormat(outputFormat);

        // register citation items
        citeproc.registerCitationItems(itemIds);

        String actualResult;
        if ("bibliography".equals(mode)) {
            Bibliography bibl = citeproc.makeBibliography();
            actualResult = bibl.makeString();
        } else if ("citation".equals(mode)) {
            List<Citation> citations = citeproc.makeCitation(itemIds);
            actualResult = citations.stream()
                    .map(Citation::getText)
                    .collect(Collectors.joining("\n"));
        } else {
            throw new IllegalStateException("Unknown mode: " + mode);
        }

        // compare result
        assertEquals(expectedResult, actualResult);
    }
}

package de.undercouch.citeproc;

import de.undercouch.citeproc.bibtex.BibTeXConverter;
import de.undercouch.citeproc.bibtex.BibTeXItemDataProvider;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.output.Bibliography;
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
     * The current test to run
     */
    private File testFile;

    /**
     * {@code true} if the test should be run in experimental mode
     */
    private boolean experimentalMode;

    /**
     * Get all test files
     */
    @Parameterized.Parameters(name = "{0}, {1}")
    public static Iterable<Object[]> data() {
        URL fixturesUrl = CSL.class.getResource(FIXTURES_DIR);
        File fixturesDir = new File(fixturesUrl.getPath());

        // noinspection ConstantConditions
        return Arrays.stream(fixturesDir.listFiles((dir, name) -> name.endsWith(".yaml")))
                .flatMap(f -> Stream.of(
                        new Object[] { f.getName(), true },
                        new Object[] { f.getName(), false }
                ))
                .collect(Collectors.toList());
    }

    /**
     * Create a new test
     * @param name the name of the test file
     * @param experimentalMode {@code true} if the test should be run in
     * experimental mode
     */
    public FixturesTest(String name, boolean experimentalMode) {
        URL fixturesUrl = CSL.class.getResource(FIXTURES_DIR);
        File fixturesDir = new File(fixturesUrl.getPath());
        testFile = new File(fixturesDir, name);
        this.experimentalMode = experimentalMode;
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
        Map<String, Object> data;
        Yaml yaml = new Yaml();
        try (FileInputStream is = new FileInputStream(testFile)) {
            data = yaml.loadAs(is, Map.class);
        }

        String style = (String)data.get("style");
        String expectedResult = (String)data.get("result");

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

        List<String> itemIds = (List<String>)data.get("itemIds");
        if (itemIds == null) {
            itemIds = new ArrayList<>(Arrays.asList(itemDataProvider.getIds()));
        }

        // create CSL processor
        CSL citeproc = new CSL(itemDataProvider, style, experimentalMode);
        citeproc.setOutputFormat("text");

        // register citation items
        citeproc.registerCitationItems(itemIds.toArray(new String[0]));

        // make bibliography
        Bibliography bibl = citeproc.makeBibliography();

        // compare result
        String actualResult = bibl.makeString();
        assertEquals(expectedResult, actualResult);
    }
}

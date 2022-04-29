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
import org.apache.commons.io.IOUtils;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.jbibtex.BibTeXDatabase;
import org.jbibtex.ParseException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
    private static final String CITEPROC_JS_FILE = "/citeproc-js/citeproc.js";
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
     * Shared JavaScript engine
     */
    private static Engine engine;

    /**
     * Source of citeproc.js
     */
    private static Source citeprocJsSource;

    /**
     * Initialize shared JavaScript engine
     */
    @BeforeClass
    public static void startUpClass() throws IOException {
        engine = Engine.create();

        URL citeprocJsUrl = FixturesTest.class.getResource(CITEPROC_JS_FILE);
        assert citeprocJsUrl != null;
        String citeprocJs = IOUtils.toString(citeprocJsUrl, StandardCharsets.UTF_8);
        citeprocJsSource = Source.create("js", citeprocJs);
    }

    /**
     * Close shared JavaScript engine
     */
    @AfterClass
    public static void tearDownClass() {
        engine.close();
    }

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

                            case "result": {
                                if (value.startsWith("<div")) {
                                    Map<String, Object> htmlMap = new HashMap<>();
                                    htmlMap.put("html", value);
                                    result.put("result", htmlMap);
                                } else {
                                    result.put("result", value);
                                }
                                break;
                            }

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

                            case "bibentries": {
                                JsonParser parser = new JsonParser(
                                        new JsonLexer(new StringReader(value)));
                                List<Object> itemIds = parser.parseArray();
                                result.put("itemIds", itemIds);
                                break;
                            }

                            case "abbreviations": {
                                JsonParser parser = new JsonParser(
                                        new JsonLexer(new StringReader(value)));
                                Map<String, Object> abbrevs = parser.parseObject();
                                result.put("abbreviations", abbrevs.get("default"));
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

        if (!"citation".equals(result.get("mode"))) {
            // `citations' can only be defined if `mode' equals `citation'
            result.remove("citations");
        }

        return result;
    }

    /**
     * Get all test files
     */
    @Parameterized.Parameters(name = "{0}, {1}, {2}")
    @SuppressWarnings("unchecked")
    public static Iterable<Object[]> data() {
        URL fixturesUrl = FixturesTest.class.getResource(FIXTURES_DIR);
        URL testSuiteUrl = FixturesTest.class.getResource(TEST_SUITE_DIR);
        URL testSuiteOverridesUrl = FixturesTest.class.getResource(TEST_SUITE_OVERRIDES_DIR);
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
        Stream<Map<String, Object>> testSuiteStream = Stream.of(TEST_SUITE_TESTS)
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
                            Object oldResult = data.get("result");
                            data.put("resultLegacy", oldResult);
                            if (overridesResult instanceof Map && oldResult instanceof String) {
                                Map<String, Object> overridesResultMap =
                                        (Map<String, Object>)overridesResult;
                                if (overridesResultMap.get("html") != null) {
                                    Map<String, Object> resultLegacyMap =
                                            new HashMap<>();
                                    resultLegacyMap.put("html", oldResult);
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

        // get abbreviations
        DefaultAbbreviationProvider abbreviationProvider = null;
        if (data.containsKey("abbreviations")) {
            abbreviationProvider = new DefaultAbbreviationProvider();
            Map<String, Map<String, String>> abbrevs =
                    (Map<String, Map<String, String>>)data.get("abbreviations");
            for (Map.Entry<String, Map<String, String>> vm : abbrevs.entrySet()) {
                for (Map.Entry<String, String> om : vm.getValue().entrySet()) {
                    abbreviationProvider.addAbbreviation(vm.getKey(),
                            om.getKey(), om.getValue());
                }
            }
        }

        // get the item IDs to test against
        List<Collection<String>> itemIds;
        List<Object> itemIdsListObj = (List<Object>)data.get("itemIds");
        if (itemIdsListObj != null && !itemIdsListObj.isEmpty() &&
                itemIdsListObj.get(0) instanceof String) {
            ArrayList<String> iis = new ArrayList<>();
            for (Object o : itemIdsListObj) {
                iis.add((String)o);
            }
            itemIds = Collections.singletonList(iis);
        } else if (itemIdsListObj != null) {
            itemIds = new ArrayList<>();
            for (Object o : itemIdsListObj) {
                List<String> l = (List<String>)o;
                itemIds.add(l);
            }
        } else {
            itemIds = Collections.singletonList(itemDataProvider.getIds());
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

        String actualResult;
        if (experimentalMode) {
            actualResult = applyCSL(mode, style, itemDataProvider,
                    abbreviationProvider, itemIds, citations);
        } else {
            actualResult = applyCiteprocJs(mode, style, itemDataProvider,
                    abbreviationProvider, itemIds, citations);
        }

        // compare result
        assertEquals(expectedResult, actualResult);
    }

    private String applyCSL(String mode, String style, ItemDataProvider itemDataProvider,
            AbbreviationProvider abbreviationProvider, List<Collection<String>> itemIds,
            List<CSLCitation> citations) throws IOException {
        // create CSL processor
        CSL citeproc = new CSLBuilder()
                .itemDataProvider(itemDataProvider)
                .style(style)
                .abbreviationProvider(abbreviationProvider)
                .build();
        citeproc.setOutputFormat(outputFormat);
        citeproc.setConvertLinks(true);

        // register citation items
        for (Collection<String> ii : itemIds) {
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
        return actualResult;
    }

    private String applyCiteprocJs(String mode, String style, ItemDataProvider itemDataProvider,
            AbbreviationProvider abbreviationProvider, List<Collection<String>> itemIds,
            List<CSLCitation> citations) throws IOException {
        // load style if necessary
        if (!style.trim().startsWith("<")) {
            if (!style.endsWith(".csl")) {
                style = style + ".csl";
            }
            if (!style.startsWith("/")) {
                style = "/" + style;
            }
            URL styleUrl = CSL.class.getResource(style);
            assert styleUrl != null;
            style = IOUtils.toString(styleUrl, StandardCharsets.UTF_8);
        }

        try (Context context = Context.newBuilder("js")
                .engine(engine)
                .allowHostAccess(HostAccess.ALL)
                .allowHostClassLookup(className -> true)
                .build()) {
            context.eval(citeprocJsSource);

            Value bindings = context.getBindings("js");
            bindings.putMember("style", style);
            bindings.putMember("itemDataProvider", itemDataProvider);
            bindings.putMember("itemIds", itemIds);
            bindings.putMember("outputFormat", outputFormat);
            bindings.putMember("citations", citations);

            LocaleProvider localeProvider = new DefaultLocaleProvider();
            bindings.putMember("localeProvider", localeProvider);

            context.eval("js",
                "let StringJsonBuilderFactory = Java.type('de.undercouch.citeproc.helper.json.StringJsonBuilderFactory')\n" +
                "let jsonFactory = new StringJsonBuilderFactory()\n");

            context.eval("js",
                    "let sys = {\n" +
                        "retrieveLocale: function(lang) { return localeProvider.retrieveLocale(lang) },\n" +
                        "retrieveItem: function(id) {\n" +
                            "let item = itemDataProvider.retrieveItem(id)\n" +
                            "let jsonBuilder = jsonFactory.createJsonBuilder()\n" +
                            "return JSON.parse(item.toJson(jsonBuilder))\n" +
                        "}\n" +
                    "}\n" +
                    "let csl = new CSL.Engine(sys, style)\n" +
                    "csl.setOutputFormat(outputFormat)\n" +
                    "csl.opt.development_extensions.wrap_url_and_doi = true\n" +
                    "for (let ids of itemIds) {\n" +
                        // convert IDs to native JavaScript array
                        "let idsArr = []\n" +
                        "for (let id of ids) { idsArr.push(id) }\n" +
                        "csl.updateItems(idsArr);\n" +
                    "}\n");

            if (abbreviationProvider != null) {
                bindings.putMember("abbreviationProvider", abbreviationProvider);
                context.eval("js",
                        "sys.getAbbreviation = function(styleID, abbrevs, name, category, orig, itemType) {\n" +
                            "let r = abbreviationProvider.getAbbreviation(category, orig, null)\n" +
                            "if (r) {\n" +
                                "abbrevs[name][category][orig] = r\n" +
                            "}\n" +
                        "}\n");
            }

            String actualResult;
            if ("bibliography".equals(mode)) {
                context.eval("js",
                        "let bibliography = csl.makeBibliography()\n" +
                        "let biblStr = ''\n" +
                        "if (bibliography[0].bibstart) biblStr += bibliography[0].bibstart\n" +
                        "for (let e of bibliography[1]) biblStr += e\n" +
                        "if (bibliography[0].bibend) biblStr += bibliography[0].bibend\n");
                actualResult = bindings.getMember("biblStr").asString();
            } else if ("citation".equals(mode)) {
                context.eval("js",
                        "let generatedCitations = []\n" +
                        "if (citations) {\n" +
                            "for (let c of citations) {\n" +
                                "let jsonBuilder = jsonFactory.createJsonBuilder()\n" +
                                "let obj = JSON.parse(c.toJson(jsonBuilder))\n" +
                                "let r = csl.appendCitationCluster(obj)\n" +
                                "generatedCitations.push(r[0][1])\n" +
                            "}\n" +
                        "} else {\n" +
                            "let citation = { citationItems: csl.registry.reflist }\n" +
                            "let r = csl.appendCitationCluster(citation)\n" +
                            "generatedCitations.push(r[0][1])\n" +
                        "}\n" +
                        "generatedCitations = generatedCitations.join('\\n')\n");
                actualResult = bindings.getMember("generatedCitations").asString();
            } else {
                throw new IllegalStateException("Unknown mode: " + mode);
            }
            return actualResult;
        }
    }

    private final static String[] TEST_SUITE_TESTS = new String[] {
            "affix_CommaAfterQuote",
            "affix_InterveningEmpty",
            "affix_MovingPunctuation",
            "affix_PrefixFullCitationTextOnly",
            // "affix_PrefixWithDecorations",
            "affix_SpaceWithQuotes",
            "affix_TextNodeWithMacro",
            // "affix_WithCommas",
            "affix_WordProcessorAffixNoSpace",
            "api_UpdateItemsDelete",
            "api_UpdateItemsDeleteDecrementsByCiteDisambiguation",
            "api_UpdateItemsReshuffle",
            // "bibheader_EntryspacingDefaultValueOne",
            // "bibheader_EntryspacingExplicitValueZero",
            // "bibheader_SecondFieldAlign",
            // "bibheader_SecondFieldAlignWithAuthor",
            // "bibheader_SecondFieldAlignWithNumber",
            "bugreports_Abnt",
            // "bugreports_AccidentalAllCaps",
            // "bugreports_AllCapsLeakage",
            // "bugreports_ApostropheOnParticle",
            // "bugreports_ArabicLocale",
            // "bugreports_AsaSpacing",
            // "bugreports_AsmJournals",
            // "bugreports_AuthorPosition",
            // "bugreports_AuthorYear",
            // "bugreports_AutomaticallyDeleteItemsFails",
            // "bugreports_BadCitationUpdate",
            // "bugreports_BadDelimiterBeforeCollapse",
            // "bugreports_ByBy",
            // "bugreports_CapsAfterOneWordPrefix",
            // "bugreports_ChicagoAuthorDateLooping",
            // "bugreports_ChineseCharactersFamilyOnlyPluralLabel",
            // "bugreports_CitationSortsWithEtAl",
            // "bugreports_CollapseFailure",
            // "bugreports_ContainerTitleShort",
            // "bugreports_ContentPunctuationDuplicate1",
            // "bugreports_ContextualPluralWithMainItemFields",
            // "bugreports_CreepingAddNames",
            // "bugreports_DelimiterOnLayout",
            // "bugreports_DelimitersOnLocator",
            // "bugreports_DemoPageFullCiteCruftOnSubsequent",
            // "bugreports_DisambiguationAddNames",
            // "bugreports_DisambiguationAddNamesBibliography",
            // "bugreports_DoubleEncodedAngleBraces",
            // "bugreports_DroppingGroupDelimiterSpace",
            // "bugreports_DuplicateSpaces",
            // "bugreports_DuplicateSpaces2",
            // "bugreports_DuplicateSpaces3",
            // "bugreports_DuplicateTerminalPunctuationInBibliography",
            // "bugreports_EmptyIfMatchNoneFail",
            // "bugreports_EmptyInput",
            // "bugreports_EnvAndUrb",
            // "bugreports_EtAlSubsequent",
            // "bugreports_FourAndFour",
            // "bugreports_FrenchApostrophe",
            // "bugreports_GreekStyleProblems",
            // "bugreports_GreekStyleTwoEditors",
            // "bugreports_IeeePunctuation",
            // "bugreports_IllustratorInExtra",
            // "bugreports_LabelsOutOfPlace",
            // "bugreports_LegislationCrash",
            // "bugreports_MatchedAuthorAndDate",
            // "bugreports_MissingItemInJoin",
            // "bugreports_MovePunctuationInsideQuotesForLocator",
            // "bugreports_NoCaseEscape",
            // "bugreports_NoEventInNestedMacroWithOldProcessor",
            // "bugreports_NoTitle",
            // "bugreports_NonBreakingSpaceJoinFail",
            // "bugreports_NumberAffixEscape",
            // "bugreports_NumberInMacroWithVerticalAlign",
            // "bugreports_OldMhraDisambiguationFailure",
            // "bugreports_OverwriteCitationItems",
            // "bugreports_ProcessorHang1",
            // "bugreports_SectionAndLocator",
            // "bugreports_SelfLink",
            // "bugreports_SimpleBib",
            // "bugreports_SingleQuote",
            // "bugreports_SingleQuoteXml",
            // "bugreports_SingletonIfMatchNoneFail",
            // "bugreports_SmallCapsEscape",
            // "bugreports_SortSecondaryKey",
            // "bugreports_SortSecondaryKeyBibliography",
            // "bugreports_SortedIeeeItalicsFail",
            // "bugreports_StyleError001",
            // "bugreports_ThesisUniversityAppearsTwice",
            // "bugreports_TitleCase",
            // "bugreports_TwoFullStops",
            // "bugreports_UndefinedBeforeVal",
            // "bugreports_UndefinedInName",
            // "bugreports_UndefinedInName2",
            // "bugreports_UndefinedInName3",
            // "bugreports_UndefinedNotString",
            // "bugreports_UndefinedStr",
            // "bugreports_UnisaHarvardInitialization",
            // "bugreports_YearSuffixInHarvard1",
            // "bugreports_YearSuffixLingers",
            // "bugreports_disambiguate",
            // "bugreports_effingBug",
            // "bugreports_ikeyOne",
            // "bugreports_parenthesis",
            // name with quotes and particle
            // "bugreports_parseName",
            // "bugreports_undefinedCrash",
            // "collapse_AuthorCollapse",
            // "collapse_AuthorCollapseDifferentAuthorsOneWithEtAl",
            // "collapse_AuthorCollapseNoDate",
            // "collapse_AuthorCollapseNoDateSorted",
            // "collapse_ChicagoAfterCollapse",
            // "collapse_CitationNumberRangesInsert",
            // "collapse_CitationNumberRangesMixed",
            // "collapse_CitationNumberRangesMixed2",
            // "collapse_CitationNumberRangesMixed3",
            // "collapse_CitationNumberRangesOneOnly",
            // "collapse_CitationNumberRangesWithAffixes",
            // "collapse_CitationNumberRangesWithAffixesGrouped",
            // "collapse_CitationNumberRangesWithAffixesGroupedLocator",
            // "collapse_CitationNumberRangesWithAffixesNoCollapse",
            // "collapse_NumericDuplicate",
            // "collapse_NumericDuplicate2",
            // "collapse_TrailingDelimiter",
            // "collapse_YearSuffixCollapse",
            // "collapse_YearSuffixCollapseNoRange",
            // "collapse_YearSuffixCollapseNoYearSuffixDelimiter",
            // "collapse_YearSuffixImplicitCollapseNoYearSuffixDelimiter",
            "condition_EmptyDate",
            "condition_EmptyIsNumericFalse",
            "condition_EmptyIsUncertainDateFalse",
            "condition_EmptyShortTitleFalse",
            "condition_FirstNullAny",
            // Abbreviations are not supported yet
            // "condition_IsNumeric",
            "condition_LocatorIsFalse",
            "condition_MatchAll",
            "condition_NameAndTextVars",
            "condition_NumberIsNumeric",
            "condition_NumeralIsNumeric",
            "condition_NumeralWithTextIsNumeric",
            "condition_RefTypeBranching",
            "condition_SingletonIfMatchNone",
            "condition_TextIsNotNumeric",
            "condition_VariableAll",
            "condition_VariableAny",
            "condition_VariableNone",
            // "date_Accessed",
            // "date_AccessedCrash",
            // "date_DateAD",
            // "date_DateBC",
            // "date_DateNoDateNoTest",
            // "date_DateNoDateWithTest",
            // "date_DayOrdinalDayOneOnly",
            // "date_DisappearingBug",
            // "date_EmptyStrings",
            // "date_IgnoreNonexistentSort",
            // "date_InPress",
            // "date_January",
            // "date_KeyVariable",
            // "date_LiteralFailGracefullyIfNoValue",
            // "date_LocalizedDateFormats-af-ZA",
            // "date_LocalizedDateFormats-ar-AR",
            // "date_LocalizedDateFormats-bg-BG",
            // "date_LocalizedDateFormats-ca-AD",
            // "date_LocalizedDateFormats-cs-CZ",
            // "date_LocalizedDateFormats-da-DK",
            // "date_LocalizedDateFormats-de-AT",
            // "date_LocalizedDateFormats-de-CH",
            // "date_LocalizedDateFormats-de-DE",
            // "date_LocalizedDateFormats-el-GR",
            // "date_LocalizedDateFormats-en-US",
            // "date_LocalizedDateFormats-es-ES",
            // "date_LocalizedDateFormats-et-EE",
            // "date_LocalizedDateFormats-fr-FR",
            // "date_LocalizedDateFormats-he-IL",
            // "date_LocalizedDateFormats-hu-HU",
            // "date_LocalizedDateFormats-is-IS",
            // "date_LocalizedDateFormats-it-IT",
            // "date_LocalizedDateFormats-ja-JP",
            // "date_LocalizedDateFormats-kh-KH",
            // "date_LocalizedDateFormats-ko-KR",
            // "date_LocalizedDateFormats-mn-MN",
            // "date_LocalizedDateFormats-nb-NO",
            // "date_LocalizedDateFormats-nl-NL",
            // "date_LocalizedDateFormats-pl-PL",
            // "date_LocalizedDateFormats-pt-BR",
            // "date_LocalizedDateFormats-pt-PT",
            // "date_LocalizedDateFormats-ro-RO",
            // "date_LocalizedDateFormats-ru-RU",
            // "date_LocalizedDateFormats-sk-SK",
            // "date_LocalizedDateFormats-sl-SL",
            // "date_LocalizedDateFormats-sr-RS",
            // "date_LocalizedDateFormats-sv-SE",
            // "date_LocalizedDateFormats-th-TH",
            // "date_LocalizedDateFormats-tr-TR",
            // "date_LocalizedDateFormats-uk-UA",
            // "date_LocalizedDateFormats-vi-VN",
            // "date_LocalizedDateFormats-zh-CN",
            // "date_LocalizedDateFormats-zh-TW",
            // "date_LocalizedNumericDefault",
            // "date_LocalizedNumericDefaultMissingDay",
            // "date_LocalizedNumericDefaultWithAffixes",
            // "date_LocalizedNumericYear",
            // "date_LocalizedNumericYearMonth",
            // "date_LocalizedNumericYearRange",
            // "date_LocalizedNumericYearWithAffixes",
            // "date_LocalizedTextDefault",
            // "date_LocalizedTextDefaultMissingDay",
            // "date_LocalizedTextDefaultWithAffixes",
            // "date_LocalizedTextInStyleLocaleWithTextCase",
            // "date_LocalizedTextMonthFormOverride",
            // "date_LocalizedTextYear",
            // "date_LocalizedTextYearMonth",
            // "date_LocalizedTextYearWithAffixes",
            // "date_LocalizedWithInStyleFormatting",
            // "date_LongMonth",
            // "date_LopsidedDataYearSuffixCollapse",
            // "date_MaskNonexistentWithCondition",
            // "date_NegativeDateSort",
            // "date_NegativeDateSortViaMacro",
            // "date_NegativeDateSortViaMacroOnYearMonthOnly",
            // "date_NoDate",
            // "date_NonexistentSortReverseBibliography",
            // "date_NonexistentSortReverseCitation",
            // "date_OtherAlone",
            // "date_OtherWithDate",
            // "date_RangeDelimiter",
            // "date_SeasonRange1",
            // "date_SeasonRange2",
            // "date_SeasonRange3",
            // "date_SeasonSubstituteInGroup",
            // "date_SortEmptyDatesBibliography",
            // "date_SortEmptyDatesCitation",
            "date_String",
            // "date_TextFormFulldateDayRange",
            // "date_TextFormFulldateMonthRange",
            // "date_TextFormFulldateYearRange",
            // "date_TextFormMonthdateMonthRange",
            // "date_TextFormMonthdateYearRange",
            // "date_TextFormYeardateYearRange",
            // "date_TextFormYeardateYearRangeOpen",
            // "date_Uncertain",
            // "date_VariousInvalidDates",
            // "date_YearSuffixDelimiter",
            // "date_YearSuffixImplicitWithNoDate",
            // "date_YearSuffixImplicitWithNoDateOneOnly",
            // "date_YearSuffixWithNoDate",
            // "decorations_AndTermUnaffectedByNameDecorations",
            // "decorations_Baseline",
            // "decorations_NestedQuotes",
            // "decorations_NestedQuotesInnerReverse",
            // "decorations_NoNormalWithoutDecoration",
            // "decorations_SimpleFlipFlop",
            // "decorations_SimpleQuotes",
            // "disambiguate_AddNamesFailure",
            // "disambiguate_AddNamesFailureWithAddGivenname",
            // "disambiguate_AddNamesSuccess",
            // "disambiguate_AllNamesBaseNameCountOnFailureIfYearSuffixAvailable",
            // "disambiguate_AllNamesGenerally",
            // "disambiguate_AllNamesSimpleSequence",
            // "disambiguate_AllNamesWithInitialsBibliography",
            // "disambiguate_AllNamesWithInitialsGenerally",
            // "disambiguate_AndreaEg1a",
            // "disambiguate_AndreaEg1b",
            // "disambiguate_AndreaEg1c",
            // "disambiguate_AndreaEg2",
            // "disambiguate_AndreaEg3",
            // "disambiguate_AndreaEg4",
            // "disambiguate_AndreaEg5",
            // "disambiguate_BasedOnEtAlSubsequent",
            // "disambiguate_BasedOnSubsequentFormWithBackref",
            // "disambiguate_BasedOnSubsequentFormWithBackref2",
            // "disambiguate_BasedOnSubsequentFormWithLocator",
            // "disambiguate_ByCiteBaseNameCountOnFailureIfYearSuffixAvailable",
            // "disambiguate_ByCiteDisambiguateCondition",
            // "disambiguate_ByCiteGivennameExpandCrossNestedNames",
            // "disambiguate_ByCiteGivennameNoShortFormInitializeWith",
            // "disambiguate_ByCiteGivennameShortFormInitializeWith",
            // "disambiguate_ByCiteGivennameShortFormNoInitializeWith",
            // "disambiguate_ByCiteIncremental1",
            // "disambiguate_ByCiteIncremental2",
            // "disambiguate_ByCiteIsDefault",
            // "disambiguate_ByCiteMinimalGivennameExpandMinimalNames",
            // "disambiguate_ByCiteRetainNamesOnFailureIfYearSuffixNotAvailable",
            // "disambiguate_ByCiteTwoAuthorsSameCite",
            // "disambiguate_ByCiteTwoAuthorsSameFamilyName",
            // "disambiguate_CitationLabelDefault",
            // "disambiguate_CitationLabelInData",
            // "disambiguate_DifferentSpacingInInitials",
            // "disambiguate_DisambiguateTrueAndYearSuffixOne",
            // "disambiguate_DisambiguateTrueReflectedInBibliography",
            // "disambiguate_DisambiguateWithThree",
            // "disambiguate_DisambiguateWithThree2",
            // "disambiguate_DisambiguationHang",
            // "disambiguate_ExtraTextCitation",
            // "disambiguate_FailWithYearSuffix",
            // "disambiguate_FamilyNameOnly",
            // "disambiguate_HonorFullnameInBibliography",
            // "disambiguate_ImplicitYearSuffixOnceOnly",
            // "disambiguate_IncrementalExtraText",
            // "disambiguate_InitializeWithButNoDisambiguation",
            // "disambiguate_LastOnlyFailWithByCite",
            // "disambiguate_NoTextElementUsesYearSuffixVariable",
            // "disambiguate_PrimaryNameGenerally",
            // "disambiguate_PrimaryNameWithInitialsLimitedToPrimary",
            // "disambiguate_SetsOfNames",
            // "disambiguate_SkipAccessedYearSuffix",
            // "disambiguate_ThreeNoAuthorNoTitleEntries",
            // "disambiguate_ToInitialOnly",
            // "disambiguate_Trigraph",
            // "disambiguate_WithOriginalYear",
            // "disambiguate_YearCollapseWithInstitution",
            // "disambiguate_YearSuffixAndSort",
            // "disambiguate_YearSuffixAtTwoLevels",
            // "disambiguate_YearSuffixFiftyTwoEntries",
            // "disambiguate_YearSuffixFiftyTwoEntriesByCite",
            // "disambiguate_YearSuffixMacroSameYearExplicit",
            // "disambiguate_YearSuffixMacroSameYearImplicit",
            // "disambiguate_YearSuffixMidInsert",
            // "disambiguate_YearSuffixMixedDates",
            // "disambiguate_YearSuffixTwoPairsBibliography",
            // "disambiguate_YearSuffixTwoPairsFirstNameBibliography",
            // "disambiguate_YearSuffixTwoPairsFullNamesBibliography",
            // "disambiguate_YearSuffixWithEtAlSubequent",
            // "disambiguate_YearSuffixWithMixedCreatorTypes",
            // "display_AuthorAsHeading",
            // "display_DisplayBlock",
            // "display_LostSuffix",
            // "display_SecondFieldAlignClone",
            // "display_SecondFieldAlignMigratePunctuation",
            // "etal_CitationAndBibliographyDecorationsInBibliography",
            // "etal_CitationAndBibliographyDecorationsInCitation",
            // "etal_ShortFormOfName",
            // "etal_UseZeroFirst",
            // "flipflop_Apostrophes",
            // "flipflop_BoldfaceNodeLevelMarkup",
            // "flipflop_CompleteCiteInPrefix",
            // "flipflop_ItalicsFlipped",
            // "flipflop_ItalicsSimple",
            // "flipflop_ItalicsWithOk",
            // "flipflop_ItalicsWithOkAndTextcase",
            // "flipflop_LeadingMarkupWithApostrophe",
            // "flipflop_LeadingSingleQuote",
            // "flipflop_LongComplexPrefix",
            // "flipflop_NumericField",
            // "flipflop_OrphanQuote",
            // "flipflop_QuotesInFieldNotOnNode",
            // "flipflop_QuotesNodeLevelMarkup",
            // "flipflop_SingleBeforeColon",
            // "flipflop_SingleQuotesOnItalics",
            // "flipflop_SmallCaps",
            // "flipflop_StartingApostrophe",
            // "form_ShortTitleOnly",
            // "form_TitleShort",
            // "form_TitleShortNoLong",
            // "form_TitleTestNoLongFalse",
            // "fullstyles_ABdNT",
            // "fullstyles_APA",
            // "fullstyles_ChicagoArticleTitleQuestion",
            // "fullstyles_ChicagoAuthorDateSimple",
            // "fullstyles_ChicagoNoteWithBibliographyWithPublisher",
            "group_ComplexNesting",
            // "group_LegalWithAuthorDate",
            "group_ShortOutputOnly",
            // "group_SuppressTermInMacro",
            "group_SuppressTermWhenNoOutputFromPartialDate",
            "group_SuppressValueWithEmptySubgroup",
            "group_SuppressWithEmptyNestedDateNode",
            // "integration_CitationSort",
            // "integration_CitationSortTwice",
            // "integration_DeleteName",
            // "integration_DisambiguateAddGivenname1",
            // "integration_DisambiguateAddGivenname2",
            // "integration_DuplicateItem",
            // "integration_DuplicateItem2",
            // "integration_FirstReferenceNoteNumberPositionChange",
            // "integration_IbidOnInsert",
            // "integration_IbidWithDifferentLocators",
            // "integration_SimpleFirstReferenceNoteNumber",
            // "integration_SimpleIbid",
            // "integration_SubsequentWhenInterveningFootnote",
            // "integration_YearSuffixOnOffOn",
            // "label_CollapsedPageNumberPluralDetection",
            // "label_CompactNamesAfterFullNames",
            // "label_EditorTranslator1",
            // "label_EditorTranslator2",
            // "label_EmptyLabelVanish",
            // "label_EmptyLabelVanishPage",
            // "label_ImplicitForm",
            // "label_MissingReturnsEmpty",
            // "label_NameLabelThroughSubstitute",
            // "label_NoFirstCharCapWithInTextClass",
            // "label_NonexistentNameVariableLabel",
            // "label_PageWithEmbeddedLabel",
            // "label_PluralNumberOfVolumes",
            // "label_PluralPagesWithAlphaPrefix",
            // "label_PluralWithAmpersand",
            // "label_PluralWithAnd",
            // "label_PluralWithCommaAnd",
            // "label_PluralWithCommaLocalizedAnd",
            // "label_PluralWithLocalizedAmpersand",
            // "label_PluralWithLocalizedAnd",
            // "locale_EmptyDate",
            // "locale_EmptyPlusOverrideDate",
            // "locale_EmptyPlusOverrideStyleOpt",
            // "locale_EmptyPlusOverrideTerm",
            // "locale_EmptyStyleOpt",
            // "locale_EmptyTerm",
            // "locale_ForceEmptyAndOthersTerm",
            // "locale_ForceEmptyEtAlTerm",
            // "locale_NonExistentLocaleDef",
            // "locale_OverloadWithEmptyString",
            // "locale_PageRangeDelimiterTermDefined",
            // "locale_PageRangeDelimiterTermFrenchUndef",
            // "locale_PageRangeDelimiterTermUndefined",
            // "locale_SpecificDate",
            // "locale_SpecificStyleOpt",
            // "locale_SpecificTerm",
            // "locale_TitleCaseEmptyLangEmptyLocale",
            // "locale_TitleCaseEmptyLangNonEnglishLocale",
            // "locale_TitleCaseGarbageLangEmptyLocale",
            // "locale_TitleCaseGarbageLangEnglishLocale",
            // "locale_TitleCaseGarbageLangNonEnglishLocale",
            // "locale_TitleCaseNonEnglishLangUpperEmptyLocale",
            // "locale_UnknownTerm",
            // "locator_SimpleLocators",
            // "locator_SingularEmbeddedLabelAfterPlural",
            // "locator_TermSelection",
            // "locator_TrickyEntryForPlurals",
            // "locator_WithLeadingSpace",
            // "locator_WorkaroundTestForSubVerbo",
            // "magic_AllowRepeatDateRenderings",
            // "magic_CapitalizeFirstOccurringNameParticle",
            // "magic_CapitalizeFirstOccurringTerm",
            // "magic_CitationLabelInBibliography",
            // "magic_CitationLabelInCitation",
            // "magic_EntrySpacingDouble",
            // "magic_HangingIndent",
            // "magic_ImplicitYearSuffixDelimiter",
            // "magic_ImplicitYearSuffixExplicitDelimiter",
            // "magic_LineSpacingDouble",
            // "magic_LineSpacingTripleStretch",
            // "magic_NameParticle",
            // "magic_NameSuffixNoComma",
            // "magic_NameSuffixWithComma",
            // "magic_NumberRangeEnglish",
            // "magic_NumberRangeFrench",
            // "magic_PunctuationInQuoteDefaultEnglishDelimiter",
            // "magic_PunctuationInQuoteDefaultEnglishSuffix",
            // "magic_PunctuationInQuoteDelimiterTrue",
            // "magic_PunctuationInQuoteFalse",
            // "magic_PunctuationInQuoteFalseSuppressExtra",
            // "magic_PunctuationInQuoteNested",
            // "magic_PunctuationInQuoteSuffixTrue",
            // "magic_PunctuationInQuoteTrueSuppressExtra",
            // "magic_QuotesAndBraces1",
            // "magic_QuotesAndBraces2",
            // "magic_SecondFieldAlign",
            // "magic_StripPeriodsExcludeAffixes",
            // "magic_StripPeriodsFalse",
            // "magic_StripPeriodsTrue",
            // "magic_StripPeriodsTrueShortForm",
            // "magic_SubsequentAuthorSubstitute",
            // "magic_SubsequentAuthorSubstituteNotFooled",
            // "magic_SubsequentAuthorSubstituteOfTitleField",
            // "magic_SuperscriptChars",
            // "magic_SuppressDuplicateVariableRendering",
            // "magic_SuppressLayoutDelimiterIfPrefixComma",
            // "magic_TermCapitalizationWithPrefix",
            // "magic_TextRangeEnglish",
            // "magic_TextRangeFrench",
            // "name_AfterInvertedName",
            // "name_AllCapsInitialsUntouched",
            // "name_AndTextDelimiterPrecedesLastAlways",
            // "name_ApostropheInGivenName",
            // "name_ArabicShortForms",
            // "name_ArticularNameAsSortOrder",
            // "name_ArticularPlain",
            // "name_ArticularShortForm",
            // "name_ArticularShortFormCommaSuffix",
            // "name_ArticularWithComma",
            // "name_ArticularWithCommaNameAsSortOrder",
            // "name_AsianGlyphs",
            // "name_AuthorCount",
            // "name_AuthorCountWithMultipleVariables",
            // "name_AuthorCountWithSameVarContentAndCombinedTermFail",
            // "name_AuthorCountWithSameVarContentAndCombinedTermSucceed",
            // "name_AuthorEditorCount",
            // "name_BibliographyNameFormNeverShrinks",
            // "name_CelticClanName",
            // "name_CeltsAndToffsCrowdedInitials",
            // "name_CeltsAndToffsNoHyphens",
            // "name_CeltsAndToffsSpacedInitials",
            // "name_CeltsAndToffsWithHyphens",
            // "name_CiteGroupDelimiterWithYearCollapse",
            // "name_CiteGroupDelimiterWithYearSuffixCollapse",
            // "name_CiteGroupDelimiterWithYearSuffixCollapse2",
            // "name_CiteGroupDelimiterWithYearSuffixCollapse3",
            // "name_CollapseRoleLabels",
            // "name_Delimiter",
            // "name_EditorTranslatorBoth",
            // "name_EditorTranslatorSameEmptyTerm",
            // "name_EditorTranslatorSameWithTerm",
            // "name_EditorTranslatorWithTranslatorOnlyBib",
            // "name_EtAlKanji",
            // "name_EtAlUseLast",
            // "name_EtAlWithCombined",
            // "name_FirstInitialFullForm",
            // "name_FormattingOfParticles",
            // "name_GreekSimple",
            // "name_HebrewAnd",
            // "name_HierarchicalDelimiter",
            // "name_HyphenatedFirstName",
            // "name_HyphenatedNonDroppingParticle1",
            // "name_HyphenatedNonDroppingParticle2",
            // "name_InheritAttributesEtAlStyle",
            "name_InitialsInitializeFalse",
            "name_InitialsInitializeFalseEmpty",
            "name_InitialsInitializeFalsePeriod",
            "name_InitialsInitializeFalsePeriodSpace",
            "name_InitialsInitializeTrue",
            "name_InitialsInitializeTrueEmpty",
            "name_InitialsInitializeTruePeriod",
            "name_InitialsInitializeTruePeriodSpace",
            // "name_Institution",
            // "name_InstitutionDecoration",
            // "name_LabelAfterPlural",
            // "name_LabelAfterPluralDecorations",
            // "name_LabelFormatBug",
            // "name_LiteralWithComma",
            // "name_LongAbbreviation",
            // "name_LowercaseSurnameSuffix",
            // "name_MultipleLiteral",
            // "name_NoNameNode",
            // "name_NonDroppingParticleDefault",
            // "name_OnlyFamilyname",
            // "name_OnlyGivenname",
            // "name_OverridingHierarchicalDelimiter",
            // "name_ParseNames",
            // "name_ParsedCommaDelimitedDroppingParticleSortOrderingWithoutAffixes",
            // "name_ParsedDroppingParticleWithAffixes",
            // "name_ParsedDroppingParticleWithApostrophe",
            // "name_ParsedNonDroppingParticleWithAffixes",
            // "name_ParsedNonDroppingParticleWithApostrophe",
            // "name_ParsedUpperCaseNonDroppingParticle",
            // "name_ParticleCaps1",
            // "name_ParticleCaps2",
            // "name_ParticleCaps3",
            // "name_ParticleFormatting",
            // "name_ParticleParse1",
            // "name_ParticlesDemoteNonDroppingNever",
            // "name_PeriodAfterInitials",
            // "name_QuashOrdinaryVariableRenderedViaSubstitute",
            // "name_RomanianTwo",
            // "name_SemicolonWithAnd",
            // "name_SplitInitials",
            // "name_StaticParticles",
            // "name_SubsequentAuthorSubstituteMultipleNames",
            // "name_SubstituteInheritLabel",
            // "name_SubstituteMacroInheritDecorations",
            // "name_SubstituteName",
            // "name_SubstituteOnDateGroupSpanFail",
            // "name_SubstituteOnGroupSpanGroupSpanFail",
            // "name_SubstituteOnMacroGroupSpanFail",
            // "name_SubstituteOnNamesSingletonGroupSpanFail",
            // "name_SubstituteOnNamesSpanGroupSpanFail",
            // "name_SubstituteOnNamesSpanNamesSpanFail",
            // "name_SubstituteOnNumberGroupSpanFail",
            // "name_TwoRolesSameRenderingSeparateRoleLabels",
            // "name_WesternArticularLowercase",
            // "name_WesternPrimaryFontStyle",
            // "name_WesternPrimaryFontStyleTwoAuthors",
            // "name_WesternSimple",
            // "name_WesternTwoAuthors",
            // "name_WithNonBreakingSpace",
            // "name_namepartAffixes",
            // "name_namepartAffixesNameAsSortOrder",
            // "name_namepartAffixesNameAsSortOrderDemoteNonDroppingParticle",
            // "nameattr_AndOnBibliographyInBibliography",
            // "nameattr_AndOnBibliographyInCitation",
            // "nameattr_AndOnCitationInBibliography",
            // "nameattr_AndOnCitationInCitation",
            // "nameattr_AndOnNamesInBibliography",
            // "nameattr_AndOnNamesInCitation",
            // "nameattr_AndOnStyleInBibliography",
            // "nameattr_AndOnStyleInCitation",
            // "nameattr_DelimiterPrecedesEtAlOnBibliographyInBibliography",
            // "nameattr_DelimiterPrecedesEtAlOnBibliographyInCitation",
            // "nameattr_DelimiterPrecedesEtAlOnCitationInBibliography",
            // "nameattr_DelimiterPrecedesEtAlOnCitationInCitation",
            // "nameattr_DelimiterPrecedesEtAlOnNamesInBibliography",
            // "nameattr_DelimiterPrecedesEtAlOnNamesInCitation",
            // "nameattr_DelimiterPrecedesEtAlOnStyleInBibliography",
            // "nameattr_DelimiterPrecedesEtAlOnStyleInCitation",
            // "nameattr_DelimiterPrecedesLastOnBibliographyInBibliography",
            // "nameattr_DelimiterPrecedesLastOnBibliographyInCitation",
            // "nameattr_DelimiterPrecedesLastOnCitationInBibliography",
            // "nameattr_DelimiterPrecedesLastOnCitationInCitation",
            // "nameattr_DelimiterPrecedesLastOnNamesInBibliography",
            // "nameattr_DelimiterPrecedesLastOnNamesInCitation",
            // "nameattr_DelimiterPrecedesLastOnStyleInBibliography",
            // "nameattr_DelimiterPrecedesLastOnStyleInCitation",
            // "nameattr_EtAlMinOnBibliographyInBibliography",
            // "nameattr_EtAlMinOnBibliographyInCitation",
            // "nameattr_EtAlMinOnCitationInBibliography",
            // "nameattr_EtAlMinOnCitationInCitation",
            // "nameattr_EtAlMinOnNamesInBibliography",
            // "nameattr_EtAlMinOnNamesInCitation",
            // "nameattr_EtAlMinOnStyleInBibliography",
            // "nameattr_EtAlMinOnStyleInCitation",
            // "nameattr_EtAlSubsequentMinOnBibliographyInBibliography",
            // "nameattr_EtAlSubsequentMinOnBibliographyInCitation",
            // "nameattr_EtAlSubsequentMinOnCitationInBibliography",
            // "nameattr_EtAlSubsequentMinOnCitationInCitation",
            // "nameattr_EtAlSubsequentMinOnNamesInBibliography",
            // "nameattr_EtAlSubsequentMinOnStyleInBibliography",
            // "nameattr_EtAlSubsequentMinOnStyleInCitation",
            // "nameattr_EtAlSubsequentUseFirstOnBibliographyInBibliography",
            // "nameattr_EtAlSubsequentUseFirstOnBibliographyInCitation",
            // "nameattr_EtAlSubsequentUseFirstOnCitationInBibliography",
            // "nameattr_EtAlSubsequentUseFirstOnCitationInCitation",
            // "nameattr_EtAlSubsequentUseFirstOnStyleInBibliography",
            // "nameattr_EtAlSubsequentUseFirstOnStyleInCitation",
            // "nameattr_EtAlUseFirstOnBibliographyInBibliography",
            // "nameattr_EtAlUseFirstOnBibliographyInCitation",
            // "nameattr_EtAlUseFirstOnCitationInBibliography",
            // "nameattr_EtAlUseFirstOnCitationInCitation",
            // "nameattr_EtAlUseFirstOnNamesInBibliography",
            // "nameattr_EtAlUseFirstOnNamesInCitation",
            // "nameattr_EtAlUseFirstOnStyleInBibliography",
            // "nameattr_EtAlUseFirstOnStyleInCitation",
            // "nameattr_InitializeWithOnBibliographyInBibliography",
            // "nameattr_InitializeWithOnBibliographyInCitation",
            // "nameattr_InitializeWithOnCitationInBibliography",
            // "nameattr_InitializeWithOnCitationInCitation",
            // "nameattr_InitializeWithOnNamesInBibliography",
            // "nameattr_InitializeWithOnNamesInCitation",
            // "nameattr_InitializeWithOnStyleInBibliography",
            // "nameattr_InitializeWithOnStyleInCitation",
            // "nameattr_NameAsSortOrderOnBibliographyInBibliography",
            // "nameattr_NameAsSortOrderOnBibliographyInCitation",
            // "nameattr_NameAsSortOrderOnCitationInBibliography",
            // "nameattr_NameAsSortOrderOnCitationInCitation",
            // "nameattr_NameAsSortOrderOnNamesInBibliography",
            // "nameattr_NameAsSortOrderOnNamesInCitation",
            // "nameattr_NameAsSortOrderOnStyleInBibliography",
            // "nameattr_NameAsSortOrderOnStyleInCitation",
            // "nameattr_NameDelimiterOnBibliographyInBibliography",
            // "nameattr_NameDelimiterOnBibliographyInCitation",
            // "nameattr_NameDelimiterOnCitationInBibliography",
            // "nameattr_NameDelimiterOnCitationInCitation",
            // "nameattr_NameDelimiterOnNamesInBibliography",
            // "nameattr_NameDelimiterOnNamesInCitation",
            // "nameattr_NameDelimiterOnStyleInBibliography",
            // "nameattr_NameDelimiterOnStyleInCitation",
            // "nameattr_NameFormOnBibliographyInBibliography",
            // "nameattr_NameFormOnBibliographyInCitation",
            // "nameattr_NameFormOnCitationInBibliography",
            // "nameattr_NameFormOnCitationInCitation",
            // "nameattr_NameFormOnNamesInBibliography",
            // "nameattr_NameFormOnNamesInCitation",
            // "nameattr_NameFormOnStyleInBibliography",
            // "nameattr_NameFormOnStyleInCitation",
            // "nameattr_NamesDelimiterOnBibliographyInBibliography",
            // "nameattr_NamesDelimiterOnBibliographyInCitation",
            // "nameattr_NamesDelimiterOnCitationInBibliography",
            // "nameattr_NamesDelimiterOnCitationInCitation",
            // "nameattr_NamesDelimiterOnNamesInBibliography",
            // "nameattr_NamesDelimiterOnNamesInCitation",
            // "nameattr_NamesDelimiterOnStyleInBibliography",
            // "nameattr_NamesDelimiterOnStyleInCitation",
            // "nameattr_SortSeparatorOnBibliographyInBibliography",
            // "nameattr_SortSeparatorOnBibliographyInCitation",
            // "nameattr_SortSeparatorOnCitationInBibliography",
            // "nameattr_SortSeparatorOnCitationInCitation",
            // "nameattr_SortSeparatorOnNamesInBibliography",
            // "nameattr_SortSeparatorOnNamesInCitation",
            // "nameattr_SortSeparatorOnStyleInBibliography",
            // "nameattr_SortSeparatorOnStyleInCitation",
            // "nameorder_Long",
            // "nameorder_LongNameAsSortDemoteDisplayAndSort",
            // "nameorder_LongNameAsSortDemoteNever",
            // "nameorder_Short",
            // "nameorder_ShortDemoteDisplayAndSort",
            // "nameorder_ShortNameAsSortDemoteNever",
            // "namespaces_NonNada3",
            // "number_EditionSort",
            // "number_FailingDelimiters",
            // "number_IsNumericWithAlpha",
            // "number_LeadingZeros",
            // "number_MixedPageRange",
            // "number_MixedText",
            // "number_NewOrdinalsEdition",
            // "number_NewOrdinalsWithGenderChange",
            // "number_OrdinalSpacing",
            // "number_PageFirst",
            // "number_PageRange",
            // "number_PlainHyphenOrEnDashAlwaysPlural",
            // "number_PreserveDelimiter",
            // "number_SeparateOrdinalNamespaces",
            // "number_SimpleNumberArabic",
            // "number_SimpleNumberOrdinalLong",
            // "number_SimpleNumberOrdinalShort",
            // "number_SimpleNumberRoman",
            // "number_StrangeError",
            // "page_Chicago",
            // "page_ChicagoWeird",
            // "page_Expand",
            // "page_ExpandWeirdComposite",
            // "page_Minimal",
            // "page_NoOption",
            "page_NumberPageFirst",
            // "page_PluralDetectWithEndash",
            // "page_WithLocaleAndWeirdDelimiter",
            // "plural_LabelForced",
            // "plural_NameLabelAlways",
            // "plural_NameLabelContextualPlural",
            // "plural_NameLabelContextualSingular",
            // "plural_NameLabelDefaultPlural",
            // "plural_NameLabelDefaultSingular",
            // "plural_NameLabelNever",
            "position_FalseInBibliography",
            // "position_FirstTrueOnlyOnce",
            // "position_IbidInText",
            // "position_IbidSeparateCiteSameNote",
            // "position_IbidWithLocator",
            // "position_IbidWithMultipleSoloCitesInBackref",
            // "position_IbidWithPrefixFullStop",
            "position_IbidWithSuffix",
            // "position_IfIbidIsTrueThenSubsequentIsTrue",
            // "position_IfIbidWithLocatorIsTrueThenIbidIsTrue",
            // "position_NearNoteFalse",
            // "position_NearNoteSameNote",
            // "position_NearNoteTrue",
            // "position_NearNoteUnsupported",
            // "position_NearNoteWithPlugin",
            // "position_ResetNoteNumbers",
            "position_TrueInCitation",
            // "punctuation_DateStripPeriods",
            // "punctuation_DefaultYearSuffixDelimiter",
            // "punctuation_DelimiterWithStripPeriodsAndSubstitute1",
            // "punctuation_DelimiterWithStripPeriodsAndSubstitute2",
            // "punctuation_DelimiterWithStripPeriodsAndSubstitute3",
            // "punctuation_DoNotSuppressColonAfterPeriod",
            // "punctuation_FieldDuplicates",
            // "punctuation_FrenchOrthography",
            // "punctuation_FullMontyField",
            // "punctuation_FullMontyPlain",
            // "punctuation_FullMontyQuotesIn",
            // "punctuation_FullMontyQuotesOut",
            // "punctuation_NoSuppressOfPeriodBeforeSemicolon",
            // "punctuation_OnMacro",
            // "punctuation_SemicolonDelimiter",
            // "punctuation_SuppressPrefixPeriodForDelimiterSemicolon",
            // "quotes_Punctuation",
            // "quotes_PunctuationNasty",
            // "quotes_PunctuationWithInnerQuote",
            // "quotes_QuotesUnderQuotesFalse",
            "simplespace_case1",
            // "sort_AguStyle",
            // "sort_AguStyleReverseGroups",
            // "sort_AuthorDateWithYearSuffix",
            // "sort_BibliographyCitationNumberDescending",
            // "sort_BibliographyCitationNumberDescendingSecondary",
            // "sort_BibliographyCitationNumberDescendingViaCompositeMacro",
            // "sort_BibliographyCitationNumberDescendingViaMacro",
            // "sort_BibliographyNosortOption",
            // "sort_BibliographyResortOnUpdate",
            // "sort_CaseInsensitiveBibliography",
            // "sort_CaseInsensitiveCitation",
            // "sort_ChangeInNameSort",
            // "sort_ChicagoYearSuffix1",
            // "sort_ChicagoYearSuffix2",
            // "sort_Citation",
            // "sort_CitationEdit",
            // "sort_CitationNumberPrimaryAscendingViaMacroBibliography",
            // "sort_CitationNumberPrimaryAscendingViaMacroCitation",
            // "sort_CitationNumberPrimaryAscendingViaVariableBibliography",
            // "sort_CitationNumberPrimaryAscendingViaVariableCitation",
            // "sort_CitationNumberPrimaryDescendingViaMacroBibliography",
            // "sort_CitationNumberPrimaryDescendingViaMacroCitation",
            // "sort_CitationNumberPrimaryDescendingViaVariableBibliography",
            // "sort_CitationNumberPrimaryDescendingViaVariableCitation",
            // "sort_CitationNumberSecondaryAscendingViaMacroBibliography",
            // "sort_CitationNumberSecondaryAscendingViaMacroCitation",
            // "sort_CitationNumberSecondaryAscendingViaVariableBibliography",
            // "sort_CitationNumberSecondaryAscendingViaVariableCitation",
            // "sort_CitationSecondaryKey",
            // "sort_CitationUnsorted",
            // "sort_CiteGroupDelimiter",
            // "sort_ConditionalMacroDates",
            // "sort_DaleDalebout",
            // "sort_DateMacroSortWithSecondFieldAlign",
            // "sort_DateVariable",
            // "sort_DateVariableMixedElementsAscendingA",
            // "sort_DateVariableMixedElementsAscendingB",
            // "sort_DateVariableMixedElementsDescendingA",
            // "sort_DateVariableMixedElementsDescendingB",
            // "sort_DateVariableRange",
            // "sort_DateVariableRangeMixed",
            // "sort_DropNameLabelInSort",
            // "sort_EtAlUseLast",
            // "sort_FamilyOnly",
            // "sort_GroupedByAuthorstring",
            // "sort_LatinUnicode",
            // "sort_LeadingA",
            // "sort_LeadingApostropheOnNameParticle",
            // "sort_LocalizedDateLimitedParts",
            // "sort_NameImplicitSortOrderAndForm",
            // "sort_NameParticleInNameSortFalse",
            // "sort_NameParticleInNameSortTrue",
            // "sort_NameVariable",
            // "sort_NamesUseLast",
            // "sort_NumberOfAuthorsAsKey",
            // "sort_OmittedBibRefMixedNumericStyle",
            // "sort_OmittedBibRefNonNumericStyle",
            // "sort_Quotes",
            // "sort_RangeUnaffected",
            // "sort_SeparateAuthorsAndOthers",
            // "sort_StatusFieldAscending",
            // "sort_StatusFieldDescending",
            // "sort_StripMarkup",
            // "sort_SubstituteTitle",
            // "sort_TestInheritance",
            // "sort_VariousNameMacros1",
            // "sort_VariousNameMacros2",
            // "sort_VariousNameMacros3",
            // "sort_WithAndInOneEntry",
            // "sortseparator_SortSeparatorEmpty",
            // "substitute_RepeatedNamesOk",
            // "substitute_SharedMacro",
            // "substitute_SuppressOrdinaryVariable",
            // "testers_FirstAutoGeneratedZoteroPluginTest",
            // "testers_SecondAutoGeneratedZoteroPluginTest",
            "textcase_AfterQuote",
            // we do not support <span class="nocase"/>
            // "textcase_CapitalizeAll",
            // we do not support <span class="nocase"/>
            // "textcase_CapitalizeFirst",
            // we do not support <span class="nocase"/>
            // "textcase_CapitalizeFirstWithDecor",
            // Title case should work at the beginning of a new sentence
            // "textcase_CapitalsUntouched",
            // we do not support <span class="xxx"/>
            // "textcase_ImplicitNocase",
            "textcase_InQuotes",
            // why should we not make the last character uppercase?
            // "textcase_LastChar",
            // we do not support <span class="nocase"/>
            // "textcase_Lowercase",
            // invalid style XML
            // "textcase_NoSpaceBeforeApostrophe",
            "textcase_NonEnglishChars",
            "textcase_RepeatedTitleBug",
            // we do not support <span class="nocase"/>
            // "textcase_SentenceCapitalization",
            // not supported yet
            // "textcase_SkipNameParticlesInTitleCase",
            "textcase_StopWordBeforeHyphen",
            // we do not support <span class="nocase"/>
            // "textcase_TitleCapitalization",
            // we do not support <span class="nocase"/>
            // "textcase_TitleCapitalization2",
            "textcase_TitleCaseNonEnglish",
            "textcase_TitleCaseNonEnglish2",
            // we do not support <span class="nocase"/>
            // "textcase_TitleCaseWithFinalNocase",
            "textcase_TitleCaseWithHyphens",
            "textcase_TitleCaseWithInitials",
            "textcase_TitleCaseWithNonBreakSpace",
            "textcase_TitleWithCircumflex",
            "textcase_TitleWithEmDash",
            "textcase_TitleWithEnDash",
            // we do not support <span class="nocase"/>
            // "textcase_Uppercase",
            // we do not support roman numbers yet
            // "textcase_UppercaseNumber",
            "unicode_NonBreakingSpace",
            // "variables_ContainerTitleShort",
            // "variables_ContainerTitleShort2",
            "variables_ShortForm",
            "variables_TitleShortOnAbbrevWithTitle",
            "variables_TitleShortOnAbbrevWithTitleCondition",
            "variables_TitleShortOnAbbrevWithTitleGroup",
            "variables_TitleShortOnShortTitleNoTitle",
            "variables_TitleShortOnShortTitleNoTitleCondition",
            "variables_TitleShortOnShortTitleNoTitleGroup",
            // "virtual_PageFirst"
    };
}

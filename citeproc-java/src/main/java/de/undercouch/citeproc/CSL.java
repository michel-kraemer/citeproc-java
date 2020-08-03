package de.undercouch.citeproc;

import de.undercouch.citeproc.csl.CSLCitation;
import de.undercouch.citeproc.csl.CSLCitationItem;
import de.undercouch.citeproc.csl.CSLCitationItemBuilder;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CSLItemDataBuilder;
import de.undercouch.citeproc.csl.CitationIDIndexPair;
import de.undercouch.citeproc.csl.internal.GeneratedCitation;
import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.SSort;
import de.undercouch.citeproc.csl.internal.SStyle;
import de.undercouch.citeproc.csl.internal.format.Format;
import de.undercouch.citeproc.csl.internal.format.HtmlFormat;
import de.undercouch.citeproc.csl.internal.format.TextFormat;
import de.undercouch.citeproc.csl.internal.locale.LLocale;
import de.undercouch.citeproc.helper.CSLUtils;
import de.undercouch.citeproc.helper.json.JsonBuilder;
import de.undercouch.citeproc.helper.json.MapJsonBuilderFactory;
import de.undercouch.citeproc.output.Bibliography;
import de.undercouch.citeproc.output.Citation;
import de.undercouch.citeproc.output.SecondFieldAlign;
import de.undercouch.citeproc.script.ScriptRunner;
import de.undercouch.citeproc.script.ScriptRunnerException;
import de.undercouch.citeproc.script.ScriptRunnerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * <p>The citation processor.</p>
 *
 * <p>In order to use the processor in your application you first have to
 * create an {@link ItemDataProvider} that provides citation item data. For
 * example, the following dummy provider returns always the same data:</p>
 *
 * <blockquote><pre>
 * public class MyItemProvider implements ItemDataProvider {
 *     &#64;Override
 *     public CSLItemData retrieveItem(String id) {
 *         return new CSLItemDataBuilder()
 *             .id(id)
 *             .type(CSLType.ARTICLE_JOURNAL)
 *             .title("A dummy journal article")
 *             .author("John", "Smith")
 *             .issued(2013, 9, 6)
 *             .containerTitle("Dummy journal")
 *             .build();
 *     }
 *
 *     &#64;Override
 *     public String[] getIds() {
 *         String ids[] = {"ID-0", "ID-1", "ID-2"};
 *         return ids;
 *     }
 * }</pre></blockquote>
 *
 * Now you can instantiate the CSL processor.
 *
 * <blockquote><pre>
 * CSL citeproc = new CSL(new MyItemProvider(), "ieee");
 * citeproc.setOutputFormat("html");</pre></blockquote>
 *
 * <h3>Ad-hoc usage</h3>
 *
 * <p>You may also use {@link #makeAdhocBibliography(String, CSLItemData...)} or
 * {@link #makeAdhocBibliography(String, String, CSLItemData...)} to create
 * ad-hoc bibliographies from CSL items.</p>
 *
 * <blockquote><pre>
 * CSLItemData item = new CSLItemDataBuilder()
 *     .type(CSLType.WEBPAGE)
 *     .title("citeproc-java: A Citation Style Language (CSL) processor for Java")
 *     .author("Michel", "Kraemer")
 *     .issued(2014, 7, 13)
 *     .URL("http://michel-kraemer.github.io/citeproc-java/")
 *     .accessed(2014, 7, 13)
 *     .build();
 *
 * String bibl = CSL.makeAdhocBibliography("ieee", item).makeString();</pre></blockquote>
 *
 * <h3>Cleanup</h3>
 *
 * <p>Make sure to call {@link #close()} to release all resources associated
 * with the CSL processor when you're done with it. We recommend using a
 * try-with-resources statement:</p>
 *
 * <blockquote><pre>
 * try (CSL citeproc = new CSL(new MyItemProvider(), "ieee")) {
 *     citeproc.setOutputFormat("html");
 *     ...
 * }</pre></blockquote>
 *
 * <h3>Thread-safety</h3>
 *
 * <p>Please note that this class is not thread-safe. However, shared resources
 * are held in memory, so constructing new instances is generally rather cheap.
 * If your settings do not change you may cache instances of this class in a
 * <code>ThreadLocal</code>.</p>
 *
 * <blockquote><pre>
 * ThreadLocal&lt;CSL&gt; csl = new ThreadLocal&lt;CSL&gt;() {
 *     &#64;Override
 *     protected CSL initialValue() {
 *         return new CSL(itemDataProvider, style, lang);
 *     }
 * };</pre></blockquote>
 *
 * Please remember to reset the <code>ThreadLocal</code> if you don't need
 * the <code>CSL</code> instance anymore to avoid memory leaks.
 *
 * @author Michel Kraemer
 */
public class CSL implements Closeable {
    /**
     * A thread-local holding a JavaScript runner that can
     * be shared amongst multiple instances of this class
     */
    private static ThreadLocal<ScriptRunner> sharedRunner = new ThreadLocal<>();

    /**
     * A JavaScript runner used to execute citeproc-js
     */
    private final ScriptRunner runner;

    /**
     * The underlying citeproc-js engine
     */
    private final Object engine;

    /**
     * The output format
     * @see #setOutputFormat(String)
     */
    private String outputFormatName = "html";

    /**
     * The output format
     */
    private Format outputFormat = new HtmlFormat();

    /**
     * {@code true} if the processor should convert URLs and DOIs in the output
     * to links.
     * @see #setConvertLinks(boolean)
     */
    private boolean convertLinks = false;

    /**
     * {@code true} if the new experimental pure Java CSL processor should be used
     */
    private final boolean experimentalMode;

    /**
     * The CSL style used to render citations and bibliographies
     */
    private final SStyle style;

    /**
     * The localization data used to render citations and bibliographies
     */
    private final LLocale locale;

    /**
     * An object that provides citation item data
     */
    private final ItemDataProvider itemDataProvider;

    /**
     * Citation items registered through {@link #registerCitationItems(String...)}
     */
    private final Map<String, CSLItemData> registeredItems = new LinkedHashMap<>();

    /**
     * Contains the same items as {@link #registeredItems} but sorted
     */
    private final List<CSLItemData> sortedItems = new ArrayList<>();

    /**
     * A list of generated citations sorted by their index
     */
    private List<GeneratedCitation> generatedCitations = new ArrayList<>();

    /**
     * Constructs a new citation processor
     * @param itemDataProvider an object that provides citation item data
     * @param style the citation style to use. May either be a serialized
     * XML representation of the style or a style's name such as <code>ieee</code>.
     * In the latter case, the processor loads the style from the classpath (e.g.
     * <code>/ieee.csl</code>)
     * @throws IOException if the underlying JavaScript files or the CSL style
     * could not be loaded
     */
    public CSL(ItemDataProvider itemDataProvider, String style) throws IOException {
        this(itemDataProvider, style, false);
    }

    /**
     * Constructs a new citation processor
     * @param itemDataProvider an object that provides citation item data
     * @param style the citation style to use. May either be a serialized
     * XML representation of the style or a style's name such as <code>ieee</code>.
     * In the latter case, the processor loads the style from the classpath (e.g.
     * <code>/ieee.csl</code>)
     * @param experimentalMode {@code true} if the new experimental pure Java
     * CSL processor should be used
     * @throws IOException if the underlying JavaScript files or the CSL style
     * could not be loaded
     */
    public CSL(ItemDataProvider itemDataProvider, String style,
            boolean experimentalMode) throws IOException {
        this(itemDataProvider, new DefaultLocaleProvider(),
                new DefaultAbbreviationProvider(), null, style, "en-US",
                false, experimentalMode);
    }

    /**
     * Constructs a new citation processor
     * @param itemDataProvider an object that provides citation item data
     * @param abbreviationProvider an object that provides abbreviations
     * @param style the citation style to use. May either be a serialized
     * XML representation of the style or a style's name such as <code>ieee</code>.
     * In the latter case, the processor loads the style from the classpath (e.g.
     * <code>/ieee.csl</code>)
     * @throws IOException if the underlying JavaScript files or the CSL style
     * could not be loaded
     */
    public CSL(ItemDataProvider itemDataProvider, AbbreviationProvider abbreviationProvider,
            String style) throws IOException {
        this(itemDataProvider, abbreviationProvider, style, "en-US");
    }

    /**
     * Constructs a new citation processor
     * @param itemDataProvider an object that provides citation item data
     * @param style the citation style to use. May either be a serialized
     * XML representation of the style or a style's name such as <code>ieee</code>.
     * In the latter case, the processor loads the style from the classpath (e.g.
     * <code>/ieee.csl</code>)
     * @param lang an RFC 4646 identifier for the citation locale (e.g. <code>en-US</code>)
     * @throws IOException if the underlying JavaScript files or the CSL style
     * could not be loaded
     */
    public CSL(ItemDataProvider itemDataProvider, String style, String lang) throws IOException {
        this(itemDataProvider, new DefaultLocaleProvider(), style, lang, false);
    }

    /**
     * Constructs a new citation processor
     * @param itemDataProvider an object that provides citation item data
     * @param abbreviationProvider an object that provides abbreviations
     * @param style the citation style to use. May either be a serialized
     * XML representation of the style or a style's name such as <code>ieee</code>.
     * In the latter case, the processor loads the style from the classpath (e.g.
     * <code>/ieee.csl</code>)
     * @param lang an RFC 4646 identifier for the citation locale (e.g. <code>en-US</code>)
     * @throws IOException if the underlying JavaScript files or the CSL style
     * could not be loaded
     */
    public CSL(ItemDataProvider itemDataProvider, AbbreviationProvider abbreviationProvider,
            String style, String lang) throws IOException {
        this(itemDataProvider, new DefaultLocaleProvider(), abbreviationProvider,
                style, lang, false);
    }

    /**
     * Constructs a new citation processor
     * @param itemDataProvider an object that provides citation item data
     * @param localeProvider an object that provides CSL locales
     * @param style the citation style to use. May either be a serialized
     * XML representation of the style or a style's name such as <code>ieee</code>.
     * In the latter case, the processor loads the style from the classpath (e.g.
     * <code>/ieee.csl</code>)
     * @param lang an RFC 4646 identifier for the citation locale (e.g. <code>en-US</code>)
     * @param forceLang true if the given locale should overwrite any default locale
     * @throws IOException if the underlying JavaScript files or the CSL style
     * could not be loaded
     */
    public CSL(ItemDataProvider itemDataProvider, LocaleProvider localeProvider,
            String style, String lang, boolean forceLang) throws IOException {
        this(itemDataProvider, localeProvider, new DefaultAbbreviationProvider(),
                style, lang, forceLang);
    }

    /**
     * Constructs a new citation processor
     * @param itemDataProvider an object that provides citation item data
     * @param localeProvider an object that provides CSL locales
     * @param abbreviationProvider an object that provides abbreviations
     * @param style the citation style to use. May either be a serialized
     * XML representation of the style or a style's name such as <code>ieee</code>.
     * In the latter case, the processor loads the style from the classpath (e.g.
     * <code>/ieee.csl</code>)
     * @param lang an RFC 4646 identifier for the citation locale (e.g. <code>en-US</code>)
     * @param forceLang true if the given locale should overwrite any default locale
     * @throws IOException if the underlying JavaScript files or the CSL style
     * could not be loaded
     */
    public CSL(ItemDataProvider itemDataProvider, LocaleProvider localeProvider,
            AbbreviationProvider abbreviationProvider, String style,
            String lang, boolean forceLang) throws IOException {
        this(itemDataProvider, localeProvider, abbreviationProvider, null,
                style, lang, forceLang);
    }

    /**
     * Constructs a new citation processor
     * @param itemDataProvider an object that provides citation item data
     * @param localeProvider an object that provides CSL locales
     * @param abbreviationProvider an object that provides abbreviations
     * @param variableWrapper an object that decorates rendered items
     * @param style the citation style to use. May either be a serialized
     * XML representation of the style or a style's name such as <code>ieee</code>.
     * In the latter case, the processor loads the style from the classpath (e.g.
     * <code>/ieee.csl</code>)
     * @param lang an RFC 4646 identifier for the citation locale (e.g. <code>en-US</code>)
     * @param forceLang true if the given locale should overwrite any default locale
     * @throws IOException if the underlying JavaScript files or the CSL style
     * could not be loaded
     */
    public CSL(ItemDataProvider itemDataProvider, LocaleProvider localeProvider,
            AbbreviationProvider abbreviationProvider, VariableWrapper variableWrapper,
            String style, String lang, boolean forceLang) throws IOException {
        this(itemDataProvider, localeProvider, abbreviationProvider,
                variableWrapper, style, lang, forceLang, false);
    }

    /**
     * Constructs a new citation processor
     * @param itemDataProvider an object that provides citation item data
     * @param localeProvider an object that provides CSL locales
     * @param abbreviationProvider an object that provides abbreviations
     * @param variableWrapper an object that decorates rendered items
     * @param style the citation style to use. May either be a serialized
     * XML representation of the style or a style's name such as <code>ieee</code>.
     * In the latter case, the processor loads the style from the classpath (e.g.
     * <code>/ieee.csl</code>)
     * @param lang an RFC 4646 identifier for the citation locale (e.g. <code>en-US</code>)
     * @param forceLang true if the given locale should overwrite any default locale
     * @param experimentalMode {@code true} if the new experimental pure Java
     * CSL processor should be used
     * @throws IOException if the underlying JavaScript files or the CSL style
     * could not be loaded
     */
    public CSL(ItemDataProvider itemDataProvider, LocaleProvider localeProvider,
            AbbreviationProvider abbreviationProvider, VariableWrapper variableWrapper,
            String style, String lang, boolean forceLang,
            boolean experimentalMode) throws IOException {
        this.experimentalMode = experimentalMode;

        // load style if needed
        if (!isStyle(style)) {
            style = loadStyle(style);
        }

        if (experimentalMode) {
            this.runner = null;
            this.engine = null;
            this.itemDataProvider = itemDataProvider;

            // TODO parse style and locale directly from URL if possible
            // TODO instead of loading them into strings first

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            try {
                builder = factory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                throw new IOException("Could not create document builder", e);
            }

            // load style
            Document styleDocument;
            try {
                styleDocument = builder.parse(new InputSource(
                        new StringReader(style)));
            } catch (SAXException e) {
                throw new IOException("Could not parse style", e);
            }
            this.style = new SStyle(styleDocument);

            // load locale
            String strLocale = localeProvider.retrieveLocale(lang);
            Document localeDocument;
            try {
                localeDocument = builder.parse(new InputSource(
                        new StringReader(strLocale)));
            } catch (SAXException e) {
                throw new IOException("Could not parse locale", e);
            }
            LLocale locale = new LLocale(localeDocument);

            if (this.style.getLocale() != null &&
                    (this.style.getLocale().getLang() == null ||
                            (this.style.getLocale().getLang().getLanguage().equals(locale.getLang().getLanguage()) &&
                                    (this.style.getLocale().getLang().getCountry().isEmpty() ||
                                            this.style.getLocale().getLang().getCountry().equals(locale.getLang().getCountry()))))) {
                // additional localization data in the style file overrides or
                // augments the data from the locale file
                this.locale = locale.merge(this.style.getLocale());
            } else {
                this.locale = locale;
            }
        } else {
            this.style = null;
            this.locale = null;
            this.itemDataProvider = null;

            this.runner = getRunner();

            // initialize engine
            try {
                this.engine = this.runner.callMethod("makeCsl", Object.class,
                        style, lang, forceLang, this.runner, itemDataProvider,
                        localeProvider, abbreviationProvider, variableWrapper);
            } catch (ScriptRunnerException e) {
                throw new IllegalArgumentException("Could not parse arguments", e);
            }
        }
    }

    /**
     * Calculates a list of supported output formats
     * @return the formats
     * @throws IOException if the underlying JavaScript files could not be loaded
     */
    public static List<String> getSupportedOutputFormats() throws IOException {
        ScriptRunner runner = getRunner();
        return getSupportedOutputFormats(runner);
    }

    @SuppressWarnings("unchecked")
    private static List<String> getSupportedOutputFormats(ScriptRunner runner) {
        try {
            return runner.callMethod("getSupportedFormats", List.class);
        } catch (ScriptRunnerException e) {
            throw new IllegalStateException("Could not get supported formats", e);
        }
    }

    private static Set<String> getAvailableFiles(String prefix,
            String knownName, String extension) throws IOException {
        Set<String> result = new LinkedHashSet<>();

        // first load a file that is known to exist
        String name = prefix + knownName + "." + extension;
        URL knownUrl = CSL.class.getResource("/" + name);
        if (knownUrl != null) {
            String path = knownUrl.getPath();
            // get the jar file containing the file
            if (path.endsWith(".jar!/" + name)) {
                String jarPath = path.substring(0, path.length() - name.length() - 2);
                URI jarUri;
                try {
                    jarUri = new URI(jarPath);
                } catch (URISyntaxException e) {
                    // ignore
                    return result;
                }
                try (ZipFile zip = new ZipFile(new File(jarUri))) {
                    Enumeration<? extends ZipEntry> entries = zip.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry e = entries.nextElement();
                        if (e.getName().endsWith("." + extension) &&
                                (prefix.isEmpty() || e.getName().startsWith(prefix))) {
                            result.add(e.getName().substring(
                                    prefix.length(), e.getName().length() - 4));
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Calculates a list of available citation styles
     * @return the list
     * @throws IOException if the citation styles could not be loaded
     */
    public static Set<String> getSupportedStyles() throws IOException {
        return getAvailableFiles("", "ieee", "csl");
    }

    /**
     * Get version of the underlying citeproc-js CSL processor
     * @return the version string
     * @throws IOException if the version could not be obtained
     */
    public static String getCiteprocJsVersion() throws IOException {
        ScriptRunner runner = getRunner();
        try {
            return runner.callMethod("getCiteprocJsVersion", String.class);
        } catch (ScriptRunnerException e) {
            throw new IllegalStateException("Could not get citeproc-js version", e);
        }
    }

    /**
     * Get the name of the underlying JavaScript engine
     * @return the engine name
     * @throws IOException if the JavaScript engine could not be initialized
     */
    public static String getJavaScriptEngineName() throws IOException {
        return getRunner().getName();
    }

    /**
     * Get the version of the underlying JavaScript engine
     * @return the version string
     * @throws IOException if the JavaScript engine could not be initialized
     */
    public static String getJavaScriptEngineVersion() throws IOException {
        return getRunner().getVersion();
    }

    /**
     * Checks if a given citation style is supported
     * @param style the citation style's name
     * @return true if the style is supported, false otherwise
     */
    public static boolean supportsStyle(String style) {
        String styleFileName = style;
        if (!styleFileName.endsWith(".csl")) {
            styleFileName = styleFileName + ".csl";
        }
        if (!styleFileName.startsWith("/")) {
            styleFileName = "/" + styleFileName;
        }
        URL url = CSL.class.getResource(styleFileName);
        return (url != null);
    }

    /**
     * Calculates a list of available citation locales
     * @return the list
     * @throws IOException if the citation locales could not be loaded
     */
    public static Set<String> getSupportedLocales() throws IOException {
        Set<String> locales = getAvailableFiles("locales-", "en-US", "xml");
        try {
            @SuppressWarnings("unchecked")
            List<String> baseLocales = getRunner().callMethod(
                    "getBaseLocales", List.class);
            locales.addAll(baseLocales);
        } catch (ScriptRunnerException e) {
            // ignore. don't add base locales
        }
        return locales;
    }

    /**
     * Gets or initializes the shared script runner {@link #sharedRunner}
     * @return the runner
     * @throws IOException if bundles scripts could not be loaded
     */
    private static ScriptRunner getRunner() throws IOException {
        if (sharedRunner.get() == null) {
            // create JavaScript runner
            ScriptRunner runner = ScriptRunnerFactory.createRunner();

            // load bundled scripts
            try {
                runner.loadScript(CSL.class.getResource("dump.js"));
                runner.loadScript(CSL.class.getResource("citeproc.js"));
                runner.loadScript(CSL.class.getResource("loadsys.js"));
            } catch (ScriptRunnerException e) {
                // should never happen because bundled JavaScript files
                // should be OK indeed
                throw new RuntimeException("Invalid bundled javascript file", e);
            }

            sharedRunner.set(runner);
        }
        return sharedRunner.get();
    }

    /**
     * @return the JavaScript runner used to execute citeproc-js
     */
    protected ScriptRunner getScriptRunner() {
        return runner;
    }

    /**
     * @return the underlying citeproc-js engine
     */
    protected Object getEngine() {
        return engine;
    }

    /**
     * Checks if the given String contains the serialized XML representation
     * of a style
     * @param style the string to examine
     * @return true if the String is XML, false otherwise
     */
    private boolean isStyle(String style) {
        for (int i = 0; i < style.length(); ++i) {
            char c = style.charAt(i);
            if (!Character.isWhitespace(c)) {
                return (c == '<');
            }
        }
        return false;
    }

    /**
     * Loads a CSL style from the classpath. For example, if the given name
     * is <code>ieee</code> this method will load the file <code>/ieee.csl</code>
     * @param styleName the style's name
     * @return the serialized XML representation of the style
     * @throws IOException if the style could not be loaded
     */
    private String loadStyle(String styleName) throws IOException {
        URL url;
        if (styleName.startsWith("http://") || styleName.startsWith("https://")) {
            try {
                // try to load matching style from classpath
                return loadStyle(styleName.substring(styleName.lastIndexOf('/') + 1));
            } catch (FileNotFoundException e) {
                // there is no matching style in classpath
                url = new URL(styleName);
            }
        } else {
            // normalize file name
            if (!styleName.endsWith(".csl")) {
                styleName = styleName + ".csl";
            }
            if (!styleName.startsWith("/")) {
                styleName = "/" + styleName;
            }

            // try to find style in classpath
            url = getClass().getResource(styleName);
            if (url == null) {
                throw new FileNotFoundException("Could not find style in "
                        + "classpath: " + styleName);
            }
        }

        // load style
        String result = CSLUtils.readURLToString(url, "UTF-8");

        // handle dependent styles
        if (isDependent(result)) {
            String independentParentLink;
            try {
                independentParentLink = getIndependentParentLink(result);
            } catch (ParserConfigurationException | IOException | SAXException e) {
                throw new IOException("Could not load independent parent style", e);
            }
            if (independentParentLink == null) {
                throw new IOException("Dependent style does not have an "
                        + "independent parent");
            }
            return loadStyle(independentParentLink);
        }

        return result;
    }

    /**
     * Test if the given string represents a dependent style
     * @param style the style
     * @return true if the string is a dependent style, false otherwise
     */
    private boolean isDependent(String style) {
        if (!style.trim().startsWith("<")) {
            return false;
        }
        Pattern p = Pattern.compile("rel\\s*=\\s*\"\\s*independent-parent\\s*\"");
        Matcher m = p.matcher(style);
        return m.find();
    }

    /**
     * Parse a string representing a dependent parent style and
     * get link to its independent parent style
     * @param style the dependent style
     * @return the link to the parent style or <code>null</code> if the link
     * could not be found
     * @throws ParserConfigurationException if the XML parser could not be created
     * @throws IOException if the string could not be read
     * @throws SAXException if the string could not be parsed
     */
    public String getIndependentParentLink(String style)
            throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource src = new InputSource(new StringReader(style));
        Document doc = builder.parse(src);
        NodeList links = doc.getElementsByTagName("link");
        for (int i = 0; i < links.getLength(); ++i) {
            Node n = links.item(i);
            Node relAttr = n.getAttributes().getNamedItem("rel");
            if (relAttr != null) {
                if ("independent-parent".equals(relAttr.getTextContent())) {
                    Node hrefAttr = n.getAttributes().getNamedItem("href");
                    if (hrefAttr != null) {
                        return hrefAttr.getTextContent();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Sets the processor's output format
     * @param format the format (one of {@code "html"}, {@code "text"},
     * {@code "asciidoc"}, {@code "fo"}, or {@code "rtf"}
     */
    public void setOutputFormat(String format) {
        if (experimentalMode) {
            if ("text".equals(format)) {
                outputFormat = new TextFormat();
            } else if ("html".equals(format)) {
                outputFormat = new HtmlFormat();
            } else {
                throw new IllegalArgumentException("Experimental mode " +
                        "only supports `text' and `html' output formats " +
                        "at the moment.");
            }
            outputFormat.setConvertLinks(convertLinks);
            return;
        }

        if (!getSupportedOutputFormats(runner).contains(format)) {
            throw new IllegalArgumentException("Unknown output format: " + format);
        }

        try {
            runner.callMethod(engine, "setOutputFormat", format);
            outputFormatName = format;
        } catch (ScriptRunnerException e) {
            throw new IllegalArgumentException("Could not set output format", e);
        }
    }

    /**
     * Specifies if the processor should convert URLs and DOIs in the output
     * to links. How links are created depends on the output format that has
     * been set with {@link #setOutputFormat(String)}
     * @param convert true if URLs and DOIs should be converted to links
     */
    public void setConvertLinks(boolean convert) {
        if (experimentalMode) {
            convertLinks = convert;
            outputFormat.setConvertLinks(convert);
        } else {
            try {
                runner.callMethod("setConvertLinks", engine, convert);
            } catch (ScriptRunnerException e) {
                throw new IllegalArgumentException("Could not set option", e);
            }
        }
    }

    /**
     * Enables the abbreviation list with the given name. The processor will
     * call {@link AbbreviationProvider#getAbbreviations(String)} with the
     * given String to get the abbreviations that should be used from here on.
     * @param name the name of the abbreviation list to enable
     */
    public void setAbbreviations(String name) {
        if (experimentalMode) {
            throw new IllegalArgumentException("Experimental mode does not " +
                    "support abbreviations yet.");
        }

        try {
            runner.callMethod(engine, "setAbbreviations", name);
        } catch (ScriptRunnerException e) {
            throw new IllegalArgumentException("Could not set abbreviations", e);
        }
    }

    /**
     * Fetches the item data for the given citation items and adds it to
     * {@link #registeredItems}. Also, sorts the items according to the sorting
     * specified in the style's bibliography element and stores the result in
     * {@link #sortedItems}. If the style does not have a bibliography element
     * or no sorting is specified, the items will just be appended to
     * {@link #sortedItems}. In addition, the method updates any items already
     * stored in {@link #sortedItems} and coming after the generated ones.
     * Updated items will be returned in the given {@code updatedItems} set
     * (unless it is {@code null}).
     * @param ids the IDs of the citation items to register
     * @param updatedItems an empty set that will be filled with the citation
     * items the method had to update (may be {@code null})
     * @param unsorted {@code true} if any sorting specified in the style
     * should be ignored
     * @return a list of registered citation item data
     */
    private List<CSLItemData> registerItems(String[] ids,
            Set<CSLItemData> updatedItems, boolean unsorted) {
        List<CSLItemData> result = new ArrayList<>();
        SSort.SortComparator comparator = null;

        for (String id : ids) {
            // check if item has already been registered
            CSLItemData itemData = registeredItems.get(id);
            if (itemData != null) {
                result.add(itemData);
                continue;
            }

            // fetch item data
            itemData = itemDataProvider.retrieveItem(id);
            if (itemData == null) {
                throw new IllegalArgumentException("Missing citation " +
                        "item with ID: " + id);
            }

            // register item
            if (unsorted || style.getBibliography() == null ||
                    style.getBibliography().getSort() == null) {
                // We don't have to sort. Add item to the end of the list.
                itemData = new CSLItemDataBuilder(itemData)
                        .citationNumber(String.valueOf(registeredItems.size() + 1))
                        .build();
                sortedItems.add(itemData);
            } else {
                // We have to sort. Find insert point.
                if (comparator == null) {
                    comparator = style.getBibliography().getSort()
                            .comparator(style, locale);
                }
                int i = Collections.binarySearch(sortedItems, itemData, comparator);
                if (i < 0) {
                    i = -(i + 1);
                } else {
                    // binarySearch thinks we found the item in the list but
                    // this is impossible. It's more likely that the comparator
                    // returned 0 because no key was given or it did not yield
                    // sensible results. Just append the item to the list.
                    i = sortedItems.size();
                }

                // determine citation number depending on sort direction
                int citationNumber;
                int citationNumberDirection = comparator.getCitationNumberDirection();
                if (citationNumberDirection > 0) {
                    citationNumber = i + 1;
                } else {
                    citationNumber = sortedItems.size() + 1 - i;
                }

                // create new item data with citation data and add it to
                // the list of sorted items
                itemData = new CSLItemDataBuilder(itemData)
                        .citationNumber(String.valueOf(citationNumber))
                        .build();
                sortedItems.add(i, itemData);

                // determine if we need to update the following items or
                // the preceding ones (depending on the sort direction)
                IntStream idStream;
                if (citationNumberDirection > 0) {
                    idStream = IntStream.range(i + 1, sortedItems.size());
                } else {
                    int e = i;
                    idStream = IntStream.range(0, e).map(n -> e - 1 - n);
                }

                // update the other items if necessary
                idStream.forEach(j -> {
                    CSLItemData item2 = sortedItems.get(j);

                    // determine new citation number
                    int citationNumber2;
                    if (citationNumberDirection > 0) {
                        citationNumber2 = j + 1;
                    } else {
                        citationNumber2 = sortedItems.size() - j;
                    }

                    // create new item data with new citation number
                    item2 = new CSLItemDataBuilder(item2)
                            .citationNumber(String.valueOf(citationNumber2))
                            .build();

                    // overwrite existing item data
                    sortedItems.set(j, item2);
                    registeredItems.put(item2.getId(), item2);

                    // store updated item
                    if (updatedItems != null) {
                        updatedItems.add(item2);
                    }
                });
            }

            // save registered item data
            registeredItems.put(itemData.getId(), itemData);
            result.add(itemData);
        }

        return result;
    }

    /**
     * Introduces the given citation IDs to the processor. The processor will
     * call {@link ItemDataProvider#retrieveItem(String)} for each ID to get
     * the respective citation item. The retrieved items will be added to the
     * bibliography, so you don't have to call {@link #makeCitation(String...)}
     * for each of them anymore.
     * @param ids the IDs to register
     * @throws IllegalArgumentException if one of the given IDs refers to
     * citation item data that does not exist
     */
    public void registerCitationItems(String... ids) {
        if (experimentalMode) {
            registerCitationItems(ids, false);
        } else {
            try {
                runner.callMethod(engine, "updateItems", new Object[] { ids });
            } catch (ScriptRunnerException e) {
                throw new IllegalArgumentException("Could not update items", e);
            }
        }
    }

    /**
     * Introduces the given citation IDs to the processor. The processor will
     * call {@link ItemDataProvider#retrieveItem(String)} for each ID to get
     * the respective citation item. The retrieved items will be added to the
     * bibliography, so you don't have to call {@link #makeCitation(String...)}
     * for each of them anymore.
     * @param ids the IDs to register
     * @param unsorted true if items should not be sorted in the bibliography
     * @throws IllegalArgumentException if one of the given IDs refers to
     * citation item data that does not exist
     */
    public void registerCitationItems(String[] ids, boolean unsorted) {
        if (experimentalMode) {
            registeredItems.clear();
            sortedItems.clear();
            registerItems(ids, null, unsorted);
        } else {
            try {
                runner.callMethod(engine, "updateItems", ids, unsorted);
            } catch (ScriptRunnerException e) {
                throw new IllegalArgumentException("Could not update items", e);
            }
        }
    }

    /**
     * Get an unmodifiable collection of all citation items that have been
     * registered with the processor so far
     * @return the registered citation items
     */
    public Collection<CSLItemData> getRegisteredItems() {
        if (experimentalMode) {
            return Collections.unmodifiableCollection(sortedItems);
        } else {
            List<?> r;
            try {
                r = getScriptRunner().callMethod("getRefList", List.class, getEngine());
            } catch (ScriptRunnerException e) {
                throw new IllegalArgumentException("Could not get registered citation items", e);
            }

            List<CSLItemData> result = new ArrayList<>();
            for (Object o : r) {
                @SuppressWarnings("unchecked")
                Map<String, Object> m = (Map<String, Object>)o;
                @SuppressWarnings("unchecked")
                Map<String, Object> ref = (Map<String, Object>)m.get("ref");
                result.add(CSLItemData.fromJson(ref));
            }
            return result;
        }
    }

    /**
     * Generates citation strings that can be inserted into the text. The
     * method calls {@link ItemDataProvider#retrieveItem(String)} for each of the given
     * IDs to request the corresponding citation item. Additionally, it saves
     * the IDs, so {@link #makeBibliography()} will generate a bibliography
     * that only consists of the retrieved citation items.
     * @param ids IDs of citation items for which strings should be generated
     * @return citations strings that can be inserted into the text
     * @throws IllegalArgumentException if one of the given IDs refers to
     * citation item data that does not exist
     */
    public List<Citation> makeCitation(String... ids) {
        CSLCitationItem[] items = new CSLCitationItem[ids.length];
        for (int i = 0; i < ids.length; ++i) {
            items[i] = new CSLCitationItem(ids[i]);
        }
        return makeCitation(new CSLCitation(items));
    }

    /**
     * Generates citation strings that can be inserted into the text. The
     * method calls {@link ItemDataProvider#retrieveItem(String)} for each item in the
     * given set to request the corresponding citation item data. Additionally,
     * it saves the requested citation IDs, so {@link #makeBibliography()} will
     * generate a bibliography that only consists of the retrieved items.
     * @param citation a set of citation items for which strings should be generated
     * @return citations strings that can be inserted into the text
     * @throws IllegalArgumentException if the given set of citation items
     * refers to citation item data that does not exist
     */
    public List<Citation> makeCitation(CSLCitation citation) {
        return makeCitation(citation, null, null);
    }

    /**
     * Perform steps to prepare the given citation for rendering. Register
     * citation items and sort them. Return a prepared citation that can be
     * passed to {@link #renderCitation(CSLCitation)}
     * @param citation the citation to render
     * @param updatedItems an empty set that will be filled with citation
     * items that had to be updated while rendering the given one (may be
     * {@code null})
     * @return the prepared citation
     */
    private CSLCitation preRenderCitation(CSLCitation citation,
            Set<CSLItemData> updatedItems) {
        // get item IDs
        int len = citation.getCitationItems().length;
        String[] itemIds = new String[len];
        CSLCitationItem[] items = citation.getCitationItems();
        for (int i = 0; i < len; i++) {
            CSLCitationItem item = items[i];
            itemIds[i] = item.getId();
        }

        // register items
        List<CSLItemData> registeredItems = registerItems(itemIds,
                updatedItems, false);

        // prepare items
        CSLCitationItem[] preparedItems = new CSLCitationItem[len];
        for (int i = 0; i < len; i++) {
            CSLCitationItem item = items[i];
            CSLItemData itemData = registeredItems.get(i);

            // overwrite locator
            if (item.getLocator() != null) {
                itemData = new CSLItemDataBuilder(itemData)
                        .locator(item.getLocator())
                        .build();
            }

            preparedItems[i] = new CSLCitationItemBuilder(item)
                    .itemData(itemData)
                    .build();
        }

        // sort array of items
        boolean unsorted = false;
        if (citation.getProperties() != null &&
                citation.getProperties().getUnsorted() != null) {
            unsorted = citation.getProperties().getUnsorted();
        }
        if (!unsorted && style.getCitation().getSort() != null) {
            Comparator<CSLItemData> itemComparator =
                    style.getCitation().getSort().comparator(style, locale);
            Arrays.sort(preparedItems, (a, b) -> itemComparator.compare(
                    a.getItemData(), b.getItemData()));
        }

        return new CSLCitation(preparedItems,
                citation.getCitationID(), citation.getProperties());
    }

    /**
     * Render the given prepared citation
     * @param preparedCitation the citation to render. The citation must have
     * been prepared by {@link #preRenderCitation(CSLCitation, Set)}
     * @return the rendered string
     */
    private String renderCitation(CSLCitation preparedCitation) {
        // render items
        RenderContext ctx = new RenderContext(style, locale, null,
                preparedCitation, Collections.unmodifiableList(generatedCitations));
        style.getCitation().render(ctx);
        return outputFormat.formatCitation(ctx);
    }

    /**
     * Generates citation strings that can be inserted into the text. The
     * method calls {@link ItemDataProvider#retrieveItem(String)} for each item in the
     * given set to request the corresponding citation item data. Additionally,
     * it saves the requested citation IDs, so {@link #makeBibliography()} will
     * generate a bibliography that only consists of the retrieved items.
     * @param citation a set of citation items for which strings should be generated
     * @param citationsPre citations that precede <code>citation</code>
     * @param citationsPost citations that come after <code>citation</code>
     * @return citations strings that can be inserted into the text
     * @throws IllegalArgumentException if the given set of citation items
     * refers to citation item data that does not exist
     */
    public List<Citation> makeCitation(CSLCitation citation,
            List<CitationIDIndexPair> citationsPre,
            List<CitationIDIndexPair> citationsPost) {
        if (experimentalMode) {
            if (citationsPre != null || citationsPost != null) {
                throw new IllegalArgumentException("Experimental mode does " +
                        "not support preceding or succeeding citations. This " +
                        "method will likely be removed in a future release.");
            }

            Set<CSLItemData> updatedItems = new LinkedHashSet<>();
            CSLCitation preparedCitation = preRenderCitation(citation, updatedItems);
            String text = renderCitation(preparedCitation);

            // re-render updated citations
            List<Citation> result = new ArrayList<>();
            if (!updatedItems.isEmpty()) {
                List<GeneratedCitation> oldGeneratedCitations = generatedCitations;
                generatedCitations = new ArrayList<>(oldGeneratedCitations.size());
                for (int i = 0; i < oldGeneratedCitations.size(); i++) {
                    GeneratedCitation gc = oldGeneratedCitations.get(i);

                    boolean needsUpdate = false;
                    for (CSLItemData updatedItemData : updatedItems) {
                        for (CSLCitationItem item : gc.getOriginal().getCitationItems()) {
                            if (item.getId().equals(updatedItemData.getId())) {
                                needsUpdate = true;
                                break;
                            }
                        }
                    }

                    if (!needsUpdate) {
                        generatedCitations.add(gc);
                        continue;
                    }

                    // prepare citation again (!)
                    CSLCitation upc = preRenderCitation(gc.getOriginal(), null);

                    // render it again
                    String ut = renderCitation(upc);
                    if (!ut.equals(gc.getGenerated().getText())) {
                        // render result was different
                        Citation uc = new Citation(i, ut);
                        generatedCitations.add(new GeneratedCitation(
                                gc.getOriginal(), upc, uc));
                        result.add(uc);
                    }
                }
            }

            // generate citation
            Citation generatedCitation = new Citation(generatedCitations.size(), text);
            generatedCitations.add(new GeneratedCitation(citation,
                    preparedCitation, generatedCitation));
            result.add(generatedCitation);

            return result;
        } else {
            return makeCitationLegacy(citation, citationsPre, citationsPost);
        }
    }

    private List<Citation> makeCitationLegacy(CSLCitation citation,
            List<CitationIDIndexPair> citationsPre,
            List<CitationIDIndexPair> citationsPost) {
        List<?> r;
        try {
            if (citationsPre == null && citationsPost == null) {
                r = runner.callMethod(engine, "appendCitationCluster",
                        List.class, citation);
            } else {
                r = runner.callMethod(engine, "processCitationCluster",
                        List.class, citation, citationsPre, citationsPost);
                r = runner.convert(r.get(1), List.class);
            }
        } catch (ScriptRunnerException e) {
            throw new IllegalArgumentException("Could not make citation", e);
        }

        List<Citation> result = new ArrayList<>();
        for (Object o : r) {
            if (o instanceof Map) {
                o = runner.convert(o, List.class);
            }
            if (o instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> i = (List<Object>)o;
                if (i.get(0) instanceof Number && i.get(1) instanceof CharSequence) {
                    int index = ((Number)i.get(0)).intValue();
                    String text = i.get(1).toString();
                    result.add(new Citation(index, text));
                }
            }
        }

        return result;
    }

    /**
     * Generates a bibliography for the registered citations
     * @return the bibliography
     */
    public Bibliography makeBibliography() {
        return makeBibliography(null);
    }

    /**
     * Generates a bibliography for the registered citations. Depending
     * on the selection mode selects, includes, or excludes bibliography
     * items whose fields and field values match the fields and field values
     * from the given example item data objects.
     * @param mode the selection mode
     * @param selection the example item data objects that contain
     * the fields and field values to match
     * @return the bibliography
     */
    public Bibliography makeBibliography(SelectionMode mode, CSLItemData... selection) {
        return makeBibliography(mode, selection, null);
    }

    /**
     * <p>Generates a bibliography for the registered citations. Depending
     * on the selection mode selects, includes, or excludes bibliography
     * items whose fields and field values match the fields and field values
     * from the given example item data objects.</p>
     * <p>Note: This method will be deprecated in the next release.</p>
     * @param mode the selection mode
     * @param selection the example item data objects that contain
     * the fields and field values to match
     * @param quash regardless of the item data in {@code selection}
     * skip items if all fields/values from this list match
     * @return the bibliography
     */
    public Bibliography makeBibliography(SelectionMode mode,
            CSLItemData[] selection, CSLItemData[] quash) {
        if (experimentalMode) {
            return makeBibliography(item -> {
                boolean include = true;

                if (selection != null) {
                    switch (mode) {
                        case INCLUDE:
                            include = false;
                            for (CSLItemData s : selection) {
                                if (itemDataEqualsAny(item, s)) {
                                    include = true;
                                    break;
                                }
                            }
                            break;

                        case EXCLUDE:
                            for (CSLItemData s : selection) {
                                if (itemDataEqualsAny(item, s)) {
                                    include = false;
                                    break;
                                }
                            }
                            break;

                        case SELECT:
                            for (CSLItemData s : selection) {
                                if (!itemDataEqualsAny(item, s)) {
                                    include = false;
                                    break;
                                }
                            }
                            break;
                    }
                }

                if (include && quash != null) {
                    boolean match = true;
                    for (CSLItemData s : quash) {
                        if (!itemDataEqualsAny(item, s)) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        include = false;
                    }
                }

                return include;
            });
        } else {
            return makeBibliographyLegacy(mode, selection, quash);
        }
    }

    /**
     * Generates a bibliography for registered citations
     * @param filter a function to apply to each registered citation item to
     * determine if it should be included in the bibliography or not (may
     * be {@code null} if all items should be included)
     * @return the bibliography
     */
    public Bibliography makeBibliography(Predicate<CSLItemData> filter) {
        if (!experimentalMode) {
            if (filter == null) {
                return makeBibliographyLegacy(null, null, null);
            }
            throw new IllegalStateException("Making a bibliography with a " +
                    "filter is not supported in legacy mode. Use " +
                    "CSL.makeBibliography(SelectionMode, CSLItemData[], CSLItemData[])" +
                    "instead.");
        }

        List<String> entries = new ArrayList<>();
        for (CSLItemData item : sortedItems) {
            if (filter != null && !filter.test(item)) {
                continue;
            }

            RenderContext ctx = new RenderContext(style, locale, item);
            style.getBibliography().render(ctx);

            if (!ctx.getResult().isEmpty()) {
                entries.add(outputFormat.formatBibliographyEntry(ctx));
            }
        }

        return outputFormat.makeBibliography(entries.toArray(new String[0]),
                style.getBibliography());
    }

    private Bibliography makeBibliographyLegacy(SelectionMode mode,
            CSLItemData[] selection, CSLItemData[] quash) {
        List<?> r;
        try {
            if ((selection == null || mode == null) && quash == null) {
                r = runner.callMethod(engine, "makeBibliography", List.class);
            } else {
                Map<String, Object> args = new HashMap<>();
                if (selection != null && mode != null) {
                    args.put(mode.toString(), selectionToList(selection));
                }
                if (quash != null) {
                    args.put("quash", selectionToList(quash));
                }
                r = runner.callMethod(engine, "makeBibliography",
                        List.class, args);
            }
        } catch (ScriptRunnerException e) {
            throw new IllegalArgumentException("Could not make bibliography", e);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> fpm = (Map<String, Object>)r.get(0);
        @SuppressWarnings("unchecked")
        List<CharSequence> entriesList = runner.convert(r.get(1), List.class);

        String[] entries = new String[entriesList.size()];
        for (int i = 0; i < entries.length; ++i) {
            entries[i] = entriesList.get(i).toString();
        }

        int maxOffset = getFromMap(fpm, "maxoffset", 0);
        int entrySpacing = getFromMap(fpm, "entryspacing", 0);
        int lineSpacing = getFromMap(fpm, "linespacing", 0);
        boolean hangingIndent = getFromMap(fpm, "hangingindent", false);
        boolean done = getFromMap(fpm, "done", false);
        List<?> srcEntryIds = runner.convert(fpm.get("entry_ids"), List.class);
        List<String> dstEntryIds = new ArrayList<>();
        for (Object o : srcEntryIds) {
            if (o instanceof Map) {
                o = runner.convert(o, List.class);
            }
            if (o instanceof Collection) {
                Collection<?> oc = (Collection<?>)o;
                for (Object oco : oc) {
                    dstEntryIds.add(oco.toString());
                }
            } else {
                dstEntryIds.add(o.toString());
            }
        }
        String[] entryIds = dstEntryIds.toArray(new String[0]);
        SecondFieldAlign secondFieldAlign = SecondFieldAlign.FALSE;
        Object sfa = fpm.get("second-field-align");
        if (sfa != null) {
            secondFieldAlign = SecondFieldAlign.fromString(sfa.toString());
        }
        String bibStart = getFromMap(fpm, "bibstart", "");
        String bibEnd = getFromMap(fpm, "bibend", "");

        // special treatment for some output formats
        if (outputFormatName.equals("fo")) {
            // make reasonable margin for an average character width
            String em = Math.max(2.5, maxOffset * 0.6) + "em";
            for (int i = 0; i < entries.length; ++i) {
                entries[i] = entries[i].replace("$$$__COLUMN_WIDTH_1__$$$", em);
            }
        }

        return new Bibliography(entries, bibStart, bibEnd, entryIds,
                maxOffset, entrySpacing, lineSpacing, hangingIndent,
                done, secondFieldAlign);
    }

    /**
     * Converts the given CSLItemData objects to a list of field/value pairs
     * that can be used to filter bibliography items. Only those fields will
     * be included that are actually set in the given objects.
     * @param selection the CSLItemData objects
     * @return the list of field/value pairs
     */
    private List<Map<String, Object>> selectionToList(CSLItemData[] selection) {
        MapJsonBuilderFactory mjbf = new MapJsonBuilderFactory();
        List<Map<String, Object>> sl = new ArrayList<>();
        for (CSLItemData item : selection) {
            JsonBuilder jb = mjbf.createJsonBuilder();
            @SuppressWarnings("unchecked")
            Map<String, Object> mi = (Map<String, Object>)item.toJson(jb);
            for (Map.Entry<String, Object> e : mi.entrySet()) {
                Object v = e.getValue();
                if (e.getKey().equals("id") && v instanceof String &&
                        ((String)v).startsWith("-GEN-")) {
                    // skip generated ids
                    continue;
                }
                if (v instanceof Collection) {
                    Collection<?> coll = (Collection<?>)v;
                    if (coll.isEmpty()) {
                        putSelectionFieldValue(sl, e, "");
                    } else {
                        for (Object ao : coll) {
                            putSelectionFieldValue(sl, e, ao);
                        }
                    }
                } else if (v instanceof Map && ((Map<?, ?>)v).isEmpty()) {
                    putSelectionFieldValue(sl, e, "");
                } else {
                    putSelectionFieldValue(sl, e, v);
                }
            }
        }
        return sl;
    }

    private void putSelectionFieldValue(List<Map<String, Object>> sl,
            Map.Entry<String, Object> e, Object v) {
        Map<String, Object> sf = new HashMap<>(2);
        sf.put("field", e.getKey());
        sf.put("value", v);
        sl.add(sf);
    }

    private int getFromMap(Map<String, Object> m, String key, int def) {
        Number r = (Number)m.get(key);
        if (r == null) {
            return def;
        }
        return r.intValue();
    }

    private boolean getFromMap(Map<String, Object> m, String key, boolean def) {
        Object r = m.get(key);
        if (r == null) {
            return def;
        }
        if (r instanceof CharSequence) {
            return Boolean.parseBoolean(r.toString());
        }
        return (Boolean)r;
    }

    private String getFromMap(Map<String, Object> m, String key, String def) {
        String r = (String)m.get(key);
        if (r == null) {
            r = def;
        }
        return r;
    }

    /**
     * Resets the processor's state
     */
    public void reset() {
        if (experimentalMode) {
            outputFormatName = "html";
            outputFormat = new HtmlFormat();
            convertLinks = false;
            registeredItems.clear();
            sortedItems.clear();
            generatedCitations.clear();
        } else {
            try {
                runner.callMethod(engine, "restoreProcessorState");
            } catch (ScriptRunnerException e) {
                throw new IllegalArgumentException("Could not reset processor state", e);
            }
        }
    }

    /**
     * Creates an ad hoc bibliography from the given citation items using the
     * <code>"html"</code> output format. Calling this method is rather
     * expensive as it initializes the CSL processor. If you need to create
     * bibliographies multiple times in your application you should create
     * the processor yourself and cache it if necessary.
     * @param style the citation style to use. May either be a serialized
     * XML representation of the style or a style's name such as <code>ieee</code>.
     * In the latter case, the processor loads the style from the classpath (e.g.
     * <code>/ieee.csl</code>)
     * @param items the citation items to add to the bibliography
     * @return the bibliography
     * @throws IOException if the underlying JavaScript files or the CSL style
     * could not be loaded
     * @see #makeAdhocBibliography(String, String, CSLItemData...)
     */
    public static Bibliography makeAdhocBibliography(String style, CSLItemData... items)
            throws IOException {
        return makeAdhocBibliography(style, "html", items);
    }

    /**
     * Creates an ad hoc bibliography from the given citation items. Calling
     * this method is rather expensive as it initializes the CSL processor.
     * If you need to create bibliographies multiple times in your application
     * you should create the processor yourself and cache it if necessary.
     * @param style the citation style to use. May either be a serialized
     * XML representation of the style or a style's name such as <code>ieee</code>.
     * In the latter case, the processor loads the style from the classpath (e.g.
     * <code>/ieee.csl</code>)
     * @param outputFormat the processor's output format (one of
     * <code>"html"</code>, <code>"text"</code>, <code>"asciidoc"</code>,
     * <code>"fo"</code>, or <code>"rtf"</code>)
     * @param items the citation items to add to the bibliography
     * @return the bibliography
     * @throws IOException if the underlying JavaScript files or the CSL style
     * could not be loaded
     */
    public static Bibliography makeAdhocBibliography(String style, String outputFormat,
            CSLItemData... items) throws IOException {
        return makeAdhocBibliography(style, outputFormat, false, items);
    }

    /**
     * Creates an ad hoc bibliography from the given citation items. Calling
     * this method is rather expensive as it initializes the CSL processor.
     * If you need to create bibliographies multiple times in your application
     * you should create the processor yourself and cache it if necessary.
     * @param style the citation style to use. May either be a serialized
     * XML representation of the style or a style's name such as <code>ieee</code>.
     * In the latter case, the processor loads the style from the classpath (e.g.
     * <code>/ieee.csl</code>)
     * @param outputFormat the processor's output format (one of
     * <code>"html"</code>, <code>"text"</code>, <code>"asciidoc"</code>,
     * <code>"fo"</code>, or <code>"rtf"</code>)
     * @param experimentalMode {@code true} if the new experimental pure Java
     * CSL processor should be used
     * @param items the citation items to add to the bibliography
     * @return the bibliography
     * @throws IOException if the underlying JavaScript files or the CSL style
     * could not be loaded
     */
    public static Bibliography makeAdhocBibliography(String style, String outputFormat,
            boolean experimentalMode, CSLItemData... items) throws IOException {
        ItemDataProvider provider = new ListItemDataProvider(items);
        try (CSL csl = new CSL(provider, style, experimentalMode)) {
            csl.setOutputFormat(outputFormat);

            String[] ids = new String[items.length];
            for (int i = 0; i < items.length; ++i) {
                ids[i] = items[i].getId();
            }
            csl.registerCitationItems(ids);

            return csl.makeBibliography();
        }
    }

    @Override
    public void close() {
        if (!experimentalMode) {
            runner.release(engine);
        }
    }

    /**
     * Removes the shared JavaScript runner from the internal thread-local.
     * Only call this method if you want to force re-initialization of the
     * shared runner or if you want to gracefully shutdown the JVM and clean up
     * behind you (usually not necessary).
     */
    public static void removeThreadLocals() {
        sharedRunner.remove();
    }

    /**
     * Test if any of the attributes of {@code b} match the ones of {@code a}.
     * Note: This method will be deprecated in the next release
     * @param a the first object
     * @param b the object to match against
     * @return {@code true} if the match succeeds
     */
    private static boolean itemDataEqualsAny(CSLItemData a, CSLItemData b) {
        if (a == b) {
            return true;
        }
        if (b == null) {
            return false;
        }

        if (b.getId() != null && Objects.equals(a.getId(), b.getId())) {
            return true;
        }
        if (b.getType() != null && Objects.equals(a.getType(), b.getType())) {
            return true;
        }
        if (b.getCategories() != null && Arrays.equals(a.getCategories(), b.getCategories())) {
            return true;
        }
        if (b.getLanguage() != null && Objects.equals(a.getLanguage(), b.getLanguage())) {
            return true;
        }
        if (b.getJournalAbbreviation() != null && Objects.equals(a.getJournalAbbreviation(), b.getJournalAbbreviation())) {
            return true;
        }
        if (b.getShortTitle() != null && Objects.equals(a.getShortTitle(), b.getShortTitle())) {
            return true;
        }
        if (b.getAuthor() != null && Arrays.equals(a.getAuthor(), b.getAuthor())) {
            return true;
        }
        if (b.getCollectionEditor() != null && Arrays.equals(a.getCollectionEditor(), b.getCollectionEditor())) {
            return true;
        }
        if (b.getComposer() != null && Arrays.equals(a.getComposer(), b.getComposer())) {
            return true;
        }
        if (b.getContainerAuthor() != null && Arrays.equals(a.getContainerAuthor(), b.getContainerAuthor())) {
            return true;
        }
        if (b.getDirector() != null && Arrays.equals(a.getDirector(), b.getDirector())) {
            return true;
        }
        if (b.getEditor() != null && Arrays.equals(a.getEditor(), b.getEditor())) {
            return true;
        }
        if (b.getEditorialDirector() != null && Arrays.equals(a.getEditorialDirector(), b.getEditorialDirector())) {
            return true;
        }
        if (b.getInterviewer() != null && Arrays.equals(a.getInterviewer(), b.getInterviewer())) {
            return true;
        }
        if (b.getIllustrator() != null && Arrays.equals(a.getIllustrator(), b.getIllustrator())) {
            return true;
        }
        if (b.getOriginalAuthor() != null && Arrays.equals(a.getOriginalAuthor(), b.getOriginalAuthor())) {
            return true;
        }
        if (b.getRecipient() != null && Arrays.equals(a.getRecipient(), b.getRecipient())) {
            return true;
        }
        if (b.getReviewedAuthor() != null && Arrays.equals(a.getReviewedAuthor(), b.getReviewedAuthor())) {
            return true;
        }
        if (b.getTranslator() != null && Arrays.equals(a.getTranslator(), b.getTranslator())) {
            return true;
        }
        if (b.getAccessed() != null && Objects.equals(a.getAccessed(), b.getAccessed())) {
            return true;
        }
        if (b.getContainer() != null && Objects.equals(a.getContainer(), b.getContainer())) {
            return true;
        }
        if (b.getEventDate() != null && Objects.equals(a.getEventDate(), b.getEventDate())) {
            return true;
        }
        if (b.getIssued() != null && Objects.equals(a.getIssued(), b.getIssued())) {
            return true;
        }
        if (b.getOriginalDate() != null && Objects.equals(a.getOriginalDate(), b.getOriginalDate())) {
            return true;
        }
        if (b.getSubmitted() != null && Objects.equals(a.getSubmitted(), b.getSubmitted())) {
            return true;
        }
        if (b.getAbstrct() != null && Objects.equals(a.getAbstrct(), b.getAbstrct())) {
            return true;
        }
        if (b.getAnnote() != null && Objects.equals(a.getAnnote(), b.getAnnote())) {
            return true;
        }
        if (b.getArchive() != null && Objects.equals(a.getArchive(), b.getArchive())) {
            return true;
        }
        if (b.getArchiveLocation() != null && Objects.equals(a.getArchiveLocation(), b.getArchiveLocation())) {
            return true;
        }
        if (b.getArchivePlace() != null && Objects.equals(a.getArchivePlace(), b.getArchivePlace())) {
            return true;
        }
        if (b.getAuthority() != null && Objects.equals(a.getAuthority(), b.getAuthority())) {
            return true;
        }
        if (b.getCallNumber() != null && Objects.equals(a.getCallNumber(), b.getCallNumber())) {
            return true;
        }
        if (b.getChapterNumber() != null && Objects.equals(a.getChapterNumber(), b.getChapterNumber())) {
            return true;
        }
        if (b.getCitationNumber() != null && Objects.equals(a.getCitationNumber(), b.getCitationNumber())) {
            return true;
        }
        if (b.getCitationLabel() != null && Objects.equals(a.getCitationLabel(), b.getCitationLabel())) {
            return true;
        }
        if (b.getCollectionNumber() != null && Objects.equals(a.getCollectionNumber(), b.getCollectionNumber())) {
            return true;
        }
        if (b.getCollectionTitle() != null && Objects.equals(a.getCollectionTitle(), b.getCollectionTitle())) {
            return true;
        }
        if (b.getContainerTitle() != null && Objects.equals(a.getContainerTitle(), b.getContainerTitle())) {
            return true;
        }
        if (b.getContainerTitleShort() != null && Objects.equals(a.getContainerTitleShort(), b.getContainerTitleShort())) {
            return true;
        }
        if (b.getDimensions() != null && Objects.equals(a.getDimensions(), b.getDimensions())) {
            return true;
        }
        if (b.getDOI() != null && Objects.equals(a.getDOI(), b.getDOI())) {
            return true;
        }
        if (b.getEdition() != null && Objects.equals(a.getEdition(), b.getEdition())) {
            return true;
        }
        if (b.getEvent() != null && Objects.equals(a.getEvent(), b.getEvent())) {
            return true;
        }
        if (b.getEventPlace() != null && Objects.equals(a.getEventPlace(), b.getEventPlace())) {
            return true;
        }
        if (b.getFirstReferenceNoteNumber() != null && Objects.equals(a.getFirstReferenceNoteNumber(), b.getFirstReferenceNoteNumber())) {
            return true;
        }
        if (b.getGenre() != null && Objects.equals(a.getGenre(), b.getGenre())) {
            return true;
        }
        if (b.getISBN() != null && Objects.equals(a.getISBN(), b.getISBN())) {
            return true;
        }
        if (b.getISSN() != null && Objects.equals(a.getISSN(), b.getISSN())) {
            return true;
        }
        if (b.getIssue() != null && Objects.equals(a.getIssue(), b.getIssue())) {
            return true;
        }
        if (b.getJurisdiction() != null && Objects.equals(a.getJurisdiction(), b.getJurisdiction())) {
            return true;
        }
        if (b.getKeyword() != null && Objects.equals(a.getKeyword(), b.getKeyword())) {
            return true;
        }
        if (b.getLocator() != null && Objects.equals(a.getLocator(), b.getLocator())) {
            return true;
        }
        if (b.getMedium() != null && Objects.equals(a.getMedium(), b.getMedium())) {
            return true;
        }
        if (b.getNote() != null && Objects.equals(a.getNote(), b.getNote())) {
            return true;
        }
        if (b.getNumber() != null && Objects.equals(a.getNumber(), b.getNumber())) {
            return true;
        }
        if (b.getNumberOfPages() != null && Objects.equals(a.getNumberOfPages(), b.getNumberOfPages())) {
            return true;
        }
        if (b.getNumberOfVolumes() != null && Objects.equals(a.getNumberOfVolumes(), b.getNumberOfVolumes())) {
            return true;
        }
        if (b.getOriginalPublisher() != null && Objects.equals(a.getOriginalPublisher(), b.getOriginalPublisher())) {
            return true;
        }
        if (b.getOriginalPublisherPlace() != null && Objects.equals(a.getOriginalPublisherPlace(), b.getOriginalPublisherPlace())) {
            return true;
        }
        if (b.getOriginalTitle() != null && Objects.equals(a.getOriginalTitle(), b.getOriginalTitle())) {
            return true;
        }
        if (b.getPage() != null && Objects.equals(a.getPage(), b.getPage())) {
            return true;
        }
        if (b.getPageFirst() != null && Objects.equals(a.getPageFirst(), b.getPageFirst())) {
            return true;
        }
        if (b.getPMCID() != null && Objects.equals(a.getPMCID(), b.getPMCID())) {
            return true;
        }
        if (b.getPMID() != null && Objects.equals(a.getPMID(), b.getPMID())) {
            return true;
        }
        if (b.getPublisher() != null && Objects.equals(a.getPublisher(), b.getPublisher())) {
            return true;
        }
        if (b.getPublisherPlace() != null && Objects.equals(a.getPublisherPlace(), b.getPublisherPlace())) {
            return true;
        }
        if (b.getReferences() != null && Objects.equals(a.getReferences(), b.getReferences())) {
            return true;
        }
        if (b.getReviewedTitle() != null && Objects.equals(a.getReviewedTitle(), b.getReviewedTitle())) {
            return true;
        }
        if (b.getScale() != null && Objects.equals(a.getScale(), b.getScale())) {
            return true;
        }
        if (b.getSection() != null && Objects.equals(a.getSection(), b.getSection())) {
            return true;
        }
        if (b.getSource() != null && Objects.equals(a.getSource(), b.getSource())) {
            return true;
        }
        if (b.getStatus() != null && Objects.equals(a.getStatus(), b.getStatus())) {
            return true;
        }
        if (b.getTitle() != null && Objects.equals(a.getTitle(), b.getTitle())) {
            return true;
        }
        if (b.getTitleShort() != null && Objects.equals(a.getTitleShort(), b.getTitleShort())) {
            return true;
        }
        if (b.getURL() != null && Objects.equals(a.getURL(), b.getURL())) {
            return true;
        }
        if (b.getVersion() != null && Objects.equals(a.getVersion(), b.getVersion())) {
            return true;
        }
        if (b.getVolume() != null && Objects.equals(a.getVolume(), b.getVolume())) {
            return true;
        }

        return b.getYearSuffix() != null && Objects.equals(a.getYearSuffix(), b.getYearSuffix());
    }
}

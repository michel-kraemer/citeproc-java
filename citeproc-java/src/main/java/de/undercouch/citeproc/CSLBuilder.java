package de.undercouch.citeproc;

import java.io.IOException;

/**
 * <p>Builder for {@link CSL} objects.</p>
 *
 * <p>Please read the javadoc of the {@link CSL} class for more information
 * about its usage.</p>
 *
 * @author Michel Kraemer
 */
public class CSLBuilder {
    private ItemDataProvider itemDataProvider;
    private LocaleProvider localeProvider = new DefaultLocaleProvider();
    private AbbreviationProvider abbreviationProvider = new DefaultAbbreviationProvider();
    private String style;
    private String lang;

    /**
     * Set the item data provider
     * @param itemDataProvider an object that provides citation item data
     * @return {@code this} builder
     */
    public CSLBuilder itemDataProvider(ItemDataProvider itemDataProvider) {
        this.itemDataProvider = itemDataProvider;
        return this;
    }

    /**
     * Set an optional locale provider
     * @param localeProvider an object that provides CSL locales
     * @return {@code this} builder
     */
    public CSLBuilder localeProvider(LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
        return this;
    }

    /**
     * Set an optional abbreviation provider
     * @param abbreviationProvider an object that provides abbreviations
     * @return {@code this} builder
     */
    public CSLBuilder abbreviationProvider(AbbreviationProvider abbreviationProvider) {
        this.abbreviationProvider = abbreviationProvider;
        return this;
    }

    /**
     * Set the citation style to use. This may either be a serialized XML
     * representation of the style or a style's name such as <code>ieee</code>.
     * In the latter case, the processor loads the style from the classpath (e.g.
     * <code>/ieee.csl</code>).
     * @param style the style
     * @return {@code this} builder
     */
    public CSLBuilder style(String style) {
        this.style = style;
        return this;
    }

    /**
     * Set an RFC 4646 identifier for the citation locale (e.g. <code>en-US</code>)
     * @param lang the language identifier
     * @return {@code this} builder
     */
    public CSLBuilder lang(String lang) {
        this.lang = lang;
        return this;
    }

    /**
     * Creates the {@code CSL} object with the configured parameters
     * @return the {@code CSL} object
     * @throws IOException if the CSL style could not be loaded
     */
    public CSL build() throws IOException {
        if (itemDataProvider == null) {
            throw new IllegalArgumentException("Cannot construct a CSL " +
                    "object without an ItemDataProvider");
        }
        if (style == null) {
            throw new IllegalArgumentException("Cannot construct a CSL " +
                    "object without a citation style");
        }
        return new CSL(itemDataProvider, localeProvider, abbreviationProvider, style, lang);
    }
}

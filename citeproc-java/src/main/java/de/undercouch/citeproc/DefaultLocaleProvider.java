package de.undercouch.citeproc;

import de.undercouch.citeproc.helper.CSLUtils;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of {@link LocaleProvider}. Loads locales from
 * the classpath.
 * @author Michel Kraemer
 */
public class DefaultLocaleProvider implements LocaleProvider {
    /**
     * A cache for the serialized XML of locales
     */
    private Map<String, String> locales = new HashMap<>();

    /**
     * Retrieves the serialized XML for the given locale from the classpath.
     * For example, if the locale is <code>en-US</code> this method loads
     * the file <code>/locales-en-US.xml</code> from the classpath.
     */
    @Override
    public String retrieveLocale(String lang) {
        String r = locales.get(lang);
        if (r == null) {
            try {
                URL u = getClass().getResource("/locales-" + lang + ".xml");
                if (u == null) {
                    throw new IllegalArgumentException("Unable to load locale " +
                            lang + ". Make sure you have a file called " +
                            "'/locales-" + lang + ".xml' at the root of your " +
                            "classpath. Did you add the CSL locale files to "
                            + "your classpath?");
                }
                r = CSLUtils.readURLToString(u, "UTF-8");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            locales.put(lang, r);
        }
        return r;
    }
}

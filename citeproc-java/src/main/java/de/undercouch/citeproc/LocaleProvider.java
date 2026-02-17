package de.undercouch.citeproc;

/**
 * Provides the serialized XML representation for a locale
 * @author Michel Kraemer
 */
public interface LocaleProvider {
    /**
     * Retrieves the serialized XML representation for a given locale
     * @param lang the locale identifier (e.g. "en" or "en-GB")
     * @return the serialized XML of the given locale or {@code null} if there
     * is no such locale
     */
    String retrieveLocale(String lang);
}

package de.undercouch.citeproc.csl.internal.format;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.SBibliography;
import de.undercouch.citeproc.output.Bibliography;

/**
 * An output format converts the result of a {@link RenderContext} to a
 * formatted {@link String}.
 * @author Michel Kraemer
 */
public interface Format {
    /**
     * Get the format's name
     * @return the name
     */
    String getName();

    /**
     * Specifies if URLs and DOIs should be converted to to links
     * @param convert true if URLs and DOIs should be converted to links
     */
    void setConvertLinks(boolean convert);

    /**
     * Format a citation
     * @param ctx the render context containing the citation to format
     * @return the formatted citation
     */
    String formatCitation(RenderContext ctx);

    /**
     * Format a bibliography entry
     * @param ctx the render context containing the bibliography entry
     * @param index the index of the entry
     * @return the formatted entry
     */
    String formatBibliographyEntry(RenderContext ctx, int index);

    /**
     * Create a {@link Bibliography} object with the given entries and
     * default parameters specified by this format
     * @param entries the entries
     * @param bibliographyElement the bibliography element from the style file
     * @return the {@link Bibliography} object
     */
    Bibliography makeBibliography(String[] entries, SBibliography bibliographyElement);
}

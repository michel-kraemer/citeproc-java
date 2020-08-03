package de.undercouch.citeproc.csl.internal;

import de.undercouch.citeproc.csl.CSLCitation;
import de.undercouch.citeproc.output.Citation;

/**
 * A helper class that holds prepared and generated citations for each
 * original citation
 * @author Michel Kraemer
 */
public class GeneratedCitation {
    private final CSLCitation original;
    private final CSLCitation prepared;
    private final Citation generated;

    /**
     * Default constructor
     * @param original the original citation
     * @param prepared the prepared citation
     * @param generated the generated citation
     */
    public GeneratedCitation(CSLCitation original, CSLCitation prepared,
            Citation generated) {
        this.original = original;
        this.prepared = prepared;
        this.generated = generated;
    }

    /**
     * @return the original citation
     */
    public CSLCitation getOriginal() {
        return original;
    }

    /**
     * @return the prepared citation
     */
    public CSLCitation getPrepared() {
        return prepared;
    }

    /**
     * @return the generated citation
     */
    public Citation getGenerated() {
        return generated;
    }
}

package de.undercouch.citeproc.csl;

import de.undercouch.citeproc.helper.json.JsonBuilder;
import de.undercouch.citeproc.helper.json.JsonObject;

import java.util.List;

/**
 * A pair containing a {@link CSLCitation}'s citation ID and an note index
 * from a {@link CSLCitation}'s {@link CSLProperties}
 * @author Michel Kraemer
 */
public class CitationIDIndexPair implements JsonObject {
    private final String citationId;
    private final int noteIndex;

    /**
     * Constructs a new pair
     * @param citationId the citation ID
     * @param noteIndex the index
     */
    public CitationIDIndexPair(String citationId, int noteIndex) {
        this.citationId = citationId;
        this.noteIndex = noteIndex;
    }

    /**
     * Constructs a new pair with the values from the given citation object
     * @param citation the citation object
     */
    public CitationIDIndexPair(CSLCitation citation) {
        this.citationId = citation.getCitationID();
        this.noteIndex = citation.getProperties().getNoteIndex();
    }

    /**
     * @return the citation ID
     */
    public String getCitationId() {
        return citationId;
    }

    /**
     * @return the note index
     */
    public int getNoteIndex() {
        return noteIndex;
    }

    @Override
    public Object toJson(JsonBuilder builder) {
        return builder.toJson(new Object[] { citationId, noteIndex });
    }

    /**
     * Converts a JSON array to a CitationIDIndexPair object.
     * @param arr the JSON array to convert
     * @return the converted CitationIDIndexPair object
     */
    public static CitationIDIndexPair fromJson(List<?> arr) {
        String citationId = (String)arr.get(0);
        int noteIndex = ((Number)arr.get(1)).intValue();
        return new CitationIDIndexPair(citationId, noteIndex);
    }
}

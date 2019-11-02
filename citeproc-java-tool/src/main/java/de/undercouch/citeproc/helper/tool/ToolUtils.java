package de.undercouch.citeproc.helper.tool;

import de.undercouch.citeproc.helper.Levenshtein;

import java.util.Collection;

/**
 * Utility methods for the CSL tool
 * @author Michel Kraemer
 */
public class ToolUtils {
    private ToolUtils() {
        // hidden constructor
    }

    /**
     * Finds strings similar to a string the user has entered and then
     * generates a "Did you mean one of these" message
     * @param available all possibilities
     * @param it the string the user has entered
     * @return the "Did you mean..." string or {@code null} if no string could
     * be found
     */
    public static String getDidYouMeanString(Collection<String> available, String it) {
        StringBuilder message = null;

        Collection<String> mins = Levenshtein.findSimilar(available, it);
        if (mins.size() > 0) {
            message = new StringBuilder();
            if (mins.size() == 1) {
                message.append("Did you mean this?");
            } else {
                message.append("Did you mean one of these?");
            }
            for (String m : mins) {
                message.append("\n\t").append(m);
            }
        }

        return message != null ? message.toString() : null;
    }
}

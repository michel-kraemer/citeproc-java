package de.undercouch.citeproc;

import de.undercouch.citeproc.csl.CSLDate;
import de.undercouch.citeproc.csl.CSLDateBuilder;
import de.undercouch.citeproc.script.ScriptRunner;
import de.undercouch.citeproc.script.ScriptRunnerException;
import de.undercouch.citeproc.script.ScriptRunnerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Map;

/**
 * An intelligent parser for date strings. This class is able to handle
 * a wide range of date formats (e.g. YYYY-mm-dd, YYYY/mm/dd, Month YYYY)
 * @author Michel Kraemer
 */
public class CSLDateParser {
    /**
     * A JavaScript runner used to execute citeproc-js
     */
    private final ScriptRunner runner;

    /**
     * The underlying date parser
     */
    private final Object parser;

    /**
     * Creates a new date parser
     */
    public CSLDateParser() {
        // create JavaScript runner
        runner = ScriptRunnerFactory.createRunner();

        // load bundles scripts
        try {
            runner.eval(new StringReader(
                    "var CSL = new function() {};" +
                            "CSL.DATE_PARTS_ALL = [\"year\", \"month\", \"day\", \"season\"];" +
                            "CSL.debug = function(msg) {};" +
                            "function getParser() { return new CSL.DateParser; }"
            ));
            runner.loadScript(getClass().getResource("dateparser.js"));
        } catch (IOException e) {
            // should never happen because bundled JavaScript files should be readable indeed
        } catch (ScriptRunnerException e) {
            // should never happen because bundled JavaScript files should be OK indeed
            throw new RuntimeException("Invalid bundled javascript file", e);
        }

        // initialize parser
        try {
            parser = runner.callMethod("getParser", Object.class);
        } catch (ScriptRunnerException e) {
            throw new IllegalArgumentException("Could not initialize date parser", e);
        }
    }

    /**
     * Parses a string to a date
     * @param str the string to parse
     * @return the parsed date
     */
    public CSLDate parse(String str) {
        Map<String, Object> res;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> m = runner.callMethod(
                    parser, "parseDateToArray", Map.class, str);
            res = m;
        } catch (ScriptRunnerException e) {
            throw new IllegalArgumentException("Could not update items", e);
        }

        CSLDate r = CSLDate.fromJson(res);
        if (r.getDateParts().length == 2 && Arrays.equals(r.getDateParts()[0], r.getDateParts()[1])) {
            r = new CSLDateBuilder(r).dateParts(r.getDateParts()[0]).build();
        }
        return r;
    }
}

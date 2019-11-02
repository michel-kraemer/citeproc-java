package de.undercouch.citeproc.bibtex;

import de.undercouch.citeproc.bibtex.internal.InternalNameLexer;
import de.undercouch.citeproc.bibtex.internal.InternalNameParser;
import de.undercouch.citeproc.bibtex.internal.InternalNameParser.NamesContext;
import de.undercouch.citeproc.csl.CSLName;
import de.undercouch.citeproc.csl.CSLNameBuilder;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * Parses a human's name to a {@link CSLName} object
 * @author Michel Kraemer
 */
public class NameParser {
    /**
     * Parses names to {@link CSLName} objects. Also handles strings
     * containing multiple names separated by {@code and}. The
     * method always returns at least one object, even if the given
     * names cannot be parsed. In this case the returned object just
     * contains a literal string.
     * @param names the names to parse
     * @return the {@link CSLName} objects (never {@code null} and never empty)
     */
    public static CSLName[] parse(String names) {
        ANTLRInputStream is = new ANTLRInputStream(names);
        InternalNameLexer lexer = new InternalNameLexer(is);
        lexer.removeErrorListeners(); // do not output errors to console
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        InternalNameParser parser = new InternalNameParser(tokens);
        parser.removeErrorListeners(); // do not output errors to console
        NamesContext ctx = parser.names();
        if (ctx.result.isEmpty() || ctx.exception != null) {
            // unparsable fall back to literal string
            return new CSLName[] { new CSLNameBuilder().literal(names).build() };
        }
        return ctx.result.toArray(new CSLName[0]);
    }
}

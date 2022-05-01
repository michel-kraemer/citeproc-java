package de.undercouch.citeproc.bibtex;

import de.undercouch.citeproc.bibtex.internal.InternalPageLexer;
import de.undercouch.citeproc.bibtex.internal.InternalPageParser;
import de.undercouch.citeproc.bibtex.internal.InternalPageParser.PagesContext;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * Parses pages
 * @author Michel Kraemer
 */
public class PageParser {
    /**
     * Parses a given page or range of pages. If the given string cannot
     * be parsed, the method will return a single page range with a literal string.
     * @param pages the page or range of pages
     * @return the parsed page or page ranges (never {@code null} and never empty)
     */
    public static PageRanges parse(String pages) {
        CharStream cs = CharStreams.fromString(pages);
        InternalPageLexer lexer = new InternalPageLexer(cs);
        lexer.removeErrorListeners(); // do not output errors to console
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        InternalPageParser parser = new InternalPageParser(tokens);
        parser.removeErrorListeners(); // do not output errors to console
        PagesContext ctx;
        try {
            ctx = parser.pages();
        } catch (NumberFormatException e) {
            ctx = null;
        }
        if (ctx == null || ctx.ranges == null || ctx.ranges.isEmpty() ||
                ctx.exception != null || parser.getNumberOfSyntaxErrors() > 0) {
            // unparsable fall back to literal string
            return new PageRanges(new PageRange(pages, null, null, null, false));
        }
        return ctx.ranges;
    }
}

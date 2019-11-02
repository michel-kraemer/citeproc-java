package de.undercouch.citeproc.bibtex;

import de.undercouch.citeproc.bibtex.internal.InternalPageLexer;
import de.undercouch.citeproc.bibtex.internal.InternalPageParser;
import de.undercouch.citeproc.bibtex.internal.InternalPageParser.PagesContext;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * Parses pages
 * @author Michel Kraemer
 */
public class PageParser {
    /**
     * Parses a given page or range of pages. If the given string cannot
     * be parsed, the method will return a page range with a literal string.
     * @param pages the page or range of pages
     * @return the parsed page or page range (never {@code null})
     */
    public static PageRange parse(String pages) {
        ANTLRInputStream is = new ANTLRInputStream(pages);
        InternalPageLexer lexer = new InternalPageLexer(is);
        lexer.removeErrorListeners(); // do not output errors to console
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        InternalPageParser parser = new InternalPageParser(tokens);
        parser.removeErrorListeners(); // do not output errors to console
        PagesContext ctx = parser.pages();
        return new PageRange(ctx.literal != null ? ctx.literal : pages, ctx.pageFrom, ctx.numberOfPages);
    }
}

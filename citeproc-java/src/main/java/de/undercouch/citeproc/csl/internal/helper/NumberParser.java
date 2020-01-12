package de.undercouch.citeproc.csl.internal.helper;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.Collections;
import java.util.List;

/**
 * Parses numbers with labels
 * @author Michel Kraemer
 */
public class NumberParser {
    /**
     * Parse numbers with labels to a list of elements. An element is a string
     * with an optional label as well as a flag specifying whether the string
     * contains multiple numbers (or a range) or a single number.
     * @param number the numbers and labels to parse
     * @return the list of elements
     */
    public static List<NumberElement> parse(String number) {
        CharStream cs = CharStreams.fromString(number);
        InternalNumberLexer lexer = new InternalNumberLexer(cs);
        lexer.removeErrorListeners(); // do not output errors to console
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        InternalNumberParser parser = new InternalNumberParser(tokens);
        parser.removeErrorListeners(); // do not output errors to console

        InternalNumberParser.NumbersContext ctx = parser.numbers();
        if (ctx.exception != null || parser.getNumberOfSyntaxErrors() > 0 ||
                ctx.elements.isEmpty()) {
            // fall back to literal string
            return Collections.singletonList(new NumberElement(number));
        }

        return ctx.elements;
    }
}

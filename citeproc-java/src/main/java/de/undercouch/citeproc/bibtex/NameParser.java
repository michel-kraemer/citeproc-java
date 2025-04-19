package de.undercouch.citeproc.bibtex;

import de.undercouch.citeproc.bibtex.internal.InternalNameLexer;
import de.undercouch.citeproc.bibtex.internal.InternalNameParser;
import de.undercouch.citeproc.bibtex.internal.InternalNameParser.NamesContext;
import de.undercouch.citeproc.csl.CSLName;
import de.undercouch.citeproc.csl.CSLNameBuilder;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses a human's name to a {@link CSLName} object
 * @author Michel Kraemer
 */
public class NameParser {
    /**
     * Pattern to match braced content in names
     */
    private static final Pattern BRACED_CONTENT_PATTERN = Pattern.compile("\\{([^{}]*)\\}");

    /**
     * Pattern to match "and" separators between names
     */
    private static final Pattern AND_SEPARATOR_PATTERN = Pattern.compile("\\s+and\\s+", Pattern.CASE_INSENSITIVE);

    /**
     * Parses names to {@link CSLName} objects. Also handles strings
     * containing multiple names separated by {@code and}. The
     * method always returns at least one object, even if the given
     * names cannot be parsed. In this case the returned object just
     * contains a literal string.
     * <p>
     * Special handling is provided for names enclosed in braces {},
     * which are meant to be preserved as-is in the output.
     *
     * @param names the names to parse
     * @return the {@link CSLName} objects (never {@code null} and never empty)
     */
    public static CSLName[] parse(String names) {
        if (names.contains("{")) {
            return parseWithBraces(names);
        }

        CharStream cs = CharStreams.fromString(names);
        InternalNameLexer lexer = new InternalNameLexer(cs);
        lexer.removeErrorListeners(); // do not output errors to console
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        InternalNameParser parser = new InternalNameParser(tokens);
        parser.removeErrorListeners(); // do not output errors to console
        NamesContext ctx = parser.names();
        if (ctx.result.isEmpty() || ctx.exception != null ||
                parser.getNumberOfSyntaxErrors() > 0) {
            // unparsable fall back to literal string
            return new CSLName[] { new CSLNameBuilder().literal(names).build() };
        }
        return ctx.result.toArray(new CSLName[0]);
    }

    private static CSLName[] parseWithBraces(String names) {
        // for mixed corporate and normal authors
        // e.g. author = {John Smith and {Corporation Name} and Jane Doe}
        String[] nameParts = AND_SEPARATOR_PATTERN.split(names);
        List<CSLName> result = new ArrayList<>();

        for (String namePart : nameParts) {
            namePart = namePart.trim();

            Matcher matcher = BRACED_CONTENT_PATTERN.matcher(namePart);

            if (matcher.find()) {
                String bracedContent = matcher.group(1);

                result.add(new CSLNameBuilder().literal(bracedContent).build());
            } else {
                CSLName[] parsed = parse(namePart);
                if (parsed.length > 0) {
                    result.add(parsed[0]);
                }
            }
        }

        return result.isEmpty() ?
                new CSLName[] { new CSLNameBuilder().literal(names).build() } :
                result.toArray(new CSLName[0]);
    }
}

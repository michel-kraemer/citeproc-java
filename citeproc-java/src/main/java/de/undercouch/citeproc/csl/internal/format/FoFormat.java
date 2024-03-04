package de.undercouch.citeproc.csl.internal.format;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.SBibliography;
import de.undercouch.citeproc.csl.internal.TokenBuffer;
import de.undercouch.citeproc.csl.internal.behavior.Display;
import de.undercouch.citeproc.output.Bibliography;
import de.undercouch.citeproc.output.SecondFieldAlign;
import org.apache.commons.text.StringEscapeUtils;

import static de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes.FS_ITALIC;
import static de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes.FW_BOLD;
import static de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes.VA_SUP;

/**
 * The XSL-FO output format
 * @author Michel Kraemer
 */
public class FoFormat extends BaseFormat {
    private final String columnWidth;

    /**
     * Default constructor
     */
    public FoFormat() {
        this("2.5em");
    }

    /**
     * Constructs a new output format
     * @param columnWidth the width of the first column if
     * {@link SecondFieldAlign} is enabled
     */
    public FoFormat(String columnWidth) {
        this.columnWidth = columnWidth;
    }

    @Override
    public String getName() {
        return "fo";
    }

    @Override
    protected String doFormatCitation(TokenBuffer buffer, RenderContext ctx) {
        return format(buffer);
    }

    @Override
    protected String doFormatBibliographyEntry(TokenBuffer buffer,
            RenderContext ctx, int index) {
        String result;

        SecondFieldAlign sfa = ctx.getStyle().getBibliography().getSecondFieldAlign();
        if (sfa != SecondFieldAlign.FALSE && !buffer.getTokens().isEmpty()) {
            // find tokens that are part of the first field
            int i = 0;
            while (buffer.getTokens().get(i).isFirstField()) {
                ++i;
            }
            TokenBuffer firstBuffer = buffer.copy(0, i);
            TokenBuffer restBuffer = buffer.copy(i, buffer.getTokens().size());

            // render first field and rest independently
            result = "\n  <fo:table table-layout=\"fixed\" width=\"100%\">\n    " +
                    "<fo:table-column column-number=\"1\" column-width=\"" + columnWidth + "\"/>\n    " +
                    "<fo:table-column column-number=\"2\" column-width=\"proportional-column-width(1)\"/>\n    " +
                    "<fo:table-body>\n      " +
                    "<fo:table-row>\n        " +
                    "<fo:table-cell>\n          " +
                    "<fo:block>" + format(firstBuffer) + "</fo:block>\n        " +
                    "</fo:table-cell>\n        " +
                    "<fo:table-cell>\n          " +
                    "<fo:block>" + format(restBuffer) + "</fo:block>\n        " +
                    "</fo:table-cell>\n      " +
                    "</fo:table-row>\n    " +
                    "</fo:table-body>\n  " +
                    "</fo:table>\n";
        } else {
            result = format(buffer);
        }

        return "<fo:block id=\"" + ctx.getCitationItem().getId() + "\">" + result + "</fo:block>\n";
    }

    @Override
    protected String doFormatLink(String text, String uri) {
        return "<fo:basic-link external-destination=\"url('" + uri + "')\">" + text + "</fo:basic-link>";
    }

    @Override
    public Bibliography makeBibliography(String[] entries,
            SBibliography bibliographyElement) {
        SecondFieldAlign sfa = bibliographyElement.getSecondFieldAlign();
        return new Bibliography(entries, null, null, null, null, null, null,
                null, null, sfa);
    }

    @Override
    protected String escape(String str) {
        return StringEscapeUtils.escapeXml11(str);
    }

    @Override
    protected String openFontStyle(int fontStyle) {
        if (fontStyle == FS_ITALIC) {
            return "<fo:inline font-style=\"italic\">";
        } else {
            return "<fo:inline font-style=\"oblique\">";
        }
    }

    @Override
    protected String closeFontStyle(int fontStyle) {
        return "</fo:inline>";
    }

    @Override
    protected String openFontVariant(int fontVariant) {
        return "<fo:inline font-variant=\"small-caps\">";
    }

    @Override
    protected String closeFontVariant(int fontVariant) {
        return "</fo:inline>";
    }

    @Override
    protected String openFontWeight(int fontWeight) {
        if (fontWeight == FW_BOLD) {
            return "<fo:inline font-weight=\"bold\">";
        } else {
            return "<fo:inline font-weight=\"lighter\">";
        }
    }

    @Override
    protected String closeFontWeight(int fontWeight) {
        return "</fo:inline>";
    }

    @Override
    protected String openTextDecoration(int textDecoration) {
        return "<fo:inline text-decoration=\"underline\">";
    }

    @Override
    protected String closeTextDecoration(int textDecoration) {
        return "</fo:inline>";
    }

    @Override
    protected String openVerticalAlign(int verticalAlign) {
        if (verticalAlign == VA_SUP) {
            return "<fo:inline vertical-align=\"super\">";
        } else {
            return "<fo:inline vertical-align=\"sub\">";
        }
    }

    @Override
    protected String closeVerticalAlign(int verticalAlign) {
        return "</fo:inline>";
    }

    @Override
    protected String openDisplay(Display display) {
        switch (display) {
            case BLOCK:
                return "\n  <fo:block>";

            case LEFT_MARGIN:
                return "\n  <fo:table table-layout=\"fixed\" width=\"100%\">\n    " +
                        "<fo:table-column column-number=\"1\" column-width=\"" + columnWidth + "\"/>\n    " +
                        "<fo:table-column column-number=\"2\" column-width=\"proportional-column-width(1)\"/>\n    " +
                        "<fo:table-body>\n      " +
                        "<fo:table-row>\n        " +
                        "<fo:table-cell>\n          " +
                        "<fo:block>";

            case RIGHT_INLINE:
                return "<fo:table-cell>\n          " +
                        "<fo:block>";

            case INDENT:
                return "<fo:block margin-left=\"2em\">";

            default:
                break;
        }

        return null;
    }

    @Override
    protected String closeDisplay(Display display) {
        switch (display) {
            case BLOCK:
            case INDENT:
                return "</fo:block>\n";

            case LEFT_MARGIN:
                return "</fo:block>\n        " +
                        "</fo:table-cell>\n        ";

            case RIGHT_INLINE:
                return "</fo:block>\n        " +
                        "</fo:table-cell>\n      " +
                        "</fo:table-row>\n    " +
                        "</fo:table-body>\n  " +
                        "</fo:table>\n";

            default:
                break;
        }

        return null;
    }
}

package de.undercouch.citeproc.csl.internal.format;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.SBibliography;
import de.undercouch.citeproc.csl.internal.TokenBuffer;
import de.undercouch.citeproc.csl.internal.token.Token;
import de.undercouch.citeproc.output.Bibliography;
import de.undercouch.citeproc.output.SecondFieldAlign;

import java.util.List;

import static de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes.FW_BOLD;
import static de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes.VA_SUP;

/**
 * The AsciiDoc output format
 * @author Michel Kraemer
 */
public class AsciiDocFormat extends BaseFormat {
    @Override
    public String getName() {
        return "asciidoc";
    }

    @Override
    protected String doFormatCitation(TokenBuffer buffer, RenderContext ctx) {
        return format(buffer);
    }

    @Override
    protected String doFormatBibliographyEntry(TokenBuffer buffer,
            RenderContext ctx, int index) {
        String pre;
        if (index > 0) {
            pre = "\n";
        } else {
            pre = "";
        }

        String result;

        SecondFieldAlign sfa = ctx.getStyle().getBibliography().getSecondFieldAlign();
        if (sfa != SecondFieldAlign.FALSE && !buffer.getTokens().isEmpty()) {
            // find tokens that are part of the first field
            int i = 0;
            List<Token> tokens = buffer.getTokens();
            while (i < tokens.size() && tokens.get(i).isFirstField()) {
                ++i;
            }
            TokenBuffer firstBuffer = buffer.copy(0, i);
            TokenBuffer restBuffer = buffer.copy(i, tokens.size());

            // render first field and rest independently
            result = "[.csl-left-margin]##" + format(firstBuffer) +
                    "##[.csl-right-inline]##" + format(restBuffer) + "##";
        } else {
            result = format(buffer);
        }

        return pre + "[.csl-entry]\n" + result + "\n";
    }

    @Override
    protected String doFormatLink(String text, String uri) {
        // AsciiDoc renders URLs automatically as links
        return uri;
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
        return str.replace("*", "pass:[*]")
                .replace("_", "pass:[_]")
                .replace("#", "pass:[#]")
                .replace("^", "pass:[^]")
                .replace("~", "pass:[~]")
                .replace("[[", "pass:[[[]")
                .replace("  ", "&#160; ");
    }

    @Override
    protected String openFontStyle(int fontStyle) {
        return "__";
    }

    @Override
    protected String closeFontStyle(int fontStyle) {
        return "__";
    }

    @Override
    protected String openFontVariant(int fontVariant) {
        return "[.small-caps]#";
    }

    @Override
    protected String closeFontVariant(int fontVariant) {
        return "#";
    }

    @Override
    protected String openFontWeight(int fontWeight) {
        if (fontWeight == FW_BOLD) {
            return "**";
        }
        return "";
    }

    @Override
    protected String closeFontWeight(int fontWeight) {
        if (fontWeight == FW_BOLD) {
            return "**";
        }
        return "";
    }

    @Override
    protected String openTextDecoration(int textDecoration) {
        return "[.underline]#";
    }

    @Override
    protected String closeTextDecoration(int textDecoration) {
        return "#";
    }

    @Override
    protected String openVerticalAlign(int verticalAlign) {
        if (verticalAlign == VA_SUP) {
            return "^";
        } else {
            return "~";
        }
    }

    @Override
    protected String closeVerticalAlign(int verticalAlign) {
        if (verticalAlign == VA_SUP) {
            return "^";
        } else {
            return "~";
        }
    }
}

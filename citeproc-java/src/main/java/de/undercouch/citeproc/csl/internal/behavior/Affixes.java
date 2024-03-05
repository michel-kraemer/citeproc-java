package de.undercouch.citeproc.csl.internal.behavior;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.TokenBuffer;
import de.undercouch.citeproc.helper.NodeHelper;
import org.w3c.dom.Node;

import java.util.function.Consumer;

import static de.undercouch.citeproc.csl.internal.token.TextToken.Type.PREFIX;
import static de.undercouch.citeproc.csl.internal.token.TextToken.Type.SUFFIX;

/**
 * Wraps around a render function and adds prefixes and suffixes
 * @author Michel Kraemer
 */
public class Affixes implements Behavior {
    private final String prefix;
    private final String suffix;

    /**
     * Default constructor without prefix and suffix
     */
    public Affixes() {
        prefix = null;
        suffix = null;
    }

    /**
     * Extract prefix and suffix from an XML node
     * @param node the XML node
     */
    public Affixes(Node node) {
        prefix = NodeHelper.getAttrValue(node, "prefix");
        suffix = NodeHelper.getAttrValue(node, "suffix");
    }

    @Override
    public void accept(Consumer<RenderContext> renderFunction, RenderContext ctx) {
        RenderContext child = new RenderContext(ctx);
        renderFunction.accept(child);

        if (!child.getResult().isEmpty()) {
            if (prefix != null) {
                ctx.emit(prefix, PREFIX);
            }
            ctx.emit(child.getResult());
            if (suffix != null) {
                ctx.emit(suffix, SUFFIX);
            }
        }
    }

    /**
     * Modifies the given token buffer and renders prefix and suffix
     * @param buffer the buffer to modify
     */
    public void applyTo(TokenBuffer buffer) {
        if (!buffer.isEmpty()) {
            if (prefix != null) {
                buffer.prepend(prefix, PREFIX);
            }
            if (suffix != null) {
                buffer.append(suffix, SUFFIX);
            }
        }
    }
}

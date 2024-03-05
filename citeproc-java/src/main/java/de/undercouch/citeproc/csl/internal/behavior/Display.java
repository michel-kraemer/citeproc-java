package de.undercouch.citeproc.csl.internal.behavior;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.token.DisplayGroupToken;
import de.undercouch.citeproc.csl.internal.token.DisplayGroupToken.Type;
import de.undercouch.citeproc.helper.NodeHelper;
import org.w3c.dom.Node;

import java.util.function.Consumer;

import static de.undercouch.citeproc.csl.internal.token.DisplayGroupToken.Type.BLOCK;
import static de.undercouch.citeproc.csl.internal.token.DisplayGroupToken.Type.INDENT;
import static de.undercouch.citeproc.csl.internal.token.DisplayGroupToken.Type.LEFT_MARGIN;
import static de.undercouch.citeproc.csl.internal.token.DisplayGroupToken.Type.RIGHT_INLINE;

/**
 * Wraps around a render function and adds display group tokens
 * @author Michel Kraemer
 */
public class Display implements Behavior {
    private final Type type;

    /**
     * Parses an XML node and determines if display group tokens should be added
     * @param node the XML node
     */
    public Display(Node node) {
        String strDisplay = NodeHelper.getAttrValue(node, "display");
        if (strDisplay != null) {
            switch (strDisplay) {
                case "block":
                    this.type = BLOCK;
                    break;
                case "left-margin":
                    this.type = LEFT_MARGIN;
                    break;
                case "right-inline":
                    this.type = RIGHT_INLINE;
                    break;
                case "indent":
                    this.type = INDENT;
                    break;
                default:
                    this.type = null;
                    break;
            }
        } else {
            this.type = null;
        }
    }

    @Override
    public void accept(Consumer<RenderContext> renderFunction, RenderContext ctx) {
        if (type != null) {
            RenderContext tmp = new RenderContext(ctx);
            renderFunction.accept(tmp);

            if (!tmp.getResult().isEmpty()) {
                ctx.emit(new DisplayGroupToken(true, type));
                ctx.emit(tmp.getResult());
                ctx.emit(new DisplayGroupToken(false, type));
            }
        } else {
            renderFunction.accept(ctx);
        }
    }
}

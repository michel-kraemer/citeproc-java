package de.undercouch.citeproc.csl.internal.behavior;

import de.undercouch.citeproc.csl.internal.RenderContext;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Wraps around a render function and augments its result
 * @author Michel Kraemer
 */
public interface Behavior extends BiConsumer<Consumer<RenderContext>, RenderContext> {
    /**
     * Convenience function to wrap this behavior and others around a render
     * function.
     * @param renderFunction the render function to wrap around
     * @return a new render function that can be called instead of the
     * wrapped one
     */
    default Consumer<RenderContext> wrap(Consumer<RenderContext> renderFunction) {
        return ctx -> accept(renderFunction, ctx);
    }
}

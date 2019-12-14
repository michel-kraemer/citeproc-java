package de.undercouch.citeproc.csl.internal;

/**
 * An element in a style file
 * @author Michel Kraemer
 */
public interface SElement {
    /**
     * Renders the element
     * @param ctx the context in which to render
     */
    void render(RenderContext ctx);
}

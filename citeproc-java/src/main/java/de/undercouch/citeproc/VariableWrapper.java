package de.undercouch.citeproc;

/**
 * Decorates individual items in citations and bibliographies
 * @author Michel Kraemer
 */
public interface VariableWrapper {
    /**
     * This method will be called by the citation processor when an item in a
     * citation or bibliography is about to be rendered. The method may change
     * the way the item is rendered, for example, by prepending or appending
     * strings, or by completely replacing the item. The default implementation
     * of this method always returns <code>prePunct + str + postPunct</code>.
     * @param params a number of parameters that specify the context in which
     * rendering happens, the citation item that is currently being rendered,
     * and additional information.
     * @param prePunct the text that precedes the item to render
     * @param str the item to render
     * @param postPunct the text that follows the item to render
     * @return the string to be rendered
     */
    String wrap(VariableWrapperParams params, String prePunct, String str, String postPunct);
}

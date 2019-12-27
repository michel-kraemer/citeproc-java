package de.undercouch.citeproc.csl.internal.behavior;

import de.undercouch.citeproc.helper.NodeHelper;
import org.w3c.dom.Node;

/**
 * Formatting attributes to apply when a
 * {@link de.undercouch.citeproc.csl.internal.TokenBuffer} is rendered in
 * an output {@link de.undercouch.citeproc.csl.internal.format.Format}.
 * @author Michel Kraemer
 */
public class Formatting {
    /**
     * Font style
     */
    public enum FontStyle {
        NORMAL,
        ITALIC,
        OBLIQUE;

        /**
         * Create a {@link FontStyle} object from a string
         * @param s the string
         * @return the {@link FontStyle} object or {@code null} if the value
         * is invalid
         */
        public static FontStyle of(String s) {
            if ("normal".equals(s)) {
                return NORMAL;
            } else if ("italic".equals(s)) {
                return ITALIC;
            } else if ("oblique".equals(s)) {
                return OBLIQUE;
            }
            return null;
        }
    }

    /**
     * Font variant
     */
    public enum FontVariant {
        NORMAL,
        SMALL_CAPS;

        /**
         * Create a {@link FontVariant} object from a string
         * @param s the string
         * @return the {@link FontVariant} object or {@code null} if the value
         * is invalid
         */
        public static FontVariant of(String s) {
            if ("normal".equals(s)) {
                return NORMAL;
            } else if ("small-caps".equals(s)) {
                return SMALL_CAPS;
            }
            return null;
        }
    }

    /**
     * Font weight
     */
    public enum FontWeight {
        NORMAL,
        BOLD,
        LIGHT;

        /**
         * Create a {@link FontWeight} object from a string
         * @param s the string
         * @return the {@link FontWeight} object or {@code null} if the value
         * is invalid
         */
        public static FontWeight of(String s) {
            if ("normal".equals(s)) {
                return NORMAL;
            } else if ("bold".equals(s)) {
                return BOLD;
            } else if ("light".equals(s)) {
                return LIGHT;
            }
            return null;
        }
    }

    /**
     * Text decoration
     */
    public enum TextDecoration {
        NONE,
        UNDERLINE;

        /**
         * Create a {@link TextDecoration} object from a string
         * @param s the string
         * @return the {@link TextDecoration} object or {@code null} if the
         * value is invalid
         */
        public static TextDecoration of(String s) {
            if ("none".equals(s)) {
                return NONE;
            } else if ("underline".equals(s)) {
                return UNDERLINE;
            }
            return null;
        }
    }

    /**
     * Vertical alignment
     */
    public enum VerticalAlign {
        BASELINE,
        SUP,
        SUB;

        /**
         * Create a {@link VerticalAlign} object from a string
         * @param s the string
         * @return the {@link VerticalAlign} object or {@code null} if the
         * value is invalid
         */
        public static VerticalAlign of(String s) {
            if ("baseline".equals(s)) {
                return BASELINE;
            } else if ("sup".equals(s)) {
                return SUP;
            } else if ("sub".equals(s)) {
                return SUB;
            }
            return null;
        }
    }

    private final FontStyle fontStyle;
    private final FontVariant fontVariant;
    private final FontWeight fontWeight;
    private final TextDecoration textDecoration;
    private final VerticalAlign verticalAlign;

    /**
     * Hidden constructor. Use {@link #of(Node)} to create instances of
     * this class.
     * @param fontStyle font style
     * @param fontVariant font variant
     * @param fontWeight font weight
     * @param textDecoration text decoration
     * @param verticalAlign vertical alignment
     */
    private Formatting(FontStyle fontStyle, FontVariant fontVariant,
            FontWeight fontWeight, TextDecoration textDecoration,
            VerticalAlign verticalAlign) {
        this.fontStyle = fontStyle;
        this.fontVariant = fontVariant;
        this.fontWeight = fontWeight;
        this.textDecoration = textDecoration;
        this.verticalAlign = verticalAlign;
    }

    /**
     * Get the font style
     * @return the font style
     */
    public FontStyle getFontStyle() {
        return fontStyle;
    }

    /**
     * Get the font variant
     * @return the font variant
     */
    public FontVariant getFontVariant() {
        return fontVariant;
    }

    /**
     * Get the font weight
     * @return the font weight
     */
    public FontWeight getFontWeight() {
        return fontWeight;
    }

    /**
     * Get the text decoration
     * @return the text decoration
     */
    public TextDecoration getTextDecoration() {
        return textDecoration;
    }

    /**
     * Get the vertical alignment
     * @return the vertical alignment
     */
    public VerticalAlign getVerticalAlign() {
        return verticalAlign;
    }

    /**
     * Create a {@link Formatting} object from an XML node
     * @param node the XML node to parse
     * @return the {@link Formatting} object or {@code null} if the XML node
     * does not contain formatting attributes
     */
    public static Formatting of(Node node) {
        FontStyle fontStyle = FontStyle.of(
                NodeHelper.getAttrValue(node, "font-style"));
        FontVariant fontVariant = FontVariant.of(
                NodeHelper.getAttrValue(node, "font-variant"));
        FontWeight fontWeight = FontWeight.of(
                NodeHelper.getAttrValue(node, "font-weight"));
        TextDecoration textDecoration = TextDecoration.of(
                NodeHelper.getAttrValue(node, "text-decoration"));
        VerticalAlign verticalAlign = VerticalAlign.of(
                NodeHelper.getAttrValue(node, "vertical-align"));

        if (fontStyle != null || fontVariant != null || fontWeight != null ||
                textDecoration != null || verticalAlign != null) {
            return new Formatting(fontStyle, fontVariant, fontWeight,
                    textDecoration, verticalAlign);
        }
        return null;
    }
}

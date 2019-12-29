package de.undercouch.citeproc.csl.internal.behavior;

import de.undercouch.citeproc.helper.NodeHelper;
import org.w3c.dom.Node;

/**
 * <p>A class providing static methods to create formatting attributes. Each
 * attribute is encoded using two bits of an integer as follows:</p>
 * <pre>
 *             VA  |  TD  |  FW  |  FV  |  FS
 * BIT 31 ... 9  8 | 7  6 | 5  4 | 3  2 | 1  0
 *
 * FS = Font style
 * FV = Font variant
 * FW = Font weight
 * TD = Text decoration
 * VA = Vertical alignment
 * </pre>
 * <p>The values of each attribute are as follows:</p>
 * <pre>
 * FS: 00 - UNDEFINED
 *     01 - NORMAL
 *     10 - ITALIC
 *     11 - OBLIQUE
 *
 * FV: 00 - UNDEFINED
 *     01 - NORMAL
 *     10 - SMALL_CAPS
 *
 * FW: 00 - UNDEFINED
 *     01 - NORMAL
 *     10 - BOLD
 *     11 - LIGHT
 *
 * TD: 00 - UNDEFINED
 *     01 - NONE
 *     10 - UNDERLINE
 *
 * VA: 00 - UNDEFINED
 *     01 - BASELINE
 *     10 - SUP
 *     11 - SUB
 * </pre>
 * @author Michel Kraemer
 */
public class FormattingAttributes {
    /**
     * The number of bits to shift for each formatting attribute
     */
    private static final int FS_SHIFT = 0;
    private static final int FV_SHIFT = 2;
    private static final int FW_SHIFT = 4;
    private static final int TD_SHIFT = 6;
    private static final int VA_SHIFT = 8;

    public static final int UNDEFINED = 0;
    public static final int NORMAL = 1;

    /**
     * Font style values
     */
    public static final int FS_NORMAL = NORMAL;
    public static final int FS_ITALIC = 2;
    public static final int FS_OBLIQUE = 3;

    /**
     * Font variant values
     */
    public static final int FV_NORMAL = NORMAL;
    public static final int FV_SMALLCAPS = 2;

    /**
     * Font weight values
     */
    public static final int FW_NORMAL = NORMAL;
    public static final int FW_BOLD = 2;
    public static final int FW_LIGHT = 3;

    /**
     * Text decoration values
     */
    public static final int TD_NONE = NORMAL;
    public static final int TD_UNDERLINE = 2;

    /**
     * Vertical alignment values
     */
    public static final int VA_BASELINE = NORMAL;
    public static final int VA_SUP = 2;
    public static final int VA_SUB = 3;

    /**
     * Hidden constructor. Use {@link #of(Node)} instead.
     */
    private FormattingAttributes() {
        // hidden constructor
    }

    /**
     * Create formatting attributes from an XML node
     * @param node the XML node to parse
     * @return the formatting attributes (will equal {@code 0} if the XML node
     * did not contain any values)
     */
    public static int of(Node node) {
        String strFontStyle = NodeHelper.getAttrValue(node, "font-style");
        String strFontVariant = NodeHelper.getAttrValue(node, "font-variant");
        String strFontWeight = NodeHelper.getAttrValue(node, "font-weight");
        String strTextDecoration = NodeHelper.getAttrValue(node, "text-decoration");
        String strVerticalAlign = NodeHelper.getAttrValue(node, "vertical-align");

        int fontStyle = 0;
        int fontVariant = 0;
        int fontWeight = 0;
        int textDecoration = 0;
        int verticalAlign = 0;

        if (strFontStyle != null) {
            switch (strFontStyle) {
                case "normal":
                    fontStyle = FS_NORMAL;
                    break;
                case "italic":
                    fontStyle = FS_ITALIC;
                    break;
                case "oblique":
                    fontStyle = FS_OBLIQUE;
                    break;
                default:
                    break;
            }
        }

        if (strFontVariant != null) {
            switch (strFontVariant) {
                case "normal":
                    fontVariant = FV_NORMAL;
                    break;
                case "small-caps":
                    fontVariant = FV_SMALLCAPS;
                    break;
                default:
                    break;
            }
        }

        if (strFontWeight != null) {
            switch (strFontWeight) {
                case "normal":
                    fontWeight = FW_NORMAL;
                    break;
                case "bold":
                    fontWeight = FW_BOLD;
                    break;
                case "light":
                    fontWeight = FW_LIGHT;
                    break;
                default:
                    break;
            }
        }

        if (strTextDecoration != null) {
            switch (strTextDecoration) {
                case "none":
                    textDecoration = TD_NONE;
                    break;
                case "underline":
                    textDecoration = TD_UNDERLINE;
                    break;
                default:
                    break;
            }
        }

        if (strVerticalAlign != null) {
            switch (strVerticalAlign) {
                case "baseline":
                    verticalAlign = VA_BASELINE;
                    break;
                case "sup":
                    verticalAlign = VA_SUP;
                    break;
                case "sub":
                    verticalAlign = VA_SUB;
                    break;
                default:
                    break;
            }
        }

        return fontStyle << FS_SHIFT | fontVariant << FV_SHIFT |
                fontWeight << FW_SHIFT | textDecoration << TD_SHIFT |
                verticalAlign << VA_SHIFT;
    }

    /**
     * Get the font style from an integer
     * @param attributes the integer
     * @return the font style
     */
    public static int getFontStyle(int attributes) {
        return attributes >> FS_SHIFT & 3;
    }

    /**
     * Get the font variant from an integer
     * @param attributes the integer
     * @return the font variant
     */
    public static int getFontVariant(int attributes) {
        return attributes >> FV_SHIFT & 3;
    }

    /**
     * Get the font weight from an integer
     * @param attributes the integer
     * @return the font weight
     */
    public static int getFontWeight(int attributes) {
        return attributes >> FW_SHIFT & 3;
    }

    /**
     * Get the text decoration from an integer
     * @param attributes the integer
     * @return the text decoration
     */
    public static int getTextDecoration(int attributes) {
        return attributes >> TD_SHIFT & 3;
    }

    /**
     * Get the vertical alignment from an integer
     * @param attributes the integer
     * @return the vertical alignment
     */
    public static int getVerticalAlign(int attributes) {
        return attributes >> VA_SHIFT & 3;
    }

    /**
     * Merge two sets of formatting attributes
     * @param a the set to merge into
     * @param b the set to merge (overwrites attributes from {@code a} unless
     * they are not undefined)
     * @return the merged formatting attributes
     */
    public static int merge(int a, int b) {
        for (int i = FS_SHIFT; i <= VA_SHIFT; ++i) {
            int mask = 3 << i;
            int t = b & mask;
            if (t != 0) {
                a = a & ~mask | t;
            }
        }
        return a;
    }
}

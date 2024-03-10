package de.undercouch.citeproc.csl.internal.rendering.condition;

import de.undercouch.citeproc.csl.CSLCitationItem;
import de.undercouch.citeproc.csl.CSLType;
import de.undercouch.citeproc.csl.internal.GeneratedCitation;
import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.helper.NumberElement;
import de.undercouch.citeproc.csl.internal.helper.NumberParser;
import de.undercouch.citeproc.helper.NodeHelper;
import de.undercouch.citeproc.helper.NumberHelper;
import org.w3c.dom.Node;

import java.util.List;

/**
 * A conditional element in a style file
 * @author Michel Kraemer
 */
public class SIf extends SCondition {
    private static final int ALL = 0;
    private static final int ANY = 1;
    private static final int NONE = 2;

    private final String[] types;
    private final String[] variables;
    private final String[] isNumerics;
    private final String[] numbers;
    private final String[] positions;
    private final int match;

    /**
     * Create the conditional element from an XML node
     * @param node the XML node
     */
    public SIf(Node node) {
        super(node);

        // get the citation item types to check against
        String type = NodeHelper.getAttrValue(node, "type");
        if (type != null) {
            types = type.split("\\s+");
        } else {
            types = null;
        }

        // get the variables to check
        String variable = NodeHelper.getAttrValue(node, "variable");
        if (variable != null) {
            variables = variable.split("\\s+");
        } else {
            variables = null;
        }

        // get the numeric variables to check
        String isNumeric = NodeHelper.getAttrValue(node, "is-numeric");
        if (isNumeric != null) {
            isNumerics = isNumeric.split("\\s+");
        } else {
            isNumerics = null;
        }

        // get the labels to check the number variable against
        String number = NodeHelper.getAttrValue(node, "number");
        if (number != null) {
            numbers = number.split("\\s+");
        } else {
            numbers = null;
        }

        // get the positions to check
        String position = NodeHelper.getAttrValue(node, "position");
        if (position != null) {
            positions = position.split("\\s+");
        } else {
            positions = null;
        }

        // get the match mode
        String match = NodeHelper.getAttrValue(node, "match");
        if (match == null || match.equals("all")) {
            this.match = ALL;
        } else if (match.equals("any")) {
            this.match = ANY;
        } else if (match.equals("none")) {
            this.match = NONE;
        } else {
            throw new IllegalStateException("Unknown match mode: " + match);
        }
    }

    @Override
    public boolean matches(RenderContext ctx) {
        if (types == null && variables == null && isNumerics == null &&
                numbers == null && positions == null) {
            return false;
        }

        Boolean mt = matchesType(ctx);
        if (mt != null) {
            return mt;
        }

        Boolean mv = matchesVariable(ctx);
        if (mv != null) {
            return mv;
        }

        Boolean mne = matchesNumerics(ctx);
        if (mne != null) {
            return mne;
        }

        Boolean mnb = matchesNumbers(ctx);
        if (mnb != null) {
            return mnb;
        }

        Boolean mnp = matchesPositions(ctx);
        if (mnp != null) {
            return mnp;
        }

        return match != ANY;
    }

    private Boolean matchesType(RenderContext ctx) {
        CSLType cslType = ctx.getItemData().getType();
        if (types != null && cslType != null) {
            // check if the citation item matches the given types
            String type = cslType.toString();
            for (String s : types) {
                boolean r = type.equals(s);
                if (match == ALL && !r) {
                    return Boolean.FALSE;
                }
                if (match == ANY && r) {
                    return Boolean.TRUE;
                }
                if (match == NONE && r) {
                    return Boolean.FALSE;
                }
            }
        }
        return null;
    }

    private Boolean matchesVariable(RenderContext ctx) {
        if (variables != null) {
            // check the values of the given variables
            for (String v : variables) {
                Object o = ctx.getVariable(v, true);
                if (match == ALL && o == null) {
                    return Boolean.FALSE;
                }
                if (match == ANY && o != null) {
                    return Boolean.TRUE;
                }
                if (match == NONE && o != null) {
                    return Boolean.FALSE;
                }
            }
        }
        return null;
    }

    private Boolean matchesNumerics(RenderContext ctx) {
        if (isNumerics != null) {
            // check if the given variables are numeric
            for (String v : isNumerics) {
                Object o = ctx.getVariable(v, true);
                boolean numeric = o != null && (o instanceof Number ||
                        NumberHelper.isNumeric(String.valueOf(o)));
                if (match == ALL && !numeric) {
                    return Boolean.FALSE;
                }
                if (match == ANY && numeric) {
                    return Boolean.TRUE;
                }
                if (match == NONE && numeric) {
                    return Boolean.FALSE;
                }
            }
        }
        return null;
    }

    private Boolean matchesNumbers(RenderContext ctx) {
        if (numbers != null) {
            // check if the number variable has the given label(s)
            String v = ctx.getStringVariable("number", true);
            String firstLabel = null;
            if (v != null) {
                List<NumberElement> elements = NumberParser.parse(v);
                if (elements.get(0).getLabel() != null) {
                    firstLabel = elements.get(0).getLabel().toString();
                }
            }
            if (firstLabel == null) {
                firstLabel = "number";
            }

            for (String number : numbers) {
                if (match == ALL && !number.equals(firstLabel)) {
                    return Boolean.FALSE;
                }
                if (match == ANY && number.equals(firstLabel)) {
                    return Boolean.TRUE;
                }
                if (match == NONE && number.equals(firstLabel)) {
                    return Boolean.FALSE;
                }
            }
        }
        return null;
    }

    private boolean isFirstCitation(RenderContext ctx) {
        List<GeneratedCitation> gcs = ctx.getGeneratedCitations();

        for (GeneratedCitation gc : gcs) {
            for (CSLCitationItem preparedItem : gc.getPrepared().getCitationItems()) {
                if (preparedItem.getId().equals(ctx.getCitationItem().getId())) {
                    // this item has been generated before
                    // it is therefore not the first one
                    return false;
                }
            }
        }

        CSLCitationItem firstItem = ctx.getCitation().getCitationItems()[0];
        return firstItem == ctx.getCitationItem();
    }

    private boolean isIbid(RenderContext ctx) {
        CSLCitationItem currentItem = ctx.getCitationItem();

        // look for current cite (= citation item) in current citation
        CSLCitationItem[] citationItems = ctx.getCitation().getCitationItems();
        for (int i = 0; i < citationItems.length; i++) {
            CSLCitationItem item = citationItems[i];
            if (item == currentItem) {
                if (i > 0) {
                    // According to the specification:
                    // a. The cite is not the first one in this citation. Check
                    // if the preceding cite references the same item
                    return citationItems[i - 1].getId().equals(currentItem.getId());
                } else {
                    // b. The cite is the first one in this citation. Check if
                    // the preceding citation consists of a single cite
                    // referencing the same item.
                    List<GeneratedCitation> gcs = ctx.getGeneratedCitations();
                    if (gcs != null && !gcs.isEmpty()) {
                        CSLCitationItem[] gcis = gcs.get(gcs.size() - 1)
                                .getPrepared().getCitationItems();
                        return gcis.length == 1 && gcis[0].getId().equals(currentItem.getId());
                    }
                    break;
                }
            }
        }

        return false;
    }

    private Boolean matchesPositions(RenderContext ctx) {
        if (positions != null) {
            if (ctx.getGeneratedCitations() == null) {
                // we are not rendering a citation
                if (match == ALL) {
                    return Boolean.FALSE;
                }
                return null;
            }

            for (String position : positions) {
                boolean b;
                if (position.equals("first")) {
                    b = isFirstCitation(ctx);
                } else if (position.equals("ibid")) {
                    b = isIbid(ctx);
                } else {
                    b = false;
                }

                if (match == ALL && !b) {
                    return Boolean.FALSE;
                }
                if (match == ANY && b) {
                    return Boolean.TRUE;
                }
                if (match == NONE && b) {
                    return Boolean.FALSE;
                }
            }
        }
        return null;
    }
}

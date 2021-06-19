package de.undercouch.citeproc.csl.internal.rendering;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.behavior.Behavior;
import de.undercouch.citeproc.helper.NodeHelper;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Attributes for a name element that can also be inherited from a style,
 * citation, or bibliography element.
 * @author Michel Kraemer
 */
public class SNameInheritableAttributes implements Behavior {
    private static final String DEFAULT_AND = null;
    private static final String DEFAULT_DELIMITER_PRECEDES_ET_AL = "contextual";
    private static final String DEFAULT_DELIMITER_PRECEDES_LAST = "contextual";
    private static final boolean DEFAULT_INITIALIZE = true;
    private static final String DEFAULT_INITIALIZE_WITH = null;
    private static final String DEFAULT_NAME_AS_SORT_ORDER = null;
    private static final String DEFAULT_SORT_SEPARATOR = ", ";
    private static final Integer DEFAULT_ET_AL_MIN = null;
    private static final Integer DEFAULT_ET_AL_USE_FIRST = null;

    private final String and;
    private final String delimiterPrecedesEtAl;
    private final String delimiterPrecedesLast;
    private final boolean initialize;
    private final String initializeWith;
    private final String nameAsSortOrder;
    private final String sortSeparator;
    private final Integer etAlMin;
    private final Integer etAlUseFirst;
    // private final Integer etAlUseLast;
    // private final Integer etAlUseSubsequentMin;
    // private final Integer etAlUseSubsequentUseFirst;
    private final boolean hasInheritableAttributes;

    public SNameInheritableAttributes(Node node) {
        String and;
        boolean initialize;
        String initializeWith;
        String nameAsSortOrder;
        String delimiterPrecedesEtAl;
        String delimiterPrecedesLast;
        String sortSeparator;
        String strEtAlMin;
        String strEtAlUseFirst;

        if (node != null) {
            and = NodeHelper.getAttrValue(node, "and");
            String strInitialize = NodeHelper.getAttrValue(node, "initialize");
            initialize = strInitialize == null ? DEFAULT_INITIALIZE : Boolean.parseBoolean(strInitialize);
            initializeWith = StringUtils.strip(NodeHelper.getAttrValue(node, "initialize-with"));
            nameAsSortOrder = NodeHelper.getAttrValue(node, "name-as-sort-order");
            delimiterPrecedesEtAl = NodeHelper.getAttrValue(node,
                    "delimiter-precedes-et-al");
            delimiterPrecedesLast = NodeHelper.getAttrValue(node,
                    "delimiter-precedes-last");
            sortSeparator = NodeHelper.getAttrValue(node, "sort-separator");
            strEtAlMin = NodeHelper.getAttrValue(node, "et-al-min");
            strEtAlUseFirst = NodeHelper.getAttrValue(node, "et-al-use-first");
        } else {
            and = DEFAULT_AND;
            initialize = DEFAULT_INITIALIZE;
            initializeWith = DEFAULT_INITIALIZE_WITH;
            nameAsSortOrder = DEFAULT_NAME_AS_SORT_ORDER;
            delimiterPrecedesEtAl = null;
            delimiterPrecedesLast = null;
            sortSeparator = null;
            strEtAlMin = null;
            strEtAlUseFirst = null;
        }

        if (delimiterPrecedesEtAl == null) {
            delimiterPrecedesEtAl = DEFAULT_DELIMITER_PRECEDES_ET_AL;
        }

        if (delimiterPrecedesLast == null) {
            delimiterPrecedesLast = DEFAULT_DELIMITER_PRECEDES_LAST;
        }

        if (sortSeparator == null) {
            sortSeparator = DEFAULT_SORT_SEPARATOR;
        }

        Integer etAlMin;
        if (strEtAlMin != null) {
            etAlMin = Integer.parseInt(strEtAlMin);
        } else {
            etAlMin = DEFAULT_ET_AL_MIN;
        }

        Integer etAlUseFirst;
        if (strEtAlUseFirst != null) {
            etAlUseFirst = Integer.parseInt(strEtAlUseFirst);
        } else {
            etAlUseFirst = DEFAULT_ET_AL_USE_FIRST;
        }

        this.and = and;
        this.delimiterPrecedesEtAl = delimiterPrecedesEtAl;
        this.delimiterPrecedesLast = delimiterPrecedesLast;
        this.initialize = initialize;
        this.initializeWith = initializeWith;
        this.nameAsSortOrder = nameAsSortOrder;
        this.sortSeparator = sortSeparator;
        this.etAlMin = etAlMin;
        this.etAlUseFirst = etAlUseFirst;
        hasInheritableAttributes = determineHasInheritableAttributes(this);
    }

    public SNameInheritableAttributes(String and,
            String delimiterPrecedesEtAl, String delimiterPrecedesLast,
            boolean initialize, String initializeWith, String nameAsSortOrder,
            String sortSeparator, Integer etAlMin, Integer etAlUseFirst) {
        this.and = and;
        this.delimiterPrecedesEtAl = delimiterPrecedesEtAl;
        this.delimiterPrecedesLast = delimiterPrecedesLast;
        this.initialize = initialize;
        this.initializeWith = initializeWith;
        this.nameAsSortOrder = nameAsSortOrder;
        this.sortSeparator = sortSeparator;
        this.etAlMin = etAlMin;
        this.etAlUseFirst = etAlUseFirst;
        hasInheritableAttributes = determineHasInheritableAttributes(this);
    }

    private static boolean determineHasInheritableAttributes(SNameInheritableAttributes nia) {
        return !Objects.equals(nia.and, DEFAULT_AND) ||
                nia.initialize != DEFAULT_INITIALIZE ||
                !Objects.equals(nia.initializeWith, DEFAULT_INITIALIZE_WITH) ||
                !Objects.equals(nia.nameAsSortOrder, DEFAULT_NAME_AS_SORT_ORDER) ||
                !Objects.equals(nia.delimiterPrecedesEtAl, DEFAULT_DELIMITER_PRECEDES_ET_AL) ||
                !Objects.equals(nia.delimiterPrecedesLast, DEFAULT_DELIMITER_PRECEDES_LAST) ||
                !Objects.equals(nia.sortSeparator, DEFAULT_SORT_SEPARATOR) ||
                !Objects.equals(nia.etAlMin, DEFAULT_ET_AL_MIN) ||
                !Objects.equals(nia.etAlUseFirst, DEFAULT_ET_AL_USE_FIRST);
    }

    public String getAnd() {
        return and;
    }

    public String getDelimiterPrecedesEtAl() {
        return delimiterPrecedesEtAl;
    }

    public String getDelimiterPrecedesLast() {
        return delimiterPrecedesLast;
    }

    public boolean isInitialize() {
        return initialize;
    }

    public String getInitializeWith() {
        return initializeWith;
    }

    public String getNameAsSortOrder() {
        return nameAsSortOrder;
    }

    public String getSortSeparator() {
        return sortSeparator;
    }

    public Integer getEtAlMin() {
        return etAlMin;
    }

    public Integer getEtAlUseFirst() {
        return etAlUseFirst;
    }

    public SNameInheritableAttributes merge(SNameInheritableAttributes override) {
        if (override == null || !override.hasInheritableAttributes) {
            return this;
        }

        String and = this.and;
        if (!Objects.equals(override.and, DEFAULT_AND)) {
            and = override.and;
        }

        boolean initialize = this.initialize;
        if (override.initialize != DEFAULT_INITIALIZE) {
            initialize = override.initialize;
        }

        String initializeWith = this.initializeWith;
        if (!Objects.equals(override.initializeWith, DEFAULT_INITIALIZE_WITH)) {
            initializeWith = override.initializeWith;
        }

        String nameAsSortOrder = this.nameAsSortOrder;
        if (!Objects.equals(override.nameAsSortOrder, DEFAULT_NAME_AS_SORT_ORDER)) {
            nameAsSortOrder = override.nameAsSortOrder;
        }

        String delimiterPrecedesEtAl = this.delimiterPrecedesEtAl;
        if (!Objects.equals(override.delimiterPrecedesEtAl, DEFAULT_DELIMITER_PRECEDES_ET_AL)) {
            delimiterPrecedesEtAl = override.delimiterPrecedesEtAl;
        }

        String delimiterPrecedesLast = this.delimiterPrecedesLast;
        if (!Objects.equals(override.delimiterPrecedesLast, DEFAULT_DELIMITER_PRECEDES_LAST)) {
            delimiterPrecedesLast = override.delimiterPrecedesLast;
        }

        String sortSeparator = this.sortSeparator;
        if (!Objects.equals(override.sortSeparator, DEFAULT_SORT_SEPARATOR)) {
            sortSeparator = override.sortSeparator;
        }

        Integer etAlMin = this.etAlMin;
        if (!Objects.equals(override.etAlMin, DEFAULT_ET_AL_MIN)) {
            etAlMin = override.etAlMin;
        }

        Integer etAlUseFirst = this.etAlUseFirst;
        if (!Objects.equals(override.etAlUseFirst, DEFAULT_ET_AL_USE_FIRST)) {
            etAlUseFirst = override.etAlUseFirst;
        }

        return new SNameInheritableAttributes(and, delimiterPrecedesEtAl,
                delimiterPrecedesLast, initialize, initializeWith, nameAsSortOrder,
                sortSeparator, etAlMin, etAlUseFirst);
    }

    @Override
    public void accept(Consumer<RenderContext> renderFunction, RenderContext ctx) {
        if (hasInheritableAttributes) {
            RenderContext tmp = new RenderContext(ctx, this);
            renderFunction.accept(tmp);
            ctx.emit(tmp.getResult());
        } else {
            renderFunction.accept(ctx);
        }
    }
}

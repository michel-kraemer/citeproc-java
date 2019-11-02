package de.undercouch.citeproc.lint;

import de.undercouch.citeproc.csl.CSLItemData;

import java.util.function.Predicate;

/**
 * A filter for citation items that should be validated by a {@link Linter}
 * @author Michel Kraemer
 * @since 1.1.0
 */
public interface LintFilter extends Predicate<CSLItemData> {
    /**
     * Tests if a citation item matches the filter's criteria
     * @param item the citation item to check
     * @return <code>true</code> if the item matches the criteria,
     * <code>false</code> otherwise
     */
    @Override
    boolean test(CSLItemData item);
}

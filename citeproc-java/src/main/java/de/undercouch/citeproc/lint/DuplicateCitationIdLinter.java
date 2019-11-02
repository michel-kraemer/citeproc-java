package de.undercouch.citeproc.lint;

import de.undercouch.citeproc.ItemDataProvider;
import de.undercouch.citeproc.csl.CSLItemData;

import java.util.HashSet;
import java.util.Set;

/**
 * A linter that reports duplicate citation IDs
 * @author Michel Kraemer
 * @since 1.1.0
 */
public class DuplicateCitationIdLinter extends AbstractLinter {
    private Set<String> ids = new HashSet<>();

    @Override
    protected void doLintItem(CSLItemData item, ItemDataProvider provider) {
        String id = item.getId();
        if (ids.contains(id)) {
            LintErrorEvent e = new DefaultLintErrorEvent(
                    LintErrorEvent.Type.DUPLICATE_ID,
                    "Duplicate citation ID `" + id + "'", id, provider, this);
            fireError(e);
        } else {
            ids.add(id);
        }
    }
}


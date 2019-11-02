package de.undercouch.citeproc.lint;

import de.undercouch.citeproc.ItemDataProvider;
import de.undercouch.citeproc.csl.CSLItemData;

/**
 * A linter calling several other linters to validate citation items
 * @author Michel Kraemer
 * @since 1.1.0
 */
public class DefaultLinter extends AbstractLinter {
    /**
     * The linters that will be called (in the given order)
     */
    private final Linter[] LINTERS = {
            new DuplicateCitationIdLinter()
    };

    @Override
    public void addListener(LintListener listener) {
        super.addListener(listener);
        for (Linter l : LINTERS) {
            l.addListener(listener);
        }
    }

    @Override
    public void removeListener(LintListener listener) {
        super.removeListener(listener);
        for (Linter l : LINTERS) {
            l.removeListener(listener);
        }
    }

    @Override
    public void lint(ItemDataProvider provider) {
        LintEvent event = new DefaultLintEvent(provider, this);
        fireStart(event);

        for (Linter l : LINTERS) {
            l.lint(provider);
        }

        fireEnd(event);
    }

    @Override
    protected void doLintItem(CSLItemData item, ItemDataProvider provider) {
        for (Linter l : LINTERS) {
            l.lintItem(item, provider);
        }
    }
}

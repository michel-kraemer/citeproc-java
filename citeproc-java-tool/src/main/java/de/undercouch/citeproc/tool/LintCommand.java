package de.undercouch.citeproc.tool;

import de.undercouch.citeproc.lint.DefaultLinter;
import de.undercouch.citeproc.lint.LintErrorEvent;
import de.undercouch.citeproc.lint.LintListenerAdapter;
import de.undercouch.citeproc.lint.Linter;
import de.undercouch.underline.InputReader;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * CLI command that validates citation entries in an input file
 * @author Michel Kraemer
 * @since 1.1.0
 */
public class LintCommand extends CitationIdsCommand {
    @Override
    public String getUsageName() {
        return "lint";
    }

    @Override
    public String getUsageDescription() {
        return "Validate citation items";
    }

    @Override
    public int doRun(String[] remainingArgs, InputReader in, PrintWriter out)
            throws IOException {
        int ret = super.doRun(remainingArgs, in, out);
        if (ret != 0) {
            return ret;
        }

        Set<String> citationItemIdsToProcess = new HashSet<>(getCitationIds());

        Linter linter = new DefaultLinter();
        linter.addFilter(citationItemIdsToProcess::contains);

        AtomicBoolean ok = new AtomicBoolean(true);
        linter.addListener(new LintListenerAdapter() {
            @Override
            public void onError(LintErrorEvent e) {
                ok.set(false);
                error(e.getMessage());
            }
        });

        linter.lint(getProvider());

        return ok.get() ? 0 : 1;
    }
}

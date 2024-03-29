package de.undercouch.citeproc.tool.shell;

import de.undercouch.citeproc.tool.AbstractCSLToolCommand;
import de.undercouch.underline.InputReader;

import java.io.PrintWriter;

/**
 * Get the current citation locale
 * @author Michel Kraemer
 */
public class ShellGetLocaleCommand extends AbstractCSLToolCommand {
    @Override
    public String getUsageName() {
        return "get locale";
    }

    @Override
    public String getUsageDescription() {
        return "Get the current citation locale";
    }

    @Override
    public int doRun(String[] remainingArgs, InputReader in, PrintWriter out) {
        out.println(ShellContext.current().getLocale());
        return 0;
    }
}

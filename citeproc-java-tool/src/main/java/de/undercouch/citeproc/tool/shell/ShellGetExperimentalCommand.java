package de.undercouch.citeproc.tool.shell;

import de.undercouch.citeproc.tool.AbstractCSLToolCommand;
import de.undercouch.citeproc.tool.CSLToolContext;
import de.undercouch.underline.InputReader;
import de.undercouch.underline.OptionParserException;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Determine if the experimental pure Java mode is currently enabled
 * @author Michel Kraemer
 */
public class ShellGetExperimentalCommand extends AbstractCSLToolCommand {
    @Override
    public String getUsageName() {
        return "get experimental mode";
    }

    @Override
    public String getUsageDescription() {
        return "Determine if the experimental pure Java mode is currently enabled";
    }

    @Override
    public int doRun(String[] remainingArgs, InputReader in, PrintWriter out)
            throws OptionParserException, IOException {
        out.println(CSLToolContext.current().isExperimental());
        return 0;
    }
}

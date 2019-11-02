package de.undercouch.citeproc.tool.shell;

import de.undercouch.citeproc.tool.AbstractCSLToolCommand;
import de.undercouch.underline.InputReader;
import de.undercouch.underline.OptionParserException;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Get the current output format
 * @author Michel Kraemer
 */
public class ShellGetFormatCommand extends AbstractCSLToolCommand {
    @Override
    public String getUsageName() {
        return "get format";
    }

    @Override
    public String getUsageDescription() {
        return "Get the current output format";
    }

    @Override
    public int doRun(String[] remainingArgs, InputReader in, PrintWriter out)
            throws OptionParserException, IOException {
        out.println(ShellContext.current().getFormat());
        return 0;
    }
}

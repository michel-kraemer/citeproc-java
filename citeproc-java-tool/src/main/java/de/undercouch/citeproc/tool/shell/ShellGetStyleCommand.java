package de.undercouch.citeproc.tool.shell;

import de.undercouch.citeproc.tool.AbstractCSLToolCommand;
import de.undercouch.underline.InputReader;
import de.undercouch.underline.OptionParserException;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Get the current citation style
 * @author Michel Kraemer
 */
public class ShellGetStyleCommand extends AbstractCSLToolCommand {
    @Override
    public String getUsageName() {
        return "get style";
    }

    @Override
    public String getUsageDescription() {
        return "Get the current citation style";
    }

    @Override
    public int doRun(String[] remainingArgs, InputReader in, PrintWriter out)
            throws OptionParserException, IOException {
        out.println(ShellContext.current().getStyle());
        return 0;
    }
}

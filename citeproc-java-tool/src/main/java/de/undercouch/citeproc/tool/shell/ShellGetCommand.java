package de.undercouch.citeproc.tool.shell;

import de.undercouch.citeproc.tool.AbstractCSLToolCommand;
import de.undercouch.underline.CommandDesc;
import de.undercouch.underline.CommandDescList;
import de.undercouch.underline.InputReader;
import de.undercouch.underline.OptionParserException;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Get values of variables that affect the operation of the interactive shell
 * @author Michel Kraemer
 */
public class ShellGetCommand extends AbstractCSLToolCommand {
    private AbstractCSLToolCommand subcommand;

    @Override
    public String getUsageName() {
        return "get";
    }

    @Override
    public String getUsageDescription() {
        return "Get values of variables that affect the operation of "
                + "the interactive shell";
    }

    /**
     * Sets the subcommand to delegate to
     * @param subcommand the subcommand
     */
    @CommandDescList({
            @CommandDesc(longName = "style",
                    description = "get the current citation style",
                    command = ShellGetStyleCommand.class),
            @CommandDesc(longName = "locale",
                    description = "get the current citation locale",
                    command = ShellGetLocaleCommand.class),
            @CommandDesc(longName = "format",
                    description = "get the current output format",
                    command = ShellGetFormatCommand.class),
    })
    public void setSubcommand(AbstractCSLToolCommand subcommand) {
        this.subcommand = subcommand;
    }

    @Override
    public boolean checkArguments() {
        if (subcommand == null) {
            error("no variable specified");
            return false;
        }
        return super.checkArguments();
    }

    @Override
    public int doRun(String[] remainingArgs, InputReader in, PrintWriter out)
            throws OptionParserException, IOException {
        return subcommand.run(remainingArgs, in, out);
    }
}

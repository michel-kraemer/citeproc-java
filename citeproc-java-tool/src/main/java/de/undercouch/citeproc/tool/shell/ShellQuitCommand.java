package de.undercouch.citeproc.tool.shell;

import de.undercouch.citeproc.tool.AbstractCSLToolCommand;
import de.undercouch.underline.InputReader;

import java.io.PrintWriter;

/**
 * Exit the interactive shell
 * @author Michel Kraemer
 */
public class ShellQuitCommand extends AbstractCSLToolCommand {
    @Override
    public String getUsageName() {
        return "quit";
    }

    @Override
    public String getUsageDescription() {
        return "Exit the interactive shell";
    }

    @Override
    public int doRun(String[] remainingArgs, InputReader in, PrintWriter out) {
        return 0;
    }
}

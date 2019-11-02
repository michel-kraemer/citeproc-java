package de.undercouch.citeproc.tool.shell;

import de.undercouch.citeproc.tool.AbstractCSLToolCommand;
import de.undercouch.underline.InputReader;
import de.undercouch.underline.OptionParserException;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Exit the interactive shell
 * @author Michel Kraemer
 */
public class ShellExitCommand extends AbstractCSLToolCommand {
	@Override
	public String getUsageName() {
		return "exit";
	}
	
	@Override
	public String getUsageDescription() {
		return "Exit the interactive shell";
	}
	
	@Override
	public int doRun(String[] remainingArgs, InputReader in, PrintWriter out)
			throws OptionParserException, IOException {
		return 0;
	}
}

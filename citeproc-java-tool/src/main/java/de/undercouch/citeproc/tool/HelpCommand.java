package de.undercouch.citeproc.tool;

import de.undercouch.citeproc.CSLTool;
import de.undercouch.underline.Command;
import de.undercouch.underline.InputReader;
import de.undercouch.underline.OptionParserException;
import de.undercouch.underline.UnknownAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Displays a command's help
 * @author Michel Kraemer
 */
public class HelpCommand extends AbstractCSLToolCommand {
    private List<String> commands = new ArrayList<>();

    /**
     * Sets the commands to display the help for
     * @param commands the commands
     */
    @UnknownAttributes("COMMAND")
    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    @Override
    public String getUsageName() {
        return "help";
    }

    @Override
    public String getUsageDescription() {
        return "Display a command's help";
    }

    @Override
    public int doRun(String[] remainingArgs, InputReader in, PrintWriter out)
            throws OptionParserException, IOException {
        // simply forward commands to CSLTool and append '-h'
        Command cmd = new CSLTool();
        String[] args = commands.toArray(new String[commands.size() + 1]);
        args[args.length - 1] = "-h";
        cmd.run(args, in, out);
        return 1;
    }
}

package de.undercouch.citeproc.tool;

import de.undercouch.underline.Command;
import de.undercouch.underline.InputReader;
import de.undercouch.underline.OptionParserException;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * An interface for commands from the {@link de.undercouch.citeproc.CSLTool}
 * @author Michel Kraemer
 */
public interface CSLToolCommand extends Command {
    /**
     * @return the command's name displayed in the help
     */
    String getUsageName();

    /**
     * @return the command description that should be displayed in the help
     */
    String getUsageDescription();

    /**
     * Checks the provided arguments
     * @return true if all arguments are OK, false otherwise
     */
    boolean checkArguments();

    /**
     * Runs the command
     * @param remainingArgs arguments that have not been parsed yet, can
     * be forwarded to subcommands
     * @param in a stream from which user input can be read
     * @param out a stream to write the output to
     * @return the exit code
     * @throws OptionParserException if the remaining arguments could not be parsed
     * @throws IOException if input files could not be read or the output
     * stream could not be written
     */
    int doRun(String[] remainingArgs, InputReader in, PrintWriter out)
            throws OptionParserException, IOException;
}

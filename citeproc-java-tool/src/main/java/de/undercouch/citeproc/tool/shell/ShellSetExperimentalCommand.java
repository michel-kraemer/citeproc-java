package de.undercouch.citeproc.tool.shell;

import de.undercouch.citeproc.tool.AbstractCSLToolCommand;
import de.undercouch.citeproc.tool.CSLToolContext;
import de.undercouch.underline.InputReader;
import de.undercouch.underline.OptionParserException;
import de.undercouch.underline.UnknownAttributes;
import org.apache.commons.lang3.BooleanUtils;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Enable or disable the experimental pure Java mode
 * @author Michel Kraemer
 */
public class ShellSetExperimentalCommand extends AbstractCSLToolCommand implements Completer {
    /**
     * The arguments passed to this command
     */
    private List<String> arguments;

    @Override
    public String getUsageName() {
        return "set format";
    }

    @Override
    public String getUsageDescription() {
        return "Set the current output format";
    }

    /**
     * Sets arguments passed to this command
     * @param arguments the arguments
     */
    @UnknownAttributes("EXPERIMENTAL")
    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    @Override
    public boolean checkArguments() {
        if (arguments == null || arguments.isEmpty()) {
            error("specify either 'true' or 'false' to enable or disable the " +
                    "experimental mode");
            return false;
        }
        if (arguments.size() > 1) {
            error("too many arguments");
            return false;
        }
        return true;
    }

    @Override
    public int doRun(String[] remainingArgs, InputReader in, PrintWriter out)
            throws OptionParserException, IOException {
        CSLToolContext.current().setExperimental(BooleanUtils.toBoolean(arguments.get(0)));
        return 0;
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        candidates.add(new Candidate("true"));
        candidates.add(new Candidate("on"));
        candidates.add(new Candidate("yes"));
        candidates.add(new Candidate("false"));
        candidates.add(new Candidate("off"));
        candidates.add(new Candidate("no"));
    }
}

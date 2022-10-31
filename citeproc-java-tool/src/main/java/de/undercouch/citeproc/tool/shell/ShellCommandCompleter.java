package de.undercouch.citeproc.tool.shell;

import de.undercouch.citeproc.CSLTool;
import de.undercouch.citeproc.tool.HelpCommand;
import de.undercouch.citeproc.tool.shell.ShellCommandParser.Result;
import de.undercouch.underline.Command;
import de.undercouch.underline.InvalidOptionException;
import de.undercouch.underline.Option;
import de.undercouch.underline.OptionGroup;
import de.undercouch.underline.OptionIntrospector;
import de.undercouch.underline.OptionIntrospector.ID;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.reader.impl.DefaultParser;

import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Calculates completions for citeproc-java's interactive mode
 * @author Michel Kraemer
 */
public class ShellCommandCompleter implements Completer {
    /**
     * A list of CSLTool commands that should be excluded from completions
     */
    private final Set<Class<? extends Command>> excludedCommands;

    /**
     * Default constructor
     */
    public ShellCommandCompleter() {
        this(Collections.emptyList());
    }

    /**
     * Constructs a new completer
     * @param excludedCommands a list of CSLTool commands that should be
     * excluded from completions
     */
    public ShellCommandCompleter(List<Class<? extends Command>> excludedCommands) {
        this.excludedCommands = new HashSet<>(excludedCommands);
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        boolean allparsed;
        Set<Candidate> result = new TreeSet<>();

        try {
            Result pr = ShellCommandParser.parse(line.line(), excludedCommands);
            if (pr.getFirstCommand() == HelpCommand.class ||
                    pr.getFirstCommand() == ShellHelpCommand.class) {
                // parse again, but skip 'help'
                pr = ShellCommandParser.parse(pr.getRemainingArgs(),
                        excludedCommands);
            }

            // noinspection StatementWithEmptyBody
            if (pr.getRemainingArgs().length > 1) {
                // command line could not be parsed completely. we cannot
                // provide suggestions for more than one unrecognized argument.
            } else {
                OptionGroup<ID> options;
                if (pr.getLastCommand() == null) {
                    options = OptionIntrospector.introspect(CSLTool.class,
                            AdditionalShellCommands.class);
                } else {
                    options = OptionIntrospector.introspect(pr.getLastCommand());
                }

                String[] ra = pr.getRemainingArgs();
                allparsed = (ra == null || ra.length == 0);

                // add completions
                for (Option<ID> o : options.getCommands()) {
                    Class<? extends Command> cmd =
                            OptionIntrospector.getCommand(o.getId());
                    if (excludedCommands.contains(cmd)) {
                        continue;
                    }

                    if (allparsed || o.getLongName().startsWith(ra[0])) {
                        result.add(new Candidate(o.getLongName()));
                    }
                }
            }

            if (pr.getLastCommand() != null &&
                    Completer.class.isAssignableFrom(pr.getLastCommand())) {
                Completer cc;
                try {
                    cc = (Completer)pr.getLastCommand().getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    // should never happen
                    throw new RuntimeException(e);
                }
                ParsedLine pl = new DefaultParser().new ArgumentList(line.line(),
                        Arrays.asList(pr.getRemainingArgs()),
                        pr.getRemainingArgs().length - 1, 0, 0, null, 0, 0);
                List<Candidate> ccl = new ArrayList<>();
                cc.complete(reader, pl, ccl);
                result.addAll(ccl);
            }
        } catch (InvalidOptionException e) {
            // there's an option, we cannot calculate completions anymore
            // because options are not allowed in the interactive shell
        } catch (IntrospectionException e) {
            throw new RuntimeException("Could not inspect command", e);
        }

        // sort completions
        List<Candidate> resultList = new ArrayList<>(result);
        Collections.sort(resultList);
        candidates.addAll(resultList);
    }
}

package de.undercouch.citeproc.tool;

import de.undercouch.citeproc.CSLTool;
import de.undercouch.citeproc.tool.shell.ConsoleInputReader;
import de.undercouch.citeproc.tool.shell.ErrorOutputStream;
import de.undercouch.citeproc.tool.shell.ShellCommandCompleter;
import de.undercouch.citeproc.tool.shell.ShellCommandParser;
import de.undercouch.citeproc.tool.shell.ShellCommandParser.Result;
import de.undercouch.citeproc.tool.shell.ShellContext;
import de.undercouch.citeproc.tool.shell.ShellExitCommand;
import de.undercouch.citeproc.tool.shell.ShellQuitCommand;
import de.undercouch.underline.Command;
import de.undercouch.underline.InputReader;
import de.undercouch.underline.Option;
import de.undercouch.underline.OptionGroup;
import de.undercouch.underline.OptionIntrospector;
import de.undercouch.underline.OptionIntrospector.ID;
import de.undercouch.underline.OptionParserException;
import org.apache.commons.lang3.ArrayUtils;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Runs the tool in interactive mode
 * @author Michel Kraemer
 */
public class ShellCommand extends AbstractCSLToolCommand {
    /**
     * Commands that should not be available in the interactive shell
     */
    public static final List<Class<? extends Command>> EXCLUDED_COMMANDS;
    static {{
        List<Class<? extends Command>> ec = new ArrayList<>();
        ec.add(HelpCommand.class);
        ec.add(ShellCommand.class);
        EXCLUDED_COMMANDS = Collections.unmodifiableList(ec);
    }}

    @Override
    public String getUsageName() {
        return "shell";
    }

    @Override
    public String getUsageDescription() {
        return "Run " + CSLToolContext.current().getToolName() +
                " in interactive mode";
    }

    @Override
    public int doRun(String[] remainingArgs, InputReader in, PrintWriter out)
            throws OptionParserException, IOException {
        // prepare console
        Terminal terminal = TerminalBuilder.terminal();
        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .appName(CSLToolContext.current().getToolName())
                .completer(new ShellCommandCompleter(EXCLUDED_COMMANDS))
                .variable(LineReader.HISTORY_FILE, new File(
                        CSLToolContext.current().getConfigDir(), "shell_history_2.txt"))
                .build();

        // enable colored error stream for ANSI terminals
        OutputStream errout = new ErrorOutputStream(terminal.output());
        System.setErr(new PrintStream(errout, false, terminal.encoding().name()));

        PrintWriter cout = new PrintWriter(terminal.output(), true);

        // print welcome message
        cout.println("Welcome to " + CSLToolContext.current().getToolName() +
                " " + CSLTool.getVersion());
        cout.println();
        cout.println("Type `help' for a list of commands and `help "
                + "<command>' for information");
        cout.println("on a specific command. Type `quit' to exit " +
                CSLToolContext.current().getToolName() + ".");
        cout.println();

        ShellContext.enter();
        try {
            mainLoop(reader, cout);
        } finally {
            ShellContext.exit();
        }

        // print Goodbye message
        cout.println("Bye!");

        return 0;
    }

    /**
     * Runs the shell's main loop
     * @param reader the line reader used to read user input from
     * the command line
     * @param cout the output stream
     * @throws IOException if an I/O error occurs
     */
    private void mainLoop(LineReader reader, PrintWriter cout) throws IOException {
        InputReader lr = new ConsoleInputReader(reader);

        while (true) {
            String line = null;
            try {
                line = reader.readLine("> ");
            } catch (UserInterruptException e) {
                // ignore
            } catch (EndOfFileException e) {
                break;
            }
            if (line == null || line.isEmpty()) {
                continue;
            }

            String[] args = ShellCommandParser.split(line);

            Result pr;
            try {
                pr = ShellCommandParser.parse(args, EXCLUDED_COMMANDS);
            } catch (OptionParserException e) {
                // there is an option, only commands are allowed in the
                // interactive shell
                error(e.getMessage());
                continue;
            } catch (IntrospectionException e) {
                // should never happen
                throw new RuntimeException(e);
            }

            Class<? extends Command> cmdClass = pr.getFirstCommand();

            if (cmdClass == ShellExitCommand.class ||
                    cmdClass == ShellQuitCommand.class) {
                break;
            } else if (cmdClass == null) {
                error("unknown command `" + args[0] + "'");
                continue;
            }

            Command cmd;
            try {
                cmd = cmdClass.newInstance();
            } catch (Exception e) {
                // should never happen
                throw new RuntimeException(e);
            }

            boolean acceptsInputFile = false;
            if (cmd instanceof ProviderCommand) {
                cmd = new InputFileCommand((ProviderCommand)cmd);
                acceptsInputFile = true;
            }

            args = ArrayUtils.subarray(args, 1, args.length);
            args = augmentCommand(args, pr.getLastCommand(), acceptsInputFile);

            try {
                cmd.run(args, lr, cout);
            } catch (OptionParserException e) {
                error(e.getMessage());
            }
        }
    }

    /**
     * Augments the given command line with context variables
     * @param args the current command line
     * @param cmd the last parsed command in the command line
     * @param acceptsInputFile true if the given command accepts an input file
     * @return the new command line
     */
    private String[] augmentCommand(String[] args, Class<? extends Command> cmd,
            boolean acceptsInputFile) {
        OptionGroup<ID> options;
        try {
            if (acceptsInputFile) {
                options = OptionIntrospector.introspect(cmd, InputFileCommand.class);
            } else {
                options = OptionIntrospector.introspect(cmd);
            }
        } catch (IntrospectionException e) {
            // should never happen
            throw new RuntimeException(e);
        }

        ShellContext sc = ShellContext.current();
        for (Option<ID> o : options.getOptions()) {
            if (o.getLongName().equals("style")) {
                args = ArrayUtils.add(args, "--style");
                args = ArrayUtils.add(args, sc.getStyle());
            } else if (o.getLongName().equals("locale")) {
                args = ArrayUtils.add(args, "--locale");
                args = ArrayUtils.add(args, sc.getLocale());
            } else if (o.getLongName().equals("format")) {
                args = ArrayUtils.add(args, "--format");
                args = ArrayUtils.add(args, sc.getFormat());
            } else if (o.getLongName().equals("input") &&
                    sc.getInputFile() != null && !sc.getInputFile().isEmpty()) {
                args = ArrayUtils.add(args, "--input");
                args = ArrayUtils.add(args, sc.getInputFile());
            }
        }

        return args;
    }
}

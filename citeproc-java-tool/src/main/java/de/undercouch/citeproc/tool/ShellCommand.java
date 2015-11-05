// Copyright 2014 Michel Kraemer
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package de.undercouch.citeproc.tool;

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

import org.apache.commons.lang3.ArrayUtils;

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
import jline.console.ConsoleReader;
import jline.console.history.FileHistory;

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
		List<Class<? extends Command>> ec = new ArrayList<Class<? extends Command>>();
		ec.add(HelpCommand.class);
		ec.add(ShellCommand.class);
		EXCLUDED_COMMANDS = Collections.unmodifiableList(ec);
	}};
	
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
		//prepare console
		final ConsoleReader reader = new ConsoleReader();
		reader.setPrompt("> ");
		reader.addCompleter(new ShellCommandCompleter(EXCLUDED_COMMANDS));
		FileHistory history = new FileHistory(new File(
				CSLToolContext.current().getConfigDir(), "shell_history.txt"));
		reader.setHistory(history);
		
		//enable colored error stream for ANSI terminals
		if (reader.getTerminal().isAnsiSupported()) {
			OutputStream errout = new ErrorOutputStream(reader.getTerminal()
					.wrapOutIfNeeded(System.out));
			System.setErr(new PrintStream(errout, false,
					((OutputStreamWriter)reader.getOutput()).getEncoding()));
		}
		
		PrintWriter cout = new PrintWriter(reader.getOutput(), true);
		
		//print welcome message
		cout.println("Welcome to " + CSLToolContext.current().getToolName() +
				" " + CSLTool.getVersion());
		cout.println();
		cout.println("Type `help' for a list of commands and `help "
				+ "<command>' for information");
		cout.println("on a specific command. Type `quit' to exit " +
				CSLToolContext.current().getToolName() + ".");
		cout.println();
		
		String line;
		ShellContext.enter();
		try {
			line = mainLoop(reader, cout);
		} finally {
			ShellContext.exit();
			
			//make sure we save the history before we exit
			history.flush();
		}
		
		//print Goodbye message
		if (line == null) {
			//user pressed Ctrl+D
			cout.println();
		}
		cout.println("Bye!");
		
		return 0;
	}
	
	/**
	 * Runs the shell's main loop
	 * @param reader the console reader used to read user input from
	 * the command line
	 * @param cout the output stream
	 * @return the last line read or null if the user pressed Ctrl+D and
	 * the input stream has ended
	 * @throws IOException if an I/O error occurs
	 */
	private String mainLoop(ConsoleReader reader, PrintWriter cout) throws IOException {
		InputReader lr = new ConsoleInputReader(reader);
		
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.isEmpty()) {
				continue;
			}
			
			String[] args = ShellCommandParser.split(line);
			
			Result pr;
			try {
				pr = ShellCommandParser.parse(args, EXCLUDED_COMMANDS);
			} catch (OptionParserException e) {
				//there is an option, only commands are allowed in the
				//interactive shell
				error(e.getMessage());
				continue;
			} catch (IntrospectionException e) {
				//should never happen
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
				//should never happen
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
		
		return line;
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
			//should never happen
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

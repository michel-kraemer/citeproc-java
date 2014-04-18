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
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jline.console.ConsoleReader;
import de.undercouch.citeproc.CSLTool;
import de.undercouch.citeproc.helper.tool.Command;
import de.undercouch.citeproc.helper.tool.InputReader;
import de.undercouch.citeproc.helper.tool.OptionParserException;
import de.undercouch.citeproc.tool.shell.ConsoleInputReader;
import de.undercouch.citeproc.tool.shell.ErrorOutputStream;
import de.undercouch.citeproc.tool.shell.ShellCommandCompleter;
import de.undercouch.citeproc.tool.shell.ShellCommandParser;
import de.undercouch.citeproc.tool.shell.ShellCommandParser.Result;
import de.undercouch.citeproc.tool.shell.ShellExitCommand;
import de.undercouch.citeproc.tool.shell.ShellQuitCommand;

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
	public String getUsageDescription() {
		return "Run " + CSLToolContext.current().getToolName() +
				" in interactive mode";
	}

	@Override
	public String getUsageArguments() {
		return "shell";
	}

	@Override
	public int doRun(String[] remainingArgs, InputReader in, PrintWriter out)
			throws OptionParserException, IOException {
		//prepare console
		final ConsoleReader reader = new ConsoleReader();
		reader.setPrompt("> ");
		reader.addCompleter(new ShellCommandCompleter(EXCLUDED_COMMANDS));
		
		//enable colored error stream for ANSI terminals
		if (reader.getTerminal().isAnsiSupported()) {
			OutputStream errout = new ErrorOutputStream(reader.getTerminal()
					.wrapOutIfNeeded(System.out));
			System.setErr(new PrintStream(errout, false,
					((OutputStreamWriter)reader.getOutput()).getEncoding()));
		}
		
		//prepare input and output for commands to run
		InputReader lr = new ConsoleInputReader(reader);
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
		
		//read and interpret lines
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.isEmpty()) {
				continue;
			}
			
			Result pr;
			try {
				pr = ShellCommandParser.parse(line, EXCLUDED_COMMANDS);
			} catch (IntrospectionException e) {
				//should never happen
				throw new RuntimeException(e);
			}
			
			Class<? extends Command> cmdClass = pr.getCommand();
			
			if (cmdClass == ShellExitCommand.class ||
					cmdClass == ShellQuitCommand.class) {
				break;
			}
			
			Command cmd;
			try {
				cmd = cmdClass.newInstance();
			} catch (Exception e) {
				//should never happen
				throw new RuntimeException(e);
			}
			
			try {
				cmd.run(pr.getRemainingArgs(), lr, cout);
			} catch (OptionParserException e) {
				error(e.getMessage());
			}
		}
		
		//print Goodbye message
		if (line == null) {
			//user pressed Ctrl+D
			cout.println();
		}
		cout.println("Bye!");
		
		return 0;
	}
}

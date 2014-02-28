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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;

import jline.console.ConsoleReader;
import jline.console.completer.Completer;
import jline.console.history.History;
import jline.console.history.MemoryHistory;
import de.undercouch.citeproc.CSLTool;
import de.undercouch.citeproc.helper.tool.Command;
import de.undercouch.citeproc.helper.tool.InputReader;
import de.undercouch.citeproc.helper.tool.OptionParserException;

/**
 * Runs the tool in interactive mode
 * @author Michel Kraemer
 */
public class ShellCommand extends AbstractCSLToolCommand {
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
		reader.setPrompt(CSLToolContext.current().getToolName() + "> ");
		reader.addCompleter(new ShellCommandCompleter());
		
		//enable colored error stream for ANSI terminals
		if (reader.getTerminal().isAnsiSupported()) {
			OutputStream errout = new ErrorOutputStream(reader.getTerminal()
					.wrapOutIfNeeded(System.out));
			System.setErr(new PrintStream(errout, false,
					((OutputStreamWriter)reader.getOutput()).getEncoding()));
		}
		
		//prepare input and output for commands to run
		InputReader lr = new ConsoleInputReader(reader);
		PrintWriter cout = new PrintWriter(reader.getOutput());
		
		//read and interpret lines
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) {
				break;
			}
			if (line.isEmpty()) {
				continue;
			}
			
			//run command
			Command cmd = new CSLTool();
			String[] args = line.split("\\s+");
			try {
				cmd.run(args, lr, cout);
			} catch (OptionParserException e) {
				error(e.getMessage());
			}
		}
		
		return 0;
	}
	
	/**
	 * A filter output stream that colors everything in red on ANSI terminals
	 */
	private static class ErrorOutputStream extends FilterOutputStream {
		/**
		 * True if we're currently writing to the underlying output stream
		 */
		private boolean writing = false;
		
		/**
		 * Constructs a new filter output stream
		 * @param out the underlying output stream
		 */
		public ErrorOutputStream(OutputStream out) {
			super(out);
		}
		
		@Override
		public void write(int b) throws IOException {
			boolean oldwriting = enableRed();
			super.write(b);
			if (!oldwriting) {
				disableRed();
			}
		}
		
		@Override
		public void write(byte b[], int off, int len) throws IOException {
			boolean oldwriting = enableRed();
			super.write(b, off, len);
			if (!oldwriting) {
				disableRed();
			}
		}
		
		/**
		 * Enables colored output
		 * @return true if the colored output was already enabled before
		 * @throws IOException if writing to the underlying output stream failed
		 */
		private boolean enableRed() throws IOException {
			boolean oldwriting = writing;
			if (!writing) {
				writing = true;
				byte[] en = "\u001B[31m".getBytes();
				super.write(en, 0, en.length);
			}
			return oldwriting;
		}
		
		/**
		 * Disabled colored output
		 * @throws IOException if writing to the underlying output stream failed
		 */
		private void disableRed() throws IOException {
			byte[] dis = "\u001B[0m".getBytes();
			super.write(dis, 0, dis.length);
			writing = false;
		}
	}
	
	/**
	 * Reads input from the user, but first disables prompt, completions,
	 * and history.
	 */
	private static class ConsoleInputReader implements InputReader {
		private final ConsoleReader reader;
		
		public ConsoleInputReader(ConsoleReader reader) {
			this.reader = reader;
		}
		
		@Override
		public String readLine(String prompt) throws IOException {
			boolean oldHistoryEnabled = reader.isHistoryEnabled();
			History oldHistory = reader.getHistory();
			Collection<Completer> oldCompleters =
					new ArrayList<Completer>(reader.getCompleters());
			String oldPrompt = reader.getPrompt();
			try {
				reader.setHistoryEnabled(false);
				reader.setHistory(new MemoryHistory());
				for (Completer c : oldCompleters) {
					reader.removeCompleter(c);
				}
				String result = reader.readLine(prompt);
				return result;
			} finally {
				for (Completer c : oldCompleters) {
					reader.addCompleter(c);
				}
				reader.setPrompt(oldPrompt);
				reader.setHistory(oldHistory);
				reader.setHistoryEnabled(oldHistoryEnabled);
			}
		}
	}
}

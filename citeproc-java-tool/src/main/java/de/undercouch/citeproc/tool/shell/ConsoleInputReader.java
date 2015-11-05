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

package de.undercouch.citeproc.tool.shell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import de.undercouch.underline.InputReader;
import jline.console.ConsoleReader;
import jline.console.completer.Completer;
import jline.console.history.History;
import jline.console.history.MemoryHistory;

/**
 * Reads input from the user, but first disables prompt, completions,
 * and history.
 * @author Michel Kraemer
 */
public class ConsoleInputReader implements InputReader {
	private final ConsoleReader reader;
	
	/**
	 * Constructs a new console input reader
	 * @param reader the underlying console reader
	 */
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

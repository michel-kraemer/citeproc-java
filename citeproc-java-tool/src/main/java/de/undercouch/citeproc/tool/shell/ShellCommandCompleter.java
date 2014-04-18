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

import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jline.console.completer.Completer;
import de.undercouch.citeproc.CSLTool;
import de.undercouch.citeproc.helper.tool.Command;
import de.undercouch.citeproc.helper.tool.InvalidOptionException;
import de.undercouch.citeproc.helper.tool.Option;
import de.undercouch.citeproc.helper.tool.OptionGroup;
import de.undercouch.citeproc.helper.tool.OptionIntrospector;
import de.undercouch.citeproc.helper.tool.OptionIntrospector.ID;
import de.undercouch.citeproc.tool.shell.ShellCommandParser.Result;

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
		this(Collections.<Class<? extends Command>>emptyList());
	}
	
	/**
	 * Constructs a new completer
	 * @param excludedCommands a list of CSLTool commands that should be
	 * excluded from completions
	 */
	public ShellCommandCompleter(List<Class<? extends Command>> excludedCommands) {
		this.excludedCommands = new HashSet<Class<? extends Command>>(excludedCommands);
	}
	
	@Override
	public int complete(String buffer, int cursor,
			List<CharSequence> candidates) {
		boolean allparsed;
		Set<String> result = new HashSet<String>();
		
		try {
			Result pr = ShellCommandParser.parse(buffer);
			if (pr.getRemainingArgs().length > 1) {
				//command line could not be parsed completely. we cannot
				//provide suggestions for more than one unrecognized argument.
				allparsed = false;
			} else {
				OptionGroup<ID> options;
				if (pr.getCommand() == CSLTool.class) {
					options = OptionIntrospector.introspect(pr.getCommand(),
							AdditionalShellCommands.class);
				} else {
					options = OptionIntrospector.introspect(pr.getCommand());
				}
				
				String[] ra = pr.getRemainingArgs();
				allparsed = (ra == null || ra.length == 0);
				
				//add completions
				for (Option<ID> o : options.getCommands()) {
					Class<? extends Command> cmd =
							OptionIntrospector.getCommand(o.getId());
					if (excludedCommands.contains(cmd)) {
						continue;
					}
					
					if (allparsed || o.getLongName().startsWith(ra[0])) {
						result.add(o.getLongName());
					}
				}
			}
		} catch (InvalidOptionException e) {
			//there's an option, we cannot calculate completions anymore
			//because options are not allowed in the interactive shell
			allparsed = false;
		} catch (IntrospectionException e) {
			throw new RuntimeException("Could not inspect command", e);
		}
		
		//sort completions
		List<String> resultList = new ArrayList<String>(result);
		Collections.sort(resultList);
		candidates.addAll(resultList);
		
		//determine place to insert completion
		int pos = buffer.length();
		if (!allparsed && pos > 0) {
			while (pos > 0 && Character.isWhitespace(buffer.charAt(pos - 1))) --pos;
			if (pos == 0) {
				//buffer consists of whitespaces only
				pos = buffer.length();
			}
			while (pos > 0 && !Character.isWhitespace(buffer.charAt(pos - 1))) --pos;
		} else if (allparsed && buffer.length() > 0 &&
				!Character.isWhitespace(buffer.charAt(buffer.length() - 1))) {
			++pos;
		}
		
		return candidates.isEmpty() ? -1 : pos;
	}
}

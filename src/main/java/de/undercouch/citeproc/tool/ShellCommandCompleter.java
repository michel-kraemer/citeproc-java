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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jline.console.completer.Completer;
import de.undercouch.citeproc.CSLTool;
import de.undercouch.citeproc.helper.tool.InvalidOptionException;
import de.undercouch.citeproc.helper.tool.MissingArgumentException;
import de.undercouch.citeproc.helper.tool.Option;
import de.undercouch.citeproc.helper.tool.OptionGroup;
import de.undercouch.citeproc.helper.tool.OptionIntrospector;
import de.undercouch.citeproc.helper.tool.OptionParser;
import de.undercouch.citeproc.helper.tool.OptionIntrospector.ID;
import de.undercouch.citeproc.helper.tool.OptionParser.Result;
import de.undercouch.citeproc.helper.tool.Value;

/**
 * Calculated completions for citeproc-java's interactive mode
 * @author Michel Kraemer
 */
class ShellCommandCompleter implements Completer {
	@Override
	public int complete(String buffer, int cursor,
			List<CharSequence> candidates) {
		boolean allparsed = false;
		List<String> result = new ArrayList<String>();
		
		//iterate through each argument and get the last command
		String[] args = buffer.split("\\s+");
		try {
			Class<?> commandClass = CSLTool.class;
			OptionGroup<ID> options;
			String[] remainingArgs = args;
			
			while (true) {
				//inspect current command
				options = OptionIntrospector.introspect(commandClass);
				
				if (remainingArgs == null || remainingArgs.length == 0) {
					allparsed = true;
					break;
				}
				
				try {
					//parse remaining arguments
					Result<ID> pr = OptionParser.parse(remainingArgs, options, null);
					
					//get last command in the list of arguments
					Value<ID> v = pr.getValues().get(pr.getValues().size() - 1);
					Class<?> cc = OptionIntrospector.getCommand(v.getId());
					if (cc != null) {
						commandClass = cc;
					}
					
					remainingArgs = pr.getRemainingArgs();
				} catch (InvalidOptionException e) {
					break;
				}
			}
			
			//skip flags
			int ra = 0;
			if (remainingArgs != null && remainingArgs.length > 0) {
				while (remainingArgs[ra].startsWith("-")) ++ra;
			}
			
			//add completions
			for (Option<ID> o : options.getCommands()) {
				if (remainingArgs == null || remainingArgs.length == 0 ||
						o.getLongName().startsWith(remainingArgs[ra])) {
					result.add(o.getLongName());
				}
			}
		} catch (IntrospectionException e) {
			throw new RuntimeException("Could not inspect command", e);
		} catch (MissingArgumentException e) {
			//ignore. just don't return any completions.
		}
		
		//sort completions
		Collections.sort(result);
		candidates.addAll(result);
		
		//determine place to insert completion
		int pos = buffer.length();
		if (!allparsed && pos > 0) {
			while (pos > 0 && Character.isWhitespace(buffer.charAt(pos - 1))) --pos;
			while (pos > 0 && !Character.isWhitespace(buffer.charAt(pos - 1))) --pos;
		} else if (allparsed && buffer.length() > 0 &&
				!Character.isWhitespace(buffer.charAt(buffer.length() - 1))) {
			++pos;
		}
		
		return candidates.isEmpty() ? -1 : pos;
	}
}

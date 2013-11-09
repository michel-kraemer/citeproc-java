// Copyright 2013 Michel Kraemer
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
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import de.undercouch.citeproc.CSLTool;
import de.undercouch.citeproc.helper.tool.Command;
import de.undercouch.citeproc.helper.tool.Option;
import de.undercouch.citeproc.helper.tool.OptionGroup;
import de.undercouch.citeproc.helper.tool.OptionIntrospector;
import de.undercouch.citeproc.helper.tool.OptionIntrospector.ID;
import de.undercouch.citeproc.helper.tool.OptionParserException;
import de.undercouch.citeproc.helper.tool.UnknownAttributes;

/**
 * Displays a command's help
 * @author Michel Kraemer
 */
public class HelpCommand extends AbstractCSLToolCommand {
	private List<String> commands = new ArrayList<String>();
	
	/**
	 * Sets the commands to display the help for
	 * @param commands the commands
	 */
	@UnknownAttributes
	public void setCommands(List<String> commands) {
		this.commands = commands;
	}
	
	@Override
	public String getUsageDescription() {
		return "Display a command's help";
	}

	@Override
	public String getUsageArguments() {
		return "help [COMMAND] [SUBCOMMAND]";
	}

	@Override
	public int doRun(String[] remainingArgs, PrintStream out)
			throws OptionParserException, IOException {
		Class<? extends Command> cmd = CSLTool.class;
		
		try {
			for (String c : commands) {
				OptionGroup<ID> options = OptionIntrospector.introspect(cmd);
				boolean found = false;
				for (Option<ID> o : options.getCommands()) {
					if (c.equals(o.getLongName())) {
						cmd = OptionIntrospector.getCommand(o.getId());
						found = true;
						break;
					}
				}
				if (!found) {
					error("unknown command `" + c + "'");
					return 1;
				}
			}
		} catch (IntrospectionException e) {
			throw new RuntimeException("Could not inspect command", e);
		}
		
		try {
			return cmd.newInstance().run(new String[] { "-h" }, out);
		} catch (InstantiationException e) {
			error("command could not be instantiated");
		} catch (IllegalAccessException e) {
			error("command could not be accessed");
		}
		return 1;
	}
}

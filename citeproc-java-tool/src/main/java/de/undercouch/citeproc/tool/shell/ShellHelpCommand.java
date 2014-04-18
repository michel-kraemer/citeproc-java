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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import de.undercouch.citeproc.CSLTool;
import de.undercouch.citeproc.helper.tool.Command;
import de.undercouch.citeproc.helper.tool.InputReader;
import de.undercouch.citeproc.helper.tool.Option;
import de.undercouch.citeproc.helper.tool.OptionGroup;
import de.undercouch.citeproc.helper.tool.OptionIntrospector;
import de.undercouch.citeproc.helper.tool.OptionIntrospector.ID;
import de.undercouch.citeproc.helper.tool.OptionParser;
import de.undercouch.citeproc.helper.tool.OptionParserException;
import de.undercouch.citeproc.helper.tool.UnknownAttributes;
import de.undercouch.citeproc.tool.AbstractCSLToolCommand;
import de.undercouch.citeproc.tool.CSLToolCommand;
import de.undercouch.citeproc.tool.ShellCommand;

/**
 * Displays help about a command in the interactive shell
 * @author Michel Kraemer
 */
public class ShellHelpCommand extends AbstractCSLToolCommand {
	private List<String> commands = new ArrayList<String>();
	
	/**
	 * Sets the commands to display the help for
	 * @param commands the commands
	 */
	@UnknownAttributes("COMMAND")
	public void setCommands(List<String> commands) {
		this.commands = commands;
	}
	
	@Override
	public String getUsageName() {
		return "help";
	}
	
	@Override
	public String getUsageDescription() {
		return "Display help for a given command";
	}
	
	@Override
	public int doRun(String[] remainingArgs, InputReader in, PrintWriter out)
			throws OptionParserException, IOException {
		Class<? extends Command> cmdClass;
		
		String[] args = commands.toArray(new String[commands.size()]);
		
		OptionGroup<ID> options;
		try {
			ShellCommandParser.Result pr = ShellCommandParser.parse(
					args, ShellCommand.EXCLUDED_COMMANDS);
			String[] ra = pr.getRemainingArgs();
			if (ra.length > 0) {
				error("unknown command `" + ra[0] + "'");
				return 1;
			}
			
			cmdClass = pr.getLastCommand();
			if (cmdClass == null) {
				options = OptionIntrospector.introspect(CSLTool.class,
						AdditionalShellCommands.class);
			} else {
				options = OptionIntrospector.introspect(cmdClass);
			}
		} catch (IntrospectionException e) {
			//should never happen
			throw new RuntimeException(e);
		}
		
		OptionGroup<ID> filtered = new OptionGroup<ID>();
		for (Option<ID> cmd : options.getCommands()) {
			Class<? extends Command> cc =
					OptionIntrospector.getCommand(cmd.getId());
			if (!ShellCommand.EXCLUDED_COMMANDS.contains(cc)) {
				filtered.addCommand(cmd);
			}
		}
		
		CSLToolCommand cmd = null;
		if (cmdClass != null) {
			try {
				cmd = (CSLToolCommand)cmdClass.newInstance();
			} catch (Exception e) {
				//should never happen
				throw new RuntimeException(e);
			}
		}
		
		if (cmdClass == null) {
			OptionParser.usage(null, null, filtered, null, out);
		} else {
			String unknownArguments = OptionIntrospector.getUnknownArgumentName(
					cmdClass);
			OptionParser.usage(cmd.getUsageName(), cmd.getUsageDescription(),
					filtered, unknownArguments, null, out);
		}

		return 0;
	}
}

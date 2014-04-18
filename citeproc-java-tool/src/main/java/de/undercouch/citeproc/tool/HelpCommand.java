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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import de.undercouch.citeproc.CSLTool;
import de.undercouch.citeproc.helper.tool.Command;
import de.undercouch.citeproc.helper.tool.InputReader;
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
		return "Display a command's help";
	}

	@Override
	public int doRun(String[] remainingArgs, InputReader in, PrintWriter out)
			throws OptionParserException, IOException {
		//simply forward commands to CSLTool and append '-h'
		Command cmd = new CSLTool();
		String[] args = commands.toArray(new String[commands.size() + 1]);
		args[args.length - 1] = "-h";
		cmd.run(args, in, out);
		return 1;
	}
}

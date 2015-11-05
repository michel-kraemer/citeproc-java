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
import java.io.PrintWriter;

import de.undercouch.citeproc.tool.AbstractCSLToolCommand;
import de.undercouch.underline.CommandDesc;
import de.undercouch.underline.CommandDescList;
import de.undercouch.underline.InputReader;
import de.undercouch.underline.OptionParserException;

/**
 * Assign values to variables that affect the operation of the interactive shell
 * @author Michel Kraemer
 */
public class ShellSetCommand extends AbstractCSLToolCommand {
	private AbstractCSLToolCommand subcommand;
	
	@Override
	public String getUsageName() {
		return "set";
	}

	@Override
	public String getUsageDescription() {
		return "Assign values to variables that affect the operation "
				+ "of the interactive shell";
	}
	
	/**
	 * Sets the subcommand to delegate to
	 * @param subcommand the subcommand
	 */
	@CommandDescList({
		@CommandDesc(longName = "style",
				description = "set the current citation style",
				command = ShellSetStyleCommand.class),
		@CommandDesc(longName = "locale",
				description = "set the current citation locale",
				command = ShellSetLocaleCommand.class),
		@CommandDesc(longName = "format",
				description = "set the current output format",
				command = ShellSetFormatCommand.class),
	})
	public void setSubcommand(AbstractCSLToolCommand subcommand) {
		this.subcommand = subcommand;
	}
	
	@Override
	public boolean checkArguments() {
		if (subcommand == null) {
			error("no variable specified");
			return false;
		}
		return super.checkArguments();
	}

	@Override
	public int doRun(String[] remainingArgs, InputReader in, PrintWriter out)
			throws OptionParserException, IOException {
		return subcommand.run(remainingArgs, in, out);
	}
}

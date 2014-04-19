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

import de.undercouch.citeproc.helper.tool.CommandDesc;
import de.undercouch.citeproc.helper.tool.CommandDescList;
import de.undercouch.citeproc.tool.AbstractCSLToolCommand;

/**
 * Contains the configuration for all additional shell commands
 * @author Michel Kraemer
 */
public final class AdditionalShellCommands {
	/**
	 * Configures all additional shell commands
	 * @param command the configured command
	 */
	@CommandDescList({
		@CommandDesc(longName = "load",
				description = "load an input bibliography from a file",
				command = ShellLoadCommand.class),
		@CommandDesc(longName = "get",
				description = "get values of shell variables",
				command = ShellGetCommand.class),
		@CommandDesc(longName = "set",
				description = "assign values to shell variables",
				command = ShellSetCommand.class),
		@CommandDesc(longName = "help",
				description = "display help for a given command",
				command = ShellHelpCommand.class),
		@CommandDesc(longName = "exit",
				description = "exit the interactive shell",
				command = ShellExitCommand.class),
		@CommandDesc(longName = "quit",
				description = "exit the interactive shell",
				command = ShellQuitCommand.class),
	})
	public void setCommand(AbstractCSLToolCommand command) {
		//we don't have to do anything here
	}
}

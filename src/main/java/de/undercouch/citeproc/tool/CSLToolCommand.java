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
import java.io.PrintStream;

import de.undercouch.citeproc.helper.tool.Command;
import de.undercouch.citeproc.helper.tool.OptionParserException;

/**
 * An interface for commands from the {@link de.undercouch.citeproc.CSLTool}
 * @author Michel Kraemer
 */
public interface CSLToolCommand extends Command {
	/**
	 * @return the command description that should be displayed in the help
	 */
	String getUsageDescription();

	/**
	 * @return the arguments that should be displayed in the help
	 * including the command's name itself
	 */
	String getUsageArguments();

	/**
	 * Checks the provided arguments
	 * @return true if all arguments are OK, false otherwise
	 */
	boolean checkArguments();

	/**
	 * Runs the command
	 * @param remainingArgs arguments that have not been parsed yet, can
	 * be forwarded to subcommands
	 * @param out a stream to write the output to
	 * @return the exit code
	 * @throws OptionParserException if the remaining arguments could not be parsed
	 * @throws IOException if input files could not be read or the output
	 * stream could not be written
	 */
	int doRun(String[] remainingArgs, PrintStream out)
			throws OptionParserException, IOException;
}

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

package de.undercouch.citeproc.helper.tool;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * A command line interface command
 * @author Michel Kraemer
 */
public interface Command {
	/**
	 * Executes the command
	 * @param args the command's arguments
	 * @param in a stream from which user input can be read
	 * @param out a stream to write the output to
	 * @return the command's exit code
	 * @throws OptionParserException if the arguments could not be parsed
	 * @throws IOException if input files could not be read or the output
	 * stream could not be written
	 */
	int run(String[] args, InputReader in, PrintWriter out)
			throws OptionParserException, IOException;
}

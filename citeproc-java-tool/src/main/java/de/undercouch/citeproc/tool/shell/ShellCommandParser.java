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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import de.undercouch.citeproc.CSLTool;
import de.undercouch.citeproc.helper.tool.Command;
import de.undercouch.citeproc.helper.tool.Option;
import de.undercouch.citeproc.helper.tool.OptionGroup;
import de.undercouch.citeproc.helper.tool.OptionIntrospector;
import de.undercouch.citeproc.helper.tool.OptionIntrospector.ID;

/**
 * Parses command lines in the interactive shell
 * @author Michel Kraemer
 */
public final class ShellCommandParser {
	private ShellCommandParser() {
		//hidden constructor
	}
	
	/**
	 * Parser result
	 */
	public static class Result {
		private final String[] remainingArgs;
		private final Class<? extends Command> command;
		
		private Result(String[] remainingArgs, Class<? extends Command> command) {
			this.remainingArgs = remainingArgs;
			this.command = command;
		}
		
		/**
		 * @return the remaining (unparsed) arguments
		 */
		public String[] getRemainingArgs() {
			return remainingArgs;
		}
		
		/**
		 * @return the class of the last parsed command
		 */
		public Class<? extends Command> getCommand() {
			return command;
		}
	}
	
	/**
	 * Parses a shell command line
	 * @param line the command line
	 * @return the parser result
	 * @throws IntrospectionException if a {@link de.undercouch.citeproc.tool.CSLToolCommand}
	 * could not be introspected
	 */
	public static Result parse(String line) throws IntrospectionException {
		return parse(line, Collections.<Class<? extends Command>>emptyList());
	}
	
	/**
	 * Parses a shell command line
	 * @param line the command line
	 * @param excluded a list of commands that should not be parsed
	 * @return the parser result
	 * @throws IntrospectionException if a {@link de.undercouch.citeproc.tool.CSLToolCommand}
	 * could not be introspected
	 */
	public static Result parse(String line, List<Class<? extends Command>> excluded)
			throws IntrospectionException {
		String[] args = line.trim().split("\\s+");
		return parse(args, excluded);
	}
	
	/**
	 * Parses arguments of a shell command line
	 * @param args the arguments to parse
	 * @param excluded a list of commands that should not be parsed
	 * @return the parser result
	 * @throws IntrospectionException if a {@link de.undercouch.citeproc.tool.CSLToolCommand}
	 * could not be introspected
	 */
	public static Result parse(String[] args, List<Class<? extends Command>> excluded)
			throws IntrospectionException {
		return getCommandClass(args, 0, CSLTool.class,
				new HashSet<Class<? extends Command>>(excluded));
	}
	
	private static Result getCommandClass(String[] args, int i,
			Class<? extends Command> cls, Set<Class<? extends Command>> excluded)
					throws IntrospectionException {
		if (i >= args.length) {
			return new Result(new String[0], cls);
		}
		
		OptionGroup<ID> options;
		if (cls == CSLTool.class) {
			options = OptionIntrospector.introspect(cls,
					AdditionalShellCommands.class);
		} else {
			options = OptionIntrospector.introspect(cls);
		}
		
		List<Option<ID>> commands = options.getCommands();
		if (commands != null) {
			for (Option<ID> cmd : commands) {
				if (cmd.getLongName().equals(args[i])) {
					Class<? extends Command> cmdClass =
							OptionIntrospector.getCommand(cmd.getId());
					if (!excluded.contains(cmdClass)) {
						return getCommandClass(args, i + 1, cmdClass, excluded);
					}
				}
			}
		}
		
		return new Result(ArrayUtils.subarray(args, i, args.length), cls);
	}
}

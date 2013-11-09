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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import de.undercouch.citeproc.helper.tool.Option.ArgumentType;

/**
 * Parses command line options and prints usage information
 * @author Michel Kraemer
 */
public class OptionParser {
	/**
	 * Parser results
	 * @param <T> option identifier type
	 */
	public static class Result<T> {
		private final List<Value<T>> values;
		private final String[] remainingArgs;
		
		/**
		 * Constructs new parser results
		 * @param values the parsed values
		 * @param remainingArgs the command line arguments that have not
		 * been parsed
		 */
		public Result(List<Value<T>> values, String[] remainingArgs) {
			this.values = values;
			this.remainingArgs = remainingArgs;
		}
		
		/**
		 * @return the parsed values
		 */
		public List<Value<T>> getValues() {
			return values;
		}
		
		/**
		 * @return the command line arguments that have not been parsed
		 */
		public String[] getRemainingArgs() {
			return remainingArgs;
		}
		
		/**
		 * @return true if the list of values is empty
		 */
		public boolean isEmpty() {
			return values.isEmpty();
		}
	}
	
	/**
	 * Print usage information
	 * @param <T> option identifier type
	 * @param command the command that has to be called in order
	 * to use the application
	 * @param description the application's description
	 * @param options the options to print out
	 * @param out destination stream
	 */
	public static <T> void usage(String command, String description,
			OptionGroup<T> options, PrintStream out) {
		out.println("Usage: " + command);
		out.println(description);
		out.println();
		
		if (options == null) {
			return;
		}
		
		//calculate column widths
		int firstColumnWidth = 0;
		int secondColumnWidth = 0;
		for (Option<T> o : options.getFlatOptions()) {
			//calculate width of first column (short name)
			if (o.getShortName() != null) {
				int fcw = o.getShortName().length() + 1;
				if (fcw > firstColumnWidth) {
					firstColumnWidth = fcw;
				}
			}
			
			//calculate width of second column (long name)
			int scw = o.getLongName().length() + 2;
			if (o.getArgumentType() != ArgumentType.NONE) {
				scw += o.getArgumentName().length() + 3;
			}
			if (scw > secondColumnWidth) {
				secondColumnWidth = scw;
			}
		}
		
		//extend columns if there are commands
		if (options.getCommands() != null && !options.getCommands().isEmpty()) {
			for (Option<T> cmd : options.getCommands()) {
				int scw = cmd.getLongName().length() - firstColumnWidth - 1;
				if (scw > secondColumnWidth) {
					secondColumnWidth = scw;
				}
			}
		}
		
		//output options
		printOptions(options, out, firstColumnWidth, secondColumnWidth);
		
		//output commands
		if (options.getCommands() != null && !options.getCommands().isEmpty()) {
			out.println();
			out.println("Commands:");
			for (Option<T> cmd : options.getCommands()) {
				out.print("  " + cmd.getLongName());
				int lnl = cmd.getLongName().length();
				int pad = secondColumnWidth + firstColumnWidth - lnl + 3;
				while (pad > 0) {
					out.print(" ");
					--pad;
				}
				out.println(cmd.getDescription());
			}
		}
	}

	private static <T> void printOptions(OptionGroup<T> options, PrintStream out,
			int firstColumnWidth, int secondColumnWidth) {
		//print group name (if any)
		if (options.getName() != null && !options.getName().isEmpty()) {
			out.println();
			out.println(options.getName());
		}
		
		for (Option<T> o : options.getOptions()) {
			out.print("  ");
			
			//output short name
			if (firstColumnWidth > 0) {
				int snl = 0;
				if (o.getShortName() != null) {
					out.print("-" + o.getShortName() + ",");
					snl = o.getShortName().length();
				} else {
					out.print("  ");
				}
				int pad = firstColumnWidth - snl - 1;
				while (pad > 0) {
					out.print(" ");
					--pad;
				}
			}
			
			//output long name
			int lnl = o.getLongName().length();
			out.print("--" + o.getLongName());
			if (o.getArgumentType() != ArgumentType.NONE) {
				out.print(" <" + o.getArgumentName() + ">");
				lnl += o.getArgumentName().length() + 3;
			}
			int pad = secondColumnWidth - lnl - 1;
			while (pad > 0) {
				out.print(" ");
				--pad;
			}
			
			//output description (wrap it if needed)
			out.print(" ");
			int w = firstColumnWidth + secondColumnWidth + 4;
			if (firstColumnWidth > 0) {
				w++;
			}
			String desc = o.getDescription();
			while (w + desc.length() > 75) {
				int sp = desc.lastIndexOf(' ', 74 - w);
				if (sp == -1) {
					break;
				}
				out.println(desc.substring(0, sp));
				for (int i = 0; i < w; ++i) {
					out.print(" ");
				}
				desc = desc.substring(sp + 1);
			}
			out.println(desc);
		}
		
		//print children
		for (OptionGroup<T> c : options.getChildren()) {
			printOptions(c, out, firstColumnWidth, secondColumnWidth);
		}
	}
	
	/**
	 * Parses a command line and returns an object containing a list of
	 * parsed values. The method stops at the first command it encounters. The
	 * remaining arguments not parsed can be retrieved via the result object's
	 * {@link Result#getRemainingArgs()} method.
	 * @param <T> option identifier type
	 * @param args the command line
	 * @param options the options to parse
	 * @param def the default option identifier for parameters
	 * that are neither options nor commands
	 * @return the parsed options
	 * @throws MissingArgumentException if an option misses a required argument
	 * @throws InvalidOptionException if one of the arguments is unknown
	 */
	public static <T> Result<T> parse(String[] args, OptionGroup<T> options,
			T def) throws MissingArgumentException, InvalidOptionException {
		List<Value<T>> result = new ArrayList<Value<T>>();
		int i;
		for (i = 0; i < args.length; ++i) {
			String a = args[i];
			
			boolean found = false;
			boolean foundCommand = false;
			
			if (a.startsWith("--")) {
				//handle long name options
				String an = a.substring(2);
				for (Option<T> o : options.getFlatOptions()) {
					if (o.getLongName().equals(an)) {
						i += parseValue(o, args, i, result);
						found = true;
						break;
					}
				}
			} else if (a.startsWith("-")) {
				//handle short name options
				String an = a.substring(1);
				for (Option<T> o : options.getFlatOptions()) {
					if (o.getShortName() != null && o.getShortName().equals(an)) {
						i += parseValue(o, args, i, result);
						found = true;
						break;
					}
				}
			} else {
				//handle arguments that are not options
				for (Option<T> cd : options.getCommands()) {
					if (cd.getLongName().equals(a)) {
						result.add(new Value<T>(cd.getId(), a));
						found = true;
						foundCommand = true;
						break;
					}
				}
				
				//handle generic arguments
				if (!found && def != null) {
					result.add(new Value<T>(def, a));
					found = true;
				}
			}
			
			if (!found) {
				throw new InvalidOptionException(args[i]);
			}
			
			if (foundCommand) {
				//we found a command. stop parsing here
				++i; //skip command
				break;
			}
		}
		
		//save arguments not parsed
		String[] remainingArgs = new String[args.length - i];
		System.arraycopy(args, i, remainingArgs, 0, remainingArgs.length);
		
		return new Result<T>(result, remainingArgs);
	}
	
	/**
	 * Parses an option's value
	 * @param <T> option identifier type
	 * @param o the option to parse
	 * @param args the command line
	 * @param i the index of the command line argument to parse
	 * @param result a list of parsed values where the resulting value
	 * should be added to
	 * @return the number of arguments to skip after this method has returned
	 * @throws MissingArgumentException if the option misses a required argument
	 */
	private static <T> int parseValue(Option<T> o, String[] args, int i,
			List<Value<T>> result) throws MissingArgumentException {
		int skip = 0;
		Object value = null;
		
		switch (o.getArgumentType()) {
		case STRING:
			if (args.length <= i + 1 || args[i + 1].startsWith("-")) {
				throw new MissingArgumentException(args[i], o.getArgumentName());
			}
			value = args[i + 1];
			skip = 1;
			break;
		
		case NONE:
			break;
		}
		
		result.add(new Value<T>(o.getId(), value));
		
		return skip;
	}
}

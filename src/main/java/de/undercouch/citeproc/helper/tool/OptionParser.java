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
		
		//output options
		printOptions(options, out, firstColumnWidth, secondColumnWidth);
	}

	private static <T> void printOptions(OptionGroup<T> options, PrintStream out,
			int firstColumnWidth, int secondColumnWidth) {
		//print group name (if any)
		if (options.getName() != null && !options.getName().isEmpty()) {
			System.out.println();
			System.out.println(options.getName());
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
	 * Parses a command line and returns a list of parsed values
	 * @param <T> option identifier type
	 * @param args the command line
	 * @param options the options to parse
	 * @param def the default option identifier for parameters
	 * that are not options
	 * @return a list of parsed values
	 * @throws MissingArgumentException if an option misses a required argument
	 * @throws InvalidOptionException if one of the arguments is unknown
	 */
	public static <T> List<Value<T>> parse(String[] args, OptionGroup<T> options,
			T def) throws MissingArgumentException, InvalidOptionException {
		List<Value<T>> result = new ArrayList<Value<T>>();
		for (int i = 0; i < args.length; ++i) {
			String a = args[i];
			
			boolean found = false;
			
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
				result.add(new Value<T>(def, a));
				found = true;
			}
			
			if (!found) {
				throw new InvalidOptionException(args[i]);
			}
		}
		return result;
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

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

/**
 * Command line tool option. Each option has an identifier that is
 * used in the list of result values returned by {@link OptionParser} to
 * link {@link Value}s with {@link Option}s.
 * @author Michel Kraemer
 * @param <T> identifier type
 */
public class Option<T> {
	/**
	 * Type of an option's argument
	 */
	public static enum ArgumentType {
		/**
		 * The option has no argument
		 */
		NONE,
		
		/**
		 * The argument is a string
		 */
		STRING
	}
	
	private final T id;
	private final String shortName;
	private final String longName;
	private final String description;
	private final String argumentName;
	private final ArgumentType argumentType;
	
	/**
	 * Constructs a new option without arguments
	 * @param id the option's identifier
	 * @param longName the option's long name
	 * @param description a human-readable description
	 */
	public Option(T id, String longName, String description) {
		this(id, longName, null, description);
	}
	
	/**
	 * Constructs a new option without arguments
	 * @param id the option's identifier
	 * @param longName the option's long name
	 * @param shortName the option's short name (may be null if the option
	 * should only have a long name)
	 * @param description a human-readable description
	 */
	public Option(T id, String longName, String shortName, String description) {
		this(id, longName, shortName, description, null, ArgumentType.NONE);
	}
	
	/**
	 * Constructs a new option with a required argument
	 * @param id the option's identifier
	 * @param longName the option's long name
	 * @param shortName the option's short name (may be null if the option
	 * should only have a long name)
	 * @param description a human-readable description
	 * @param argumentName the argument's name (may be null if the option
	 * should not have an argument)
	 * @param argumentType the argument's type (may be {@link ArgumentType#NONE}
	 * if <code>argumentName</code> is <code>null</code>
	 */
	public Option(T id, String longName, String shortName, String description,
			String argumentName, ArgumentType argumentType) {
		this.id = id;
		this.longName = longName;
		this.shortName = shortName;
		this.description = description;
		this.argumentName = argumentName;
		this.argumentType = argumentType;
	}
	
	/**
	 * @return the option's identifier
	 */
	public T getId() {
		return id;
	}
	
	/**
	 * @return the option's long name
	 */
	public String getLongName() {
		return longName;
	}
	
	/**
	 * @return the option's short name (might be null if the
	 * option only has a long name)
	 */
	public String getShortName() {
		return shortName;
	}
	
	/**
	 * @return a human-readable description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * @return the argument's name (might be null if the option
	 * does not have an argument)
	 */
	public String getArgumentName() {
		return argumentName;
	}
	
	/**
	 * @return the argument's type (might be {@link ArgumentType#NONE}
	 * if the option does not have an argument)
	 */
	public ArgumentType getArgumentType() {
		return argumentType;
	}
}

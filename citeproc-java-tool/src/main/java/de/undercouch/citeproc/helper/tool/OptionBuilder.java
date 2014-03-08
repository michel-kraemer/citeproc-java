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

import de.undercouch.citeproc.helper.tool.Option.ArgumentType;

/**
 * Builder for a group of command line options
 * @author Michel Kraemer
 * @param <T> identifier type
 */
public class OptionBuilder<T> {
	private final OptionGroup<T> options;
	
	/**
	 * Creates a new option builder
	 */
	public OptionBuilder() {
		options = new OptionGroup<T>();
	}
	
	/**
	 * Creates a new option builder
	 * @param groupName the name of the option group the builder will create
	 * when calling {@link #build()}
	 */
	public OptionBuilder(String groupName) {
		options = new OptionGroup<T>(groupName);
	}

	/**
	 * Adds a new option without arguments
	 * @param id the option's identifier
	 * @param longName the option's long name
	 * @param description a human-readable description
	 * @return this option builder
	 */
	public OptionBuilder<T> add(T id, String longName, String description) {
		options.addOption(new Option<T>(id, longName, description));
		return this;
	}
	
	/**
	 * Adds a new option without arguments
	 * @param id the option's identifier
	 * @param longName the option's long name
	 * @param shortName the option's short name (may be null if the option
	 * should only have a long name)
	 * @param description a human-readable description
	 * @return this option builder
	 */
	public OptionBuilder<T> add(T id, String longName, String shortName, String description) {
		options.addOption(new Option<T>(id, longName, shortName, description));
		return this;
	}
	
	/**
	 * Adds a new option with a required argument
	 * @param id the option's identifier
	 * @param longName the option's long name
	 * @param shortName the option's short name (may be null if the option
	 * should only have a long name)
	 * @param description a human-readable description
	 * @param argumentName the argument's name (may be null if the option
	 * should not have an argument)
	 * @param argumentType the argument's type (may be {@link ArgumentType#NONE}
	 * if <code>argumentName</code> is <code>null</code>
	 * @return this option builder
	 */
	public OptionBuilder<T> add(T id, String longName, String shortName, String description,
			String argumentName, ArgumentType argumentType) {
		options.addOption(new Option<T>(id, longName, shortName, description, argumentName, argumentType));
		return this;
	}
	
	/**
	 * Adds a new option group
	 * @param group the group to add
	 * @return this option builder
	 */
	public OptionBuilder<T> add(OptionGroup<T> group) {
		options.addChild(group);
		return this;
	}
	
	/**
	 * Adds a new command
	 * @param id the commands's identifier
	 * @param longName the commands's long name
	 * @param description a human-readable description
	 * @return this option builder
	 */
	public OptionBuilder<T> addCommand(T id, String longName, String description) {
		this.options.addCommand(new Option<T>(id, longName, description));
		return this;
	}
	
	/**
	 * @return the built group of options
	 */
	public OptionGroup<T> build() {
		return options;
	}
}

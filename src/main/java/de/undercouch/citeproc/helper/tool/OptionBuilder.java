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

import java.util.ArrayList;
import java.util.List;

import de.undercouch.citeproc.helper.tool.Option.ArgumentType;

/**
 * Builder for a list of command line options
 * @author Michel Kraemer
 * @param <T> identifier type
 */
public class OptionBuilder<T> {
	private List<Option<T>> options = new ArrayList<Option<T>>();

	/**
	 * Adds a new option without arguments
	 * @param id the option's identifier
	 * @param longName the option's long name
	 * @param description a human-readable description
	 * @return this option builder
	 */
	public OptionBuilder<T> add(T id, String longName, String description) {
		options.add(new Option<T>(id, longName, description));
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
		options.add(new Option<T>(id, longName, shortName, description));
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
		options.add(new Option<T>(id, longName, shortName, description, argumentName, argumentType));
		return this;
	}
	
	/**
	 * @return the built list of options
	 */
	public List<Option<T>> build() {
		return options;
	}
}

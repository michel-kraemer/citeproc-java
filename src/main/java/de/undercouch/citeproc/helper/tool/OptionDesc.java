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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.undercouch.citeproc.helper.tool.Option.ArgumentType;

/**
 * A command line option's descriptor
 * @author Michel Kraemer
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OptionDesc {
	/**
	 * @return the option's long name
	 */
	String longName();
	
	/**
	 * @return the option's short name (may be empty if the option
	 * should only have a long name)
	 */
	String shortName() default "";
	
	/**
	 * @return a human-readable description
	 */
	String description();
	
	/**
	 * @return the argument's name (may be empty if the option
	 * should not have an argument)
	 */
	String argumentName() default "";
	
	/**
	 * @return the argument's type (may be {@link ArgumentType#NONE}
	 * if {@link #argumentName()} is empty
	 */
	ArgumentType argumentType() default ArgumentType.NONE;
	
	/**
	 * Returns the option's priority. The priority is used to sort options
	 * in particular if they are displayed in the help on the console. The
	 * default priority is 50. Use lower values to put options before default
	 * ones and higher values to put them after them.
	 * @return the priority
	 */
	int priority() default 50;
}

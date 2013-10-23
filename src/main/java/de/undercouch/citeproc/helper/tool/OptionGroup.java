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

/**
 * A group of command line options
 * @author Michel Kraemer
 * @param <T> identifier type
 */
public class OptionGroup<T> {
	private final String name;
	private final List<Option<T>> options = new ArrayList<Option<T>>();
	private final List<OptionGroup<T>> children = new ArrayList<OptionGroup<T>>();
	
	/**
	 * Creates a new option group with no name
	 */
	public OptionGroup() {
		this.name = "";
	}
	
	/**
	 * Creates a new option group
	 * @param name the group's name
	 */
	public OptionGroup(String name) {
		this.name = name;
	}
	
	/**
	 * @return the group's name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return the options in this group
	 */
	public List<Option<T>> getOptions() {
		return options;
	}
	
	/**
	 * @return a flat list of options in this group and all options from all children
	 */
	public List<Option<T>> getFlatOptions() {
		List<Option<T>> result = new ArrayList<Option<T>>();
		result.addAll(getOptions());
		for (OptionGroup<T> g : getChildren()) {
			result.addAll(g.getFlatOptions());
		}
		return result;
	}
	
	/**
	 * @return the group's children
	 */
	public List<OptionGroup<T>> getChildren() {
		return children;
	}
	
	/**
	 * Adds an option to this group
	 * @param o the option to add
	 */
	public void addOption(Option<T> o) {
		options.add(o);
	}
	
	/**
	 * Adds a child to this group
	 * @param c the child to add
	 */
	public void addChild(OptionGroup<T> c) {
		children.add(c);
	}
}

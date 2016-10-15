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

package de.undercouch.citeproc;

import java.util.HashMap;
import java.util.Map;

import de.undercouch.citeproc.csl.CSLAbbreviationList;

/**
 * Default implementation of {@link AbbreviationProvider}
 * @author Michel Kraemer
 */
public class DefaultAbbreviationProvider implements AbbreviationProvider {
	private final Map<String, CSLAbbreviationList> lists = new HashMap<>();
	
	/**
	 * Adds an abbreviation list to this provider
	 * @param name the list's name
	 * @param list the list
	 */
	public void add(String name, CSLAbbreviationList list) {
		lists.put(name, list);
	}
	
	@Override
	public CSLAbbreviationList getAbbreviations(String name) {
		return lists.get(name);
	}
}

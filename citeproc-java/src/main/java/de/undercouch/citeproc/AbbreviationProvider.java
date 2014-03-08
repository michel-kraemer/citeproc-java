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

import de.undercouch.citeproc.csl.CSLAbbreviationList;

/**
 * Retrieves abbreviations for titles, authorities, institution names, etc.
 * @author Michel Kraemer
 */
public interface AbbreviationProvider {
	/**
	 * A list name that can be used for default abbreviations that do not
	 * have to be enabled via {@link CSL#setAbbreviations(String)}
	 */
	static final String DEFAULT_LIST_NAME = "default";
	
	/**
	 * Retrieves an abbreviation list with a given name
	 * @param name the list's name
	 * @return the abbreviation list or null if there is no such list
	 */
	CSLAbbreviationList getAbbreviations(String name);
}

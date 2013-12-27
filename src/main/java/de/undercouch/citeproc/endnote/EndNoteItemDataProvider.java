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

package de.undercouch.citeproc.endnote;

import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.ListItemDataProvider;

/**
 * Loads citation items from an EndNote library
 * @author Michel Kraemer
 */
public class EndNoteItemDataProvider extends ListItemDataProvider {
	/**
	 * Adds the given library
	 * @param lib the library to add
	 */
	public void addLibrary(EndNoteLibrary lib) {
		items.putAll(new EndNoteConverter().toItemData(lib));
	}
	
	/**
	 * Introduces all citation items from the EndNote libraries added
	 * via {@link #addLibrary(EndNoteLibrary)} to the given CSL processor
	 * @see CSL#registerCitationItems(String[])
	 * @param citeproc the CSL processor
	 */
	public void registerCitationItems(CSL citeproc) {
		citeproc.registerCitationItems(getIds());
	}
}

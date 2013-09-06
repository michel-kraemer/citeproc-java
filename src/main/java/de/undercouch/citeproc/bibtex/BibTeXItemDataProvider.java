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

package de.undercouch.citeproc.bibtex;

import java.util.HashMap;
import java.util.Map;

import org.jbibtex.BibTeXDatabase;

import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.ItemDataProvider;
import de.undercouch.citeproc.csl.CSLItemData;

/**
 * Loads citation items from a BibTeX database
 * @author Michel Kraemer
 */
public class BibTeXItemDataProvider implements ItemDataProvider {
	private Map<String, CSLItemData> items = new HashMap<String, CSLItemData>();
	
	/**
	 * Adds the given database. You should call {@link #registerCitationItems(CSL)}
	 * after you added all databases.
	 * @param db the database to add
	 */
	public void addDatabase(BibTeXDatabase db) {
		items.putAll(new BibTeXConverter().toItemData(db));
	}
	
	/**
	 * Introduces all citation items from the BibTeX databases added
	 * via {@link #addDatabase(BibTeXDatabase)} to the given CSL processor
	 * @see CSL#registerCitationItems(String[])
	 * @param citeproc the CSL processor
	 */
	public void registerCitationItems(CSL citeproc) {
		String[] ids = new String[items.size()];
		int i = 0;
		for (Map.Entry<String, CSLItemData> e : items.entrySet()) {
			ids[i++] = e.getKey();
		}
		citeproc.registerCitationItems(ids);
	}
	
	@Override
	public CSLItemData retrieveItem(String id) {
		return items.get(id);
	}
}

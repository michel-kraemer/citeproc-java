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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.undercouch.citeproc.csl.CSLItemData;

/**
 * Provides item data from a given list
 * @author Michel Kraemer
 */
public class ListItemDataProvider implements ItemDataProvider {
	/**
	 * The items that this provider holds
	 */
	protected Map<String, CSLItemData> items = new LinkedHashMap<>();
	
	/**
	 * Creates a data provider that serves items from the given array
	 * @param items the items to serve
	 */
	public ListItemDataProvider(CSLItemData... items) {
		this(Arrays.asList(items));
	}
	
	/**
	 * Creates a data provider that serves items from the given list
	 * @param items the items to serve
	 */
	public ListItemDataProvider(List<CSLItemData> items) {
		for (CSLItemData i : items) {
			this.items.put(i.getId(), i);
		}
	}

	@Override
	public CSLItemData retrieveItem(String id) {
		return items.get(id);
	}

	@Override
	public String[] getIds() {
		String[] ids = new String[items.size()];
		int i = 0;
		for (Map.Entry<String, CSLItemData> e : items.entrySet()) {
			ids[i++] = e.getKey();
		}
		return ids;
	}
}

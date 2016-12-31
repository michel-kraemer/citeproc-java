// Copyright 2013-2016 Michel Kraemer
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
import java.util.List;

import de.undercouch.citeproc.csl.CSLItemData;

/**
 * <p>Retrieves citation items from a list of other
 * {@link ItemDataProvider}s.</p>
 * <p>Calls each {@link ItemDataProvider} in the order they have been added to
 * the list and returns the first citation item retrieved. Returns
 * <code>null</code> if no provider returned a result.</p>
 * <p>Does not check for duplicate items or item IDs.</p>
 * @author Michel Kraemer
 * @since 1.1.0
 */
public class CompoundItemDataProvider implements ItemDataProvider {
	/**
	 * The list of other providers to query
	 */
	private final List<ItemDataProvider> providers;
	
	/**
	 * Creates a new compound provider
	 * @param providers the list of other providers to query for citation items
	 */
	public CompoundItemDataProvider(List<ItemDataProvider> providers) {
		this.providers = providers;
	}

	@Override
	public CSLItemData retrieveItem(String id) {
		return providers.stream()
			.map(p -> p.retrieveItem(id))
			.filter(item -> item != null)
			.findFirst()
			.orElse(null);
	}

	@Override
	public String[] getIds() {
		return providers.stream()
			.map(ItemDataProvider::getIds)
			.flatMap(Arrays::stream)
			.toArray(String[]::new);
	}
}

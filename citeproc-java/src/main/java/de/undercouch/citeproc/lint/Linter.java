// Copyright 2013-2017 Michel Kraemer
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

package de.undercouch.citeproc.lint;

import de.undercouch.citeproc.ItemDataProvider;
import de.undercouch.citeproc.csl.CSLItemData;

/**
 * A linter validates citation items from an {@link ItemDataProvider} and
 * notifies a {@link LintListener} about the results
 * @author Michel Kraemer
 * @since 1.1.0
 */
public interface Linter {
	/**
	 * Validate citation items provided by the given {@link ItemDataProvider}
	 * @param provider the item data provider
	 */
	void lint(ItemDataProvider provider);
	
	/**
	 * Validate a single citation item
	 * @param item the item to validate
	 * @param provider the item data provider that provided the citation item
	 */
	void lintItem(CSLItemData item, ItemDataProvider provider);
	
	/**
	 * Add a new listener that will receive events with validation results
	 * @param listener the listener to add
	 */
	void addListener(LintListener listener);
	
	/**
	 * Remove a listener
	 * @param listener the listener to remove
	 */
	void removeListener(LintListener listener);
	
	/**
	 * Add a filter that will decide which citation items should be validated
	 * and which should not
	 * @param filter the filter to add
	 */
	void addFilter(LintFilter filter);
	
	/**
	 * Remove a filter
	 * @param filter the filter to remove
	 */
	void removeFilter(LintFilter filter);
}

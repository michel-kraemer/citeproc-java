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

import java.util.LinkedHashSet;
import java.util.Set;

import de.undercouch.citeproc.ItemDataProvider;
import de.undercouch.citeproc.csl.CSLItemData;

/**
 * Abstract base class for all implementations of {@link Linter}
 * @author Michel Kraemer
 * @since 1.1.0
 */
public abstract class AbstractLinter implements Linter {
	/**
	 * A set of listeners to call on validation events
	 */
	private Set<LintListener> listeners = new LinkedHashSet<>();
	
	/**
	 * A set of filters to apply to citation items
	 */
	private Set<LintFilter> filters = new LinkedHashSet<>();

	@Override
	public void lint(ItemDataProvider provider) {
		LintEvent event = new DefaultLintEvent(provider, this);
		fireStart(event);
		
		for (String id : provider.getIds()) {
			CSLItemData item = provider.retrieveItem(id);
			if (filters.stream().allMatch(f -> f.test(item))) {
				lintItem(item, provider);
			}
		}
		
		fireEnd(event);
	}
	
	@Override
	public void lintItem(CSLItemData item, ItemDataProvider provider) {
		LintItemEvent itemEvent = new DefaultLintItemEvent(item.getId(),
				provider, this);
		fireStartItem(itemEvent);
		doLintItem(item, provider);
		fireEndItem(itemEvent);
	}
	
	/**
	 * Validate a single citation item. Do not send {@link LintItemEvent}s
	 * when starting and finishing but send {@link LintErrorEvent}s if the
	 * item is invalid.
	 * @param item the item to validate
	 * @param provider the item data provider that provided the citation item
	 */
	protected abstract void doLintItem(CSLItemData item, ItemDataProvider provider);

	@Override
	public void addListener(LintListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(LintListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void addFilter(LintFilter filter) {
		filters.add(filter);
	}

	@Override
	public void removeFilter(LintFilter filter) {
		filters.remove(filter);
	}
	
	/**
	 * Notify all listeners that we started validating an item data provider
	 * @param e the event to pass to the listeners
	 */
	protected void fireStart(LintEvent e) {
		for (LintListener l : listeners) {
			l.onStart(e);
		}
	}
	
	/**
	 * Notify all listeners that we finished validating an item data provider
	 * @param e the event to pass to the listeners
	 */
	protected void fireEnd(LintEvent e) {
		for (LintListener l : listeners) {
			l.onEnd(e);
		}
	}
	
	/**
	 * Notify all listeners that we started validating a specific citation item
	 * @param e the event to pass to the listeners
	 */
	protected void fireStartItem(LintItemEvent e) {
		for (LintListener l : listeners) {
			l.onStartItem(e);
		}
	}
	
	/**
	 * Notify all listeners that we finished validating a specific citation item
	 * @param e the event to pass to the listeners
	 */
	protected void fireEndItem(LintItemEvent e) {
		for (LintListener l : listeners) {
			l.onEndItem(e);
		}
	}
	
	/**
	 * Notify all listeners about a validation error
	 * @param e the event to pass to the listeners
	 */
	protected void fireError(LintErrorEvent e) {
		for (LintListener l : listeners) {
			l.onError(e);
		}
	}
}


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
 * A linter calling several other linters to validate citation items
 * @author Michel Kraemer
 * @since 1.1.0
 */
public class DefaultLinter extends AbstractLinter {
	/**
	 * The linters that will be called (in the given order)
	 */
	private final Linter[] LINTERS = {
			new DuplicateCitationIdLinter()
	};
	
	@Override
	public void addListener(LintListener listener) {
		super.addListener(listener);
		for (Linter l : LINTERS) {
			l.addListener(listener);
		}
	}

	@Override
	public void removeListener(LintListener listener) {
		super.removeListener(listener);
		for (Linter l : LINTERS) {
			l.removeListener(listener);
		}
	}
	
	@Override
	public void lint(ItemDataProvider provider) {
		LintEvent event = new DefaultLintEvent(provider, this);
		fireStart(event);
		
		for (Linter l : LINTERS) {
			l.lint(provider);
		}
		
		fireEnd(event);
	}
	
	@Override
	protected void doLintItem(CSLItemData item, ItemDataProvider provider) {
		for (Linter l : LINTERS) {
			l.lintItem(item, provider);
		}
	}
}

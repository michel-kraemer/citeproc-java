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

import java.util.HashSet;
import java.util.Set;

import de.undercouch.citeproc.ItemDataProvider;
import de.undercouch.citeproc.csl.CSLItemData;

/**
 * A linter that reports duplicate citation IDs
 * @author Michel Kraemer
 * @since 1.1.0
 */
public class DuplicateCitationIdLinter extends AbstractLinter {
	private Set<String> ids = new HashSet<>();
	
	@Override
	protected void doLintItem(CSLItemData item, ItemDataProvider provider) {
		String id = item.getId();
		if (ids.contains(id)) {
			LintErrorEvent e = new DefaultLintErrorEvent(
					LintErrorEvent.Type.DUPLICATE_ID,
					"Duplicate citation ID `" + id + "'", id, provider, this);
			fireError(e);
		} else {
			ids.add(id);
		}
	}
}


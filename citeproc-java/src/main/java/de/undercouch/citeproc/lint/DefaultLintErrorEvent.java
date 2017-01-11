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

/**
 * Default implementation of {@link LintErrorEvent}
 * @author Michel Kraemer
 * @since 1.1.0
 */
public class DefaultLintErrorEvent extends DefaultLintItemEvent
		implements LintErrorEvent {
	private final Type type;
	private final String message;
	
	/**
	 * Construct a new error event
	 * @param type the error type
	 * @param message the error message
	 * @param citationId the ID of the citation considered erroneous
	 * @param itemDataProvider the item data provider that provided the
	 * erroneous citation item
	 * @param source the {@link Linter} that produced this event
	 */
	public DefaultLintErrorEvent(Type type, String message, String citationId,
			ItemDataProvider itemDataProvider, Linter source) {
		super(citationId, itemDataProvider, source);
		this.type = type;
		this.message = message;
	}
	
	@Override
	public Type getType() {
		return type;
	}

	@Override
	public String getMessage() {
		return message;
	}
}

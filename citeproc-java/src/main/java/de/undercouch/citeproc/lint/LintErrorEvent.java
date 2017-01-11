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

/**
 * An event produced by {@link Linter} when an erroneous
 * citation item was found
 * @author Michel Kraemer
 * @since 1.1.0
 */
public interface LintErrorEvent extends LintItemEvent {
	/**
	 * Error types
	 */
	enum Type {
		/**
		 * The linter found a duplicate citation ID
		 */
		DUPLICATE_ID
	}
	
	/**
	 * Get the error type
	 * @return the type
	 */
	Type getType();
	
	/**
	 * Get the error message
	 * @return the message
	 */
	String getMessage();
}


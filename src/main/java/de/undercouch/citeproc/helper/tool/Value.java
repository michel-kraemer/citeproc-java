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

package de.undercouch.citeproc.helper.tool;

/**
 * An option's value
 * @author Michel Kraemer
 * @param <T> option identifier type
 */
public class Value<T> {
	private final T id;
	private final Object value;
	
	/**
	 * Constructs a new option value
	 * @param id the option's identifier
	 * @param value the value
	 */
	public Value(T id, Object value) {
		this.id = id;
		this.value = value;
	}
	
	/**
	 * @return the option's identifier
	 */
	public T getId() {
		return id;
	}
	
	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}
}

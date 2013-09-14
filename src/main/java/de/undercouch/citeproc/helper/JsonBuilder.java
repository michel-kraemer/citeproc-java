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

package de.undercouch.citeproc.helper;

/**
 * Builds JSON objects
 * @author Michel Kraemer
 */
public interface JsonBuilder {
	/**
	 * Adds a property to the object to build
	 * @param name the property's name
	 * @param o the property's value
	 * @return the {@link JsonBuilder}
	 */
	JsonBuilder add(String name, Object o);

	/**
	 * Builds the JSON object
	 * @return the object
	 */
	Object build();

	/**
	 * Converts an array of Strings to a JSON array
	 * @param arr the array of Strings
	 * @return the JSON array
	 */
	Object toJson(String[] arr);
}

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

/**
 * Provides the serialized XML representation for a locale
 * @author Michel Kraemer
 */
public interface LocaleProvider {
	/**
	 * Retrieves the serialized XML representation for a given locale
	 * @param lang the locale identifier (e.g. "en" or "en-GB")
	 * @return the serializes XML of the given locale or null if there is
	 * no such locale
	 */
	String retrieveLocale(String lang);
}

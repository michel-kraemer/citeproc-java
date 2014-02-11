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

package $pkg;

import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.ListItemDataProvider;

/**
 * Loads citation items from a $desc library
 * @author Michel Kraemer
 */
public class $providername extends ListItemDataProvider {
	/**
	 * Adds the given library
	 * @param lib the library to add
	 */
	public void addLibrary($libname lib) {
		items.putAll(new $convname().toItemData(lib));
	}
	
	/**
	 * Introduces all citation items from the $desc libraries added
	 * via {@link #addLibrary($libname)} to the given CSL processor
	 * @see CSL#registerCitationItems(String[])
	 * @param citeproc the CSL processor
	 */
	public void registerCitationItems(CSL citeproc) {
		citeproc.registerCitationItems(getIds());
	}
}

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

package de.undercouch.citeproc.tool;

import de.undercouch.citeproc.ItemDataProvider;

/**
 * A command that uses an {@link ItemDataProvider} to get citation data
 * @author Michel Kraemer
 */
public interface ProviderCommand extends CSLToolCommand {
	/**
	 * @return the item data provider holding input citation data
	 */
	ItemDataProvider getProvider();
	
	/**
	 * Sets the item data provider holding input citation data
	 * @param provider the provider
	 */
	void setProvider(ItemDataProvider provider);
}

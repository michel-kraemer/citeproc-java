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

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.undercouch.citeproc.ItemDataProvider;
import de.undercouch.citeproc.helper.Levenshtein;
import de.undercouch.citeproc.helper.tool.UnknownAttributes;

/**
 * A base class for commands that accept citation IDs as arguments
 * @author Michel Kraemer
 */
public abstract class CitationIdsCommand extends AbstractCSLToolCommand implements ProviderCommand {
	/**
	 * The item data provider
	 */
	private ItemDataProvider provider;
	
	/**
	 * The citation IDs
	 */
	private List<String> citationIds = new ArrayList<String>();
	
	/**
	 * Sets the citation IDs
	 * @param ids the IDs
	 */
	@UnknownAttributes
	public void setCitationIds(List<String> ids) {
		citationIds = ids;
	}
	
	@Override
	public int doRun(String[] remainingArgs, PrintStream out) throws IOException {
		//check provided citation ids
		if (!checkCitationIds(citationIds, getProvider())) {
			return 1;
		}
		
		return 0;
	}
	
	/**
	 * @return the citation IDs
	 */
	protected List<String> getCitationIds() {
		return citationIds;
	}
	
	/**
	 * Checks the citation IDs provided on the command line
	 * @param citationIds the citation IDs
	 * @param provider the item data provider
	 * @return true if all citation IDs are OK, false if they're not
	 */
	protected boolean checkCitationIds(List<String> citationIds, ItemDataProvider provider) {
		for (String id : citationIds) {
			if (provider.retrieveItem(id) == null) {
				String min = Levenshtein.findMinimum(Arrays.asList(provider.getIds()), id);
				error("unknown citation id: " + id + "\nDid you mean `" + min + "'?");
				return false;
			}
		}
		return true;
	}
	
	@Override
	public ItemDataProvider getProvider() {
		return provider;
	}
	
	@Override
	public void setProvider(ItemDataProvider provider) {
		this.provider = provider;
	}
}

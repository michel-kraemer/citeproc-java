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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.undercouch.citeproc.ItemDataProvider;
import de.undercouch.underline.InputReader;

/**
 * CLI command that lists items from an input bibliography
 * @author Michel Kraemer
 */
public class ListCommand extends AbstractCSLToolCommand implements ProviderCommand {
	/**
	 * The item data provider
	 */
	private ItemDataProvider provider;
	
	@Override
	public String getUsageName() {
		return "list";
	}
	
	@Override
	public String getUsageDescription() {
		return "Display sorted list of available citation IDs";
	}
	
	@Override
	public int doRun(String[] remainingArgs, InputReader in, PrintWriter out)
			throws IOException {
		//list available citation ids and exit
		List<String> ids = new ArrayList<String>(Arrays.asList(getProvider().getIds()));
		Collections.sort(ids);
		for (String id : ids) {
			out.println(id);
		}
		
		return 0;
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

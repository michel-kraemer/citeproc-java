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

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.undercouch.citeproc.ItemDataProvider;
import de.undercouch.citeproc.ListItemDataProvider;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.helper.oauth.AuthenticationStore;
import de.undercouch.citeproc.helper.oauth.FileAuthenticationStore;
import de.undercouch.citeproc.helper.oauth.RequestException;
import de.undercouch.citeproc.helper.oauth.UnauthorizedException;
import de.undercouch.citeproc.helper.tool.OptionParserException;
import de.undercouch.citeproc.helper.tool.internal.CachingRemoteConnector;
import de.undercouch.citeproc.remote.AuthenticatedRemoteConnector;
import de.undercouch.citeproc.remote.RemoteConnector;

/**
 * Generates bibliographies and citations from remote sources
 * @author Michel Kraemer
 */
public abstract class AbstractRemoteCommand extends AbstractCSLToolCommand {
	private boolean sync = false;
	private ProviderCommand subcommand;
	
	/**
	 * Sets the sync flag
	 * @param sync true if the command should synchronize with the
	 * remote service before doing anything
	 */
	public void setSync(boolean sync) {
		this.sync = sync;
	}
	
	/**
	 * Sets the subcommand to execute
	 * @param subcommand the subcommand
	 */
	public void setSubcommand(ProviderCommand subcommand) {
		this.subcommand = subcommand;
	}
	
	@Override
	public boolean checkArguments() {
		if (subcommand == null) {
			error("no subcommand specified");
			return false;
		}
		return super.checkArguments();
	}
	
	@Override
	public int doRun(String[] remainingArgs, PrintStream out)
			throws OptionParserException, IOException {
		ItemDataProvider provider = connect(sync);
		if (provider == null) {
			return 1;
		}
		subcommand.setProvider(provider);
		return subcommand.run(remainingArgs, out);
	}
	
	/**
	 * Reads items from the remote server
	 * @param sync true if synchronization should be forced
	 * @return an item data provider providing all items from the
	 * remote server
	 */
	private ItemDataProvider connect(boolean sync) {
		//read app's consumer key and secret
		String[] consumer;
		try {
			consumer = readConsumer();
		} catch (Exception e) {
			//should never happen
			throw new RuntimeException("Could not read OAuth consumer key and secret");
		}
		
		//use previously stored authentication
		File authStoreFile = new File(CSLToolContext.current().getConfigDir(),
				getAuthStoreFileName());
		AuthenticationStore authStore;
		try {
			authStore = new FileAuthenticationStore(authStoreFile);
		} catch (IOException e) {
			error("could not read user's authentication store: " + authStoreFile.getPath());
			return null;
		}
		
		//connect to remote server
		RemoteConnector dmc = createRemoteConnector(consumer[0], consumer[1]);
		dmc = new AuthenticatedRemoteConnector(dmc, authStore);
		
		//enable cache
		File cacheFile = new File(CSLToolContext.current().getConfigDir(),
				getCacheFileName());
		CachingRemoteConnector mc = new CachingRemoteConnector(dmc, cacheFile);
		
		//clear cache if necessary
		if (sync) {
			mc.clear();
		}

		List<CSLItemData> itemDataList;
		int retries = 1;
		while (true) {
			try {
				//download list of item IDs
				boolean cacheempty = false;
				if (!mc.hasItemList()) {
					System.out.print("Retrieving items ...");
					cacheempty = true;
				}
				List<String> items = mc.getItemIDs();
				if (cacheempty) {
					System.out.println();
				}
				
				//download all items
				itemDataList = new ArrayList<CSLItemData>(items.size());
				int s = 0;
				int printed = 0;
				int bulk = mc.getMaxBulkItems();
				while (s < items.size()) {
					int n = 0;
					List<String> itemsToRetrieve = new ArrayList<String>(bulk);
					while (s < items.size() && n < bulk) {
						String did = items.get(s);
						if (!mc.containsItemId(did)) {
							String msg = String.format("\rSynchronizing (%d/%d) ...",
									s + 1, items.size());
							System.out.print(msg);
							++printed;
							++n;
						}
						itemsToRetrieve.add(did);
						++s;
					}
					Map<String, CSLItemData> itemData = mc.getItems(itemsToRetrieve);
					itemDataList.addAll(itemData.values());
				}
				
				if (printed > 0) {
					System.out.println();
				}
			} catch (UnauthorizedException e) {
				if (retries == 0) {
					error("failed to authorize.");
					return null;
				}
				--retries;
				
				//app is not authenticated yet
				System.out.print("\r"); //overwrite 'Retrieving items' message
				if (!authorize(mc)) {
					return null;
				}
				
				continue;
			} catch (RequestException e) {
				error(e.getMessage());
				return null;
			} catch (IOException e) {
				error("could not get list of items from remote server.");
				return null;
			}
			
			break;
		}
		
		//return provider that contains all items from the server
		CSLItemData[] itemDataArr = itemDataList.toArray(new CSLItemData[itemDataList.size()]);
		return new ListItemDataProvider(itemDataArr);
	}
	
	/**
	 * Creates the remote connector
	 * @param consumerKey the OAuth consumer key
	 * @param consumerSecret the OAuth consumer secret
	 * @return the remote connector
	 */
	protected abstract RemoteConnector createRemoteConnector(
			String consumerKey, String consumerSecret);
	
	/**
	 * @return the filename of the authentication store
	 */
	protected abstract String getAuthStoreFileName();
	
	/**
	 * @return the filename of the item cache
	 */
	protected abstract String getCacheFileName();
	
	/**
	 * Request authorization for the tool from remote server
	 * @param mc the remote connector
	 * @return true if authorization was successful
	 */
	private boolean authorize(RemoteConnector mc) {
		//get authorization URL
		String authUrl;
		try {
			authUrl = mc.getAuthorizationURL();
		} catch (IOException e) {
			error("could not get authorization URL from remote server.");
			return false;
		}
		
		//ask user to point browser to authorization URL
		System.out.println("This tool requires authorization. Please point your "
				+ "web browser to the\nfollowing URL and follow the instructions:\n");
		System.out.println(authUrl);
		System.out.println();
		
		//open authorization tool in browser
		if (Desktop.isDesktopSupported()) {
			Desktop d = Desktop.getDesktop();
			if (d.isSupported(Desktop.Action.BROWSE)) {
				try {
					d.browse(new URI(authUrl));
				} catch (Exception e) {
					//ignore. let the user open the browser manually.
				}
			}
		}
		
		//read verification code from console
		System.out.print("Enter verification code: ");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String verificationCode;
		try {
			verificationCode = br.readLine();
		} catch (IOException e) {
			throw new RuntimeException("Could not read from console.");
		}
		
		if (verificationCode == null || verificationCode.isEmpty()) {
			//user aborted process
			return false;
		}
		
		//authorize...
		try {
			System.out.println("Connecting ...");
			mc.authorize(verificationCode);
		} catch (IOException e) {
			error("remote server refused authorization.");
			return false;
		}
		
		return true;
	}
	
	/**
	 * Reads the OAuth consumer key and consumer secret
	 * @return the key and secret
	 * @throws Exception if something goes wrong
	 */
	protected abstract String[] readConsumer() throws Exception;
}

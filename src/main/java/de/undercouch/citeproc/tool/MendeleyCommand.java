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
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import de.undercouch.citeproc.CSLTool;
import de.undercouch.citeproc.ItemDataProvider;
import de.undercouch.citeproc.ListItemDataProvider;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.helper.CSLUtils;
import de.undercouch.citeproc.helper.oauth.AuthenticationStore;
import de.undercouch.citeproc.helper.oauth.FileAuthenticationStore;
import de.undercouch.citeproc.helper.oauth.RequestException;
import de.undercouch.citeproc.helper.oauth.UnauthorizedException;
import de.undercouch.citeproc.helper.tool.CommandDesc;
import de.undercouch.citeproc.helper.tool.CommandDescList;
import de.undercouch.citeproc.helper.tool.OptionDesc;
import de.undercouch.citeproc.helper.tool.OptionParserException;
import de.undercouch.citeproc.helper.tool.internal.CachingMendeleyConnector;
import de.undercouch.citeproc.mendeley.AuthenticatedMendeleyConnector;
import de.undercouch.citeproc.mendeley.DefaultMendeleyConnector;
import de.undercouch.citeproc.mendeley.MendeleyConnector;

/**
 * Generates bibliographies and citations from Mendeley Web
 * @author Michel Kraemer
 */
public class MendeleyCommand extends AbstractCSLToolCommand {
	private boolean sync = false;
	private ProviderCommand subcommand;
	
	/**
	 * Sets the sync flag
	 * @param sync true if the command should synchronize with Mendeley
	 * Web before doing anything
	 */
	@OptionDesc(longName = "sync", shortName = "s",
			description = "force synchronization with Mendeley Web")
	public void setSync(boolean sync) {
		this.sync = sync;
	}
	
	/**
	 * Sets the subcommand to execute
	 * @param subcommand the subcommand
	 */
	@CommandDescList({
		@CommandDesc(longName = "bibliography",
				description = "generate bibliography from Mendeley Web",
				command = MendeleyBibliographyCommand.class),
		@CommandDesc(longName = "citation",
				description = "generate citations from Mendeley Web",
				command = MendeleyCitationCommand.class),
		@CommandDesc(longName = "list",
				description = "display sorted list of available citation IDs "
						+ "in the Mendeley Web catalog",
				command = MendeleyListCommand.class),
		@CommandDesc(longName = "json",
				description = "convert Mendeley Web catalog to JSON",
				command = MendeleyJsonCommand.class)
	})
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
	public String getUsageDescription() {
		return "Connect to Mendeley Web and generate styled citations and bibliographies";
	}
	
	@Override
	public String getUsageArguments() {
		return "mendeley [OPTION]... [COMMAND] [COMMAND OPTION]...";
	}

	@Override
	public int doRun(String[] remainingArgs, PrintStream out)
			throws OptionParserException, IOException {
		ItemDataProvider provider = readMendeley(sync);
		subcommand.setProvider(provider);
		return subcommand.run(remainingArgs, out);
	}
	
	/**
	 * Reads documents from the Mendeley server
	 * @param sync true if synchronization should be forced
	 * @return an item data provider providing all documents from the
	 * Mendeley server
	 */
	private ItemDataProvider readMendeley(boolean sync) {
		//read app's consumer key and secret
		String[] consumer;
		try {
			consumer = readConsumer();
		} catch (Exception e) {
			//should never happen
			throw new RuntimeException("Could not read Mendeley consumer key and secret");
		}
		
		//use previously stored authentication
		File authStoreFile = new File(CSLToolContext.current().getConfigDir(),
				"mendeley-auth-store.conf");
		AuthenticationStore authStore;
		try {
			authStore = new FileAuthenticationStore(authStoreFile);
		} catch (IOException e) {
			System.err.println("citeproc-java: could not read user's "
					+ "authentication store: " + authStoreFile.getPath());
			return null;
		}
		
		//connect to Mendeley server
		MendeleyConnector dmc = new DefaultMendeleyConnector(consumer[1], consumer[0]);
		dmc = new AuthenticatedMendeleyConnector(dmc, authStore);
		
		//enable cache
		File cacheFile = new File(CSLToolContext.current().getConfigDir(),
				"mendeley-cache.dat");
		CachingMendeleyConnector mc = new CachingMendeleyConnector(dmc, cacheFile);
		
		//clear cache if necessary
		if (sync) {
			mc.clear();
		}

		CSLItemData[] items;
		int retries = 1;
		while (true) {
			try {
				//download list of document IDs
				boolean cacheempty = false;
				if (!mc.hasDocumentList()) {
					System.out.print("Retrieving documents ...");
					cacheempty = true;
				}
				List<String> docs = mc.getDocuments();
				if (cacheempty) {
					System.out.println();
				}
				
				//download all documents
				items = new CSLItemData[docs.size()];
				int i = 0;
				int printed = 0;
				for (String did : docs) {
					if (!mc.containsDocumentId(did)) {
						String msg = String.format("\rSynchronizing (%d/%d) ...",
								i + 1, docs.size());
						System.out.print(msg);
						++printed;
					}
					
					CSLItemData item = mc.getDocument(did);
					items[i] = item;
					++i;
				}
				
				if (printed > 0) {
					System.out.println();
				}
			} catch (UnauthorizedException e) {
				if (retries == 0) {
					System.err.println("citeproc-java: failed to authorize.");
					return null;
				}
				--retries;
				
				//app is not authenticated yet
				if (!mendeleyAuthorize(mc)) {
					return null;
				}
				
				continue;
			} catch (RequestException e) {
				System.err.println("citeproc-java: " + e.getMessage());
				return null;
			} catch (IOException e) {
				System.err.println("citeproc-java: could not get list of "
						+ "documents from Mendeley server.");
				return null;
			}
			
			break;
		}
		
		//return provider that contains all items from the server
		return new ListItemDataProvider(items);
	}
	
	/**
	 * Request authorization for the tool from the Mendeley server
	 * @param mc the Mendeley connector
	 * @return true if authorization was successful
	 */
	private boolean mendeleyAuthorize(MendeleyConnector mc) {
		//get authorization URL
		String authUrl;
		try {
			authUrl = mc.getAuthorizationURL();
		} catch (IOException e) {
			System.err.println("citeproc-java: could not get authorization "
					+ "URL from Mendeley server.");
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
		
		//authorize...
		try {
			System.out.println("Connecting ...");
			mc.authorize(verificationCode);
		} catch (IOException e) {
			System.err.println("citeproc-java: Mendeley server refused "
					+ "authorization.");
			return false;
		}
		
		return true;
	}
	
	/**
	 * Reads the Mendeley consumer key and consumer secret from an encrypted
	 * file. This is indeed not the most secure way to save these tokens, but
	 * it's certainly better than putting them unencrypted in this file.
	 * @return the key and secret
	 * @throws Exception if something goes wrong
	 */
	private String[] readConsumer() throws Exception {
		String str = CSLUtils.readStreamToString(CSLTool.class.getResourceAsStream(
				"helper/tool/internal/citeproc-java-tool-consumer"), "UTF-8");
		byte[] arr = DatatypeConverter.parseBase64Binary(str);
		
		SecretKeySpec k = new SecretKeySpec("#x$gbf5zs%4QvzAx".getBytes(), "AES");
		Cipher c = Cipher.getInstance("AES");
		c.init(Cipher.DECRYPT_MODE, k);
		arr = c.doFinal(arr);
		arr = DatatypeConverter.parseBase64Binary(new String(arr));
		
		String[] result = new String[] { "", "" };
		for (int i = 0; i < 32; ++i) {
			result[0] += (char)arr[i + 31];
		}
		for (int i = 0; i < 41; ++i) {
			result[1] += (char)arr[i + 1857];
		}
		
		return result;
	}
}

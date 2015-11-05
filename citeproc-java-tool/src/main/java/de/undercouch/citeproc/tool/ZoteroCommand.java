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

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import de.undercouch.citeproc.CSLTool;
import de.undercouch.citeproc.ItemDataProvider;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.helper.CSLUtils;
import de.undercouch.citeproc.zotero.ZoteroConnector;
import de.undercouch.citeproc.zotero.ZoteroItemDataProvider;
import de.undercouch.underline.CommandDesc;
import de.undercouch.underline.CommandDescList;
import de.undercouch.underline.OptionDesc;

/**
 * Generates bibliographies and citations from Zotero
 * @author Michel Kraemer
 */
public class ZoteroCommand extends AbstractRemoteCommand {
	@OptionDesc(longName = "sync", shortName = "s",
			description = "force synchronization with Zotero")
	@Override
	public void setSync(boolean sync) {
		super.setSync(sync);
	}
	
	@CommandDescList({
		@CommandDesc(longName = "bibliography",
				description = "generate bibliography from Zotero",
				command = ZoteroBibliographyCommand.class),
		@CommandDesc(longName = "citation",
				description = "generate citations from Zotero",
				command = ZoteroCitationCommand.class),
		@CommandDesc(longName = "list",
				description = "display sorted list of available citation IDs "
						+ "in the Zotero library",
				command = ZoteroListCommand.class),
		@CommandDesc(longName = "json",
				description = "convert Zotero library to JSON",
				command = ZoteroJsonCommand.class),
		@CommandDesc(longName = "sync",
				description = "synchronize with Zotero",
				command = ZoteroSyncCommand.class)
	})
	@Override
	public void setSubcommand(ProviderCommand subcommand) {
		super.setSubcommand(subcommand);
	}
	
	@Override
	public String getUsageName() {
		return "zotero";
	}
	
	@Override
	public String getUsageDescription() {
		return "Connect to Zotero and generate styled citations and bibliographies";
	}
	
	@Override
	protected ItemDataProvider createItemDataProvider(CSLItemData[] itemData) {
		return new ZoteroItemDataProvider(super.createItemDataProvider(itemData));
	}
	
	@Override
	protected ZoteroConnector createRemoteConnector(String consumerKey,
			String consumerSecret) {
		return new ZoteroConnector(consumerKey, consumerSecret);
	}
	
	@Override
	protected String getAuthStoreFileName() {
		return "zotero-auth-store.conf";
	}
	
	@Override
	protected String getCacheFileName() {
		return "zotero-cache.dat";
	}
	
	/**
	 * Reads the Zotero consumer key and consumer secret from an encrypted
	 * file. This is indeed not the most secure way to save these tokens, but
	 * it's certainly better than putting them unencrypted in this file.
	 * @return the key and secret
	 * @throws Exception if something goes wrong
	 */
	protected String[] readConsumer() throws Exception {
		String str = CSLUtils.readStreamToString(CSLTool.class.getResourceAsStream(
				"helper/tool/internal/citeproc-java-tool-consumer2"), "UTF-8");
		byte[] arr = DatatypeConverter.parseBase64Binary(str);
		
		SecretKeySpec k = new SecretKeySpec("5zt&%3,oc\"??823_".getBytes(), "AES");
		Cipher c = Cipher.getInstance("AES");
		c.init(Cipher.DECRYPT_MODE, k);
		arr = c.doFinal(arr);
		arr = DatatypeConverter.parseBase64Binary(new String(arr));
		
		String[] result = new String[] { "", "" };
		for (int i = 0; i < 20; ++i) {
			result[0] += (char)arr[i + 789];
		}
		for (int i = 0; i < 20; ++i) {
			result[1] += (char)arr[i + 1478];
		}
		
		return result;
	}
}

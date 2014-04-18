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
import de.undercouch.citeproc.helper.CSLUtils;
import de.undercouch.citeproc.helper.tool.CommandDesc;
import de.undercouch.citeproc.helper.tool.CommandDescList;
import de.undercouch.citeproc.helper.tool.OptionDesc;
import de.undercouch.citeproc.mendeley.MendeleyConnector;

/**
 * Generates bibliographies and citations from Mendeley Web
 * @author Michel Kraemer
 */
public class MendeleyCommand extends AbstractRemoteCommand {
	/**
	 * The location users are redirected to after they granted
	 * citeproc-java access to their Mendeley library
	 */
	private static final String REDIRECT_URI =
			"http://www.undercouch.de/citeproc-java/authorize/";
	
	@OptionDesc(longName = "sync", shortName = "s",
			description = "force synchronization with Mendeley Web")
	@Override
	public void setSync(boolean sync) {
		super.setSync(sync);
	}
	
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
	@Override
	public void setSubcommand(ProviderCommand subcommand) {
		super.setSubcommand(subcommand);
	}
	
	@Override
	public String getUsageName() {
		return "mendeley";
	}
	
	@Override
	public String getUsageDescription() {
		return "Connect to Mendeley Web and generate styled citations and bibliographies";
	}
	
	@Override
	protected MendeleyConnector createRemoteConnector(String consumerKey,
			String consumerSecret) {
		return new MendeleyConnector(consumerKey, consumerSecret, REDIRECT_URI);
	}
	
	@Override
	protected String getAuthStoreFileName() {
		return "mendeley-auth-store.conf";
	}
	
	@Override
	protected String getCacheFileName() {
		return "mendeley-cache.dat";
	}
	
	/**
	 * Reads the Mendeley consumer key and consumer secret from an encrypted
	 * file. This is indeed not the most secure way to save these tokens, but
	 * it's certainly better than putting them unencrypted in this file.
	 * @return the key and secret
	 * @throws Exception if something goes wrong
	 */
	protected String[] readConsumer() throws Exception {
		String str = CSLUtils.readStreamToString(CSLTool.class.getResourceAsStream(
				"helper/tool/internal/citeproc-java-tool-consumer"), "UTF-8");
		byte[] arr = DatatypeConverter.parseBase64Binary(str);
		
		SecretKeySpec k = new SecretKeySpec("#x$gbf5zs%4QvzAx".getBytes(), "AES");
		Cipher c = Cipher.getInstance("AES");
		c.init(Cipher.DECRYPT_MODE, k);
		arr = c.doFinal(arr);
		arr = DatatypeConverter.parseBase64Binary(new String(arr));
		
		String[] result = new String[] { "", "" };
		for (int i = 0; i < 2; ++i) {
			result[0] += (char)arr[i + 31];
		}
		for (int i = 0; i < 16; ++i) {
			result[1] += (char)arr[i + 1857];
		}
		
		return result;
	}
}

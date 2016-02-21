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

package de.undercouch.citeproc.zotero;

import java.util.Map;

import de.undercouch.citeproc.helper.oauth.OAuth1;
import de.undercouch.citeproc.helper.oauth.Token;

/**
 * Extends {@link OAuth1} and creates special token credentials for Zotero
 * @author Michel Kraemer
 */
public class ZoteroOAuth extends OAuth1 {
	private static final String OAUTH_USERID = "userID";
	
	/**
	 * Creates a new OAuth client for Zotero
	 * @param consumerKey the consumer key
	 * @param consumerSecret the consumer secret
	 * @see OAuth1#OAuth1(String, String)
	 */
	public ZoteroOAuth(String consumerKey, String consumerSecret) {
		super(consumerKey, consumerSecret);
	}
	
	@Override
	protected Token responseToToken(Map<String, String> response) {
		String userId = response.get(OAUTH_USERID);
		if (userId != null) {
			//since Zotero uses a single API key we can store the user ID in the token
			return new Token(response.get(OAUTH_USERID), response.get(OAUTH_TOKEN_SECRET));
		}
		return super.responseToToken(response);
	}
}

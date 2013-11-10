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

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import de.undercouch.citeproc.helper.oauth.OAuth;
import de.undercouch.citeproc.helper.oauth.Token;

/**
 * Extends {@link OAuth} to create {@link ZoteroToken}s
 * @author Michel Kraemer
 */
public class ZoteroOAuth extends OAuth {
	private static final String OAUTH_USERID = "userID";
	
	/**
	 * @see OAuth#OAuth(String, String)
	 */
	public ZoteroOAuth(String consumerKey, String consumerSecret) {
		super(consumerKey, consumerSecret);
	}
	
	@Override
	protected ZoteroToken responseToToken(Map<String, String> response) {
		return new ZoteroToken(response.get(OAUTH_TOKEN),
				response.get(OAUTH_TOKEN_SECRET), response.get(OAUTH_USERID));
	}
	
	@Override
	public ZoteroToken requestTokenCredentials(URL url, Method method,
			Token temporaryCredentials, String verifier) throws IOException {
		return (ZoteroToken)super.requestTokenCredentials(url, method,
				temporaryCredentials, verifier);
	}
}

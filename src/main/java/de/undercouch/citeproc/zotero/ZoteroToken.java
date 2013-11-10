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

import de.undercouch.citeproc.helper.oauth.Token;

/**
 * An OAuth token containing additional information from the Zotero API v2
 * @author Michel Kraemer
 */
public class ZoteroToken extends Token {
	private final String userId;
	
	/**
	 * Constructs a new token
	 * @param token the token value
	 * @param secret the token secret
	 * @param userId the Zotero user's user ID (may be null)
	 */
	public ZoteroToken(String token, String secret, String userId) {
		super(token, secret);
		this.userId = userId;
	}
	
	/**
	 * @return the Zotero user's user ID (may be null)
	 */
	public String getUserId() {
		return userId;
	}
}

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

package de.undercouch.citeproc.mendeley;

import java.io.IOException;

import de.undercouch.citeproc.helper.oauth.AuthenticationStore;

/**
 * A Mendeley connector that saves its access token in an authentication
 * store, so you don't have to authenticate again until the access token
 * becomes invalid.
 * @author Michel Kraemer
 */
public class AuthenticatedMendeleyConnector extends MendeleyConnectorAdapter {
	private final AuthenticationStore store;
	
	/**
	 * Creates a Mendeley connector that delegates to an underlying connector
	 * and saves its access token in an authentication store
	 * @param delegate the underlying Mendeley connector
	 * @param store the authentication store
	 */
	public AuthenticatedMendeleyConnector(MendeleyConnector delegate,
			AuthenticationStore store) {
		super(delegate);
		this.store = store;
		
		String token = store.getToken();
		String secret = store.getSecret();
		if (token != null && secret != null) {
			delegate.setAccessToken(token, secret);
		}
	}

	@Override
	public void authorize(String verificationCode) throws IOException {
		super.authorize(verificationCode);
		try {
			store.save(getAccessTokenValue(), getAccessTokenSecret());
		} catch (IOException e) {
			throw new IllegalStateException("Could not store access token", e);
		}
	}

	@Override
	public void setAccessToken(String token, String secret) {
		super.setAccessToken(token, secret);
		try {
			store.save(token, secret);
		} catch (IOException e) {
			throw new IllegalStateException("Could not store access token", e);
		}
	}
}

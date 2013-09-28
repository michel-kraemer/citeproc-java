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
import java.util.List;

/**
 * Can be used by {@link MendeleyConnector} implementations to delegate to
 * an underlying connector
 * @author Michel Kraemer
 */
public class MendeleyConnectorAdapter implements MendeleyConnector {
	/**
	 * The underlying connector
	 */
	private final MendeleyConnector delegate;

	/**
	 * Creates a new adapter for the given underlying connector
	 * @param delegate the underlying connector
	 */
	public MendeleyConnectorAdapter(MendeleyConnector delegate) {
		this.delegate = delegate;
	}

	@Override
	public String getAuthorizationURL() {
		return delegate.getAuthorizationURL();
	}

	@Override
	public void authorize(String verificationCode) {
		delegate.authorize(verificationCode);
	}

	@Override
	public void setAccessToken(String token, String secret) {
		delegate.setAccessToken(token, secret);
	}

	@Override
	public String getAccessTokenValue() {
		return delegate.getAccessTokenValue();
	}

	@Override
	public String getAccessTokenSecret() {
		return delegate.getAccessTokenSecret();
	}

	@Override
	public List<String> getDocuments() throws MendeleyRequestException, IOException {
		return delegate.getDocuments();
	}
}

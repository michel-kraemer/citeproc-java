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

package de.undercouch.citeproc.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import de.undercouch.citeproc.helper.json.JsonLexer;
import de.undercouch.citeproc.helper.json.JsonParser;
import de.undercouch.citeproc.helper.oauth.OAuth;
import de.undercouch.citeproc.helper.oauth.OAuth.Method;
import de.undercouch.citeproc.helper.oauth.Token;
import de.undercouch.citeproc.helper.oauth.UnauthorizedException;

/**
 * Abstract base class for remote connectors
 * @author Michel Kraemer
 */
public abstract class AbstractRemoteConnector implements RemoteConnector {
	/**
	 * OAuth client that is used to authenticate the user
	 */
	protected final OAuth auth;
	
	/**
	 * The request token used to authorize the app
	 */
	protected Token requestToken;
	
	/**
	 * The access token used to sign requests
	 */
	private Token accessToken;
	
	/**
	 * Constructs a new connector
	 * @param consumerKey the app's consumer key
	 * @param consumerSecret the app's consumer secret
	 */
	public AbstractRemoteConnector(String consumerKey, String consumerSecret) {
		auth = new OAuth(consumerKey, consumerSecret);
	}
	
	/**
	 * @return the remote service's end-point for temporary credentials
	 */
	protected abstract String getOAuthRequestTokenURL();
	
	/**
	 * @return the remote service's authorization end-point
	 */
	protected abstract String getOAuthAuthorizationURL();
	
	/**
	 * @return the remote service's end-point for access tokens
	 */
	protected abstract String getOAuthAccessTokenURL();
	
	@Override
	public String getAuthorizationURL() throws IOException {
		try {
			requestToken = auth.requestTemporaryCredentials(
					new URL(getOAuthRequestTokenURL()), Method.GET,
					OAuth.CALLBACK_OOB);
		} catch (MalformedURLException e) {
			//should never happen
			throw new RuntimeException(e);
		}
		return getOAuthAuthorizationURL() + requestToken.getToken();
	}

	@Override
	public void authorize(String verificationCode) throws IOException {
		try {
			accessToken = auth.requestTokenCredentials(
					new URL(getOAuthAccessTokenURL()), Method.GET,
					requestToken, verificationCode);
		} catch (MalformedURLException e) {
			//should never happen
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setAccessToken(String token, String secret) {
		accessToken = new Token(token, secret);
	}

	@Override
	public String getAccessTokenValue() {
		if (accessToken == null) {
			return null;
		}
		return accessToken.getToken();
	}

	@Override
	public String getAccessTokenSecret() {
		if (accessToken == null) {
			return null;
		}
		return accessToken.getSecret();
	}
	
	/**
	 * Performs a request
	 * @param url the URL to query
	 * @return the parsed response
	 * @throws IOException if the request was not successful
	 */
	protected Map<String, Object> performRequest(String url) throws IOException {
		if (accessToken == null) {
			throw new UnauthorizedException("Access token has not yet been requested");
		}
		URL u = new URL(url);
		InputStream is = auth.request(u, Method.GET, accessToken);
		try {
			Reader r = new BufferedReader(new InputStreamReader(is));
			return new JsonParser(new JsonLexer(r)).parseObject();
		} finally {
			consumeResponse(is);
			is.close();
		}
	}

	/**
	 * Consumes the rest of an input stream. All contents will be discarded.
	 * Consuming a whole input stream is needed for HTTP connections
	 * to clear all resources.
	 * @param is the input stream
	 * @throws IOException if the input stream could not be read
	 */
	private void consumeResponse(InputStream is) throws IOException {
		byte[] buf = new byte[1024 * 8];
		while (is.read(buf) >= 0);
	}
}

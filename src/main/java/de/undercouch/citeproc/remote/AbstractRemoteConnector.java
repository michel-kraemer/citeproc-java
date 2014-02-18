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
import de.undercouch.citeproc.helper.oauth.OAuth1;
import de.undercouch.citeproc.helper.oauth.Response;
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
	protected Token accessToken;
	
	/**
	 * Constructs a new connector
	 * @param consumerKey the app's consumer key
	 * @param consumerSecret the app's consumer secret
	 */
	public AbstractRemoteConnector(String consumerKey, String consumerSecret) {
		auth = createOAuth(consumerKey, consumerSecret);
	}
	
	/**
	 * @return the remote service's end-point for temporary credentials or
	 * null if not supported
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
	
	/**
	 * @return the HTTP method to use while requesting access tokens
	 */
	protected abstract Method getOAuthAccessTokenMethod();
	
	/**
	 * Creates an OAuth object
	 * @param consumerKey the app's consumer key
	 * @param consumerSecret the app's consumer secret
	 * @return the created object
	 */
	protected OAuth createOAuth(String consumerKey, String consumerSecret) {
		return new OAuth1(consumerKey, consumerSecret);
	}
	
	@Override
	public String getAuthorizationURL() throws IOException {
		String rtu = getOAuthRequestTokenURL();
		if (rtu == null) {
			return getOAuthAuthorizationURL();
		}
		
		try {
			requestToken = auth.requestTemporaryCredentials(
					new URL(rtu), Method.GET);
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
					new URL(getOAuthAccessTokenURL()), getOAuthAccessTokenMethod(),
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
	 * @param additionalHeaders additional HTTP request headers (may be null)
	 * @return the parsed response
	 * @throws IOException if the request was not successful
	 */
	protected Map<String, Object> performRequest(String url,
			Map<String, String> additionalHeaders) throws IOException {
		if (accessToken == null) {
			throw new UnauthorizedException("Access token has not yet been requested");
		}
		URL u = new URL(url);
		Response response = auth.request(u, Method.GET, accessToken, additionalHeaders);
		InputStream is = response.getInputStream();
		try {
			return parseResponse(response);
		} finally {
			is.close();
		}
	}
	
	/**
	 * Parses the given response. The response's input stream doesn't have
	 * to be closed. The caller will already do this.
	 * @param response the HTTP response to parse
	 * @return the parsed result
	 * @throws IOException if the response could not be read
	 */
	protected Map<String, Object> parseResponse(Response response) throws IOException {
		InputStream is = response.getInputStream();
		Reader r = new BufferedReader(new InputStreamReader(is));
		return new JsonParser(new JsonLexer(r)).parseObject();
	}
}

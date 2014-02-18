// Copyright 2014 Michel Kraemer
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

package de.undercouch.citeproc.helper.oauth;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import de.undercouch.citeproc.helper.json.JsonLexer;
import de.undercouch.citeproc.helper.json.JsonParser;

/**
 * Performs OAuth v2 authentication. Very minimal implementation, only used
 * for Mendeley authentication currently.
 * @author Michel Kraemer
 */
public class OAuth2 implements OAuth {
	private static final String ACCESS_TOKEN = "access_token";
	private static final String AUTHORIZATION_CODE = "authorization_code";
	private static final String BEARER = "Bearer";
	private static final String CLIENT_ID = "client_id";
	private static final String CLIENT_SECRET = "client_secret";
	private static final String CODE = "code";
	private static final String GRANT_TYPE = "grant_type";
	private static final String REDIRECT_URI = "redirect_uri";
	private static final String UTF8 = "UTF-8";
	
	private static final String HEADER_AUTHORIZATION = "Authorization";
	private static final String HEADER_CONTENT_LENGTH = "Content-Length";
	
	private final String consumerKey;
	private final String consumerSecret;
	private final String redirectUri;
	
	/**
	 * Creates a new OAuth client
	 * @param consumerKey the consumer key
	 * @param consumerSecret the consumer secret
	 * @param redirectUri the location users are redirected to after
	 * they granted access
	 */
	public OAuth2(String consumerKey, String consumerSecret, String redirectUri) {
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.redirectUri = redirectUri;
	}
	
	@Override
	public Token requestTemporaryCredentials(URL url, Method method)
			throws IOException {
		throw new UnsupportedOperationException("OAuth 2 does not require "
				+ "temporary credentials");
	}

	@Override
	public Token requestTokenCredentials(URL url, Method method,
			Token temporaryCredentials, String verifier) throws IOException {
		//prepare body
		String body = "";
		body += GRANT_TYPE + "=" + URLEncoder.encode(AUTHORIZATION_CODE, UTF8);
		body += "&" + CODE + "=" + URLEncoder.encode(verifier, UTF8);
		body += "&" + REDIRECT_URI + "=" + URLEncoder.encode(redirectUri, UTF8);
		body += "&" + CLIENT_ID + "=" + URLEncoder.encode(consumerKey, UTF8);
		body += "&" + CLIENT_SECRET + "=" + URLEncoder.encode(consumerSecret, UTF8);
		
		//perform request
		Response r = request(url, method, null, null, body);
		
		//read response
		InputStream is = r.getInputStream();
		try {
			JsonParser parser = new JsonParser(new JsonLexer(
					new InputStreamReader(is, UTF8)));
			Map<String, Object> obj = parser.parseObject();
			Object at = obj.get(ACCESS_TOKEN);
			if (at == null) {
				return null;
			}
			return new Token(at.toString(), at.toString());
		} finally {
			is.close();
		}
	}

	@Override
	public Response request(URL url, Method method, Token token)
			throws IOException {
		return request(url, method, token, null);
	}

	@Override
	public Response request(URL url, Method method, Token token,
			Map<String, String> additionalHeaders) throws IOException {
		return request(url, method, token, additionalHeaders, null);
	}
	
	private Response request(URL url, Method method, Token token,
			Map<String, String> additionalHeaders, String body) throws IOException {
		//prepare HTTP connection
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setInstanceFollowRedirects(true);
		conn.setRequestMethod(method.toString());
		
		if (additionalHeaders != null) {
			for (Map.Entry<String, String> e : additionalHeaders.entrySet()) {
				conn.setRequestProperty(e.getKey(), e.getValue());
			}
		}
		
		if (token != null) {
			conn.setRequestProperty(HEADER_AUTHORIZATION,
					BEARER + " " + token.getSecret());
		}
		
		//perform request
		if (body == null) {
			conn.connect();
		} else {
			byte[] by = body.getBytes();
			
			conn.setRequestProperty(HEADER_CONTENT_LENGTH,
					Integer.toString(by.length));
			
			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			
			OutputStream os = conn.getOutputStream();
			try {
				os.write(by);
				os.flush();
			} finally {
				os.close();
			}
		}
		
		//check response
		if (conn.getResponseCode() == 401) {
			throw new UnauthorizedException("Not authorized");
		} else if (conn.getResponseCode() != 200) {
			throw new RequestException("HTTP request failed with error code: " +
					conn.getResponseCode());
		}
		
		return new Response(conn);
	}
}

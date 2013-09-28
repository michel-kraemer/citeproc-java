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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.helper.JsonLexer;
import de.undercouch.citeproc.helper.JsonParser;

/**
 * Default implementation of a {@link MendeleyConnector}
 * @author Michel Kraemer
 */
public class DefaultMendeleyConnector implements MendeleyConnector {
	/**
	 * The REST end-point used to request a Mendeley user's library
	 */
	private static final String MENDELEY_LIBRARY_ENDPOINT =
			"http://api.mendeley.com/oapi/library/";
	
	/**
	 * The REST end-point used to request a document
	 */
	private static final String MENDELEY_DOCUMENTS_ENDPOINT =
			"http://api.mendeley.com/oapi/library/documents/";

	/**
	 * OAuth service that is used to authenticate the Mendeley user
	 */
	private final OAuthService service;
	
	/**
	 * The request token used to authorize the app
	 */
	private Token requestToken;
	
	/**
	 * The access token used to sign requests
	 */
	private Token accessToken;
	
	/**
	 * Constructs a new connector
	 * @param apiKey the app's API key
	 * @param apiSecret the app's API secret
	 */
	public DefaultMendeleyConnector(String apiKey, String apiSecret) {
		service = new ServiceBuilder()
			.provider(MendeleyApi.class)
			.apiKey(apiKey)
			.apiSecret(apiSecret)
			.callback("oob")
			.build();
	}
	
	@Override
	public String getAuthorizationURL() {
		requestToken = service.getRequestToken();
		return service.getAuthorizationUrl(requestToken);
	}
	
	@Override
	public void authorize(String verificationCode) {
		Verifier verifier = new Verifier(verificationCode);
		accessToken = service.getAccessToken(requestToken, verifier);
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
	
	private Map<String, Object> performRequest(String url)
			throws MendeleyRequestException, IOException {
		OAuthRequest request = new OAuthRequest(Verb.GET, url);
		service.signRequest(accessToken, request);
		Response response = request.send();
		InputStream is = response.getStream();
		try {
			if (response.getCode() == 401) {
				throw new UnauthorizedException("Not authenticated");
			} else if (response.getCode() == 200) {
				Reader r = new BufferedReader(new InputStreamReader(is));
				return new JsonParser(new JsonLexer(r)).parseObject();
			}
			throw new MendeleyRequestException("Mendeley server returned an error. "
					+ "Response code: " + response.getCode());
		} finally {
			consumeResponse(is);
			is.close();
		}
	}
	
	@Override
	public List<String> getDocuments() throws MendeleyRequestException, IOException {
		if (accessToken == null) {
			throw new UnauthorizedException("Access token has not yet been requested");
		}
		Map<String, Object> response = performRequest(MENDELEY_LIBRARY_ENDPOINT);
		@SuppressWarnings("unchecked")
		List<String> documentIds = (List<String>)response.get("document_ids");
		return documentIds;
	}
	
	/**
	 * Consumes the rest of an input stream. All contents will be discarded.
	 * Consuming a while input stream is needed for HTTP connections
	 * to clear all resources.
	 * @param is the input stream
	 * @throws IOException if the input stream could not be read
	 */
	private void consumeResponse(InputStream is) throws IOException {
		byte[] buf = new byte[1024 * 8];
		while (is.read(buf) >= 0);
	}

	@Override
	public CSLItemData getDocument(String documentId)
			throws MendeleyRequestException, IOException {
		if (accessToken == null) {
			throw new UnauthorizedException("Access token has not yet been requested");
		}
		Map<String, Object> response = performRequest(MENDELEY_DOCUMENTS_ENDPOINT + documentId);
		return MendeleyConverter.convert(documentId, response);
	}
}

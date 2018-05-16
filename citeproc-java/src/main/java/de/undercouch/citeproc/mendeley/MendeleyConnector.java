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
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.helper.oauth.OAuth;
import de.undercouch.citeproc.helper.oauth.OAuth.Method;
import de.undercouch.citeproc.helper.oauth.OAuth2;
import de.undercouch.citeproc.helper.oauth.Response;
import de.undercouch.citeproc.remote.AbstractRemoteConnector;

/**
 * Reads documents from the Mendeley REST services. Needs an OAuth API key and
 * secret in order to authenticate. Users of this class should
 * <a href="http://dev.mendeley.com/">register their app</a> to receive such a
 * key and secret.
 * @author Michel Kraemer
 */
public class MendeleyConnector extends AbstractRemoteConnector {
	private static final String OAUTH_ACCESS_TOKEN_URL =
			"https://api.mendeley.com/oauth/token";
	private static final String OAUTH_AUTHORIZATION_URL_TEMPLATE =
			"https://api.mendeley.com/oauth/authorize?redirect_uri=%s"
			+ "&response_type=code&scope=all&client_id=%s";
	
	/**
	 * The REST end-point used to request a Mendeley user's documents
	 */
	private static final String MENDELEY_DOCUMENTS_ENDPOINT =
			"https://api.mendeley.com/documents/";
	
	/**
	 * The default headers to send with every request
	 */
	private static final Map<String, String> DEFAULT_HEADERS;
	static {
		DEFAULT_HEADERS = new HashMap<>();
		// Mendeley API version 1
		DEFAULT_HEADERS.put("Accept", "application/vnd.mendeley-document.1+json");
	}
	
	/**
	 * The response header field containing links to next and previous pages
	 */
	private static final String LINK = "Link";
	
	/**
	 * Parameter to get all attributes of a document
	 */
	private static final String VIEWALL = "view=all";
	
	/**
	 * The remote service's authorization end-point
	 */
	private final String oauthAuthorizationUrl;
	
	/**
	 * A cache for retrieved documents
	 */
	private Map<String, CSLItemData> cachedDocuments;
	
	/**
	 * Constructs a new connector
	 * @param clientId the Mendeley app's client ID
	 * @param clientSecret the app's client secret
	 * @param redirectUri the location users are redirected to after
	 * they granted the Mendeley app access to their library
	 */
	public MendeleyConnector(String clientId, String clientSecret,
			String redirectUri) {
		super(clientId, clientSecret, redirectUri);
		
		try {
			oauthAuthorizationUrl = String.format(
					OAUTH_AUTHORIZATION_URL_TEMPLATE,
					URLEncoder.encode(redirectUri, "UTF-8"), clientId);
		} catch (UnsupportedEncodingException e) {
			//should never happen
			throw new RuntimeException(e);
		}
	}
	
	@Override
	protected String getOAuthRequestTokenURL() {
		//we don't need this for OAuth v2
		return null;
	}
	
	@Override
	protected String getOAuthAuthorizationURL() {
		return oauthAuthorizationUrl;
	}
	
	@Override
	protected String getOAuthAccessTokenURL() {
		return OAUTH_ACCESS_TOKEN_URL;
	}
	
	@Override
	protected Method getOAuthAccessTokenMethod() {
		return Method.POST;
	}
	
	@Override
	protected OAuth createOAuth(String consumerKey, String consumerSecret,
			String redirectUri) {
		return new OAuth2(consumerKey, consumerSecret, redirectUri);
	}
	
	private void addToCache(String id, CSLItemData doc) {
		if (cachedDocuments == null) {
			cachedDocuments = new HashMap<>();
		}
		cachedDocuments.put(id, doc);
	}
	
	private CSLItemData getFromCache(String id) {
		if (cachedDocuments == null) {
			return null;
		}
		return cachedDocuments.get(id);
	}
	
	private void clearCache() {
		if (cachedDocuments != null) {
			cachedDocuments = null;
		}
	}
	
	@Override
	public List<String> getItemIDs() throws IOException {
		String nextLink = MENDELEY_DOCUMENTS_ENDPOINT + "?" + VIEWALL;
		List<String> result = new ArrayList<>();
		clearCache();
		
		while (nextLink != null) {
			Response response = performRequest(nextLink, DEFAULT_HEADERS);
			
			// get link to next page from response
			nextLink = null;
			List<String> linkHeaders = response.getHeaders(LINK);
			if (linkHeaders != null) {
				for (String linkHeader : linkHeaders) {
					String[] linksHeaderParts = linkHeader.split("\\s*,\\s*");
					for (String linksHeaderPart : linksHeaderParts) {
						String[] parts = linksHeaderPart.split("\\s*;\\s*");
						if (parts.length > 1 && parts[1].equals("rel=\"next\"")) {
							nextLink = parts[0];
							break;
						}
					}
					if (nextLink != null) {
						if (nextLink.charAt(0) == '<' &&
								nextLink.charAt(nextLink.length() - 1) == '>') {
							nextLink = nextLink.substring(1, nextLink.length() - 1);
						}
						break;
					}
				}
			}
			
			// get documents from response
			try (InputStream is = response.getInputStream()) {
				List<Object> docsArr = parseResponseArray(response);
				for (Object docObj : docsArr) {
					if (docObj instanceof Map) {
						@SuppressWarnings("unchecked")
						Map<String, Object> docMap = (Map<String, Object>)docObj;
						String id = docMap.get("id").toString();
						CSLItemData doc = MendeleyConverter.convert(id, docMap);
						result.add(id);
						addToCache(id, doc);
					}
				}
			}
		}
		
		return result;
	}
	
	@Override
	public CSLItemData getItem(String itemId) throws IOException {
		CSLItemData item = getFromCache(itemId);
		if (item == null) {
			Map<String, Object> response = performRequestObject(
					MENDELEY_DOCUMENTS_ENDPOINT + itemId + "?" + VIEWALL, null);
			item = MendeleyConverter.convert(itemId, response);
			addToCache(itemId, item);
		}
		return item;
	}
	
	@Override
	public Map<String, CSLItemData> getItems(List<String> itemIds) throws IOException {
		Map<String, CSLItemData> result = new LinkedHashMap<>(itemIds.size());
		for (String id : itemIds) {
			result.put(id, getItem(id));
		}
		return result;
	}
	
	@Override
	public int getMaxBulkItems() {
		return 1;
	}
}

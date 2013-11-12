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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.helper.json.JsonLexer;
import de.undercouch.citeproc.helper.json.JsonParser;
import de.undercouch.citeproc.helper.oauth.OAuth;
import de.undercouch.citeproc.helper.oauth.Response;
import de.undercouch.citeproc.helper.oauth.UnauthorizedException;
import de.undercouch.citeproc.remote.AbstractRemoteConnector;

/**
 * Connects to the Zotero API v2. Needs an OAuth API key and secret in order
 * to authenticate. Users of this class should
 * <a href="http://www.zotero.org/oauth/apps">register their app</a> to
 * receive such a key and secret.
 * @author Michel Kraemer
 */
public class ZoteroConnector extends AbstractRemoteConnector {
	private static final String OAUTH_ACCESS_TOKEN_URL =
			"https://www.zotero.org/oauth/access";
	private static final String OAUTH_REQUEST_TOKEN_URL =
			"https://www.zotero.org/oauth/request";
	private static final String OAUTH_AUTHORIZATION_URL =
			"https://www.zotero.org/oauth/authorize?" +
					"name=citeproc-java&library_access=1&oauth_token=";
	
	private static final String ENDPOINT_USERS = "https://api.zotero.org/users/";
	
	private static final String CSLJSON = "csljson";
	
	private static final Map<String, String> REQUEST_HEADERS = new HashMap<String, String>();
	static {
		REQUEST_HEADERS.put("Zotero-API-Version", "2");
	}
	
	/**
	 * @see AbstractRemoteConnector#AbstractRemoteConnector(String, String)
	 */
	public ZoteroConnector(String consumerKey, String consumerSecret) {
		super(consumerKey, consumerSecret);
	}
	
	@Override
	protected String getOAuthRequestTokenURL() {
		return OAUTH_REQUEST_TOKEN_URL;
	}

	@Override
	protected String getOAuthAuthorizationURL() {
		return OAUTH_AUTHORIZATION_URL;
	}

	@Override
	protected String getOAuthAccessTokenURL() {
		return OAUTH_ACCESS_TOKEN_URL;
	}
	
	@Override
	protected OAuth createOAuth(String consumerKey, String consumerSecret) {
		return new ZoteroOAuth(consumerKey, consumerSecret);
	}
	
	@Override
	public List<String> getItemIDs() throws IOException {
		if (accessToken == null) {
			throw new UnauthorizedException("Access token has not yet been requested");
		}
		
		//since Zotero uses a single API key we store the user ID in the token
		//see ZoteroOAuth#responseToToken(Map<String, String>)
		String userId = accessToken.getToken();
		String key = accessToken.getSecret();
		
		Map<String, Object> res = performRequest(ENDPOINT_USERS +
				userId + "/items?key=" + key + "&newer=0&format=versions"
						+ "&itemType=-attachment", REQUEST_HEADERS);
		return new ArrayList<String>(res.keySet());
	}

	@Override
	public CSLItemData getItem(String itemId) throws IOException {
		Map<String, CSLItemData> r = getItems(Arrays.asList(itemId));
		return r.get(itemId);
	}
	
	@Override
	protected Map<String, Object> parseResponse(Response response) throws IOException {
		String contentType = response.getHeader("Content-Type");
		InputStream is = response.getInputStream();
		Reader r = new BufferedReader(new InputStreamReader(is));
		if (contentType != null && contentType.equals("application/atom+xml")) {
			//response is an Atom. parse it...
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			Document doc;
			try {
				DocumentBuilder builder = factory.newDocumentBuilder();
				InputSource source = new InputSource(r);
				doc = builder.parse(source);
			} catch (SAXException e) {
				throw new IOException("Could not parse server response", e);
			} catch (ParserConfigurationException e) {
				throw new IOException("Could not create XML parser", e);
			}
			
			Map<String, Object> result = new HashMap<String, Object>();
			
			//extract content in 'csljson' format
			Element feed = doc.getDocumentElement();
			NodeList entries = feed.getElementsByTagName("entry");
			for (int i = 0; i < entries.getLength(); ++i) {
				String key = null;
				String csljson = null;
				
				Node entry = entries.item(i);
				if (entry instanceof Element) {
					Element entryElem = (Element)entry;
					NodeList keys = entryElem.getElementsByTagNameNS(
							"http://zotero.org/ns/api", "key");
					if (keys.getLength() > 0) {
						key = keys.item(0).getTextContent().trim();
						NodeList contents = entryElem.getElementsByTagName("content");
						if (contents.getLength() > 0) {
							Node content = contents.item(0);
							Node type = content.getAttributes().getNamedItemNS(
									"http://zotero.org/ns/api", "type");
							if (type != null && type.getTextContent().equals(CSLJSON)) {
								csljson = content.getTextContent().trim();
							}
						}
					}
				}
				
				if (csljson == null || csljson.isEmpty()) {
					throw new IOException("Could not extract CSL json content from entry");
				}
				
				Map<String, Object> item = new JsonParser(
						new JsonLexer(new StringReader(csljson))).parseObject();
				result.put(key, item);
			}
			
			return result;
		} else {
			return super.parseResponse(response);
		}
	}
	
	@Override
	public Map<String, CSLItemData> getItems(List<String> itemIds) throws IOException {
		if (accessToken == null) {
			throw new UnauthorizedException("Access token has not yet been requested");
		}
		
		//since Zotero uses a single API key we store the user ID in the token
		//see ZoteroOAuth#responseToToken(Map<String, String>)
		String userId = accessToken.getToken();
		String key = accessToken.getSecret();
		
		Map<String, CSLItemData> result = new LinkedHashMap<String, CSLItemData>(itemIds.size());
		int s = 0;
		while (s < itemIds.size()) {
			int n = Math.min(getMaxBulkItems(), itemIds.size() - s);
			List<String> itemsToRequest = itemIds.subList(s, s + n);
			String istr = StringUtils.join(itemsToRequest, ',');
			
			Map<String, Object> res = performRequest(ENDPOINT_USERS +
					userId + "/items?key=" + key +
					"&content=" + CSLJSON  +
					"&itemKey=" + istr, REQUEST_HEADERS);
			
			for (Map.Entry<String, Object> e : res.entrySet()) {
				String id = e.getKey();
				@SuppressWarnings("unchecked")
				Map<String, Object> m = (Map<String, Object>)e.getValue();
				CSLItemData item = CSLItemData.fromJson(m);
				result.put(id, item);
			}
			
			s += n;
		}
		
		return result;
	}
	
	@Override
	public int getMaxBulkItems() {
		return 50;
	}
}

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

package de.undercouch.citeproc.helper.oauth;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import de.undercouch.citeproc.helper.CSLUtils;

/**
 * Performs OAuth authentication. Very minimal implementation, only used
 * for Zotero and Mendeley authentication currently.
 * @author Michel Kraemer
 */
public class OAuth1 implements OAuth {
	/**
	 * Service response item specifying a token's value
	 */
	protected static final String OAUTH_TOKEN = "oauth_token";
	
	/**
	 * Service response item specifying a token's secret
	 */
	protected static final String OAUTH_TOKEN_SECRET = "oauth_token_secret";
	
	private static final String OAUTH_CALLBACK = "oauth_callback";
	private static final String OAUTH_CONSUMER_KEY = "oauth_consumer_key";
	private static final String OAUTH_NONCE = "oauth_nonce";
	private static final String OAUTH_SIGNATURE = "oauth_signature";
	private static final String OAUTH_SIGNATURE_METHOD = "oauth_signature_method";
	private static final String OAUTH_TIMESTAMP = "oauth_timestamp";
	private static final String OAUTH_VERIFIER = "oauth_verifier";
	private static final String OAUTH_VERSION = "oauth_version";

	private static final String UTF8 = "UTF-8";

	private static final String OAUTH_IMPL_VERSION = "1.0";
	private static final String HMAC_SHA1_METHOD = "HMAC-SHA1";
	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

	private static final String HEADER_AUTHORIZATION = "Authorization";
	private static final String HEADER_HOST = "Host";

	/**
	 * Out-of-band callback. Use this if you don't want to provide a callback
	 * URL to the OAuth server
	 */
	private static final String CALLBACK_OOB = "oob";

	private final String consumerKey;
	private final String consumerSecret;
	private final SecureRandom random = new SecureRandom();
	
	/**
	 * Creates a new OAuth client
	 * @param consumerKey the consumer key
	 * @param consumerSecret the consumer secret
	 */
	public OAuth1(String consumerKey, String consumerSecret) {
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
	}
	
	@Override
	public Token requestTemporaryCredentials(URL url, Method method)
			throws IOException {
		Map<String, String> aap = new HashMap<String, String>();
		aap.put(OAUTH_CALLBACK, CALLBACK_OOB);
		return requestCredentials(url, method, null, aap);
	}
	
	@Override
	public Token requestTokenCredentials(URL url, Method method,
			Token temporaryCredentials, String verifier) throws IOException {
		Map<String, String> aap = new HashMap<String, String>();
		aap.put(OAUTH_VERIFIER, verifier);
		return requestCredentials(url, method, temporaryCredentials, aap);
	}
	
	@Override
	public Response request(URL url, Method method, Token token) throws IOException {
		return request(url, method, token, null);
	}
	
	@Override
	public Response request(URL url, Method method, Token token,
			Map<String, String> additionalHeaders) throws IOException {
		return requestInternal(url, method, token, null, additionalHeaders);
	}
	
	/**
	 * Sends a request to the server and returns a token
	 * @param url the URL to send the request to
	 * @param method the HTTP request method
	 * @param token a token used for authorization (may be null if the
	 * app is not authorized yet)
	 * @param additionalAuthParams additional parameters that should be
	 * added to the <code>Authorization</code> header
	 * @return the token
	 * @throws IOException if the request was not successful
	 * @throws RequestException if the server returned an error
	 * @throws UnauthorizedException if the request is not authorized
	 */
	private Token requestCredentials(URL url, Method method, Token token,
			Map<String, String> additionalAuthParams) throws IOException {
		Response r = requestInternal(url, method, token, additionalAuthParams, null);
		InputStream is = r.getInputStream();
		String response = CSLUtils.readStreamToString(is, UTF8);
		
		//create token for temporary credentials
		Map<String, String> sr = splitResponse(response);
		return responseToToken(sr);
	}
	
	/**
	 * Parses a service response and creates a token
	 * @param response the response
	 * @return the token
	 */
	protected Token responseToToken(Map<String, String> response) {
		return new Token(response.get(OAUTH_TOKEN), response.get(OAUTH_TOKEN_SECRET));
	}
	
	/**
	 * Sends a request to the server and returns an input stream from which
	 * the response can be read. The caller is responsible for consuming
	 * the input stream's content and for closing the stream.
	 * @param url the URL to send the request to
	 * @param method the HTTP request method
	 * @param token a token used for authorization (may be null if the
	 * app is not authorized yet)
	 * @param additionalAuthParams additional parameters that should be
	 * added to the <code>Authorization</code> header (may be null)
	 * @param additionalHeaders additional HTTP headers (may be null)
	 * @return a response
	 * @throws IOException if the request was not successful
	 * @throws RequestException if the server returned an error
	 * @throws UnauthorizedException if the request is not authorized
	 */
	private Response requestInternal(URL url, Method method, Token token,
			Map<String, String> additionalAuthParams,
			Map<String, String> additionalHeaders) throws IOException {
		//prepare HTTP connection
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setInstanceFollowRedirects(true);
		conn.setRequestMethod(method.toString());
		
		conn.setRequestProperty(HEADER_HOST, makeBaseUri(url));

		String timestamp = makeTimestamp();
		String nonce = makeNonce(timestamp);
		
		//create OAuth parameters
		Map<String, String> authParams = new HashMap<String, String>();
		if (additionalAuthParams != null) {
			authParams.putAll(additionalAuthParams);
		}
		if (token != null) {
			authParams.put(OAUTH_TOKEN, token.getToken());
		}
		authParams.put(OAUTH_CONSUMER_KEY, consumerKey);
		authParams.put(OAUTH_SIGNATURE_METHOD, HMAC_SHA1_METHOD);
		authParams.put(OAUTH_TIMESTAMP, timestamp);
		authParams.put(OAUTH_NONCE, nonce);
		authParams.put(OAUTH_VERSION, OAUTH_IMPL_VERSION);
		
		//create signature from method, url, and OAuth parameters
		String signature = makeSignature(method.toString(), url, authParams, token);
		
		//put OAuth parameters into "Authorization" header
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> e : authParams.entrySet()) {
			appendAuthParam(sb, e.getKey(), e.getValue());
		}
		appendAuthParam(sb, OAUTH_SIGNATURE, signature);
		
		conn.setRequestProperty(HEADER_AUTHORIZATION, "OAuth " + sb.toString());
		
		if (additionalHeaders != null) {
			for (Map.Entry<String, String> e : additionalHeaders.entrySet()) {
				conn.setRequestProperty(e.getKey(), e.getValue());
			}
		}
		
		//perform request
		conn.connect();
		
		//check response
		if (conn.getResponseCode() == 401) {
			throw new UnauthorizedException("Not authorized");
		} else if (conn.getResponseCode() != 200) {
			throw new RequestException("HTTP request failed with error code: " +
					conn.getResponseCode());
		}
		
		return new Response(conn);
	}
	
	/**
	 * Appends an authorization parameter to the given string builder
	 * @param sb the string builder
	 * @param key the parameter's key
	 * @param value the parameter's value
	 */
	private static void appendAuthParam(StringBuilder sb, String key, String value) {
		if (sb.length() > 0) {
			sb.append(",");
		}
		sb.append(key);
		sb.append("=\"");
		sb.append(value);
		sb.append("\"");
	}
	
	/**
	 * Generates a timestamp for a request
	 * @return the timestamp
	 */
	private static String makeTimestamp() {
		return String.valueOf(System.currentTimeMillis() / 1000);
	}
	
	/**
	 * Generates a nonce for a request using a secure random number generator
	 * @param timestamp the request's timestamp (generated by {@link #makeTimestamp()})
	 * @return the nonce
	 */
	private String makeNonce(String timestamp) {
		byte[] bytes = new byte[10];
		random.nextBytes(bytes);
		StringBuilder sb = new StringBuilder(timestamp);
		sb.append("-");
		for (int i = 0; i < bytes.length; ++i) {
			String b = Integer.toHexString(bytes[i] & 0xff);
			if (b.length() < 2) {
				sb.append("0");
			}
			sb.append(b);
		}
		return sb.toString();
	}
	
	/**
	 * Generates a base URI from a given URL. The base URI consists of the
	 * protocol, the host, and also the port if its not the default port
	 * for the given protocol.
	 * @param url the URL from which the URI should be generated
	 * @return the base URI
	 */
	private static String makeBaseUri(URL url) {
		String r = url.getProtocol().toLowerCase() + "://" +
				url.getHost().toLowerCase();
		if ((url.getProtocol().equalsIgnoreCase("http") &&
				url.getPort() != -1 && url.getPort() != 80) ||
			(url.getProtocol().equalsIgnoreCase("https") &&
					url.getPort() != -1 && url.getPort() != 443)) {
			r += ":" + url.getPort();
		}
		return r;
	}
	
	/**
	 * Splits the query parameters of the given URL, encodes their
	 * keys and values and puts each key-value pair in a list
	 * @param url the URL
	 * @return the encoded parameters
	 */
	private static List<String> splitAndEncodeParams(URL url) {
		if (url.getQuery() == null) {
			return new ArrayList<String>();
		}
		String[] params = url.getQuery().split("&");
		List<String> result = new ArrayList<String>(params.length);
		for (String p : params) {
			String[] kv = p.split("=");
			kv[0] = PercentEncoding.decode(kv[0]);
			kv[1] = PercentEncoding.decode(kv[1]);
			kv[0] = PercentEncoding.encode(kv[0]);
			kv[1] = PercentEncoding.encode(kv[1]);
			String np = kv[0] + "=" + kv[1];
			result.add(np);
		}
		return result;
	}
	
	/**
	 * Splits a <code>application/x-www-form-urlencoded</code> response
	 * @param response the response
	 * @return the key-value pairs
	 */
	private static Map<String, String> splitResponse(String response) {
		String[] params = response.split("&");
		Map<String, String> result = new HashMap<String, String>(params.length);
		for (String p : params) {
			String[] kv = p.split("=");
			result.put(PercentEncoding.decode(kv[0]), PercentEncoding.decode(kv[1]));
		}
		return result;
	}
	
	/**
	 * Generates a OAuth signature from the HTTP method, the URL, and the
	 * authorization parameters
	 * @param method the HTTP method
	 * @param url the URL
	 * @param authParams the authorization parameters
	 * @param token the authorization token used for this request (may be null)
	 * @return the signature
	 */
	private String makeSignature(String method, URL url,
			Map<String, String> authParams, Token token) {
		//encode method and URL
		StringBuilder sb = new StringBuilder(method + "&" +
				PercentEncoding.encode(makeBaseUri(url) + url.getPath()));
		
		//encode parameters
		List<String> params = splitAndEncodeParams(url);
		for (Map.Entry<String, String> p : authParams.entrySet()) {
			params.add(PercentEncoding.encode(p.getKey()) + "=" +
					PercentEncoding.encode(p.getValue()));
		}
		
		//sort parameters and append them to the base string
		Collections.sort(params);
		StringBuilder pb = new StringBuilder();
		for (String p : params) {
			if (pb.length() > 0) {
				pb.append("&");
			}
			pb.append(p);
		}
		sb.append("&");
		sb.append(PercentEncoding.encode(pb.toString()));
		
		//create base string and key for hash function
		String baseString = sb.toString();
		String tokenSecret = token != null ? token.getSecret() : "";
		String key = PercentEncoding.encode(consumerSecret) + "&" +
				PercentEncoding.encode(tokenSecret);
		
		//hash base string with the secret key
		try {
			SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(UTF8),
					HMAC_SHA1_ALGORITHM);
			Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(secretKey);
			byte[] bytes = mac.doFinal(baseString.getBytes(UTF8));
			return PercentEncoding.encode(DatatypeConverter.printBase64Binary(bytes));
		} catch (UnsupportedEncodingException e) {
			//should never happen
			throw new RuntimeException(e);
		} catch (NoSuchAlgorithmException e) {
			//should never happen
			throw new RuntimeException(e);
		} catch (InvalidKeyException e) {
			//should never happen
			throw new RuntimeException(e);
		}
	}
}

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
import java.net.URL;
import java.util.Map;

/**
 * Performs OAuth authentication (either version 1 or 2)
 * @author Michel Kraemer
 */
public interface OAuth {
	/**
	 * HTTP request methods
	 */
	public static enum Method {
		/**
		 * HTTP GET method
		 */
		GET("GET"),
		
		/**
		 * HTTP POST method
		 */
		POST("POST"),
		
		/**
		 * HTTP HEAD method
		 */
		HEAD("HEAD"),
		
		/**
		 * HTTP OPTIONS method
		 */
		OPTIONS("OPTIONS"),
		
		/**
		 * HTTP PUT method
		 */
		PUT("PUT"),
		
		/**
		 * HTTP DELETE method
		 */
		DELETE("DELETE");
		
		private String name;
		
		private Method(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	/**
	 * Requests temporary credentials. This is the first step in the OAuth
	 * authentication process. The temporary credentials are used to generate
	 * an authorization URL.
	 * @param url the URL from which the temporary credentials should be requested
	 * @param method the HTTP request method
	 * @return the temporary credentials
	 * @throws IOException if the request was not successful
	 * @throws RequestException if the server returned an error
	 * @throws UnauthorizedException if the request is not authorized
	 */
	public abstract Token requestTemporaryCredentials(URL url, Method method)
			throws IOException;

	/**
	 * Requests token credentials. This is the third step in the OAuth
	 * authentication process after the user has authenticated the app
	 * using the authorization URL and the temporary credentials created with
	 * {@link #requestTemporaryCredentials(URL, Method)}
	 * @param url the URL from which the token credentials should be requested
	 * @param method the HTTP request method
	 * @param temporaryCredentials the temporary credentials
	 * @param verifier the verification code returned by the OAuth server
	 * @return the token credentials
	 * @throws IOException if the request was not successful
	 * @throws RequestException if the server returned an error
	 * @throws UnauthorizedException if the request is not authorized
	 */
	public abstract Token requestTokenCredentials(URL url, Method method,
			Token temporaryCredentials, String verifier) throws IOException;

	/**
	 * Sends a request to the server and returns an input stream from which
	 * the response can be read. The caller is responsible for consuming
	 * the input stream's content and for closing the stream.
	 * @param url the URL to send the request to
	 * @param method the HTTP request method
	 * @param token a token used for authorization (may be null if the
	 * authorization is not required for this request)
	 * @return a response
	 * @throws IOException if the request was not successful
	 * @throws RequestException if the server returned an error
	 * @throws UnauthorizedException if the request is not authorized
	 */
	public abstract Response request(URL url, Method method, Token token)
			throws IOException;

	/**
	 * Sends a request to the server and returns an input stream from which
	 * the response can be read. The caller is responsible for consuming
	 * the input stream's content and for closing the stream.
	 * @param url the URL to send the request to
	 * @param method the HTTP request method
	 * @param token a token used for authorization (may be null if the
	 * authorization is not required for this request)
	 * @param additionalHeaders additional HTTP headers (may be null)
	 * @return a response
	 * @throws IOException if the request was not successful
	 * @throws RequestException if the server returned an error
	 * @throws UnauthorizedException if the request is not authorized
	 */
	public abstract Response request(URL url, Method method, Token token,
			Map<String, String> additionalHeaders) throws IOException;
}

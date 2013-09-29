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

/**
 * Saves an OAuth access token
 * @author Michel Kraemer
 */
public interface AuthenticationStore {
	/**
	 * @return the stored access token's value or null if no token value has
	 * been stored yet
	 */
	String getToken();
	
	/**
	 * @return the stored access token's secret or null if no secret has
	 * been stored yet
	 */
	String getSecret();
	
	/**
	 * Stores a new access token
	 * @param token the token's value (must not be null)
	 * @param secret the token's secret (must not be null)
	 * @throws IOException if the values could not stored
	 */
	void save(String token, String secret) throws IOException;
	
	/**
	 * Resets the contents of this authentication store
	 * @throws IOException if the store could not be reset
	 */
	void reset() throws IOException;
}

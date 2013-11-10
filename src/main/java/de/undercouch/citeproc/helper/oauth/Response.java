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
import java.net.HttpURLConnection;

/**
 * A response of an OAuth request
 * @author Michel Kraemer
 */
public class Response {
	private final HttpURLConnection conn;
	private InputStream is;
	
	/**
	 * Creates a new response for a HTTP connection
	 * @param conn the HTTP connection
	 */
	public Response(HttpURLConnection conn) {
		this.conn = conn;
	}
	
	/**
	 * @return an input stream to read the response contents
	 * @throws IOException if the input stream could not be created
	 */
	public InputStream getInputStream() throws IOException {
		if (is == null) {
			is = conn.getInputStream();
		}
		return is;
	}
	
	/**
	 * Gets the value of a named response header field
	 * @param name the header field's name
	 * @return the header's value or null if there is no such header field
	 */
	public String getHeader(String name) {
		return conn.getHeaderField(name);
	}
}

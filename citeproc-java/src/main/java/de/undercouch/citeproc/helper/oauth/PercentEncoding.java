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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Encodes and decodes strings according to the rules defined in the
 * OAuth specification
 * @author Michel Kraemer
 */
public class PercentEncoding {
	/**
	 * Encodes a string
	 * @param str the string
	 * @return the encoded string
	 */
	public static String encode(String str) {
		try {
			str = URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			//should never happen
			throw new RuntimeException(e);
		}
		return str.replace("*", "%2A").replace("+", "%20").replace("%7E", "~");
	}
	
	/**
	 * Decodes a string
	 * @param str the string
	 * @return the decoded string
	 */
	public static String decode(String str) {
		try {
			return URLDecoder.decode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			//should never happen
			throw new RuntimeException(e);
		}
	}
}

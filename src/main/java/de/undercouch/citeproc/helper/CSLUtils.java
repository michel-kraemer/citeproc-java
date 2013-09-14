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

package de.undercouch.citeproc.helper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Utilities for the CSL processor
 * @author Michel Kraemer
 */
public class CSLUtils {
	/**
	 * Reads a string from a URL
	 * @param u the URL
	 * @param encoding the character encoding
	 * @return the string
	 * @throws IOException if the URL contents could not be read
	 */
	public static String readURLToString(URL u, String encoding) throws IOException {
		return readStreamToString(u.openStream(), encoding);
	}
	
	/**
	 * Reads a string from a file.
	 * @param f the file
	 * @param encoding the character encoding
	 * @return the string
	 * @throws IOException if the file contents could not be read
	 */
	public static String readFileToString(File f, String encoding) throws IOException {
		return readStreamToString(new FileInputStream(f), encoding);
	}
	
	/**
	 * Reads a string from a stream. Closes the stream after reading.
	 * @param is the stream
	 * @param encoding the character encoding
	 * @return the string
	 * @throws IOException if the URL contents could not be read
	 */
	public static String readStreamToString(InputStream is, String encoding) throws IOException {
		try {
			StringBuffer sb = new StringBuffer();
			byte[] buf = new byte[1024 * 10];
			int read;
			while ((read = is.read(buf)) >= 0) {
				sb.append(new String(buf, 0, read, encoding));
			}
			return sb.toString();
		} finally {
			is.close();
		}
	}
	
	/**
	 * Reads a byte array from a stream. Closes the stream after reading.
	 * @param is the stream
	 * @return the byte array
	 * @throws IOException if the URL contents could not be read
	 */
	public static byte[] readStream(InputStream is) throws IOException {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buf = new byte[1024 * 10];
			int read;
			while ((read = is.read(buf)) >= 0) {
				baos.write(buf, 0, read);
			}
			return baos.toByteArray();
		} finally {
			is.close();
		}
	}
}

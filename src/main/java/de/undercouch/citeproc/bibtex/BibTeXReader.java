// Copyright 2013 The Docear Project and Michel Kraemer
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

package de.undercouch.citeproc.bibtex;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;

/**
 * A special reader for BibTeX files skipping the header
 * @author Michel Kraemer
 */
public class BibTeXReader extends PushbackReader {
	/**
	 * @see PushbackReader#PushbackReader(Reader)
	 * @throws IOException if <code>in</code> could not be read
	 */
	public BibTeXReader(Reader in) throws IOException {
		super(in);
		skipHeader();
	}
	
	/**
	 * Reads from the input stream until the first bibtex entry
	 * has been reached
	 * @throws IOException if the input stream could not be read
	 */
	private void skipHeader() throws IOException {
		int c;
		while (true) {
			c = read();
			if (c == '@') {
				break;
			}
			if (c < 0) {
				return;
			}
		}
		unread(c);
	}
}

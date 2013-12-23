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

package de.undercouch.citeproc.bibtex;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;

/**
 * A special reader for BibTeX files skipping header and extra
 * commas after entries
 * @author Michel Kraemer
 */
public class BibTeXReader extends PushbackReader {
	private int level = 0;
	private boolean inescape = false;
	
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
	
	@Override
	public int read() throws IOException {
		int r = super.read();
		if (r >= 0) {
			return filterCharacter((char)r);
		}
		return r;
	}
	
	@Override
	public int read(char cbuf[], int off, int len) throws IOException {
		int r = super.read(cbuf, off, len);
		if (r > 0) {
			for (int i = 0; i < r; ++i) {
				cbuf[i + off] = filterCharacter(cbuf[i + off]);
			}
		}
		return r;
	}
	
	private char filterCharacter(char c) {
		if (inescape) {
			inescape = false;
			return c;
		}
		
		inescape = (c == '\\');
		
		if (c == '{') {
			++level;
		} else if (c == '}') {
			--level;
		}
		
		//skip every extra comma on level 0. replace it by
		//a whitespace character
		if (level == 0 && c == ',') {
			c = ' ';
		}
		
		return c;
	}
}

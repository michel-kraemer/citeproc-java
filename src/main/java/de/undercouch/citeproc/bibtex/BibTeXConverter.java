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
import java.io.InputStream;
import java.util.Map;

import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.Key;
import org.jbibtex.ParseException;

import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CSLType;

/**
 * <p>Converts BibTeX items to CSL citation items</p>
 * @author Michel Kraemer
 */
public class BibTeXConverter {
	/**
	 * <p>Loads a BibTeX database from a stream.</p>
	 * <p>This method does not close the given stream. The caller is
	 * responsible for closing it.</p>
	 * @param is the input stream to read from
	 * @return the BibTeX database
	 * @throws IOException if the database could not be read
	 * @throws ParseException if the database is invalid
	 */
	public BibTeXDatabase loadDatabase(InputStream is) throws IOException, ParseException {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Converts the given database to a map of CSL citation items
	 * @param db the database
	 * @return a map consisting of citation keys and citation items
	 */
	public Map<String, CSLItemData> toItemData(BibTeXDatabase db) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Converts a BibTeX entry to a citation item
	 * @param e the BibTeX entry to convert
	 * @return the citation item
	 */
	public CSLItemData toItemData(BibTeXEntry e) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Converts a BibTeX type to a CSL type
	 * @param type the type to convert
	 * @return the converted type (never null, falls back to {@link CSLType#ARTICLE})
	 */
	public CSLType toType(Key type) {
		throw new UnsupportedOperationException();
	}
}

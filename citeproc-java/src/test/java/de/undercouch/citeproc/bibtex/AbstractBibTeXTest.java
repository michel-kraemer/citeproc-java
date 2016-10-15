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
import java.util.zip.GZIPInputStream;

import org.jbibtex.BibTeXDatabase;
import org.jbibtex.ParseException;

/**
 * Abstract base class for BibTeX-related unit tests
 * @author Michel Kraemer
 */
public abstract class AbstractBibTeXTest {
	/**
	 * Loads the <code>unix.bib</code> database from the classpath
	 * @return the database
	 * @throws IOException if the database could not be loaded
	 * @throws ParseException if the database is invalid
	 */
	protected static BibTeXDatabase loadUnixDatabase() throws IOException, ParseException {
		BibTeXDatabase db;
		try (InputStream is = AbstractBibTeXTest.class.getResourceAsStream("/unix.bib.gz")) {
			GZIPInputStream gis = new GZIPInputStream(is);
			db = new BibTeXConverter().loadDatabase(gis);
		}
		return db;
	}
}

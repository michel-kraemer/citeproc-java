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

package de.undercouch.citeproc;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Map;

import de.undercouch.citeproc.csl.CSLDate;
import de.undercouch.citeproc.csl.CSLDateBuilder;
import de.undercouch.citeproc.script.ScriptRunner;
import de.undercouch.citeproc.script.ScriptRunnerException;
import de.undercouch.citeproc.script.ScriptRunnerFactory;

/**
 * An intelligent parser for date strings. This class is able to handle
 * a wide range of date formats (e.g. YYYY-mm-dd, YYYY/mm/dd, Month YYYY)
 * @author Michel Kraemer
 */
public class CSLDateParser {
	/**
	 * A JavaScript runner used to execute citeproc-js
	 */
	private final ScriptRunner runner;
	
	/**
	 * The underlying date parser
	 */
	private final Object parser;
	
	/**
	 * Creates a new date parser
	 */
	public CSLDateParser() {
		//create JavaScript runner
		runner = ScriptRunnerFactory.createRunner();
		
		//load bundles scripts
		try {
			runner.eval(new StringReader(
					"var CSL = new function() {};" +
					"CSL.DATE_PARTS_ALL = [\"year\", \"month\", \"day\", \"season\"];" +
					"CSL.debug = function(msg) {};" +
					"function makeParser() {" +
						"var p = new CSL.DateParser();" +
						"p.returnAsArray();" +
						"return p; }"
			));
			runner.loadScript(getClass().getResource("dateparser.js"));
		} catch (IOException e) {
			//should never happen because bundled JavaScript files should be readable indeed
		} catch (ScriptRunnerException e) {
			//should never happen because bundled JavaScript files should be OK indeed
			throw new RuntimeException("Invalid bundled javascript file", e);
		}
		
		//initialize parser
		try {
			parser = runner.callMethod("makeParser", Object.class);
		} catch (ScriptRunnerException e) {
			throw new IllegalArgumentException("Could not initialize date parser", e);
		}
	}
	
	/**
	 * Parses a string to a date
	 * @param str the string to parse
	 * @return the parsed date
	 */
	public CSLDate parse(String str) {
		Map<String, Object> res;
		try {
			Map<String, Object> m = runner.callMethod(
					parser, "parse", Map.class, str);
			res = m;
		} catch (ScriptRunnerException e) {
			throw new IllegalArgumentException("Could not update items", e);
		}
		
		CSLDate r = CSLDate.fromJson(res);
		if (r.getDateParts().length == 2 && Arrays.equals(r.getDateParts()[0], r.getDateParts()[1])) {
			r = new CSLDateBuilder(r).dateParts(r.getDateParts()[0]).build();
		}
		return r;
	}
}

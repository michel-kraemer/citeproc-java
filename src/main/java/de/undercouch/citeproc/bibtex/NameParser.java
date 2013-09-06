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

import de.undercouch.citeproc.csl.CSLName;

/**
 * Parses a human's name to a {@link CSLName} object
 * @author Michel Kraemer
 */
public class NameParser {
	/**
	 * Parses names to {@link CSLName} objects. Also handles strings
	 * containing multiple names separated by <code>and</code>. The
	 * method always returns at least one object, even if the given
	 * names cannot be parsed. In this case the returned object just
	 * contains a literal string.
	 * FIXME this method is not yet ready
	 * @param names the names to parse
	 * @return the {@link CSLName} objects (never null and never empty)
	 */
	public static CSLName[] parse(String names) {
		/*ANTLRInputStream is = new ANTLRInputStream(names);
		InternalNameLexer lexer = new InternalNameLexer(is);
		lexer.removeErrorListeners(); //do not output errors to console
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		InternalNameParser parser = new InternalNameParser(tokens);
		parser.removeErrorListeners(); //do not output errors to console
		NamesContext ctx = parser.names();
		if (ctx.result.isEmpty() || ctx.exception != null) {
			//unparsable fall back to literal string
			return new CSLName[] { new CSLNameBuilder().literal(names).build() };
		}
		return ctx.result.toArray(new CSLName[ctx.result.size()]);*/
		throw new UnsupportedOperationException();
	}
}

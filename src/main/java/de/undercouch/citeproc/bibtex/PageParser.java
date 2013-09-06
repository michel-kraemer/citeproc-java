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

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import de.undercouch.citeproc.bibtex.internal.InternalPageLexer;
import de.undercouch.citeproc.bibtex.internal.InternalPageParser;
import de.undercouch.citeproc.bibtex.internal.InternalPageParser.PagesContext;

/**
 * Parses pages
 * @author Michel Kraemer
 */
public class PageParser {
	/**
	 * Parses a given page or range of pages. If the given string cannot
	 * be parsed, the method will return a page range with a literal string.
	 * @param pages the page or range of pages
	 * @return the parsed page or page range (never null)
	 */
	public static PageRange parse(String pages) {
		ANTLRInputStream is = new ANTLRInputStream(pages);
		InternalPageLexer lexer = new InternalPageLexer(is);
		lexer.removeErrorListeners(); //do not output errors to console
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		InternalPageParser parser = new InternalPageParser(tokens);
		parser.removeErrorListeners(); //do not output errors to console
		PagesContext ctx = parser.pages();
		return new PageRange(ctx.literal != null ? ctx.literal : pages, ctx.pageFrom, ctx.numberOfPages);
	}
}

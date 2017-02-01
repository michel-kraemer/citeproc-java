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

package de.undercouch.citeproc.tool;

import java.io.PrintWriter;
import java.util.List;

import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.output.Citation;

/**
 * CLI command that generates citations which can be inserted into the text
 * @author Michel Kraemer
 */
public class CitationCommand extends BibliographyCommand {
	@Override
	public String getUsageName() {
		return "citation";
	}
	
	@Override
	public String getUsageDescription() {
		return "Generate citations from an input file";
	}
	
	@Override
	protected void doGenerateCSL(CSL citeproc, String[] citationIds, PrintWriter out) {
		List<Citation> cits = citeproc.makeCitation(citationIds);
		for (Citation c : cits) {
			out.println(c.getText());
		}
	}
}

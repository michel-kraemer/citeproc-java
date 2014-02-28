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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import de.undercouch.citeproc.bibtex.AuxFile;
import de.undercouch.citeproc.bibtex.AuxFileParser;
import de.undercouch.citeproc.helper.tool.InputReader;
import de.undercouch.citeproc.helper.tool.OptionDesc;
import de.undercouch.citeproc.helper.tool.OptionParserException;
import de.undercouch.citeproc.helper.tool.UnknownAttributes;

/**
 * Generates LaTeX bibliographies
 * @author Michel Kraemer
 */
public class BibTeXCommand extends AbstractCSLToolCommand {
	private boolean simpleMode;
	private List<String> auxfiles;
	
	/**
	 * Sets the aux files to process. Since there is usually
	 * only one output file the command accepts one aux file only.
	 * This will be checked in {@link #checkArguments()}
	 * @param auxfiles the aux files (should only contain one file)
	 */
	@UnknownAttributes
	public void setAuxFiles(List<String> auxfiles) {
		this.auxfiles = auxfiles;
	}
	
	/**
	 * Enables simple mode
	 * @param simpleMode true if simple mode should be enabled, false otherwise
	 */
	@OptionDesc(longName = "simple", shortName = "s",
			description = "enable simple mode")
	public void setSimpleMode(boolean simpleMode) {
		this.simpleMode = simpleMode;
	}
	
	@Override
	public String getUsageDescription() {
		return "Generate a LaTeX bibliography";
	}

	@Override
	public String getUsageArguments() {
		return "bibtex [AUXFILE]";
	}

	@Override
	public boolean checkArguments() {
		if (auxfiles == null || auxfiles.isEmpty()) {
			error("no auxiliary file specified");
			return false;
		}
		if (auxfiles.size() > 1) {
			error("too many auxiliary files specified");
			return false;
		}
		return super.checkArguments();
	}

	@Override
	public int doRun(String[] remainingArgs, InputReader in, PrintWriter out)
			throws OptionParserException, IOException {
		//currently only simple mode is implemented
		simpleMode = true;
		
		if (simpleMode) {
			BibliographyCommand bc = new BibliographyCommand();
			
			AuxFile af;
			try {
				af= AuxFileParser.parse(auxfiles.get(0));
			} catch (FileNotFoundException e) {
				error("auxiliary file does not exist. Please run latex first.");
				return 1;
			}
			
			if (af.getStyle() != null) {
				bc.setStyle(af.getStyle());
			}
			
			if (af.getInput() == null) {
				error("auxiliary file does not specify an input bibliography");
				return 1;
			}
			String input = af.getInput();
			if (!input.toLowerCase().endsWith(".bib")) {
				input = input + ".bib";
			}
			
			bc.setFormat("latexbbl");
			bc.setCitationIds(af.getCitations());
			
			InputFileCommand ifc = new InputFileCommand(bc);
			ifc.setInput(input);
			
			return ifc.run(remainingArgs, in, out);
		}
		
		return 1;
	}
}

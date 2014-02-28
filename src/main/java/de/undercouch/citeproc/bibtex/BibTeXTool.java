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
import java.io.PrintWriter;

import de.undercouch.citeproc.CSLTool;
import de.undercouch.citeproc.helper.tool.StandardInputReader;
import de.undercouch.citeproc.helper.tool.OptionBuilder;
import de.undercouch.citeproc.helper.tool.OptionGroup;
import de.undercouch.citeproc.helper.tool.OptionParser;
import de.undercouch.citeproc.helper.tool.OptionParserException;
import de.undercouch.citeproc.helper.tool.Value;
import de.undercouch.citeproc.tool.CSLToolContext;

/**
 * Command line tool that allows citeproc-java to be used as an alternative
 * to the bibtex command. This class calls {@link de.undercouch.citeproc.CSLTool}
 * internally.
 * @author Michel Kraemer
 */
public class BibTeXTool {
	/**
	 * Option identifiers
	 */
	private static enum OID {
		HELP,
		VERSION,
		AUXFILE
	}
	
	/**
	 * A list of possible command line options for this tool
	 */
	private static OptionGroup<OID> options = new OptionBuilder<OID>()
		.add(OID.HELP, "help", "h", "display this help and exit")
		.add(OID.VERSION, "version", "V", "output version information and exit")
		.build();
	
	/**
	 * The main method of the CSL tool. Use <code>citeproc-java-bibtex --help</code>
	 * for more information.
	 * @param args the command line
	 * @throws IOException if a stream could not be read
	 */
	public static void main(String[] args) throws IOException {
		CSLToolContext ctx = CSLToolContext.enter();
		try {
			ctx.setToolName("citeproc-java-bibtex");
			BibTeXTool tool = new BibTeXTool();
			int exitCode;
			try {
				exitCode = tool.run(args);
			} catch (OptionParserException e) {
				System.err.println("citeproc-java-bibtex: " + e.getMessage());
				exitCode = 1;
			}
			if (exitCode != 0) {
				System.exit(exitCode);
			}
		} finally {
			CSLToolContext.exit();
		}
	}
	
	/**
	 * The main method of the BibTeX tool. Use <code>citeproc-java-bibtex --help</code>
	 * for more information.
	 * @param args the command line
	 * @return the application's exit code
	 * @throws OptionParserException if the arguments could not be parsed
	 * @throws IOException if a stream could not be read
	 */
	public int run(String[] args) throws OptionParserException, IOException {
		//parse command line
		OptionParser.Result<OID> parsedOptions;
		try {
			parsedOptions = OptionParser.parse(args, options, OID.AUXFILE);
		} catch (OptionParserException e) {
			System.err.println(CSLToolContext.current().getToolName() +
					": " + e.getMessage());
			return 1;
		}
		
		//we need exactly one AUXFILE or one option
		if (parsedOptions.getValues().size() != 1) {
			usage();
			return 0;
		}
		
		String auxfile = null;
		
		CSLTool cslTool = new CSLTool();
		
		for (Value<OID> v : parsedOptions.getValues()) {
			switch (v.getId()) {
			case HELP:
				usage();
				return 0;
			
			case VERSION:
				cslTool.run(new String[] { "-V" }, new StandardInputReader(),
						new PrintWriter(System.out));
				return 0;
			
			case AUXFILE:
				auxfile = v.getValue().toString();
				break;
			}
		}
		
		String bblfile = auxfile;
		if (bblfile.toLowerCase().endsWith(".tex") || bblfile.toLowerCase().endsWith(".aux")) {
			bblfile = bblfile.substring(0, bblfile.length() - 4);
		}
		bblfile = bblfile + ".bbl";
		
		return cslTool.run(new String[] { "-o", bblfile, "bibtex",
				"--simple", auxfile }, new StandardInputReader(),
				new PrintWriter(System.out));
	}
	
	/**
	 * Prints out usage information
	 */
	private static void usage() {
		OptionParser.usage(CSLToolContext.current().getToolName() + " AUXFILE",
				"Generate bibliographies for LaTeX documents", options, System.out);
	}
}

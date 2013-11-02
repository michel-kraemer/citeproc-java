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
import java.util.List;

import de.undercouch.citeproc.CSLTool;
import de.undercouch.citeproc.helper.tool.OptionBuilder;
import de.undercouch.citeproc.helper.tool.OptionGroup;
import de.undercouch.citeproc.helper.tool.OptionParser;
import de.undercouch.citeproc.helper.tool.OptionParserException;
import de.undercouch.citeproc.helper.tool.Value;

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
		BibTeXTool tool = new BibTeXTool();
		int exitCode = tool.run(args);
		if (exitCode != 0) {
			System.exit(exitCode);
		}
	}
	
	/**
	 * The main method of the BibTeX tool. Use <code>citeproc-java-bibtex --help</code>
	 * for more information.
	 * @param args the command line
	 * @return the application's exit code
	 * @throws IOException if a stream could not be read
	 */
	public int run(String[] args) throws IOException {
		//parse command line
		List<Value<OID>> values;
		try {
			values = OptionParser.parse(args, options, OID.AUXFILE);
		} catch (OptionParserException e) {
			System.err.println("citeproc-java-bibtex: " + e.getMessage());
			return 1;
		}
		
		//we need exactly one AUXFILE or one option
		if (values.size() != 1) {
			usage();
			return 0;
		}
		
		String auxfile = null;
		
		for (Value<OID> v : values) {
			switch (v.getId()) {
			case HELP:
				usage();
				return 0;
			
			case VERSION:
				CSLTool.version("citeproc-java-bibtex");
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
		
		CSLTool cslTool = new CSLTool();
		return cslTool.run(new String[] { "--bibtex-simple", auxfile, "-o", bblfile });
	}
	
	/**
	 * Prints out usage information
	 */
	private static void usage() {
		OptionParser.usage("citeproc-java-bibtex AUXFILE",
				"Generate bibliographies for LaTeX documents", options, System.out);
	}
}

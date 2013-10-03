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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jbibtex.BibTeXDatabase;
import org.jbibtex.ParseException;

import de.undercouch.citeproc.bibtex.BibTeXConverter;
import de.undercouch.citeproc.bibtex.BibTeXItemDataProvider;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.helper.CSLUtils;
import de.undercouch.citeproc.helper.Levenshtein;
import de.undercouch.citeproc.helper.json.JsonBuilder;
import de.undercouch.citeproc.helper.json.StringJsonBuilderFactory;
import de.undercouch.citeproc.helper.tool.Option;
import de.undercouch.citeproc.helper.tool.Option.ArgumentType;
import de.undercouch.citeproc.helper.tool.OptionBuilder;
import de.undercouch.citeproc.helper.tool.OptionParser;
import de.undercouch.citeproc.helper.tool.OptionParserException;
import de.undercouch.citeproc.helper.tool.Value;
import de.undercouch.citeproc.output.Bibliography;
import de.undercouch.citeproc.output.Citation;

/**
 * Command line tool for the CSL processor. Use <code>citeproc-java --help</code>
 * for more information.
 * @author Michel Kraemer
 */
public class CSLTool {
	/**
	 * Option identifiers
	 */
	private static enum OID {
		BIBLIOGRAPHY,
		STYLE,
		LOCALE,
		FORMAT,
		CITATION,
		HELP,
		VERSION,
		CITATIONID
	}
	
	/**
	 * A list of possible command line options for this tool
	 */
	private static List<Option<OID>> options = new OptionBuilder<OID>()
		.add(OID.BIBLIOGRAPHY, "bibliography", "b", "input bibliography FILE (*.bib)",
				"FILE", ArgumentType.STRING)
		.add(OID.STYLE, "style", "s", "citation STYLE name (default: ieee)",
				"STYLE", ArgumentType.STRING)
		.add(OID.LOCALE, "locale", "l", "citation LOCALE (default: en-US)",
				"LOCALE", ArgumentType.STRING)
		.add(OID.FORMAT, "format", "f", "output format: text (default), html, asciidoc, fo, rtf",
				"FORMAT", ArgumentType.STRING)
		.add(OID.CITATION, "citation", "c", "generate citations and not a bibliography")
		.add(OID.HELP, "help", "h", "display this help and exit")
		.add(OID.VERSION, "version", "v", "output version information and exit")
		.build();
	
	/**
	 * The main method of the CSL tool. Use <code>citeproc-java --help</code>
	 * for more information.
	 * @param args the command line
	 * @throws IOException if a stream could not be read
	 */
	public static void main(String[] args) throws IOException {
		CSLTool tool = new CSLTool();
		int exitCode = tool.run(args);
		if (exitCode != 0) {
			System.exit(exitCode);
		}
	}
	
	/**
	 * The main method of the CSL tool. Use <code>citeproc-java --help</code>
	 * for more information.
	 * @param args the command line
	 * @return the application's exit code
	 * @throws IOException if a stream could not be read
	 */
	public int run(String[] args) throws IOException {
		//parse command line
		List<Value<OID>> values;
		try {
			values = OptionParser.parse(args, options, OID.CITATIONID);
		} catch (OptionParserException e) {
			System.err.println("citeproc-java: " + e.getMessage());
			return 1;
		}
		
		//if there are no values print usage and exit
		if (values.isEmpty()) {
			usage();
			return 0;
		}
		
		//evaluate option values
		String bibliography = null;
		String style = "ieee";
		String locale = "en-US";
		String format = "text";
		boolean citation = false;
		List<String> citationIds = new ArrayList<String>();
		
		for (Value<OID> v : values) {
			switch (v.getId()) {
			case BIBLIOGRAPHY:
				bibliography = v.getValue().toString();
				break;
				
			case STYLE:
				style = v.getValue().toString();
				break;
			
			case LOCALE:
				locale = v.getValue().toString();
				break;
			
			case FORMAT:
				format = v.getValue().toString();
				break;
			
			case CITATION:
				citation = true;
				break;
			
			case HELP:
				usage();
				return 0;
			
			case VERSION:
				version();
				return 0;
			
			case CITATIONID:
				citationIds.add(v.getValue().toString());
				break;
			}
		}
		
		//check if there is a bibliography file
		if (bibliography == null) {
			System.err.println("citeproc-java: no input bibliography specified.");
			return 1;
		}
		
		//check output format
		if (!format.equals("text") && !format.equals("html") &&
				!format.equals("asciidoc") && !format.equals("fo") &&
				!format.equals("rtf")) {
			System.err.println("citeproc-java: invalid output format: " + format);
			return 1;
		}
		
		//load bibliography file
		FileInputStream fis = new FileInputStream(new File(bibliography));
		BibTeXDatabase db;
		try {
			db = new BibTeXConverter().loadDatabase(fis);
		} catch (ParseException e) {
			System.err.println("citeproc-java: could not parse bibliography file.");
			System.err.println(e.getMessage());
			return 1;
		} finally {
			fis.close();
		}
		
		BibTeXItemDataProvider provider = new BibTeXItemDataProvider();
		provider.addDatabase(db);
		
		//check provided citation ids
		if (citation && citationIds.isEmpty()) {
			System.err.println("citeproc-java: no citation id specified.");
			return 1;
		}
		for (String id : citationIds) {
			if (provider.retrieveItem(id) == null) {
				System.err.println("citeproc-java: unknown citation id: " + id);
				String min = Levenshtein.findMinimum(Arrays.asList(provider.getIds()), id);
				System.err.println("Did you mean `" + min + "'?");
				return 1;
			}
		}
		
		//run conversion
		int ret;
		if (style.equals("json")) {
			ret = generateJSON(citation, citationIds, provider);
		} else {
			ret = generateCSL(style, locale, format, citation, citationIds, provider);
		}
		
		return ret;
	}
	
	/**
	 * Generates JSON
	 * @param citation true if an array of citation ids should be generated
	 * @param citationIds the citation ids given on the command line
	 * @param provider a provider containing all citation item data
	 * @return the exit code
	 */
	private int generateJSON(boolean citation, List<String> citationIds,
			ItemDataProvider provider) {
		StringJsonBuilderFactory factory = new StringJsonBuilderFactory();
		if (citation) {
			//create an array of citation ids
			JsonBuilder b = factory.createJsonBuilder();
			String s = (String)b.toJson(citationIds.toArray(new String[citationIds.size()]));
			System.out.println(s);
		} else {
			//create an array of citation item data objects (either for
			//the whole bibliography or for the given citation ids only)
			System.out.print("[");
			List<String> ids = citationIds;
			if (ids.isEmpty()) {
				ids = Arrays.asList(provider.getIds());
			}
			
			int i = 0;
			for (String id : ids) {
				if (i > 0) {
					System.out.print(",");
				}
				CSLItemData item = provider.retrieveItem(id);
				JsonBuilder b = factory.createJsonBuilder();
				System.out.print(item.toJson(b));
				++i;
			}
			
			System.out.println("]");
		}
		return 0;
	}
	
	/**
	 * Performs CSL conversion and generates citations or a bibliography
	 * @param style the CSL style
	 * @param locale the CSL locale
	 * @param format the output format
	 * @param citation true if citations should be created instead of
	 * a bibliography
	 * @param citationIds the citation ids given on the command line
	 * @param provider a provider containing all citation item data
	 * @return the exit code
	 * @throws IOException if the CSL processor could not be initialized
	 */
	private int generateCSL(String style, String locale, String format,
			boolean citation, List<String> citationIds,
			ItemDataProvider provider) throws IOException {
		//initialize citation processor
		CSL citeproc;
		try {
			citeproc = new CSL(provider, style, locale);
		} catch (FileNotFoundException e) {
			System.err.println("citeproc-java: " + e.getMessage());
			return 1;
		}
		
		//set output format
		citeproc.setOutputFormat(format);
		
		//register citation items
		String[] citationIdsArr = new String[citationIds.size()];
		citationIdsArr = citationIds.toArray(citationIdsArr);
		if (citationIds.isEmpty()) {
			citeproc.registerCitationItems(provider.getIds());
		} else {
			citeproc.registerCitationItems(citationIdsArr);
		}
		
		//generate citation(s) or bibliography
		if (citation) {
			List<Citation> cits = citeproc.makeCitation(citationIdsArr);
			for (Citation c : cits) {
				System.out.println(c.getText());
			}
		} else {
			Bibliography bibl = citeproc.makeBibliography();
			System.out.println(bibl.makeString());
		}
		
		return 0;
	}
	
	/**
	 * Prints out version information
	 */
	private void version() {
		URL u = CSLTool.class.getResource("version.dat");
		String version;
		try {
			version = CSLUtils.readURLToString(u, "UTF-8");
		} catch (IOException e) {
			throw new RuntimeException("Could not read version information", e);
		}
		System.out.println("citeproc-java " + version);
	}
	
	/**
	 * Prints out usage information
	 */
	private void usage() {
		OptionParser.usage("citeproc-java [OPTION]... [CITATION ID]...",
				"Generate styled citations and bibliographies", options, System.out);
	}
}

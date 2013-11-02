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

import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.jbibtex.BibTeXDatabase;
import org.jbibtex.ParseException;

import de.undercouch.citeproc.bibtex.AuxFile;
import de.undercouch.citeproc.bibtex.AuxFileParser;
import de.undercouch.citeproc.bibtex.BibTeXConverter;
import de.undercouch.citeproc.bibtex.BibTeXItemDataProvider;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.helper.CSLUtils;
import de.undercouch.citeproc.helper.Levenshtein;
import de.undercouch.citeproc.helper.json.JsonBuilder;
import de.undercouch.citeproc.helper.json.JsonLexer;
import de.undercouch.citeproc.helper.json.JsonParser;
import de.undercouch.citeproc.helper.json.StringJsonBuilderFactory;
import de.undercouch.citeproc.helper.oauth.AuthenticationStore;
import de.undercouch.citeproc.helper.oauth.FileAuthenticationStore;
import de.undercouch.citeproc.helper.oauth.RequestException;
import de.undercouch.citeproc.helper.oauth.UnauthorizedException;
import de.undercouch.citeproc.helper.tool.Option.ArgumentType;
import de.undercouch.citeproc.helper.tool.OptionBuilder;
import de.undercouch.citeproc.helper.tool.OptionGroup;
import de.undercouch.citeproc.helper.tool.OptionParser;
import de.undercouch.citeproc.helper.tool.OptionParserException;
import de.undercouch.citeproc.helper.tool.Value;
import de.undercouch.citeproc.helper.tool.internal.CachingMendeleyConnector;
import de.undercouch.citeproc.mendeley.AuthenticatedMendeleyConnector;
import de.undercouch.citeproc.mendeley.DefaultMendeleyConnector;
import de.undercouch.citeproc.mendeley.MendeleyConnector;
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
		MENDELEY,
		MENDELEY_SYNC,
		BIBTEX_SIMPLE,
		STYLE,
		LOCALE,
		FORMAT,
		CITATION,
		LIST,
		OUT,
		HELP,
		VERSION,
		CITATIONID
	}
	
	/**
	 * File formats for bibliography files
	 */
	private static enum FileFormat {
		BIBTEX,
		JSON_OBJECT,
		JSON_ARRAY,
		UNKNOWN
	}
	
	/**
	 * A list of possible command line options for this tool
	 */
	private static OptionGroup<OID> options = new OptionBuilder<OID>()
		.add(OID.BIBLIOGRAPHY, "bibliography", "b", "input bibliography FILE (*.bib, *.json)",
				"FILE", ArgumentType.STRING)
		.add(OID.STYLE, "style", "s", "citation STYLE name (default: ieee)",
				"STYLE", ArgumentType.STRING)
		.add(OID.LOCALE, "locale", "l", "citation LOCALE (default: en-US)",
				"LOCALE", ArgumentType.STRING)
		.add(OID.FORMAT, "format", "f", "output format: text (default), html, asciidoc, fo, rtf",
				"FORMAT", ArgumentType.STRING)
		.add(OID.CITATION, "citation", "c", "generate citations and not a bibliography")
		.add(OID.LIST, "list", "display sorted list of available citation IDs")
		.add(OID.OUT, "output", "o", "write output to FILE instead of stdout",
				"FILE", ArgumentType.STRING)
		.add(new OptionBuilder<OID>("Mendeley:")
				.add(OID.MENDELEY, "mendeley", "read input bibliography from Mendeley Web")
				.add(OID.MENDELEY_SYNC, "mendeley-sync", "synchronize with Mendeley Web, "
						+ "implies --mendeley")
				.build()
		)
		.add(new OptionBuilder<OID>("BibTeX:")
				.add(OID.BIBTEX_SIMPLE, "bibtex-simple", null,
						"generate a LaTeX bibliography from an AUXFILE",
						"AUXFILE", ArgumentType.STRING)
				.build()
		)
		.add(new OptionBuilder<OID>("Miscellaneous:")
				.add(OID.HELP, "help", "h", "display this help and exit")
				.add(OID.VERSION, "version", "V", "output version information and exit")
				.build()
		)
		.build();
	
	/**
	 * Path to the tool's configuration directory
	 */
	private File configDir;
	
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
		configDir = new File(System.getProperty("user.home"), ".citeproc-java");
		configDir.mkdirs();
		
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
		boolean mendeley = false;
		boolean mendeleySync = false;
		boolean bibtex = false;
		String style = "ieee";
		String locale = "en-US";
		String format = "text";
		boolean citation = false;
		boolean list = false;
		List<String> citationIds = new ArrayList<String>();
		String outputFile = null;
		
		for (Value<OID> v : values) {
			switch (v.getId()) {
			case BIBLIOGRAPHY:
				bibliography = v.getValue().toString();
				break;
			
			case MENDELEY:
				mendeley = true;
				break;
			
			case MENDELEY_SYNC:
				mendeley = true;
				mendeleySync = true;
				break;
			
			case BIBTEX_SIMPLE:
				bibtex = true;
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
			
			case LIST:
				list = true;
				break;
			
			case OUT:
				outputFile = v.getValue().toString();
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
		if (bibliography == null && !mendeley) {
			System.err.println("citeproc-java: no input bibliography specified.");
			return 1;
		}
		
		if (bibliography != null && mendeley) {
			System.err.println("citeproc-java: You can either specify an "
					+ "input bibliography file or let the tool read it from "
					+ "the Mendeley server, but not both.");
			return 1;
		}
		
		//check output format
		if (!format.equals("text") && !format.equals("html") &&
				!format.equals("asciidoc") && !format.equals("fo") &&
				!format.equals("rtf") && !format.equals("latexbbl")) {
			System.err.println("citeproc-java: invalid output format: " + format);
			return 1;
		}
		
		//override output format if we're using bibtex mode
		if (bibtex) {
			format = "latexbbl";
		}
		
		//load input bibliography
		ItemDataProvider provider;
		if (bibliography != null) {
			if (bibtex) {
				//handle LaTeX auxiliary file
				AuxFile af= AuxFileParser.parse(bibliography);
				if (af.getStyle() != null) {
					style = af.getStyle();
				}
				if (af.getInput() == null) {
					System.err.println("citeproc-java: aux file does not "
							+ "specify an input bibliography.");
					return 1;
				}
				bibliography = af.getInput();
				if (!bibliography.toLowerCase().endsWith(".bib")) {
					bibliography = bibliography + ".bib";
				}
				citationIds = af.getCitations();
			}
			
			provider = readBibliographyFile(bibliography);
		} else {
			provider = readMendeley(mendeleySync);
		}
		if (provider == null) {
			return 1;
		}
		
		if (list) {
			//list available citation ids and exit
			List<String> ids = new ArrayList<String>(Arrays.asList(provider.getIds()));
			Collections.sort(ids);
			for (String id : ids) {
				System.out.println(id);
			}
			return 0;
		}
		
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
		
		//prepare output
		PrintStream out = System.out;
		if (outputFile != null) {
			out = new PrintStream(outputFile);
		}
		
		//run conversion
		int ret;
		try {
			if (style.equals("json")) {
				ret = generateJSON(citation, citationIds, provider, out);
			} else {
				ret = generateCSL(style, locale, format, citation, citationIds, provider, out);
			}
			out.flush();
		} finally {
			if (outputFile != null) {
				out.close();
			}
		}
		
		return ret;
	}
	
	/**
	 * Reads all items from an input bibliography file and returns a provider
	 * serving these items
	 * @param bibliography the input file
	 * @return the provider
	 * @throws FileNotFoundException if the input file was not found
	 * @throws IOException if the input file could not be read
	 */
	private ItemDataProvider readBibliographyFile(String bibliography)
			throws FileNotFoundException, IOException {
		//open buffered input stream to bibliography file
		File bibfile = new File(bibliography);
		if (!bibfile.exists()) {
			System.err.println("citeproc-java: bibliography file `" + bibliography + "' does not exist.");
			return null;
		}
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(bibfile));
		
		ItemDataProvider provider;
		try {
			//determine file format
			FileFormat ff = determineFileFormat(bis);
			
			//load bibliography file
			if (ff == FileFormat.BIBTEX) {
				BibTeXDatabase db = new BibTeXConverter().loadDatabase(bis);
				BibTeXItemDataProvider bibtexprovider = new BibTeXItemDataProvider();
				bibtexprovider.addDatabase(db);
				provider = bibtexprovider;
			} else if (ff == FileFormat.JSON_ARRAY || ff == FileFormat.JSON_OBJECT) {
				JsonParser parser = new JsonParser(new JsonLexer(new InputStreamReader(bis)));
				List<Object> objs;
				if (ff == FileFormat.JSON_ARRAY) {
					objs = parser.parseArray();
				} else {
					objs = new ArrayList<Object>();
					objs.add(parser.parseObject());
				}
				CSLItemData[] items = new CSLItemData[objs.size()];
				for (int i = 0; i < items.length; ++i) {
					@SuppressWarnings("unchecked")
					Map<String, Object> obj = (Map<String, Object>)objs.get(i);
					items[i] = CSLItemData.fromJson(obj);
				}
				provider = new ListItemDataProvider(items);
			} else {
				System.err.println("citeproc-java: unknown bibliography file format");
				return null;
			}
		} catch (ParseException e) {
			System.err.println("citeproc-java: could not parse bibliography file.");
			System.err.println(e.getMessage());
			return null;
		} finally {
			bis.close();
		}
		
		return provider;
	}
	
	/**
	 * Reads documents from the Mendeley server
	 * @param sync true if synchronization should be forced
	 * @return an item data provider providing all documents from the
	 * Mendeley server
	 */
	private ItemDataProvider readMendeley(boolean sync) {
		//read app's consumer key and secret
		String[] consumer;
		try {
			consumer = readConsumer();
		} catch (Exception e) {
			//should never happen
			throw new RuntimeException("Could not read Mendeley consumer key and secret");
		}
		
		//use previously stored authentication
		File authStoreFile = new File(configDir, "mendeley-auth-store.conf");
		AuthenticationStore authStore;
		try {
			authStore = new FileAuthenticationStore(authStoreFile);
		} catch (IOException e) {
			System.err.println("citeproc-java: could not read user's "
					+ "authentication store: " + authStoreFile.getPath());
			return null;
		}
		
		//connect to Mendeley server
		MendeleyConnector dmc = new DefaultMendeleyConnector(consumer[1], consumer[0]);
		dmc = new AuthenticatedMendeleyConnector(dmc, authStore);
		
		//enable cache
		File cacheFile = new File(configDir, "mendeley-cache.dat");
		CachingMendeleyConnector mc = new CachingMendeleyConnector(dmc, cacheFile);
		
		//clear cache if necessary
		if (sync) {
			mc.clear();
		}

		CSLItemData[] items;
		int retries = 1;
		while (true) {
			try {
				//download list of document IDs
				boolean cacheempty = false;
				if (!mc.hasDocumentList()) {
					System.out.print("Retrieving documents ...");
					cacheempty = true;
				}
				List<String> docs = mc.getDocuments();
				if (cacheempty) {
					System.out.println();
				}
				
				//download all documents
				items = new CSLItemData[docs.size()];
				int i = 0;
				int printed = 0;
				for (String did : docs) {
					if (!mc.containsDocumentId(did)) {
						String msg = String.format("\rSynchronizing (%d/%d) ...",
								i + 1, docs.size());
						System.out.print(msg);
						++printed;
					}
					
					CSLItemData item = mc.getDocument(did);
					items[i] = item;
					++i;
				}
				
				if (printed > 0) {
					System.out.println();
				}
			} catch (UnauthorizedException e) {
				if (retries == 0) {
					System.err.println("citeproc-java: failed to authorize.");
					return null;
				}
				--retries;
				
				//app is not authenticated yet
				if (!mendeleyAuthorize(mc)) {
					return null;
				}
				
				continue;
			} catch (RequestException e) {
				System.err.println("citeproc-java: " + e.getMessage());
				return null;
			} catch (IOException e) {
				System.err.println("citeproc-java: could not get list of "
						+ "documents from Mendeley server.");
				return null;
			}
			
			break;
		}
		
		//return provider that contains all items from the server
		return new ListItemDataProvider(items);
	}
	
	/**
	 * Request authorization for the tool from the Mendeley server
	 * @param mc the Mendeley connector
	 * @return true if authorization was successful
	 */
	private boolean mendeleyAuthorize(MendeleyConnector mc) {
		//get authorization URL
		String authUrl;
		try {
			authUrl = mc.getAuthorizationURL();
		} catch (IOException e) {
			System.err.println("citeproc-java: could not get authorization "
					+ "URL from Mendeley server.");
			return false;
		}
		
		//ask user to point browser to authorization URL
		System.out.println("This tool requires authorization. Please point your "
				+ "web browser to the\nfollowing URL and follow the instructions:\n");
		System.out.println(authUrl);
		System.out.println();
		
		//open authorization tool in browser
		if (Desktop.isDesktopSupported()) {
			Desktop d = Desktop.getDesktop();
			if (d.isSupported(Desktop.Action.BROWSE)) {
				try {
					d.browse(new URI(authUrl));
				} catch (Exception e) {
					//ignore. let the user open the browser manually.
				}
			}
		}
		
		//read verification code from console
		System.out.print("Enter verification code: ");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String verificationCode;
		try {
			verificationCode = br.readLine();
		} catch (IOException e) {
			throw new RuntimeException("Could not read from console.");
		}
		
		//authorize...
		try {
			System.out.println("Connecting ...");
			mc.authorize(verificationCode);
		} catch (IOException e) {
			System.err.println("citeproc-java: Mendeley server refused "
					+ "authorization.");
			return false;
		}
		
		return true;
	}
	
	/**
	 * Reads the Mendeley consumer key and consumer secret from an encrypted
	 * file. This is indeed not the most secure way to save these tokens, but
	 * it's certainly better than putting them unencrypted in this file.
	 * @return the key and secret
	 * @throws Exception if something goes wrong
	 */
	private String[] readConsumer() throws Exception {
		String str = CSLUtils.readStreamToString(CSLTool.class.getResourceAsStream(
				"helper/tool/internal/citeproc-java-tool-consumer"), "UTF-8");
		byte[] arr = DatatypeConverter.parseBase64Binary(str);
		
		SecretKeySpec k = new SecretKeySpec("#x$gbf5zs%4QvzAx".getBytes(), "AES");
		Cipher c = Cipher.getInstance("AES");
		c.init(Cipher.DECRYPT_MODE, k);
		arr = c.doFinal(arr);
		arr = DatatypeConverter.parseBase64Binary(new String(arr));
		
		String[] result = new String[] { "", "" };
		for (int i = 0; i < 32; ++i) {
			result[0] += (char)arr[i + 31];
		}
		for (int i = 0; i < 41; ++i) {
			result[1] += (char)arr[i + 1857];
		}
		
		return result;
	}
	
	/**
	 * Generates JSON
	 * @param citation true if an array of citation ids should be generated
	 * @param citationIds the citation ids given on the command line
	 * @param provider a provider containing all citation item data
	 * @param out the print stream to write the output to
	 * @return the exit code
	 */
	private int generateJSON(boolean citation, List<String> citationIds,
			ItemDataProvider provider, PrintStream out) {
		StringJsonBuilderFactory factory = new StringJsonBuilderFactory();
		if (citation) {
			//create an array of citation ids
			JsonBuilder b = factory.createJsonBuilder();
			String s = (String)b.toJson(citationIds.toArray(new String[citationIds.size()]));
			out.println(s);
		} else {
			//create an array of citation item data objects (either for
			//the whole bibliography or for the given citation ids only)
			out.print("[");
			List<String> ids = citationIds;
			if (ids.isEmpty()) {
				ids = Arrays.asList(provider.getIds());
			}
			
			int i = 0;
			for (String id : ids) {
				if (i > 0) {
					out.print(",");
				}
				CSLItemData item = provider.retrieveItem(id);
				JsonBuilder b = factory.createJsonBuilder();
				out.print(item.toJson(b));
				++i;
			}
			
			out.println("]");
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
	 * @param out the print stream to write the output to
	 * @return the exit code
	 * @throws IOException if the CSL processor could not be initialized
	 */
	private int generateCSL(String style, String locale, String format,
			boolean citation, List<String> citationIds,
			ItemDataProvider provider, PrintStream out) throws IOException {
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
				out.println(c.getText());
			}
		} else {
			Bibliography bibl = citeproc.makeBibliography();
			out.println(bibl.makeString());
		}
		
		return 0;
	}
	
	/**
	 * Prints out version information
	 */
	private void version() {
		version("citeproc-java");
	}
	
	/**
	 * Prints out version information for a given application
	 * @param applicationName the application's name
	 */
	public static void version(String applicationName) {
		URL u = CSLTool.class.getResource("version.dat");
		String version;
		try {
			version = CSLUtils.readURLToString(u, "UTF-8");
		} catch (IOException e) {
			throw new RuntimeException("Could not read version information", e);
		}
		System.out.println(applicationName + " " + version);
	}
	
	/**
	 * Prints out usage information
	 */
	private void usage() {
		OptionParser.usage("citeproc-java [OPTION]... [CITATION ID]...",
				"Generate styled citations and bibliographies", options, System.out);
	}
	
	/**
	 * Checks the first 100 KB of the given input stream and tries to
	 * determine the file format. Resets the input stream to the position
	 * it had when the method was called.
	 * @param bis the input stream
	 * @return the file format
	 * @throws IOException if the input stream could not be read
	 */
	private FileFormat determineFileFormat(BufferedInputStream bis) throws IOException {
		int len = 1024 * 100;
		bis.mark(len);
		try {
			while (true) {
				int c = bis.read();
				--len;
				if (c < 0) {
					return FileFormat.UNKNOWN;
				}
				if (len < 2) {
					return FileFormat.UNKNOWN;
				}
				if (!Character.isWhitespace(c)) {
					if (c == '%' || c == '@') {
						return FileFormat.BIBTEX;
					} else if (c == '[') {
						return FileFormat.JSON_ARRAY;
					} else if (c == '{') {
						return FileFormat.JSON_OBJECT;
					}
					return FileFormat.UNKNOWN;
				}
			}
		} finally {
			bis.reset();
		}
	}
}

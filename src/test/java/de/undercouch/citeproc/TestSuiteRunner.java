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
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import de.undercouch.citeproc.csl.CSLAbbreviationList;
import de.undercouch.citeproc.csl.CSLCitation;
import de.undercouch.citeproc.csl.CSLCitationItem;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CitationIDIndexPair;
import de.undercouch.citeproc.helper.json.JsonLexer;
import de.undercouch.citeproc.helper.json.JsonParser;
import de.undercouch.citeproc.output.Bibliography;
import de.undercouch.citeproc.output.Citation;
import de.undercouch.citeproc.script.ScriptRunnerException;

/**
 * Runs the CSL test suite (<a href="https://bitbucket.org/bdarcus/citeproc-test">https://bitbucket.org/bdarcus/citeproc-test</a>)
 * @author Michel Kraemer
 */
public class TestSuiteRunner {
	/**
	 * Main method of the test runner
	 * @param args the first argument can either be a compiled test file (.json)
	 * to run or a directory containing compiled test files
	 * @throws IOException if a file could not be loaded
	 */
	public static void main(String[] args) throws IOException {
		TestSuiteRunner runner = new TestSuiteRunner();
		runner.runTests(new File(args[0]));
	}
	
	/**
	 * Runs tests
	 * @param f either a compiled test file (.json) to run or a directory
	 * containing compiled test files
	 * @throws IOException if a file could not be loaded
	 */
	public void runTests(File f) throws IOException {
		//find test files
		File[] testFiles;
		if (f.isDirectory()) {
			testFiles = f.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".json");
				}
			});
		} else {
			testFiles = new File[] { f };
		}
		
		AnsiConsole.systemInstall();
		try {
			long start = System.currentTimeMillis();
			int count = testFiles.length;
			int success = 0;
			
			//run each test file
			for (File fi : testFiles) {
				//output name
				String name = fi.getName().substring(0, fi.getName().length() - 5);
				System.out.print(name);
				for (int i = 0; i < (79 - name.length() - 9); ++i) {
					System.out.print(" ");
				}
				
				try {
					runTest(fi);
					System.out.println("[" + Ansi.ansi().fg(Ansi.Color.GREEN)
							.a("SUCCESS").reset() + "]");
					++success;
				} catch (IllegalStateException e) {
					System.out.println("[" + Ansi.ansi().fg(Ansi.Color.RED)
							.a("FAILURE").reset() + "]");
					System.err.println(e.getMessage());
				}
			}
			
			//output total time
			long end = System.currentTimeMillis();
			double time = (end - start) / 1000.0;
			System.out.println("Successfully executed " + success + " of " + count + " tests.");
			System.out.println(String.format(Locale.ENGLISH, "Total time: %.3f secs", time));
		} finally {
			AnsiConsole.systemUninstall();
		}
	}
	
	/**
	 * Runs a single test file
	 * @param f the test file to run
	 * @throws IOException if the file could not be loaded
	 */
	@SuppressWarnings("unchecked")
	private void runTest(File f) throws IOException {
		//load file
		Map<String, Object> conf = readFile(f);
		
		//get configuration
		String mode = (String)conf.get("mode");
		String result = (String)conf.get("result");
		String style = (String)conf.get("csl");
		Collection<Map<String, Object>> input =
				(Collection<Map<String, Object>>)conf.get("input");
		Collection<List<Object>> rawCitations =
				(Collection<List<Object>>)conf.get("citations");
		Collection<List<Map<String, Object>>> rawCitationItems =
				(Collection<List<Map<String, Object>>>)conf.get("citation_items");
		Map<String, Map<String, Object>> abbreviations =
				(Map<String, Map<String, Object>>)conf.get("abbreviations");
		Collection<List<String>> bibentries =
				(Collection<List<String>>)conf.get("bibentries");
		Map<String, Collection<Map<String, Object>>> rawBibsection =
				(Map<String, Collection<Map<String, Object>>>)conf.get("bibsection");
		
		//convert item data
		int i = 0;
		CSLItemData[] items = new CSLItemData[input.size()];
		for (Map<String, Object> m : input) {
			items[i++] = CSLItemData.fromJson(m);
		}
		
		//convert citations
		List<List<Object>> citations = null;
		if (rawCitations != null) {
			citations = new ArrayList<List<Object>>();
			for (List<Object> m : rawCitations) {
				List<Object> cits = new ArrayList<Object>();
				cits.add(CSLCitation.fromJson((Map<String, Object>)m.get(0)));
				
				Collection<List<Object>> coll1 = (Collection<List<Object>>)m.get(1);
				Collection<List<Object>> coll2 = (Collection<List<Object>>)m.get(2);
				List<CitationIDIndexPair> citsPre = new ArrayList<CitationIDIndexPair>();
				List<CitationIDIndexPair> citsPost = new ArrayList<CitationIDIndexPair>();
				for (List<Object> c1m : coll1) {
					citsPre.add(CitationIDIndexPair.fromJson(c1m));
				}
				for (List<Object> c2m : coll2) {
					citsPost.add(CitationIDIndexPair.fromJson(c2m));
				}
				
				cits.add(citsPre);
				cits.add(citsPost);
				citations.add(cits);
			}
		}
		
		//convert citation items
		List<List<CSLCitationItem>> citationItems = null;
		if (rawCitationItems != null) {
			citationItems = new ArrayList<List<CSLCitationItem>>();
			for (List<Map<String, Object>> l : rawCitationItems) {
				List<CSLCitationItem> cits = new ArrayList<CSLCitationItem>();
				for (Map<String, Object> m : l) {
					cits.add(CSLCitationItem.fromJson(m));
				}
				citationItems.add(cits);
			}
		}
		
		//convert abbreviations
		DefaultAbbreviationProvider abbreviationProvider = new DefaultAbbreviationProvider();
		if (abbreviations != null) {
			for (Map.Entry<String, Map<String, Object>> e : abbreviations.entrySet()) {
				CSLAbbreviationList al = CSLAbbreviationList.fromJson(e.getValue());
				abbreviationProvider.add(e.getKey(), al);
			}
		}
		
		//convert the 'bibsection' configuration
		SelectionMode bibSectionMode = null;
		CSLItemData[] bibSection = null;
		CSLItemData[] bibSectionQuash = null;
		if (rawBibsection != null) {
			for (Map.Entry<String, Collection<Map<String, Object>>> e : rawBibsection.entrySet()) {
				CSLItemData[] r = convertBibSection(e.getValue());
				if (e.getKey().equals("quash")) {
					bibSectionQuash = r;
				} else {
					bibSection = r;
					bibSectionMode = SelectionMode.fromString(e.getKey());
				}
			}
		}
		
		//create CSL processor
		ListItemDataProvider itemDataProvider = new ListItemDataProvider(items);
		TestSuiteCSL citeproc = new TestSuiteCSL(itemDataProvider, abbreviationProvider, style);
		
		//register citation items
		boolean nosort = mode.equals("bibliography-nosort");
		if (bibentries != null) {
			for (List<String> be : bibentries) {
				citeproc.registerCitationItems(be.toArray(new String[be.size()]), nosort);
			}
		} else if (citations == null) {
			citeproc.registerCitationItems(itemDataProvider.getIds(), nosort);
		}
		
		//set default citation items
		if (citations == null && citationItems == null) {
			citationItems = new ArrayList<List<CSLCitationItem>>();
			citationItems.add(citeproc.getRegistryReflist());
		}
		
		//make citations
		String citationResult = null;
		if (citationItems != null) {
			citationResult = "";
			for (List<CSLCitationItem> cits : citationItems) {
				if (citationResult.length() > 0) {
					citationResult += "\n";
				}
				citationResult += citeproc.makeCitationCluster(
						cits.toArray(new CSLCitationItem[cits.size()]));
			}
		} else if (citations != null) {
			List<List<Object>> slice = citations.subList(0, citations.size() - 1);
			for (List<Object> cit : slice) {
				citeproc.makeCitation((CSLCitation)cit.get(0),
						(List<CitationIDIndexPair>)cit.get(1),
						(List<CitationIDIndexPair>)cit.get(2));
			}
			
			List<Object> citation = citations.get(citations.size() - 1);
			List<Citation> r = citeproc.makeCitation((CSLCitation)citation.get(0),
					(List<CitationIDIndexPair>)citation.get(1),
					(List<CitationIDIndexPair>)citation.get(2));
			
			Map<Integer, Integer> indexMap = new HashMap<Integer, Integer>();
			int pos = 0;
			for (Citation c : r) {
				indexMap.put(c.getIndex(), pos);
				++pos;
			}
			
			List<String> resultCitations = new ArrayList<String>();
			for (int cpos = 0; cpos < citeproc.getCitationsByIndex().size(); ++cpos) {
				if (indexMap.containsKey(cpos)) {
					resultCitations.add(">>[" + cpos + "] " + r.get(indexMap.get(cpos)).getText());
				} else {
					resultCitations.add("..[" + cpos + "] " + citeproc.callProcessCitationCluster(cpos));
				}
			}
			citationResult = StringUtils.join(resultCitations, "\n");
		}
		
		//make bibliography
		if (mode.equals("bibliography") || mode.equals("bibliography-nosort")) {
			if (bibSection != null || bibSectionQuash != null) {
				citationResult = citeproc.makeBibliography(bibSectionMode,
						bibSection, bibSectionQuash).makeString();
			} else {
				citationResult = citeproc.makeBibliography().makeString();
			}
		} else if (mode.equals("bibliography-header")) {
			Bibliography p = citeproc.makeBibliography();
			citationResult = "";
			citationResult += "bibend: " + p.getBibEnd() + "\n";
			citationResult += "bibliography_errors: \n"; //TODO not implemented yet
			citationResult += "bibstart: " + p.getBibStart() + "\n";
			citationResult += "done: " + p.getDone() + "\n";
			citationResult += "entry_ids: " + StringUtils.join(p.getEntryIds(), ",") + "\n";
			citationResult += "entryspacing: " + p.getEntrySpacing() + "\n";
			citationResult += "linespacing: " + p.getLineSpacing() + "\n";
			citationResult += "maxoffset: " + p.getMaxOffset() + "\n";
			citationResult += "second-field-align: " + p.getSecondFieldAlign();
		}
		
		//compare result
		if (!result.equals(citationResult)) {
			throw new IllegalStateException("expected: <" + result +
					"> but was <" + citationResult + ">");
		}
	}
	
	/**
	 * Reads a compiled test file (.json)
	 * @param f the test file
	 * @return the configuration read from the file
	 * @throws IOException if the file could not be read
	 */
	private Map<String, Object> readFile(File f) throws IOException {
		FileInputStream fis = new FileInputStream(f);
		try {
			JsonLexer jsonLexer = new JsonLexer(new InputStreamReader(fis, "UTF-8"));
			JsonParser jsonParser = new JsonParser(jsonLexer);
			Map<String, Object> m = jsonParser.parseObject();
			
			//remove items whose value is 'false'
			Iterator<Map.Entry<String, Object>> i = m.entrySet().iterator();
			while (i.hasNext()) {
				Map.Entry<String, Object> e = i.next();
				if (e.getValue() instanceof Boolean && !((Boolean)e.getValue()).booleanValue()) {
					i.remove();
				}
			}
			
			return m;
		} finally {
			fis.close();
		}
	}
	
	/**
	 * Convert a 'bibsection' configuration to a list of item data objects
	 * @param bs the configuration
	 * @return the item data objects
	 */
	private CSLItemData[] convertBibSection(Collection<Map<String, Object>> bs) {
		CSLItemData[] r = new CSLItemData[bs.size()];
		int i = 0;
		for (Map<String, Object> s : bs) {
			String f = (String)s.get("field");
			Object v = s.get("value");
			if (f.equals("issued") && ((String)v).isEmpty()) {
				v = new HashMap<String, Object>();
			} else if (f.equals("categories")) {
				v = Arrays.asList(v);
			}
			Map<String, Object> m = new HashMap<String, Object>();
			m.put(f, v);
			r[i++] = CSLItemData.fromJson(m);
		}
		return r;
	}
	
	/**
	 * A special citation processor that allows access to the internal API
	 */
	private static class TestSuiteCSL extends CSL {
		public TestSuiteCSL(ItemDataProvider itemDataProvider,
				AbbreviationProvider abbreviationProvider, String style)
				throws IOException {
			super(itemDataProvider, abbreviationProvider, style);
		}

		public List<CSLCitation> getCitationsByIndex() {
			List<?> r;
			try {
				r = (List<?>)getScriptRunner().eval("__engine__.registry.citationreg.citationByIndex");
			} catch (ScriptRunnerException e) {
				throw new IllegalArgumentException("Could not get registered citations", e);
			}
			
			List<CSLCitation> result = new ArrayList<CSLCitation>();
			for (Object o : r) {
				@SuppressWarnings("unchecked")
				Map<String, Object> m = (Map<String, Object>)o;
				result.add(CSLCitation.fromJson(m));
			}
			return result;
		}
		
		public String callProcessCitationCluster(int cpos) {
			try {
				return (String)getScriptRunner().eval("__engine__.process_CitationCluster("
						+ "__engine__.registry.citationreg.citationByIndex[" + cpos + "].sortedItems)");
			} catch (ScriptRunnerException e) {
				throw new IllegalArgumentException("Could not get registered citations", e);
			}
		}
		
		public List<CSLCitationItem> getRegistryReflist() {
			List<?> r;
			try {
				r = (List<?>)getScriptRunner().eval("__engine__.registry.reflist");
			} catch (ScriptRunnerException e) {
				throw new IllegalArgumentException("Could not get registered citation items", e);
			}
			
			List<CSLCitationItem> result = new ArrayList<CSLCitationItem>();
			for (Object o : r) {
				@SuppressWarnings("unchecked")
				Map<String, Object> m = (Map<String, Object>)o;
				result.add(CSLCitationItem.fromJson(m));
			}
			return result;
		}
		
		public String makeCitationCluster(CSLCitationItem... citation) {
			try {
				return (String)getScriptRunner().callMethod("__engine__",
						"makeCitationCluster", (Object)citation);
			} catch (ScriptRunnerException e) {
				throw new IllegalArgumentException("Could not make citation custer", e);
			}
		}
	}
}

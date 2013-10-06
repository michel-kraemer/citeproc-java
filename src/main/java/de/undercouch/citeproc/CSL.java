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

import static org.apache.commons.lang.StringEscapeUtils.escapeJava;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.undercouch.citeproc.csl.CSLCitation;
import de.undercouch.citeproc.csl.CSLCitationItem;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CitationIDIndexPair;
import de.undercouch.citeproc.helper.CSLUtils;
import de.undercouch.citeproc.helper.json.JsonBuilder;
import de.undercouch.citeproc.helper.json.MapJsonBuilderFactory;
import de.undercouch.citeproc.output.Bibliography;
import de.undercouch.citeproc.output.Citation;
import de.undercouch.citeproc.output.SecondFieldAlign;
import de.undercouch.citeproc.script.ScriptRunner;
import de.undercouch.citeproc.script.ScriptRunnerException;
import de.undercouch.citeproc.script.ScriptRunnerFactory;

/**
 * The citation processor
 * @author Michel Kraemer
 */
public class CSL {
	/**
	 * A JavaScript runner used to execute citeproc-js
	 */
	private final ScriptRunner runner;
	
	/**
	 * Constructs a new citation processor
	 * @param itemDataProvider an object that provides citation item data
	 * @param style the citation style to use. May either be a serialized
	 * XML representation of the style or a style's name such as <code>ieee</code>.
	 * In the latter case, the processor loads the style from the classpath (e.g.
	 * <code>/ieee.csl</code>)
	 * @throws IOException if the underlying JavaScript files or the CSL style
	 * could not be loaded 
	 */
	public CSL(ItemDataProvider itemDataProvider, String style) throws IOException {
		this(itemDataProvider, style, "en-US");
	}
	
	/**
	 * Constructs a new citation processor
	 * @param itemDataProvider an object that provides citation item data
	 * @param abbreviationProvider an object that provides abbreviations
	 * @param style the citation style to use. May either be a serialized
	 * XML representation of the style or a style's name such as <code>ieee</code>.
	 * In the latter case, the processor loads the style from the classpath (e.g.
	 * <code>/ieee.csl</code>)
	 * @throws IOException if the underlying JavaScript files or the CSL style
	 * could not be loaded 
	 */
	public CSL(ItemDataProvider itemDataProvider, AbbreviationProvider abbreviationProvider,
			String style) throws IOException {
		this(itemDataProvider, abbreviationProvider, style, "en-US");
	}
	
	/**
	 * Constructs a new citation processor
	 * @param itemDataProvider an object that provides citation item data
	 * @param style the citation style to use. May either be a serialized
	 * XML representation of the style or a style's name such as <code>ieee</code>.
	 * In the latter case, the processor loads the style from the classpath (e.g.
	 * <code>/ieee.csl</code>)
	 * @param lang an RFC 4646 identifier for the citation locale (e.g. <code>en-US</code>)
	 * @throws IOException if the underlying JavaScript files or the CSL style
	 * could not be loaded 
	 */
	public CSL(ItemDataProvider itemDataProvider, String style, String lang) throws IOException {
		this(itemDataProvider, new DefaultLocaleProvider(), style, lang, false);
	}
	
	/**
	 * Constructs a new citation processor
	 * @param itemDataProvider an object that provides citation item data
	 * @param abbreviationProvider an object that provides abbreviations
	 * @param style the citation style to use. May either be a serialized
	 * XML representation of the style or a style's name such as <code>ieee</code>.
	 * In the latter case, the processor loads the style from the classpath (e.g.
	 * <code>/ieee.csl</code>)
	 * @param lang an RFC 4646 identifier for the citation locale (e.g. <code>en-US</code>)
	 * @throws IOException if the underlying JavaScript files or the CSL style
	 * could not be loaded 
	 */
	public CSL(ItemDataProvider itemDataProvider, AbbreviationProvider abbreviationProvider,
			String style, String lang) throws IOException {
		this(itemDataProvider, new DefaultLocaleProvider(), abbreviationProvider,
				style, lang, false);
	}
	
	/**
	 * Constructs a new citation processor
	 * @param itemDataProvider an object that provides citation item data
	 * @param localeProvider an object that provides CSL locales
	 * @param style the citation style to use. May either be a serialized
	 * XML representation of the style or a style's name such as <code>ieee</code>.
	 * In the latter case, the processor loads the style from the classpath (e.g.
	 * <code>/ieee.csl</code>)
	 * @param lang an RFC 4646 identifier for the citation locale (e.g. <code>en-US</code>)
	 * @param forceLang true if the given locale should overwrite any default locale
	 * @throws IOException if the underlying JavaScript files or the CSL style
	 * could not be loaded 
	 */
	public CSL(ItemDataProvider itemDataProvider, LocaleProvider localeProvider,
			String style, String lang, boolean forceLang) throws IOException {
		this(itemDataProvider, localeProvider, new DefaultAbbreviationProvider(),
				style, lang, forceLang);
	}
	
	/**
	 * Constructs a new citation processor
	 * @param itemDataProvider an object that provides citation item data
	 * @param localeProvider an object that provides CSL locales
	 * @param abbreviationProvider an object that provides abbreviations
	 * @param style the citation style to use. May either be a serialized
	 * XML representation of the style or a style's name such as <code>ieee</code>.
	 * In the latter case, the processor loads the style from the classpath (e.g.
	 * <code>/ieee.csl</code>)
	 * @param lang an RFC 4646 identifier for the citation locale (e.g. <code>en-US</code>)
	 * @param forceLang true if the given locale should overwrite any default locale
	 * @throws IOException if the underlying JavaScript files or the CSL style
	 * could not be loaded 
	 */
	public CSL(ItemDataProvider itemDataProvider, LocaleProvider localeProvider,
			AbbreviationProvider abbreviationProvider, String style,
			String lang, boolean forceLang) throws IOException {
		//create JavaScript runner
		runner = ScriptRunnerFactory.createRunner();
		
		//initialize global variables
		runner.put("__scriptRunner__", runner);
		runner.put("__itemDataProvider__", itemDataProvider);
		runner.put("__localeProvider__", localeProvider);
		runner.put("__abbreviationProvider__", abbreviationProvider);
		
		//load bundles scripts
		try {
			runner.loadScript(getClass().getResource("xmle4x.js"));
			runner.loadScript(getClass().getResource("citeproc.js"));
			runner.loadScript(getClass().getResource("formats.js"));
			runner.loadScript(getClass().getResource("loadsys.js"));
		} catch (ScriptRunnerException e) {
			//should never happen because bundled JavaScript files should be OK indeed
			throw new RuntimeException("Invalid bundled javascript file", e);
		}
		
		//load style if needed
		if (!isStyle(style)) {
			style = loadStyle(style);
		}
		
		//initialize engine
		try {
			runner.eval("var __engine__ = new CSL.Engine(Sys, \"" + escapeJava(style) + "\", \"" +
					escapeJava(lang) + "\", " + forceLang + ");");
		} catch (ScriptRunnerException e) {
			throw new IllegalArgumentException("Could not parse arguments", e);
		}
	}
	
	/**
	 * @return the JavaScript runner used to execute citeproc-js
	 */
	protected ScriptRunner getScriptRunner() {
		return runner;
	}
	
	/**
	 * Checks if the given String contains the serialized XML representation
	 * of a style
	 * @param style the string to examine
	 * @return true if the String is XML, false otherwise
	 */
	private boolean isStyle(String style) {
		for (int i = 0; i < style.length(); ++i) {
			char c = style.charAt(i);
			if (!Character.isWhitespace(c)) {
				return (c == '<');
			}
		}
		return false;
	}
	
	/**
	 * Loads a CSL style from the classpath. For example, if the given name
	 * is <code>ieee</code> this method will load the file <code>/ieee.csl</code>
	 * @param styleName the style's name
	 * @return the serialized XML representation of the style
	 * @throws IOException if the style could not be loaded
	 */
	private String loadStyle(String styleName) throws IOException {
		if (!styleName.endsWith(".csl")) {
			styleName = styleName + ".csl";
		}
		if (!styleName.startsWith("/")) {
			styleName = "/" + styleName;
		}
		URL url = getClass().getResource(styleName);
		if (url == null) {
			throw new FileNotFoundException("Could not find style in classpath: " + styleName);
		}
		return CSLUtils.readURLToString(url, "UTF-8");
	}

	/**
	 * Sets the processor's output format
	 * @param format the format (one of <code>"html"</code>,
	 * <code>"text"</code>, <code>"asciidoc"</code>, <code>"fo"</code>,
	 * or <code>"rtf"</code>)
	 */
	public void setOutputFormat(String format) {
		try {
			runner.eval("__engine__.setOutputFormat(\"" + escapeJava(format) + "\");");
		} catch (ScriptRunnerException e) {
			throw new IllegalArgumentException("Could not set output format", e);
		}
	}
	
	/**
	 * Specifies if the processor should convert URLs and DOIs in the output
	 * to links. How links are created depends on the output format that has
	 * been set with {@link #setOutputFormat(String)}
	 * @param convert true if URLs and DOIs should be converted to links
	 */
	public void setConvertLinks(boolean convert) {
		try {
			runner.eval("__engine__.opt.development_extensions.wrap_url_and_doi = " + convert + ";");
		} catch (ScriptRunnerException e) {
			throw new IllegalArgumentException("Could not set option", e);
		}
	}
	
	/**
	 * Enables the abbreviation list with the given name. The processor will
	 * call {@link AbbreviationProvider#getAbbreviations(String)} with the
	 * given String to get the abbreviations that should be used from here on.
	 * @param name the name of the abbreviation list to enable
	 */
	public void setAbbreviations(String name) {
		try {
			runner.eval("__engine__.setAbbreviations(\"" + escapeJava(name) + "\");");
		} catch (ScriptRunnerException e) {
			throw new IllegalArgumentException("Could not set abbreviations", e);
		}
	}

	/**
	 * Introduces the given citation IDs to the processor. The processor will
	 * call {@link ItemDataProvider#retrieveItem(String)} for each ID to get
	 * the respective citation item. The retrieved items will be added to the
	 * bibliography, so you don't have to call {@link #makeCitation(String...)}
	 * for each of them anymore.
	 * @param ids the IDs to register
	 * @throws IllegalArgumentException if one of the given IDs refers to
	 * citation item data that does not exist
	 */
	public void registerCitationItems(String... ids) {
		try {
			runner.callMethod("__engine__", "updateItems", ids);
		} catch (ScriptRunnerException e) {
			throw new IllegalArgumentException("Could not update items", e);
		}
	}
	
	/**
	 * Introduces the given citation IDs to the processor. The processor will
	 * call {@link ItemDataProvider#retrieveItem(String)} for each ID to get
	 * the respective citation item. The retrieved items will be added to the
	 * bibliography, so you don't have to call {@link #makeCitation(String...)}
	 * for each of them anymore.
	 * @param ids the IDs to register
	 * @param unsorted true if items should not be sorted in the bibliography
	 * @throws IllegalArgumentException if one of the given IDs refers to
	 * citation item data that does not exist
	 */
	public void registerCitationItems(String[] ids, boolean unsorted) {
		try {
			runner.callMethod("__engine__", "updateItems", ids, unsorted);
		} catch (ScriptRunnerException e) {
			throw new IllegalArgumentException("Could not update items", e);
		}
	}
	
	/**
	 * Generates citation strings that can be inserted into the text. The
	 * method calls {@link ItemDataProvider#retrieveItem(String)} for each of the given
	 * IDs to request the corresponding citation item. Additionally, it saves
	 * the IDs, so {@link #makeBibliography()} will generate a bibliography
	 * that only consists of the retrieved citation items.
	 * @param ids IDs of citation items for which strings should be generated
	 * @return citations strings that can be inserted into the text
	 * @throws IllegalArgumentException if one of the given IDs refers to
	 * citation item data that does not exist
	 */
	public List<Citation> makeCitation(String... ids) {
		CSLCitationItem[] items = new CSLCitationItem[ids.length];
		for (int i = 0; i < ids.length; ++i) {
			items[i] = new CSLCitationItem(ids[i]);
		}
		return makeCitation(new CSLCitation(items));
	}
	
	/**
	 * Generates citation strings that can be inserted into the text. The
	 * method calls {@link ItemDataProvider#retrieveItem(String)} for each item in the
	 * given set to request the corresponding citation item data. Additionally,
	 * it saves the requested citation IDs, so {@link #makeBibliography()} will
	 * generate a bibliography that only consists of the retrieved items.
	 * @param citation a set of citation items for which strings should be generated
	 * @return citations strings that can be inserted into the text
	 * @throws IllegalArgumentException if the given set of citation items
	 * refers to citation item data that does not exist
	 */
	public List<Citation> makeCitation(CSLCitation citation) {
		return makeCitation(citation, null, null);
	}
	
	/**
	 * Generates citation strings that can be inserted into the text. The
	 * method calls {@link ItemDataProvider#retrieveItem(String)} for each item in the
	 * given set to request the corresponding citation item data. Additionally,
	 * it saves the requested citation IDs, so {@link #makeBibliography()} will
	 * generate a bibliography that only consists of the retrieved items.
	 * @param citation a set of citation items for which strings should be generated
	 * @param citationsPre citations that precede <code>citation</code>
	 * @param citationsPost citations that come after <code>citation</code>
	 * @return citations strings that can be inserted into the text
	 * @throws IllegalArgumentException if the given set of citation items
	 * refers to citation item data that does not exist
	 */
	public List<Citation> makeCitation(CSLCitation citation,
			List<CitationIDIndexPair> citationsPre,
			List<CitationIDIndexPair> citationsPost) {
		List<?> r;
		try {
			if (citationsPre == null && citationsPost == null) {
				r = (List<?>)runner.callMethod("__engine__",
						"appendCitationCluster", citation);
			} else {
				r = (List<?>)runner.callMethod("__engine__",
						"processCitationCluster", citation, citationsPre, citationsPost);
				for (Object o : r) {
					if (o instanceof List) {
						r = (List<?>) o;
						break;
					}
				}
			}
		} catch (ScriptRunnerException e) {
			throw new IllegalArgumentException("Could not make citation", e);
		}
		
		List<Citation> result = new ArrayList<Citation>();
		for (Object o : r) {
			if (o instanceof List) {
				@SuppressWarnings("unchecked")
				List<Object> i = (List<Object>)o;
				if (i.get(0) instanceof Number && i.get(1) instanceof CharSequence) {
					int index = ((Number)i.get(0)).intValue();
					String text = i.get(1).toString();
					result.add(new Citation(index, text));
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Generates a bibliography for the registered citations
	 * @return the bibliography
	 */
	public Bibliography makeBibliography() {
		return makeBibliography(null);
	}
	
	/**
	 * Generates a bibliography for the registered citations. Depending
	 * on the selection mode selects, includes, or excludes bibliography
	 * items whose fields and field values match the fields and field values
	 * from the given example item data objects.
	 * @param mode the selection mode
	 * @param selection the example item data objects that contain
	 * the fields and field values to match
	 * @return the bibliography
	 */
	public Bibliography makeBibliography(SelectionMode mode, CSLItemData... selection) {
		return makeBibliography(mode, selection, null);
	}
	
	/**
	 * Generates a bibliography for the registered citations. Depending
	 * on the selection mode selects, includes, or excludes bibliography
	 * items whose fields and field values match the fields and field values
	 * from the given example item data objects.
	 * @param mode the selection mode
	 * @param selection the example item data objects that contain
	 * the fields and field values to match
	 * @param quash regardless of the item data in <code>selection</code>
	 * skip items if all fields/values from this list match
	 * @return the bibliography
	 */
	public Bibliography makeBibliography(SelectionMode mode,
			CSLItemData[] selection, CSLItemData[] quash) {
		List<?> r;
		try {
			if ((selection == null || mode == null) && quash == null) {
				r = (List<?>)runner.eval("__engine__.makeBibliography();");
			} else {
				Map<String, Object> args = new HashMap<String, Object>();
				if (selection != null && mode != null) {
					args.put(mode.toString(), selectionToList(selection));
				}
				if (quash != null) {
					args.put("quash", selectionToList(quash));
				}
				r = (List<?>)runner.callMethod("__engine__", "makeBibliography", args);
			}
		} catch (ScriptRunnerException e) {
			throw new IllegalArgumentException("Could not make bibliography", e);
		}
		
		@SuppressWarnings("unchecked")
		Map<String, Object> fpm = (Map<String, Object>)r.get(0);
		@SuppressWarnings("unchecked")
		List<CharSequence> entriesList = (List<CharSequence>)r.get(1);
		
		String[] entries = new String[entriesList.size()];
		for (int i = 0; i < entries.length; ++i) {
			entries[i] = entriesList.get(i).toString();
		}
		
		int maxOffset = getFromMap(fpm, "maxoffset", 0);
		int entrySpacing = getFromMap(fpm, "entryspacing", 0);
		int lineSpacing = getFromMap(fpm, "linespacing", 0);
		int hangingIndent = getFromMap(fpm, "hangingindent", 0);
		boolean done = getFromMap(fpm, "done", false);
		Collection<?> srcEntryIds = (Collection<?>)fpm.get("entry_ids");
		List<String> dstEntryIds = new ArrayList<String>();
		for (Object o : srcEntryIds) {
			if (o instanceof Collection) {
				Collection<?> oc = (Collection<?>)o;
				for (Object oco : oc) {
					dstEntryIds.add(oco.toString());
				}
			} else {
				dstEntryIds.add(o.toString());
			}
		}
		String[] entryIds = dstEntryIds.toArray(new String[dstEntryIds.size()]);
		SecondFieldAlign secondFieldAlign = SecondFieldAlign.FALSE;
		Object sfa = fpm.get("second-field-align");
		if (sfa != null) {
			secondFieldAlign = SecondFieldAlign.fromString(sfa.toString());
		}
		String bibStart = getFromMap(fpm, "bibstart", "");
		String bibEnd = getFromMap(fpm, "bibend", "");
		
		return new Bibliography(entries, bibStart, bibEnd, entryIds,
				maxOffset, entrySpacing, lineSpacing, hangingIndent,
				done, secondFieldAlign);
	}
	
	/**
	 * Converts the given CSLItemData objects to a list of field/value pairs
	 * that can be used to filter bibliography items. Only those fields will
	 * be included that are actually set in the given objects.
	 * @param selection the CSLItemData objects
	 * @return the list of field/value pairs
	 */
	private List<Map<String, Object>> selectionToList(CSLItemData[] selection) {
		MapJsonBuilderFactory mjbf = new MapJsonBuilderFactory();
		List<Map<String, Object>> sl = new ArrayList<Map<String, Object>>();
		for (CSLItemData item : selection) {
			JsonBuilder jb = mjbf.createJsonBuilder();
			@SuppressWarnings("unchecked")
			Map<String, Object> mi = (Map<String, Object>)item.toJson(jb);
			for (Map.Entry<String, Object> e : mi.entrySet()) {
				Object v = e.getValue();
				if (e.getKey().equals("id") && v instanceof String &&
						((String)v).startsWith("-GEN-")) {
					//skip generated ids
					continue;
				}
				if (v instanceof Collection) {
					Collection<?> coll = (Collection<?>)v;
					if (coll.isEmpty()) {
						putSelectionFieldValue(sl, e, "");
					} else {
						for (Object ao : coll) {
							putSelectionFieldValue(sl, e, ao);
						}
					}
				} else if (v instanceof Map && ((Map<?, ?>)v).isEmpty()) {
					putSelectionFieldValue(sl, e, "");
				} else {
					putSelectionFieldValue(sl, e, v);
				}
			}
		}
		return sl;
	}

	private void putSelectionFieldValue(List<Map<String, Object>> sl,
			Map.Entry<String, Object> e, Object v) {
		Map<String, Object> sf = new HashMap<String, Object>(2);
		sf.put("field", e.getKey());
		sf.put("value", v);
		sl.add(sf);
	}
	
	private int getFromMap(Map<String, Object> m, String key, int def) {
		Number r = (Number)m.get(key);
		if (r == null) {
			return def;
		}
		return r.intValue();
	}
	
	private boolean getFromMap(Map<String, Object> m, String key, boolean def) {
		Object r = m.get(key);
		if (r == null) {
			return def;
		}
		if (r instanceof CharSequence) {
			return Boolean.parseBoolean(r.toString());
		}
		return (Boolean)r;
	}
	
	private String getFromMap(Map<String, Object> m, String key, String def) {
		String r = (String)m.get(key);
		if (r == null) {
			r = def;
		}
		return r;
	}
	
	/**
	 * Resets the processor's state
	 */
	public void reset() {
		try {
			runner.eval("__engine__.restoreProcessorState();");
		} catch (ScriptRunnerException e) {
			throw new IllegalArgumentException("Could not reset processor state", e);
		}
	}
	
	/**
	 * Creates an ad hoc bibliography from the given citation items using the
	 * <code>"html"</code> output format. Calling this method is rather
	 * expensive as it initializes the CSL processor. If you need to create
	 * bibliographies multiple times in your application you should create
	 * the processor yourself and cache it if necessary.
	 * @param style the citation style to use. May either be a serialized
	 * XML representation of the style or a style's name such as <code>ieee</code>.
	 * In the latter case, the processor loads the style from the classpath (e.g.
	 * <code>/ieee.csl</code>)
	 * @param items the citation items to add to the bibliography
	 * @return the bibliography
	 * @throws IOException if the underlying JavaScript files or the CSL style
	 * could not be loaded
	 * @see #makeAdhocBibliography(String, String, CSLItemData...)
	 */
	public static Bibliography makeAdhocBibliography(String style, CSLItemData... items)
			throws IOException {
		return makeAdhocBibliography(style, "html", items);
	}
	
	/**
	 * Creates an ad hoc bibliography from the given citation items. Calling
	 * this method is rather expensive as it initializes the CSL processor.
	 * If you need to create bibliographies multiple times in your application
	 * you should create the processor yourself and cache it if necessary.
	 * @param style the citation style to use. May either be a serialized
	 * XML representation of the style or a style's name such as <code>ieee</code>.
	 * In the latter case, the processor loads the style from the classpath (e.g.
	 * <code>/ieee.csl</code>)
	 * @param outputFormat the processor's output format (one of
	 * <code>"html"</code>, <code>"text"</code>, <code>"asciidoc"</code>,
	 * <code>"fo"</code>, or <code>"rtf"</code>)
	 * @param items the citation items to add to the bibliography
	 * @return the bibliography
	 * @throws IOException if the underlying JavaScript files or the CSL style
	 * could not be loaded
	 */
	public static Bibliography makeAdhocBibliography(String style, String outputFormat,
			CSLItemData... items) throws IOException {
		ItemDataProvider provider = new ListItemDataProvider(items);
		CSL csl = new CSL(provider, style);
		csl.setOutputFormat(outputFormat);
		
		String[] ids = new String[items.length];
		for (int i = 0; i < items.length; ++i) {
			ids[i] = items[i].getId();
		}
		csl.registerCitationItems(ids);
		
		return csl.makeBibliography();
	}
}

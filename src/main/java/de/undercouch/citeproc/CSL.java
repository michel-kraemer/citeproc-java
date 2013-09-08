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
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import de.undercouch.citeproc.csl.CSLCitation;
import de.undercouch.citeproc.csl.CSLCitationItem;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.helper.CSLUtils;
import de.undercouch.citeproc.helper.JsonHelper;
import de.undercouch.citeproc.output.Bibliography;
import de.undercouch.citeproc.output.Citation;
import de.undercouch.citeproc.output.FormattingParameters;

/**
 * The citation processor
 * @author Michel Kraemer
 */
public class CSL {
	/**
	 * A JavaScript engine used to execute citeproc-js
	 */
	private final ScriptEngine engine;
	
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
		//create JavaScript engine
		engine = new ScriptEngineManager().getEngineByName("javascript");
		
		//initialize global variables
		engine.put("__itemDataProvider__", itemDataProvider);
		engine.put("__localeProvider__", localeProvider);
		
		//load bundles scripts
		try {
			evaluateScript("/xmle4x.js");
			evaluateScript("/citeproc.js");
			evaluateScript("/loadsys.js");
		} catch (ScriptException e) {
			//should never happen because bundled JavaScript files should be OK indeed
			throw new RuntimeException("Invalid bundled javascript file", e);
		}
		
		//load style if needed
		if (!isStyle(style)) {
			style = loadStyle(style);
		}
		
		//initialize engine
		try {
			engine.eval("var __engine__ = new CSL.Engine(Sys, \"" + escapeJava(style) + "\", \"" +
					escapeJava(lang) + "\", " + forceLang + ");");
		} catch (ScriptException e) {
			throw new IllegalArgumentException("Could not parse arguments", e);
		}
	}
	
	/**
	 * Loads a script from the classpath and evaluates it
	 * @param filename the script's filename
	 * @throws IOException if the script could not be loaded
	 * @throws ScriptException if the script is invalid
	 */
	private void evaluateScript(String filename) throws IOException, ScriptException {
		URL citeProcURL = getClass().getResource(filename);
		if (citeProcURL == null) {
			throw new FileNotFoundException("Could not find " + filename + " in classpath");
		}
		
		InputStreamReader reader = new InputStreamReader(citeProcURL.openStream());
		try {
			engine.eval(reader);
		} finally {
			reader.close();
		}
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
		URL url = getClass().getResource("/" + styleName + ".csl");
		if (url == null) {
			throw new FileNotFoundException("Could not find style in classpath: " + styleName);
		}
		return CSLUtils.readURLToString(url, "UTF-8");
	}

	/**
	 * Sets the processor's output format
	 * @param format the format (one of <code>"html"</code>,
	 * <code>"text"</code>, or <code>"rtf"</code>)
	 */
	public void setOutputFormat(String format) {
		try {
			engine.eval("__engine__.setOutputFormat(\"" + escapeJava(format) + "\");");
		} catch (ScriptException e) {
			throw new IllegalArgumentException("Could not set output format", e);
		}
	}

	/**
	 * Introduces the given citation IDs to the processor. The processor will
	 * call {@link ItemDataProvider#retrieveItem(String)} for each ID to get the respective
	 * citation item.
	 * @param ids the IDs to register
	 */
	public void registerCitationItems(String... ids) {
		try {
			engine.eval("__engine__.updateItems(" + JsonHelper.toJson(ids) + ");");
		} catch (ScriptException e) {
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
	 */
	public List<Citation> makeCitation(CSLCitation citation) {
		List<Object> r;
		try {
			@SuppressWarnings("unchecked")
			List<Object> rr = (List<Object>)engine.eval(
					"__engine__.appendCitationCluster(" + JsonHelper.toJson(citation) + ");");
			r = rr;
		} catch (ScriptException e) {
			throw new IllegalArgumentException("Could not append citation cluster", e);
		}
		
		List<Citation> result = new ArrayList<Citation>();
		for (Object o : r) {
			if (o instanceof List) {
				@SuppressWarnings("unchecked")
				List<Object> i = (List<Object>)o;
				if (i.get(0) instanceof Number && i.get(1) instanceof String) {
					int index = ((Number)i.get(0)).intValue();
					String text = (String)i.get(1);
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
		List<Object> r;
		try {
			@SuppressWarnings("unchecked")
			List<Object> rr = (List<Object>)engine.eval("__engine__.makeBibliography();");
			r = rr;
		} catch (ScriptException e) {
			throw new IllegalArgumentException("Could not make bibliography", e);
		}
		
		@SuppressWarnings("unchecked")
		Map<String, Object> fpm = (Map<String, Object>)r.get(0);
		@SuppressWarnings("unchecked")
		List<String> entriesList = (List<String>)r.get(1);
		String[] entries = entriesList.toArray(new String[entriesList.size()]);
		
		int maxOffset = getFromMap(fpm, "maxoffset", 0);
		int entrySpacing = getFromMap(fpm, "entryspacing", 0);
		int lineSpacing = getFromMap(fpm, "linespacing", 0);
		int hangingIndent = getFromMap(fpm, "hangingindent", 0);
		boolean secondFieldAlign = getFromMap(fpm, "second-field-align", false);
		String bibStart = getFromMap(fpm, "bibstart", "");
		String bibEnd = getFromMap(fpm, "bibend", "");
		
		FormattingParameters fp = new FormattingParameters(maxOffset, entrySpacing,
				lineSpacing, hangingIndent, secondFieldAlign, bibStart, bibEnd);
		return new Bibliography(entries, fp);
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
		if (r instanceof String) {
			return Boolean.parseBoolean((String)r);
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
	 * <code>"html"</code>, <code>"text"</code>, or <code>"rtf"</code>)
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

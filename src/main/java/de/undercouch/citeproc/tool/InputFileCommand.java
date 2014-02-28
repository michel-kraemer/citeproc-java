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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jbibtex.BibTeXDatabase;
import org.jbibtex.ParseException;

import de.undercouch.citeproc.ItemDataProvider;
import de.undercouch.citeproc.ListItemDataProvider;
import de.undercouch.citeproc.bibtex.BibTeXConverter;
import de.undercouch.citeproc.bibtex.BibTeXItemDataProvider;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.endnote.EndNoteConverter;
import de.undercouch.citeproc.endnote.EndNoteItemDataProvider;
import de.undercouch.citeproc.endnote.EndNoteLibrary;
import de.undercouch.citeproc.helper.json.JsonLexer;
import de.undercouch.citeproc.helper.json.JsonParser;
import de.undercouch.citeproc.helper.tool.Command;
import de.undercouch.citeproc.helper.tool.InputReader;
import de.undercouch.citeproc.helper.tool.Option.ArgumentType;
import de.undercouch.citeproc.helper.tool.OptionDesc;
import de.undercouch.citeproc.helper.tool.OptionParserException;
import de.undercouch.citeproc.ris.RISConverter;
import de.undercouch.citeproc.ris.RISItemDataProvider;
import de.undercouch.citeproc.ris.RISLibrary;

/**
 * A command that reads input bibliography files and delegates them
 * to another command
 * @author Michel Kraemer
 */
public class InputFileCommand extends AbstractCSLToolCommand {
	/**
	 * File formats for bibliography files
	 */
	private static enum FileFormat {
		BIBTEX,
		JSON_OBJECT,
		JSON_ARRAY,
		ENDNOTE,
		RIS,
		UNKNOWN
	}
	
	private final ProviderCommand delegate;

	/**
	 * The input bibliography file
	 */
	private String input;
	
	/**
	 * Constructs a new command
	 * @param delegate the delegate
	 */
	public InputFileCommand(ProviderCommand delegate) {
		this.delegate = delegate;
	}
	
	/**
	 * Sets the input bibliography file
	 * @param input the file
	 */
	@OptionDesc(longName = "input", shortName = "i",
			description = "input bibliography FILE (*.bib, *.enl, *.ris, *.json)",
			argumentName = "FILE", argumentType = ArgumentType.STRING,
			priority = 1)
	public void setInput(String input) {
		this.input = input;
	}
	
	@Override
	protected Class<?>[] getClassesToIntrospect() {
		Class<?>[] sc = super.getClassesToIntrospect();
		Class<?>[] result = new Class<?>[sc.length + 1];
		System.arraycopy(sc, 0, result, 0, sc.length);
		result[result.length - 1] = delegate.getClass();
		return result;
	}
	
	@Override
	protected Command[] getObjectsToEvaluate() {
		Command[] so = super.getObjectsToEvaluate();
		Command[] result = new Command[so.length + 1];
		System.arraycopy(so, 0, result, 0, so.length);
		result[result.length - 1] = delegate;
		return result;
	}
	
	@Override
	public void setDisplayHelp(boolean display) {
		//override this method to disable the help option on this command
		super.setDisplayHelp(display);
	}
	
	@Override
	public String getUsageDescription() {
		return delegate.getUsageDescription();
	}

	@Override
	public String getUsageArguments() {
		return delegate.getUsageArguments();
	}

	@Override
	public boolean checkArguments() {
		//check if there is a bibliography file
		if (input == null) {
			error("no input bibliography specified.");
			return false;
		}
		return delegate.checkArguments();
	}

	@Override
	public int doRun(String[] remainingArgs, InputReader in, PrintWriter out)
			throws OptionParserException, IOException {
		//load input bibliography
		ItemDataProvider provider = readBibliographyFile(input);
		if (provider == null) {
			return 1;
		}
		
		delegate.setProvider(provider);
		return delegate.doRun(remainingArgs, in, out);
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
			error("bibliography file `" + bibliography + "' does not exist.");
			return null;
		}
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(bibfile));
		
		ItemDataProvider provider;
		try {
			//determine file format
			FileFormat ff = determineFileFormat(bis, bibfile);
			
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
			} else if (ff == FileFormat.ENDNOTE) {
				EndNoteLibrary lib = new EndNoteConverter().loadLibrary(bis);
				EndNoteItemDataProvider endnoteprovider = new EndNoteItemDataProvider();
				endnoteprovider.addLibrary(lib);
				provider = endnoteprovider;
			} else if (ff == FileFormat.RIS) {
				RISLibrary lib = new RISConverter().loadLibrary(bis);
				RISItemDataProvider risprovider = new RISItemDataProvider();
				risprovider.addLibrary(lib);
				provider = risprovider;
			} else {
				error("unknown bibliography file format");
				return null;
			}
		} catch (ParseException e) {
			error("could not parse bibliography file.\n" + e.getMessage());
			return null;
		} finally {
			bis.close();
		}
		
		return provider;
	}

	/**
	 * Checks the first 100 KB of the given input stream and tries to
	 * determine the file format. Resets the input stream to the position
	 * it had when the method was called.
	 * @param bis the input stream
	 * @param file the input file
	 * @return the file format
	 * @throws IOException if the input stream could not be read
	 */
	private FileFormat determineFileFormat(BufferedInputStream bis, File file) throws IOException {
		int len = 1024 * 100;
		
		String ext = "";
		int dot = file.getName().lastIndexOf('.');
		if (dot > 0) {
			ext = file.getName().substring(dot + 1);
		}
		
		//check if it's an EndNote library
		bis.mark(len);
		try {
			byte[] firstCharacters = new byte[5];
			bis.read(firstCharacters);
			
			//check if the file starts with an EndNote tag, but
			//also make sure the extension is not 'bib' because
			//BibTeX comments look like EndNote tags
			if (firstCharacters[0] == '%' &&
					Character.isWhitespace(firstCharacters[2]) &&
					!ext.equalsIgnoreCase("bib")) {
				return FileFormat.ENDNOTE;
			}
			
			//check if the file starts with a RIS type tag
			if (firstCharacters[0] == 'T' && firstCharacters[1] == 'Y' &&
					Character.isWhitespace(firstCharacters[2]) &&
					Character.isWhitespace(firstCharacters[3]) &&
					firstCharacters[4] == '-') {
				return FileFormat.RIS;
			}
		} finally {
			bis.reset();
		}
		
		//now check if it's json or bibtex
		bis.mark(len);
		try {
			while (true) {
				int c = bis.read();
				--len;
				if (c < 0 || len < 2) {
					return FileFormat.UNKNOWN;
				}
				
				if (!Character.isWhitespace(c)) {
					if (c == '[') {
						return FileFormat.JSON_ARRAY;
					} else if (c == '{') {
						return FileFormat.JSON_OBJECT;
					}
					return FileFormat.BIBTEX;
				}
			}
		} finally {
			bis.reset();
		}
	}
}

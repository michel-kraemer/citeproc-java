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

import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.ItemDataProvider;
import de.undercouch.citeproc.helper.tool.InputReader;
import de.undercouch.citeproc.helper.tool.Option.ArgumentType;
import de.undercouch.citeproc.helper.tool.OptionDesc;
import de.undercouch.citeproc.helper.tool.ToolUtils;
import de.undercouch.citeproc.output.Bibliography;

/**
 * CLI command that generates a bibliography
 * @author Michel Kraemer
 */
public class BibliographyCommand extends CitationIdsCommand {
	private String style = "ieee";
	private String locale = "en-US";
	private String format = "text";
	
	/**
	 * Sets the citation style (default: ieee)
	 * @param style the style
	 */
	@OptionDesc(longName = "style", shortName = "s",
			description = "citation STYLE name (default: ieee)",
			argumentName = "STYLE", argumentType = ArgumentType.STRING,
			priority = 10)
	public void setStyle(String style) {
		this.style = style;
	}

	/**
	 * Sets the output format (default: text)
	 * @param format the format
	 */
	@OptionDesc(longName = "format", shortName = "f",
			description = "output format: text (default), html, asciidoc, fo, rtf",
			argumentName = "FORMAT", argumentType = ArgumentType.STRING,
			priority = 20)
	public void setFormat(String format) {
		this.format = format;
	}
	
	/**
	 * Sets the citation locale (default: en-US)
	 * @param locale the locale
	 */
	@OptionDesc(longName = "locale", shortName = "l",
			description = "citation LOCALE (default: en-US)",
			argumentName = "LOCALE", argumentType = ArgumentType.STRING,
			priority = 30)
	public void setLocale(String locale) {
		this.locale = locale;
	}
	
	@Override
	public String getUsageName() {
		return "bibliography";
	}
	
	@Override
	public String getUsageDescription() {
		return "Generate a bibliography from an input file";
	}
	
	@Override
	public boolean checkArguments() {
		//check output format
		if (!format.equals("text") && !format.equals("html") &&
				!format.equals("asciidoc") && !format.equals("fo") &&
				!format.equals("rtf")) {
			error("invalid output format: " + format);
			return false;
		}
		
		return super.checkArguments();
	}
	
	/**
	 * Checks if the given style exists and output possible alternatives if
	 * it does not
	 * @param style the style
	 * @return true if the style exists, false otherwise
	 * @throws IOException if the style could not be loaded
	 */
	private boolean checkStyle(String style) throws IOException {
		if (CSL.supportsStyle(style)) {
			//style is supported
			return true;
		}

		//style is not supported. look for alternatives.
		String message = "Could not find style in classpath: " + style;
		List<String> availableStyles = CSL.getSupportedStyles();
		
		//output alternative
		if (!availableStyles.isEmpty()) {
			String dyms = ToolUtils.getDidYouMeanString(availableStyles, style);
			if (dyms != null && !dyms.isEmpty()) {
				message += "\n\n" + dyms;
			}
		}
		
		error(message);
		return false;
	}
	
	@Override
	public int doRun(String[] remainingArgs, InputReader in, PrintWriter out)
			throws IOException {
		int ret = super.doRun(remainingArgs, in, out);
		if (ret != 0) {
			return ret;
		}
		
		//run conversion
		return generateCSL(style, locale, format, getCitationIds(),
				getProvider(), out);
	}
	
	/**
	 * Performs CSL conversion and generates a bibliography
	 * @param style the CSL style
	 * @param locale the CSL locale
	 * @param format the output format
	 * @param citationIds the citation ids given on the command line
	 * @param provider a provider containing all citation item data
	 * @param out the print stream to write the output to
	 * @return the exit code
	 * @throws IOException if the CSL processor could not be initialized
	 */
	private int generateCSL(String style, String locale, String format,
			List<String> citationIds, ItemDataProvider provider,
			PrintWriter out) throws IOException {
		if (!checkStyle(style)) {
			return 1;
		}
		
		//initialize citation processor
		CSL citeproc;
		try {
			citeproc = new CSL(provider, style, locale);
		} catch (FileNotFoundException e) {
			error(e.getMessage());
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
		
		//generate bibliography
		doGenerateCSL(citeproc, citationIdsArr, out);
		
		return 0;
	}
	
	/**
	 * Performs CSL conversion and generates a bibliography
	 * @param citeproc the CSL processor
	 * @param citationIds the citation IDs for which the bibliography
	 * should be generated (the CSL processor should already be prepared)
	 * @param out the stream to write the result to
	 */
	protected void doGenerateCSL(CSL citeproc, String[] citationIds, PrintWriter out) {
		Bibliography bibl = citeproc.makeBibliography();
		out.println(bibl.makeString());
	}
}

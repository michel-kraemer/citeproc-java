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

package de.undercouch.citeproc.endnote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Parses EndNote library files
 * @author Michel Kraemer
 */
public class EndNoteParser {
	/**
	 * Parses EndNote library files
	 * @param r the reader that provides the input to parse
	 * @return the parsed EndNote library
	 * @throws IOException if the input could not be read
	 */
	@SuppressWarnings("resource")
	public EndNoteLibrary parse(Reader r) throws IOException {
		BufferedReader br;
		if (r instanceof BufferedReader) {
			br = (BufferedReader)r;
		} else {
			br = new BufferedReader(r);
		}
		
		EndNoteLibrary result = new EndNoteLibrary();
		EndNoteReferenceBuilder builder = null;
		List<String> authors = new ArrayList<String>();
		List<String> editors = new ArrayList<String>();
		List<String> translatedAuthors = new ArrayList<String>();
		List<String> tertiaryAuthors = new ArrayList<String>();
		List<String> subsidiaryAuthors = new ArrayList<String>();
		List<String> notes = new ArrayList<String>();
		
		int lc = 0;
		String line;
		while ((line = br.readLine()) != null) {
			++lc;
			line = line.trim();
			if (line.isEmpty()) {
				//end of reference
				handleReference(builder, authors, editors, translatedAuthors,
						tertiaryAuthors, subsidiaryAuthors, notes, result);
				authors.clear();
				editors.clear();
				translatedAuthors.clear();
				tertiaryAuthors.clear();
				subsidiaryAuthors.clear();
				notes.clear();
				builder = null;
				continue;
			}
			
			if (line.length() < 3) {
				throw new IOException("Line " + lc + " is too short");
			}
			if (line.charAt(0) != '%') {
				throw new IOException("Illegal first character in line " + lc);
			}
			if (!Character.isWhitespace(line.charAt(2))) {
				throw new IOException("Tag and value must be separated by "
						+ "whitespace in line " + lc);
			}
			
			String value = line.substring(3).trim();
			
			if (builder == null) {
				builder = new EndNoteReferenceBuilder();
			}
			
			switch (line.charAt(1)) {
			case '0':
				builder.type(parseType(value, lc));
				break;
			
			case '1':
				builder.custom1(value);
				break;
			
			case '2':
				builder.custom2(value);
				break;
			
			case '3':
				builder.custom3(value);
				break;
			
			case '4':
				builder.custom4(value);
				break;
			
			case '6':
				builder.numberOfVolumes(value);
				break;
			
			case '7':
				builder.edition(value);
				break;
			
			case '8':
				builder.date(value);
				break;
			
			case '9':
				builder.typeOfWork(value);
				break;
			
			case 'A':
				authors.add(value);
				break;
			
			case 'B':
				builder.bookOrConference(value);
				break;
			
			case 'C':
				builder.place(value);
				break;
			
			case 'D':
				builder.year(value);
				break;
			
			case 'E':
				editors.add(value);
				break;
			
			case 'F':
				builder.label(value);
				break;
			
			case 'G':
				builder.language(value);
				break;
			
			case 'H':
				translatedAuthors.add(value);
				break;
			
			case 'I':
				builder.publisher(value);
				break;
			
			case 'J':
				builder.journal(value);
				break;
			
			case 'K':
				builder.keywords(value);
				break;
			
			case 'L':
				builder.callNumber(value);
				break;
			
			case 'M':
				builder.accessionNumber(value);
				break;
			
			case 'N':
				builder.numberOrIssue(value);
				break;
			
			case 'O':
				notes.add(value);
				break;
			
			case 'P':
				builder.pages(value);
				break;
			
			case 'Q':
				builder.translatedTitle(value);
				break;
			
			case 'R':
				builder.electronicResourceNumber(value);
				break;
			
			case 'S':
				builder.tertiaryTitle(value);
				break;
			
			case 'T':
				builder.title(value);
				break;
			
			case 'U':
				builder.URL(value);
				break;
			
			case 'V':
				builder.volume(value);
				break;
			
			case 'W':
				builder.databaseProvider(value);
				break;
			
			case 'X':
				builder.abstrct(value);
				break;
			
			case 'Y':
				tertiaryAuthors.add(value);
				break;
			
			case 'Z':
				notes.add(value);
				break;
			
			case '?':
				subsidiaryAuthors.add(value);
				break;
			
			case '@':
				builder.isbnOrIssn(value);
				break;
			
			case '!':
				builder.shortTitle(value);
				break;
			
			case '#':
				builder.custom5(value);
				break;
			
			case '$':
				builder.custom6(value);
				break;
			
			case ']':
				builder.custom7(value);
				break;
			
			case '&':
				builder.section(value);
				break;
			
			case '(':
				builder.originalPublication(value);
				break;
			
			case ')':
				builder.reprintEdition(value);
				break;
			
			case '*':
				builder.reviewedItem(value);
				break;
			
			case '+':
				builder.authorAddress(value);
				break;
			
			case '^':
				builder.caption(value);
				break;
			
			case '>':
				builder.linkToPDF(value);
				break;
			
			case '<':
				builder.researchNotes(value);
				break;
			
			case '[':
				builder.accessDate(value);
				break;
			
			case '=':
				builder.lastModifiedDate(value);
				break;
			
			case '~':
				builder.nameOfDatabase(value);
				break;
			
			default:
				throw new IOException("Illegal tag " + line.charAt(1) +
						" in line " + lc);
			}
		}
		
		handleReference(builder, authors, editors, translatedAuthors,
				tertiaryAuthors, subsidiaryAuthors, notes, result);
		
		return result;
	}
	
	private void handleReference(EndNoteReferenceBuilder builder, List<String> authors,
			List<String> editors, List<String> translatedAuthors,
			List<String> tertiaryAuthors, List<String> subsidiaryAuthors,
			List<String> notes, EndNoteLibrary result) {
		if (builder != null) {
			if (!authors.isEmpty()) {
				builder.authors(authors.toArray(new String[authors.size()]));
			}
			if (!editors.isEmpty()) {
				builder.editors(editors.toArray(new String[editors.size()]));
			}
			if (!translatedAuthors.isEmpty()) {
				builder.translatedAuthors(translatedAuthors.toArray(
						new String[translatedAuthors.size()]));
			}
			if (!tertiaryAuthors.isEmpty()) {
				builder.tertiaryAuthors(tertiaryAuthors.toArray(
						new String[tertiaryAuthors.size()]));
			}
			if (!subsidiaryAuthors.isEmpty()) {
				builder.subsidiaryAuthors(subsidiaryAuthors.toArray(
						new String[subsidiaryAuthors.size()]));
			}
			if (!notes.isEmpty()) {
				builder.notes(StringUtils.join(notes, "\n"));
			}
		
			result.addReference(builder.build());
		}
	}
	
	private EndNoteType parseType(String value, int lc) throws IOException {
		try {
			return EndNoteType.fromString(value);
		} catch (IllegalArgumentException e) {
			throw new IOException("Unknown type in line " + lc);
		}
	}
}

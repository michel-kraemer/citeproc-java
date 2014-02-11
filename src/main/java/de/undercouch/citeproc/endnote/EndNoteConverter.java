// Copyright 2013 The Docear Project and Michel Kraemer
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jbibtex.ParseException;

import de.undercouch.citeproc.bibtex.DateParser;
import de.undercouch.citeproc.bibtex.NameParser;
import de.undercouch.citeproc.csl.CSLDate;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CSLItemDataBuilder;
import de.undercouch.citeproc.csl.CSLName;
import de.undercouch.citeproc.csl.CSLType;

/**
 * Converts EndNote references to CSL citation items
 * @author Michel Kraemer
 */
public class EndNoteConverter {
	/**
	 * <p>Loads an EndNote library from a stream.</p>
	 * <p>This method does not close the given stream. The caller is
	 * responsible for closing it.</p>
	 * @param is the input stream to read from
	 * @return the EndNote library
	 * @throws IOException if the library could not be read
	 * @throws ParseException if the library is invalid
	 */
	public EndNoteLibrary loadLibrary(InputStream is) throws IOException, ParseException {
		Reader reader = new InputStreamReader(is, "UTF-8");
		EndNoteParser parser = new EndNoteParser();
		return parser.parse(reader);
	}
	
	/**
	 * Converts the given library to a map of CSL citation items
	 * @param lib the library
	 * @return a map consisting of citation keys and citation items
	 */
	public Map<String, CSLItemData> toItemData(EndNoteLibrary lib) {
		Map<String, CSLItemData> result = new HashMap<String, CSLItemData>();
		for (EndNoteReference ref : lib.getReferences()) {
			CSLItemData item = toItemData(ref);
			result.put(item.getId(), toItemData(ref));
		}
		return result;
	}
	
	/**
	 * Converts an EndNote reference to a citation item
	 * @param ref the reference to convert
	 * @return the citation item
	 */
	public CSLItemData toItemData(EndNoteReference ref) {
		//map type
		CSLType type = toType(ref.getType());
		
		CSLItemDataBuilder builder = new CSLItemDataBuilder().type(type);
		
		//map label
		if (ref.getLabel() != null) {
			builder.id(ref.getLabel());
		}
		
		//map date of last access
		if (ref.getAccessDate() != null) {
			builder.accessed(DateParser.toDate(ref.getAccessDate()));
		}
		
		//map authors
		if (ref.getAuthors() != null) {
			builder.author(toAuthors(ref.getAuthors()));
		}
		
		//map editors
		if (ref.getEditors() != null) {
			builder.editor(toAuthors(ref.getEditors()));
		}
		
		//map container title
		if (ref.getJournal() != null) {
			builder.containerTitle(ref.getJournal());
			builder.collectionTitle(ref.getJournal());
		} else if (ref.getNameOfDatabase() != null) {
			builder.containerTitle(ref.getNameOfDatabase());
		} else {
			builder.containerTitle(ref.getBookOrConference());
			builder.collectionTitle(ref.getBookOrConference());
		}
		
		//map date
		if (ref.getDate() != null) {
			CSLDate date = DateParser.toDate(ref.getDate());
			builder.issued(date);
			builder.eventDate(date);
		} else {
			CSLDate date = DateParser.toDate(ref.getYear());
			builder.issued(date);
			builder.eventDate(date);
		}
		
		//map URL
		if (ref.getLinkToPDF() != null) {
			builder.URL(ref.getLinkToPDF());
		} else {
			builder.URL(ref.getURL());
		}
		
		//map notes
		if (ref.getResearchNotes() != null) {
			builder.note(ref.getResearchNotes());
		} else {
			builder.note(StringUtils.join(ref.getNotes(), "\n"));
		}
		
		//map issue
		builder.issue(ref.getNumberOrIssue());
		builder.number(ref.getNumberOrIssue());
		
		//map location
		builder.eventplace(ref.getPlace());
		builder.publisherPlace(ref.getPlace());
		
		//map other attributes
		builder.abstrct(ref.getAbstrct());
		builder.callNumber(ref.getCallNumber());
		builder.edition(ref.getEdition());
		builder.ISBN(ref.getIsbnOrIssn());
		builder.ISSN(ref.getIsbnOrIssn());
		builder.keyword(StringUtils.join(ref.getKeywords(), ','));
		builder.language(ref.getLanguage());
		builder.numberOfVolumes(ref.getNumberOfVolumes());
		builder.originalTitle(ref.getOriginalPublication());
		builder.page(ref.getPages());
		builder.publisher(ref.getPublisher());
		builder.reviewedTitle(ref.getReviewedItem());
		builder.section(ref.getSection());
		builder.shortTitle(ref.getShortTitle());
		builder.title(ref.getTitle());
		builder.volume(ref.getVolume());
		
		//create citation item
		return builder.build();
	}
	
	/**
	 * Converts a EndNote reference type to a CSL type
	 * @param type the type to convert
	 * @return the converted type (never null, falls back to {@link CSLType#ARTICLE})
	 */
	public CSLType toType(EndNoteType type) {
		switch (type) {
		case ARTWORK:
			return CSLType.ARTICLE;
		case AUDIOVISUAL_MATERIAL:
			return CSLType.ARTICLE;
		case BILL:
			return CSLType.BILL;
		case BOOK:
			return CSLType.BOOK;
		case BOOK_SECTION:
			return CSLType.CHAPTER;
		case CASE:
			return CSLType.LEGAL_CASE;
		case CHART_OR_TABLE:
			return CSLType.ARTICLE;
		case CLASSICAL_WORK:
			return CSLType.MANUSCRIPT;
		case COMPUTER_PROGRAM:
			return CSLType.ARTICLE;
		case CONFERENCE_PAPER:
			return CSLType.PAPER_CONFERENCE;
		case CONFERENCE_PROCEEDINGS:
			return CSLType.BOOK;
		case EDITED_BOOK:
			return CSLType.BOOK;
		case ELECTRONIC_ARTICLE:
			return CSLType.ARTICLE;
		case ELECTRONIC_BOOK:
			return CSLType.BOOK;
		case ELECTRONIC_SOURCE:
			return CSLType.WEBPAGE;
		case EQUATION:
			return CSLType.ARTICLE;
		case FIGURE:
			return CSLType.FIGURE;
		case FILM_OR_BROADCAST:
			return CSLType.BROADCAST;
		case GENERIC:
			return CSLType.ARTICLE;
		case GOVERNMENT_DOCUMENT:
			return CSLType.LEGISLATION;
		case HEARING:
			return CSLType.ARTICLE;
		case JOURNAL_ARTICLE:
			return CSLType.ARTICLE_JOURNAL;
		case LEGAL_RULE_REGULATION:
			return CSLType.LEGISLATION;
		case MAGAZINE_ARTICLE:
			return CSLType.ARTICLE_MAGAZINE;
		case MANUSCRIPT:
			return CSLType.MANUSCRIPT;
		case MAP:
			return CSLType.MAP;
		case NEWSPAPER_ARTICLE:
			return CSLType.ARTICLE_NEWSPAPER;
		case ONLINE_DATABASE:
			return CSLType.WEBPAGE;
		case ONLINE_MULTIMEDIA:
			return CSLType.WEBPAGE;
		case PATENT:
			return CSLType.PATENT;
		case PERSONAL_COMMUNICATION:
			return CSLType.PERSONAL_COMMUNICATION;
		case REPORT:
			return CSLType.REPORT;
		case STATUTE:
			return CSLType.LEGISLATION;
		case THESIS:
			return CSLType.THESIS;
		case UNPUBLISHED_WORK:
			return CSLType.ARTICLE;
		case UNUSED_1:
			return CSLType.ARTICLE;
		case UNUSED_2:
			return CSLType.ARTICLE;
		case UNUSED_3:
			return CSLType.ARTICLE;
		default:
			return CSLType.ARTICLE;
		}
	}
	
	private static CSLName[] toAuthors(String[] authors) {
		List<CSLName> result = new ArrayList<CSLName>();
		for (String a : authors) {
			CSLName[] names = NameParser.parse(a);
			for (CSLName n : names) {
				result.add(n);
			}
		}
		return result.toArray(new CSLName[result.size()]);
	}
}

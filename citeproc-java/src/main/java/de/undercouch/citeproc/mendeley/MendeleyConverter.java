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

package de.undercouch.citeproc.mendeley;

import java.util.List;
import java.util.Map;

import de.undercouch.citeproc.csl.CSLDate;
import de.undercouch.citeproc.csl.CSLDateBuilder;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CSLItemDataBuilder;
import de.undercouch.citeproc.csl.CSLName;
import de.undercouch.citeproc.csl.CSLNameBuilder;
import de.undercouch.citeproc.csl.CSLType;

/**
 * <p>Converts Mendeley documents to CSL citation items.</p>
 * <p>This implementation is based on the
 * <a href="https://github.com/citation-style-editor/csl-editor/wiki/CSL-Editor-Type-and-Field-Mappings-for-Mendeley-Desktop">mapping</a>
 * used in the <a href="http://editor.citationstyles.org/">CSL editor</a>.
 * @author Michel Kraemer
 */
public class MendeleyConverter {
	private static final String FIELD_ABSTRACT = "abstract";
	private static final String FIELD_AUTHORS = "authors";
	private static final String FIELD_BOOK = "book";
	private static final String FIELD_CHAPTER = "chapter";
	private static final String FIELD_CITATION_KEY = "citation_key";
	private static final String FIELD_CITY = "city";
	private static final String FIELD_DATE_ACCESSED = "dateAccessed";
	private static final String FIELD_DAY = "day";
	private static final String FIELD_DISTRIBUTOR = "distributor";
	private static final String FIELD_DOI = "doi";
	private static final String FIELD_EDITION = "edition";
	private static final String FIELD_EDITORS = "editors";
	private static final String FIELD_ENCYCLOPEDIA = "encyclopedia";
	private static final String FIELD_FORENAME = "forename";
	private static final String FIELD_GENRE = "genre";
	private static final String FIELD_ISBN = "isbn";
	private static final String FIELD_ISSUE = "issue";
	private static final String FIELD_ISSUER = "issuer";
	private static final String FIELD_MONTH = "month";
	private static final String FIELD_NOTE = "note";
	private static final String FIELD_NUMBER = "number";
	private static final String FIELD_PAGES = "pages";
	private static final String FIELD_PUBLICATION = "publication";
	private static final String FIELD_PUBLICATION_OUTLET = "publication_outlet";
	private static final String FIELD_PUBLISHED_IN = "published_in";
	private static final String FIELD_PUBLISHER = "publisher";
	private static final String FIELD_REVISION_NUMBER = "revisionNumber";
	private static final String FIELD_SHORT_TITLE = "shortTitle";
	private static final String FIELD_SOURCE = "source";
	private static final String FIELD_SURNAME = "surname";
	private static final String FIELD_TITLE = "title";
	private static final String FIELD_TYPE = "type";
	private static final String FIELD_URL = "url";
	private static final String FIELD_VERSION = "version";
	private static final String FIELD_VOLUME = "volume";
	private static final String FIELD_YEAR = "year";
	
	private static final String TYPE_BILL = "Bill";
	private static final String TYPE_BOOK = "Book";
	private static final String TYPE_BOOK_SECTION = "Book Section";
	private static final String TYPE_CASE = "Case";
	private static final String TYPE_COMPUTER_PROGRAM = "Computer Program";
	private static final String TYPE_CONFERENCE_PROCEEDINGS = "Conference Proceedings";
	private static final String TYPE_ENCYCLOPEDIA_ARTICLE = "Encyclopedia Article";
	private static final String TYPE_FILM = "Film";
	private static final String TYPE_GENERIC = "Generic";
	private static final String TYPE_HEARING = "Hearing";
	private static final String TYPE_JOURNAL_ARTICLE = "Journal Article";
	private static final String TYPE_MAGAZINE_ARTICLE = "Magazine Article";
	private static final String TYPE_NEWSPAPER_ARTICLE = "Newspaper Article";
	private static final String TYPE_PATENT = "Patent";
	private static final String TYPE_REPORT = "Report";
	private static final String TYPE_STATUTE = "Statute";
	private static final String TYPE_TELEVISION_BROADCAST = "Television Broadcast";
	private static final String TYPE_THESIS = "Thesis";
	private static final String TYPE_WEB_PAGE = "Web Page";
	private static final String TYPE_WORKING_PAPER = "Working Paper";

	/**
	 * Converts the given Mendeley document to CSL item data
	 * @param documentId the Mendeley document's ID. Will be used as citation
	 * key if the document does not contain its own
	 * @param document the Mendeley document
	 * @return the CSL item data
	 */
	public static CSLItemData convert(String documentId, Map<String, Object> document) {
		//convert citation id
		String id = (String)document.get(FIELD_CITATION_KEY);
		if (id == null) {
			id = documentId;
		}
		
		//convert type
		String mtype = strOrNull(document.get(FIELD_TYPE));
		CSLType type = toType(mtype);
		
		CSLItemDataBuilder builder = new CSLItemDataBuilder().id(id).type(type);
		
		//convert authors
		if (document.containsKey(FIELD_AUTHORS)) {
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> al = (List<Map<String, Object>>)document.get(FIELD_AUTHORS);
			builder.author(toAuthors(al));
		}
		
		//convert editors
		if (document.containsKey(FIELD_EDITORS)) {
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> el = (List<Map<String, Object>>)document.get(FIELD_EDITORS);
			CSLName[] editors = toAuthors(el);
			builder.editor(editors);
			builder.collectionEditor(editors);
			builder.containerAuthor(editors);
		}
		
		//convert issue date
		if (document.containsKey(FIELD_YEAR)) {
			CSLDateBuilder db = new CSLDateBuilder();
			int year = Integer.parseInt(document.get(FIELD_YEAR).toString());
			if (document.containsKey(FIELD_MONTH)) {
				int month = Integer.parseInt(document.get(FIELD_MONTH).toString());
				if (document.containsKey(FIELD_DAY)) {
					int day = Integer.parseInt(document.get(FIELD_DAY).toString());
					db.dateParts(year, month, day);
				} else {
					db.dateParts(year, month);
				}
			} else {
				db.dateParts(year);
			}
			CSLDate d = db.build();
			builder.issued(d);
			builder.eventDate(d);
		}
		
		//convert number
		if (mtype.equalsIgnoreCase(TYPE_COMPUTER_PROGRAM) && document.containsKey(FIELD_VERSION)) {
			builder.number(strOrNull(document.get(FIELD_VERSION)));
		} else if (document.containsKey(FIELD_REVISION_NUMBER)) {
			builder.number(strOrNull(document.get(FIELD_REVISION_NUMBER)));
		} else {
			builder.number(strOrNull(document.get(FIELD_NUMBER)));
		}
		
		//convert container title
		String containerTitle;
		if (mtype.equalsIgnoreCase(TYPE_BOOK_SECTION) && document.containsKey(FIELD_BOOK)) {
			containerTitle = strOrNull(document.get(FIELD_BOOK));
		} else if (mtype.equalsIgnoreCase(TYPE_ENCYCLOPEDIA_ARTICLE) &&
				document.containsKey(FIELD_ENCYCLOPEDIA)) {
			containerTitle = strOrNull(document.get(FIELD_ENCYCLOPEDIA));
		} else if (document.containsKey(FIELD_PUBLISHED_IN)) {
			containerTitle = strOrNull(document.get(FIELD_PUBLISHED_IN));
		} else if (document.containsKey(FIELD_PUBLICATION_OUTLET)) {
			containerTitle = strOrNull(document.get(FIELD_PUBLICATION_OUTLET));
		} else {
			containerTitle = strOrNull(document.get(FIELD_PUBLICATION));
		}
		builder.containerTitle(containerTitle);
		builder.collectionTitle(containerTitle);
		
		//convert publisher
		if (mtype.equalsIgnoreCase(TYPE_PATENT) && document.containsKey(FIELD_ISSUER)) {
			builder.publisher(strOrNull(document.get(FIELD_ISSUER)));
		} else if (mtype.equalsIgnoreCase(TYPE_PATENT) && document.containsKey(FIELD_SOURCE)) {
			builder.publisher(strOrNull(document.get(FIELD_SOURCE)));
		} else if (mtype.equalsIgnoreCase(TYPE_FILM) && document.containsKey(FIELD_DISTRIBUTOR)) {
			builder.publisher(strOrNull(document.get(FIELD_DISTRIBUTOR)));
		} else {
			builder.publisher(strOrNull(document.get(FIELD_PUBLISHER)));
		}
		
		//convert access date
		if (document.containsKey(FIELD_DATE_ACCESSED)) {
			builder.accessed(new CSLDateBuilder().raw(
					strOrNull(document.get(FIELD_DATE_ACCESSED))).build());
		}
		
		//convert other fields
		builder.abstrct(strOrNull(document.get(FIELD_ABSTRACT)));
		builder.chapterNumber(strOrNull(document.get(FIELD_CHAPTER)));
		builder.eventPlace(strOrNull(document.get(FIELD_CITY)));
		builder.publisherPlace(strOrNull(document.get(FIELD_CITY)));
		builder.DOI(strOrNull(document.get(FIELD_DOI)));
		builder.edition(strOrNull(document.get(FIELD_EDITION)));
		builder.genre(strOrNull(document.get(FIELD_GENRE)));
		builder.ISBN(strOrNull(document.get(FIELD_ISBN)));
		builder.issue(strOrNull(document.get(FIELD_ISSUE)));
		builder.note(strOrNull(document.get(FIELD_NOTE)));
		builder.page(strOrNull(document.get(FIELD_PAGES)));
		builder.shortTitle(strOrNull(document.get(FIELD_SHORT_TITLE)));
		builder.title(strOrNull(document.get(FIELD_TITLE)));
		builder.URL(strOrNull(document.get(FIELD_URL)));
		builder.volume(strOrNull(document.get(FIELD_VOLUME)));
		
		return builder.build();
	}
	
	/**
	 * Converts an object to a string
	 * @param o the object
	 * @return the string or <code>null</code> if <code>o</code> was
	 * also <code>null</code>
	 */
	private static String strOrNull(Object o) {
		if (o == null) {
			return null;
		}
		return o.toString();
	}
	
	/**
	 * Converts a list of authors
	 * @param authors the authors as returned by Mendeley
	 * @return the authors in CSL format
	 */
	private static CSLName[] toAuthors(List<Map<String, Object>> authors) {
		CSLName[] result = new CSLName[authors.size()];
		int i = 0;
		for (Map<String, Object> a : authors) {
			CSLNameBuilder builder = new CSLNameBuilder();
			if (a.containsKey(FIELD_FORENAME)) {
				builder.given(strOrNull(a.get(FIELD_FORENAME)));
			}
			if (a.containsKey(FIELD_SURNAME)) {
				builder.family(strOrNull(a.get(FIELD_SURNAME)));
			}
			builder.parseNames(true);
			result[i] = builder.build();
			++i;
		}
		return result;
	}
	
	/**
	 * Converts a Mendeley type to a CSL type
	 * @param type the Mendeley type
	 * @return the CSL type
	 */
	private static CSLType toType(String type) {
		if (type.equalsIgnoreCase(TYPE_BILL)) {
			return CSLType.BILL;
		} else if (type.equalsIgnoreCase(TYPE_BOOK)) {
			return CSLType.BOOK;
		} else if (type.equalsIgnoreCase(TYPE_BOOK_SECTION)) {
			return CSLType.CHAPTER;
		} else if (type.equalsIgnoreCase(TYPE_CASE)) {
			return CSLType.ARTICLE;
		} else if (type.equalsIgnoreCase(TYPE_COMPUTER_PROGRAM)) {
			return CSLType.ARTICLE;
		} else if (type.equalsIgnoreCase(TYPE_CONFERENCE_PROCEEDINGS)) {
			return CSLType.PAPER_CONFERENCE;
		} else if (type.equalsIgnoreCase(TYPE_ENCYCLOPEDIA_ARTICLE)) {
			return CSLType.ENTRY_ENCYCLOPEDIA;
		} else if (type.equalsIgnoreCase(TYPE_FILM)) {
			return CSLType.MOTION_PICTURE;
		} else if (type.equalsIgnoreCase(TYPE_GENERIC)) {
			return CSLType.ARTICLE;
		} else if (type.equalsIgnoreCase(TYPE_HEARING)) {
			return CSLType.SPEECH;
		} else if (type.equalsIgnoreCase(TYPE_JOURNAL_ARTICLE)) {
			return CSLType.ARTICLE_JOURNAL;
		} else if (type.equalsIgnoreCase(TYPE_MAGAZINE_ARTICLE)) {
			return CSLType.ARTICLE_MAGAZINE;
		} else if (type.equalsIgnoreCase(TYPE_NEWSPAPER_ARTICLE)) {
			return CSLType.ARTICLE_NEWSPAPER;
		} else if (type.equalsIgnoreCase(TYPE_PATENT)) {
			return CSLType.PATENT;
		} else if (type.equalsIgnoreCase(TYPE_REPORT)) {
			return CSLType.REPORT;
		} else if (type.equalsIgnoreCase(TYPE_STATUTE)) {
			return CSLType.LEGISLATION;
		} else if (type.equalsIgnoreCase(TYPE_TELEVISION_BROADCAST)) {
			return CSLType.BROADCAST;
		} else if (type.equalsIgnoreCase(TYPE_THESIS)) {
			return CSLType.THESIS;
		} else if (type.equalsIgnoreCase(TYPE_WEB_PAGE)) {
			return CSLType.WEBPAGE;
		} else if (type.equalsIgnoreCase(TYPE_WORKING_PAPER)) {
			return CSLType.ARTICLE;
		}
		return CSLType.ARTICLE;
	}
}

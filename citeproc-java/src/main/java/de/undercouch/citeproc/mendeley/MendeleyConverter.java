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

import org.apache.commons.lang3.StringUtils;

import de.undercouch.citeproc.bibtex.NameParser;
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
	private static final String FIELD_ACCESSED = "accessed";
	private static final String FIELD_AUTHORS = "authors";
	private static final String FIELD_CHAPTER = "chapter";
	private static final String FIELD_CITATION_KEY = "citation_key";
	private static final String FIELD_CITY = "city";
	private static final String FIELD_DAY = "day";
	private static final String FIELD_EDITION = "edition";
	private static final String FIELD_EDITORS = "editors";
	private static final String FIELD_FIRSTNAME = "first_name";
	private static final String FIELD_GENRE = "genre";
	private static final String FIELD_IDENTIFIERS = "identifiers";
	private static final String FIELD_ISSUE = "issue";
	private static final String FIELD_KEYWORDS = "keywords";
	private static final String FIELD_LANGUAGE = "language";
	private static final String FIELD_MEDIUM = "medium";
	private static final String FIELD_MONTH = "month";
	private static final String FIELD_PAGES = "pages";
	private static final String FIELD_PUBLISHER = "publisher";
	private static final String FIELD_REVISION = "revision";
	private static final String FIELD_SERIES = "series";
	private static final String FIELD_SERIES_EDITOR = "series_editor";
	private static final String FIELD_SERIES_NUMBER = "series_number";
	private static final String FIELD_SHORT_TITLE = "short_title";
	private static final String FIELD_SOURCE = "source";
	private static final String FIELD_LASTNAME = "last_name";
	private static final String FIELD_TITLE = "title";
	private static final String FIELD_TRANSLATORS = "translators";
	private static final String FIELD_TYPE = "type";
	private static final String FIELD_VOLUME = "volume";
	private static final String FIELD_WEBSITES = "websites";
	private static final String FIELD_YEAR = "year";
	
	private static final String IDENTIFIER_DOI = "doi";
	private static final String IDENTIFIER_ISBN = "isbn";
	private static final String IDENTIFIER_ISSN = "issn";
	private static final String IDENTIFIER_PMID = "pmid";
	
	private static final String TYPE_BILL = "bill";
	private static final String TYPE_BOOK = "book";
	private static final String TYPE_BOOK_SECTION = "book_section";
	private static final String TYPE_CASE = "case";
	private static final String TYPE_COMPUTER_PROGRAM = "computer_program";
	private static final String TYPE_CONFERENCE_PROCEEDINGS = "conference_proceedings";
	private static final String TYPE_ENCYCLOPEDIA_ARTICLE = "encyclopedia_article";
	private static final String TYPE_FILM = "film";
	private static final String TYPE_GENERIC = "generic";
	private static final String TYPE_HEARING = "hearing";
	private static final String TYPE_JOURNAL = "journal";
	private static final String TYPE_MAGAZINE_ARTICLE = "magazine_article";
	private static final String TYPE_NEWSPAPER_ARTICLE = "newspaper_article";
	private static final String TYPE_PATENT = "patent";
	private static final String TYPE_REPORT = "report";
	private static final String TYPE_STATUTE = "statute";
	private static final String TYPE_TELEVISION_BROADCAST = "television_broadcast";
	private static final String TYPE_THESIS = "thesis";
	private static final String TYPE_WEB_PAGE = "web_page";
	private static final String TYPE_WORKING_PAPER = "working_paper";

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
		
		if (document.containsKey(FIELD_SERIES_EDITOR)) {
			String seriesEditor = strOrNull(document.get(FIELD_SERIES_EDITOR));
			if (seriesEditor != null) {
				CSLName[] sen = NameParser.parse(seriesEditor);
				builder.collectionEditor(sen);
				builder.containerAuthor(sen);
			}
		}
		
		//convert translators
		if (document.containsKey(FIELD_TRANSLATORS)) {
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> tl = (List<Map<String, Object>>)document.get(FIELD_TRANSLATORS);
			builder.translator(toAuthors(tl));
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
		if (document.containsKey(FIELD_REVISION)) {
			builder.number(strOrNull(document.get(FIELD_REVISION)));
		} else {
			builder.number(strOrNull(document.get(FIELD_SERIES_NUMBER)));
		}
		
		//convert container title
		String containerTitle;
		if (document.containsKey(FIELD_SERIES)) {
			containerTitle = strOrNull(document.get(FIELD_SERIES));
		} else {
			containerTitle = strOrNull(document.get(FIELD_SOURCE));
		}
		builder.containerTitle(containerTitle);
		builder.collectionTitle(containerTitle);
		
		//convert publisher
		if (mtype.equalsIgnoreCase(TYPE_PATENT) && document.containsKey(FIELD_SOURCE)) {
			builder.publisher(strOrNull(document.get(FIELD_SOURCE)));
		} else {
			builder.publisher(strOrNull(document.get(FIELD_PUBLISHER)));
		}
		
		//convert access date
		if (document.containsKey(FIELD_ACCESSED)) {
			builder.accessed(new CSLDateBuilder().raw(
					strOrNull(document.get(FIELD_ACCESSED))).build());
		}
		
		//convert identifiers
		if (document.containsKey(FIELD_IDENTIFIERS)) {
			@SuppressWarnings("unchecked")
			Map<String, String> identifiers = (Map<String, String>)document.get(FIELD_IDENTIFIERS);
			builder.DOI(identifiers.get(IDENTIFIER_DOI));
			builder.ISBN(identifiers.get(IDENTIFIER_ISBN));
			builder.ISSN(identifiers.get(IDENTIFIER_ISSN));
			builder.PMID(identifiers.get(IDENTIFIER_PMID));
		}
		
		//convert keywords
		if (document.containsKey(FIELD_KEYWORDS)) {
			@SuppressWarnings("unchecked")
			List<String> keywords = (List<String>)document.get(FIELD_KEYWORDS);
			builder.keyword(StringUtils.join(keywords, ','));
		}
		
		//convert URL
		if (document.containsKey(FIELD_WEBSITES)) {
			@SuppressWarnings("unchecked")
			List<String> websites = (List<String>)document.get(FIELD_WEBSITES);
			if (websites.size() > 0) {
				//use the first website only
				builder.URL(websites.get(0));
			}
		}
		
		//convert other fields
		builder.abstrct(strOrNull(document.get(FIELD_ABSTRACT)));
		builder.chapterNumber(strOrNull(document.get(FIELD_CHAPTER)));
		builder.eventPlace(strOrNull(document.get(FIELD_CITY)));
		builder.publisherPlace(strOrNull(document.get(FIELD_CITY)));
		builder.edition(strOrNull(document.get(FIELD_EDITION)));
		builder.genre(strOrNull(document.get(FIELD_GENRE)));
		builder.issue(strOrNull(document.get(FIELD_ISSUE)));
		builder.language(strOrNull(document.get(FIELD_LANGUAGE)));
		builder.medium(strOrNull(document.get(FIELD_MEDIUM)));
		builder.page(strOrNull(document.get(FIELD_PAGES)));
		builder.shortTitle(strOrNull(document.get(FIELD_SHORT_TITLE)));
		builder.title(strOrNull(document.get(FIELD_TITLE)));
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
			if (a.containsKey(FIELD_FIRSTNAME)) {
				builder.given(strOrNull(a.get(FIELD_FIRSTNAME)));
			}
			if (a.containsKey(FIELD_LASTNAME)) {
				builder.family(strOrNull(a.get(FIELD_LASTNAME)));
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
		} else if (type.equalsIgnoreCase(TYPE_JOURNAL)) {
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

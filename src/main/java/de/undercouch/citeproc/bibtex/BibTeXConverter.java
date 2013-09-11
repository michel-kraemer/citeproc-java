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

package de.undercouch.citeproc.bibtex;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.BibTeXParser;
import org.jbibtex.BibTeXString;
import org.jbibtex.Key;
import org.jbibtex.LaTeXObject;
import org.jbibtex.LaTeXParser;
import org.jbibtex.LaTeXPrinter;
import org.jbibtex.ParseException;
import org.jbibtex.Value;

import de.undercouch.citeproc.csl.CSLDate;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CSLItemDataBuilder;
import de.undercouch.citeproc.csl.CSLType;

/**
 * <p>Converts BibTeX items to CSL citation items</p>
 * <p>The class maps BibTeX attributes to CSL attributes. The mapping is
 * based on the one used in <a href="http://www.docear.org">Docear</a> as
 * <a href="http://www.docear.org/2012/08/08/docear4word-mapping-bibtex-fields-and-types-with-the-citation-style-language/">presented
 * by Joeran Beel</a>.</p>
 * <p>Docear is released under the GPLv2 but its code may also be reused in
 * projects licensed under Apache License 2.0 (see
 * <a href="http://www.docear.org/software/licence/">http://www.docear.org/software/licence/</a>,
 * last visited 2013-09-06). The mapping here is released under the
 * Apache License 2.0 by permission of Joaran Beel, Docear.</p>
 * @author Joaran Beel
 * @author Michel Kraemer
 */
public class BibTeXConverter {
	private static final String FIELD_ABSTRACT = "abstract";
	private static final String FIELD_ACCESSED = "accessed";
	private static final String FIELD_ADDRESS = "address";
	private static final String FIELD_ANNOTE = "annote";
	private static final String FIELD_AUTHOR = "author";
	private static final String FIELD_BOOKTITLE = "booktitle";
	private static final String FIELD_CHAPTER = "chapter";
	private static final String FIELD_DOI = "doi";
	private static final String FIELD_EDITION = "edition";
	private static final String FIELD_EDITOR = "editor";
	private static final String FIELD_INSTITUTION = "institution";
	private static final String FIELD_ISBN = "isbn";
	private static final String FIELD_ISSN = "issn";
	private static final String FIELD_ISSUE = "issue";
	private static final String FIELD_JOURNAL = "journal";
	private static final String FIELD_KEYWORDS = "keywords";
	private static final String FIELD_LOCATION = "location";
	private static final String FIELD_MONTH = "month";
	private static final String FIELD_NOTE = "note";
	private static final String FIELD_NUMBER = "number";
	private static final String FIELD_ORGANIZATION = "organization";
	private static final String FIELD_PAGES = "pages";
	private static final String FIELD_PUBLISHER = "publisher";
	private static final String FIELD_REVISION = "revision";
	private static final String FIELD_SCHOOL = "school";
	private static final String FIELD_SERIES = "series";
	private static final String FIELD_STATUS = "status";
	private static final String FIELD_TITLE = "title";
	private static final String FIELD_URL = "url";
	private static final String FIELD_VOLUME = "volume";
	private static final String FIELD_YEAR = "year";
	
	private static final String TYPE_ARTICLE = "article";
	private static final String TYPE_BOOK = "book";
	private static final String TYPE_BOOKLET = "booklet";
	private static final String TYPE_CONFERENCE = "conference";
	private static final String TYPE_ELECTRONIC = "electronic";
	private static final String TYPE_INBOOK = "inbook";
	private static final String TYPE_INCOLLECTION = "incollection";
	private static final String TYPE_INPROCEEDINGS = "inproceedings";
	private static final String TYPE_MANUAL = "manual";
	private static final String TYPE_MASTERSTHESIS = "mastersthesis";
	private static final String TYPE_PATENT = "patent";
	private static final String TYPE_PERIODICAL = "periodical";
	private static final String TYPE_PHDTHESIS = "phdthesis";
	private static final String TYPE_PROCEEDINGS = "proceedings";
	private static final String TYPE_STANDARD = "standard";
	private static final String TYPE_TECHREPORT = "techreport";
	private static final String TYPE_UNPUBLISHED = "unpublished";
	
	private LaTeXParser latexParser = new LaTeXParser();
	private LaTeXPrinter latexPrinter = new LaTeXPrinter();
	
	/**
	 * <p>Loads a BibTeX database from a stream.</p>
	 * <p>This method does not close the given stream. The caller is
	 * responsible for closing it.</p>
	 * @param is the input stream to read from
	 * @return the BibTeX database
	 * @throws IOException if the database could not be read
	 * @throws ParseException if the database is invalid
	 */
	public BibTeXDatabase loadDatabase(InputStream is) throws IOException, ParseException {
		InputStreamReader reader = new InputStreamReader(is);
		BibTeXParser parser = new BibTeXParser() {
			@Override
			public void checkStringResolution(Key key, BibTeXString string) {
				if (string == null) {
					//ignore
				}
			}
		};
		return parser.parse(reader);
	}
	
	/**
	 * Converts the given database to a map of CSL citation items
	 * @param db the database
	 * @return a map consisting of citation keys and citation items
	 */
	public Map<String, CSLItemData> toItemData(BibTeXDatabase db) {
		Map<String, CSLItemData> result = new HashMap<String, CSLItemData>();
		for (Map.Entry<Key, BibTeXEntry> e : db.getEntries().entrySet()) {
			result.put(e.getKey().getValue(), toItemData(e.getValue()));
		}
		return result;
	}
	
	/**
	 * Converts a BibTeX entry to a citation item
	 * @param e the BibTeX entry to convert
	 * @return the citation item
	 */
	public CSLItemData toItemData(BibTeXEntry e) {
		//get all fields from the BibTeX entry
		Map<String, String> entries = new HashMap<String, String>();
		for (Map.Entry<Key, Value> field : e.getFields().entrySet()) {
			String us = field.getValue().toUserString();
			
			//convert LaTeX string to normal text
			try {
				List<LaTeXObject> objs = latexParser.parse(new StringReader(us));
				us = latexPrinter.print(objs).replaceAll("\\n", " ").replaceAll("\\r", "").trim();
			} catch (IOException ex) {
				//ignore
			} catch (ParseException ex) {
				//ignore
			}
			
			entries.put(field.getKey().getValue().toLowerCase(), us);
		}
		
		//map type
		CSLType type = toType(e.getType());
		
		CSLItemDataBuilder builder = new CSLItemDataBuilder(e.getKey().getValue(), type);
		
		//map address
		if (entries.containsKey(FIELD_LOCATION)) {
			builder.eventplace(entries.get(FIELD_LOCATION));
			builder.publisherPlace(entries.get(FIELD_LOCATION));
		} else {
			builder.eventplace(entries.get(FIELD_ADDRESS));
			builder.publisherPlace(entries.get(FIELD_ADDRESS));
		}
		
		//map author
		if (entries.containsKey(FIELD_AUTHOR)) {
			builder.author(NameParser.parse(entries.get(FIELD_AUTHOR)));
		}
		
		//map editor
		if (entries.containsKey(FIELD_EDITOR)) {
			builder.editor(NameParser.parse(entries.get(FIELD_EDITOR)));
			builder.collectionEditor(NameParser.parse(entries.get(FIELD_EDITOR)));
			builder.containerAuthor(NameParser.parse(entries.get(FIELD_EDITOR)));
		}
		
		//map date
		CSLDate date = DateParser.toDate(entries.get(FIELD_YEAR), entries.get(FIELD_MONTH));
		builder.issued(date);
		builder.eventDate(date);
		
		//map journal, booktitle, series
		if (entries.containsKey(FIELD_JOURNAL)) {
			builder.containerTitle(entries.get(FIELD_JOURNAL));
			builder.collectionTitle(entries.get(FIELD_JOURNAL));
		} else if (entries.containsKey(FIELD_BOOKTITLE)) {
			builder.containerTitle(entries.get(FIELD_BOOKTITLE));
			builder.collectionTitle(entries.get(FIELD_BOOKTITLE));
		} else {
			builder.containerTitle(entries.get(FIELD_SERIES));
			builder.collectionTitle(entries.get(FIELD_SERIES));
		}
		
		//map number and issue
		builder.number(entries.get(FIELD_NUMBER));
		if (entries.containsKey(FIELD_ISSUE)) {
			builder.issue(entries.get(FIELD_ISSUE));
		} else {
			builder.issue(entries.get(FIELD_NUMBER));
		}
		
		//map publisher, insitution, school, organisation
		if (type == CSLType.REPORT) {
			if (entries.containsKey(FIELD_PUBLISHER)) {
				builder.publisher(entries.get(FIELD_PUBLISHER));
			} else if (entries.containsKey(FIELD_INSTITUTION)) {
				builder.publisher(entries.get(FIELD_INSTITUTION));
			} else if (entries.containsKey(FIELD_SCHOOL)) {
				builder.publisher(entries.get(FIELD_SCHOOL));
			} else {
				builder.publisher(entries.get(FIELD_ORGANIZATION));
			}
		} else if (type == CSLType.THESIS) {
			if (entries.containsKey(FIELD_PUBLISHER)) {
				builder.publisher(entries.get(FIELD_PUBLISHER));
			} else if (entries.containsKey(FIELD_SCHOOL)) {
				builder.publisher(entries.get(FIELD_SCHOOL));
			} else if (entries.containsKey(FIELD_INSTITUTION)) {
				builder.publisher(entries.get(FIELD_INSTITUTION));
			} else {
				builder.publisher(entries.get(FIELD_ORGANIZATION));
			}
		} else {
			if (entries.containsKey(FIELD_PUBLISHER)) {
				builder.publisher(entries.get(FIELD_PUBLISHER));
			} else if (entries.containsKey(FIELD_ORGANIZATION)) {
				builder.publisher(entries.get(FIELD_ORGANIZATION));
			} else if (entries.containsKey(FIELD_INSTITUTION)) {
				builder.publisher(entries.get(FIELD_INSTITUTION));
			} else {
				builder.publisher(entries.get(FIELD_SCHOOL));
			}
		}
		
		//map title or chapter
		if (entries.containsKey(FIELD_TITLE)) {
			builder.title(entries.get(FIELD_TITLE));
		} else {
			builder.title(entries.get(FIELD_CHAPTER));
		}
		
		//map pages
		String pages = entries.get(FIELD_PAGES);
		if (pages != null) {
			PageRange pr = PageParser.parse(pages);
			builder.page(pr.getLiteral());
			builder.pageFirst(pr.getPageFirst());
			if (pr.getNumberOfPages() != null) {
				builder.numberOfPages(String.valueOf(pr.getNumberOfPages()));
			}
		}
		
		//map last accessed date
		if (entries.containsKey(FIELD_ACCESSED)) {
			builder.accessed(DateParser.toDate(entries.get(FIELD_ACCESSED)));
		}
		
		//map other attributes
		builder.volume(entries.get(FIELD_VOLUME));
		builder.keyword(entries.get(FIELD_KEYWORDS));
		builder.URL(entries.get(FIELD_URL));
		builder.status(entries.get(FIELD_STATUS));
		builder.ISSN(entries.get(FIELD_ISSN));
		builder.ISBN(entries.get(FIELD_ISBN));
		builder.version(entries.get(FIELD_REVISION));
		builder.annote(entries.get(FIELD_ANNOTE));
		builder.edition(entries.get(FIELD_EDITION));
		builder.abstrct(entries.get(FIELD_ABSTRACT));
		builder.DOI(entries.get(FIELD_DOI));
		builder.note(entries.get(FIELD_NOTE));
		
		//create citation item
		return builder.build();
	}
	
	/**
	 * Converts a BibTeX type to a CSL type
	 * @param type the type to convert
	 * @return the converted type (never null, falls back to {@link CSLType#ARTICLE})
	 */
	public CSLType toType(Key type) {
		String s = type.getValue();
		if (s.equalsIgnoreCase(TYPE_ARTICLE)) {
			return CSLType.ARTICLE_JOURNAL;
		} else if (s.equalsIgnoreCase(TYPE_PROCEEDINGS)) {
			return CSLType.BOOK;
		} else if (s.equalsIgnoreCase(TYPE_MANUAL)) {
			return CSLType.BOOK;
		} else if (s.equalsIgnoreCase(TYPE_BOOK)) {
			return CSLType.BOOK;
		} else if (s.equalsIgnoreCase(TYPE_PERIODICAL)) {
			return CSLType.BOOK;
		} else if (s.equalsIgnoreCase(TYPE_BOOKLET)) {
			return CSLType.PAMPHLET;
		} else if (s.equalsIgnoreCase(TYPE_INBOOK)) {
			return CSLType.CHAPTER;
		} else if (s.equalsIgnoreCase(TYPE_INCOLLECTION)) {
			return CSLType.CHAPTER;
		} else if (s.equalsIgnoreCase(TYPE_INPROCEEDINGS)) {
			return CSLType.PAPER_CONFERENCE;
		} else if (s.equalsIgnoreCase(TYPE_CONFERENCE)) {
			return CSLType.PAPER_CONFERENCE;
		} else if (s.equalsIgnoreCase(TYPE_MASTERSTHESIS)) {
			return CSLType.THESIS;
		} else if (s.equalsIgnoreCase(TYPE_PHDTHESIS)) {
			return CSLType.THESIS;
		} else if (s.equalsIgnoreCase(TYPE_TECHREPORT)) {
			return CSLType.REPORT;
		} else if (s.equalsIgnoreCase(TYPE_PATENT)) {
			return CSLType.PATENT;
		} else if (s.equalsIgnoreCase(TYPE_ELECTRONIC)) {
			return CSLType.WEBPAGE;
		} else if (s.equalsIgnoreCase(TYPE_STANDARD)) {
			return CSLType.LEGISLATION;
		} else if (s.equalsIgnoreCase(TYPE_UNPUBLISHED)) {
			return CSLType.MANUSCRIPT;
		}
		return CSLType.ARTICLE;
	}
}

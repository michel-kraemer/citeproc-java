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

package $pkg;

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
 * Converts $desc references to CSL citation items
 * @author Michel Kraemer
 */
public class ${desc}Converter {
	/**
	 * <p>Loads a $desc library from a stream.</p>
	 * <p>This method does not close the given stream. The caller is
	 * responsible for closing it.</p>
	 * @param is the input stream to read from
	 * @return the $desc library
	 * @throws IOException if the library could not be read
	 * @throws ParseException if the library is invalid
	 */
	public ${desc}Library loadLibrary(InputStream is) throws IOException, ParseException {
		Reader reader = new InputStreamReader(is, "UTF-8");
		$name parser = new $name();
		return parser.parse(reader);
	}
	
	/**
	 * Converts the given library to a map of CSL citation items
	 * @param lib the library
	 * @return a map consisting of citation keys and citation items
	 */
	public Map<String, CSLItemData> toItemData(${desc}Library lib) {
		Map<String, CSLItemData> result = new HashMap<String, CSLItemData>();
		for (${desc}Reference ref : lib.getReferences()) {
			CSLItemData item = toItemData(ref);
			result.put(item.getId(), toItemData(ref));
		}
		return result;
	}
	
	/**
	 * Converts an $desc reference to a citation item
	 * @param ref the reference to convert
	 * @return the citation item
	 */
	public CSLItemData toItemData(${desc}Reference ref) {
		//map type
		CSLType type = toType(ref.getType());
		
		CSLItemDataBuilder builder = new CSLItemDataBuilder().type(type);
		
		//map label
		<% if (props.values().contains("id")) { %>
		if (ref.getId() != null) {
			builder.id(ref.getId());
		} else
		<% } %>
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
		<% if (props.values().contains("linkToPDF")) { %>
		if (ref.getLinkToPDF() != null) {
			builder.URL(ref.getLinkToPDF());
		} else <% } %>{
			builder.URL(ref.getURL());
		}
		
		//map notes
		if (ref.getResearchNotes() != null) {
			builder.note(ref.getResearchNotes());
		} else {
			builder.note(StringUtils.join(ref.getNotes(), '\\n'));
		}
		
		//map issue
		<% if (props.values().contains("issue")) { %>
		builder.issue(ref.getIssue());
		builder.number(ref.getNumber());
		<% } else { %>
		builder.issue(ref.getNumberOrIssue());
		builder.number(ref.getNumberOrIssue());
		<% } %>
		
		//map location
		builder.eventplace(ref.getPlace());
		builder.publisherPlace(ref.getPlace());
		
		//map other attributes
		builder.abstrct(ref.getAbstrct());
		builder.callNumber(ref.getCallNumber());
		<% if (props.values().contains("DOI")) { %>
		builder.DOI(ref.getDOI());
		<% } %>
		builder.edition(ref.getEdition());
		builder.ISBN(ref.getIsbnOrIssn());
		builder.ISSN(ref.getIsbnOrIssn());
		builder.keyword(StringUtils.join(ref.getKeywords(), ','));
		builder.language(ref.getLanguage());
		builder.numberOfVolumes(ref.getNumberOfVolumes());
		builder.originalTitle(ref.getOriginalPublication());
		<% if (props.values().contains("pages")) { %>
		builder.page(ref.getPages());
		<% } else { %>
		if (ref.getStartPage() != null && ref.getEndPage() != null) {
			builder.page(ref.getStartPage() + "-" + ref.getEndPage());
		} else if (ref.getStartPage() != null) {
			builder.page(ref.getStartPage());
		} else if (ref.getEndPage() != null) {
			builder.page(ref.getEndPage());
		}
		<% } %>
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
	 * Converts a $desc reference type to a CSL type
	 * @param type the type to convert
	 * @return the converted type (never null, falls back to {@link CSLType#ARTICLE})
	 */
	public CSLType toType(${desc}Type type) {
		switch (type) {
		<% for (t in types) { %>
		case ${toEnum.call(t.key)}:
			return CSLType.${t.value};
		<% } %>
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

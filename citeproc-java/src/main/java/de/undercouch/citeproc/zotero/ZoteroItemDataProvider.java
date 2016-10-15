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

package de.undercouch.citeproc.zotero;

import java.util.LinkedHashSet;
import java.util.Set;

import de.undercouch.citeproc.CSLDateParser;
import de.undercouch.citeproc.ItemDataProvider;
import de.undercouch.citeproc.ListItemDataProvider;
import de.undercouch.citeproc.csl.CSLDate;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CSLItemDataBuilder;
import de.undercouch.citeproc.csl.CSLName;
import de.undercouch.citeproc.helper.StringHelper;

/**
 * A item data provider that translates Zotero IDs to human-readable
 * citations IDs
 * @author Michel Kraemer
 */
public class ZoteroItemDataProvider extends ListItemDataProvider {
	/**
	 * Creates a data provider that copies items from the given provider
	 * but replaces item IDs with human-readable IDs
	 * @param provider the provider to copy the items from
	 */
	public ZoteroItemDataProvider(ItemDataProvider provider) {
		super(sanitizeItems(provider));
	}
	
	/**
	 * Copies all items from the given provider and sanitizes its IDs
	 * @param provider the provider
	 * @return the sanitized items
	 */
	private static CSLItemData[] sanitizeItems(ItemDataProvider provider) {
		Set<String> knownIds = new LinkedHashSet<>();
		
		//create a date parser which will be used to get the item's year
		CSLDateParser dateParser = new CSLDateParser();
		
		//iterate through all items
		String[] ids = provider.getIds();
		CSLItemData[] result = new CSLItemData[ids.length];
		for (int i = 0; i < ids.length; ++i) {
			String id = ids[i];
			CSLItemData item = provider.retrieveItem(id);
			
			//create a new ID
			String newId = makeId(item, dateParser);
			
			//make ID unique
			newId = uniquify(newId, knownIds);
			knownIds.add(newId);
			
			//copy item and replace ID
			item = new CSLItemDataBuilder(item).id(newId).build();
			result[i] = item;
		}
		
		return result;
	}
	
	/**
	 * Generates a human-readable ID for an item
	 * @param item the item
	 * @param dateParser a date parser
	 * @return the human-readable ID
	 */
	private static String makeId(CSLItemData item, CSLDateParser dateParser) {
		if (item.getAuthor() == null || item.getAuthor().length == 0) {
			//there's no author information, return original ID
			return item.getId();
		}
		
		//get author's name
		CSLName firstAuthor = item.getAuthor()[0];
		String a = firstAuthor.getFamily();
		if (a == null || a.isEmpty()) {
			a = firstAuthor.getGiven();
			if (a == null || a.isEmpty()) {
				a = firstAuthor.getLiteral();
				if (a == null || a.isEmpty()) {
					//author could not be found, return original ID
					return item.getId();
				}
			}
		}
		a = StringHelper.sanitize(a);
		
		//try to get year
		int year = getYear(item.getIssued(), dateParser);
		if (year < 0) {
			year = getYear(item.getContainer(), dateParser);
			if (year < 0) {
				year = getYear(item.getOriginalDate(), dateParser);
				if (year < 0) {
					year = getYear(item.getEventDate(), dateParser);
					if (year < 0) {
						year = getYear(item.getSubmitted(), dateParser);
					}
				}
			}
		}
		
		//append year to author
		if (year >= 0) {
			a = a + year;
		}
		
		return a;
	}
	
	/**
	 * Makes the given ID unique
	 * @param id the ID
	 * @param knownIds a set of known IDs to compare to
	 * @return the unique IDs
	 */
	private static String uniquify(String id, Set<String> knownIds) {
		int n = 10;
		String olda = id;
		while (knownIds.contains(id)) {
			id = olda + Integer.toString(n, Character.MAX_RADIX);
			++n;
		}
		return id;
	}
	
	/**
	 * Retrieves the year from a {@link CSLDate} object. Parses the raw
	 * string if necessary.
	 * @param date the date object
	 * @param dateParser a date parser
	 * @return the year or -1 if the year could not be retrieved
	 */
	private static int getYear(CSLDate date, CSLDateParser dateParser) {
		if (date == null) {
			return -1;
		}
		if (date.getDateParts() == null ||
				date.getDateParts().length == 0 ||
				date.getDateParts()[0] == null ||
				date.getDateParts()[0].length == 0) {
			if (date.getRaw() != null && !date.getRaw().isEmpty()) {
				CSLDate d = dateParser.parse(date.getRaw());
				return getYear(d, dateParser);
			}
			return -1;
		}
		return date.getDateParts()[0][0];
	}
}

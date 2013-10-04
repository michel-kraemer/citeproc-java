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

package de.undercouch.citeproc.csl;

import de.undercouch.citeproc.helper.json.JsonBuilder;
import de.undercouch.citeproc.helper.json.JsonObject;

/**
 * A pair containing a {@link CSLCitation}'s citation ID and an note index
 * from a {@link CSLCitation}'s {@link CSLProperties}
 * @author Michel Kraemer
 */
public class CitationIDIndexPair implements JsonObject {
	private final String citationId;
	private final int noteIndex;
	
	/**
	 * Constructs a new pair
	 * @param citationId the citation ID
	 * @param noteIndex the index
	 */
	public CitationIDIndexPair(String citationId, int noteIndex) {
		this.citationId = citationId;
		this.noteIndex = noteIndex;
	}
	
	/**
	 * Constructs a new pair with the values from the given citation object
	 * @param citation the citation object
	 */
	public CitationIDIndexPair(CSLCitation citation) {
		this.citationId = citation.getCitationID();
		this.noteIndex = citation.getProperties().getNoteIndex();
	}
	
	/**
	 * @return the citation ID
	 */
	public String getCitationId() {
		return citationId;
	}
	
	/**
	 * @return the note index
	 */
	public int getNoteIndex() {
		return noteIndex;
	}
	
	@Override
	public Object toJson(JsonBuilder builder) {
		return builder.toJson(new Object[] { citationId, noteIndex });
	}
}

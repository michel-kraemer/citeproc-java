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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An EndNote library
 * @author Michel Kraemer
 */
public class EndNoteLibrary {
	private final List<EndNoteReference> references = new ArrayList<EndNoteReference>();
	
	/**
	 * Adds a reference to this library
	 * @param reference the reference to add
	 */
	public void addReference(EndNoteReference reference) {
		references.add(reference);
	}
	
	/**
	 * @return an unmodifiable list of references in this library
	 */
	public List<EndNoteReference> getReferences() {
		return Collections.unmodifiableList(references);
	}
}

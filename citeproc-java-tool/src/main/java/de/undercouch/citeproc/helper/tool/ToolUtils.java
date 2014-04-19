// Copyright 2014 Michel Kraemer
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

package de.undercouch.citeproc.helper.tool;

import java.util.Collection;

import de.undercouch.citeproc.helper.Levenshtein;

/**
 * Utility methods for the CSL tool
 * @author Michel Kraemer
 */
public class ToolUtils {
	private ToolUtils() {
		//hidden constructor
	}
	
	/**
	 * Finds strings similar to a string the user has entered and then
	 * generates a "Did you mean one of these" message
	 * @param available all possibilities
	 * @param it the string the user has entered
	 * @return the "Did you mean..." string
	 */
	public static String getDidYouMeanString(Collection<String> available, String it) {
		String message = "";
		
		Collection<String> mins = Levenshtein.findSimilar(available, it);
		if (mins.size() > 0) {
			if (mins.size() == 1) {
				message += "Did you mean this?";
			} else {
				message += "Did you mean one of these?";
			}
			for (String m : mins) {
				message += "\n\t" + m;
			}
		}
		
		return message;
	}
}

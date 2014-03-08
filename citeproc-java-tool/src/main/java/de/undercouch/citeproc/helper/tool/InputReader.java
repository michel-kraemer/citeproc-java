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

import java.io.IOException;

/**
 * Reads input from the user
 * @author Michel Kraemer
 */
public interface InputReader {
	/**
	 * Reads a line that the user enters
	 * @param prompt the prompt to display to the user (may be null)
	 * @return the line without line delimiters
	 * @throws IOException if an I/O error occurs
	 */
	String readLine(String prompt) throws IOException;
}

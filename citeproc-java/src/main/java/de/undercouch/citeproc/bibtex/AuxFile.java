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

package de.undercouch.citeproc.bibtex;

import java.util.List;

/**
 * Represents a LaTeX .aux file
 * @author Michel Kraemer
 */
public class AuxFile {
	private final String style;
	private final String input;
	private final List<String> citations;
	
	/**
	 * Creates a new .aux file
	 * @param style the citation style
	 * @param input the input bibliography
	 * @param citations the citations used in the LaTeX document
	 */
	public AuxFile(String style, String input, List<String> citations) {
		this.style = style;
		this.input = input;
		this.citations = citations;
	}
	
	/**
	 * @return the citation style
	 */
	public String getStyle() {
		return style;
	}
	
	/**
	 * @return the input bibliography
	 */
	public String getInput() {
		return input;
	}
	
	/**
	 * @return the citations used in the LaTeX document
	 */
	public List<String> getCitations() {
		return citations;
	}
}

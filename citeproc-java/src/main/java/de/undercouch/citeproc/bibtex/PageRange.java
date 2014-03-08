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

/**
 * A range of pages
 * @author Michel Kraemer
 */
public class PageRange {
	private final String literal;
	private final String pageFirst;
	private final Integer numberOfPages;
	
	/**
	 * Constructs a range of pages
	 * @param literal the string from which this range has been created
	 * @param pageFirst the first page in the range (can be null)
	 * @param numberOfPages the number of pages in this range (can be null)
	 */
	public PageRange(String literal, String pageFirst, Integer numberOfPages) {
		this.literal = literal;
		this.pageFirst = pageFirst;
		this.numberOfPages = numberOfPages;
	}
	/**
	 * @return the string from which this range has been created
	 */
	public String getLiteral() {
		return literal;
	}
	
	/**
	 * @return the first page in the range (can be null)
	 */
	public String getPageFirst() {
		return pageFirst;
	}
	
	/**
	 * @return the number of pages in this range (can be null)
	 */
	public Integer getNumberOfPages() {
		return numberOfPages;
	}
}

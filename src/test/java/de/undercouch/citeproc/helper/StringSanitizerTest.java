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

package de.undercouch.citeproc.helper;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test {@link StringSanitizer}
 * @author Michel Kraemer
 */
public class StringSanitizerTest {
	/**
	 * Tests some simple strings
	 */
	@Test
	public void some() {
		assertEquals("Kramer", StringSanitizer.sanitize("Kr\u00E4mer"));
		assertEquals("Giessen", StringSanitizer.sanitize("Gie\u00dfen"));
		assertEquals("Elsyee", StringSanitizer.sanitize("Elsy\u00e9e"));
		assertEquals("Champs_Elysees", StringSanitizer.sanitize("Champs-\u00c9lys\u00e9es"));
		assertEquals("A_test_with_spaces", StringSanitizer.sanitize("A test with spaces"));
		assertEquals("any_thing_else", StringSanitizer.sanitize("any+thing*else"));
		assertEquals("Numbers_0124", StringSanitizer.sanitize("Numbers 0124"));
	}
}

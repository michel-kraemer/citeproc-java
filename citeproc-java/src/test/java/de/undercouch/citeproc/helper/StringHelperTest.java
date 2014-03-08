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

package de.undercouch.citeproc.helper;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test {@link StringHelper}
 * @author Michel Kraemer
 */
public class StringHelperTest {
	/**
	 * Sanitize some simple strings
	 */
	@Test
	public void sanitize() {
		assertEquals("Kramer", StringHelper.sanitize("Kr\u00E4mer"));
		assertEquals("Giessen", StringHelper.sanitize("Gie\u00dfen"));
		assertEquals("Elsyee", StringHelper.sanitize("Elsy\u00e9e"));
		assertEquals("Champs_Elysees", StringHelper.sanitize("Champs-\u00c9lys\u00e9es"));
		assertEquals("A_test_with_spaces", StringHelper.sanitize("A test with spaces"));
		assertEquals("any_thing_else", StringHelper.sanitize("any+thing*else"));
		assertEquals("Numbers_0124", StringHelper.sanitize("Numbers 0124"));
	}
	
	/**
	 * Tests {@link StringHelper#escapeJava(String)}
	 */
	@Test
	public void escapeJava() {
		assertEquals(null, StringHelper.escapeJava(null));
		assertEscapeJava("", "");
		assertEscapeJava("test", "test");
		assertEscapeJava("\\t", "\t");
		assertEscapeJava("\\\\", "\\");
		assertEscapeJava("'", "'");
		assertEscapeJava("\\\"", "\"");
		assertEscapeJava("/", "/");
		assertEscapeJava("\\\\\\b\\r", "\\\b\r");
		assertEscapeJava("\\u4711", "\u4711");
		assertEscapeJava("\\u0815", "\u0815");
		assertEscapeJava("\\u0080", "\u0080");
		assertEscapeJava("\u007f", "\u007f");
		assertEscapeJava(" ", "\u0020");
		assertEscapeJava("\\u0000", "\u0000");
		assertEscapeJava("\\u001F", "\u001f");
	}
	
	private void assertEscapeJava(String expected, String original) {
		String r = StringHelper.escapeJava(original);
		assertEquals(expected, r);
	}
}

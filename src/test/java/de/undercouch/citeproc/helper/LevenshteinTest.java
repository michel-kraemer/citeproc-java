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

package de.undercouch.citeproc.helper;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * Tests the {@link Levenshtein} implementation
 * @author Michel Kraemer
 */
public class LevenshteinTest {
	/**
	 * Tests some simple distances
	 */
	@Test
	public void distance() {
		assertEquals(0, Levenshtein.distance("hello", "hello"));
		assertEquals(1, Levenshtein.distance("hello", "hell"));
		assertEquals(4, Levenshtein.distance("hello", "world"));
		assertEquals(1, Levenshtein.distance("hello", "hullo"));
	}
	
	/**
	 * Tests the {@link Levenshtein#findMinimum(java.util.Collection, CharSequence)} method
	 */
	@Test
	public void findMinimum() {
		List<String> ss = Arrays.asList("Holla", "World", "Hello", "Hippo", "Hiplo");
		CharSequence min = Levenshtein.findMinimum(ss, "Hillo");
		assertEquals("Hello", min);
	}
}

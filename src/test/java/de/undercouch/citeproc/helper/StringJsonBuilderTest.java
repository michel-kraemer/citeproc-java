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

import org.junit.Test;

/**
 * Tests the JSON builder that creates JSON strings
 * @author Michel Kraemer
 */
public class StringJsonBuilderTest {
	private JsonBuilderFactory factory = new StringJsonBuilderFactory();
	
	/**
	 * Tests if a string array is converted correctly
	 */
	@Test
	public void toJsonStringArray() {
		String[] a = new String[] {
				"a", "b", "c", "That's it"
		};
		assertEquals("[\"a\",\"b\",\"c\",\"That's it\"]",
				factory.createJsonBuilder().toJson(a));
	}
	
	/**
	 * Tests if a JsonObject is converted correctly
	 */
	@Test
	public void toJsonObject() {
		JsonObject obj = new JsonObject() {
			@Override
			public Object toJson(JsonBuilder builder) {
				int[][] g = new int[][] { new int[] { 1, 2 }, new int[] { 3, 4 } };
				builder.add("a", "test");
				builder.add("b", "that's it");
				builder.add("c", "var s = \"Hello\"");
				builder.add("d", true);
				builder.add("e", false);
				builder.add("f", 42);
				builder.add("g", g);
				return builder.build();
			}
		};
		assertEquals("{\"a\":\"test\",\"b\":\"that's it\",\"c\":\"var s = "
				+ "\\\"Hello\\\"\",\"d\":true,\"e\":false,\"f\":42,\"g\":"
				+ "[[1,2],[3,4]]}", obj.toJson(factory.createJsonBuilder()));
	}
}

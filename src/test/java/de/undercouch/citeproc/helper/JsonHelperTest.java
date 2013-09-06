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

import static de.undercouch.citeproc.helper.JsonHelper.toJson;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests the JSON helper
 * @author Michel Kraemer
 */
public class JsonHelperTest {
	/**
	 * Tests if a string is converted correctly
	 */
	@Test
	public void toJsonString() {
		assertEquals("\"test\"", toJson("test"));
		assertEquals("\"that's it\"", toJson("that's it"));
		assertEquals("\"var s = \\\"Hello\\\";\"", toJson("var s = \"Hello\";"));
	}
	
	/**
	 * Tests if a boolean is converted correctly
	 */
	@Test
	public void toJsonBoolean() {
		assertEquals("false", toJson(false));
		assertEquals("true", toJson(true));
	}
	
	/**
	 * Tests if a string array is converted correctly
	 */
	@Test
	public void toJsonStringArray() {
		String[] a = new String[] {
				"a", "b", "c", "That's it"
		};
		assertEquals("[\"a\",\"b\",\"c\",\"That's it\"]", toJson(a));
	}
	
	/**
	 * Tests if a multi-dimensional array is converted correctly
	 */
	@Test
	public void toJsonMultiIntArray() {
		int[][] a = new int[][] { new int[] { 1, 2 }, new int[] { 3, 4 } };
		assertEquals("[[1,2],[3,4]]", toJson(a));
	}
	
	/**
	 * Tests if a JsonObject is converted correctly
	 */
	@Test
	public void toJsonJsonObject() {
		JsonObject obj = new JsonObject() {
			@Override
			public String toJson() {
				return "{\"title\":" + JsonHelper.toJson("that's it") + "}";
			}
		};
		assertEquals("{\"title\":\"that's it\"}", toJson(obj));
	}
}

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

package de.undercouch.citeproc.helper.json;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * Tests the {@link JsonParser}
 * @author Michel Kraemer
 */
public class JsonParserTest {
	/**
	 * Tests if a simple object consisting of several name-value pairs
	 * can be read
	 * @throws IOException if the test failed
	 */
	@Test
	public void simpleObject() throws IOException {
		String obj = "{\"name\":\"value\",\"int\":1302,\"float\":1.57,"
				+ "\"negint\":-5,\"negfloat\":-1.57,\"floatexp\":-1.5e7}";
		JsonLexer l = new JsonLexer(new StringReader(obj));
		JsonParser p = new JsonParser(l);
		Map<String, Object> m = p.parseObject();
		
		assertEquals(6, m.size());
		assertEquals("value", m.get("name"));
		assertEquals(Long.valueOf(1302), m.get("int"));
		assertEquals(Double.valueOf(1.57), m.get("float"));
		assertEquals(Long.valueOf(-5), m.get("negint"));
		assertEquals(Double.valueOf(-1.57), m.get("negfloat"));
		assertEquals(Double.valueOf(-1.5e7), m.get("floatexp"));
	}
	
	/**
	 * Tests if embedded objects and embedded arrays can be read
	 * @throws IOException if the test failed
	 */
	@Test
	public void embedded() throws IOException {
		String obj = "{\"authors\":[\"Ted\", \"Mark\"],\"date\": {\"year\":2013,\"month\":9}}";
		JsonLexer l = new JsonLexer(new StringReader(obj));
		JsonParser p = new JsonParser(l);
		Map<String, Object> m = p.parseObject();
		assertEquals(2, m.size());
		
		@SuppressWarnings("unchecked")
		List<String> authors = (List<String>)m.get("authors");
		assertEquals(2, authors.size());
		assertEquals("Ted", authors.get(0));
		assertEquals("Mark", authors.get(1));
		
		@SuppressWarnings("unchecked")
		Map<String, Object> date = (Map<String, Object>)m.get("date");
		assertEquals(2, date.size());
		assertEquals(Long.valueOf(2013), date.get("year"));
		assertEquals(Long.valueOf(9), date.get("month"));
	}
}

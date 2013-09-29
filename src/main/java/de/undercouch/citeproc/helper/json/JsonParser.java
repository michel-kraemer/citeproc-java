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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.undercouch.citeproc.helper.json.JsonLexer.Type;

/**
 * Parses JSON tokens to maps
 * @author Michel Kraemer
 */
public class JsonParser {
	private final JsonLexer lexer;
	
	/**
	 * Constructs a new JSON parser
	 * @param lexer a JSON lexer to read from
	 */
	public JsonParser(JsonLexer lexer) {
		this.lexer = lexer;
	}
	
	/**
	 * Parses an object into a map
	 * @return the parsed object
	 * @throws IOException if the input stream could not be read or if
	 * the input stream contained an unexpected token
	 */
	public Map<String, Object> parseObject() throws IOException {
		Type t = lexer.readNextToken();
		if (t != Type.START_OBJECT) {
			throw new IOException("Unexpected token: " + t);
		}
		
		return parseObjectInternal();
	}
	
	/**
	 * Parses an object into a map without reading the
	 * {@link Type#START_OBJECT} token
	 * @return the parsed object
	 * @throws IOException if the input stream could not be read or if
	 * the input stream contained an unexpected token
	 */
	private Map<String, Object> parseObjectInternal() throws IOException {
		Map<String, Object> result = new HashMap<String, Object>();
		
		Type t;
		while (true) {
			t = lexer.readNextToken();
			if (t == Type.END_OBJECT) {
				break;
			}
			
			if (!result.isEmpty()) {
				//skip comma and read next token
				if (t != Type.COMMA) {
					throw new IOException("Unexpected token: " + t);
				}
				t = lexer.readNextToken();
			}
			
			//first token must be the name
			if (t != Type.STRING) {
				throw new IOException("Unexpected token: " + t);
			}
			String name = lexer.readString();
			
			//skip colon
			t = lexer.readNextToken();
			if (t != Type.COLON) {
				throw new IOException("Unexpected token: " + t);
			}
			
			//next token must be the value
			t = lexer.readNextToken();
			Object value = readValue(t);
			result.put(name, value);
		}
		
		return result;
	}
	
	/**
	 * Parses an array
	 * @return the parsed array
	 * @throws IOException if the input stream could not be read or if
	 * the input stream contained an unexpected token
	 */
	public List<Object> parseArray() throws IOException {
		Type t = lexer.readNextToken();
		if (t != Type.START_ARRAY) {
			throw new IOException("Unexpected token: " + t);
		}
		
		return parseArrayInternal();
	}
	
	/**
	 * Parses an array without reading the {@link Type#START_ARRAY} token
	 * @return the parsed array
	 * @throws IOException if the input stream could not be read or if
	 * the input stream contained an unexpected token
	 */
	private List<Object> parseArrayInternal() throws IOException {
		List<Object> result = new ArrayList<Object>();
		
		Type t;
		while (true) {
			t = lexer.readNextToken();
			if (t == Type.END_ARRAY) {
				break;
			}
			
			if (!result.isEmpty()) {
				//skip comma and read next token
				if (t != Type.COMMA) {
					throw new IOException("Unexpected token: " + t);
				}
				t = lexer.readNextToken();
			}
			
			//read value
			Object value = readValue(t);
			result.add(value);
		}
		
		return result;
	}
	
	/**
	 * Reads a value for a given type
	 * @param t the type
	 * @return the value
	 * @throws IOException if the input stream could not be read or if
	 * the input stream contained an unexpected token
	 */
	private Object readValue(Type t) throws IOException {
		switch (t) {
		case START_OBJECT:
			return parseObjectInternal();
		
		case START_ARRAY:
			return parseArrayInternal();
		
		case STRING:
			return lexer.readString();
		
		case NUMBER:
			return lexer.readNumber();
		
		case TRUE:
			return true;
		
		case FALSE:
			return false;
		
		case NULL:
			return null;
			
		default:
			throw new IOException("Unexpected token: " + t);
		}
	}
}

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

import java.io.IOException;
import java.io.Reader;

/**
 * A simple JSON lexer
 * @author Michel Kramer
 */
public class JsonLexer {
	/**
	 * Token types
	 */
	public static enum Type {
		/**
		 * The lexer has encountered the start of an object
		 */
		START_OBJECT,
		
		/**
		 * The lexer has encountered the start of an array
		 */
		START_ARRAY,
		
		/**
		 * The lexer has encountered the end of an object
		 */
		END_OBJECT,
		
		/**
		 * The lexer has encountered the end of an array
		 */
		END_ARRAY,
		
		/**
		 * The lexer has encountered a colon (most likely between a name
		 * and a value)
		 */
		COLON,
		
		/**
		 * The lexer has encountered a comma (most likely between a name-value
		 * pairs in objects or values in arrays)
		 */
		COMMA,
		
		/**
		 * The lexer has encountered a string value
		 */
		STRING,
		
		/**
		 * The lexer has encountered a number value
		 */
		NUMBER,
		
		/**
		 * The lexer has encountered a 'true' literal
		 */
		TRUE,
		
		/**
		 * The lexer has encountered a 'false' literal
		 */
		FALSE,
		
		/**
		 * The lexer has encountered a 'null' literal
		 */
		NULL
	}
	
	/**
	 * The reader that provides the JSON to scan
	 */
	private final Reader r;
	
	private int currentCharacter = -1;
	private Type currentTokenType = null;
	
	/**
	 * Creates a new lexer
	 * @param r the reader that provides the JSON to scan
	 */
	public JsonLexer(Reader r) {
		this.r = r;
	}
	
	/**
	 * Reads the next token from the stream
	 * @return the token
	 * @throws IOException if the stream could not be read
	 */
	public Type readNextToken() throws IOException {
		int c;
		if (currentCharacter >= 0 && !Character.isWhitespace(currentCharacter)) {
			//there's still a character left from the last step
			c = currentCharacter;
			currentCharacter = -1;
		} else {
			//skip whitespace characters
			c = skipWhitespace();
		}
		if (c < 0) {
			return null;
		}
		
		//handle character
		if (c =='{') {
			currentTokenType = Type.START_OBJECT;
		} else if (c == '}') {
			currentTokenType = Type.END_OBJECT;
		} else if (c == '[') {
			currentTokenType = Type.START_ARRAY;
		} else if (c == ']') {
			currentTokenType = Type.END_ARRAY;
		} else if (c == ':') {
			currentTokenType = Type.COLON;
		} else if (c == ',') {
			currentTokenType = Type.COMMA;
		} else if (c == '"') {
			currentTokenType = Type.STRING;
		} else if (c == '-' || (c >= '0' && c<= '9')) {
			currentTokenType = Type.NUMBER;
			//the next token is a number. save the last character read because
			//readNumber() will need it.
			currentCharacter = c;
		} else if (c == 't') {
			int c2 = r.read();
			int c3 = r.read();
			int c4 = r.read();
			if (c2 == 'r' && c3 == 'u' & c4 == 'e') {
				currentTokenType = Type.TRUE;
			} else {
				currentTokenType = null;
			}
		} else if (c == 'f') {
			int c2 = r.read();
			int c3 = r.read();
			int c4 = r.read();
			int c5 = r.read();
			if (c2 == 'a' && c3 == 'l' & c4 == 's' && c5 == 'e') {
				currentTokenType = Type.FALSE;
			} else {
				currentTokenType = null;
			}
		} else if (c == 'n') {
			int c2 = r.read();
			int c3 = r.read();
			int c4 = r.read();
			if (c2 == 'u' && c3 == 'l' & c4 == 'l') {
				currentTokenType = Type.NULL;
			} else {
				currentTokenType = null;
			}
		} else {
			currentTokenType = null;
		}
		
		if (currentTokenType == null) {
			throw new IllegalStateException("Unrecognized token: " + c);
		}
		
		return currentTokenType;
	}
	
	/**
	 * Reads characters from the stream until a non-whitespace character
	 * has been found. Reads at least one character.
	 * @return the next non-whitespace character
	 * @throws IOException if the stream could not be read
	 */
	private int skipWhitespace() throws IOException {
		int c = 0;
		do {
			c = r.read();
			if (c < 0) {
				return -1;
			}
		} while (Character.isWhitespace(c));
		
		return c;
	}
	
	/**
	 * Reads a string from the stream
	 * @return the string
	 * @throws IOException if the stream could not be read
	 */
	public String readString() throws IOException {
		StringBuilder result = new StringBuilder();
		while (true) {
			int c = r.read();
			if (c < 0) {
				throw new IllegalStateException("Premature end of stream");
			} else if (c == '"') {
				break;
			} else if (c == '\\') {
				int c2 = r.read();
				if (c2 == '"' || c2 == '\\' || c2 == '/') {
					result.append(c2);
				} else if (c2 == 'b') {
					result.append("\b");
				} else if (c2 == 'f') {
					result.append("\f");
				} else if (c2 == 'n') {
					result.append("\n");
				} else if (c2 == 'r') {
					result.append("\r");
				} else if (c2 == 't') {
					result.append("\t");
				} else if (c2 == 'u') {
					int d1 = r.read();
					int d2 = r.read();
					int d3 = r.read();
					int d4 = r.read();
					checkHexDigit(d1);
					checkHexDigit(d2);
					checkHexDigit(d3);
					checkHexDigit(d4);
					int e = Character.digit(d1, 16);
					e = (e << 4) + Character.digit(d2, 16);
					e = (e << 4) + Character.digit(d3, 16);
					e = (e << 4) + Character.digit(d4, 16);
					result.append((char)e);
				}
			} else {
				result.append((char)c);
			}
		}
		return result.toString();
	}
	
	/**
	 * Checks if the given character is a hexadecimal character
	 * @param c the character
	 * @throws IllegalStateException if the character is not hexadecimal
	 */
	private static void checkHexDigit(int c) {
		if (!Character.isDigit(c) && !(c >= 'a' && c <= 'f') && !(c >= 'A' && c <= 'F')) {
			throw new IllegalStateException("Not a hexadecimal digit: " + c);
		}
	}
	
	/**
	 * Reads a number from the stream
	 * @return the number
	 * @throws IOException if the stream could not be read
	 */
	public Number readNumber() throws IOException {
		//there should be a character left from readNextToken!
		if (currentCharacter < 0) {
			throw new IllegalStateException("Missed first digit");
		}
		
		//read sign
		boolean negative = false;
		if (currentCharacter == '-') {
			negative = true;
			currentCharacter = r.read();
		}
		
		//try to real an integer first
		long result = 0;
		while (currentCharacter >= 0) {
			if (currentCharacter >= '0' && currentCharacter <= '9') {
				result = result * 10 + currentCharacter - '0';
			} else if (currentCharacter == '.') {
				//there is a dot. read real number
				return readReal(result, negative);
			} else {
				break;
			}
			currentCharacter = r.read();
		}
		
		return negative ? -result : result;
	}
	
	/**
	 * Reads a real number from the stream
	 * @param prev the digits read to far
	 * @param negative true if the number is negative
	 * @return the real number
	 * @throws IOException if the stream could not be read
	 */
	private Number readReal(long prev, boolean negative) throws IOException {
		StringBuilder b = new StringBuilder(prev + ".");
		boolean exponent = false;
		boolean expsign = false;
		do {
			currentCharacter = r.read();
			if (currentCharacter >= '0' && currentCharacter <= '9') {
				b.append((char)currentCharacter);
			} else if (currentCharacter == 'e' || currentCharacter == 'E') {
				if (exponent) {
					break;
				}
				b.append((char)currentCharacter);
				exponent = true;
			} else if (currentCharacter == '-' || currentCharacter == '+') {
				if (expsign) {
					break;
				}
				b.append((char)currentCharacter);
				expsign = true;
			} else {
				break;
			}
		} while (currentCharacter >= 0);
		
		double result = Double.parseDouble(b.toString());
		return negative ? -result : result;
	}
}

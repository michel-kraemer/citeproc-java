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

/**
 * Sanitizes strings so they can be used as identifiers
 * @author Michel Kraemer
 */
public class StringSanitizer {
	/**
	 * Sanitizes a string so it can be used as an identifier
	 * @param s the string to sanitize
	 * @return the sanitized string
	 */
	public static String sanitize(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); ++i) {
			char c = s.charAt(i);
			switch (c) {
			case '\u00c0':
			case '\u00c1':
			case '\u00c3':
			case '\u00c4':
				sb.append('A');
				break;
			
			case '\u00c8':
			case '\u00c9':
			case '\u00cb':
				sb.append('E');
				break;
			
			case '\u00cc':
			case '\u00cd':
			case '\u00cf':
				sb.append('I');
				break;
			
			case '\u00d2':
			case '\u00d3':
			case '\u00d5':
			case '\u00d6':
				sb.append('O');
				break;
			
			case '\u00d9':
			case '\u00da':
			case '\u00dc':
				sb.append('U');
				break;
			
			case '\u00e0':
			case '\u00e1':
			case '\u00e3':
			case '\u00e4':
				sb.append('a');
				break;
			
			case '\u00e8':
			case '\u00e9':
			case '\u00eb':
				sb.append('e');
				break;
			
			case '\u00ec':
			case '\u00ed':
			case '\u00ef':
				sb.append('i');
				break;
			
			case '\u00f2':
			case '\u00f3':
			case '\u00f6':
			case '\u00f5':
				sb.append('o');
				break;
			
			case '\u00f9':
			case '\u00fa':
			case '\u00fc':
				sb.append('u');
				break;
			
			case '\u00d1':
				sb.append('N');
				break;
			
			case '\u00f1':
				sb.append('n');
				break;
			
			case '\u010c':
				sb.append('C');
				break;
			
			case '\u0160':
				sb.append('S');
				break;
			
			case '\u017d':
				sb.append('Z');
				break;
			
			case '\u010d':
				sb.append('c');
				break;
			
			case '\u0161':
				sb.append('s');
				break;
			
			case '\u017e':
				sb.append('z');
				break;
			
			case '\u00df':
				sb.append("ss");
				break;
			
			default:
				if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') ||
						(c >= '0' && c <= '9')) {
					sb.append(c);
				} else {
					sb.append('_');
				}
				break;
			}
		}
		return sb.toString();
	}
}

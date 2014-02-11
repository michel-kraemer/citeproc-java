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

package $pkg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Parses $desc library files
 * @author Michel Kraemer
 */
public class $name {
	<%
	def knownLists = props.collect{it.value}.findAll{
		it instanceof List}.collect{it[0]} as Set
	def useSwitch = props.every{it.key.length() == 1}
	def keyLen = props.collect{it.key.length()}.min()
	def keyPos = 0
	if (!firstCharInLine.empty) {
		++keyPos
	}
	%>
	/**
	 * Parses $desc library files
	 * @param r the reader that provides the input to parse
	 * @return the parsed $desc library
	 * @throws IOException if the input could not be read
	 */
	@SuppressWarnings("resource")
	public ${desc}Library parse(Reader r) throws IOException {
		BufferedReader br;
		if (r instanceof BufferedReader) {
			br = (BufferedReader)r;
		} else {
			br = new BufferedReader(r);
		}
		
		${desc}Library result = new ${desc}Library();
		${desc}ReferenceBuilder builder = null;
		<%
		for (kl in knownLists) {
			out << "List<String> ${kl} = new ArrayList<String>();"
		}
		%>
		
		int lc = 0;
		String line;
		while ((line = br.readLine()) != null) {
			++lc;
			line = line.trim();
			<% if (entrySeparator.empty) { %>
			if (line.isEmpty()) {
			<% } else { %>
			if (line.equals("$entrySeparator")) {
			<% } %>
				//end of reference
				handleReference(builder, ${knownLists.join(',')}, result);
				<%
				for (kl in knownLists) {
					out << "${kl}.clear();"
				}
				%>
				builder = null;
				continue;
			}
			
			if (line.length() < ${valuePos + 1}) {
				throw new IOException("Line " + lc + " is too short");
			}
			<% if (!firstCharInLine.empty) { %>
			if (line.charAt(0) != '${firstCharInLine}') {
				throw new IOException("Illegal first character in line " + lc);
			}
			<% } %>
			<% if (Character.isWhitespace(separator.charAt(0))) { %>
			if (!Character.isWhitespace(line.charAt(${separatorPos}))) {
				throw new IOException("Tag and value must be separated by "
						+ "whitespace character in line " + lc);
			}
			<% } else { %>
			if (line.charAt(${separatorPos}) != '${separator}') {
				throw new IOException("Tag and value must be separated by "
						+ "'${separator}' character in line " + lc);
			}
			<% } %>
			
			String key = line.substring($keyPos, ${keyPos + keyLen}).trim();
			String value = line.substring($valuePos).trim();
			
			if (builder == null) {
				builder = new ${desc}ReferenceBuilder();
			}
			
			<% if (useSwitch) { %>
				switch (line.charAt($keyPos)) {
			<% } %>
			<% for (p in props) { %>
				<% if (useSwitch) { %>
					case '${p.key}':
				<% } else { %>
					if (key.equalsIgnoreCase("${p.key}")) {
				<% } %>
				<% if (p.value == 'type') { %>
					builder.type(parseType(value, lc));
				<% } else if (p.value instanceof List) { %>
					${p.value[0]}.add(value);
				<% } else { %>
					builder.${p.value}(value);
				<% } %>
				<% if (useSwitch) { %>
					break;
				<% } else { %>
					} else
				<% } %>
			<% } %>
			<% if (useSwitch) { %>
				default:
			<% } else { %>
				{
			<% } %>
				throw new IOException("Illegal tag " + key +
						" in line " + lc);
			}
		}
		
		handleReference(builder, ${knownLists.join(',')}, result);
		
		return result;
	}
	
	private void handleReference(${desc}ReferenceBuilder builder,
			${knownLists.collect{'List<String> ' + it}.join(',')},
			${desc}Library result) {
		if (builder != null) {
			<% for (kl in knownLists) { %>
			if (!${kl}.isEmpty()) {
				builder.${kl}(${kl}.toArray(new String[${kl}.size()]));
			}
			<% } %>
		
			result.addReference(builder.build());
		}
	}
	
	private ${desc}Type parseType(String value, int lc) throws IOException {
		try {
			return ${desc}Type.fromString(value);
		} catch (IllegalArgumentException e) {
			throw new IOException("Unknown type in line " + lc);
		}
	}
}

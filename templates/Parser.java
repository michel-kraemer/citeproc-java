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
 * Parses $desc files
 * @author Michel Kraemer
 */
public class $name {
	<%
	def knownLists = props.collect{it.value}.findAll{
		it instanceof List}.collect{it[0]} as Set
	%>
	/**
	 * Parses $desc files
	 * @param r the reader that provides the input to parse
	 * @return the parsed $desc
	 * @throws IOException if the input could not be read
	 */
	@SuppressWarnings("resource")
	public $libname parse(Reader r) throws IOException {
		BufferedReader br;
		if (r instanceof BufferedReader) {
			br = (BufferedReader)r;
		} else {
			br = new BufferedReader(r);
		}
		
		$libname result = new $libname();
		${refname}Builder builder = null;
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
			if (line.isEmpty()) {
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
			
			if (line.length() < 3) {
				throw new IOException("Line " + lc + " is too short");
			}
			if (line.charAt(0) != '%') {
				throw new IOException("Illegal first character in line " + lc);
			}
			if (!Character.isWhitespace(line.charAt(2))) {
				throw new IOException("Tag and value must be separated by "
						+ "whitespace in line " + lc);
			}
			
			String value = line.substring(3).trim();
			
			if (builder == null) {
				builder = new ${refname}Builder();
			}
			
			switch (line.charAt(1)) {
			<% for (p in props) { %>
			case '${p.key}':
				<% if (p.value == 'type') { %>
				builder.type(parseType(value, lc));
				<% } else if (p.value instanceof List) { %>
				${p.value[0]}.add(value);
				<% } else { %>
				builder.${p.value}(value);
				<% } %>
				break;
			
			<% } %>
			
			default:
				throw new IOException("Illegal tag " + line.charAt(1) +
						" in line " + lc);
			}
		}
		
		handleReference(builder, ${knownLists.join(',')}, result);
		
		return result;
	}
	
	private void handleReference(${refname}Builder builder,
			${knownLists.collect{'List<String> ' + it}.join(',')},
			$libname result) {
		if (builder != null) {
			<% for (kl in knownLists) { %>
			if (!${kl}.isEmpty()) {
				builder.${kl}(${kl}.toArray(new String[${kl}.size()]));
			}
			<% } %>
		
			result.addReference(builder.build());
		}
	}
	
	private $typename parseType(String value, int lc) throws IOException {
		try {
			return ${typename}.fromString(value);
		} catch (IllegalArgumentException e) {
			throw new IOException("Unknown type in line " + lc);
		}
	}
}

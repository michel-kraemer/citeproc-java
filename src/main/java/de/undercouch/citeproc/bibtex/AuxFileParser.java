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

package de.undercouch.citeproc.bibtex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses LaTeX .aux files
 * @author Michel Kraemer
 */
public class AuxFileParser {
	private static final Pattern CITATION_PATTERN = Pattern.compile("^\\\\citation\\{([^\\}]+)\\}$");
	private static final Pattern BIBSTYLE_PATTERN = Pattern.compile("^\\\\bibstyle\\{([^\\}]+)\\}$");
	private static final Pattern BIBDATA_PATTERN = Pattern.compile("^\\\\bibdata\\{([^\\}]+)\\}$");
	
	/**
	 * Parses a LaTeX .aux file
	 * @param name the file name (either with extension or not)
	 * @return the parsed .aux file
	 * @throws IOException if the file could not be read
	 */
	public static AuxFile parse(String name) throws IOException {
		if (name.toLowerCase().endsWith(".tex")) {
			name = name.substring(0, name.length() - 4);
		}
		if (!name.toLowerCase().endsWith(".aux")) {
			name = name + ".aux";
		}
		
		String style = null;
		String input = null;
		List<String> citations = new ArrayList<String>();
		
		File auxFile = new File(name);
		InputStream is = new FileInputStream(auxFile);
		BufferedReader r = new BufferedReader(new InputStreamReader(is));
		try {
			String line;
			while ((line = r.readLine()) != null) {
				Matcher m;
				
				m = BIBSTYLE_PATTERN.matcher(line);
				if (m.matches()) {
					style = m.group(1);
					continue;
				}
				
				m = BIBDATA_PATTERN.matcher(line);
				if (m.matches()) {
					input = m.group(1);
					continue;
				}
				
				m = CITATION_PATTERN.matcher(line);
				if (m.matches()) {
					citations.add(m.group(1));
					continue;
				}
			}
		} finally {
			r.close();
		}
		
		return new AuxFile(style, input, citations);
	}
}

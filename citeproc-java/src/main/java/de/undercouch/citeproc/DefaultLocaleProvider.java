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

package de.undercouch.citeproc;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import de.undercouch.citeproc.helper.CSLUtils;

/**
 * Default implementation of {@link LocaleProvider}. Loads locales from
 * the classpath.
 * @author Michel Kraemer
 */
public class DefaultLocaleProvider implements LocaleProvider {
	/**
	 * A cache for the serialized XML of locales
	 */
	private Map<String, String> locales = new HashMap<>();
	
	/**
	 * Retrieves the serialized XML for the given locale from the classpath.
	 * For example, if the locale is <code>en-US</code> this method loads
	 * the file <code>/locales-en-US.xml</code> from the classpath.
	 */
	@Override
	public String retrieveLocale(String lang) {
		String r = locales.get(lang);
		if (r == null) {
			try {
				URL u = getClass().getResource("/locales-" + lang + ".xml");
				if (u == null) {
					throw new IllegalArgumentException("Unable to load locale " +
							lang + ". Make sure you have a file called " +
							"'/locales-" + lang + ".xml' at the root of your " +
							"classpath. Did you add the CSL locale files to "
							+ "your classpath?");
				}
				r = CSLUtils.readURLToString(u, "UTF-8");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			locales.put(lang, r);
		}
		return r;
	}
}

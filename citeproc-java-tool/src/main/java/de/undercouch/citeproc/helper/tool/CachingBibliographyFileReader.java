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

package de.undercouch.citeproc.helper.tool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.undercouch.citeproc.BibliographyFileReader;
import de.undercouch.citeproc.ItemDataProvider;

/**
 * Works like {@link BibliographyFileReader} but caches bibliography files
 * in memory. Note that this class only caches files but not input
 * streams, so only {@link #readBibliographyFile(File)} is overridden here.
 * The cache is not automatically cleaned (by some background thread for
 * example), so this is by far no ideal implementation. However, for the
 * citeproc-java tool it's more than enough.
 * @author Michel Kraemer
 */
public class CachingBibliographyFileReader extends BibliographyFileReader {
	private Map<String, SoftReference<ItemDataProvider>> cache = new HashMap<>();
	
	@Override
	public ItemDataProvider readBibliographyFile(File bibfile)
			throws FileNotFoundException, IOException {
		clean();
		
		SoftReference<ItemDataProvider> sr = cache.get(bibfile.getAbsolutePath());
		if (sr != null) {
			ItemDataProvider r = sr.get();
			if (r != null) {
				return r;
			}
		}
		
		ItemDataProvider r = super.readBibliographyFile(bibfile);
		if (r != null) {
			cache.put(bibfile.getAbsolutePath(), new SoftReference<>(r));
		}
		
		return r;
	}
	
	private void clean() {
		Iterator<Map.Entry<String, SoftReference<ItemDataProvider>>> i =
				cache.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry<String, SoftReference<ItemDataProvider>> e = i.next();
			if (e.getValue().get() == null) {
				i.remove();
			}
		}
	}
}

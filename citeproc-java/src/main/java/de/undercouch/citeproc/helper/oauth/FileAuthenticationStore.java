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

package de.undercouch.citeproc.helper.oauth;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Stores an OAuth access token in a file
 * @author Michel Kraemer
 */
public class FileAuthenticationStore implements AuthenticationStore {
	private static final String TOKEN = "token";
	private static final String SECRET = "secret";
	private static final String COMMENT = "citeproc-java authentication store";
	
	private final File store;
	private final Properties p;
	
	/**
	 * Creates a new file-based authentication store
	 * @param store the file to use as authentication store
	 * @throws IOException the the given file could not be read
	 */
	public FileAuthenticationStore(File store) throws IOException {
		this.store = store;
		p = new Properties();
		
		if (store.exists()) {
			try (InputStream is = new FileInputStream(store)) {
				p.load(is);
			}
		}
	}

	@Override
	public String getToken() {
		return p.getProperty(TOKEN);
	}

	@Override
	public String getSecret() {
		return p.getProperty(SECRET);
	}

	@Override
	public void save(String token, String secret) throws IOException {
		if (token == null) {
			throw new IllegalArgumentException("Access token value must not be null");
		}
		if (secret == null) {
			throw new IllegalArgumentException("Access token secret must not be null");
		}
		
		p.setProperty(TOKEN, token);
		p.setProperty(SECRET, secret);
		doSave();
	}

	@Override
	public void reset() throws IOException {
		p.remove(TOKEN);
		p.remove(SECRET);
		doSave();
	}
	
	private void doSave() throws IOException {
		try (OutputStream os = new FileOutputStream(store)) {
			p.store(os, COMMENT);
		}
	}
}

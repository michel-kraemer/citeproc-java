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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the {@link FileAuthenticationStore}
 * @author Michel Kraemer
 */
public class FileAuthenticationStoreTest {
	private File f;
	
	/**
	 * Creates a temporary file used as the authentication store
	 * @throws IOException if the file could not be created
	 */
	@Before
	public void setUp() throws IOException {
		f = File.createTempFile("citeproc-java", "xml");
	}
	
	/**
	 * Deletes the authentication store file
	 */
	@After
	public void tearDown() {
		if (!f.delete()) {
			f.deleteOnExit();
		}
	}
	
	/**
	 * Tests if the authentication store keeps values in memory
	 * @throws IOException if the authentication store could not be created
	 */
	@Test
	public void inMemory() throws IOException {
		AuthenticationStore s = new FileAuthenticationStore(f);
		assertNull(s.getToken());
		assertNull(s.getSecret());
		s.save("TOKEN", "SECRET");
		assertEquals("TOKEN", s.getToken());
		assertEquals("SECRET", s.getSecret());
	}
	
	/**
	 * Tests if the authentication store keeps values on disk
	 * @throws IOException if the authentication store could not be created
	 */
	@Test
	public void onDisk() throws IOException {
		AuthenticationStore s = new FileAuthenticationStore(f);
		assertNull(s.getToken());
		assertNull(s.getSecret());
		s.save("TOKEN", "SECRET");
		
		s = new FileAuthenticationStore(f);
		assertEquals("TOKEN", s.getToken());
		assertEquals("SECRET", s.getSecret());
	}
}

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

package de.undercouch.citeproc.helper.tool.internal;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.helper.json.JsonLexer;
import de.undercouch.citeproc.helper.json.JsonParser;
import de.undercouch.citeproc.helper.json.StringJsonBuilderFactory;
import de.undercouch.citeproc.mendeley.MendeleyConnector;
import de.undercouch.citeproc.mendeley.MendeleyConnectorAdapter;

/**
 * <p>A {@link de.undercouch.citeproc.mendeley.MendeleyConnector} that caches
 * documents read from the server.</p>
 * <p>Note: if MapDB is in the classpath this class uses a disk cache.
 * Otherwise it just caches documents in memory.</p>
 * @author Michel Kraemer
 */
public class CachingMendeleyConnector extends MendeleyConnectorAdapter {
	private final Object _db;
	private final Set<String> _documentIds;
	private final Map<String, String> _documents;
	
	/**
	 * Creates a Mendeley connector that caches documents read from the server
	 * @param delegate the underlying Mendeley connector
	 * @param cacheFile a file used to cache documents
	 */
	@SuppressWarnings("unchecked")
	public CachingMendeleyConnector(MendeleyConnector delegate, File cacheFile) {
		super(delegate);
		
		Object db;
		Set<String> documentIds;
		Map<String, String> documents;
		try {
			//use reflection to get a disk-based cache
			Class<?> dbMakerClass = CachingMendeleyConnector.class
					.getClassLoader().loadClass("org.mapdb.DBMaker");
			
			Method newFileDB = dbMakerClass.getMethod("newFileDB", File.class);
			Object dbMaker = newFileDB.invoke(null, cacheFile);
			Method closeOnJvmShutdown = dbMakerClass.getMethod("closeOnJvmShutdown");
			closeOnJvmShutdown.invoke(dbMaker);
			Method make = dbMakerClass.getMethod("make");
			db = make.invoke(dbMaker);
			
			Class<?> dbClass = db.getClass();
			Method getTreeSet = dbClass.getMethod("getTreeSet", String.class);
			documentIds = (Set<String>)getTreeSet.invoke(db, "documentIds");
			Method getHashMap = dbClass.getMethod("getHashMap", String.class);
			documents = (Map<String, String>)getHashMap.invoke(db, "documents");
		} catch (Exception e) {
			//disk cache is not available. use in-memory cache
			db = null;
			documentIds = new HashSet<String>();
			documents = new HashMap<String, String>();
		}
		
		_db = db;
		_documentIds = documentIds;
		_documents = documents;
	}
	
	@Override
	public List<String> getDocuments() throws IOException {
		if (!_documentIds.isEmpty()) {
			return new ArrayList<String>(_documentIds);
		}
		List<String> ids = super.getDocuments();
		try {
			_documentIds.addAll(ids);
			commit();
		} catch (RuntimeException e) {
			rollback();
			throw e;
		}
		return ids;
	}

	@Override
	public CSLItemData getDocument(String documentId) throws IOException {
		String doc = _documents.get(documentId);
		CSLItemData item;
		if (doc == null) {
			item = super.getDocument(documentId);
			doc = (String)item.toJson(new StringJsonBuilderFactory().createJsonBuilder());
			try {
				_documents.put(documentId, doc);
				commit();
			} catch (RuntimeException e) {
				rollback();
				throw e;
			}
		} else {
			Map<String, Object> m = new JsonParser(
					new JsonLexer(new StringReader(doc))).parseObject();
			item = CSLItemData.fromJson(m);
		}
		return item;
	}
	
	/**
	 * Checks if the cache contains a document with the given ID
	 * @param documentId the document ID
	 * @return true if the cache contains such a document, false otherwise
	 */
	public boolean containsDocumentId(String documentId) {
		return _documents.containsKey(documentId);
	}
	
	/**
	 * @return true if the cache contains a list of document IDs, false
	 * if the cache is empty
	 */
	public boolean hasDocumentList() {
		return !_documentIds.isEmpty();
	}
	
	/**
	 * Clears the cache
	 */
	public void clear() {
		_documentIds.clear();
		_documents.clear();
	}
	
	/**
	 * Commit database transaction. This method is a NOOP if there is no database.
	 */
	private void commit() {
		if (_db == null) {
			return;
		}
		try {
			Method m = _db.getClass().getMethod("commit");
			m.invoke(_db);
		} catch (Exception e) {
			throw new IllegalStateException("Could not commit transaction", e);
		}
	}
	
	/**
	 * Roll back database transaction. This method is a NOOP if there is no database.
	 */
	private void rollback() {
		if (_db == null) {
			return;
		}
		try {
			Method m = _db.getClass().getMethod("rollback");
			m.invoke(_db);
		} catch (Exception e) {
			throw new IllegalStateException("Could not roll back transaction", e);
		}
	}
}

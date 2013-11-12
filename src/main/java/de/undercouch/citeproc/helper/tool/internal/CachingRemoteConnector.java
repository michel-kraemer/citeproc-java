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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.helper.json.JsonLexer;
import de.undercouch.citeproc.helper.json.JsonParser;
import de.undercouch.citeproc.helper.json.StringJsonBuilderFactory;
import de.undercouch.citeproc.remote.RemoteConnector;
import de.undercouch.citeproc.remote.RemoteConnectorAdapter;

/**
 * <p>A {@link de.undercouch.citeproc.remote.RemoteConnector} that caches
 * items read from the server.</p>
 * <p>Note: if MapDB is in the classpath this class uses a disk cache.
 * Otherwise it just caches items in memory.</p>
 * @author Michel Kraemer
 */
public class CachingRemoteConnector extends RemoteConnectorAdapter {
	private final Object _db;
	private final Set<String> _itemIds;
	private final Map<String, String> _items;
	
	/**
	 * Creates a connector that caches items read from the server
	 * @param delegate the underlying connector
	 * @param cacheFile a file used to cache items
	 */
	@SuppressWarnings("unchecked")
	public CachingRemoteConnector(RemoteConnector delegate, File cacheFile) {
		super(delegate);
		
		Object db;
		Set<String> itemIds;
		Map<String, String> items;
		try {
			//use reflection to get a disk-based cache
			Class<?> dbMakerClass = CachingRemoteConnector.class
					.getClassLoader().loadClass("org.mapdb.DBMaker");
			
			Method newFileDB = dbMakerClass.getMethod("newFileDB", File.class);
			Object dbMaker = newFileDB.invoke(null, cacheFile);
			Method closeOnJvmShutdown = dbMakerClass.getMethod("closeOnJvmShutdown");
			closeOnJvmShutdown.invoke(dbMaker);
			Method make = dbMakerClass.getMethod("make");
			db = make.invoke(dbMaker);
			
			Class<?> dbClass = db.getClass();
			Method getTreeSet = dbClass.getMethod("getTreeSet", String.class);
			itemIds = (Set<String>)getTreeSet.invoke(db, "itemIds");
			Method getHashMap = dbClass.getMethod("getHashMap", String.class);
			items = (Map<String, String>)getHashMap.invoke(db, "items");
		} catch (Exception e) {
			//disk cache is not available. use in-memory cache
			db = null;
			itemIds = new HashSet<String>();
			items = new HashMap<String, String>();
		}
		
		_db = db;
		_itemIds = itemIds;
		_items = items;
	}
	
	@Override
	public List<String> getItemIDs() throws IOException {
		if (!_itemIds.isEmpty()) {
			return new ArrayList<String>(_itemIds);
		}
		List<String> ids = super.getItemIDs();
		try {
			_itemIds.addAll(ids);
			commit();
		} catch (RuntimeException e) {
			rollback();
			throw e;
		}
		return ids;
	}

	@Override
	public CSLItemData getItem(String itemId) throws IOException {
		String item = _items.get(itemId);
		CSLItemData itemData;
		if (item == null) {
			itemData = super.getItem(itemId);
			item = (String)itemData.toJson(new StringJsonBuilderFactory().createJsonBuilder());
			try {
				_items.put(itemId, item);
				commit();
			} catch (RuntimeException e) {
				rollback();
				throw e;
			}
		} else {
			Map<String, Object> m = new JsonParser(
					new JsonLexer(new StringReader(item))).parseObject();
			itemData = CSLItemData.fromJson(m);
		}
		return itemData;
	}
	
	@Override
	public Map<String, CSLItemData> getItems(List<String> itemIds) throws IOException {
		Map<String, CSLItemData> result = new LinkedHashMap<String, CSLItemData>(itemIds.size());
		List<String> unknownIds = new ArrayList<String>();
		
		//load items from cache
		for (String id : itemIds) {
			String item = _items.get(id);
			if (item == null) {
				unknownIds.add(id);
			} else {
				Map<String, Object> m = new JsonParser(
						new JsonLexer(new StringReader(item))).parseObject();
				result.put(id, CSLItemData.fromJson(m));
			}
		}
		
		//load items which are not in the cache yet from remote
		if (!unknownIds.isEmpty()) {
			Map<String, CSLItemData> newItems = super.getItems(unknownIds);
			try {
				for (Map.Entry<String, CSLItemData> e : newItems.entrySet()) {
					String s = (String)e.getValue().toJson(
							new StringJsonBuilderFactory().createJsonBuilder());
					_items.put(e.getKey(), s);
				}
				commit();
			} catch (RuntimeException e) {
				rollback();
				throw e;
			}
			result.putAll(newItems);
		}
		
		return result;
	}
	
	/**
	 * Checks if the cache contains an item with the given ID
	 * @param itemId the item ID
	 * @return true if the cache contains such an item, false otherwise
	 */
	public boolean containsItemId(String itemId) {
		return _items.containsKey(itemId);
	}
	
	/**
	 * @return true if the cache contains a list of item IDs, false
	 * if the cache is empty
	 */
	public boolean hasItemList() {
		return !_itemIds.isEmpty();
	}
	
	/**
	 * Clears the cache
	 */
	public void clear() {
		_itemIds.clear();
		_items.clear();
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

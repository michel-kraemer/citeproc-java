// Copyright 2016 Michel Kraemer
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

package de.undercouch.citeproc.script;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.eclipsesource.v8.V8RuntimeException;
import com.eclipsesource.v8.V8Value;

import de.undercouch.citeproc.AbbreviationProvider;
import de.undercouch.citeproc.ItemDataProvider;
import de.undercouch.citeproc.LocaleProvider;
import de.undercouch.citeproc.csl.CSLAbbreviationList;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.helper.json.JsonBuilder;
import de.undercouch.citeproc.helper.json.JsonObject;
import de.undercouch.citeproc.helper.json.StringJsonBuilder;

/**
 * Executes JavaScript using the V8 runtime
 * @author Michel Kraemer
 */
public class V8ScriptRunner extends AbstractScriptRunner {
	/**
	 * The V8 runtime
	 */
	private final V8 runtime;
	
	public V8ScriptRunner() {
		runtime = V8.createV8Runtime();
	}
	
	public void release() {
		runtime.release();
	}
	
	@Override
	public String getName() {
		return "V8";
	}

	@Override
	public String getVersion() {
		return String.valueOf(runtime.getBuildID());
	}

	@Override
	public void eval(Reader reader) throws ScriptRunnerException, IOException {
		//read whole script into memory
		StringBuilder sb = new StringBuilder();
		char[] buf = new char[1024 * 10];
		int read;
		while ((read = reader.read(buf)) >= 0) {
			sb.append(buf, 0, read);
		}

		//execute script
		runtime.executeVoidScript(sb.toString());
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T callMethod(String name, Class<T> resultType, Object... args)
			throws ScriptRunnerException {
		Set<V8Value> newValues = new HashSet<>();
		V8Array parameters = convertArguments(args, newValues);
		try {
			if (String.class.isAssignableFrom(resultType)) {
				return (T)runtime.executeStringFunction(name, parameters);
			}
			return convert(runtime.executeObjectFunction(name, parameters), resultType);
		} catch (V8RuntimeException e) {
			throw new ScriptRunnerException("Could not call method", e);
		} finally {
			newValues.forEach(V8Value::release);
		}
	}

	@Override
	public void callMethod(String name, Object... args)
			throws ScriptRunnerException {
		Set<V8Value> newValues = new HashSet<>();
		V8Array parameters = convertArguments(args, newValues);
		try {
			runtime.executeVoidFunction(name, parameters);
		} catch (V8RuntimeException e) {
			throw new ScriptRunnerException("Could not call method", e);
		} finally {
			newValues.forEach(V8Value::release);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T callMethod(Object obj, String name, Class<T> resultType,
			Object... args) throws ScriptRunnerException {
		Set<V8Value> newValues = new HashSet<>();
		V8Array parameters = convertArguments(args, newValues);
		try {
			V8Object vo = (V8Object)obj;
			if (String.class.isAssignableFrom(resultType)) {
				return (T)vo.executeStringFunction(name, parameters);
			}
			return convert(vo.executeObjectFunction(name, parameters), resultType);
		} catch (V8RuntimeException e) {
			throw new ScriptRunnerException("Could not call method", e);
		} finally {
			newValues.forEach(V8Value::release);
		}
	}

	@Override
	public void callMethod(Object obj, String name, Object... args)
			throws ScriptRunnerException {
		Set<V8Value> newValues = new HashSet<>();
		V8Array parameters = convertArguments(args, newValues);
		try {
			((V8Object)obj).executeVoidFunction(name, parameters);
		} catch (V8RuntimeException e) {
			throw new ScriptRunnerException("Could not call method", e);
		} finally {
			newValues.forEach(V8Value::release);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T convert(Object r, Class<T> resultType) {
		if (List.class.isAssignableFrom(resultType) && r instanceof V8Array) {
			V8Array arr = (V8Array)r;
			r = convertArray(arr);
			arr.release();
		} else if (Map.class.isAssignableFrom(resultType) && r instanceof V8Object) {
			V8Object obj = (V8Object)r;
			r = convertObject(obj);
			obj.release();
		}
		return (T)r;
	}
	
	/**
	 * Recursively convert a V8 array to a list and release it
	 * @param arr the array to convert
	 * @return the list
	 */
	private List<Object> convertArray(V8Array arr) {
		List<Object> l = new ArrayList<>();
		for (int i = 0; i < arr.length(); ++i) {
			Object o = arr.get(i);
			if (o instanceof V8Array) {
				o = convert((V8Array)o, List.class);
			} else if (o instanceof V8Object) {
				o = convert((V8Object)o, Map.class);
			}
			l.add(o);
		}
		return l;
	}
	
	/**
	 * Recursively convert a V8 object to a map and release it
	 * @param obj the object to convert
	 * @return the map
	 */
	private Map<String, Object> convertObject(V8Object obj) {
		if (obj.isUndefined()) {
			return null;
		}
		Map<String, Object> r = new LinkedHashMap<>();
		for (String k : obj.getKeys()) {
			Object o = obj.get(k);
			if (o instanceof V8Array) {
				o = convert((V8Array)o, List.class);
			} else if (o instanceof V8Object) {
				o = convert((V8Object)o, Map.class);
			}
			r.put(k, o);
		}
		return r;
	}

	@Override
	public JsonBuilder createJsonBuilder() {
		return new StringJsonBuilder(this);
	}
	
	/**
	 * Convert an array of object to a V8 array
	 * @param args the array to convert
	 * @param newValues a set that will be filled with all V8 values created
	 * during the operation
	 * @return the V8 array
	 */
	private V8Array convertArguments(Object[] args, Set<V8Value> newValues) {
		//create the array
		V8Array result = new V8Array(runtime);
		newValues.add(result);
		
		//convert the values
		for (int i = 0; i < args.length; ++i) {
			Object o = args[i];
			if (o instanceof JsonObject || o instanceof Collection ||
					o.getClass().isArray() || o instanceof Map) {
				V8Object v = runtime.executeObjectScript("(" +
						createJsonBuilder().toJson(o).toString() + ")");
				newValues.add(v);
				result.push(v);
			} else if (o instanceof String) {
				result.push((String)o);
			} else if (o instanceof Integer) {
				result.push((Integer)o);
			} else if (o instanceof Boolean) {
				result.push((Boolean)o);
			} else if (o instanceof Double) {
				result.push((Double)o);
			} else if (o instanceof ItemDataProvider) {
				o = new ItemDataProviderWrapper((ItemDataProvider)o);
				V8Object v8o = convertJavaObject(o);
				newValues.add(v8o);
				result.push(v8o);
			} else if (o instanceof AbbreviationProvider) {
				o = new AbbreviationProviderWrapper((AbbreviationProvider)o);
				V8Object v8o = convertJavaObject(o);
				newValues.add(v8o);
				result.push(v8o);
			} else if (o instanceof V8ScriptRunner || o instanceof LocaleProvider) {
				V8Object v8o = convertJavaObject(o);
				newValues.add(v8o);
				result.push(v8o);
			} else if (o instanceof V8Value) {
				//already converted
				V8Value v = (V8Value)o;
				result.push(v);
			} else {
				throw new IllegalArgumentException("Unsupported argument: " +
						o.getClass());
			}
		}
		return result;
	}

	/**
	 * Convert a Java object to a V8 object. Register all methods of the
	 * Java object as functions in the created V8 object.
	 * @param o the Java object
	 * @return the V8 object
	 */
	private V8Object convertJavaObject(Object o) {
		V8Object v8o = new V8Object(runtime);
		Method[] methods = o.getClass().getMethods();
		for (Method m : methods) {
			v8o.registerJavaMethod(o, m.getName(), m.getName(),
					m.getParameterTypes());
		}
		return v8o;
	}
	
	/**
	 * Wraps around {@link ItemDataProvider} and converts all retrieved
	 * items to JSON objects
	 * @author Michel Kraemer
	 */
	private class ItemDataProviderWrapper {
		private final ItemDataProvider provider;
		
		public ItemDataProviderWrapper(ItemDataProvider provider) {
			this.provider = provider;
		}
		
		/**
		 * Retrieve an item from the {@link ItemDataProvider} and convert
		 * it to a JSON object
		 * @param id the ID of the item to retrieve
		 * @return the JSON object
		 */
		@SuppressWarnings("unused")
		public Object retrieveItem(String id) {
			CSLItemData item = provider.retrieveItem(id);
			if (item == null) {
				return null;
			}
			return item.toJson(createJsonBuilder());
		}
	}
	
	/**
	 * Wraps around {@link AbbreviationProvider} and converts all retrieved
	 * abbreviation lists to JSON objects
	 * @author Michel Kraemer
	 */
	private class AbbreviationProviderWrapper {
		private final AbbreviationProvider provider;
		
		public AbbreviationProviderWrapper(AbbreviationProvider provider) {
			this.provider = provider;
		}
		
		/**
		 * Retrieve an abbreviation list from the {@link AbbreviationProvider}
		 * and convert it to a JSON object
		 * @param id the name of the list to retrieve
		 * @return the JSON object
		 */
		@SuppressWarnings("unused")
		public Object getAbbreviations(String name) {
			CSLAbbreviationList a = provider.getAbbreviations(name);
			if (a == null) {
				return null;
			}
			return a.toJson(createJsonBuilder());
		}
	}
}

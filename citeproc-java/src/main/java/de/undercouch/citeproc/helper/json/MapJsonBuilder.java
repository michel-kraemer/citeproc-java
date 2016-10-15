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

package de.undercouch.citeproc.helper.json;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A JSON builder that creates maps that represent JSON objects
 * @author Michel Kraemer
 */
public class MapJsonBuilder implements JsonBuilder {
	private final JsonBuilderFactory factory;
	private final Map<String, Object> m;

	/**
	 * Creates a JSON builder
	 * @param factory the factory that created this builder
	 */
	public MapJsonBuilder(JsonBuilderFactory factory) {
		this.factory = factory;
		m = new LinkedHashMap<>();
	}
	
	@Override
	public JsonBuilder add(String name, Object o) {
		m.put(name, toJson(o, factory));
		return this;
	}
	
	@Override
	public Map<String, Object> build() {
		return m;
	}
	
	@Override
	public Object toJson(Object arr) {
		return toJson(arr, factory);
	}
	
	/**
	 * Converts an object to a JSON object
	 * @param obj the object to convert
	 * @param factory a factory used to create JSON builders
	 * @return the JSON object
	 */
	private static Object toJson(Object obj, JsonBuilderFactory factory) {
		if (obj instanceof JsonObject) {
			return ((JsonObject)obj).toJson(factory.createJsonBuilder());
		} else if (obj.getClass().isArray()) {
			List<Object> r = new ArrayList<>();
			int len = Array.getLength(obj);
			for (int i = 0; i < len; ++i) {
				Object ao = Array.get(obj, i);
				r.add(toJson(ao, factory));
			}
			return r;
		} else if (obj instanceof Collection) {
			Collection<?> coll = (Collection<?>)obj;
			List<Object> r = new ArrayList<>();
			for (Object ao : coll) {
				r.add(toJson(ao, factory));
			}
			return r;
		} else if (obj instanceof Map) {
			Map<?, ?> m = (Map<?, ?>)obj;
			Map<String, Object> r = new LinkedHashMap<>();
			for (Map.Entry<?, ?> e : m.entrySet()) {
				String key = toJson(e.getKey(), factory).toString();
				Object value = toJson(e.getValue(), factory);
				r.put(key, value);
			}
			return r;
		}
		return obj;
	}
}

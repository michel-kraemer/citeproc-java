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

package de.undercouch.citeproc.script;

import java.lang.reflect.Array;
import java.util.Map;

import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.TopLevel;

import de.undercouch.citeproc.csl.CSLType;
import de.undercouch.citeproc.helper.json.JsonBuilder;
import de.undercouch.citeproc.helper.json.JsonBuilderFactory;
import de.undercouch.citeproc.helper.json.JsonObject;

/**
 * Creates Rhino JSON objects
 * @author Michel Kraemer
 */
public class RhinoJsonBuilder implements JsonBuilder {
	private final Scriptable scope;
	private final JsonBuilderFactory factory;
	private final Scriptable obj;
	
	/**
	 * Creates a new JSON builder
	 * @param scope the current scope
	 * @param factory the factory that created this object
	 */
	public RhinoJsonBuilder(Scriptable scope, JsonBuilderFactory factory) {
		this.scope = scope;
		this.factory = factory;
		NativeObject no = new NativeObject();
		ScriptRuntime.setBuiltinProtoAndParent(no, scope, TopLevel.Builtins.Object);
		obj = no;
	}
	
	@Override
	public JsonBuilder add(String name, Object o) {
		obj.put(name, obj, toJson(o, scope, factory));
		return this;
	}
	
	/**
	 * Converts an object to a JSON object
	 * @param obj the object to convert
	 * @param scope the current scope
	 * @param factory the factory that creates {@link JsonBuilder} objects
	 * @return the converted object
	 */
	private static Object toJson(Object obj, Scriptable scope, JsonBuilderFactory factory) {
		if (obj instanceof JsonObject) {
			obj = ((JsonObject)obj).toJson(factory.createJsonBuilder());
		} else if (obj.getClass().isArray()) {
			int len = Array.getLength(obj);
			NativeArray na = new NativeArray(len);
			ScriptRuntime.setBuiltinProtoAndParent(na, scope, TopLevel.Builtins.Array);
			for (int i = 0; i < len; ++i) {
				Object ao = Array.get(obj, i);
				na.put(i, na, toJson(ao, scope, factory));
			}
			obj = na;
		} else if (obj instanceof CSLType) {
			obj = obj.toString();
		} else if (obj instanceof Map) {
			Map<?, ?> m = (Map<?, ?>)obj;
			NativeObject no = new NativeObject();
			ScriptRuntime.setBuiltinProtoAndParent(no, scope, TopLevel.Builtins.Object);
			for (Map.Entry<?, ?> e : m.entrySet()) {
				String key = e.getKey().toString();
				no.put(key, no, toJson(e.getValue(), scope, factory));
			}
			obj = no;
		}
		return obj;
	}

	@Override
	public Object build() {
		return obj;
	}

	@Override
	public Object toJson(Object[] arr) {
		int len = arr.length;
		
		NativeArray na = new NativeArray(len);
		ScriptRuntime.setBuiltinProtoAndParent(na, scope, TopLevel.Builtins.Array);
		
		for (int i = 0; i < len; ++i) {
			na.put(i, na, toJson(arr[i], scope, factory));
		}
		
		return na;
	}
}

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

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import de.undercouch.citeproc.csl.CSLType;
import de.undercouch.citeproc.helper.JsonBuilder;
import de.undercouch.citeproc.helper.JsonBuilderFactory;
import de.undercouch.citeproc.helper.JsonObject;

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
		Context ctx = Context.enter();
		try {
			obj = ctx.newObject(scope);
		} finally {
			Context.exit();
		}
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
			Context ctx = Context.enter();
			Scriptable na;
			try {
				na = ctx.newArray(scope, len);
			} finally {
				Context.exit();
			}
			for (int i = 0; i < len; ++i) {
				Object ao = Array.get(obj, i);
				na.put(i, na, toJson(ao, scope, factory));
			}
			obj = na;
		} else if (obj instanceof CSLType) {
			obj = obj.toString();
		}
		return obj;
	}

	@Override
	public Object build() {
		return obj;
	}

	@Override
	public Object toJson(String[] arr) {
		int len = arr.length;
		Context ctx = Context.enter();
		Scriptable na;
		try {
			na = ctx.newArray(scope, len);
		} finally {
			Context.exit();
		}
		
		for (int i = 0; i < len; ++i) {
			na.put(i, na, arr[i]);
		}
		
		return na;
	}
}

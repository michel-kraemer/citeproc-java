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

import static org.apache.commons.lang.StringEscapeUtils.escapeJava;

import java.lang.reflect.Array;

/**
 * A JSON builder that creates JSON strings
 * @author Michel Kraemer
 */
public class StringJsonBuilder implements JsonBuilder {
	private final JsonBuilderFactory factory;
	private final StringBuilder b;
	private int c = 0;

	/**
	 * Creates a JSON builder
	 * @param factory the factory that created this builder
	 */
	public StringJsonBuilder(JsonBuilderFactory factory) {
		this.factory = factory;
		b = new StringBuilder("{");
	}
	
	@Override
	public JsonBuilder add(String name, Object o) {
		if (c > 0) {
			b.append(",");
		}
		++c;
		
		b.append("\"" + name + "\":");
		b.append(toJson(o, factory).toString());
		
		return this;
	}
	
	@Override
	public Object build() {
		b.append("}");
		return b.toString();
	}
	
	@Override
	public Object toJson(Object[] arr) {
		return toJson(arr, factory);
	}
	
	/**
	 * Converts an object to a JSON object. The given object can be a
	 * {@link JsonObject}, a primitive, a string, or an array. Converts
	 * the object to a string via {@link Object#toString()} if its type
	 * is unknown and then converts this string to JSON.
	 * @param obj the object to convert
	 * @param factory a factory used to create JSON builders
	 * @return the JSON object
	 */
	private static Object toJson(Object obj, JsonBuilderFactory factory) {
		if (obj instanceof JsonObject) {
			return toJson((JsonObject)obj, factory);
		} else if (obj instanceof Number) {
			return toJson((Number)obj);
		} else if (obj instanceof Boolean) {
			return toJson(((Boolean)obj).booleanValue());
		} else if (obj.getClass().isArray()) {
			StringBuilder r = new StringBuilder();
			int len = Array.getLength(obj);
			for (int i = 0; i < len; ++i) {
				Object ao = Array.get(obj, i);
				if (r.length() > 0) {
					r.append(",");
				}
				r.append(toJson(ao, factory));
			}
			return "[" + r.toString() + "]";
		}
		return toJson(String.valueOf(obj));
	}
	
	/**
	 * Converts a string to a JSON string. Escapes special characters
	 * if necessary.
	 * @param s the string to convert
	 * @return the JSON string
	 */
	private static String toJson(String s) {
		//we use escapeJava() instead of escapeJavaScript() here because
		//we enclose the string in double quotes ourselves, so single
		//quotes do not have to be escaped (and in fact shouldn't, because
		//Rhino's JSON parser has got problems with this)
		return "\"" + escapeJava(s) + "\"";
	}
	
	/**
	 * Converts a boolean to a JSON string
	 * @param b the boolean to convert
	 * @return the JSON string
	 */
	private static String toJson(boolean b) {
		return String.valueOf(b);
	}
	
	/**
	 * Converts a number to a JSON string
	 * @param n the number to convert
	 * @return the JSON string
	 */
	private static String toJson(Number n) {
		return String.valueOf(n);
	}
	
	/**
	 * Converts a {@link JsonObject} to a JSON string
	 * @param obj the object to convert
	 * @param factory a factory used to create JSON builders
	 * @return the JSON string
	 */
	private static Object toJson(JsonObject obj, JsonBuilderFactory factory) {
		return obj.toJson(factory.createJsonBuilder());
	}
}

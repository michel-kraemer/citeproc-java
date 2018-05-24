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

package $pkg;

import java.util.Arrays;
import java.util.Map;

<% if (!noJsonObject) { %>
import java.util.Collection;

import de.undercouch.citeproc.helper.json.JsonBuilder;
import de.undercouch.citeproc.helper.json.JsonObject;
<% } %>

<% for (i in additionalImports) { %>
import ${i};
<% } %>

/**
 * $description
 * @author Michel Kraemer
 */
public class $name <% if (!noJsonObject) { %>implements JsonObject<% } %> {
	<% for (p in requiredProps) { %>private final ${p.type} ${p.normalizedName};
	<% } %>
	<% for (p in props) { %>private final ${p.type} ${p.normalizedName};
	<% } %>
	
	public $name(<% if (requiredProps.size > 1) { for (p in requiredProps[0..-2]) { %>${p.type} ${p.normalizedName},<% } } %><% if (!requiredProps.empty) { %>
			${toEllipse.call(requiredProps[-1].type)} ${requiredProps[-1].normalizedName}
			<% } %>) {
		<% for (p in requiredProps) { %>this.${p.normalizedName} = ${p.normalizedName};
		<% } %>
		<% for (p in props) { %>this.${p.normalizedName} = <% if (p.defval) { %>${p.defval}<% } else { %>null<% } %>;
		<% } %>
	}
	
	<% if (!props.empty) { %>
	public $name(<% for (p in requiredProps) { %>${p.type} ${p.normalizedName},<% } %>
			${props.collect({ p -> p.type + ' ' + p.normalizedName }).join(',')}) {
		<% for (p in requiredProps) { %>this.${p.normalizedName} = ${p.normalizedName};
		<% } %>
		<% for (p in props) { %>this.${p.normalizedName} = ${p.normalizedName};
		<% } %>
	}
	<% } %>
	
	<% for (p in requiredProps) { %>/**
	 * @return the <% if (shortname) { %>${shortname}'s <% } %>${p.name}
	 */
	public ${p.type} ${toGetter.call(p.normalizedName)}() {
		return ${p.normalizedName};
	}
	<% } %>
	
	<% for (p in props) { %>/**
	 * @return the <% if (shortname) { %>${shortname}'s <% } %>${p.name}
	 */
	public ${p.type} ${toGetter.call(p.normalizedName)}() {
		return ${p.normalizedName};
	}
	<% } %>
	
	<% if (!noJsonObject) { %>
	@Override
	public Object toJson(JsonBuilder builder) {
		<% for (p in requiredProps) { %>builder.add("${p.name}", ${p.normalizedName});<% } %>
		<% for (p in props) { %>if (${p.normalizedName} != null) {
			builder.add("${p.name}", ${p.normalizedName});
		}
		<% } %>
		return builder.build();
	}
	
	/**
	 * Converts a JSON object to a $name object. <% if (!requiredProps.empty) { %>The JSON object must at least contain the following required properties: ${requiredProps.collect({ p -> '<code>' + p.name + '</code>' }).join(',')}<% } %>
	 * @param obj the JSON object to convert
	 * @return the converted $name object
	 */
	@SuppressWarnings("unchecked")
	public static $name fromJson(Map<String, Object> obj) {
		<% for (p in requiredProps) { %>${p.type} ${p.normalizedName};<% } %>

		${additionalFromJsonCodePre.join('\n')}
		
		<%
		def castTemplate = { type, v ->
			def castBefore
			if (type.equals('String')) {
				castBefore = ''
			} else if (type.equals('int') || type.equals('Integer')) {
				castBefore = 'toInt('
			} else if (type.equals('boolean') || type.equals('Boolean')) {
				castBefore = 'toBool('
			} else {
				castBefore = '(' + type + ')'
			}
			
			def castAfter
			if (type.equals('String')) {
				castAfter = '.toString()'
			} else if (type.equals('int') || type.equals('Integer') ||
					type.equals('boolean') || type.equals('Boolean')) {
				castAfter = ')'
			} else {
				castAfter = ''
			}
			
			return castBefore + v + castAfter
		}
		
		def fromJsonTemplate = { type, v ->
			return "${type}.fromJson((Map<String, Object>)$v)"
		}
		
		def constructArrayTemplate = { p ->
			def r = ''
			r += """\
			if (v instanceof Map) {
				v = ((Map<?, ?>)v).values();
			} else if (!(v instanceof Collection)) {
				throw new IllegalArgumentException("`${p.name}' must be an array");
			}
			Collection<?> cv = (Collection<?>)v;
			${p.normalizedName} = """
			if (p.arrayArrayType) {
				r += "new ${p.typeNoArrayNoArray}[cv.size()][];"
			} else {
				r += "new ${p.typeNoArray}[cv.size()];"
			}
			r += """\
			int i = 0;
			for (Object vo : cv) {"""
				if (p.arrayArrayType) {
					r += """\
					if (vo instanceof Map) {
						vo = ((Map<?, ?>)vo).values();
					} else if (!(vo instanceof Collection)) {
						throw new IllegalArgumentException("`${p.name}' must be an array of arrays");
					}
					Collection<?> icv = (Collection<?>)vo;
					${p.normalizedName}[i] = new ${p.typeNoArrayNoArray}[icv.size()];
					int j = 0;
					for (Object ivo : icv) {"""
						if (p.cslType) {
							r += "${p.normalizedName}[i][j] = " + fromJsonTemplate(p.typeNoArrayNoArray, "ivo") + ";"
						} else {
							r += "${p.normalizedName}[i][j] = " + castTemplate(p.typeNoArrayNoArray, "ivo") + ";"
						}
						r += "++j;"
					r += "}"
				} else {
					if (p.cslType) {
						r += """\
						if (!(vo instanceof Map)) {
							throw new IllegalArgumentException("`${p.name}' must be an array of objects");
						}
						${p.normalizedName}[i] = """ + fromJsonTemplate(p.typeNoArray, "vo") + ";"
					} else {
						r += "${p.normalizedName}[i] = " + castTemplate(p.typeNoArray, "vo") + ";"
					}
				}
				r += "++i;"
			r += "}"
			return r
		}
		
		def propertyTemplate = { p, v ->
			def r = ''
			if (!p.required) {
				r += "${p.type} ${p.normalizedName};"
			}
			if (p.enumType) {
				r += "${p.normalizedName} = ${p.type}.fromString(${v}.toString());"
			} else if (p.cslType) {
				if (p.arrayType) {
					r += constructArrayTemplate(p)
				} else {
					r += "if (!($v instanceof Map)) {"
					r += "\tthrow new IllegalArgumentException(\"`${p.name}' must be an object\");"
					r += '}'
					r += "${p.normalizedName} = " + fromJsonTemplate(p.type, v) + ';'
				}
			} else {
				if (p.arrayType) {
					r += constructArrayTemplate(p)
				} else {
					r += "${p.normalizedName} = " + castTemplate(p.type, v) + ';'
				}
			}
			if (!p.required) {
				r += "builder.${p.normalizedName}(${p.normalizedName});"
			}
			return r
		}
		%>
		
		<% for (p in requiredProps) { %>{
			Object v = obj.get("${p.name}");
			if (v == null) {
				throw new IllegalArgumentException("Missing property `${p.name}'");
			}
			<% out << propertyTemplate(p, 'v') %>
		}<% } %>
		
		${name}Builder builder = new ${name}Builder(${requiredProps.collect({ p -> p.normalizedName }).join(',')});
		
		<% for (p in props) { %>{
			Object v = obj.get("${p.name}");
			if (<% if (p.enumType) { %>!isFalsy(v)<% } else { %>v != null<% } %>) {
				<% out << propertyTemplate(p, 'v') %>
			}<% if (p.defval) { %> else {
				builder.${p.normalizedName}(${p.defval});
			}
			<% } %>
		}<% } %>
		
		${additionalFromJsonCode.join('\n')}
		
		return builder.build();
	}

	private static boolean isFalsy(Object o) {
		if (o == null) {
			return true;
		}
		if (Boolean.FALSE.equals(o)) {
			return true;
		}
		if ("".equals(o)) {
			return true;
		}
		if (Integer.valueOf(0).equals(o)) {
			return true;
		}
		if (Long.valueOf(0L).equals(o)) {
			return true;
		}
		if (o instanceof Float && (Float.valueOf(0f).equals(o) || ((Float)o).isNaN())) {
			return true;
		}
		if (o instanceof Double && (Double.valueOf(0d).equals(o) || ((Double)o).isNaN())) {
			return true;
		}
		if (Byte.valueOf((byte)0).equals(o)) {
			return true;
		}
		if (Short.valueOf((short)0).equals(o)) {
			return true;
		}
		return false;
	}
	
	private static int toInt(Object o) {
		if (o instanceof CharSequence) {
			return Integer.parseInt(o.toString());
		}
		return ((Number)o).intValue();
	}
	
	private static boolean toBool(Object o) {
		if (o instanceof String) {
			return Boolean.parseBoolean((String)o);
		} else if (o instanceof Number) {
			return ((Number)o).intValue() != 0;
		}
		return (Boolean)o;
	}
	<% } %>
	
	${additionalMethods.join('\n')}
	
	@Override
	public int hashCode() {
		int result = 1;
		
		<% for (p in props) { %>result = 31 * result + <% if (p.arrayArrayType) { %>Arrays.deepHashCode(${p.normalizedName});<% } else
			if (p.arrayType) { %>Arrays.hashCode(${p.normalizedName});<% } else {
			%>((${p.normalizedName} == null) ? 0 : ${p.normalizedName}.hashCode());<% } %>
		<% } %>
		
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof $name))
			return false;
		$name other = ($name) obj;
		
		<% for (p in props) { %>
			<% if (p.arrayArrayType) { %>
				if (!Arrays.deepEquals(${p.normalizedName}, other.${p.normalizedName}))
					return false;
			<% } else if (p.arrayType) { %>
				if (!Arrays.equals(${p.normalizedName}, other.${p.normalizedName}))
					return false;
			<% } else { %>
				if (${p.normalizedName} == null) {
					if (other.${p.normalizedName} != null)
						return false;
				} else if (!${p.normalizedName}.equals(other.${p.normalizedName}))
					return false;
			<% } %>
		<% } %>
		
		return true;
	}
}

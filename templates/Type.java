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

/**
 * $description
 * @author Michel Kraemer
 */
public enum $name {
	<% out << types.collect({ t->
		toEnum.call(t) + '("' + t+ '")'
	}).join(',') %>;
	
	private String name;
	
	private $name(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	/**
	 * Converts the given string to a $name
	 * @param str the string
	 * @return the converted $name
	 */
	public static $name fromString(String str) {
		<% for (t in types) { %> if (<%
			def r = 'str.equals("' + t + '")'
			if (t.indexOf('-') >= 0) {
				r = r + ' || str.equals("' + t.replace('-', ' ') + '")'
			}
			out << r
		%>) {
			return ${toEnum.call(t)};
		}<% } %>
		throw new IllegalArgumentException("Unknown $name: " + str);
	}
}

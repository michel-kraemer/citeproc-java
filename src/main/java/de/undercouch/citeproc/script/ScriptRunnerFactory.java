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

import org.apache.commons.lang.SystemUtils;

/**
 * Creates {@link ScriptRunner} instances
 * @author Michel Kraemer
 */
public class ScriptRunnerFactory {
	/**
	 * Creates a new {@link ScriptRunner} instance. Checks if Mozilla Rhino
	 * is in the classpath. If so returns a runner that uses this, otherwise
	 * returns a runner that uses the Java Scripting API.
	 * @return the script runner
	 */
	public static ScriptRunner createRunner() {
		try {
			Class.forName("org.mozilla.javascript.Context");
		} catch (ClassNotFoundException e) {
			//Rhino is not available. Check if we have the right JRE version
			if (!SystemUtils.isJavaVersionAtLeast(170)) {
				throw new RuntimeException("You're using a JRE 6 or lower and "
						+ "Mozilla Rhino was not found in the classpath. The "
						+ "bundled Rhino in JRE 6 does not support E4X "
						+ "(ECMAScript for XML) which is needed for "
						+ "citeproc-java. Either include Rhino in your "
						+ "classpath or upgrade to a newer JRE.");
			}
			return new JREScriptRunner();
		}
		
		//use Rhino
		try {
			return (ScriptRunner)Class.forName("de.undercouch.citeproc.script."
					+ "RhinoScriptRunner").newInstance();
		} catch (Exception e) {
			throw new RuntimeException("No JavaScript engine found", e);
		}
	}
}

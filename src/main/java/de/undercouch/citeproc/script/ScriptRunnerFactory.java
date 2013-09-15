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

import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.SystemUtils;

/**
 * Creates {@link ScriptRunner} instances
 * @author Michel Kraemer
 */
public class ScriptRunnerFactory {
	/**
	 * The different types of runners this factory can create
	 */
	public static enum RunnerType {
		/**
		 * Automatically detect if Mozilla Rhino is in the classpath. If so,
		 * create a runner that uses this, otherwise create a runner that
		 * uses the Java Scripting API. (default)
		 */
		AUTO,
		
		/**
		 * Create a runner that explicitly uses the Java Scripting API
		 */
		JRE,
		
		/**
		 * Create a runner that explicitly uses Rhino in the classpath
		 */
		RHINO
	}
	
	/**
	 * The runner to create
	 */
	private static AtomicReference<RunnerType> runner =
			new AtomicReference<RunnerType>(RunnerType.AUTO);
	
	/**
	 * Sets the type of runners this factory creates. The default type is
	 * {@link RunnerType#AUTO}. Normally, you shouldn't have to change this.
	 * Only use this method if you're know what you're doing (for testing
	 * purpose for example)
	 * @param type the type
	 * @return the previous type
	 */
	public static RunnerType setRunnerType(RunnerType type) {
		return runner.getAndSet(type);
	}
	
	/**
	 * @return a {@link ScriptRunner} that uses the Rhino embedded into the JRE
	 */
	private static ScriptRunner createJreRunner() {
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
	
	/**
	 * @return a {@link ScriptRunner} that uses Rhino explicitly
	 */
	private static ScriptRunner createRhinoRunner() {
		try {
			return (ScriptRunner)Class.forName("de.undercouch.citeproc.script."
					+ "RhinoScriptRunner").newInstance();
		} catch (Exception e) {
			throw new RuntimeException("No JavaScript engine found", e);
		}
	}
	
	/**
	 * Creates a new {@link ScriptRunner} instance according to the
	 * type set with {@link #setRunnerType(RunnerType)}
	 * @return the script runner
	 */
	public static ScriptRunner createRunner() {
		RunnerType t = runner.get();
		
		switch (t) {
		case AUTO:
			try {
				Class.forName("org.mozilla.javascript.Context");
			} catch (ClassNotFoundException e) {
				//fall back to JRE
				return createJreRunner();
			}
			
			//use Rhino
			return createRhinoRunner();
			
		case JRE:
			return createJreRunner();
			
		case RHINO:
			return createRhinoRunner();
			
		default:
			throw new RuntimeException("Invalid runner type");
		}
	}
}

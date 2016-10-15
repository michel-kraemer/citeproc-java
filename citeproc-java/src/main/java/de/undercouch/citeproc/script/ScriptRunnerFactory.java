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
		 * Automatically detect the JavaScript engine
		 */
		AUTO,
		
		/**
		 * Create a runner that explicitly uses the Java Scripting API
		 */
		JRE
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
	 * @return a {@link ScriptRunner} that uses the Java Scripting API
	 */
	private static ScriptRunner createJreRunner() {
		return new JREScriptRunner();
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
			//fall back to JRE
			return createJreRunner();
			
		case JRE:
			return createJreRunner();
			
		default:
			throw new RuntimeException("Invalid runner type");
		}
	}
}

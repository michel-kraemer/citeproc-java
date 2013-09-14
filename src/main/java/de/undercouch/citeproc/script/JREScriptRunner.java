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

import java.io.Reader;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Executes JavaScript scripts through the Java Scripting API (using
 * Mozilla Rhino that is bundled with the JRE)
 * @author Michel Kraemer
 */
public class JREScriptRunner extends AbstractScriptRunner {
	private final ScriptEngine engine;
	
	public JREScriptRunner() {
		engine = new ScriptEngineManager().getEngineByName("javascript");
	}
	
	@Override
	public void put(String key, Object value) {
		engine.put(key, value);
	}
	
	@Override
	public Object eval(String code) throws ScriptRunnerException {
		try {
			return engine.eval(code);
		} catch (ScriptException e) {
			throw new ScriptRunnerException("Could not evaluate code", e);
		}
	}
	
	@Override
	public Object eval(Reader reader) throws ScriptRunnerException {
		try {
			return engine.eval(reader);
		} catch (ScriptException e) {
			throw new ScriptRunnerException("Could not evaluate code", e);
		}
	}
}

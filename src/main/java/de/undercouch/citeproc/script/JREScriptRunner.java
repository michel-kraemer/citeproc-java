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
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import de.undercouch.citeproc.helper.json.JsonBuilder;
import de.undercouch.citeproc.helper.json.StringJsonBuilder;

/**
 * Executes JavaScript scripts through the Java Scripting API (using
 * Mozilla Rhino that is bundled with the JRE)
 * @author Michel Kraemer
 */
public class JREScriptRunner extends AbstractScriptRunner {
	private final ScriptEngine engine;
	
	/**
	 * Default constructor
	 */
	public JREScriptRunner() {
		engine = new ScriptEngineManager().getEngineByName("javascript");
	}
	
	@Override
	public String getName() {
		return engine.getFactory().getEngineName();
	}
	
	@Override
	public String getVersion() {
		return engine.getFactory().getEngineVersion();
	}
	
	@Override
	public boolean supportsE4X() {
		//whether we support E4X or not depends on the engine used
		if (engine.getFactory().getEngineName().contains("Rhino")) {
			//Rhino always supports E4X
			return true;
		}
		return false;
	}
	
	@Override
	public void put(String key, Object value) {
		engine.put(key, value);
	}
	
	@Override
	public void eval(String code) throws ScriptRunnerException {
		try {
			engine.eval(code);
		} catch (ScriptException e) {
			throw new ScriptRunnerException("Could not evaluate code", e);
		}
	}
	
	@Override
	public <T> T eval(String code, Class<T> resultType) throws ScriptRunnerException {
		try {
			return convert(engine.eval(code), resultType);
		} catch (ScriptException e) {
			throw new ScriptRunnerException("Could not evaluate code", e);
		}
	}
	
	@Override
	public void eval(Reader reader) throws ScriptRunnerException {
		try {
			engine.eval(reader);
		} catch (ScriptException e) {
			throw new ScriptRunnerException("Could not evaluate code", e);
		}
	}

	@Override
	public JsonBuilder createJsonBuilder() {
		return new StringJsonBuilder(this);
	}
	
	@Override
	public <T> T callMethod(String obj, String name, Class<T> resultType,
			Object... args) throws ScriptRunnerException {
		String p = "";
		if (args != null && args.length > 0) {
			Object[] ca = convertArguments(args);
			StringBuilder b = new StringBuilder();
			for (Object o : ca) {
				if (b.length() > 0) {
					b.append(",");
				}
				b.append(o.toString());
			}
			p = b.toString();
		}
		
		return eval(obj + "." + name + "(" + p + ");", resultType);
	}

	@Override
	public <T> T callMethod(String obj, String name, Class<T> resultType,
			String[] argument) throws ScriptRunnerException {
		Object p = createJsonBuilder().toJson(argument);
		return eval(obj + "." + name + "(" + p + ");", resultType);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T convert(Object r, Class<T> resultType) {
		if (List.class.isAssignableFrom(resultType) &&
				r instanceof Map) {
			r = ((Map<?, ?>)r).values();
		}
		return (T)r;
	}
}

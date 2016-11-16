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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import de.undercouch.citeproc.VariableWrapper;
import de.undercouch.citeproc.VariableWrapperParams;
import de.undercouch.citeproc.helper.json.JsonBuilder;
import de.undercouch.citeproc.helper.json.JsonObject;
import de.undercouch.citeproc.helper.json.StringJsonBuilder;

/**
 * Executes JavaScript scripts through the Java Scripting API
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
	public <T> T callMethod(String name, Class<T> resultType,
			Object... args) throws ScriptRunnerException {
		Invocable i = (Invocable)engine;
		try {
			return convert(i.invokeFunction(name, convertArguments(args)),
					resultType);
		} catch (NoSuchMethodException e) {
			throw new ScriptRunnerException("Could not call method", e);
		} catch (ScriptException e) {
			throw new ScriptRunnerException("Could not call method", e);
		}
	}
	
	@Override
	public void callMethod(String name, Object... args)
			throws ScriptRunnerException {
		Invocable i = (Invocable)engine;
		try {
			i.invokeFunction(name, convertArguments(args));
		} catch (NoSuchMethodException e) {
			throw new ScriptRunnerException("Could not call method", e);
		} catch (ScriptException e) {
			throw new ScriptRunnerException("Could not call method", e);
		}
	}
	
	@Override
	public <T> T callMethod(Object obj, String name, Class<T> resultType,
			Object... args) throws ScriptRunnerException {
		Invocable i = (Invocable)engine;
		try {
			return convert(i.invokeMethod(obj, name, convertArguments(args)),
					resultType);
		} catch (NoSuchMethodException e) {
			throw new ScriptRunnerException("Could not call method", e);
		} catch (ScriptException e) {
			throw new ScriptRunnerException("Could not call method", e);
		}
	}
	
	@Override
	public void callMethod(Object obj, String name, Object... args)
			throws ScriptRunnerException {
		Invocable i = (Invocable)engine;
		try {
			i.invokeMethod(obj, name, convertArguments(args));
		} catch (NoSuchMethodException e) {
			throw new ScriptRunnerException("Could not call method", e);
		} catch (ScriptException e) {
			throw new ScriptRunnerException("Could not call method", e);
		}
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
	
	private Object[] convertArguments(Object[] args) throws ScriptException {
		Object[] result = new Object[args.length];
		for (int i = 0; i < args.length; ++i) {
			Object o = args[i];
			//convert JSON objects, collections, arrays, and maps, but do
			//not convert script objects (such as Bindings)
			if (o == null) {
				result[i] = null;
			} else if (o instanceof JsonObject || o instanceof Collection || o.getClass().isArray() ||
					(o instanceof Map && o.getClass().getPackage().getName().startsWith("java."))) {
				result[i] = engine.eval("(" + createJsonBuilder().toJson(o).toString() + ")");
			} else if (o instanceof VariableWrapper) {
				o = new VariableWrapperWrapper((VariableWrapper)o);
				result[i] = o;
			} else {
				result[i] = o;
			}
		}
		return result;
	}
	
	/**
	 * <p>Wraps around {@link VariableWrapper} and converts
	 * {@link VariableWrapperParams} objects to JSON objects</p>
	 * <p>Note: this class must be public so Nashorn can inspect it and
	 * find the <code>wrap()</code> method.</p>
	 * @author Michel Kraemer
	 */
	public static class VariableWrapperWrapper {
		private final VariableWrapper wrapper;
		
		/**
		 * Creates a new wrapper
		 * @param wrapper the variable wrapper to wrap around
		 */
		public VariableWrapperWrapper(VariableWrapper wrapper) {
			this.wrapper = wrapper;
		}
		
		/**
		 * Call the {@link VariableWrapper} with the given parameters
		 * @param params the context in which an item should be rendered
		 * @param prePunct the text that precedes the item to render
		 * @param str the item to render
		 * @param postPunct the text that follows the item to render
		 * @return the string to be rendered
		 */
		public String wrap(Map<String, Object> params, String prePunct,
				String str, String postPunct) {
			VariableWrapperParams p = VariableWrapperParams.fromJson(params);
			return wrapper.wrap(p, prePunct, str, postPunct);
		}
	}
}

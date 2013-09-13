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

import java.io.IOException;
import java.io.Reader;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Wrapper;

/**
 * Executes JavaScript scripts using Mozilla Rhino
 * @author Michel Kraemer
 */
public class RhinoScriptRunner implements ScriptRunner {
	private final Scriptable scope;
	
	public RhinoScriptRunner() {
		Context context = Context.enter();
		try {
			scope = context.initStandardObjects();
		} finally {
			Context.exit();
		}
	}

	@Override
	public void put(String key, Object value) {
		scope.put(key, scope, value);
	}

	@Override
	public Object eval(String code) throws ScriptRunnerException {
		Context context = Context.enter();
		try {
			return unwrap(context.evaluateString(scope, code, "<code>", 1, null));
		} catch (RhinoException e) {
			throw new ScriptRunnerException("Could not execute code", e);
		} finally {
			Context.exit();
		}
	}

	@Override
	public Object eval(Reader reader) throws ScriptRunnerException, IOException {
		Context context = Context.enter();
		try {
			return unwrap(context.evaluateReader(scope, reader, "<code>", 1, null));
		} catch (RhinoException e) {
			throw new ScriptRunnerException("Could not execute script", e);
		} finally {
			Context.exit();
		}
	}
	
	/**
	 * Unwraps an object
	 * @param o the object
	 * @return the unwrapped object
	 */
	private static Object unwrap(Object o) {
		if (o instanceof Wrapper) {
			return ((Wrapper)o).unwrap();
		}
		return o;
	}
}	

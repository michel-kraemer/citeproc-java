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
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.GeneratedClassLoader;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.SecurityController;
import org.mozilla.javascript.Wrapper;

import de.undercouch.citeproc.helper.CSLUtils;
import de.undercouch.citeproc.helper.json.JsonBuilder;

/**
 * Executes JavaScript scripts using Mozilla Rhino
 * @author Michel Kraemer
 */
public class RhinoScriptRunner extends AbstractScriptRunner {
	private static Map<URL, Script> compiledScripts =
			new ConcurrentHashMap<URL, Script>();
	
	private final Scriptable scope;
	
	/**
	 * Default constructor
	 */
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
	
	/**
	 * @return true if Rhino's version is exactly 1.7R4, false if it's not
	 * or if it cannot be determined
	 */
	private boolean isRhino17R4() {
		try {
			URL u = Script.class.getProtectionDomain().getCodeSource().getLocation();
			String jar = u.getFile();
			jar = jar.substring(jar.lastIndexOf('/'));
			return jar.contains("1.7R4");
		} catch (Throwable e) {
			//if anything goes wrong, just suppose it's not 1.7R4
			return false;
		}
	}
	
	@Override
	public void loadScript(URL url) throws IOException, ScriptRunnerException {
		//try to load a previously compiled script
		Script s = compiledScripts.get(url);
		if (s != null) {
			Context context = Context.enter();
			try {
				s.exec(context, scope);
				return;
			} finally {
				Context.exit();
			}
		}
		
		//try to load a precompiled script from a file (only if
		//we're using Rhino 1.7R4 as the precompile scripts are
		//only compatible to this version)
		if (url.getPath().endsWith(".js") && isRhino17R4()) {
			String name = url.getPath().substring(0, url.getPath().length() - 3);
			name = name.substring(name.lastIndexOf('/') + 1);
			name = name + ".dat";
			URL fileUrl = new URL(url, name);
			if (fileUrl != null) {
				Context context = Context.enter();
				try {
					byte[] data = CSLUtils.readURL(fileUrl);
				
					GeneratedClassLoader loader = SecurityController.createLoader(
							context.getApplicationClassLoader(), null);
					Class<?> clazz = loader.defineClass(null, data);
					loader.linkClass(clazz);
					
					s = (Script)clazz.newInstance();
					
					//cache compile script
					compiledScripts.put(url, s);
					
					s.exec(context, scope);
					return;
				} catch (InstantiationException e) {
					//ignore. fall through to normal script evaluation.
				} catch (IllegalAccessException e) {
					//ignore. fall through to normal script evaluation.
				} finally {
					Context.exit();
				}
			}
		}
		
		//evaluate script without compiling
		super.loadScript(url);
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

	@Override
	public JsonBuilder createJsonBuilder() {
		return new RhinoJsonBuilder(scope, this);
	}
	
	@Override
	public Object callMethod(String obj, String name, Object... args) throws ScriptRunnerException {
		try {
			Object p[] = convertArguments(args);
			ScriptableObject so = (ScriptableObject)scope.get(obj, scope);
			return ScriptableObject.callMethod(so, name, p);
		} catch (RhinoException e) {
			throw new ScriptRunnerException("Could not call method", e);
		}
	}

	@Override
	public Object callMethod(String obj, String name, String[] argument)
			throws ScriptRunnerException {
		try {
			Object p = createJsonBuilder().toJson(argument);
			ScriptableObject so = (ScriptableObject)scope.get(obj, scope);
			return ScriptableObject.callMethod(so, name, new Object[] { p });
		} catch (RhinoException e) {
			throw new ScriptRunnerException("Could not call method", e);
		}
	}
}

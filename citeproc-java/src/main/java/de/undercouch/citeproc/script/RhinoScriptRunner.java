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

import org.mozilla.javascript.ClassCache;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.GeneratedClassLoader;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.SecurityController;

import de.undercouch.citeproc.helper.CSLUtils;
import de.undercouch.citeproc.helper.json.JsonBuilder;

/**
 * Executes JavaScript scripts using Mozilla Rhino
 * @author Michel Kraemer
 */
public class RhinoScriptRunner extends AbstractScriptRunner {
	private static Map<String, Script> compiledScripts =
			new ConcurrentHashMap<String, Script>();

	private final RhinoScriptRunnerPreferences preferences;
	private final Scriptable scope;

	public RhinoScriptRunner(RhinoScriptRunnerPreferences preferences) {
		this.preferences = preferences;

		Context context = enterContext();
		try {
			scope = context.initStandardObjects();
			ClassCache classCache = ClassCache.get(scope);
			classCache.setCachingEnabled(this.preferences.isEnableClassCache());
		} finally {
			Context.exit();
		}
	}
	
	@Override
	public String getName() {
		return "Mozilla Rhino";
	}
	
	@Override
	public String getVersion() {
		Context context = enterContext();
		try {
			String r = context.getImplementationVersion();
			if (r.startsWith("Rhino")) {
				r = r.substring(5);
			}
			return r.trim();
		} finally {
			Context.exit();
		}
	}
	
	@Override
	public boolean supportsE4X() {
		//Rhino always supports E4X
		return true;
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
		String ustr = url.toString();
		Script s = compiledScripts.get(ustr);
		if (s != null) {
			Context context = enterContext();
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
				Context context = enterContext();
				try {
					byte[] data = CSLUtils.readURL(fileUrl);
				
					GeneratedClassLoader loader = SecurityController.createLoader(
							context.getApplicationClassLoader(), null);
					Class<?> clazz = loader.defineClass(null, data);
					loader.linkClass(clazz);
					
					s = (Script)clazz.newInstance();
					
					//cache compile script
					compiledScripts.put(ustr, s);
					
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
	public void eval(Reader reader) throws ScriptRunnerException, IOException {
		Context context = enterContext();
		try {
			context.evaluateReader(scope, reader, "<code>", 1, null);
		} catch (RhinoException e) {
			throw new ScriptRunnerException("Could not execute script", e);
		} finally {
			Context.exit();
		}
	}
	
	@Override
	public JsonBuilder createJsonBuilder() {
		return new RhinoJsonBuilder(scope, this);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T callMethod(String name, Class<T> resultType, Object... args)
			throws ScriptRunnerException {
		Context context = enterContext();
		try {
			Function f = (Function)ScriptableObject.getProperty(scope, name);
			return (T)f.call(context, scope, null, convertArguments(args));
		} catch (RhinoException e) {
			throw new ScriptRunnerException("Could not call method", e);
		} finally {
			Context.exit();
		}
	}
	
	@Override
	public void callMethod(String name, Object... args)
			throws ScriptRunnerException {
		Context context = enterContext();
		try {
			Function f = (Function)ScriptableObject.getProperty(scope, name);
			f.call(context, scope, null, convertArguments(args));
		} catch (RhinoException e) {
			throw new ScriptRunnerException("Could not call method", e);
		} finally {
			Context.exit();
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T callMethod(Object obj, String name, Class<T> resultType,
			Object... args) throws ScriptRunnerException {
		Context context = enterContext();
		try {
			Scriptable s = (Scriptable)obj;
			Function f = (Function)ScriptableObject.getProperty(s, name);
			return (T)f.call(context, scope, s, convertArguments(args));
		} catch (RhinoException e) {
			throw new ScriptRunnerException("Could not call method", e);
		} finally {
			Context.exit();
		}
	}
	
	@Override
	public void callMethod(Object obj, String name, Object... args)
			throws ScriptRunnerException {
		Context context = enterContext();
		try {
			Scriptable s = (Scriptable)obj;
			Function f = (Function)ScriptableObject.getProperty(s, name);
			f.call(context, scope, s, convertArguments(args));
		} catch (RhinoException e) {
			throw new ScriptRunnerException("Could not call method", e);
		} finally {
			Context.exit();
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T convert(Object o, Class<T> type) {
		return (T)o;
	}
	
	/**
	 * Recursively converts the given list of arguments using
	 * {@link #createJsonBuilder()} and {@link JsonBuilder#toJson(Object)}
	 * @param args the arguments to convert
	 * @return the converted arguments
	 */
	private Object[] convertArguments(Object[] args) {
		Object[] result = new Object[args.length];
		for (int i = 0; i < args.length; ++i) {
			Object o = args[i];
			if (!(o instanceof ScriptableObject)) {
				result[i] = createJsonBuilder().toJson(args[i]);
			} else {
				result[i] = o;
			}
		}
		return result;
	}

	private Context enterContext() {
		Context context = Context.enter();
		context.setOptimizationLevel(this.preferences.getCompilerOptimizationLevel());
		return context;
	}
}

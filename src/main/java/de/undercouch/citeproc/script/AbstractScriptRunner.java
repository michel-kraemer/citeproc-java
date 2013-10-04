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
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.Collection;

import de.undercouch.citeproc.helper.json.JsonObject;

/**
 * Abstract base class for {@link ScriptRunner} implementations
 * @author Michel Kraemer
 */
public abstract class AbstractScriptRunner implements ScriptRunner {
	@Override
	public void loadScript(URL url) throws IOException, ScriptRunnerException {
		InputStreamReader reader = new InputStreamReader(url.openStream());
		try {
			eval(reader);
		} finally {
			reader.close();
		}
	}
	
	/**
	 * Recursively converts the given list of arguments using
	 * {@link JsonObject#toJson(de.undercouch.citeproc.helper.json.JsonBuilder)}
	 * and {@link #createJsonBuilder()}
	 * @param args the arguments to convert
	 * @return the converted arguments
	 */
	protected Object[] convertArguments(Object[] args) {
		Object[] result = new Object[args.length];
		for (int i = 0; i < args.length; ++i) {
			Object a = args[i];
			if (a instanceof JsonObject) {
				result[i] = ((JsonObject)a).toJson(createJsonBuilder());
			} else if (a instanceof Collection) {
				Collection<?> coll = (Collection<?>)a;
				result[i] = createJsonBuilder().toJson(coll.toArray(new Object[coll.size()]));
			} else if (a.getClass().isArray()) {
				Object[] ao = new Object[Array.getLength(a)];
				for (int j = 0; j < ao.length; ++j) {
					ao[j] = Array.get(a, j);
				}
				result[i] = createJsonBuilder().toJson(ao);
			} else {
				result[i] = a;
			}
		}
		return result;
	}
}

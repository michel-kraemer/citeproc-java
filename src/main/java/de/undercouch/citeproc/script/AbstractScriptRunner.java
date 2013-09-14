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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import de.undercouch.citeproc.helper.JsonObject;

/**
 * Abstract base class for {@link ScriptRunner} implementations
 * @author Michel Kraemer
 */
public abstract class AbstractScriptRunner implements ScriptRunner {
	@Override
	public void loadScript(String filename) throws IOException, ScriptRunnerException {
		URL citeProcURL = getClass().getResource(filename);
		if (citeProcURL == null) {
			throw new FileNotFoundException("Could not find " + filename + " in classpath");
		}
		
		InputStreamReader reader = new InputStreamReader(citeProcURL.openStream());
		try {
			eval(reader);
		} finally {
			reader.close();
		}
	}
	
	@Override
	public Object callMethod(String obj, String name, JsonObject... args) throws ScriptRunnerException {
		String p = "";
		if (args != null && args.length > 0) {
			if (args.length == 1) {
				p = args[0].toJson(createJsonBuilder()).toString();
			} else {
				StringBuilder b = new StringBuilder();
				for (JsonObject o : args) {
					if (b.length() > 0) {
						b.append(",");
					}
					b.append(o.toJson(createJsonBuilder()).toString());
				}
				p = b.toString();
			}
		}
		return eval(obj + "." + name + "(" + p + ");");
	}

	@Override
	public Object callMethod(String obj, String name, String... args)
			throws ScriptRunnerException {
		return eval(obj + "." + name + "(" + createJsonBuilder().toJson(args) + ");");
	}
}

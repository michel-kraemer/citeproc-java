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

import de.undercouch.citeproc.helper.JsonBuilderFactory;
import de.undercouch.citeproc.helper.JsonObject;

/**
 * Executes JavaScript scripts
 * @author Michel Kraemer
 */
public interface ScriptRunner extends JsonBuilderFactory {
	/**
	 * Sets a key/value pair in the runner's global scope
	 * @param key the key
	 * @param value the value
	 */
	void put(String key, Object value);
	
	/**
	 * Loads a script from the classpath and evaluates it
	 * @param filename the script's filename
	 * @throws IOException if the script could not be loaded
	 * @throws ScriptRunnerException if the script is invalid
	 */
	public void loadScript(String filename) throws IOException, ScriptRunnerException;
	
	/**
	 * Executes the given code
	 * @param code the code
	 * @return the return value from the executed script
	 * @throws ScriptRunnerException if the given code could not be executed
	 */
	Object eval(String code) throws ScriptRunnerException;
	
	/**
	 * Executes a script provided by a given reader
	 * @param reader the reader
	 * @return the return value from the executed script
	 * @throws ScriptRunnerException if the script could not be executed
	 * @throws IOException if the script could not be read from the reader
	 */
	Object eval(Reader reader) throws ScriptRunnerException, IOException;
	
	/**
	 * Calls an object's method
	 * @param obj the object's name
	 * @param name the method's name
	 * @param args the arguments
	 * @return the return value
	 * @throws ScriptRunnerException if the method could not be called
	 */
	Object callMethod(String obj, String name, JsonObject... args) throws ScriptRunnerException;
	
	/**
	 * Calls an object's method
	 * @param obj the object's name
	 * @param name the method's name
	 * @param args the arguments
	 * @return the return value
	 * @throws ScriptRunnerException if the method could not be called
	 */
	Object callMethod(String obj, String name, String... args) throws ScriptRunnerException;
}

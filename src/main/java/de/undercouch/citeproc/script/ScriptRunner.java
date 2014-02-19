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

import de.undercouch.citeproc.helper.json.JsonBuilderFactory;

/**
 * Executes JavaScript scripts
 * @author Michel Kraemer
 */
public interface ScriptRunner extends JsonBuilderFactory {
	/**
	 * @return the runner's name
	 */
	String getName();
	
	/**
	 * @return the runner's version
	 */
	String getVersion();
	
	/**
	 * @return true if this script runner supports the
	 * ECMAScript for XML extension (E4X)
	 */
	boolean supportsE4X();
	
	/**
	 * Sets a key/value pair in the runner's global scope
	 * @param key the key
	 * @param value the value
	 */
	void put(String key, Object value);
	
	/**
	 * Loads a script from a URL and evaluates it
	 * @param url the script's URL
	 * @throws IOException if the script could not be loaded
	 * @throws ScriptRunnerException if the script is invalid
	 */
	void loadScript(URL url) throws IOException, ScriptRunnerException;
	
	/**
	 * Executes the given code
	 * @param code the code
	 * @throws ScriptRunnerException if the given code could not be executed
	 */
	void eval(String code) throws ScriptRunnerException;
	
	/**
	 * Executes the given code
	 * @param <T> the type of the return value
	 * @param code the code
	 * @param resultType the expected type of the return value
	 * @return the return value from the executed script
	 * @throws ScriptRunnerException if the given code could not be executed
	 */
	<T> T eval(String code, Class<T> resultType) throws ScriptRunnerException;
	
	/**
	 * Executes a script provided by a given reader
	 * @param reader the reader
	 * @throws ScriptRunnerException if the script could not be executed
	 * @throws IOException if the script could not be read from the reader
	 */
	void eval(Reader reader) throws ScriptRunnerException, IOException;
	
	/**
	 * Calls an object's method
	 * @param <T> the type of the return value
	 * @param obj the object's name
	 * @param name the method's name
	 * @param resultType the expected type of the return value
	 * @param args the arguments
	 * @return the return value
	 * @throws ScriptRunnerException if the method could not be called
	 */
	<T> T callMethod(String obj, String name, Class<T> resultType, Object... args)
			throws ScriptRunnerException;
	
	/**
	 * Calls an object's method
	 * @param <T> the type of the return value
	 * @param obj the object's name
	 * @param name the method's name
	 * @param resultType the expected type of the return value
	 * @param argument a string array that will be passed as the method's first argument
	 * @return the return value
	 * @throws ScriptRunnerException if the method could not be called
	 */
	<T> T callMethod(String obj, String name, Class<T> resultType, String[] argument)
			throws ScriptRunnerException;
	
	/**
	 * Tries to convert the given object to the given type
	 * @param <T> the type to convert to
	 * @param o the object to convert
	 * @param type the type to convert to
	 * @return the converted object
	 */
	<T> T convert(Object o, Class<T> type);
}

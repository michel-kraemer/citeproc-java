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

package de.undercouch.citeproc.tool;

import java.io.File;

import de.undercouch.citeproc.BibliographyFileReader;
import de.undercouch.citeproc.helper.tool.CachingBibliographyFileReader;

/**
 * A context containing information used during execution of
 * the {@link de.undercouch.citeproc.CSLTool}
 * @author Michel Kraemer
 */
public class CSLToolContext {
	private static ThreadLocal<CSLToolContext> current = new ThreadLocal<CSLToolContext>();
	private String toolName;
	private File configDir;
	private BibliographyFileReader bibReader = new CachingBibliographyFileReader();
	
	private CSLToolContext() {
		//hidden constructor
	}
	
	/**
	 * Enters a new context
	 * @return the new context
	 */
	public static CSLToolContext enter() {
		CSLToolContext ctx = new CSLToolContext();
		current.set(ctx);
		return ctx;
	}
	
	/**
	 * Leaves the current context
	 */
	public static void exit() {
		current.remove();
	}
	
	/**
	 * @return the current context
	 */
	public static CSLToolContext current() {
		return current.get();
	}
	
	/**
	 * Sets the tool's name
	 * @param toolName the name
	 */
	public void setToolName(String toolName) {
		this.toolName = toolName;
	}
	
	/**
	 * @return the tool's name
	 */
	public String getToolName() {
		return toolName;
	}
	
	/**
	 * Sets the tool's configuration directory
	 * @param configDir the directory
	 */
	public void setConfigDir(File configDir) {
		this.configDir = configDir;
	}
	
	/**
	 * @return the tool's configuration directory
	 */
	protected File getConfigDir() {
		return configDir;
	}
	
	/**
	 * Returns a common reader for bibliography files. Use this method
	 * instead of creating a new {@link BibliographyFileReader} instance
	 * to enable caching.
	 * @return the reader
	 */
	public BibliographyFileReader getBibliographyFileReader() {
		return bibReader;
	}
}

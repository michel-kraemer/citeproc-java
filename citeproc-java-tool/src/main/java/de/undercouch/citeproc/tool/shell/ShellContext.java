// Copyright 2014 Michel Kraemer
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

package de.undercouch.citeproc.tool.shell;

/**
 * A context containing variables that affect the operation of the
 * interactive shell
 * @author Michel Kraemer
 */
public class ShellContext {
	private static ThreadLocal<ShellContext> current = new ThreadLocal<ShellContext>();
	private String style = "ieee";
	private String locale = "en-US";
	private String format = "text";
	private String file;
	
	private ShellContext() {
		//hidden constructor
	}
	
	/**
	 * Enters a new context
	 * @return the new context
	 */
	public static ShellContext enter() {
		ShellContext ctx = new ShellContext();
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
	public static ShellContext current() {
		return current.get();
	}
	
	/**
	 * Sets the current citation style
	 * @param style the style
	 */
	public void setStyle(String style) {
		this.style = style;
	}
	
	/**
	 * @return the current citation style
	 */
	public String getStyle() {
		return style;
	}
	
	/**
	 * Sets the current locale
	 * @param locale the locale
	 */
	public void setLocale(String locale) {
		this.locale = locale;
	}
	
	/**
	 * @return the current locale
	 */
	public String getLocale() {
		return locale;
	}
	
	/**
	 * Sets the current output format
	 * @param format the format
	 */
	public void setFormat(String format) {
		this.format = format;
	}
	
	/**
	 * @return the current output format
	 */
	public String getFormat() {
		return format;
	}
	
	/**
	 * Sets the current input file
	 * @param file the file
	 */
	public void setInputFile(String file) {
		this.file = file;
	}
	
	/**
	 * @return the current input file
	 */
	public String getInputFile() {
		return file;
	}
}

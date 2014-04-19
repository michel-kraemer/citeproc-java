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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import de.undercouch.citeproc.helper.tool.InputReader;
import de.undercouch.citeproc.helper.tool.OptionParserException;
import de.undercouch.citeproc.helper.tool.UnknownAttributes;
import de.undercouch.citeproc.tool.AbstractCSLToolCommand;

/**
 * Set the current citation locale
 * @author Michel Kraemer
 */
public class ShellSetLocaleCommand extends AbstractCSLToolCommand {
	/**
	 * The current locales
	 */
	private List<String> locales;
	
	@Override
	public String getUsageName() {
		return "set locale";
	}
	
	@Override
	public String getUsageDescription() {
		return "Set the current citation locale";
	}
	
	/**
	 * Sets the current locales
	 * @param locales the locales
	 */
	@UnknownAttributes("LOCALE")
	public void setLocales(List<String> locales) {
		this.locales = locales;
	}
	
	@Override
	public boolean checkArguments() {
		if (locales == null || locales.isEmpty()) {
			error("No locale specified");
			return false;
		}
		if (locales.size() > 1) {
			error("You can only specify one locale");
			return false;
		}
		return true;
	}
	
	@Override
	public int doRun(String[] remainingArgs, InputReader in, PrintWriter out)
			throws OptionParserException, IOException {
		ShellContext.current().setLocale(locales.get(0));
		return 0;
	}
}

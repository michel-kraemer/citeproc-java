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

import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.helper.tool.InputReader;
import de.undercouch.citeproc.helper.tool.OptionParserException;
import de.undercouch.citeproc.helper.tool.ToolUtils;
import de.undercouch.citeproc.helper.tool.UnknownAttributes;
import de.undercouch.citeproc.tool.AbstractCSLToolCommand;

/**
 * Set the current output format
 * @author Michel Kraemer
 */
public class ShellSetFormatCommand extends AbstractCSLToolCommand {
	/**
	 * The current formats
	 */
	private List<String> formats;
	
	@Override
	public String getUsageName() {
		return "set format";
	}
	
	@Override
	public String getUsageDescription() {
		return "Set the current output format";
	}
	
	/**
	 * Sets the current formats
	 * @param formats the formats
	 */
	@UnknownAttributes("FORMAT")
	public void setFormats(List<String> formats) {
		this.formats = formats;
	}
	
	@Override
	public boolean checkArguments() {
		if (formats == null || formats.isEmpty()) {
			error("no format specified");
			return false;
		}
		if (formats.size() > 1) {
			error("you can only specify one format");
			return false;
		}
		
		String f = formats.get(0);
		try {
			List<String> supportedFormats = CSL.getSupportedOutputFormats();
			if (!supportedFormats.contains(f)) {
				String message = "unsupported format `" + f + "'";
				String dyms = ToolUtils.getDidYouMeanString(supportedFormats, f);
				if (dyms != null && !dyms.isEmpty()) {
					message += "\n\n" + dyms;
				}
				error(message);
				return false;
			}
		} catch (IOException e) {
			//could not check supported output formats. ignore
		}
		
		return true;
	}
	
	@Override
	public int doRun(String[] remainingArgs, InputReader in, PrintWriter out)
			throws OptionParserException, IOException {
		ShellContext.current().setFormat(formats.get(0));
		return 0;
	}
}

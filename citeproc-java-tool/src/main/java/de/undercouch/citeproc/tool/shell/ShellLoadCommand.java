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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import de.undercouch.citeproc.BibliographyFileReader;
import de.undercouch.citeproc.BibliographyFileReader.FileFormat;
import de.undercouch.citeproc.helper.tool.InputReader;
import de.undercouch.citeproc.helper.tool.OptionParserException;
import de.undercouch.citeproc.helper.tool.UnknownAttributes;
import de.undercouch.citeproc.tool.AbstractCSLToolCommand;

/**
 * Load an input bibliography
 * @author Michel Kraemer
 */
public class ShellLoadCommand extends AbstractCSLToolCommand {
	/**
	 * The current files
	 */
	private List<String> files;
	
	@Override
	public String getUsageName() {
		return "load";
	}
	
	@Override
	public String getUsageDescription() {
		return "Load an input bibliography from a file";
	}
	
	/**
	 * Sets the current files
	 * @param files the files
	 */
	@UnknownAttributes("FILE")
	public void setFiles(List<String> files) {
		this.files = files;
	}
	
	@Override
	public boolean checkArguments() {
		if (files == null || files.isEmpty()) {
			error("no file specified");
			return false;
		}
		if (files.size() > 1) {
			error("you can only specify one file");
			return false;
		}
		
		File f = new File(files.get(0));
		
		//check file format
		BibliographyFileReader reader = new BibliographyFileReader();
		FileFormat ff;
		try {
			ff = reader.determineFileFormat(f);
		} catch (FileNotFoundException e) {
			error("file not found");
			return false;
		} catch (IOException e) {
			error("could not determine file format");
			return false;
		}
		
		if (ff == FileFormat.UNKNOWN) {
			error("Unsupported file format");
			return false;
		}
		
		//check if file is readable
		try {
			reader.readBibliographyFile(f);
		} catch (IOException e) {
			error("could not read input file");
			return false;
		}
		
		return true;
	}
	
	@Override
	public int doRun(String[] remainingArgs, InputReader in, PrintWriter out)
			throws OptionParserException, IOException {
		ShellContext.current().setInputFile(files.get(0));
		return 0;
	}
}

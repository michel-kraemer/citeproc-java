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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import de.undercouch.citeproc.BibliographyFileReader;
import de.undercouch.citeproc.CompoundItemDataProvider;
import de.undercouch.citeproc.ItemDataProvider;
import de.undercouch.underline.Command;
import de.undercouch.underline.InputReader;
import de.undercouch.underline.Option.ArgumentType;
import de.undercouch.underline.OptionDesc;
import de.undercouch.underline.OptionParserException;

/**
 * A command that reads input bibliography files and delegates them
 * to another command
 * @author Michel Kraemer
 */
public class InputFileCommand extends AbstractCSLToolCommand {
	private final ProviderCommand delegate;

	/**
	 * The input bibliography file(s)
	 */
	private List<String> inputs = new ArrayList<>();
	
	/**
	 * Constructs a new command
	 * @param delegate the delegate
	 */
	public InputFileCommand(ProviderCommand delegate) {
		this.delegate = delegate;
	}
	
	/**
	 * Sets the input bibliography file
	 * @param input the file
	 */
	@OptionDesc(longName = "input", shortName = "i",
			description = "input bibliography FILE (*.bib, *.enl, *.ris, "
					+ "*.json, *.yml, *.yaml)",
			argumentName = "FILE", argumentType = ArgumentType.STRING,
			priority = 1)
	public void setInput(String input) {
		inputs.add(input);
	}
	
	@Override
	protected Class<?>[] getClassesToIntrospect() {
		Class<?>[] sc = super.getClassesToIntrospect();
		Class<?>[] result = new Class<?>[sc.length + 1];
		System.arraycopy(sc, 0, result, 0, sc.length);
		result[result.length - 1] = delegate.getClass();
		return result;
	}
	
	@Override
	protected Command[] getObjectsToEvaluate() {
		Command[] so = super.getObjectsToEvaluate();
		Command[] result = new Command[so.length + 1];
		System.arraycopy(so, 0, result, 0, so.length);
		result[result.length - 1] = delegate;
		return result;
	}
	
	@Override
	public void setDisplayHelp(boolean display) {
		//override this method to disable the help option on this command
		super.setDisplayHelp(display);
	}
	
	@Override
	public String getUsageName() {
		return delegate.getUsageName();
	}
	
	@Override
	public String getUsageDescription() {
		return delegate.getUsageDescription();
	}

	@Override
	public boolean checkArguments() {
		//check if there is a bibliography file
		if (inputs == null || inputs.isEmpty()) {
			error("no input bibliography specified.");
			return false;
		}
		return delegate.checkArguments();
	}

	@Override
	public int doRun(String[] remainingArgs, InputReader in, PrintWriter out)
			throws OptionParserException, IOException {
		//load input bibliography
		ItemDataProvider provider;
		try {
			BibliographyFileReader reader =
					CSLToolContext.current().getBibliographyFileReader();
			if (inputs.size() == 1) {
				provider = reader.readBibliographyFile(new File(inputs.get(0)));
			} else {
				List<ItemDataProvider> providers = new ArrayList<>();
				for (String input : inputs) {
					providers.add(reader.readBibliographyFile(new File(input)));
				}
				provider = new CompoundItemDataProvider(providers);
			}
		} catch (IOException e) {
			error(e.getMessage());
			return 1;
		}
		
		delegate.setProvider(provider);
		return delegate.doRun(remainingArgs, in, out);
	}
}

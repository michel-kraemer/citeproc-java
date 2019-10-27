// Copyright 2014-2019 Michel Kraemer
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

import de.undercouch.citeproc.CSLTool;
import de.undercouch.underline.Command;
import de.undercouch.underline.Option;
import de.undercouch.underline.OptionGroup;
import de.undercouch.underline.OptionIntrospector;
import de.undercouch.underline.OptionIntrospector.ID;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link ShellCommandCompleter}
 * @author Michel Kraemer
 */
public class ShellCommandCompleterTest {
	private int complete(String buffer, ArrayList<CharSequence> r) {
		return complete(buffer, r, Collections.emptyList());
	}
	
	private int complete(String buffer, ArrayList<CharSequence> r,
			List<Class<? extends Command>> excludedCommands) {
		ShellCommandCompleter cc = new ShellCommandCompleter(excludedCommands);
		return cc.complete(buffer, buffer.length(), r);
	}
	
	/**
	 * Tests if top-level commands can be completed
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void topLevel() throws Exception {
		ArrayList<CharSequence> r = new ArrayList<>();
		int pos = complete("", r);
		
		OptionGroup<ID> options = OptionIntrospector.introspect(CSLTool.class);
		OptionGroup<ID> optionsAdditional = OptionIntrospector.introspect(
				AdditionalShellCommands.class);
		//get number of commands, subtract 1 because there's a HelpCommand
		//and a ShellHelpCommand
		int numCommands = options.getCommands().size() +
				optionsAdditional.getCommands().size() - 1;
		assertEquals(numCommands, r.size());
		for (Option<ID> o : options.getCommands()) {
			assertTrue(r.contains(o.getLongName()));
		}
		for (Option<ID> o : optionsAdditional.getCommands()) {
			assertTrue(r.contains(o.getLongName()));
		}
		assertEquals(0, pos);
		
		r = new ArrayList<>();
		pos = complete(" ", r);
		assertEquals(numCommands, r.size());
		assertEquals(1, pos);
		
		r = new ArrayList<>();
		pos = complete("     ", r);
		assertEquals(numCommands, r.size());
		assertEquals(5, pos);
		
		r = new ArrayList<>();
		pos = complete("bibl", r);
		assertEquals(1, r.size());
		assertEquals("bibliography", r.get(0));
		assertEquals(0, pos);
		
		r = new ArrayList<>();
		pos = complete("ge", r);
		assertEquals(1, r.size());
		assertEquals("get", r.get(0));
		assertEquals(0, pos);
		
		r = new ArrayList<>();
		pos = complete("bla", r);
		assertEquals(0, r.size());
		assertEquals(-1, pos);
	}
	
	/**
	 * Tests if subcommands can be completed
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void subcommand() throws Exception {
		ArrayList<CharSequence> r = new ArrayList<>();
		int pos = complete("get", r);
		
		OptionGroup<ID> options = OptionIntrospector.introspect(
				ShellGetCommand.class);
		assertEquals(options.getCommands().size(), r.size());
		for (Option<ID> o : options.getCommands()) {
			assertTrue(r.contains(o.getLongName()));
		}
		assertEquals(4, pos);
		
		r = new ArrayList<>();
		pos = complete("get ", r);
		assertEquals(options.getCommands().size(), r.size());
		assertEquals(4, pos);
		
		r = new ArrayList<>();
		pos = complete("get st", r);
		assertEquals(1, r.size());
		assertEquals("style", r.get(0));
		assertEquals(4, pos);
		
		r = new ArrayList<>();
		pos = complete("get style", r);
		assertEquals(0, r.size());
		assertEquals(-1, pos);
		
		r = new ArrayList<>();
		pos = complete("get bla", r);
		assertEquals(0, r.size());
		assertEquals(-1, pos);
	}
	
	/**
	 * Tests if unknown attributes are ignored
	 */
	@Test
	public void unknownAttributes() {
		ArrayList<CharSequence> r = new ArrayList<>();
		int pos = complete("get style test", r);
		assertEquals(0, r.size());
		assertEquals(-1, pos);
		
		r = new ArrayList<>();
		pos = complete("get style test test2", r);
		assertEquals(0, r.size());
		assertEquals(-1, pos);
	}
	
	/**
	 * Tests if commands can be excluded
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void excludedCommands() throws Exception {
		List<Class<? extends Command>> cmds = new ArrayList<>();
		cmds.add(ShellGetCommand.class);
		
		OptionGroup<ID> options = OptionIntrospector.introspect(CSLTool.class);
		OptionGroup<ID> optionsAdditional = OptionIntrospector.introspect(
				AdditionalShellCommands.class);
		//get number of commands, subtract 1 because there's a HelpCommand
		//and a ShellHelpCommand
		int numCommands = options.getCommands().size() +
				optionsAdditional.getCommands().size() - 1;
		//subtract 1 again because we excluded one command
		--numCommands;
		
		ArrayList<CharSequence> r = new ArrayList<>();
		complete("", r, cmds);
		assertEquals(numCommands, r.size());
	}
	
	/**
	 * Tests if completions for the help command are computed correctly
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void help() throws Exception {
		ArrayList<CharSequence> r = new ArrayList<>();
		int pos = complete("hel", r);
		assertEquals(1, r.size());
		assertEquals("help", r.get(0));
		assertEquals(0, pos);
		
		r = new ArrayList<>();
		pos = complete("help ge", r);
		assertEquals(1, r.size());
		assertEquals("get", r.get(0));
		assertEquals(5, pos);
		
		r = new ArrayList<>();
		pos = complete("help get st", r);
		assertEquals(1, r.size());
		assertEquals("style", r.get(0));
		assertEquals(9, pos);
		
		r = new ArrayList<>();
		pos = complete("help get", r);
		OptionGroup<ID> options = OptionIntrospector.introspect(ShellGetCommand.class);
		assertEquals(options.getCommands().size(), r.size());
		for (Option<ID> cmd : options.getCommands()) {
			assertTrue(r.contains(cmd.getLongName()));
		}
		assertEquals(9, pos);
		
		r = new ArrayList<>();
		pos = complete("help", r);
		options = OptionIntrospector.introspect(CSLTool.class);
		OptionGroup<ID> optionsAdditional = OptionIntrospector.introspect(
				AdditionalShellCommands.class);
		//get number of commands, subtract 1 because there's a HelpCommand
		//and a ShellHelpCommand
		int numCommands = options.getCommands().size() +
				optionsAdditional.getCommands().size() - 1;
		assertEquals(numCommands, r.size());
		assertEquals(5, pos);
	}
	
	/**
	 * Checks if completions for output formats are calculated correctly
	 */
	@Test
	public void completeFormats() {
		ArrayList<CharSequence> r = new ArrayList<>();
		int pos = complete("set format h", r);
		assertEquals(1, r.size());
		assertEquals("html", r.get(0));
		assertEquals(11, pos);
		
		r = new ArrayList<>();
		pos = complete("set format", r);
		assertEquals(5, r.size());
		assertEquals(11, pos);
	}
}

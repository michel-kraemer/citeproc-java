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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import de.undercouch.citeproc.CSLTool;
import de.undercouch.citeproc.helper.tool.Command;
import de.undercouch.citeproc.helper.tool.Option;
import de.undercouch.citeproc.helper.tool.OptionGroup;
import de.undercouch.citeproc.helper.tool.OptionIntrospector;
import de.undercouch.citeproc.helper.tool.OptionIntrospector.ID;
import de.undercouch.citeproc.tool.MendeleyCommand;

/**
 * Tests {@link ShellCommandCompleter}
 * @author Michel Kraemer
 */
public class ShellCommandCompleterTest {
	private int complete(String buffer, ArrayList<CharSequence> r) {
		return complete(buffer, r, Collections.<Class<? extends Command>>emptyList());
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
		ArrayList<CharSequence> r = new ArrayList<CharSequence>();
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
		
		r = new ArrayList<CharSequence>();
		pos = complete(" ", r);
		assertEquals(numCommands, r.size());
		assertEquals(1, pos);
		
		r = new ArrayList<CharSequence>();
		pos = complete("     ", r);
		assertEquals(numCommands, r.size());
		assertEquals(5, pos);
		
		r = new ArrayList<CharSequence>();
		pos = complete("bibl", r);
		assertEquals(1, r.size());
		assertEquals("bibliography", r.get(0));
		assertEquals(0, pos);
		
		r = new ArrayList<CharSequence>();
		pos = complete("men", r);
		assertEquals(1, r.size());
		assertEquals("mendeley", r.get(0));
		assertEquals(0, pos);
		
		r = new ArrayList<CharSequence>();
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
		ArrayList<CharSequence> r = new ArrayList<CharSequence>();
		int pos = complete("mendeley", r);
		
		OptionGroup<ID> options = OptionIntrospector.introspect(
				MendeleyCommand.class);
		assertEquals(options.getCommands().size(), r.size());
		for (Option<ID> o : options.getCommands()) {
			assertTrue(r.contains(o.getLongName()));
		}
		assertEquals(9, pos);
		
		r = new ArrayList<CharSequence>();
		pos = complete("mendeley ", r);
		assertEquals(options.getCommands().size(), r.size());
		assertEquals(9, pos);
		
		r = new ArrayList<CharSequence>();
		pos = complete("mendeley li", r);
		assertEquals(1, r.size());
		assertEquals("list", r.get(0));
		assertEquals(9, pos);
		
		r = new ArrayList<CharSequence>();
		pos = complete("mendeley list", r);
		assertEquals(0, r.size());
		assertEquals(-1, pos);
		
		r = new ArrayList<CharSequence>();
		pos = complete("mendeley bla", r);
		assertEquals(0, r.size());
		assertEquals(-1, pos);
	}
	
	/**
	 * Tests if unknown attributes are ignored
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void unknownAttributes() throws Exception {
		ArrayList<CharSequence> r = new ArrayList<CharSequence>();
		int pos = complete("mendeley bibliography test", r);
		assertEquals(0, r.size());
		assertEquals(-1, pos);
		
		r = new ArrayList<CharSequence>();
		pos = complete("mendeley bibliography test test2", r);
		assertEquals(0, r.size());
		assertEquals(-1, pos);
	}
	
	/**
	 * Tests if commands can be excluded
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void excludedCommands() throws Exception {
		List<Class<? extends Command>> cmds = new ArrayList<Class<? extends Command>>();
		cmds.add(MendeleyCommand.class);
		
		OptionGroup<ID> options = OptionIntrospector.introspect(CSLTool.class);
		OptionGroup<ID> optionsAdditional = OptionIntrospector.introspect(
				AdditionalShellCommands.class);
		//get number of commands, subtract 1 because there's a HelpCommand
		//and a ShellHelpCommand
		int numCommands = options.getCommands().size() +
				optionsAdditional.getCommands().size() - 1;
		//subtract 1 again because we excluded one command
		--numCommands;
		
		ArrayList<CharSequence> r = new ArrayList<CharSequence>();
		complete("", r, cmds);
		assertEquals(numCommands, r.size());
	}
	
	/**
	 * Tests if completions for the help command are computed correctly
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void help() throws Exception {
		ArrayList<CharSequence> r = new ArrayList<CharSequence>();
		int pos = complete("hel", r);
		assertEquals(1, r.size());
		assertEquals("help", r.get(0));
		assertEquals(0, pos);
		
		r = new ArrayList<CharSequence>();
		pos = complete("help me", r);
		assertEquals(1, r.size());
		assertEquals("mendeley", r.get(0));
		assertEquals(5, pos);
		
		r = new ArrayList<CharSequence>();
		pos = complete("help mendeley li", r);
		assertEquals(1, r.size());
		assertEquals("list", r.get(0));
		assertEquals(14, pos);
		
		r = new ArrayList<CharSequence>();
		pos = complete("help mendeley", r);
		OptionGroup<ID> options = OptionIntrospector.introspect(MendeleyCommand.class);
		assertEquals(options.getCommands().size(), r.size());
		for (Option<ID> cmd : options.getCommands()) {
			assertTrue(r.contains(cmd.getLongName()));
		}
		assertEquals(14, pos);
		
		r = new ArrayList<CharSequence>();
		pos = complete("help", r);
		options = OptionIntrospector.introspect(CSLTool.class);
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
		ArrayList<CharSequence> r = new ArrayList<CharSequence>();
		int pos = complete("set format h", r);
		assertEquals(1, r.size());
		assertEquals("html", r.get(0));
		assertEquals(11, pos);
		
		r = new ArrayList<CharSequence>();
		pos = complete("set format", r);
		assertEquals(5, r.size());
		assertEquals(11, pos);
	}
}

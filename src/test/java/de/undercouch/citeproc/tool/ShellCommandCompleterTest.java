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

package de.undercouch.citeproc.tool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

import de.undercouch.citeproc.CSLTool;
import de.undercouch.citeproc.helper.tool.Option;
import de.undercouch.citeproc.helper.tool.OptionGroup;
import de.undercouch.citeproc.helper.tool.OptionIntrospector;
import de.undercouch.citeproc.helper.tool.OptionIntrospector.ID;

/**
 * Tests {@link ShellCommandCompleter}
 * @author Michel Kraemer
 */
public class ShellCommandCompleterTest {
	private int complete(String buffer, ArrayList<CharSequence> r) {
		ShellCommandCompleter cc = new ShellCommandCompleter();
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
		assertEquals(options.getCommands().size(), r.size());
		for (Option<ID> o : options.getCommands()) {
			assertTrue(r.contains(o.getLongName()));
		}
		assertEquals(0, pos);
		
		r = new ArrayList<CharSequence>();
		pos = complete(" ", r);
		assertEquals(options.getCommands().size(), r.size());
		assertEquals(1, pos);
		
		r = new ArrayList<CharSequence>();
		pos = complete("     ", r);
		assertEquals(options.getCommands().size(), r.size());
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
	 * Tests if commands with flags can be completed
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void flags() throws Exception {
		ArrayList<CharSequence> r = new ArrayList<CharSequence>();
		int pos = complete("mendeley -s", r);
		assertEquals(12, pos);
		
		OptionGroup<ID> options = OptionIntrospector.introspect(
				MendeleyCommand.class);
		assertEquals(options.getCommands().size(), r.size());
		for (Option<ID> o : options.getCommands()) {
			assertTrue(r.contains(o.getLongName()));
		}
		
		r = new ArrayList<CharSequence>();
		pos = complete("mendeley -s ", r);
		assertEquals(options.getCommands().size(), r.size());
		assertEquals(12, pos);
		
		r = new ArrayList<CharSequence>();
		pos = complete("mendeley -s li", r);
		assertEquals(1, r.size());
		assertEquals("list", r.get(0));
		assertEquals(12, pos);
		
		r = new ArrayList<CharSequence>();
		pos = complete("mendeley -s list", r);
		assertEquals(0, r.size());
		assertEquals(-1, pos);
		
		r = new ArrayList<CharSequence>();
		pos = complete("mendeley -s bla", r);
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
}

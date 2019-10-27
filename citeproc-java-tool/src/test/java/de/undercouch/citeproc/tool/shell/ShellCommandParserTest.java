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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.undercouch.citeproc.tool.BibliographyCommand;
import de.undercouch.citeproc.tool.ListCommand;
import de.undercouch.citeproc.tool.ShellCommand;
import de.undercouch.citeproc.tool.shell.ShellCommandParser.Result;
import de.undercouch.underline.Command;

/**
 * Tests the {@link ShellCommandParser}
 * @author Michel Kraemer
 */
public class ShellCommandParserTest {
	/**
	 * Tests if commands can be parsed
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void commands() throws Exception {
		Result pr = ShellCommandParser.parse("bibliography");
		assertEquals(0, pr.getRemainingArgs().length);
		assertEquals(BibliographyCommand.class, pr.getFirstCommand());
		assertEquals(BibliographyCommand.class, pr.getLastCommand());
		
		pr = ShellCommandParser.parse("  list  ");
		assertEquals(0, pr.getRemainingArgs().length);
		assertEquals(ListCommand.class, pr.getFirstCommand());
	}
	
	/**
	 * Tests if sub-commands can be parsed
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void subcommands() throws Exception {
		Result pr = ShellCommandParser.parse("get style");
		assertEquals(0, pr.getRemainingArgs().length);
		assertEquals(ShellGetCommand.class, pr.getFirstCommand());
		assertEquals(ShellGetStyleCommand.class, pr.getLastCommand());
		
		pr = ShellCommandParser.parse("  get    style  ");
		assertEquals(0, pr.getRemainingArgs().length);
		assertEquals(ShellGetCommand.class, pr.getFirstCommand());
		assertEquals(ShellGetStyleCommand.class, pr.getLastCommand());
	}
	
	/**
	 * Tests if incomplete command lines can be parsed
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void incomplete() throws Exception {
		Result pr = ShellCommandParser.parse("");
		assertEquals(1, pr.getRemainingArgs().length);
		assertEquals("", pr.getRemainingArgs()[0]);
		assertNull(pr.getFirstCommand());
		assertNull(pr.getLastCommand());
		
		pr = ShellCommandParser.parse("bibl");
		assertEquals(1, pr.getRemainingArgs().length);
		assertEquals("bibl", pr.getRemainingArgs()[0]);
		assertNull(pr.getFirstCommand());
		assertNull(pr.getLastCommand());
		
		pr = ShellCommandParser.parse("get s");
		assertEquals(1, pr.getRemainingArgs().length);
		assertEquals("s", pr.getRemainingArgs()[0]);
		assertEquals(ShellGetCommand.class, pr.getFirstCommand());
		assertEquals(ShellGetCommand.class, pr.getLastCommand());
	}
	
	/**
	 * Tests if commands can be excluded from parsing
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void excluded() throws Exception {
		Result pr = ShellCommandParser.parse("shell");
		assertEquals(0, pr.getRemainingArgs().length);
		assertEquals(ShellCommand.class, pr.getFirstCommand());
		
		List<Class<? extends Command>> excluded = new ArrayList<>();
		excluded.add(ShellCommand.class);
		
		pr = ShellCommandParser.parse("bibliography", excluded);
		assertEquals(0, pr.getRemainingArgs().length);
		assertEquals(BibliographyCommand.class, pr.getFirstCommand());
		
		pr = ShellCommandParser.parse("shell", excluded);
		assertEquals(1, pr.getRemainingArgs().length);
		assertEquals("shell", pr.getRemainingArgs()[0]);
		assertNull(pr.getFirstCommand());
		assertNull(pr.getLastCommand());
	}
}

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

package de.undercouch.citeproc.helper.tool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import org.junit.Test;

import de.undercouch.citeproc.helper.tool.Option.ArgumentType;
import de.undercouch.citeproc.helper.tool.OptionIntrospector.ID;

/**
 * Tests the {@link OptionIntrospector}
 * @author Michel Kraemer
 */
public class OptionIntrospectorTest {
	/**
	 * A simple command with a flag
	 */
	public static class SimpleCommand implements Command {
		/**
		 * The flag
		 */
		protected boolean flag;
		
		/**
		 * Sets the flag
		 * @param flag the flag
		 */
		@OptionDesc(longName = "flag", description = "a boolean flag")
		public void setFlag(boolean flag) {
			this.flag = flag;
		}

		@Override
		public int run(String[] args, PrintStream out)
				throws OptionParserException, IOException {
			return 0;
		}
	}
	
	/**
	 * A simple command that takes unknown argument values
	 */
	public static class SimpleCommandWithUnknown extends SimpleCommand {
		private List<String> unknownValues;
		
		/**
		 * Sets the unknown argument values
		 * @param values the values
		 */
		@UnknownAttributes
		public void setUnknownAttributes(List<String> values) {
			unknownValues = values;
		}
	}
	
	/**
	 * A command with a sub-command
	 */
	public static class CommandCommand extends SimpleCommand {
		private Command cmd;
		
		/**
		 * Sets the sub-command
		 * @param cmd the sub-command
		 */
		@CommandDesc(longName = "command", description = "a command", command = SimpleCommand.class)
		public void setCommand(Command cmd) {
			this.cmd = cmd;
		}
	}
	
	/**
	 * A command with a list of sub-commands
	 */
	public static class CommandListCommand extends SimpleCommand {
		private Command cmd;
		
		/**
		 * Sets the sub-command
		 * @param cmd the sub-command
		 */
		@CommandDescList({
			@CommandDesc(longName = "command", description = "a command", command = SimpleCommand.class),
			@CommandDesc(longName = "command2", description = "another command", command = CommandCommand.class)
		})
		public void setCommand(Command cmd) {
			this.cmd = cmd;
		}
	}
	
	/**
	 * Introspect a simple class
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void introspectSimple() throws Exception {
		OptionGroup<ID> og = OptionIntrospector.introspect(SimpleCommand.class);
		assertEquals(1, og.getOptions().size());
		assertEquals(0, og.getChildren().size());
		assertEquals(0, og.getCommands().size());
		Option<ID> first = og.getOptions().get(0);
		assertEquals("flag", first.getLongName());
		assertEquals("a boolean flag", first.getDescription());
		assertNull(first.getShortName());
		assertNull(first.getArgumentName());
		assertEquals(ArgumentType.NONE, first.getArgumentType());
	}
	
	/**
	 * Tests if simple options can be evaluated
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void evaluateSimple() throws Exception {
		OptionGroup<ID> og = OptionIntrospector.introspect(SimpleCommand.class);
		SimpleCommand cmd = new SimpleCommand();
		assertFalse(cmd.flag);
		OptionParser.Result<ID> result = OptionParser.parse(
				new String[] { "--flag" }, og, null);
		OptionIntrospector.evaluate(result.getValues(), cmd);
		assertTrue(cmd.flag);
	}
	
	/**
	 * Tests if the introspector throws an exception if there is no setter
	 * for unknown arguments
	 * @throws Exception if something goes wrong
	 */
	@Test(expected = RuntimeException.class)
	public void evaluateUnknownNoSetter() throws Exception {
		OptionGroup<ID> og = OptionIntrospector.introspect(SimpleCommand.class);
		SimpleCommand cmd = new SimpleCommand();
		assertFalse(cmd.flag);
		OptionParser.Result<ID> result = OptionParser.parse(
				new String[] { "--flag", "bla", "blubb" }, og,
				OptionIntrospector.DEFAULT_ID);
		OptionIntrospector.evaluate(result.getValues(), cmd);
	}
	
	/**
	 * Tests if unknown arguments can be parsed to values with default IDs
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void evaluateUnknown() throws Exception {
		OptionGroup<ID> og = OptionIntrospector.introspect(SimpleCommand.class);
		SimpleCommandWithUnknown cmd = new SimpleCommandWithUnknown();
		assertFalse(cmd.flag);
		OptionParser.Result<ID> result = OptionParser.parse(
				new String[] { "--flag", "bla", "blubb" }, og,
				OptionIntrospector.DEFAULT_ID);
		OptionIntrospector.evaluate(result.getValues(), cmd);
		assertTrue(cmd.flag);
		assertEquals(2, cmd.unknownValues.size());
		assertEquals("bla", cmd.unknownValues.get(0));
		assertEquals("blubb", cmd.unknownValues.get(1));
	}
	
	/**
	 * Tests if a sub-command can be parsed
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void introspectCommandCommand() throws Exception {
		OptionGroup<ID> og = OptionIntrospector.introspect(CommandCommand.class);
		CommandCommand cmd = new CommandCommand();
		assertFalse(cmd.flag);
		OptionParser.Result<ID> result = OptionParser.parse(
				new String[] { "--flag", "command" }, og, null);
		OptionIntrospector.evaluate(result.getValues(), cmd);
		assertTrue(cmd.flag);
		assertTrue(cmd.cmd instanceof SimpleCommand);
	}
	
	/**
	 * Tests if a list of sub-commands can be parsed
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void introspectCommandListCommand() throws Exception {
		OptionGroup<ID> og = OptionIntrospector.introspect(CommandListCommand.class);
		CommandListCommand cmd = new CommandListCommand();
		assertFalse(cmd.flag);
		OptionParser.Result<ID> result = OptionParser.parse(
				new String[] { "--flag", "command2" }, og, null);
		OptionIntrospector.evaluate(result.getValues(), cmd);
		assertTrue(cmd.flag);
		assertTrue(cmd.cmd instanceof CommandCommand);
	}
	
	/**
	 * Tests if options can be injected into multiple classes
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void multipleClasses() throws Exception {
		OptionGroup<ID> og = OptionIntrospector.introspect(CommandCommand.class,
				SimpleCommandWithUnknown.class);
		OptionParser.Result<ID> result = OptionParser.parse(
				new String[] { "--flag", "unknown", "command" }, og,
				OptionIntrospector.DEFAULT_ID);
		CommandCommand cmd1 = new CommandCommand();
		SimpleCommandWithUnknown cmd2 = new SimpleCommandWithUnknown();
		OptionIntrospector.evaluate(result.getValues(), cmd1, cmd2);
		assertTrue(cmd1.flag);
		assertTrue(cmd2.flag);
		assertTrue(cmd1.cmd instanceof SimpleCommand);
		assertEquals(1, cmd2.unknownValues.size());
		assertEquals("unknown", cmd2.unknownValues.get(0));
	}
}

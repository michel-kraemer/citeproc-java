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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;

import org.junit.Test;

import de.undercouch.citeproc.helper.tool.Option.ArgumentType;

/**
 * Tests the command-line interface parser
 * @author Michel Kraemer
 */
public class OptionParserTest {
	private static class SimpleCommand implements Command {
		private static final OptionGroup<Integer> options = new OptionBuilder<Integer>()
				.add(0, "arg1", "argument 1")
				.add(1, "arg2", "argument 2")
				.build();
		
		@Override
		public int run(String[] args, InputReader in, PrintWriter out)
				throws OptionParserException {
			//should not throw
			OptionParser.Result<Integer> r = OptionParser.parse(args, options, null);
			assertEquals(0, r.getRemainingArgs().length);
			return 0;
		}
	}
	
	private static final OptionGroup<Integer> SIMPLE_OPTIONS = new OptionBuilder<Integer>()
			.add(0, "opt", "o", "option 1")
			.add(1, "oarg", "a", "option with arg", "ARG", ArgumentType.STRING)
			.build();
	
	/**
	 * Tests if the parser can parse a simple command
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void simpleCommand() throws Exception {
		OptionGroup<Integer> options = new OptionBuilder<Integer>()
				.addCommand(0, "simple", "simple command")
				.build();
		
		OptionParser.Result<Integer> result = OptionParser.parse(
				new String[] { "simple" }, options, null);
		List<Value<Integer>> values = result.getValues();
		assertEquals(1, values.size());
		assertEquals(0, values.get(0).getId().intValue());
	}
	
	/**
	 * Tests if the parser can parse a command with arguments
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void commandWithArguments() throws Exception {
		OptionGroup<Integer> options = new OptionBuilder<Integer>()
				.addCommand(0, "simple", "simple command")
				.build();
		
		OptionParser.Result<Integer> result = OptionParser.parse(
				new String[] { "simple", "--arg1", "--arg2" }, options, null);
		List<Value<Integer>> values = result.getValues();
		assertEquals(1, values.size());
		assertEquals(0, values.get(0).getId().intValue());
		assertArrayEquals(new String[] { "--arg1", "--arg2" }, result.getRemainingArgs());
		new SimpleCommand().run(result.getRemainingArgs(), null, null);
	}
	
	/**
	 * Tests if the parser can parse simple options
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void simpleOptions() throws Exception {
		OptionParser.Result<Integer> result = OptionParser.parse(
				new String[] { "--opt", "-o", "--oarg", "test" }, SIMPLE_OPTIONS, null);
		List<Value<Integer>> values = result.getValues();
		assertEquals(3, values.size());
		assertEquals(0, values.get(0).getId().intValue());
		assertEquals(0, values.get(1).getId().intValue());
		assertEquals(1, values.get(2).getId().intValue());
		assertEquals("test", values.get(2).getValue().toString());
	}
	
	/**
	 * Tests if the parser fails to parse an unknown option
	 * @throws Exception if something goes wrong
	 */
	@Test(expected = InvalidOptionException.class)
	public void unknownOption() throws Exception {
		OptionParser.parse(new String[] { "--bla" }, SIMPLE_OPTIONS, null);
	}
	
	/**
	 * Tests if the parser fails to parse an unknown argument that is neither
	 * an option or a command
	 * @throws Exception if something goes wrong
	 */
	@Test(expected = InvalidOptionException.class)
	public void unknownArgument() throws Exception {
		OptionParser.parse(new String[] { "argument" }, SIMPLE_OPTIONS, null);
	}
	
	/**
	 * Tests if the parser can parse an argument that is neither an
	 * option or a command
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void otherArgument() throws Exception {
		OptionParser.Result<Integer> result = OptionParser.parse(
				new String[] { "--opt", "argument" }, SIMPLE_OPTIONS, -1);
		List<Value<Integer>> values = result.getValues();
		assertEquals(2, values.size());
		assertEquals(0, values.get(0).getId().intValue());
		assertEquals(-1, values.get(1).getId().intValue());
		assertEquals("argument", values.get(1).getValue().toString());
	}
	
	/**
	 * Tests if usage information is printed out correctly
	 * @throws Exception if something goes wrong
	 */
	@Test
	public void usage() throws Exception {
		OptionGroup<Integer> options = new OptionBuilder<Integer>()
			.add(0, "opt", "o", "option 1")
			.add(1, "oarg", "a", "option with arg", "ARG", ArgumentType.STRING)
			.add(new OptionBuilder<Integer>("Group:")
				.add(3, "b", "group option b")
				.add(4, "c", "group option c")
				.build()
			)
			.addCommand(5, "command", "command 1")
			.addCommand(5, "bla", "command 2")
			.build();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos, true, "UTF-8");
		OptionParser.usage("test", "desc", options, ps);
		ps.flush();
		String str = baos.toString("UTF-8");
		String n = System.getProperty("line.separator");
		assertEquals("Usage: test" + n
				+ "desc" + n
				+ n
				+ "  -o,--opt         option 1" + n
				+ "  -a,--oarg <ARG>  option with arg" + n
				+ n
				+ "Group:" + n
				+ "     --b           group option b" + n
				+ "     --c           group option c" + n
				+ n
				+ "Commands:" + n
				+ "  command          command 1" + n
				+ "  bla              command 2" + n, str);
	}
}

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

package de.undercouch.citeproc;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

import de.undercouch.citeproc.helper.CSLUtils;
import de.undercouch.citeproc.helper.tool.CommandDesc;
import de.undercouch.citeproc.helper.tool.CommandDescList;
import de.undercouch.citeproc.helper.tool.InputReader;
import de.undercouch.citeproc.helper.tool.Option.ArgumentType;
import de.undercouch.citeproc.helper.tool.OptionDesc;
import de.undercouch.citeproc.helper.tool.OptionParserException;
import de.undercouch.citeproc.helper.tool.StandardInputReader;
import de.undercouch.citeproc.tool.AbstractCSLToolCommand;
import de.undercouch.citeproc.tool.BibliographyCommand;
import de.undercouch.citeproc.tool.CSLToolContext;
import de.undercouch.citeproc.tool.CitationCommand;
import de.undercouch.citeproc.tool.HelpCommand;
import de.undercouch.citeproc.tool.InputFileCommand;
import de.undercouch.citeproc.tool.JsonCommand;
import de.undercouch.citeproc.tool.ListCommand;
import de.undercouch.citeproc.tool.MendeleyCommand;
import de.undercouch.citeproc.tool.ProviderCommand;
import de.undercouch.citeproc.tool.ShellCommand;
import de.undercouch.citeproc.tool.ZoteroCommand;

/**
 * Command line tool for the CSL processor. Use <code>citeproc-java --help</code>
 * for more information.
 * @author Michel Kraemer
 */
public class CSLTool extends AbstractCSLToolCommand {
	private boolean displayVersion;
	private String outputFile;
	private AbstractCSLToolCommand command;

	/**
	 * Path to the tool's configuration directory
	 */
	private File configDir;

	/**
	 * Sets the name of the output file
	 * @param outputFile the file name or null if output should be written
	 * to standard out
	 */
	@OptionDesc(longName = "output", shortName = "o",
			description = "write output to FILE instead of stdout",
			argumentName = "FILE", argumentType = ArgumentType.STRING)
	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}
	
	/**
	 * Specifies if version information should be displayed
	 * @param display true if the version should be displayed
	 */
	@OptionDesc(longName = "version", shortName = "V",
			description = "output version information and exit",
			priority = 9999)
	public void setDisplayVersion(boolean display) {
		this.displayVersion = display;
	}
	
	/**
	 * Sets the command to execute
	 * @param command the command
	 */
	@CommandDescList({
		@CommandDesc(longName = "bibliography",
				description = "generate a bibliography",
				command = BibliographyCommand.class),
		@CommandDesc(longName = "citation",
				description = "generate citations",
				command = CitationCommand.class),
		@CommandDesc(longName = "list",
				description = "display sorted list of available citation IDs",
				command = ListCommand.class),
		@CommandDesc(longName = "json",
				description = "convert input bibliography to JSON",
				command = JsonCommand.class),
		@CommandDesc(longName = "mendeley",
				description = "connect to Mendeley Web",
				command = MendeleyCommand.class),
		@CommandDesc(longName = "zotero",
				description = "connect to Zotero",
				command = ZoteroCommand.class),
		@CommandDesc(longName = "shell",
				description = "run citeproc-java in interactive mode",
				command = ShellCommand.class),
		@CommandDesc(longName = "help",
				description = "display help for a given command",
				command = HelpCommand.class)
	})
	public void setCommand(AbstractCSLToolCommand command) {
		if (command instanceof ProviderCommand) {
			this.command = new InputFileCommand((ProviderCommand)command);
		} else {
			this.command = command;
		}
	}
	
	/**
	 * The main method of the CSL tool. Use <code>citeproc-java --help</code>
	 * for more information.
	 * @param args the command line
	 * @throws IOException if a stream could not be read
	 */
	public static void main(String[] args) throws IOException {
		CSLToolContext ctx = CSLToolContext.enter();
		try {
			ctx.setToolName("citeproc-java");
			CSLTool tool = new CSLTool();
			int exitCode;
			try {
				exitCode = tool.run(args, new StandardInputReader(),
						new PrintWriter(System.out));
			} catch (OptionParserException e) {
				tool.error(e.getMessage());
				exitCode = 1;
			}
			if (exitCode != 0) {
				System.exit(exitCode);
			}
		} finally {
			CSLToolContext.exit();
		}
	}

	@Override
	public int doRun(String[] remainingArgs, InputReader in, PrintWriter out)
			throws OptionParserException, IOException {
		if (displayVersion) {
			version();
			return 0;
		}
		
		configDir = new File(System.getProperty("user.home"), ".citeproc-java");
		configDir.mkdirs();
		CSLToolContext.current().setConfigDir(configDir);
		
		//if there are no commands print usage and exit
		if (command == null) {
			usage();
			return 0;
		}
		
		//prepare output
		if (outputFile != null) {
			out = new PrintWriter(outputFile, "UTF-8");
		}
		
		try {
			int ret = command.run(remainingArgs, in, out);
			out.flush();
			return ret;
		} finally {
			if (outputFile != null) {
				out.close();
			}
		}
	}
	
	@Override
	public String getUsageName() {
		return ""; //the tool's name will be prepended
	}
	
	@Override
	public String getUsageDescription() {
		return "Generate styled citations and bibliographies";
	}
	
	/**
	 * Prints out version information
	 */
	private void version() {
		System.out.println(CSLToolContext.current().getToolName() + " " +
				getVersion());
	}
	
	/**
	 * @return the tool's version string
	 */
	public static String getVersion() {
		URL u = CSLTool.class.getResource("version.dat");
		String version;
		try {
			version = CSLUtils.readURLToString(u, "UTF-8");
		} catch (IOException e) {
			throw new RuntimeException("Could not read version information", e);
		}
		return version;
	}
}

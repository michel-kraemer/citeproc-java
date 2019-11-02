package de.undercouch.citeproc.tool.shell;

import de.undercouch.citeproc.tool.AbstractCSLToolCommand;
import de.undercouch.underline.CommandDesc;
import de.undercouch.underline.CommandDescList;

/**
 * Contains the configuration for all additional shell commands
 * @author Michel Kraemer
 */
public final class AdditionalShellCommands {
    /**
     * Configures all additional shell commands
     * @param command the configured command
     */
    @CommandDescList({
            @CommandDesc(longName = "load",
                    description = "load an input bibliography from a file",
                    command = ShellLoadCommand.class),
            @CommandDesc(longName = "get",
                    description = "get values of shell variables",
                    command = ShellGetCommand.class),
            @CommandDesc(longName = "set",
                    description = "assign values to shell variables",
                    command = ShellSetCommand.class),
            @CommandDesc(longName = "help",
                    description = "display help for a given command",
                    command = ShellHelpCommand.class),
            @CommandDesc(longName = "exit",
                    description = "exit the interactive shell",
                    command = ShellExitCommand.class),
            @CommandDesc(longName = "quit",
                    description = "exit the interactive shell",
                    command = ShellQuitCommand.class),
    })
    public void setCommand(@SuppressWarnings("unused") AbstractCSLToolCommand command) {
        // we don't have to do anything here
    }
}

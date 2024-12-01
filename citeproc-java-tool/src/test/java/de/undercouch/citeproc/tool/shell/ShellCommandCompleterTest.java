package de.undercouch.citeproc.tool.shell;

import de.undercouch.citeproc.CSLTool;
import de.undercouch.underline.Command;
import de.undercouch.underline.Option;
import de.undercouch.underline.OptionGroup;
import de.undercouch.underline.OptionIntrospector;
import de.undercouch.underline.OptionIntrospector.ID;
import org.jline.reader.Candidate;
import org.jline.reader.impl.DefaultParser;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link ShellCommandCompleter}
 * @author Michel Kraemer
 */
public class ShellCommandCompleterTest {
    private void complete(String line, ArrayList<CharSequence> r) {
        complete(line, r, Collections.emptyList());
    }

    private void complete(String line, ArrayList<CharSequence> r,
            List<Class<? extends Command>> excludedCommands) {
        ShellCommandCompleter cc = new ShellCommandCompleter(excludedCommands);
        List<Candidate> cs = new ArrayList<>();
        cc.complete(null, new DefaultParser().new ArgumentList(line,
                Arrays.asList(line.split("\\s+")), 0, 0, 0, null, 0, 0), cs);
        for (Candidate c : cs) {
            r.add(c.value());
        }
    }

    /**
     * Tests if top-level commands can be completed
     * @throws Exception if something goes wrong
     */
    @Test
    public void topLevel() throws Exception {
        ArrayList<CharSequence> r = new ArrayList<>();
        complete("", r);

        OptionGroup<ID> options = OptionIntrospector.introspect(CSLTool.class);
        OptionGroup<ID> optionsAdditional = OptionIntrospector.introspect(
                AdditionalShellCommands.class);
        // get number of commands, subtract 1 because there's a HelpCommand
        // and a ShellHelpCommand
        int numCommands = options.getCommands().size() +
                optionsAdditional.getCommands().size() - 1;
        assertEquals(numCommands, r.size());
        for (Option<ID> o : options.getCommands()) {
            assertTrue(r.contains(o.getLongName()));
        }
        for (Option<ID> o : optionsAdditional.getCommands()) {
            assertTrue(r.contains(o.getLongName()));
        }

        r = new ArrayList<>();
        complete(" ", r);
        assertEquals(numCommands, r.size());

        r = new ArrayList<>();
        complete("     ", r);
        assertEquals(numCommands, r.size());

        r = new ArrayList<>();
        complete("bibl", r);
        assertEquals(1, r.size());
        assertEquals("bibliography", r.get(0));

        r = new ArrayList<>();
        complete("   bibl", r);
        assertEquals(1, r.size());
        assertEquals("bibliography", r.get(0));

        r = new ArrayList<>();
        complete("ge", r);
        assertEquals(1, r.size());
        assertEquals("get", r.get(0));

        r = new ArrayList<>();
        complete("bla", r);
        assertEquals(0, r.size());
    }

    /**
     * Tests if subcommands can be completed
     * @throws Exception if something goes wrong
     */
    @Test
    public void subcommand() throws Exception {
        ArrayList<CharSequence> r = new ArrayList<>();
        complete("get", r);

        OptionGroup<ID> options = OptionIntrospector.introspect(
                ShellGetCommand.class);
        assertEquals(options.getCommands().size(), r.size());
        for (Option<ID> o : options.getCommands()) {
            assertTrue(r.contains(o.getLongName()));
        }

        r = new ArrayList<>();
        complete("get ", r);
        assertEquals(options.getCommands().size(), r.size());

        r = new ArrayList<>();
        complete("get st", r);
        assertEquals(1, r.size());
        assertEquals("style", r.get(0));

        r = new ArrayList<>();
        complete("get   st", r);
        assertEquals(1, r.size());
        assertEquals("style", r.get(0));

        r = new ArrayList<>();
        complete("get style", r);
        assertEquals(0, r.size());

        r = new ArrayList<>();
        complete("get bla", r);
        assertEquals(0, r.size());
    }

    /**
     * Tests if unknown attributes are ignored
     */
    @Test
    public void unknownAttributes() {
        ArrayList<CharSequence> r = new ArrayList<>();
        complete("get style test", r);
        assertEquals(0, r.size());

        r = new ArrayList<>();
        complete("get style test test2", r);
        assertEquals(0, r.size());
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
        // get number of commands, subtract 1 because there's a HelpCommand
        // and a ShellHelpCommand
        int numCommands = options.getCommands().size() +
                optionsAdditional.getCommands().size() - 1;
        // subtract 1 again because we excluded one command
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
        complete("hel", r);
        assertEquals(1, r.size());
        assertEquals("help", r.get(0));

        r = new ArrayList<>();
        complete("help ge", r);
        assertEquals(1, r.size());
        assertEquals("get", r.get(0));

        r = new ArrayList<>();
        complete("help get st", r);
        assertEquals(1, r.size());
        assertEquals("style", r.get(0));

        r = new ArrayList<>();
        complete("help get", r);
        OptionGroup<ID> options = OptionIntrospector.introspect(ShellGetCommand.class);
        assertEquals(options.getCommands().size(), r.size());
        for (Option<ID> cmd : options.getCommands()) {
            assertTrue(r.contains(cmd.getLongName()));
        }

        r = new ArrayList<>();
        complete("help", r);
        options = OptionIntrospector.introspect(CSLTool.class);
        OptionGroup<ID> optionsAdditional = OptionIntrospector.introspect(
                AdditionalShellCommands.class);
        // get number of commands, subtract 1 because there's a HelpCommand
        // and a ShellHelpCommand
        int numCommands = options.getCommands().size() +
                optionsAdditional.getCommands().size() - 1;
        assertEquals(numCommands, r.size());
    }

    /**
     * Checks if completions for output formats are calculated correctly
     */
    @Test
    public void completeFormats() {
        ArrayList<CharSequence> r = new ArrayList<>();
        complete("set format h", r);
        assertEquals(1, r.size());
        assertEquals("html", r.get(0));

        r = new ArrayList<>();
        complete("set format", r);
        assertEquals(6, r.size());
    }
}

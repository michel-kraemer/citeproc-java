package de.undercouch.citeproc.tool.shell;

import de.undercouch.underline.InputReader;
import jline.console.ConsoleReader;
import jline.console.completer.Completer;
import jline.console.history.History;
import jline.console.history.MemoryHistory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Reads input from the user, but first disables prompt, completions,
 * and history.
 * @author Michel Kraemer
 */
public class ConsoleInputReader implements InputReader {
    private final ConsoleReader reader;

    /**
     * Constructs a new console input reader
     * @param reader the underlying console reader
     */
    public ConsoleInputReader(ConsoleReader reader) {
        this.reader = reader;
    }

    @Override
    public String readLine(String prompt) throws IOException {
        boolean oldHistoryEnabled = reader.isHistoryEnabled();
        History oldHistory = reader.getHistory();
        Collection<Completer> oldCompleters =
                new ArrayList<>(reader.getCompleters());
        String oldPrompt = reader.getPrompt();
        try {
            reader.setHistoryEnabled(false);
            reader.setHistory(new MemoryHistory());
            for (Completer c : oldCompleters) {
                reader.removeCompleter(c);
            }
            return reader.readLine(prompt);
        } finally {
            for (Completer c : oldCompleters) {
                reader.addCompleter(c);
            }
            reader.setPrompt(oldPrompt);
            reader.setHistory(oldHistory);
            reader.setHistoryEnabled(oldHistoryEnabled);
        }
    }
}

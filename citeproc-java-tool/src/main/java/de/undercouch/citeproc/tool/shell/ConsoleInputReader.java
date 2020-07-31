package de.undercouch.citeproc.tool.shell;

import de.undercouch.underline.InputReader;
import org.jline.reader.LineReader;

/**
 * Wraps a {@link LineReader} into an {@link InputReader}
 * @author Michel Kraemer
 */
public class ConsoleInputReader implements InputReader {
    private final LineReader reader;

    /**
     * Constructs a new input reader
     * @param reader the underlying line reader
     */
    public ConsoleInputReader(LineReader reader) {
        this.reader = reader;
    }

    @Override
    public String readLine(String prompt) {
        return reader.readLine(prompt);
    }
}

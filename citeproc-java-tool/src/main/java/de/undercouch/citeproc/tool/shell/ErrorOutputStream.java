package de.undercouch.citeproc.tool.shell;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A filter output stream that colors everything in red on ANSI terminals
 * @author Michel Kraemer
 */
public class ErrorOutputStream extends FilterOutputStream {
    /**
     * True if we're currently writing to the underlying output stream
     */
    private boolean writing = false;

    /**
     * Constructs a new filter output stream
     * @param out the underlying output stream
     */
    public ErrorOutputStream(OutputStream out) {
        super(out);
    }

    @Override
    public void write(int b) throws IOException {
        boolean oldwriting = enableRed();
        super.write(b);
        if (!oldwriting) {
            disableRed();
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        boolean oldwriting = enableRed();
        super.write(b, off, len);
        if (!oldwriting) {
            disableRed();
        }
    }

    /**
     * Enables colored output
     * @return true if the colored output was already enabled before
     * @throws IOException if writing to the underlying output stream failed
     */
    private boolean enableRed() throws IOException {
        boolean oldwriting = writing;
        if (!writing) {
            writing = true;
            byte[] en = "\u001B[31m".getBytes();
            super.write(en, 0, en.length);
        }
        return oldwriting;
    }

    /**
     * Disabled colored output
     * @throws IOException if writing to the underlying output stream failed
     */
    private void disableRed() throws IOException {
        byte[] dis = "\u001B[0m".getBytes();
        super.write(dis, 0, dis.length);
        writing = false;
    }
}

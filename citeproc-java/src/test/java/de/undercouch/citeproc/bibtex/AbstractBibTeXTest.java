package de.undercouch.citeproc.bibtex;

import org.jbibtex.BibTeXDatabase;
import org.jbibtex.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * Abstract base class for BibTeX-related unit tests
 * @author Michel Kraemer
 */
public abstract class AbstractBibTeXTest {
    /**
     * Loads the <code>unix.bib</code> database from the classpath
     * @return the database
     * @throws IOException if the database could not be loaded
     * @throws ParseException if the database is invalid
     */
    protected static BibTeXDatabase loadUnixDatabase() throws IOException, ParseException {
        BibTeXDatabase db;
        try (InputStream is = AbstractBibTeXTest.class.getResourceAsStream("/unix.bib.gz")) {
            GZIPInputStream gis = new GZIPInputStream(is);
            db = new BibTeXConverter().loadDatabase(gis);
        }
        return db;
    }
}

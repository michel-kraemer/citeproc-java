package de.undercouch.citeproc.helper.tool;

import de.undercouch.citeproc.BibliographyFileReader;
import de.undercouch.citeproc.ItemDataProvider;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Works like {@link BibliographyFileReader} but caches bibliography files
 * in memory. Note that this class only caches files but not input
 * streams, so only {@link #readBibliographyFile(File)} is overridden here.
 * The cache is not automatically cleaned (by some background thread for
 * example), so this is by far no ideal implementation. However, for the
 * citeproc-java tool it's more than enough.
 * @author Michel Kraemer
 */
public class CachingBibliographyFileReader extends BibliographyFileReader {
    private final Map<String, SoftReference<ItemDataProvider>> cache = new HashMap<>();

    @Override
    public ItemDataProvider readBibliographyFile(File bibfile) throws IOException {
        clean();

        SoftReference<ItemDataProvider> sr = cache.get(bibfile.getAbsolutePath());
        if (sr != null) {
            ItemDataProvider r = sr.get();
            if (r != null) {
                return r;
            }
        }

        ItemDataProvider r = super.readBibliographyFile(bibfile);
        if (r != null) {
            cache.put(bibfile.getAbsolutePath(), new SoftReference<>(r));
        }

        return r;
    }

    private void clean() {
        cache.entrySet().removeIf(e -> e.getValue().get() == null);
    }
}

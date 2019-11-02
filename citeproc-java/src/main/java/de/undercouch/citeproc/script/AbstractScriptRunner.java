package de.undercouch.citeproc.script;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Abstract base class for {@link ScriptRunner} implementations
 * @author Michel Kraemer
 */
public abstract class AbstractScriptRunner implements ScriptRunner {
    @Override
    public void loadScript(URL url) throws IOException, ScriptRunnerException {
        try (InputStreamReader reader = new InputStreamReader(url.openStream(),
                StandardCharsets.UTF_8)) {
            eval(reader);
        }
    }
}

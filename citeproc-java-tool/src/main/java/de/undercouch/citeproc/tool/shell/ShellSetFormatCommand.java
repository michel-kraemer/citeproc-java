package de.undercouch.citeproc.tool.shell;

import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.helper.tool.ToolUtils;
import de.undercouch.citeproc.tool.AbstractCSLToolCommand;
import de.undercouch.underline.InputReader;
import de.undercouch.underline.UnknownAttributes;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Set the current output format
 * @author Michel Kraemer
 */
public class ShellSetFormatCommand extends AbstractCSLToolCommand implements Completer {
    /**
     * The current formats
     */
    private List<String> formats;

    @Override
    public String getUsageName() {
        return "set format";
    }

    @Override
    public String getUsageDescription() {
        return "Set the current output format";
    }

    /**
     * Sets the current formats
     * @param formats the formats
     */
    @UnknownAttributes("FORMAT")
    public void setFormats(List<String> formats) {
        this.formats = formats;
    }

    @Override
    public boolean checkArguments() {
        if (formats == null || formats.isEmpty()) {
            error("no format specified");
            return false;
        }
        if (formats.size() > 1) {
            error("you can only specify one format");
            return false;
        }

        String f = formats.get(0);
        List<String> supportedFormats = CSL.getSupportedOutputFormats();
        if (!supportedFormats.contains(f)) {
            String message = "unsupported format `" + f + "'";
            String dyms = ToolUtils.getDidYouMeanString(supportedFormats, f);
            if (dyms != null && !dyms.isEmpty()) {
                message += "\n\n" + dyms;
            }
            error(message);
            return false;
        }

        return true;
    }

    @Override
    public int doRun(String[] remainingArgs, InputReader in, PrintWriter out) {
        ShellContext.current().setFormat(formats.get(0));
        return 0;
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        List<String> sf = CSL.getSupportedOutputFormats();

        if (line.word().isEmpty()) {
            List<Candidate> sfc = new ArrayList<>(sf.size());
            for (String f : sf) {
                sfc.add(new Candidate(f));
            }
            candidates.addAll(sfc);
        } else {
            for (String f : sf) {
                if (f.startsWith(line.word())) {
                    candidates.add(new Candidate(f));
                }
            }
        }
    }
}

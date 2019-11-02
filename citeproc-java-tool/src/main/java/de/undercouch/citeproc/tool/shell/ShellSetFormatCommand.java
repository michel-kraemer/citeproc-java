package de.undercouch.citeproc.tool.shell;

import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.helper.tool.ToolUtils;
import de.undercouch.citeproc.tool.AbstractCSLToolCommand;
import de.undercouch.underline.InputReader;
import de.undercouch.underline.OptionParserException;
import de.undercouch.underline.UnknownAttributes;
import jline.console.completer.Completer;

import java.io.IOException;
import java.io.PrintWriter;
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
        try {
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
        } catch (IOException e) {
            // could not check supported output formats. ignore
        }

        return true;
    }

    @Override
    public int doRun(String[] remainingArgs, InputReader in, PrintWriter out)
            throws OptionParserException, IOException {
        ShellContext.current().setFormat(formats.get(0));
        return 0;
    }

    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {
        List<String> sf;
        try {
            sf = CSL.getSupportedOutputFormats();
        } catch (IOException e) {
            // could not get list of supported output formats. ignore.
            return 0;
        }

        if (buffer.trim().isEmpty()) {
            candidates.addAll(sf);
        } else {
            String[] args = buffer.split("\\s+");
            String last = args[args.length - 1];
            for (String f : sf) {
                if (f.startsWith(last)) {
                    candidates.add(f);
                }
            }
        }
        return 0;
    }
}

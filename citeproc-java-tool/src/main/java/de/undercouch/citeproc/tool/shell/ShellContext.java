package de.undercouch.citeproc.tool.shell;

/**
 * A context containing variables that affect the operation of the
 * interactive shell
 * @author Michel Kraemer
 */
public class ShellContext {
    private static final ThreadLocal<ShellContext> current = new ThreadLocal<>();
    private String style = "ieee";
    private String locale = "en-US";
    private String format = "text";
    private String file;

    private ShellContext() {
        // hidden constructor
    }

    /**
     * Enters a new context
     */
    public static void enter() {
        ShellContext ctx = new ShellContext();
        current.set(ctx);
    }

    /**
     * Leaves the current context
     */
    public static void exit() {
        current.remove();
    }

    /**
     * @return the current context
     */
    public static ShellContext current() {
        return current.get();
    }

    /**
     * Sets the current citation style
     * @param style the style
     */
    public void setStyle(String style) {
        this.style = style;
    }

    /**
     * @return the current citation style
     */
    public String getStyle() {
        return style;
    }

    /**
     * Sets the current locale
     * @param locale the locale
     */
    public void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     * @return the current locale
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Sets the current output format
     * @param format the format
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * @return the current output format
     */
    public String getFormat() {
        return format;
    }

    /**
     * Sets the current input file
     * @param file the file
     */
    public void setInputFile(String file) {
        this.file = file;
    }

    /**
     * @return the current input file
     */
    public String getInputFile() {
        return file;
    }
}

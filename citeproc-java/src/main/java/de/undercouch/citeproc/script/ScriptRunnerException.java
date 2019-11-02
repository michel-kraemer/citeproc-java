package de.undercouch.citeproc.script;

/**
 * This exception is thrown by {@link ScriptRunner} if some code could
 * not be executed
 * @author Michel Kraemer
 */
public class ScriptRunnerException extends Exception {
    private static final long serialVersionUID = -5745047071287708797L;

    /**
     * Constructs a new exception
     * @see Exception#Exception()
     */
    public ScriptRunnerException() {
        // nothing to do here
    }

    /**
     * Constructs a new exception with a detail message
     * @param message the detail message
     * @see Exception#Exception(String)
     */
    public ScriptRunnerException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with a specified cause
     * @param cause the cause
     * @see Exception#Exception(Throwable)
     */
    public ScriptRunnerException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new exception with a specified detail message and cause
     * @param message the detail message
     * @param cause the cause
     * @see Exception#Exception(String, Throwable)
     */
    public ScriptRunnerException(String message, Throwable cause) {
        super(message, cause);
    }
}

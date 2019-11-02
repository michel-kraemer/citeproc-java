package de.undercouch.citeproc.script;

import de.undercouch.citeproc.helper.json.JsonBuilderFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;

/**
 * Executes JavaScript scripts
 * @author Michel Kraemer
 */
public interface ScriptRunner extends JsonBuilderFactory, Closeable {
    /**
     * @return the runner's name
     */
    String getName();

    /**
     * @return the runner's version
     */
    String getVersion();

    /**
     * Loads a script from a URL and evaluates it
     * @param url the script's URL
     * @throws IOException if the script could not be loaded
     * @throws ScriptRunnerException if the script is invalid
     */
    void loadScript(URL url) throws IOException, ScriptRunnerException;

    /**
     * Executes a script provided by a given reader
     * @param reader the reader
     * @throws ScriptRunnerException if the script could not be executed
     * @throws IOException if the script could not be read from the reader
     */
    void eval(Reader reader) throws ScriptRunnerException, IOException;

    /**
     * Calls a top-level method
     * @param <T> the type of the return value
     * @param name the method's name
     * @param resultType the expected type of the return value
     * @param args the arguments
     * @return the return value
     * @throws ScriptRunnerException if the method could not be called
     */
    <T> T callMethod(String name, Class<T> resultType, Object... args)
            throws ScriptRunnerException;

    /**
     * Calls a top-level method
     * @param name the method's name
     * @param args the arguments
     * @throws ScriptRunnerException if the method could not be called
     */
    void callMethod(String name, Object... args)
            throws ScriptRunnerException;

    /**
     * Calls an object's method
     * @param <T> the type of the return value
     * @param obj the object
     * @param name the method's name
     * @param resultType the expected type of the return value
     * @param args the arguments
     * @return the return value
     * @throws ScriptRunnerException if the method could not be called
     */
    <T> T callMethod(Object obj, String name, Class<T> resultType, Object... args)
            throws ScriptRunnerException;

    /**
     * Calls an object's method
     * @param obj the object
     * @param name the method's name
     * @param args the arguments
     * @throws ScriptRunnerException if the method could not be called
     */
    void callMethod(Object obj, String name, Object... args)
            throws ScriptRunnerException;

    /**
     * Tries to convert the given object to the given type
     * @param <T> the type to convert to
     * @param o the object to convert
     * @param type the type to convert to
     * @return the converted object
     */
    <T> T convert(Object o, Class<T> type);

    /**
     * Release an object and free up its resources
     * @param o the object to release
     */
    void release(Object o);
}

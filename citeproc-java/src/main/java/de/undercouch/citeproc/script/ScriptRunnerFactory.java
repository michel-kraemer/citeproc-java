package de.undercouch.citeproc.script;

import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Creates {@link ScriptRunner} instances
 * @author Michel Kraemer
 */
public class ScriptRunnerFactory {
    /**
     * The different types of runners this factory can create
     */
    public enum RunnerType {
        /**
         * Automatically detect the JavaScript engine
         */
        AUTO,

        /**
         * Create a runner that explicitly uses the Java Scripting API
         */
        JRE,

        /**
         * Create a runner that explicitly uses the V8 runtime
         */
        V8,

        /**
         * Create a runner that explicitly uses GraalVM JavaScript
         */
        GRAALJS
    }

    /**
     * The runner to create
     */
    private static AtomicReference<RunnerType> runner =
            new AtomicReference<>(RunnerType.AUTO);

    /**
     * Sets the type of runners this factory creates. The default type is
     * {@link RunnerType#AUTO}. Normally, you shouldn't have to change this.
     * Only use this method if you're know what you're doing (for testing
     * purpose for example)
     * @param type the type
     * @return the previous type
     */
    public static RunnerType setRunnerType(RunnerType type) {
        return runner.getAndSet(type);
    }

    /**
     * @return a {@link ScriptRunner} that uses the Java Scripting API
     */
    private static ScriptRunner createJreRunner() {
        return new JREScriptRunner();
    }

    /**
     * @return a {@link ScriptRunner} that uses the V8 runtime
     */
    private static ScriptRunner createV8Runner() {
        return new V8ScriptRunner();
    }

    /**
     * @return a {@link ScriptRunner} that uses GraalVM JavaScript
     */
    private static ScriptRunner createGraalRunner() {
        return new GraalScriptRunner();
    }

    /**
     * Creates a new {@link ScriptRunner} instance according to the
     * type set with {@link #setRunnerType(RunnerType)}
     * @return the script runner
     */
    public static ScriptRunner createRunner() {
        RunnerType t = runner.get();

        switch (t) {
            case AUTO:
                if (supportsV8()) {
                    return createV8Runner();
                }
                if (SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_11)) {
                    return createGraalRunner();
                }
                // fall back to JRE
                return createJreRunner();

            case JRE:
                return createJreRunner();

            case V8:
                return createV8Runner();

            case GRAALJS:
                return createGraalRunner();

            default:
                throw new RuntimeException("Invalid runner type");
        }
    }

    /**
     * @return true if the V8 runtime is available, false otherwise
     */
    private static boolean supportsV8() {
        try {
            Class.forName("com.eclipsesource.v8.V8");
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
}

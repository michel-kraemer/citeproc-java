package de.undercouch.citeproc.script;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

final class RhinoScriptRunnerPreferences
{
	private static final Logger LOGGER = Logger.getLogger(RhinoScriptRunnerPreferences.class.getName());

	private final int compilerOptimizationLevel;
	private final boolean enableClassCache;

	private RhinoScriptRunnerPreferences(int compilerOptimizationLevel, boolean enableClassCache) {
		this.compilerOptimizationLevel = compilerOptimizationLevel;
		this.enableClassCache = enableClassCache;
	}

	public static RhinoScriptRunnerPreferences getInstance() {
		return Holder.INSTANCE;
	}

	public int getCompilerOptimizationLevel() {
		return compilerOptimizationLevel;
	}

	public boolean isEnableClassCache() {
		return enableClassCache;
	}

	static class Holder {
		private static final RhinoScriptRunnerPreferences INSTANCE = init();

		static final String RHINO_COMPILER_OPTIMIZATION = "de.undercouch.citeproc.script.rhino.compiler.opt.level";
		static final String RHINO_CLASS_CACHE_ENABLED = "de.undercouch.citeproc.script.rhino.classCache.enabled";

		static RhinoScriptRunnerPreferences init() {
			return new RhinoScriptRunnerPreferences(
				getCompilerOptimizationLevel(),
				getClassCacheEnabled()
			);
		}

		private static int getCompilerOptimizationLevel() {
			String value = System.getProperty(RHINO_COMPILER_OPTIMIZATION, "0");
			try {
				return Integer.parseInt(value);
			} catch (RuntimeException e) {
				return useDefault(Level.WARNING, RHINO_COMPILER_OPTIMIZATION, 0, value, e);
			}
		}

		private static boolean getClassCacheEnabled() {
			String value = System.getProperty(RHINO_CLASS_CACHE_ENABLED, "true");

			// more strict than Boolean.parseBoolean(...)
			if("true".equalsIgnoreCase(value)) {
				return true;
			} else if("false".equalsIgnoreCase(value)) {
				return false;
			}

			return useDefault(Level.WARNING, RHINO_CLASS_CACHE_ENABLED, true, value, null);
		}

		private static <T> T useDefault(Level level, String property, T defaultValue, String valueFound, Throwable t) {
			if (LOGGER.isLoggable(level)) {
				LogRecord logRecord = new LogRecord(level, "Invalid value for system property [{0}] = [{2}]; using default value of [{1}].");
				logRecord.setParameters(new Object[]{property, defaultValue, valueFound});
				logRecord.setLoggerName(LOGGER.getName());
				if(null != t) {
					logRecord.setThrown(t);
				}
				LOGGER.log(logRecord);
			}
			return defaultValue;
		}
	}
}

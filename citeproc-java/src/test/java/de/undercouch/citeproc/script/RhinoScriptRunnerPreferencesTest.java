package de.undercouch.citeproc.script;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;

import static de.undercouch.citeproc.script.RhinoScriptRunnerPreferences.Holder.*;
import static org.junit.Assert.assertEquals;

public class RhinoScriptRunnerPreferencesTest
{
	@Test
	public void testDefaultValues() throws Exception {
		RhinoScriptRunnerPreferences pref = RhinoScriptRunnerPreferences.Holder.init();

		assertEquals("Default compiler level", 0, pref.getCompilerOptimizationLevel());
		assertEquals("Default caching", true, pref.isEnableClassCache());
	}

	@Test
	public void testCustomCompilerLevel() throws Exception {
		System.setProperty(RHINO_COMPILER_OPTIMIZATION, "-1");

		RhinoScriptRunnerPreferences pref = RhinoScriptRunnerPreferences.Holder.init();

		assertEquals("Custom compiler level", -1, pref.getCompilerOptimizationLevel());
		assertEquals("Default caching", true, pref.isEnableClassCache());
	}

	@Test
	public void testInvalidCustomCompilerLevelResortsToDefault() throws Exception {
		System.setProperty(RHINO_COMPILER_OPTIMIZATION, "hello world");

		RhinoScriptRunnerPreferences pref = RhinoScriptRunnerPreferences.Holder.init();

		assertEquals("Default compiler level", 0, pref.getCompilerOptimizationLevel());
		assertEquals("Default caching", true, pref.isEnableClassCache());
	}

	@Test
	public void testCustomCaching() throws Exception {
		System.setProperty(RHINO_CLASS_CACHE_ENABLED, "false");

		RhinoScriptRunnerPreferences pref = RhinoScriptRunnerPreferences.Holder.init();

		assertEquals("Default compiler level", 0, pref.getCompilerOptimizationLevel());
		assertEquals("Custom caching", false, pref.isEnableClassCache());
	}

	@Test
	public void testInvalidCustomCachingResortsToDefault() throws Exception {
		System.setProperty(RHINO_CLASS_CACHE_ENABLED, "hello world");

		RhinoScriptRunnerPreferences pref = RhinoScriptRunnerPreferences.Holder.init();

		assertEquals("Default compiler level", 0, pref.getCompilerOptimizationLevel());
		assertEquals("Default caching", true, pref.isEnableClassCache());
	}

	private static String capturedOptLevel;
	private static String capturedCacheEnabled;

	@BeforeClass
	public static void capture() {
		capturedOptLevel = System.getProperty(RHINO_COMPILER_OPTIMIZATION);
		capturedCacheEnabled = System.getProperty(RHINO_CLASS_CACHE_ENABLED);
	}

	@AfterClass
	public static void restore() {
		restore(RHINO_COMPILER_OPTIMIZATION, capturedOptLevel);
		restore(RHINO_CLASS_CACHE_ENABLED, capturedCacheEnabled);
	}

	private static void restore(String prop, String capturedValue) {
		if (null != capturedValue) {
			System.setProperty(prop, capturedValue);
		} else {
			System.clearProperty(prop);
		}
	}

	@Before
	public void setupMethod() {
		System.clearProperty(RHINO_COMPILER_OPTIMIZATION);
		System.clearProperty(RHINO_CLASS_CACHE_ENABLED);
	}
}
// Copyright 2013 Michel Kraemer
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package de.undercouch.citeproc.script;

import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.undercouch.citeproc.CSLTest;
import de.undercouch.citeproc.bibtex.BibTeXItemDataProviderTest;
import de.undercouch.citeproc.script.ScriptRunnerFactory.RunnerType;

/**
 * Makes sure a citeproc-related tests run with the {@link JREScriptRunner}
 * @author Michel Kraemer
 */
@RunWith(Suite.class)
@SuiteClasses({ CSLTest.class, BibTeXItemDataProviderTest.class })
public class JREScriptRunnerTestSuite {
	private static RunnerType prev;
	
	/**
	 * Sets the runner to use
	 */
	@BeforeClass
	public static void setUp() {
		if (!SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_7)) {
			//don't use the JRE runner with Java 1.6. It would fail anyway.
			prev = ScriptRunnerFactory.setRunnerType(RunnerType.AUTO);
		} else {
			prev = ScriptRunnerFactory.setRunnerType(RunnerType.JRE);
		}
	}
	
	/**
	 * Resets the runner
	 */
	@AfterClass
	public static void tearDown() {
		ScriptRunnerFactory.setRunnerType(prev);
	}
}

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

import java.lang.reflect.Array
import org.fusesource.jansi.Ansi
import org.fusesource.jansi.AnsiConsole
import org.python.core.PyString
import org.python.core.PySystemState
import org.python.util.PythonInterpreter

class TestSuite {
    private def project
    private def cl
    private def processorPyFile
    private def testFiles
    
    TestSuite(project, zip, citeprocZip) {
        this.project = project
        
        def zipFiles = project.zipTree(zip)
        testFiles = zipFiles.filter {
            it.toURI().getPath() =~ /.*\/processor-tests\/humans\/.*\.txt/ }
        processorPyFile = zipFiles.find { it.getPath().endsWith('processor.py') }
        
        def citeprocZipFiles = project.zipTree(citeprocZip)
        def citeprocLocales = citeprocZipFiles.find  { it.toURI().path =~ /.*\/locales\/.*/ }
        citeprocLocales = citeprocLocales.parentFile.toURI().toURL()
        
        def cp = project.test.classpath
        def urls = [ citeprocLocales ] + cp.files.collect { it.toURI().toURL() }
        cl = new URLClassLoader(urls.toArray(new URL[urls.size()]))
    }
    
    def compile() {
        int count = testFiles.files.size()
        print("Compiling ${count} tests ...")
        def state = new PySystemState()
        state.setCurrentWorkingDir(processorPyFile.getParent())
        state.argv.clear()
        state.argv.append(new PyString(processorPyFile.getPath()))
        state.argv.append(new PyString('-g'))
        def interp = new PythonInterpreter(null, state)
        interp.execfile(processorPyFile.getPath())
        interp.cleanup()
    }
    
    def fix() {
        println('Fixing invalid tests ...')
        
        //we cannot parse '"label": 0'. use a reasonable default value.
        fixInFile('bugreports_NumericStyleFirstRefMultipleCiteFailure.json',
            '"label": 0,', '"label": "note",')
    }
    
    private def fixInFile(name, s, d) {
        def dir = new File(processorPyFile.getParentFile(), 'processor-tests/machines')
        def f = new File(dir, name)
        def found = false
        def tempFile = File.createTempFile('citeproc', 'json')
        tempFile.withWriter { w ->
            f.eachLine { line ->
                if (line.indexOf(s) >= 0) {
                    found = true
                }
                w << line.replace(s, d) + "\n"
            }
        }
        f.delete()
        tempFile.renameTo(f)
        if (!found) {
            println("WARNING: Could not fix ${name}. Replace string ${s} not " +
                "found. Maybe the test has been updated and should " +
                "not be fixed anymore.")
        }
    }
    
    def run() {
        def dir = new File(processorPyFile.getParentFile(), 'processor-tests/machines')
        
        def runnerTypeClass = cl.loadClass('de.undercouch.citeproc.script.ScriptRunnerFactory$RunnerType')
        def testSuiteRunnerClass = cl.loadClass('de.undercouch.citeproc.TestSuiteRunner')
        def testSuiteRunner = testSuiteRunnerClass.newInstance()
        if (project.ext.properties.containsKey('scriptRunnerType')) {
            testSuiteRunner.runTests(dir, runnerTypeClass[
                    project.ext.scriptRunnerType.toUpperCase()])
        } else {
            testSuiteRunner.runTests(dir, runnerTypeClass.AUTO)
        }
    }
}

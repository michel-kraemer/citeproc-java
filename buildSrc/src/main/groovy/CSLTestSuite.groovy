import org.python.core.PyString
import org.python.core.PySystemState
import org.python.util.PythonInterpreter

class CSLTestSuite {
    private def project
    private def cl
    private def testPyFile
    
    CSLTestSuite(project, path) {
        this.project = project
        
        testPyFile = new File(path, 'fixtures/std/processor.py')
        
        def cp = project.test.classpath
        def urls = cp.files.collect { it.toURI().toURL() }
        cl = new URLClassLoader(urls.toArray(new URL[urls.size()]))
    }
    
    def compile() {
        println("Compiling tests ...")
        def state = new PySystemState()
        state.setCurrentWorkingDir(testPyFile.getParent())
        state.argv.clear()
        state.argv.append(new PyString(testPyFile.getPath()))
        state.argv.append(new PyString('-g'))
        def interp = new PythonInterpreter(null, state)
        interp.execfile(testPyFile.getPath())
        interp.cleanup()
    }
    
    def fix() {
        println('Fixing invalid tests ...')
        
        // type 'film' does not comply with the standard
        // fixInFile('bugreports_MissingItemInJoin.json',
        //     '"type": "film"', '"type": "motion_picture"')
    }
    
    private def fixInFile(name, s, d) {
        def dir = new File(testPyFile.getParentFile(), 'processor-tests/machines')
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
        def dir = new File(testPyFile.getParentFile(), 'processor-tests/machines')

        def oldContextClassLoader = Thread.currentThread().contextClassLoader
        Thread.currentThread().setContextClassLoader(cl)
        try {
            def runnerTypeClass = cl.loadClass('de.undercouch.citeproc.script.ScriptRunnerFactory$RunnerType')
            def testSuiteRunnerClass = cl.loadClass('de.undercouch.citeproc.TestSuiteRunner')
            def testSuiteRunner = testSuiteRunnerClass.newInstance()
            if (project.ext.properties.containsKey('scriptRunnerType')) {
                testSuiteRunner.runTests(dir, runnerTypeClass[
                        project.ext.scriptRunnerType.toUpperCase()])
            } else {
                testSuiteRunner.runTests(dir, runnerTypeClass.AUTO)
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldContextClassLoader)
        }
    }
}

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

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.text.GStringTemplateEngine
import org.antlr.v4.Tool
import org.apache.commons.io.FileUtils
import org.eclipse.jface.text.Document
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.ToolFactory
import org.eclipse.jdt.core.formatter.CodeFormatter

class SourceGenerator {
    private def project
    
    SourceGenerator(project) {
        this.project = project
    }
    
    private def normalize(String s, String c) {
        int da
        while ((da = s.indexOf(c)) > 0) {
            s = s.substring(0, da) + Character.toUpperCase(s.charAt(da + 1)).toString() +
                s.substring(da + 2)
        }
        return s
    }
    
    private def normalize(String s) {
        s = normalize(normalize(s, '-'), '_')
        if (s.equals('abstract')) {
            s = 'abstrct'
        }
        return s
    }
    
    private def renderTemplate(template, attrs, dst, outName) {
        def e = new GStringTemplateEngine()
        def t = e.createTemplate(new File(project.projectDir, "templates/$template")).make(attrs)
        def r = t.toString()
        
        def options = [
            (JavaCore.COMPILER_SOURCE): "1.6",
            (JavaCore.COMPILER_COMPLIANCE): "1.6",
            (JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM): "1.6"
        ]
        def codeFormatter = ToolFactory.createCodeFormatter(options)
        def textEdit = codeFormatter.format(CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS,
            r, 0, r.length(), 0, null)
        def doc = new Document(r)
        if (textEdit != null) {
            textEdit.apply(doc)
        }
        FileUtils.writeStringToFile(new File(dst, outName), doc.get(), 'UTF-8')
    }
    
    private def addFunctionsToAttrs(attrs) {
        attrs.toEnum = { s ->
            s.toUpperCase().replace('-', '_').replace(' ', '_').replace('/', '_')
        }
        
        attrs.toGetter = { s ->
            'get' + Character.toUpperCase(s.charAt(0)).toString() + s.substring(1)
        }
        
        attrs.toEllipse = { s ->
            if (s.endsWith('[]')) {
                s = s.substring(0, s.length() - 2) + '...'
            }
            return s
        }
    }
    
    private def renderTemplatesInternal(name, dst, type = false) {
        def om = new ObjectMapper()
        def attrs = om.readValue(new File(project.projectDir, "templates/${name}.json"), Map)
        
        if (attrs.props != null) {
            attrs.requiredProps = []
            for (p in attrs.props) {
                if (p.normalizedName == null) {
                    p.normalizedName = normalize(p.name)
                }
                if (p.required == null) {
                    p.required = false
                }
                if (p.required) {
                    attrs.requiredProps += p
                }
                p.enumType = (p.type.equals("CSLType") || p.type.equals("CSLLabel") || p.type.equals("Context"))
                p.cslType = (p.type.startsWith("CSL"))
                if (p.type.endsWith("[]")) {
                    p.arrayType = true
                    p.typeNoArray = p.type.substring(0, p.type.length() - 2)
                } else {
                    p.arrayType = false
                    p.typeNoArray = p.type
                }
                if (p.type.endsWith("[][]")) {
                    p.arrayArrayType = true
                    p.typeNoArrayNoArray = p.type.substring(0, p.type.length() - 4)
                } else {
                    p.arrayArrayType = false
                    p.typeNoArrayNoArray = p.type
                }
            }
            attrs.props.removeAll(attrs.requiredProps)
        }

        if (attrs.additionalImports == null) {
            attrs.additionalImports = []
        }

        if (attrs.additionalMethods == null) {
            attrs.additionalMethods = []
        }
        
        if (attrs.additionalBuilderMethods == null) {
            attrs.additionalBuilderMethods = []
        }
        
        if (attrs.additionalFromJsonCode == null) {
            attrs.additionalFromJsonCode = []
        }
        
        if (attrs.noJsonObject == null) {
            attrs.noJsonObject = false
        }
        
        if (attrs.shortname == null) {
            attrs.shortname = ""
        }
        
        addFunctionsToAttrs(attrs)
        
        dst = new File(dst, attrs.pkg.replaceAll('\\.', '/'))
        dst.mkdirs()
        
        if (type) {
            renderTemplate("Type.java", attrs, dst, "${name}.java")
        } else {
            renderTemplate("Object.java", attrs, dst, "${name}.java")
            if (attrs.noBuilder == null || !attrs.noBuilder) {
                renderTemplate("Builder.java", attrs, dst, "${name}Builder.java")
            }
        }
    }
    
    private def renderParserTemplate(name, dst) {
        def om = new ObjectMapper()
        def attrs = om.readValue(new File(project.projectDir, "templates/${name}.json"), Map)
        
        addFunctionsToAttrs(attrs)
        
        dst = new File(dst, attrs.pkg.replaceAll('\\.', '/'))
        dst.mkdirs()
        
        renderTemplate("Parser.java", attrs, dst, "${name}.java")
        renderTemplate("Converter.java", attrs, dst, "${attrs.desc}Converter.java")
        renderTemplate("Library.java", attrs, dst, "${attrs.desc}Library.java")
        renderTemplate("ItemDataProvider.java", attrs, dst, "${attrs.desc}ItemDataProvider.java")
    }
    
    private def renderGrammar(name, dst) {
        def filename = new File(project.projectDir, "grammars/${name}.g4").absolutePath
        def tool = new Tool()
        tool.outputDirectory = dst
        tool.haveOutputDir = true
        def grast = tool.parseGrammar(filename)
        grast.fileName = filename
        def g = tool.createGrammar(grast)
        g.fileName = filename
        tool.process(g, true)
    }
    
    def renderTemplates() {
        def dst = new File(project.projectDir, 'src-gen/main/java')
        dst.mkdirs()
        
        renderTemplatesInternal('Context', dst, true)
        renderTemplatesInternal('CSLType', dst, true)
        renderTemplatesInternal('CSLLabel', dst, true)
        renderTemplatesInternal('SecondFieldAlign', dst, true)
        renderTemplatesInternal('SelectionMode', dst, true)
        renderTemplatesInternal('EndNoteType', dst, true)
        renderTemplatesInternal('RISType', dst, true)
        
        renderTemplatesInternal('CSLAbbreviationList', dst)
        renderTemplatesInternal('CSLCitation', dst)
        renderTemplatesInternal('CSLCitationItem', dst)
        renderTemplatesInternal('CSLDate', dst)
        renderTemplatesInternal('CSLItemData', dst)
        renderTemplatesInternal('CSLName', dst)
        renderTemplatesInternal('CSLProperties', dst)
        renderTemplatesInternal('EndNoteReference', dst)
        renderTemplatesInternal('RISReference', dst)
        
        renderTemplatesInternal('Bibliography', dst)
        renderTemplatesInternal('Citation', dst)

        renderTemplatesInternal('VariableWrapperParams', dst)
        
        renderParserTemplate('EndNoteParser', dst)
        renderParserTemplate('RISParser', dst)
    }
    
    def renderGrammars() {
        def dst = new File(project.projectDir, 'src-gen/main/java')
        dst.mkdirs()
        
        renderGrammar('InternalName', new File(dst, 'de/undercouch/citeproc/bibtex/internal'))
        renderGrammar('InternalPage', new File(dst, 'de/undercouch/citeproc/bibtex/internal'))
    }
    
    def filterScripts() {
        def src = new File(project.projectDir, 'src-gen/main/resources/de/undercouch/citeproc/citeproc.js')
        def dst = new File(project.projectDir, 'src-gen/main/resources/de/undercouch/citeproc/dateparser.js')
        filterScript(src, dst, 'CSL.DateParser = new function () {', '};')
    }
    
    def filterScript(src, dst, start, end) {
        src.withReader { r ->
            dst.withWriter { w ->
                def include = false
                def line
                while ((line = r.readLine()) != null) {
                    if (line == start) {
                        include = true
                    }
                    if (include) {
                        dst << line
                        dst << "\n"
                        if (line == end) {
                            break
                        }
                    }
                }
            }
        }
    }
    
    def generateVersionFile() {
        def dstRes = new File(project.projectDir, 'src-gen/main/resources')
        def versionFile = new File(dstRes, 'de/undercouch/citeproc/version.dat')
        versionFile.withWriter { w ->
            w << project.version
        }
    }
}

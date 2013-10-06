import com.fasterxml.jackson.databind.ObjectMapper
import org.antlr.v4.Tool
import org.apache.commons.io.FileUtils
import org.eclipse.jface.text.Document
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.ToolFactory
import org.eclipse.jdt.core.formatter.CodeFormatter
import org.stringtemplate.v4.AttributeRenderer
import org.stringtemplate.v4.ST
import org.stringtemplate.v4.STGroupFile

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
    
    private def renderTemplate(template, attrs, dst, outName, stg) {
        def t = FileUtils.readFileToString(new File('templates', template), 'UTF-8')
        def st = new ST(stg, t)
        for (def e in attrs) {
            st.add(e.key, e.value)
        }
        
        def r = st.render()
        
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
    
    private def renderTemplatesInternal(name, dst, stg, type = false) {
        def om = new ObjectMapper()
        def attrs = om.readValue(new File('templates', "${name}.json"), Map)
        
        if (attrs.properties != null) {
            attrs.requiredProperties = []
            for (p in attrs.properties) {
                if (p.normalizedName == null) {
                    p.normalizedName = normalize(p.name)
                }
                if (p.required == null) {
                    p.required = false
                }
                if (p.required) {
                    attrs.requiredProperties += p
                }
                p.enumType = (p.type.equals("CSLType") || p.type.equals("CSLLabel"))
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
            attrs.properties.removeAll(attrs.requiredProperties)
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
        
        dst = new File(dst, attrs.package.replaceAll('\\.', '/'))
        dst.mkdirs()
        
        if (type) {
            renderTemplate("Type.java", attrs, dst, "${name}.java", stg)
        } else {
            renderTemplate("Object.java", attrs, dst, "${name}.java", stg)
            if (attrs.noBuilder == null || !attrs.noBuilder) {
                renderTemplate("Builder.java", attrs, dst, "${name}Builder.java", stg)
            }
        }
    }
    
    private def renderGrammar(name, dst) {
        def filename = "grammars/${name}.g4"
        def tool = new Tool()
        tool.outputDirectory = dst
        tool.haveOutputDir = true
        def grast = tool.loadGrammar(filename)
        grast.fileName = filename
        def g = tool.createGrammar(grast)
        g.fileName = filename
        tool.process(g, true)
    }
    
    def renderTemplates() {
        def ar = new AttributeRenderer() {
            String toString(Object o, String formatString, Locale locale) {
                def s = o.toString()
                if (formatString.equals('toEnum')) {
                    return s.toUpperCase().replaceAll('-', '_')
                } else if (formatString.equals('toGetter')) {
                    s = Character.toUpperCase(s.charAt(0)).toString() + s.substring(1)
                    return "get${s}"
                } else if (formatString.equals('toEllipse')) {
                    if (s.endsWith('[]')) {
                        s = s.substring(0, s.length() - 2) + '...'
                    }
                    return s
                } else if (formatString.equals('toStrEqualsT')) {
                    def r = 'str.equals("' + s + '")'
                    if (s.indexOf('-') >= 0) {
                        return r + ' || str.equals("' + s.replace('-', ' ') + '")'
                    }
                    return r
                } else if (formatString.equals('castBefore')) {
                    if (s.equals("String")) {
                        return ""
                    } else if (s.equals("int") || s.equals("Integer")) {
                        return "toInt("
                    } else if (s.equals("boolean") || s.equals("Boolean")) {
                        return "toBool("
                    } else {
                        return "(" + s + ")";
                    }
                } else if (formatString.equals('castAfter')) {
                    if (s.equals("String")) {
                        return ".toString()"
                    } else if (s.equals("int") || s.equals("Integer") ||
                            s.equals("boolean") || s.equals("Boolean")) {
                        return ")"
                    } else {
                        return ""
                    }
                }
                return s
            }
        }
        
        def stg = new STGroupFile('templates/functions.stg', '$' as char, '$' as char)
        stg.registerRenderer(String, ar)
        
        def dst = new File('src-gen/main/java')
        dst.mkdirs()
        
        renderTemplatesInternal('CSLType', dst, stg, true)
        renderTemplatesInternal('CSLLabel', dst, stg, true)
        renderTemplatesInternal('SecondFieldAlign', dst, stg, true)
        renderTemplatesInternal('SelectionMode', dst, stg, true)
        
        renderTemplatesInternal('CSLAbbreviationList', dst, stg)
        renderTemplatesInternal('CSLCitation', dst, stg)
        renderTemplatesInternal('CSLCitationItem', dst, stg)
        renderTemplatesInternal('CSLDate', dst, stg)
        renderTemplatesInternal('CSLItemData', dst, stg)
        renderTemplatesInternal('CSLName', dst, stg)
        renderTemplatesInternal('CSLProperties', dst, stg)
        
        renderTemplatesInternal('Bibliography', dst, stg)
        renderTemplatesInternal('Citation', dst, stg)
    }
    
    def renderGrammars() {
        def dst = new File('src-gen/main/java')
        dst.mkdirs()
        
        renderGrammar('InternalName', new File(dst, 'de/undercouch/citeproc/bibtex/internal'))
        renderGrammar('InternalPage', new File(dst, 'de/undercouch/citeproc/bibtex/internal'))
    }
    
    def compileScripts() {
        def dstRes = new File('src-gen/main/resources')
        for (s in project.fileTree(dir: 'src/main/resources/de/undercouch/citeproc', include: '*.js')) {
            org.mozilla.javascript.tools.jsc.Main.main([ '-opt', '9',
                '-package', 'de.undercouch.citeproc', '-nosource', '-encoding', 'UTF-8',
                '-d', dstRes.toString(), s.toString() ] as String[])
            def name = s.getName().substring(0, s.getName().length() - 2)
            def dstFile = new File(dstRes, 'de/undercouch/citeproc/' + name + 'dat')
            dstFile.delete()
            new File(dstRes, 'de/undercouch/citeproc/' + name + 'class').renameTo(dstFile)
        }
    }
    
    def generateVersionFile() {
        def dstRes = new File('src-gen/main/resources')
        def versionFile = new File(dstRes, 'de/undercouch/citeproc/version.dat')
        versionFile.withWriter { w ->
            w << project.version
        }
    }
}

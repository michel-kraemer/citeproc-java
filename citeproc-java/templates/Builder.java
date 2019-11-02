package $pkg;

import java.util.Map;

<% for (i in additionalImports) { %>
import ${i};
<% } %>

/**
 * Builder for {@link $name}
 * @author Michel Kraemer
 */
public class ${name}Builder {
    <% for (p in requiredProps) { %>private ${p.type} ${p.normalizedName};
    <% } %>
    <% for (p in props) { %>private ${p.type} ${p.normalizedName};
    <% } %>
    
    public ${name}Builder(<% if (requiredProps.size > 1) { for (p in requiredProps[0..-2]) { %>${p.type} ${p.normalizedName},<% } } %><% if (!requiredProps.empty) { %>
            ${toEllipse.call(requiredProps[-1].type)} ${requiredProps[-1].normalizedName}
            <% } %>) {
        <% for (p in requiredProps) { %>this.${p.normalizedName} = ${p.normalizedName};
        <% } %>
        <% for (p in props) { %>this.${p.normalizedName} = <% if (p.defval) { %>${p.defval}<% } else { %>null<% } %>;
        <% } %>
    }
    
    <% for (p in props) { %>
    public ${name}Builder ${p.normalizedName}(${toEllipse.call(p.type)} ${p.normalizedName}) {
        this.${p.normalizedName} = ${p.normalizedName};
        return this;
    }
    <% } %>
    
    /**
     * Creates a builder that copies properties from the given original object
     * @param original the original object
     */
    public ${name}Builder($name original) {
        ${requiredProps.collect({ p -> "this." + p.normalizedName + " = original." + toGetter.call(p.normalizedName) + "();" }).join('\n')}
        ${props.collect({ p-> "this." + p.normalizedName + " = original." + toGetter.call(p.normalizedName) + "();" }).join('\n')}
    }
    
    public $name build() {
        return new $name(${requiredProps.collect({ p-> p.normalizedName }).join(',')}<% if (!requiredProps.empty && !props.empty) { %>,<% } %>${props.collect({ p -> p.normalizedName }).join(',')});
    }
    
    ${additionalBuilderMethods.join('\n')}
}

package $pkg;

/**
 * $description
 * @author Michel Kraemer
 */
public enum $name {
    <% out << types.collect({ t->
        toEnum.call(t) + '("' + t+ '")'
    }).join(',') %>;
    
    private final String name;
    
    $name(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    /**
     * Converts the given string to a $name
     * @param str the string
     * @return the converted $name
     */
    public static $name fromString(String str) {
        <% for (t in types) { %> if (<%
            def r = 'str.equals("' + t + '")'
            if (t.indexOf('-') >= 0) {
                r = r + ' || str.equals("' + t.replace('-', ' ') + '")'
            }
            out << r
        %>) {
            return ${toEnum.call(t)};
        }<% } %>
        throw new IllegalArgumentException("Unknown $name: " + str);
    }
}

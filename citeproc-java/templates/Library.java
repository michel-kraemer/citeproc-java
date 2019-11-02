package $pkg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A container for $desc references
 * @author Michel Kraemer
 */
public class ${desc}Library {
    private final List<${desc}Reference> references = new ArrayList<>();
    
    /**
     * Adds a reference to this library
     * @param reference the reference to add
     */
    public void addReference(${desc}Reference reference) {
        references.add(reference);
    }
    
    /**
     * @return an unmodifiable list of references in this library
     */
    public List<${desc}Reference> getReferences() {
        return Collections.unmodifiableList(references);
    }
}

package content;

import java.net.URI;
import org.w3c.dom.Document;
import utils.domer;

/**
 * Holds a template 
 * @author  bs
 */
public class Template {

    /** The name, the key to this template*/
    private String m_name;
    /** The absolute URI to template*/
    private URI m_absoluteUri;

    /**
     * Constructor
     * @param name The name of the template, used as Module attribute
     * @param loc The location of the template
     */
    public Template(String name, URI loc) {
        m_name = name;
        m_absoluteUri = loc;
    }

    /**
     * get the name, id, of the template
     * @return the name
     */
    public String getName() {
        return m_name;
    }

    /**
     * get the absolute URI for this template
     * @return the absolute URI
     */
    public URI getabsoluteURI() {
        return m_absoluteUri;
    }

    /**
     * get the DOM
     * @param mod The Module using the template
     * @return The Document
     * @throws Exception If we fail to parse 
     */
    public Document getDoc(Module mod)
            throws Exception {
        return domer.makeDomFromUriSomeHow(m_absoluteUri, mod.getEncoding(), mod.getReporter());
    }

    @Override
    public String toString() {

        String s = "Template: " + m_name + "  at " + m_absoluteUri.toString();
        return s;
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package references;

import content.Definitions;
import content.Module;
import indexing.IndexItem;
import indexing.indexable;
import java.util.List;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import utils.PIcommand;
import reporting.reporter;

/**
 *
 * @author Administrator
 */
public class IEEEReference extends SimpleReference {

    /**
     * Constructor
     * @param elt The element describing the reference
     * @param r The active reporter
     * @throws Exception If we fail
     */
    public IEEEReference(Element elt, reporter r)
            throws Exception {
        super(elt, r);

    }

    /**
     * Create and return a single reference to be included in the text
     * @param m The module that request the element
     * @param cmd The command describing the request
     * @return the reference as an element
     */
    @Override
    public DocumentFragment produceIntextElement(Module m, PIcommand cmd) {

        DocumentFragment df = m.getDoc().createDocumentFragment();
        Element refElt = m.getDoc().createElement("span");
        refElt.setAttribute("class", cmd.getCommandStyleName());
        Node n_ref = null;
        String reftext = "";

        // set as requested
        String index = EMPTY;
        if (cmd.paramExist(PIcommand.INDEX)) {
            index = cmd.getValue(PIcommand.INDEX);
        }

        // display according to style
        // display IEE style
        reftext += " [" + index + "] ";


        n_ref = m.getDoc().createTextNode(reftext.trim());
        refElt.appendChild(n_ref);
        df.appendChild(refElt);
        return df;
        //return refElt;
    }

    /**
     * Create and return a  li-element for an referencelist
     * The actual fields and which are mandatory for differents styles
     * are implemented here, and no other place
     * @param mod The module requesting the referencelist
     * @return A Node containing the element in the refencelist.
     */
    @Override
    public DocumentFragment produceListElement(Module mod, PIcommand cmd, List<IndexItem> users) {
        String style = mod.getDefinitions().getOption(Definitions.REFERENCE_FORM);
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        Element liElt = mod.getDoc().createElement("li");
        liElt.setAttribute("class", "refitem skiptranslate " + style);

        // do IEEE
        liElt.appendChild(m_propertMap.containsKey(AUTHORS_IN_LIST)
                ? liElt.appendChild(makePropertyElement(AUTHORS_IN_LIST, mod))
                : liElt.appendChild(makePropertyElement("title", mod)));
        liElt.appendChild(makePropertyElement("year", mod));
        if (m_propertMap.containsKey("chaptertitle")) {
            liElt.appendChild(makePropertyElement("chaptertitle", mod));
        }
        if (m_propertMap.containsKey("journal")) {
            liElt.appendChild(makePropertyElement("journal", mod));
        }
        if (m_propertMap.containsKey("editor")) {
            liElt.appendChild(makePropertyElement("editor", mod));
        }
        if (!m_propertMap.containsKey(AUTHORS_IN_LIST)) {
            liElt.appendChild(makePropertyElement("title", mod));
        }
        if (m_propertMap.containsKey("place")) {
            liElt.appendChild(makePropertyElement("place", mod));
        }
        if (m_propertMap.containsKey("publisher")) {
            liElt.appendChild(makePropertyElement("publisher", mod));
        }
        if (m_propertMap.containsKey("pages")) {
            liElt.appendChild(makePropertyElement("pages", mod));
        }
        if (m_propertMap.containsKey("isbn")) {
            liElt.appendChild(makePropertyElement("isbn", mod));
        }
        if (m_propertMap.containsKey("text")) {
            liElt.appendChild(makePropertyElement("text", mod));
        }
        if (m_propertMap.containsKey("uri")) {
            liElt.appendChild(makeUriElement(m_propertMap.get("uri"), mod));
        }
        if (m_propertMap.containsKey("dateread")) {
            liElt.appendChild(makePropertyElement("dateread", mod));
        }


        df.appendChild(liElt);
        return df;
        //return liElt;

    }

    /**
     * Get the type of reference
     * @return IEEE
     */
    public String getRefType() {
        return Definitions.IEEE;
    }

    @Override
    public int compareTo(indexable o) {
        // basically they are as they are registered
        return 0;
        //return getSortKey().compareTo(((IEEEReference)o).getSortKey());
    }
}

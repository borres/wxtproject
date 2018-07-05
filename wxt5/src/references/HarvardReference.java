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
 * Handling refrences of type Harvard
 * @author Administrator
 */
public class HarvardReference extends SimpleReference {

    /**
     * Constructor
     * @param elt The element describing the reference
     * @param r The active reporter
     * @throws Exception If we fail
     */
    public HarvardReference(Element elt, reporter r)
            throws Exception {
        super(elt, r);
        if (m_propertMap.containsKey(AUTHORS_IN_LIST)) {
            m_propertMap.put(SORTKEY, m_propertMap.get(AUTHORS_IN_LIST));
        }

    }

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

        // display harvard style
        String s = "";
        if (m_propertMap.containsKey(AUTHORS_IN_TEXT)) {
            s += accessProperty(AUTHORS_IN_TEXT);
        } else {
            s += accessProperty("title");
        }
        if (m_propertMap.containsKey("year")) {
            reftext += "(" + s + ", " + accessProperty("year") + ")";
        } else {
            reftext += "(" + s + ")";
        }


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
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        String style = mod.getDefinitions().getOption(Definitions.REFERENCE_FORM);
        Element liElt = mod.getDoc().createElement("li");
        // move to stylesheet ?
        liElt.setAttribute("style", "list-style-type:square");
        liElt.setAttribute("class", "refitem skiptranslate " + style);


        // do harvard
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
     * @return HARVARD
     */
    @Override
    public String getRefType() {
        return Definitions.HARVARD;
    }

    @Override
    public int compareTo(indexable o) {
        HarvardReference other = (HarvardReference) o;
        int v = -1;
        try {
            v = getSortKey().trim().compareTo(other.getSortKey().trim());
            if (v == 0) {
                try {
                    int y1 = Integer.parseInt(getYear().trim());
                    int y2 = Integer.parseInt(other.getYear().trim());
                    // Latest work will be ordered first
                    v = y1 > y2 ? -1 : 1;
                } catch (NumberFormatException nfe) {
                    v = 0;
                }
            }
            return v;
        } catch (NullPointerException e) {
            return 1;
        }
    }
}

package references;

import content.Module;
import indexing.IndexItem;
import java.util.HashMap;
import java.util.List;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import utils.PIcommand;
import wxt.Options;

/**
 *
 * @author Administrator
 */
public class IEEEStyle{



    /**
     * Create and return a single reference to be included in the text
     * @param m The module that request the element
     * @param cmd The command describing the request
     * @return the reference as an element
     */
    public static DocumentFragment getIntextElement(Module m, PIcommand cmd,Reference ref) {

        DocumentFragment df = m.getDoc().createDocumentFragment();
        Element refElt = m.getDoc().createElement("span");
        refElt.setAttribute("class", cmd.getCommandStyleName());
        Node n_ref = null;
        String reftext = "";

        // set as requested
        String index = Reference.EMPTY;
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
    public static DocumentFragment getListElement(Module mod, PIcommand cmd, Reference ref, List<IndexItem> users) {
        String style = mod.getScriptHandler().getOption(Options.REFERENCE_FORM);
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        Element liElt = mod.getDoc().createElement("li");
        liElt.setAttribute("class", "refitem skiptranslate " + style);
        HashMap<String,String> m_propertMap=ref.m_propertMap;

        // do IEEE
        liElt.appendChild(m_propertMap.containsKey(ref.AUTHORS_IN_LIST)
                ? liElt.appendChild(ref.makePropertyElement(ref.AUTHORS_IN_LIST, mod))
                : liElt.appendChild(ref.makePropertyElement("title", mod)));
        liElt.appendChild(ref.makePropertyElement("year", mod));
        if (m_propertMap.containsKey("chaptertitle")) {
            liElt.appendChild(ref.makePropertyElement("chaptertitle", mod));
        }
        if (m_propertMap.containsKey("journal")) {
            liElt.appendChild(ref.makePropertyElement("journal", mod));
        }
        if (m_propertMap.containsKey("editor")) {
            liElt.appendChild(ref.makePropertyElement("editor", mod));
        }
        if (!m_propertMap.containsKey(ref.AUTHORS_IN_LIST)) {
            liElt.appendChild(ref.makePropertyElement("title", mod));
        }
        if (m_propertMap.containsKey("place")) {
            liElt.appendChild(ref.makePropertyElement("place", mod));
        }
        if (m_propertMap.containsKey("publisher")) {
            liElt.appendChild(ref.makePropertyElement("publisher", mod));
        }
        if (m_propertMap.containsKey("pages")) {
            liElt.appendChild(ref.makePropertyElement("pages", mod));
        }
        if (m_propertMap.containsKey("isbn")) {
            liElt.appendChild(ref.makePropertyElement("isbn", mod));
        }
        if (m_propertMap.containsKey("text")) {
            liElt.appendChild(ref.makePropertyElement("text", mod));
        }
        if (m_propertMap.containsKey("uri")) {
            liElt.appendChild(ref.makeUriElement(m_propertMap.get("uri"), mod));
        }
        if (m_propertMap.containsKey("dateread")) {
            liElt.appendChild(ref.makePropertyElement("dateread", mod));
        }


        df.appendChild(liElt);
        return df;
        //return liElt;

    }




}

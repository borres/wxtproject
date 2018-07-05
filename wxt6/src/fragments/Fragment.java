package fragments;

import content.Definitions;
import content.Module;
import indexing.IndexItem;
import indexing.Indexable;
import java.net.URI;
import java.util.List;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import reporting.reporter;
import utils.PIcommand;
import utils.accessutils;
import wxt.Options;
import xmldom.domer;

/**
 * Holds an XML fragment.
 * Fragments are created from the script and from fragments/authors files
 * @author bs
 */
public class Fragment extends Frag {

    /** the URI where the actual content is found, if not directly quoted */
    protected URI m_absoluteUri;
    /** the anchor for all addressing elements in the fragment*/
    protected URI m_referenceUri;
    /** the (script)element where it is defined */
    protected Element m_Element;
    /** the definitions that owns the fragment */
    Definitions m_def;
    /** the short value */
    protected String m_short;

    /**
     * Constructs an single Fragment from a scriptelement.
     *
     * @param elt The element (in the script) where the fragment is described
     * @param u The absolute URI where the content exists. May be null
     * @param def The Definions instance in work
     * @throws java.lang.Exception When we fail to establish the structure
     */
    public Fragment(Element elt, URI u, Definitions def)
            throws Exception {
        m_absoluteUri = u;
        m_Element = elt;
        m_def = def;

        // the scripts encoding as default:
        m_encoding = elt.getOwnerDocument().getXmlEncoding();
        if (m_encoding == null) {
            m_encoding = def.getScriptHandler().getOption(Options.DEFAULT_ENCODING);
        }
        if (elt.hasAttribute("encoding")) {
            m_encoding = elt.getAttribute("encoding");
        }

        // id
        if (elt.hasAttribute(Definitions.ID)) {
            m_id = elt.getAttribute(Definitions.ID);
        } else {
            throw new Exception(reporter.getBundleString("missing_fragment_id"));
        }

        // short
        if (elt.hasAttribute(Definitions.SHORT)) {
            m_short = elt.getAttribute(Definitions.SHORT);
        } else {
            m_short = m_id; // thus we always a short value
        }
        // we get hold of the text either from a file or directly as quoted in the element
        if (m_absoluteUri != null) {
            // load it from file
            m_txt = accessutils.getBOMSafeTextFile(m_absoluteUri, m_encoding);
        } else {
            // we  pick it up from the element itself
            // and we use the scriptaddress as referenceUri
            m_referenceUri = def.getScriptHandler().getScriptAbsoluteUri();
            m_txt = getContentFromElement();
        }



        // content defaults to short value
        if ((m_txt == null) || (m_txt.isEmpty())) {
            m_txt = m_short;
        } else {
            m_txt = m_def.substituteFragments(m_txt);
        }

        m_txt = m_txt.trim();

        //-----------------------
        // ok we have the text. Now we must make an XML-fragment

        // first we make sure we dont have any bad &'s
        m_txt = accessutils.fixUriParameterEntities(m_txt);


        // should we fix encoding so it match that of the script ?
        /*
        if(!encoderutils.sameEncoding(m_encoding,elt.getOwnerDocument().getXmlEncoding()))
        {
        m_txt=encoderutils.reEncode(m_txt,m_encoding,elt.getOwnerDocument().getXmlEncoding(),false);
        m_encoding=elt.getOwnerDocument().getXmlEncoding();
        }*/

        try {
            m_docfrag = domer.produceDocFragmentFromString(m_txt, elt.getOwnerDocument().getXmlEncoding(), def.getReporter());
        } catch (Exception e) {
            throw new Exception("fragment id:" + m_id + " / " + e.getMessage());
        }
    }

    /**
     * Pick up content from an element.
     * Content is text or CDATA among elements children
     *
     * @return the content of the scriptelement as text or CDATA
     * @throws java.lang.Exception when something goes wrong
     */
    private String getContentFromElement()
            throws Exception {
        String s = "";
        NodeList nlist = m_Element.getChildNodes();
        if (nlist.getLength() == 0) {
            return s;
        }
        Node tn = nlist.item(0);
        try {
            while (tn != null) {
                short typ = tn.getNodeType();
                if ((typ == Node.TEXT_NODE) || (typ == Node.CDATA_SECTION_NODE)) {
                    s += tn.getNodeValue().trim();
                }
                tn = tn.getNextSibling();
            }
        } catch (Exception e) {
            throw new Exception("fragment id:" + m_id + " / " + e.getMessage());
        }
        return s;
    }

    /**
     * Get the absolute, reference uri for this fragment
     * @return the refernce uri
     */
    public URI getReferenceUri() {
        return m_referenceUri;
    }

    /**
     * Get the name field
     * @return a string, always non-null
     */
    public String getShort() {
        return m_short;
    }

    /**
     * Display this fragment
     * @param mod The Module requesting the content
     * @param cmd The PIcommand describing the request
     * @return a DocumentFragment, always
     */
    @Override
    public DocumentFragment produceIntextElement(Module mod, PIcommand cmd) {
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        // what type of display do we want
        String displayType = PIcommand.FULL; // default
        if (cmd.paramExist(PIcommand.FORM)) {
            String tmp = cmd.getValue(PIcommand.FORM);
            if (tmp.compareTo(PIcommand.ID) == 0) {
                displayType = tmp;
            }
            if (tmp.compareTo(PIcommand.SHORT) == 0) {
                displayType = tmp;
            }
        }

        // we dont want translation of id and short
        if (displayType.compareTo(PIcommand.ID) == 0) {
            Element spanElt = mod.getDoc().createElement("span");
            spanElt.setAttribute("class", PIcommand.SKIPTRANSLATE);
            spanElt.appendChild(mod.getDoc().createTextNode(getId()));
            df.appendChild(spanElt);
            return df;
        }
        if (displayType.compareTo(PIcommand.SHORT) == 0) {
            Element spanElt = mod.getDoc().createElement("span");
            spanElt.setAttribute("class", PIcommand.SKIPTRANSLATE);
            spanElt.appendChild(mod.getDoc().createTextNode(getShort()));
            df.appendChild(spanElt);
            return df;
        }
        // full
        df = (DocumentFragment) getFragment().cloneNode(true);
        // must be done in producer: correctAddressing(df, mod, getReferenceUri());
        return (DocumentFragment) (mod.getDoc().importNode(df, true));
    }

    /**
     * Produce a fragment describing this fragment in a list
     * @param mod The module who does the request 
     * @param cmd The command describing the request
     * @param users All users of this fragment
     * @return A documentfragment
     */
    @Override
    public DocumentFragment produceListElement(Module mod, PIcommand cmd, List<IndexItem> users) {
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        // pick up common display properties
        // should we drop default author
        boolean dropDefault = true;
        if (cmd.paramExist(PIcommand.DROPDEFAULT)) {
            dropDefault = cmd.getValue(PIcommand.DROPDEFAULT).compareToIgnoreCase(PIcommand.YES) == 0;
        }

        // columns
        int cols = 1;
        if (cmd.paramExist(PIcommand.COLS)) {
            cols = Math.max(cmd.getValueAsInteger(PIcommand.COLS), 1);
        }

        // short
        boolean shortform = true;
        if (cmd.paramExist(PIcommand.SHOW)) {
            shortform = cmd.getValue(PIcommand.SHOW).compareToIgnoreCase(PIcommand.SHORT) == 0;
        }

        // prepare tmpwrapper with columns
        Element wrapper = null;
        if (cols > 0) {
            wrapper = mod.getDoc().createElement("div");
            wrapper.setAttribute("class", cmd.getCommandStyleName() + String.valueOf(cols));
        } else {
            wrapper = mod.getDoc().createElement("span");
            wrapper.setAttribute("class", cmd.getCommandStyleName() + String.valueOf(cols));
        }
        // DL - list with author and written modules
        Element listhead = mod.getDoc().createElement("dt");
        if (shortform) {
            listhead.appendChild(mod.getDoc().createTextNode(getShort()));
            listhead.setAttribute("class", PIcommand.SKIPTRANSLATE);
        } else {
            Node tmp = mod.getDoc().importNode(getFragment(), true);
            // since we get a documentfragment:
            tmp = tmp.getFirstChild();
            if ((tmp == null) || (tmp.getNodeType() != Node.ELEMENT_NODE)) {
                listhead.setAttribute("class", PIcommand.SKIPTRANSLATE);
            } else if ((!((Element) tmp).hasAttribute("class"))
                    || (((Element) tmp).getAttribute("class").indexOf(PIcommand.SKIPTRANSLATE) == -1)) {
                listhead.setAttribute("class", PIcommand.SKIPTRANSLATE);
            }


            listhead.appendChild(mod.getDoc().importNode(getFragment(), true));
        }

        df.appendChild(listhead);

        // then run list of potential modules
        for (IndexItem ui : users) {
            //Element listelt=mod.getProducer().makeXLink(mod, ui.getMod(), "dd", null, Producer.NO_LEVEL_ADJUST, true);
            Element listelt = mod.makeXLink(ui.getMod(), "dd", null, -999, true);
            df.appendChild(listelt);
        }
        return df;
    }

    @Override
    public String toString() {
        return "\tFragment: " + m_id + "\n\t" + m_txt;
    }

    @Override
    public int compareTo(Indexable t) {
        return getCompareValue().compareTo(t.getCompareValue());
    }

    @Override
    public String getCompareValue() {
        return m_short;
    }
}

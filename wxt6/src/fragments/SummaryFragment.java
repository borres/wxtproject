package fragments;

import content.Module;
import indexing.IndexItem;
import indexing.Indexable;
import java.util.List;
import org.w3c.dom.DocumentFragment;
import utils.PIcommand;
import utils.accessutils;
import xmldom.domer;

/**
 * Keep summary for modules, specified as summary-element or 
 * defaults to description attribute, which in turn defaults to name
 * SummaryFragments are only owned by the Module
 * @author bs
 */
public class SummaryFragment extends Frag {

    Module m_mod;

    /**
     * Produce a new SummaryFragment from a simple (CDATA)string
     * These fragments are not stored in Definitions fragmentlist
     * The module use this for storing the content of the description attribute
     * or the summary element
     * @param txt The string defining the fragment
     * @param mod The module defining the fragment
     * @throws java.lang.Exception
     */
    public SummaryFragment(String txt, Module mod)
            throws Exception {
        m_mod = mod;
        m_txt = txt;
        m_txt = accessutils.fixUriParameterEntities(m_txt);
        // substitute pathfragments
        m_txt = mod.getDefinitions().substituteFragments(m_txt);
        m_encoding = mod.getEncoding();
        try {
            m_docfrag = domer.produceDocFragmentFromString(m_txt, m_encoding, mod.getReporter());
        } catch (Exception e) {
            // error is reported from user
            throw e;
        }
    }

    /**
     * Display this summary fragment
     * @param mod The Module requesting the content
     * @param cmd The PIcommand describing the request
     * @return a DocumentFragment, always
     */
    @Override
    public DocumentFragment produceIntextElement(Module mod, PIcommand cmd) {
        return (DocumentFragment) mod.getDoc().importNode(m_docfrag, true);
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
        return produceIntextElement(mod, cmd);
    }

    @Override
    public int compareTo(Indexable t) {
        return getCompareValue().compareTo(t.getCompareValue());
    }

    @Override
    public String getCompareValue() {
        if (m_mod != null) {
            return m_mod.getDescription();
        }
        return m_txt;
    }
}

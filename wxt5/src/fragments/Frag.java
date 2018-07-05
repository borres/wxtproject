package fragments;

import content.Definitions;
import indexing.indexable;
import java.net.URI;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import utils.accessutils;

/**
 * Basic for Fragment, AuthorFragment and SummaryFragment
 * @author bs
 */
public abstract class Frag implements indexable {

    /** the fragment as string */
    protected String m_txt;
    /** encoding as found as element attribute */
    protected String m_encoding;
    /** the parsed fragment */
    protected DocumentFragment m_docfrag;
    /** the id*/
    protected String m_id;

    /**
     * Get the DocumentFragment.
     * This method allways deliver
     * @return the fragment
     */
    public DocumentFragment getFragment() {
        // will allways be a legal DocumentFragment
        // all constructors in subclasses throw exception if 
        // the DocumetFragment is not produced
        return m_docfrag;
    }

    /**
     * Get the raw text that describes the DocumentFragment.
     * Name if no content is given
     * @return the fragment as a string
     */
    public String getFragmentAsString() {
        return m_txt;
    }

    /**
     * Get the encoding.
     * @return  The current encoding for this fragment as a string
     */
    public String getEncoding() {
        return m_encoding;
    }

    /**
     * get the id
     * @return The id
     */
    @Override
    public String getId() {
        return m_id;
    }

    /**
     * Fragments are never made on the fly, 
     * they are defined in the script (or on files)
     * @return false
     */
    @Override
    public boolean madeOnTheFly() {
        return false;
    }

    //-------------------------------------------------------
    // statics
    /**
     * Make fragments from all fragment/author-elements in the script
     * or elements in a fragments/authors file
     * @param nlist A list of fragment-elements
     * @param type Is FRAGMENT or AUTHOR
     * @param def The Definitions involved
     */
    public static void makeFragments(NodeList nlist, Definitions def, String type) {
        for (int ix = 0; ix < nlist.getLength(); ix++) {
            Element elt = (Element) nlist.item(ix);
            URI tmpuri = null;
            String tmpname = null;
            try {

                if (!elt.hasAttribute(Definitions.ID)) {
                    def.getReporter().pushMessage("missing_fragment_id");
                    continue;
                }

                if (elt.hasAttribute(Definitions.LOCATION)) {
                    String tmploc = elt.getAttribute(Definitions.LOCATION);
                    tmploc = def.substituteFragments(tmploc);
                    tmpuri = accessutils.makeAbsoluteURI(tmploc, def.getBuildCatalog());
                }

                if (type.equals(Definitions.FRAGMENT)) {
                    def.registerNewFragment(new Fragment(elt, tmpuri, def));
                } else// it is an AUTHOR                 
                {
                    def.registerNewAuthor(new AuthorFragment(elt, tmpuri, def));
                }
            } catch (Exception e) {
                def.getReporter().pushMessage("bad_fragment_element", tmpname);
            }
        }
    }
}

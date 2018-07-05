package fragments;

import content.Definitions;
import content.Module;
import indexing.IndexItem;
import java.net.URI;
import java.util.List;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import utils.PIcommand;

/**
 * Describing an outhor
 * Authors are basically Fragments
 * @author Administrator
 */
public class AuthorFragment extends Fragment {

    /**
     * Constructs an single AuthorFragment from a scriptelement.
     *
     * @param elt The element (in the script) where the fragment is described
     * @param u The absolute URI where the content exists. May be null
     * @param def The Definions instance in work
     * @throws java.lang.Exception When we fail to establish the structure
     */
    public AuthorFragment(Element elt, URI u, Definitions def)
            throws Exception {
        super(elt, u, def);

    }

    /**
     * Make a display of a single simple fragment
     * @param mod
     * @param cmd
     * @return 
     */
    @Override
    public DocumentFragment produceIntextElement(Module mod, PIcommand cmd) {
        // Google translation may be an issue ?
        // is turned off in super, maybe only here ?
        return super.produceIntextElement(mod, cmd);

    }

    /**
     * Make a display of a fragment and where it is used
     * @param mod The module that will present this lis
     * @param cmd The command describing the show
     * @param users The indexentries that hold module<->fragment pairs
     * @return An element
     */
    @Override
    public DocumentFragment produceListElement(Module mod, PIcommand cmd, List<IndexItem> users) {
        // Google translation may be an issue ?
        // is turned off in super, maybe only here ?       
        return super.produceListElement(mod, cmd, users);
    }

    @Override
    public String toString() {
        return m_short;

    }
}

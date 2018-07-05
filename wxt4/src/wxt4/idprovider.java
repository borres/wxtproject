package wxt4;

import java.util.HashMap;

/**
 * WXT needs to produce id's for elements i html-pages.
 * This class generates unique ids for a page
 * @author bs
 */
public class idprovider {

    /** holding the last used id for a module */
    protected HashMap<String, Integer> m_idmap;

    /**
     * Construct a new provider and init hashmap
     */
    public idprovider() {
        m_idmap = new HashMap<String, Integer>(100);
    }

    /**
     * Get a unique id for a module
     * @param pageid The id for the module
     * @return An ID consisting of the id + index
     */
    public String getUniqueID(String pageid) {
        // make sure it works as an id, that is as a html name attribute
        String refname = pageid;
        for (int six = 0; six < refname.length(); six++) {
            char c = refname.charAt(six);
            if (!Character.isLetterOrDigit(c)) {
                refname = refname.replace(c, '_');
            }
        }
        pageid = refname;

        if (m_idmap.containsKey(pageid)) {
            m_idmap.put(pageid, m_idmap.get(pageid) + 1);
        } else {
            m_idmap.put(pageid, 1);
        }
        return pageid + m_idmap.get(pageid);
    }
}

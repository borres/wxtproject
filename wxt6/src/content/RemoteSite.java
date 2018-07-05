package content;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import utils.PIcommand;
import utils.accessutils;
import wxt.Options;
import xmldom.domer;

/**
 * The purpose of this class is to analyze a remote script
 * and do nescessary addresscalculations.
 * It is solely depending on the information given in the script
 * It is used by: producer:makePI_CollectRemote as a response to
 * a PI: collect-remote
 *
 * NOTE: modules generated as Modulelists in the script is ignored
 * that means that ScriptListParser functionality is not involved
 */
public class RemoteSite {

    private URI m_absLocation = null;
    private String m_anchorCat = null;
    private NodeList m_Modules = null;
    private Document m_Doc = null;
    private HashMap<String, String> m_pathFragments = null;
    private List<Element> m_selectedModules = null;
    private HashMap<String, String> m_addressing = null;
    private String m_encoding = null;

    /**
     * Constructor
     * @param location The address of the sites script
     * @throws Exception When we can not parese the script
     */
    public RemoteSite(String location)
            throws Exception {
        m_absLocation = new URI(location);
        // attempt to establish DOM from script, 
        // stringaccess or tidy has no meaning here ?
        try {
            m_Doc = domer.makeDomFromUri(m_absLocation,true, null);
        } catch (Exception e) {
            throw new Exception("Cannot build DOM");
        }
        // attemptp to set up modulelist
        m_Modules = m_Doc.getElementsByTagName("module");
        // handle all path fragments
        collectFromScript((Element) m_Doc.getElementsByTagName("definitions").item(0));

        // fix addressing in script
        try {
            correctModuleAddresses();
        } catch (Exception e1) {
            throw new Exception("Addressing error");
        }

    }

    private void collectFromScript(Element defElt) {
        m_pathFragments = new HashMap<String, String>();
        m_addressing = new HashMap<String, String>();

        // this is more or less the same as pathfragment collection
        // in Definitions
        if (!defElt.hasChildNodes()) {
            return;
        }
        // first we cehck if we have an attribute, anchor, that
        // change the reference from scriptpath
        if (defElt.hasAttribute(Definitions.ANCHOR)) {
            m_anchorCat = defElt.getAttribute(Definitions.ANCHOR);
        }

        // ------ pathfragments first since we need these in other adressing
        // set up defaults
        String s = accessutils.removeFilePart(m_absLocation).toString();
        m_pathFragments.put("_scriptcatalog", s);
        m_pathFragments.put("_scriptpath", m_absLocation.toString());
        m_pathFragments.put("_scripturi", m_absLocation.toString());
        if (m_anchorCat != null) {
            m_pathFragments.put("_scriptanchor", m_anchorCat);
        }

        NodeList nlist = defElt.getElementsByTagName(Definitions.PATHFRAGMENT);
        for (int ix = 0; ix < nlist.getLength(); ix++) {
            Element elt = (Element) nlist.item(ix);
            if ((elt.hasAttribute(Definitions.ID)) && (elt.hasAttribute(Definitions.VALUE))) {
                String val = elt.getAttribute(Definitions.VALUE);

                if (elt.hasAttribute(Definitions.ALTERNATIVE)) {
                    // which pathfragment to choose depends on the OS
                    val = accessutils.selectPathValue(val, elt.getAttribute(Definitions.ALTERNATIVE));
                }
                val = substituteFragments(val);
                m_pathFragments.put(elt.getAttribute(Definitions.ID), val);
            } else {
                //m_reporter.pushMessage("bad_pathfragment_element");
            }
        }
        //---------------------------------------
        // -- addressing elements
        // set up default addressing
        m_addressing.put("a", "href");
        m_addressing.put("link", "href");
        m_addressing.put("img", "src");
        m_addressing.put("script", "src");
        nlist = defElt.getElementsByTagName("addressing");
        for (int ix = 0; ix < nlist.getLength(); ix++) {
            Element elt = (Element) nlist.item(ix);
            String tmpname = elt.getAttribute("tag");
            String tmpvalue = elt.getAttribute("attribute");
            if (elt.hasAttribute("cancel") && (elt.getAttribute("cancel").compareToIgnoreCase("yes") == 0)) {
                m_addressing.remove(tmpname);
            } else {
                m_addressing.put(tmpname, tmpvalue);
            }
        }
        //----------------------------
        // options, we are only interested in encoding
        nlist = defElt.getElementsByTagName("option");
        for (int ix = 0; ix < nlist.getLength(); ix++) {
            Element elt = (Element) nlist.item(ix);
            String tmpname = elt.getAttribute("name");
            String tmpvalue = elt.getAttribute("value");
            if (tmpname.compareTo(Options.DEFAULT_ENCODING) == 0) {
                m_encoding = tmpvalue;
            }
        }
    }

    /**
     * Correct addressing for modules
     * @throws Exception When URI generation fails
     */
    private void correctModuleAddresses()
            throws Exception {
        // TODO: 
        // this is a little bit more complicated since we should
        // account for anchors
        for (int ix = 0; ix < m_Modules.getLength(); ix++) {
            Element module = (Element) m_Modules.item(ix);
            String origloc = module.getAttribute(PIcommand.LOCATION);
            origloc = substituteFragments(origloc);
            if (m_anchorCat == null) {
                URI theURI = accessutils.makeAbsoluteURI(origloc, m_absLocation.toString());
                module.setAttribute(PIcommand.LOCATION, theURI.toString());
            } else {
                //???
                URI theURI = accessutils.makeAbsoluteURI(m_anchorCat, m_absLocation.toString());
                theURI = accessutils.makeAbsoluteURI(origloc, theURI.toString());
                module.setAttribute(PIcommand.LOCATION, theURI.toString());
            }
        }

    }

    /**
     * Return a list of elements (describing modules) that
     * are selected and ordered. Selection by idlist, books or xpath
     * @param cmd The command describing the request (idlist,books,xpath)
     * @return The ordered and filtered list of elements
     */
    public List<Element> getSelectedModuleList(PIcommand cmd) {
        // if we need it more than once
        if (m_selectedModules != null) {
            return m_selectedModules;
        }

        // make it, first time
        m_selectedModules = new ArrayList<Element>(10);
        // pick out the modules we eant in the order we want them
        // according to values in cmd.
        if (cmd.paramExist(PIcommand.IDLIST)) {
            //  we go for ids
            String[] wanted = cmd.getValue(PIcommand.IDLIST).split(",");
            for (int wix = 0; wix < wanted.length; wix++) {
                for (int mix = 0; mix < m_Modules.getLength(); mix++) {
                    Element e = (Element) m_Modules.item(mix);
                    String id = e.getAttribute("name");
                    if (e.hasAttribute("id")) {
                        id = e.getAttribute("id");
                    }
                    if (id.compareTo(wanted[wix]) == 0) {
                        m_selectedModules.add(e);
                    }

                }
            }
        }
        if (cmd.paramExist(PIcommand.BOOKS)) {
            // we go for books
            String[] wanted = cmd.getValue(PIcommand.BOOKS).split(",");
            for (int wix = 0; wix < wanted.length; wix++) {
                for (int mix = 0; mix < m_Modules.getLength(); mix++) {
                    Element e = (Element) m_Modules.item(mix);
                    String books = "," + e.getAttribute("books") + ",";
                    // does the books contain wanted[wix]
                    if (books.indexOf("," + wanted[wix] + ",") != -1) {
                        boolean found = false;
                        for (int fix = 0; fix < m_selectedModules.size() && !found; fix++) {
                            if (m_selectedModules.get(fix) == e) {
                                found = true;
                            }
                        }
                        if (!found) {
                            m_selectedModules.add(e);
                        }
                    }

                }
            }
        }
        if (cmd.paramExist(PIcommand.SCRIPTPATH)) {
            // we go for xpath
            try {
                NodeList mlist = domer.performXPathQuery(m_Doc, cmd.getValue(PIcommand.SCRIPTPATH));
                for (int nix = 0; nix < mlist.getLength(); nix++) {
                    Element e = (Element) m_Modules.item(nix);
                    boolean found = false;
                    for (int fix = 0; fix < m_selectedModules.size() && !found; fix++) {
                        if (m_selectedModules.get(fix) == e) {
                            found = true;
                        }
                    }
                    if (!found) {
                        m_selectedModules.add(e);
                    }
                }
            } catch (Exception ex) {
            }

        }
        return m_selectedModules;
    }

    /**
     * Same as in definitions
     * @param loc The string we want to correct
     * @return The corrected string
     */
    private String substituteFragments(String loc) {
        // must check them all
        // must check them all
        Set<String> keys = m_pathFragments.keySet();
        for (Iterator<String> k = keys.iterator(); k.hasNext();) {
            String ks = k.next();
            String sub = m_pathFragments.get(ks);

            // to make \ survive
            sub = sub.replace("\\", "\\\\");
            String ktmp = "\\Q${" + ks + "}\\E";
            loc = loc.replaceAll(ktmp, sub);
            if (loc.indexOf("${") == -1) {
                return loc;
            }
        }
        if (loc.indexOf("${") != -1) {
            // an unknown pathfragment
            //m_reporter.pushMessage("unknown_patfragment", loc);
        }
        return loc;
    }

    /**
     * Addresses is recalculated so they become absolute
     * @param doc The DOM we will readdress
     * @param refaddress The basic absolute reference address
     */
    public void reAddress(Document doc, String refaddress) {
        Set<String> keys = m_addressing.keySet();
        for (Iterator<String> k = keys.iterator(); k.hasNext();) {
            String tag = k.next();
            String att = m_addressing.get(tag);

            NodeList nodlist = doc.getElementsByTagName(tag);

            for (int ix = 0; ix < nodlist.getLength(); ix++) {
                Element thisElt = (Element) nodlist.item(ix);
                if (!thisElt.hasAttribute(att)) {
                    continue;
                }
                try {
                    String s = thisElt.getAttribute(att).trim();
                    if (s.length() > 0) {
                        String sourcecat = accessutils.removeFilePart(new URI(refaddress)).toString();
                        URI absUri = accessutils.makeAbsoluteURI(s, sourcecat);
                        thisElt.setAttribute(att, absUri.toString());
                    }
                } catch (Exception e) {
                    String tmp = e.getMessage();
                    //target.getReporter().pushMessage("error_in_readdressing", tmp);
                }
            }
        }
    }

    /**
     * Get the encoding used in the remote site
     * @return The encoding
     */
    public String getEncoding() {
        return m_encoding;
    }
}

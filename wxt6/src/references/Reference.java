package references;

import content.Definitions;
import content.Module;
import indexing.IndexItem;
import indexing.Indexable;
import java.util.HashMap;
import java.util.List;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import reporting.reporter;
import utils.PIcommand;
import wxt.Options;

/**
 * Implements a reference
 * 
 */
public class Reference implements Indexable {
    /*
    A reference has no style. The style is produced when the reference is
    displayed in running text or in a reference list.
    There is no control of which elements are set from the element in the constructor.
    Control is assumed in schema, prior to reference construction.
    The method produceListElement describes the elements we care about and the order they are presented
    for differenet styles.
     */
    // reference attributes, as in the refs.xsd file used for validating reference files
    // id,title,authors,year,chaptertitle,journal,editor,place,publisher,pages,isbn,uri(dateread),text,categories

    /** Used when displaying mandatory, unsupplied fields*/
    public static final String EMPTY = "?";
    /** keys for constructed properties*/
    public static String AUTHORS_IN_LIST = "authorsinlist";
    public static String AUTHORS_IN_TEXT = "authorsintext";
    public static String SORTKEY = "sortkey";
    /** Id of the reference as read from referenceelement */
    protected String m_id;
    /** Map containing all elements of the reference */
    protected HashMap<String, String> m_propertMap;

    @Override
    public String getId() {
        return m_id;
    }

    /**
     * @param elt an element from a  reference file
     * @param r the reporter in use
     * @throws java.lang.Exception if the reference cannot be made due to missing items
     */
    public Reference(Element elt, reporter r) throws Exception {

        // If no id is found, an exception is thrown
        if (elt.hasAttribute(Definitions.ID)) {
            m_id = elt.getAttribute(Definitions.ID);
        } else {
            throw new Exception(reporter.getBundleString("missing_reference_data", Definitions.ID));
        }

        m_propertMap = new HashMap<>(6);

        // pick up fields
        // no control that they are legal, assume validated
        // in any case: only legals will be displayed
        NodeList elts = elt.getChildNodes();
        for (int eix = 0; eix < elts.getLength(); eix++) {
            if (elts.item(eix).getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) elts.item(eix);
                if ((e.getNodeName().trim().compareTo("uri") == 0)
                        && (e.hasAttribute("dateread"))) {
                    m_propertMap.put("dateread", e.getAttribute("dateread"));
                }
                String tmp = e.getTagName();
                if (m_propertMap.containsKey(tmp)) {
                    // field appears again
                    r.pushMessage("repeated_field_in_ref", m_id, tmp);
                } else {
                    m_propertMap.put(e.getTagName(), e.getTextContent().trim());
                }
            }
        }
        // any other reason why we should reject it ?
        // no, we display it with the necessary entries and display empty
        // when the display style needs it and the prop is empty

        // prepare author strings for list and intext (harvard)
        prepareAuthorStrings(accessProperty("authors"));

        //decide sortkey
        if (m_propertMap.containsKey("title")) {
            m_propertMap.put("SORTKEY", m_propertMap.get("title"));
        } else if (m_propertMap.containsKey("publisher")) {
            m_propertMap.put("SORTKEY", m_propertMap.get("publisher"));
        } else if (m_propertMap.containsKey("publisher")) {
            m_propertMap.put("SORTKEY", m_propertMap.get("authors"));
        } else {
            m_propertMap.put("SORTKEY", "any");
        }

    }

    /**
     * Create and return a single reference to be included in the text
     * @param mod The module that request the element
     * @param cmd The command describing the request
     * @return the reference as an element
     */
    @Override
    public DocumentFragment produceIntextElement(Module mod, PIcommand cmd) {
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        if(mod.getScriptHandler().getOption(Options.REFERENCE_FORM).equals(Options.IEEE)){
            return IEEEStyle.getIntextElement(mod, cmd,this);
        }
        if(mod.getScriptHandler().getOption(Options.REFERENCE_FORM).equals(Options.HARVARD)){
           return HarvardStyle.getIntextElement(mod, cmd,this);
        }
        // Keep It Simple Stupid
        Element refElt = mod.getDoc().createElement("span");
        refElt.setAttribute("class", cmd.getCommandStyleName());
        Node n_ref = null;
        String reftext = "";

        // set as requested
        String index = EMPTY;
        if (cmd.paramExist(PIcommand.INDEX)) {
            index = cmd.getValue(PIcommand.INDEX);
        }

        // display according to style
        // assume simple
        reftext += " [" + index + "] ";
        // if we have simple and a ref with uri
        // we make it directly linkable
        if (m_propertMap.containsKey("uri")) {
            Element aelt = mod.getDoc().createElement("a");
            aelt.setAttribute("href", m_propertMap.get("uri"));
            aelt.appendChild(mod.getDoc().createTextNode(reftext));
            refElt.appendChild(aelt);
            String tmp = "";
            if (m_propertMap.containsKey("title")) {
                tmp += accessProperty("title");
            }
            if (m_propertMap.containsKey("uri")) {
                tmp += " | " + accessProperty("uri");
            }
            refElt.setAttribute("title", tmp);
            df.appendChild(refElt);
            return df;
            //return refElt;
        }

        n_ref = mod.getDoc().createTextNode(reftext.trim());
        refElt.appendChild(n_ref);
        df.appendChild(refElt);
        return df;

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
        DocumentFragment df = mod.getDoc().createDocumentFragment();;
        if(mod.getScriptHandler().getOption(Options.REFERENCE_FORM).equals(Options.IEEE)){
            return IEEEStyle.getListElement(mod, cmd, this,users);
        }
        if(mod.getScriptHandler().getOption(Options.REFERENCE_FORM).equals(Options.HARVARD)){
            return HarvardStyle.getListElement(mod, cmd, this,users);
        }
        // Keep It Simple Stupid
        String style = mod.getScriptHandler().getOption(Options.REFERENCE_FORM);
        Element liElt = mod.getDoc().createElement("li");
        liElt.setAttribute("class", "refitem skiptranslate " + style);

        // assume simple
        if (m_propertMap.containsKey("title")) {
            liElt.appendChild(makePropertyElement("title", mod));
        }
        if (m_propertMap.containsKey(AUTHORS_IN_LIST)) {
            liElt.appendChild(makePropertyElement(AUTHORS_IN_LIST, mod));
        }
        if (m_propertMap.containsKey("year")) {
            liElt.appendChild(makePropertyElement("year", mod));
        }
        if (m_propertMap.containsKey("chaptertitle")) {
            liElt.appendChild(makePropertyElement("chaptertitle", mod));
        }
        if (m_propertMap.containsKey("journal")) {
            liElt.appendChild(makePropertyElement("journal", mod));
        }
        if (m_propertMap.containsKey("editor")) {
            liElt.appendChild(makePropertyElement("editor", mod));
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
        //"categories" is not written

        df.appendChild(liElt);
        return df;

        //return liElt;
        //df.appendChild(liElt);
        //return df.getFirstChild();
    }

    /**
     * Get the sortkey
     * @return the sortkey
     */
    public String getSortKey() {
        return m_propertMap.get(SORTKEY);
    }

    /**
     * get year
     * @return theyear
     */
    public String getYear() {
        return m_propertMap.get("year");
    }

    /**
     * Analyze the authors-text and produce the text that should
     * be produced in a list and in the text(harvard style)
     * Put these constructed text in propertymap 
     * Do also produce sortkey for use i harvard sorting.
     * @param au The element authors text as read from element
     */
    private void prepareAuthorStrings(String au) {
        String[] authors;
        String authorsforlist;
        String authorsfortext;
        if (au.compareTo(EMPTY) == 0) {
            authors = null;
            authorsforlist = null;
            authorsfortext = null;
        } else {
            authors = au.split("\\|");
            // authors for list
            authorsforlist = makeNameList(authors);
            // authors for in text
            authorsfortext = makeNamelistForIntextReference(authors);
        }
        // store them
        if (authorsforlist != null) {
            m_propertMap.put(AUTHORS_IN_LIST, authorsforlist);
        }
        if (authorsfortext != null) {
            m_propertMap.put(AUTHORS_IN_TEXT, authorsfortext);
            m_propertMap.put(SORTKEY, authorsfortext);
        } else {
            m_propertMap.put(SORTKEY, accessProperty("title"));
        }

    }

    private String makeNameList(String[] list) {
        if (list.length == 1) {
            return list[0];
        }
        String s = "";
        int i = 0;
        for (; i < list.length - 2; i++) {
            s += list[i] + ", ";
        }
        s += list[i] + " & " + list[i + 1];

        return s;
    }

    private String makeNamelistForIntextReference(String[] authorlist) {
        String[] lastnames = new String[authorlist.length];
        for (int i = 0; i < authorlist.length; i++) {
            String[] name = authorlist[i].split(",");
            lastnames[i] = name[0];
        }
        if (lastnames.length < 3) {
            return makeNameList(lastnames);
        }
        return lastnames[0] + " et.al";
    }

    /**
     * Get a property, and return EMPTY if not found
     * @param key The key to look for
     * @return The property found, or EMPTY if not found
     */
    protected String accessProperty(String key) {
        if (m_propertMap.containsKey(key)) {
            return m_propertMap.get(key).trim();
        }
        return EMPTY;
    }

    /**
     * make an a-element
     * @param uri The ref
     * @param m The module that will end up with this element
     * @return an a- element
     */
    protected Node makeUriElement(String uri, Module m) {
        //TODO: Relative reference ?
        Element aElt = m.getDoc().createElement("a");
        aElt.setAttribute("class", "external");
        aElt.setAttribute("title", "external link");
        aElt.setAttribute("href", uri);
        if (uri.startsWith("https://")) {
            uri = uri.substring(8);
        }
        else if (uri.startsWith("http://")) {
            uri = uri.substring(7);
        }
        aElt.appendChild(m.getDoc().createTextNode(uri));
        return aElt;
    }

    /**
     * make a span element
     * @param prop The property we will display
     * @param m The module that will end up with this element
     * @return a span-element
     */
    public Node makePropertyElement(String prop, Module m) {
        Element divElt = m.getDoc().createElement("span");
        divElt.setAttribute("class", prop);
        divElt.appendChild(m.getDoc().createTextNode(accessProperty(prop)));
        return divElt;
    }

    /**
     * References are never made on the fly, they are defined
     * @return false
     */
    @Override
    public boolean madeOnTheFly() {
        return false;
    }

    /**
     * Get the id of this reference
     * @return The id
     */
    public String getid() {
        return m_id;
    }

    /**
     * Get the uri of this reference
     * @return The uri or null if nonexistant
     */
    public String getURI() {
        return m_propertMap.get("uri");
    }

    /**
     * Get the type of reference
     * @return SIMPLE
     */
    public String getRefType() {
        return Options.SIMPLE;
    }

    @Override
    public String toString() {
        String s = "**\nid: " + m_id + " ";

        for (String k : m_propertMap.keySet()) {
            s += m_propertMap.get(k) + " ";
        }

        return s;
    }

    // must be rewritten to reflect the style we are using
    // this we can pick up from scriptahndler.getOption(Option.refstyle
    @Override
    public int compareTo(Indexable o) {
        // as they are (registered)
        
        return 0;
    }

    @Override
    public String getCompareValue() {
        if (m_propertMap.containsKey("title")) {
            return m_propertMap.get("title");
        }
        if (m_propertMap.containsKey("author")) {
            return m_propertMap.get("author");
        }
        return m_id;
    }
}

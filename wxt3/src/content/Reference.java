package content;

import java.util.HashMap;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.PIcommand;
import utils.reporter;

/**
 * Implements a reference
 * 
 */
public class Reference implements Comparable<Reference>
{
    /*
    A reference has no style. The style is produced when the reference is
    displayed in running text or in a reference list.
    There is no control of which elements are set from the element in the constructor.
    Control is assumed in schema, prior to reference construction.
    The method produceListElement describes the elements we care about and the order they are presented
    for diffrenet styles.
    */

    /** Used when displaying mandatory, unsupplied fields*/
    private final String EMPTY="?";
    /** keys for constructed properties*/
    private final String AUTHORS_IN_LIST="authorsinlist";
    private final String AUTHORS_IN_TEXT="authorsintext";
    private final String SORTKEY="sortkey";


    /** Id of the reference as read from referenceelement */
    private String m_id;

    /** Map containing all elements of the reference */
    private HashMap<String, String> m_propertMap;


    /**
     * @param elt an element from a  reference file
     * @param r the reporter in use
     * @throws java.lang.Exception if the reference cannot be made due to missing items
     */
    public Reference(Element elt,reporter r) throws Exception
    {
        // If no id is found, an exception is thrown
        if (elt.hasAttribute(Definitions.ID))
            m_id = elt.getAttribute(Definitions.ID);
        else
        {
            throw new Exception(reporter.getBundleString("missing_reference_data", Definitions.ID));
        }


        m_propertMap=new HashMap<String, String>(6) ;

        // pick up fields
        // no control that they are legal, assume validated
        // in any case: only legals will be displayed
        NodeList elts=elt.getChildNodes();
        for(int eix=0;eix<elts.getLength();eix++)
        {
            if(elts.item(eix).getNodeType()==Node.ELEMENT_NODE)
            {
                Element e=(Element)elts.item(eix);
                if((e.getNodeName().trim().compareTo("uri")==0)&&
                   (e.hasAttribute("dateread")))
                        m_propertMap.put("dateread", e.getAttribute("dateread"));
                String tmp=e.getTagName();
                if(m_propertMap.containsKey(tmp))
                {
                    // field appears again
                    r.pushMessage("repeated_field_in_ref", m_id, tmp);
                }
                else
                    m_propertMap.put(e.getTagName(),e.getTextContent().trim());
            }
        }
        // any other reason why we should reject it ?
        // no, we display it with the necessary entries and display empty
        // when the display style needs it and the prop is empty

        // prepare author strings for list and intext (harvard)
        prepareAuthorStrings(accessProperty("authors"));

    }

    /* we need in principle 6 display variants:
     displaySimple in text
     displayIEE in text
     displayHarvard in text, all implemented in method: produceIntextReference

     displaySimple in list
     displayIEE in list
     displayHarvard in list, all implemented in method produceListElement
     */

     /**
     * Create and return a single reference to be included in the text
     * @param m The module that request the element
     * @param cmd The command describing the request
     * @param style The style of display
     * @return the reference as an element
     */
    public Element produceIntextReference( Module m,PIcommand cmd, String style )
    {

        DocumentFragment df = m.getDoc().createDocumentFragment();
        Element refElt = m.getDoc().createElement("span");
        refElt.setAttribute("class", cmd.getCommandStyleName());
        Node n_ref = null;
        String reftext = "";

        // set as requested
        String index =EMPTY;
        if (cmd.paramExist(PIcommand.INDEX)) {
            index = cmd.getValue(PIcommand.INDEX);
        }

        // display according to style
        if (style.compareTo(Definitions.HARVARD)==0)
        {
            // display harvard style
            String s = "";
            if (m_propertMap.containsKey(AUTHORS_IN_TEXT))
                s += accessProperty(AUTHORS_IN_TEXT);
            else
                s += accessProperty("title");
            if(m_propertMap.containsKey("year"))
                reftext += "("+ s + ", "+accessProperty("year")+")";
            else
                reftext += "("+ s +")";
        }

        else if(style.compareTo(Definitions.IEEE)==0)
        {
            // display IEE style
            reftext += " [" + index + "] ";
        }

        else
        {
            // assume simple
            reftext += " [" + index + "] ";
            // if we have simple and a ref with uri
            // we make it directly linkable
            if(m_propertMap.containsKey("uri"))
            {
                Element aelt=m.getDoc().createElement("a");
                aelt.setAttribute("href", m_propertMap.get("uri"));
                aelt.appendChild(m.getDoc().createTextNode(reftext));
                refElt.appendChild(aelt);
                String tmp = "";
                if(m_propertMap.containsKey("title"))
                        tmp+=accessProperty("title");
                if(m_propertMap.containsKey("uri"))
                        tmp+=" | "+accessProperty("uri");
                 refElt.setAttribute("title", tmp);
                return refElt;
            }
        }
        n_ref = m.getDoc().createTextNode(reftext.trim());
        refElt.appendChild(n_ref);
        return refElt;
    }


     /**
     * Create and return a  li-element for an referencelist
     * The actual fields and which are mandatory for differents styles
     * are implemented here, and no other place
     * @param m The module requesting the referencelist
     * @param style The style we want
     * @return A Node containing the element in the refencelist.
     */
    public Node produceListElement(Module m,String style)
    {
        DocumentFragment df = m.getDoc().createDocumentFragment();
        Element liElt = m.getDoc().createElement("li");
        liElt.setAttribute("class", "refitem skiptranslate "+style);

        if(style.compareTo(Definitions.HARVARD)==0)
        {
            // do harvard
            liElt.appendChild(m_propertMap.containsKey(AUTHORS_IN_LIST)?
                                liElt.appendChild(makePropertyElement(AUTHORS_IN_LIST,m)):
                                liElt.appendChild(makePropertyElement("title",m)));
            liElt.appendChild(makePropertyElement("year",m));
            if(m_propertMap.containsKey("chaptertitle")) liElt.appendChild(makePropertyElement("chaptertitle",m));
            if(m_propertMap.containsKey("journal")) liElt.appendChild(makePropertyElement("journal",m));
            if(m_propertMap.containsKey("editor")) liElt.appendChild(makePropertyElement("editor",m));
            if(!m_propertMap.containsKey(AUTHORS_IN_LIST))liElt.appendChild(makePropertyElement("title",m));
            if(m_propertMap.containsKey("place")) liElt.appendChild(makePropertyElement("place",m));
            if(m_propertMap.containsKey("publisher")) liElt.appendChild(makePropertyElement("publisher",m));
            if(m_propertMap.containsKey("pages")) liElt.appendChild(makePropertyElement("pages",m));
            if(m_propertMap.containsKey("isbn")) liElt.appendChild(makePropertyElement("isbn",m));
            if(m_propertMap.containsKey("text")) liElt.appendChild(makePropertyElement("text",m));
            if(m_propertMap.containsKey("uri")) liElt.appendChild(makeUriElement(m_propertMap.get("uri"),m));
            if(m_propertMap.containsKey("dateread")) liElt.appendChild(makePropertyElement("dateread",m));
        }

        else if(style.compareTo(Definitions.IEEE)==0)
        {
            // do IEEE
           liElt.appendChild(m_propertMap.containsKey(AUTHORS_IN_LIST)?
                                liElt.appendChild(makePropertyElement(AUTHORS_IN_LIST,m)):
                                liElt.appendChild(makePropertyElement("title",m)));
            liElt.appendChild(makePropertyElement("year",m));
            if(m_propertMap.containsKey("chaptertitle")) liElt.appendChild(makePropertyElement("chaptertitle",m));
            if(m_propertMap.containsKey("journal")) liElt.appendChild(makePropertyElement("journal",m));
            if(m_propertMap.containsKey("editor")) liElt.appendChild(makePropertyElement("editor",m));
            if(!m_propertMap.containsKey(AUTHORS_IN_LIST))liElt.appendChild(makePropertyElement("title",m));
            if(m_propertMap.containsKey("place")) liElt.appendChild(makePropertyElement("place",m));
            if(m_propertMap.containsKey("publisher")) liElt.appendChild(makePropertyElement("publisher",m));
            if(m_propertMap.containsKey("pages")) liElt.appendChild(makePropertyElement("pages",m));
            if(m_propertMap.containsKey("isbn")) liElt.appendChild(makePropertyElement("isbn",m));
            if(m_propertMap.containsKey("text")) liElt.appendChild(makePropertyElement("text",m));
            if(m_propertMap.containsKey("uri")) liElt.appendChild(makeUriElement(m_propertMap.get("uri"),m));
            if(m_propertMap.containsKey("dateread")) liElt.appendChild(makePropertyElement("dateread",m));
       }
        else
        {
            // assume simple
            if(m_propertMap.containsKey("title"))liElt.appendChild(makePropertyElement("title",m));
            if(m_propertMap.containsKey(AUTHORS_IN_LIST)) liElt.appendChild(makePropertyElement(AUTHORS_IN_LIST,m));
            if(m_propertMap.containsKey("year")) liElt.appendChild(makePropertyElement("year",m));
            if(m_propertMap.containsKey("chaptertitle")) liElt.appendChild(makePropertyElement("chaptertitle",m));
            if(m_propertMap.containsKey("journal")) liElt.appendChild(makePropertyElement("journal",m));
            if(m_propertMap.containsKey("editor")) liElt.appendChild(makePropertyElement("editor",m));
            if(m_propertMap.containsKey("place")) liElt.appendChild(makePropertyElement("place",m));
            if(m_propertMap.containsKey("publisher")) liElt.appendChild(makePropertyElement("publisher",m));
            if(m_propertMap.containsKey("pages")) liElt.appendChild(makePropertyElement("pages",m));
            if(m_propertMap.containsKey("isbn")) liElt.appendChild(makePropertyElement("isbn",m));
            if(m_propertMap.containsKey("text")) liElt.appendChild(makePropertyElement("text",m));
            if(m_propertMap.containsKey("uri")) liElt.appendChild(makeUriElement(m_propertMap.get("uri"),m));
            if(m_propertMap.containsKey("dateread"))liElt.appendChild( makePropertyElement("dateread",m));
            //"categories" is not written
        }

        df.appendChild(liElt);
        return df.getFirstChild();
    }



    public String getSortKey()
    {
        return m_propertMap.get(SORTKEY);
    }

    public String getYear()
    {
        return m_propertMap.get("year");
    }

    /**
     * Analyze the authors-text and produce the text that should
     * be produced in a list and in the text(harvard style)
     * Put these constructed text in propertymap 
     * Do also produce sortkey for use i harvard sorting.
     * @param au The element authors text as read from element
     */
     private void prepareAuthorStrings(String au)
    {
        String[] authors;
        String authorsforlist;
        String authorsfortext;
        if(au.compareTo(EMPTY)==0)
        {
            authors=null;
            authorsforlist=null;
            authorsfortext=null;
        }
        else
        {
            authors = au.split("\\|");
            // authors for list
            authorsforlist=makeNameList(authors);
            // authors for in text
            authorsfortext=makeNamelistForIntextReference(authors);
        }
        // store them
        if(authorsforlist!=null)
            m_propertMap.put(AUTHORS_IN_LIST, authorsforlist);
        if(authorsfortext!=null)
        {
            m_propertMap.put(AUTHORS_IN_TEXT, authorsfortext);
            m_propertMap.put(SORTKEY,authorsfortext);
        }
        else
            m_propertMap.put(SORTKEY,accessProperty("title"));

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

    private String makeNamelistForIntextReference(String[] authorlist)
    {
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
     * @return
     */
    private String accessProperty(String key)
    {
        if(m_propertMap.containsKey(key))
            return m_propertMap.get(key).trim();
        return EMPTY;
    }

   /**
    * make an a-element
    * @param uri The ref
    * @param m The module that will end up with this element
    * @return an a- element
    */
    private Node makeUriElement( String uri, Module m)
   {
       //TODO: Relative reference ?
        Element aElt = m.getDoc().createElement("a");
        aElt.setAttribute("class", "external");
        aElt.setAttribute("title", "external link");
        aElt.setAttribute("href", uri);
        if(uri.startsWith("http://"))
            uri=uri.substring(7);
        aElt.appendChild(m.getDoc().createTextNode(uri));
        return aElt;
    }

   /**
    * make a span element
    * @param prop The property we will display
    * @param m The module that will end up with this element
    * @return a span-element
    */
    private Node makePropertyElement(String prop,Module m)
   {
       Element divElt= m.getDoc().createElement("span");
       divElt.setAttribute("class", prop);
       divElt.appendChild(m.getDoc().createTextNode(accessProperty(prop)));
       return divElt;
   }



   /**
    * Get the id of this reference
    * @return The id
    */
    public String getid()
   {
       return m_id;
   }

   /**
    * Get the uri of this reference
    * @return The uri or null if nonexistant
    */
    public String getURI()
   {
       return m_propertMap.get("uri");
   }




    @Override
    public int compareTo(Reference other)
    {
        // note that this comparison is only used when harvard style
        int v = -1;
        try {
            v = getSortKey().trim().compareTo(other.getSortKey().trim());
            if (v == 0)
            {
                try {
                    int y1 = Integer.parseInt(getYear().trim());
                    int y2 = Integer.parseInt(other.getYear().trim());
                    // Latest work will be ordered first
                    v =  y1 > y2 ? -1 : 1;
                } catch (NumberFormatException nfe)
                {
                    v = 0;
                }

            }
            return v;
        }
        catch (NullPointerException e)
        {
            return 1;
        }
    }

    @Override
    public String toString() {
        String s = "**\nid: " + m_id + " ";

        for (String k : m_propertMap.keySet())
        {
            s += m_propertMap.get(k) + " ";
        }

        return s;
    }
}

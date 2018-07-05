package content;

import java.net.URI;
import java.util.Vector;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.accessutils;
import utils.domer;

/**
 * Holds an XML fragment.
 * Fragents are crated from the script and from module:summary.
 * Some fragments are used as author description.
 * Some of the methods are related to the fact that some
 * fragments are used as author descriptions.
 * This destiny is decided when a module refers it as an author-attribute.
 * The relevant methods are: AddModuleToauthor, isAuthor, HasWrittenAnyOf
 */
public class Fragment {
    
    /** the URI where the actual content is found, if not directly quoted */
    private URI m_absoluteUri;
    /** the anchor for all addressing elements in the fragment*/
    private URI m_referenceUri;
    /** the (script)element where it is defined */
    private Element m_Element;
    /** the definitions that owns the fragment */
    Definitions m_def;
    /** the fragment as string */
    private String m_txt;
    /** encoding as found as element attribute */
    private String m_encoding;
    /** the fragment */
    private DocumentFragment m_docfrag;
    /** the short value */
    private String m_short;
    /** the id value */
    private String m_id;
    /** modules who have used this fragment*/
    Vector<Module>m_modulesAsAuthor=new Vector<Module>(5);
    
    /**
     * Produce a new Fragment from a simple string
     * These fragments are not stored in Definitions fragmentlist
     * The module use this for storing the content of the description attribute
     * @param txt The string defining the fragment
     * @param mod The mod requesting the fragment
     * @throws java.lang.Exception
     */
    public Fragment(String txt,Module mod)
    throws Exception{
        m_txt=txt;
        // best logical, non null solution (not used)
        m_short=m_id=m_txt;
        m_def=mod.getDefinitions();
        // substitute pathfragments
        m_txt= m_def.substituteFragments(m_txt);

        m_Element=null;
        m_encoding=mod.getEncoding();

        try{
            m_docfrag=domer.produceDocFragmentFromString(m_txt,m_encoding,mod.getReporter());
        }
        catch(Exception e)
        {// alternative is to make a dummy - error-signaling fragment             
            throw e;
        }
    }
    
    /**
     * Constructs an single Fragment from a scriptelement.
     * 
     * @param elt The element (in the script) where the fragment is described
     * @param u The absolute URI where the content exists. May be null
     * @throws java.lang.Exception When we fail to establish the structure
     */
    public Fragment(Element elt,URI u,Definitions def)
    throws Exception{
        m_absoluteUri=u;
        m_Element=elt;
        m_def=def;
        // the scripts encoding as default:
        m_encoding=elt.getOwnerDocument().getXmlEncoding();
        if(m_encoding==null)
            m_encoding=def.getOption(Definitions.DEFAULT_ENCODING);
        if(elt.hasAttribute("encoding"))
            m_encoding=elt.getAttribute("encoding");

        if(elt.hasAttribute(Definitions.ID))
            m_id=elt.getAttribute(Definitions.ID);
        else
            m_id="_?_"; // should not happen

        if(elt.hasAttribute(Definitions.SHORT))
            m_short=elt.getAttribute(Definitions.SHORT);
        else
            m_short=m_id; // thus we always a short value

        // we get hold of the text either from a file or directly as quoted in the element
        if(m_absoluteUri != null)
        {
            // load it from file
            m_txt=accessutils.getBOMSafeTextFile(m_absoluteUri,m_encoding);
        }
        else
        {
            // we  pick it up from the element itself
            // and we use the scriptaddress as referenceUri
            m_referenceUri=def.m_scriptHandler.getScriptAbsoluteUri();
            m_txt=getContentFromElement();
        }
        m_txt=m_txt.trim();

        // content defaults to short value
        if(m_txt.isEmpty())
            m_txt=m_short;
        else
            m_txt= m_def.substituteFragments(m_txt);

        //-----------------------
        // ok we have the text. Now we must make an XML-fragment

        // first we make sure we dont have any bad &'s
        m_txt=accessutils.fixUriParameterEntities(m_txt);
        

        // should we fix encoding so it match that of the script ?
        /*
         if(!encoderutils.sameEncoding(m_encoding,elt.getOwnerDocument().getXmlEncoding()))
        {
            m_txt=encoderutils.reEncode(m_txt,m_encoding,elt.getOwnerDocument().getXmlEncoding(),false);
            m_encoding=elt.getOwnerDocument().getXmlEncoding();
        }*/
        
        try{
            m_docfrag=domer.produceDocFragmentFromString(m_txt,elt.getOwnerDocument().getXmlEncoding(),def.getReporter());
        }
        catch(Exception e)
        { 
            throw e;
        }
    }
    
    /**
     * Constructs a new fragmen from a fragment element in a fragments file
     * @param elt The element describing the fragment
     * @param def The definitions element containing the file ref
     * @param refUri The reference URI that the contents of the fragment are relative to
     * @throws java.lang.Exception When we fail to make the fragment
     */
    public Fragment(Element elt,Definitions def,URI refUri)
    throws Exception{        
        this(elt,null,def);
        m_referenceUri=refUri;
    }
    
    /**
     * Get the DocumentFragment.
     * @return the fragment
     */
    public DocumentFragment getFragment()
    {
       return m_docfrag; 
    }

     /**
     * Get the text that is contained in  the DocumentFragment.
     * Name if no content is given
     * @return the fragment as a string
     */
    public String getFragmentAsString()
    {
       return m_txt; 
    }
    
    /**
     * Get the encoding.
     * @return  The current encoding for this fragment as a string
     */
    public String getEncoding()
    {
       return m_encoding; 
    }
    
    /**
     * Pick up content from an element.
     * Content is text or CDATA among elements children
     * 
     * @return the content of the scriptelement as text or CDATA
     * @throws java.lang.Exception when something goes wrong
     */
    private String getContentFromElement()
    throws Exception{
        String s="";
        NodeList nlist=m_Element.getChildNodes();
        if(nlist.getLength()==0)
            return s;
        Node tn=nlist.item(0);
        try{
            while(tn!=null)
            {
                short typ=tn.getNodeType();
                if((typ==Node.TEXT_NODE)||(typ==Node.CDATA_SECTION_NODE))
                    s+=tn.getNodeValue().trim();
                tn=tn.getNextSibling();
            }
        }
        catch(Exception e)
        {
            throw e;
        }
        return s;        
    }


    /**
     * Get the absolute, refernce uri for this fragment
     * @return the refernce uri
     */
    public URI getReferenceUri()
    {
        return m_referenceUri;
    }

    /**
     * Get the name field
     * @return a string, always non-null
     */
    public String getShort()
    {
        return m_short;
    }

    /**
     * get the id field
     * @return a string, always non-null
     */
    public String getId()
    {
        return m_id;
    }

    //-----------------
    // author related methods
     /**
     * Add a new user of this fragment
     * Check if it is already there, for multiple builds
     * @param m The module
     */
    public void addModuleToAuthor(Module m)
    {
        for (Module mod : m_modulesAsAuthor)
            if(mod==m)
                return;
        m_modulesAsAuthor.add(m);
    }

     /**
     * Check if the fragment has appepared as author in any of the modules given
     * @param modList The actual modules
     * @return true or false
     */
    public boolean HasWrittenAnyOf(Vector<Module>modList)
    {
        if(!isAuthor())
            return false;
        for(Module m:modList)
            for(Module s:m_modulesAsAuthor)
                if(m.equals(s))
                    return true;
         return false;
    }

    /**
     * Is this fragment used as an author description
     * @return tre or false
     */
    public boolean isAuthor()
    {
       return m_modulesAsAuthor.size()>0;
    }
    
    @Override
    public String toString()
    {
        return "\tFragment:\n\t" + m_txt;
    }
            

}

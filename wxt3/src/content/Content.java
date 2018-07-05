
package content;

import java.net.URI;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import utils.PIcommand;
import utils.accessutils;

/**
 * Base class for TXTContent, XMLContent, WIKIContent, ODTContent, DBContent
 */
public class Content {
    
    /** The absolute location for this content file*/
    protected URI m_absoluteUri;
    /** NOT IMPLEMENTED: An absolute location for alternativ source*/
    protected URI m_absoluteBackupUri;
    /** An absolute URI to the effective location we will load from */
    protected URI m_absoluteLoadUri;
    /** The owning module */
    protected Module m_Owner;
    /** The basic cmd used to establish this object*/
    protected PIcommand m_Cmd;
     /** id, used when importing to template*/
    protected String m_id;
    /** encoding */
    String m_encoding;
    
    /**
     * Constructing a Content with initialized fields
     */    
    public Content()
    {
        m_absoluteUri=null;
        m_absoluteBackupUri=null;
        m_absoluteLoadUri=null;
        m_Owner=null;        
        m_id="_NO_ID";
        m_Cmd=null;
        m_encoding="utf-8";
    }

    /**
     * Constructs a content from a PIcommand
     * @param owner The module that request the content
     * @param cmd The PIcommand describing the request
     * @throws java.lang.Exception When bad adressing of location or transformation
     */
    public Content(Module owner,PIcommand cmd)
    throws Exception{
        this();
        m_Cmd=cmd;
        m_Owner=owner;
        m_encoding=owner.getEncoding();

        //------------------------------------
        // TODO:if we want alternative, backup, location we may use the strategy
        // applied in ScriptContent

        //--------------------------
        // location is mandatory
        if(m_Cmd.paramExist(PIcommand.LOCATION))
        {
            String tmp=m_Cmd.getValue(PIcommand.LOCATION);
            tmp=m_Owner.getDefinitions().substituteFragments(tmp);
            tmp=accessutils.fixUriParameterEntities(tmp);
            try{
                m_absoluteUri=accessutils.makeAbsoluteURI(tmp, m_Owner.getCatalog());
            }
            catch(Exception e)
            {
                m_Owner.getReporter().pushMessage("cannot_make_abs_uri", e.getMessage());
                throw new Exception(e.getMessage());
            }
        }
        else
        {// should not happen            
            throw new Exception(m_Owner.getReporter().getBundleString("missing_location_in_content"));
        }
        
       //----------------
        // since we have not implemented backup locations, this is it:
        m_absoluteLoadUri=m_absoluteUri;

        //--------------
        // id  may help doing import into template more efficient and selective
        if(m_Cmd.paramExist(PIcommand.ID))
            m_id=m_Cmd.getValue(PIcommand.ID);

        //----------------
        // cmd encoding overrules modules encoding
        if(m_Cmd.paramExist(PIcommand.ENCODING))
            m_encoding=m_Cmd.getValue(PIcommand.ENCODING);

        //-----------
        //remember that the module is dependant of this
        m_Owner.addDependency(m_absoluteUri);
    }

    
    /**
    * Get the id for this content
    * @return The id
    */
    public String getId(){  return m_id;  }
    

    /**
     * Produce a basic content div telling the cmd
     * @param mod The module that has requested this content
     * @param cmd The PIcommand that describes what we want
     * @return The content a simple notifyer of the command as a DocumentFragment
     */
    public DocumentFragment getContent(Module mod,PIcommand cmd)
    {
        DocumentFragment df =mod.getDoc().createDocumentFragment();
        Element tElt=mod.getDoc().createElement("div");
        tElt.setAttribute("class", "basic-content");
        Node tNode=mod.getDoc().createTextNode("From: "+cmd.toString());
        tElt.appendChild(tNode);
        df.appendChild(tElt);
        return df;              
    }
    
    /**
      * Return a CocumentFragment telling that the production of the
      * content failed
      * @param mod The module that has requested this content
      * @param cmd The PIcommand that describes what we want
      * @return a list of nodes
      */
    static public DocumentFragment getFailedContent(Module mod,PIcommand cmd)
    {
        DocumentFragment df =mod.getDoc().createDocumentFragment();
        Element tElt=mod.getDoc().createElement("div");
        tElt.setAttribute("class", "wxt-error");
        Node tNode=mod.getDoc().createTextNode("Evaluation of: "+cmd.toString()+" failed");
        tElt.appendChild(tNode);
        df.appendChild(tElt);
        return df;       
    }
            
    
    @Override
    public String toString() {      
        return "\n\tContent"; 
    }

}

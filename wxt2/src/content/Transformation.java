package content;

import java.io.File;
import java.net.URI;
import utils.accessutils;

/**
 * Holds an XSLT transformation
 * @author borres
 */
public class Transformation {
    
    /** The name, the key to this transformation*/
    private String m_name;
    /** The absolute URI to transformation*/
    private URI m_absoluteUri;
    
    /**
     * Constructor
     * @param name The name of the transformation, used as Module attribute
     * @param loc The location of the transformation
     */
    public Transformation(String name,URI loc)
    {
        m_name=name;
        m_absoluteUri=loc;
    }

     /**
     * Constructor
     * @param name The name of the transformation, used as Module attribute
     * @param tstring The transformation as a string
     * @param tmpCatalog the path to the catalog we will use for temporary storage
     * @throws exception when we cannot store the transformation with an uri
     */
    public Transformation(String name,String tstring,String tmpCatalog)
    throws Exception{
        m_name=name;
        // save the transformation at a temporary file (URI)
        File temp;
        try{
            temp=File.createTempFile(name,".xsl",new File(tmpCatalog));
            temp.deleteOnExit();
            //String tmp="file:///"+temp.getPath().replace(" ", "%20");
            //tmp=tmp.replace('\\', '/');
            //m_absoluteUri=new URI(tmp);
            
            m_absoluteUri=accessutils.makeUri("file:///"+temp.getPath());
            accessutils.saveTFile(m_absoluteUri, tstring, "utf-8");
        }
        catch(Exception ex)
        {
            m_absoluteUri=null;
            throw ex;
        }
    }
   
    
    /**
     * get the name, id, of the transformation
     * @return the name
     */
    public String getName()
    {
        return m_name;
    }
    
     /**
      * get the absolute URI for this transformation
      * @return the absolute URI
      */
    public URI getabsoluteURI()
    {
        return m_absoluteUri;
    }
    
    @Override
    public String toString() {
        
        String s = "Transformation: "+m_name+"  at "+m_absoluteUri.toString(); 
         return s;
    }
    
    /**
     * Find a transformation from a string.
     * If it does not exist, it will be produced
     * 
     * @param TString The string identifieing a transformation, or a transformation address
     * @param mod The module that does the request
     * @return A transformation, or null if the data cannot be interpreted
     */
    static Transformation findTransformation(String TString,Module mod)
    {
        Transformation t=null;
        try{
            // loose parameters
            int pos=TString.indexOf('(');
            if(pos!=-1)
                TString=TString.substring(0, pos);
            t=mod.getDefinitions().getTransformation(TString);
        }
        catch(Exception e)
        {
          mod.getReporter().pushMessage("bad_transformation",TString);
          return null;            
        }

        if(t==null)
        {
          // this is a transformation which is not defined. 
          // we expect to find an URI which is absolute or relative to anchor
          try{
              URI tmpUri=accessutils.makeAbsoluteURI(TString, mod.getAnchorCat());
              t=new Transformation(tmpUri.toString(),tmpUri);
              mod.getDefinitions().addTransformation(TString,t);
          }
          catch(Exception tex)
          {
              mod.getReporter().pushMessage("bad_transformation", TString); 
              return null;  
          }
        }
        
        return t;
        
    }

}

package faceutils;

import java.io.StringReader;
import java.net.URI;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 *
 * @author Administrator
 */
public class FaceDomer {
     
     static DocumentBuilder m_documentbuilder;
    
     static private String default_encoding="utf-8";

    public static void setDefaultEncoding(String s)
    {default_encoding=s;}
    public static String getDefaultEncoding()
    {return default_encoding;}
    
            
     public FaceDomer() {   }
    

     /** 
      * Make a documentbuilder.
      * 
     * @return a documentbuilder
     * @throws ParserConfigurationException when bad parser configuration
     * @throws FactoryConfigurationError when bad factory configuration
     */
    static public DocumentBuilder makeDocBuilder()
        throws ParserConfigurationException,FactoryConfigurationError{

            if(m_documentbuilder==null)
            {
                DocumentBuilderFactory dFact=DocumentBuilderFactory.newInstance();            
                // we can set up validation and other things here
                dFact.setValidating(false);
                dFact.setIgnoringComments(false);
                dFact.setIgnoringElementContentWhitespace(false); 
                // avoid html-namespace disrupts xpath search in adresse calculation ???
                dFact.setNamespaceAware(false);           

                // try to avoid search for dtds
                // this is imperative to speed !!!!!!
                // TODO: check feature
                dFact.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd",new Boolean(false));
                dFact.setAttribute("http://apache.org/xml/features/nonvalidating/load-dtd-grammar",new Boolean(false));
                
                //dFact.setExpandEntityReferences(false);
                m_documentbuilder=dFact.newDocumentBuilder();

            }
            
            return m_documentbuilder;
    }
    
 

    /** 
     * Establish a Document from a String.
     * 
     * @param S the XML-string that should be parsed
     * @return the document built
     * @throws Exception when no Document is established
     */
    public static Document makeDomFromString(String S)
        throws Exception {
        try {
            DocumentBuilder dBuilder = makeDocBuilder();
            
            Document doc=dBuilder.parse(new InputSource(new StringReader(S)));
            return doc;
        }
        catch (Exception e) {
            // for debugging only
            throw new Exception(e.getMessage());
        }
    }
     
    /** 
     * Establish a Document from a XML-file.
     * 
     * @param theUri Where the XML-file is located
     * @return the document built
     * @throws Exception when no Document is established
     */
    public static Document makeDomFromUri(URI theUri)
    throws Exception{        
        try{                       
            DocumentBuilder dBuilder=makeDocBuilder();
            return dBuilder.parse(theUri.toString());
        }
        catch (Exception e) {
            // for debugging only
            throw new Exception(e.getMessage());
        }
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wxt3utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URI;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * This class implements a static library for handling DOM-actions.
 * <p>
 * Building Document from String etc.
 * @author borres
 */
public class wxt3rundomer extends java.lang.Object {
    
     static DocumentBuilder m_documentbuilder;
    
     static private String default_encoding="utf-8";

    public static void setDefaultEncoding(String s)
    {default_encoding=s;}
    public static String getDefaultEncoding()
    {return default_encoding;}
    
            
     public wxt3rundomer() {   }
    

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
     * Validate a stringrepresentation of a Document against a schema
     * @return OK if ok , errormessage otherwise
     * @param errors An errorhandler
     * @param theText The Text we will validate
     * @param theSchema The schema we want to validate against
     */   
    public static String ValidateString(String theText,URI theSchema,validationErrorHandler errors)
    {
        SchemaFactory factory =
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema;
        
        try {
            schema = factory.newSchema(theSchema.toURL());
            Validator validator = schema.newValidator();
            validator.setErrorHandler(errors);
            
            
            DocumentBuilder parser =
            DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = parser.parse(new InputSource(new StringReader(theText)));
            validator.validate(new DOMSource(document));
            return "OK";

        } catch (SAXException ex) {
            return ex.getMessage();

        }
        
        catch(ParserConfigurationException pex)
        {
            return pex.getMessage();
        }
        
        catch(IOException iex)
        {
            return iex.getMessage();
        }

    }

     /**
     * Validate a Document against a schema
     * @return OK if ok , errormessage otherwise
     * @param errors An errorhandler
     * @param document The document we want to validate
     * @param theSchema The schema we want to validate against
     */   
    public static String ValidateString(Document document,URI theSchema,validationErrorHandler errors)
    {
        SchemaFactory factory =
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema;
        
        try {
            schema = factory.newSchema(theSchema.toURL());
            Validator validator = schema.newValidator();
            validator.setErrorHandler(errors);
        
            
            validator.validate(new DOMSource(document));
            return "OK";

        } catch (SAXException ex) {
            return ex.getMessage();

        }
                
        catch(IOException iex)
        {
            return iex.getMessage();
        }

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

   public static boolean copyfile(String srFile, String dtFile){
    try{
      File f1 = new File(srFile);
      File f2 = new File(dtFile);
      if(!f2.exists())
        f2.getParentFile().mkdirs(); //f2.mkdirs();
      InputStream in = new FileInputStream(f1);

      OutputStream out = new FileOutputStream(f2);

      byte[] buf = new byte[1024];
      int len;
      while ((len = in.read(buf)) > 0){
        out.write(buf, 0, len);
      }
      in.close();
      out.close();
      System.out.println("File copied.");
      return true;
    }
    catch(FileNotFoundException ex){
      System.out.println(ex.getMessage() + " in the specified directory.");
      return false;
    }
    catch(IOException e){
      System.out.println(e.getMessage());
      return false;
    }
  }


}

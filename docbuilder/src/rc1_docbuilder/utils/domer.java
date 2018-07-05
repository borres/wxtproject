package rc1_docbuilder.utils;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import javax.xml.xpath.*;
import org.w3c.dom.DOMException;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * This class implements a static library for handling DOM-actions.
 * <p>
 * Building Document from String etc.
 * @author borres
 */
public class domer extends java.lang.Object {
    
     static DocumentBuilder m_documentbuilder;
     static XPath m_xpath;
    
     static private String default_encoding="UTF-8";

    public static void setDefaultEncoding(String s)
    {default_encoding=s;}
    public static String getDefaultEncoding()
    {return default_encoding;}
    
            
     public domer() {   }
    

     /** 
      * Make a documentbuilder.
      * 
     * @return a documentbuilder
     * @throws ParserConfigurationException when bad parser configuration
     * @throws FactoryConfigurationError when bad factory configuration
     */
    static public DocumentBuilder makeDocBuilder()
        throws ParserConfigurationException,FactoryConfigurationError{

            if(true)
            //if(m_documentbuilder==null)
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
                
                dFact.setExpandEntityReferences(false);
                m_documentbuilder=dFact.newDocumentBuilder();

            }
            
            return m_documentbuilder;
    }
    
     /** 
      * Make an XPath object.
      *  
     * @return Xpath object
     * @throws Exception
     */
     static public XPath makeXPath()
    throws Exception{
        if(m_xpath==null)
        {
            XPathFactory factory=XPathFactory.newInstance();
            m_xpath=factory.newXPath();
        }
        return m_xpath;        
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
        StringReader sr=null;
        InputSource is=null;
        DocumentBuilder dBuilder=null;

        try {
            dBuilder = makeDocBuilder();
            sr=new StringReader(S);
            is=new InputSource(sr);
            Document doc=dBuilder.parse(is);
            return doc;
        }
        catch (SAXParseException exception)
        {
            throw new Exception(exception.getMessage());
        }
        catch (Exception e) 
        {
            // for debugging only
            throw new Exception(e.getMessage());
        }
        finally{
            if(sr!=null)
                sr.close();
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

      /** 
       * Establish a Dom from a transformed XML-file.
       * 
      * @param theString the XML-file as a string
      * @param transUri Where we find the transformation
      * @param parameters The parameters that will be applied in the transformation
      * @return a Document
      * @throws Exception when no DOM is established
      */
    public static Document makeTransformedDomFromString(String theString,URI transUri,HashMap<String,String> parameters)
    throws Exception{
        
        try{
            TransformerFactory transFac=TransformerFactory.newInstance();                        
            
            DocumentBuilderFactory dFactory=DocumentBuilderFactory.newInstance();
            dFactory.setNamespaceAware(true); 
            DocumentBuilder docBuilder=dFactory.newDocumentBuilder();
            Document xslDoc=docBuilder.parse(transUri.toString());
            DOMSource xslDomSource=new DOMSource(xslDoc);
            xslDomSource.setSystemId(transUri.toString());
            
            DOMResult xmlDomResult=new DOMResult();
            //xmlDomResult.setSystemId(theUri.toString()); // ????
            
            //String ad=theUri.getPath();
            //StreamSource inStream=new StreamSource(ad); 
            
            StreamSource inStream= new StreamSource(new StringReader(theString));

            
            //StreamSource inStream=new StreamSource(theUri.toString()); 
            
            Transformer trans=transFac.newTransformer(xslDomSource);

            if(parameters!=null){
                for( Iterator it=parameters.keySet().iterator(); it.hasNext();)
                {
                    String key=(String)it.next();
                    trans.setParameter(key,parameters.get(key));
                }
            } 
            trans.transform(inStream,xmlDomResult);
            return (Document)xmlDomResult.getNode();
        }
         catch(FactoryConfigurationError f){
             throw new Exception(f.getMessage());
         }
        catch(IOException ioe){
            throw new Exception(ioe.getMessage());
        }
        catch(SAXException saxe){
            throw new Exception(saxe.getMessage());
        }
        catch(IllegalArgumentException iae){
            throw new Exception(iae.getMessage());
        }
        catch(Exception e){
            throw new Exception(e.getMessage());
        }
    }
    
     /** 
      * Produce a transformed string from a string.
      * 
      * @param theString the XML-file as a string
      * @param transUri Where we find the transformation
      * @param parameters The parameters that will be applied in the transformation
      * @return a string
      * @throws Exception when no DOM is established
      */
    public static String makeTransformedStringFromString(String theString,URI transUri,HashMap<String,String> parameters)
    throws Exception{
        
        try{
            TransformerFactory transFac=TransformerFactory.newInstance();                        
            
            DocumentBuilderFactory dFactory=DocumentBuilderFactory.newInstance();
            dFactory.setNamespaceAware(true); 
            DocumentBuilder docBuilder=dFactory.newDocumentBuilder();
            Document xslDoc=docBuilder.parse(transUri.toString());
            DOMSource xslDomSource=new DOMSource(xslDoc);
            xslDomSource.setSystemId(transUri.toString());
            
            DOMResult xmlDomResult=new DOMResult();
            //xmlDomResult.setSystemId(theUri.toString()); // ????
            
            //String ad=theUri.getPath();
            //StreamSource inStream=new StreamSource(ad); 
            
            StreamSource inStream= new StreamSource(new StringReader(theString));
            
            //StreamSource inStream=new StreamSource(theUri.toString()); 
            
            Transformer trans=transFac.newTransformer(xslDomSource);

            if(parameters!=null){
                for( Iterator it=parameters.keySet().iterator(); it.hasNext();){
                    String key=(String)it.next();
                    trans.setParameter(key,parameters.get(key));
                }
            } 
            
             
            java.io.StringWriter s=new java.io.StringWriter();
            
            StreamResult out=new StreamResult(s);
            
            trans.transform(inStream,out);
            return s.toString();
        }
         catch(FactoryConfigurationError f){
             throw new Exception(f.getMessage());
         }
        catch(IOException ioe){
            throw new Exception(ioe.getMessage());
        }
        catch(SAXException saxe){
            throw new Exception(saxe.getMessage());
        }
        catch(IllegalArgumentException iae){
            throw new Exception(iae.getMessage());
        }
        catch(Exception e){
            throw new Exception(e.getMessage());
        }
    }
    
      /** 
       * Establish a Dom from a transformed XML-file.
       * 
      * @param theUri Where the XML-file is located
      * @param transUri Where we find the transformation
      * @param parameters The parameters that will be applied in the transformation
      * @return a Document
      * @throws Exception when no DOM is established
      */
    public static Document makeTransformedDomFromUri(URI theUri,URI transUri,HashMap<String,String> parameters)
    throws Exception{
        
        try{
            TransformerFactory transFac=TransformerFactory.newInstance();                        
            
            DocumentBuilderFactory dFactory=DocumentBuilderFactory.newInstance();
            dFactory.setNamespaceAware(true); 
            DocumentBuilder docBuilder=dFactory.newDocumentBuilder();
            Document xslDoc=docBuilder.parse(transUri.toString());
            DOMSource xslDomSource=new DOMSource(xslDoc);
            xslDomSource.setSystemId(transUri.toString());
            
            DOMResult xmlDomResult=new DOMResult();
            xmlDomResult.setSystemId(theUri.toString()); // ????
            
            //String ad=theUri.getPath();
            //StreamSource inStream=new StreamSource(ad); 
            
            StreamSource inStream=new StreamSource(theUri.toString()); 
            
            Transformer trans=transFac.newTransformer(xslDomSource);

            if(parameters!=null){
                for( Iterator it=parameters.keySet().iterator(); it.hasNext();){
                    String key=(String)it.next();
                    trans.setParameter(key,parameters.get(key));
                }
            } 
            
            

            trans.transform(inStream,xmlDomResult);
            return (Document)xmlDomResult.getNode();
        }
         catch(FactoryConfigurationError f){
             throw new Exception(f.getMessage());
         }
        catch(IOException ioe){
            throw new Exception(ioe.getMessage());
        }
        catch(SAXException saxe){
            throw new Exception(saxe.getMessage());
        }
        catch(IllegalArgumentException iae){
            throw new Exception(iae.getMessage());
        }
        catch(Exception e){
            throw new Exception(e.getMessage());
        }
    }

    
     /** 
      * Saves a dom tree to file with an identity transformation.
      * 
      * @param doc The document(DOM)
      * @param theUri Where it goes
      * @param encoding The encoding we want
      * @param bIndent Indent output or not. Use not when preparing for Prince
      * @throws Exception when save is not accomplished
      */
    public static void saveDom(Document doc,URI theUri,String encoding,boolean bIndent,String output)
    throws Exception{
        if(doc==null)
            throw(new Exception("No_dom_to_save"));
        if(theUri==null)
            throw(new Exception("No_uri_to_save_to"));
        output=output.toLowerCase().trim();
        if((output.compareToIgnoreCase("xml")!=0)&&
           (output.compareToIgnoreCase("html")!=0)&&
           (output.compareToIgnoreCase("text")!=0)&&
           (output.compareToIgnoreCase("xhtml")!=0))
        {
            throw(new Exception("unknown_output_method"+": "+output));
        }

        String ds=saveDomToString(doc,encoding,bIndent,output);
        accessutils.saveTFile(theUri, ds, encoding);
        // It would be slightly faster to transform directly to URI
        // but making the string first gives better control and one place to
        // work with the rather tricky code
        

        /*try{            
            //doc.normalize();
            DOMSource xmlDomSource=new DOMSource(doc);
            xmlDomSource.setSystemId(theUri.toString()); // ????            
            

                        
            // make an identity transformation
            TransformerFactory transFac=TransformerFactory.newInstance();                        
            Transformer trans=transFac.newTransformer();
            
            // set transformatoion properties
            // since this is an identity transformation
            // and no properties are set (exept defaults)
            DocumentType dt=doc.getDoctype();
            if(dt!=null)
            {
                trans.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC,dt.getPublicId());
                trans.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,dt.getSystemId());

            }
            
            if(htmloutput)
            {
                trans.setOutputProperty(OutputKeys.METHOD, "html");                
                trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,"yes");
            }
            else
            {
                trans.setOutputProperty(OutputKeys.METHOD, "xml");
            }
            
            if(bIndent)
            {
                trans.setOutputProperty(OutputKeys.INDENT, "yes");
                trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            }
            else
            {
                // use no to avoid prince-problems with toc targets
                trans.setOutputProperty(OutputKeys.INDENT, "no");
            }
            //
            
            // find out how we should form output
            // make sure we have some encoding
             if(encoding==null)
                 encoding=doc.getXmlEncoding();
             if(encoding==null)
                encoding=default_encoding;

            
            
            //testing:trans.getOutputProperties().list(System.out);
            
            // set outputstreamwriter
            // make sure file exists
            if(!accessutils.makeCatalog(theUri)){
                throw new Exception("Cant make file");
            }
            
            java.io.FileOutputStream out=new java.io.FileOutputStream(theUri.getPath());
           
            java.io.OutputStreamWriter os=new java.io.OutputStreamWriter(out,encoding);
            java.io.BufferedWriter bos=new java.io.BufferedWriter(os);
            StreamResult outStream=new StreamResult(bos);
            trans.transform(xmlDomSource,outStream);
            
         
         }
        catch(java.io.UnsupportedEncodingException ex){
            System.out.println("domer:saveDom. "+ex.getMessage());
            throw new Exception(ex.getMessage());
        }
        catch(TransformerConfigurationException tce){
            System.out.println("domer:saveDom. "+tce.getMessage());
            throw new Exception(tce.getMessage());
        }
        catch(TransformerException te){
            System.out.println("domer:saveDom. "+te.getMessage());
            throw new Exception(te.getMessage());
        }
         catch(FactoryConfigurationError f){
             System.out.println("domer:saveDom. "+f.getMessage());
             throw new Exception(f.getMessage());
         }
        catch(IllegalArgumentException iae){
             System.out.println("domer:saveDom. "+iae.getMessage());
            throw new Exception(iae.getMessage());
        }
        catch(Exception e){
            System.out.println("domer:saveDom. "+e.getMessage());
            throw new Exception(e.getMessage());
        }*/
    }

      /** 
       * Writes a dom tree to string with an identity transformation.
       * 
      * @param doc The document(DOM)
      * @param encoding The encoding we want
      * @return the prepared string
      * @throws Exception when save is not accomplished
      */
    public static String saveDomToString(Document doc,String encoding,boolean bIndent,String output)
    throws Exception{
        if(doc==null)
            throw(new Exception("No_dom_to_save"));
        // find out how we should form output
        // make sure we have some encoding
        if(encoding==null)
            encoding=doc.getXmlEncoding();
        if(encoding==null)
            encoding=default_encoding;

        try{            
            DOMSource xmlDomSource=new DOMSource(doc);         
                         
            // make an identity transformation
            TransformerFactory transFac=TransformerFactory.newInstance();                        
            Transformer trans=transFac.newTransformer();
            // set transformatoion properties
            // since this is an identity transformation
            // and no properties are set (exept defaults)
            DocumentType dt=doc.getDoctype();
            if(dt!=null)
            {
                trans.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC,dt.getPublicId());
                trans.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,dt.getSystemId());

            }
            
           
            
            trans.setOutputProperty(OutputKeys.ENCODING,encoding);
            if(output.compareToIgnoreCase("xhtml")==0)
            {
                trans.setOutputProperty(OutputKeys.METHOD, "xml");
                trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,"yes");
            }                
            else
            {
                trans.setOutputProperty(OutputKeys.METHOD, output);
                if(output.compareToIgnoreCase("xml")==0)   
                {
                    trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,"no");
                }
            }
            if(output.compareToIgnoreCase("text")==0)
            {
                trans.setOutputProperty(OutputKeys.MEDIA_TYPE,"text/plain; charset="+encoding);
            }
            
            if(bIndent)
            {
                trans.setOutputProperty(OutputKeys.INDENT, "yes");
                trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            }
            else
            {
                // use no to avoid prince-problems with toc targets
                trans.setOutputProperty(OutputKeys.INDENT, "no");
            }

            //Properties props=propsFromDoc(doc,"xml",encoding);
            //trans.setOutputProperties(props);
 
            java.io.StringWriter out = new java.io.StringWriter();
            java.io.BufferedWriter bos=new java.io.BufferedWriter(out);
            StreamResult outStream=new StreamResult(bos);
            trans.transform(xmlDomSource,outStream);
            bos.flush();
            
            return out.toString().toString();
         
         }
        catch(java.io.UnsupportedEncodingException ex){
            System.out.println("domer:saveDomToString. "+ex.getMessage());
            throw new Exception(ex.getMessage());
        }
        catch(TransformerConfigurationException tce){
            System.out.println("domer:saveDomToString. "+tce.getMessage());
            throw new Exception(tce.getMessage());
        }
        catch(TransformerException te){
            System.out.println("domer:saveDomToString. "+te.getMessage());
            throw new Exception(te.getMessage());
        }
         catch(FactoryConfigurationError f){
             System.out.println("domer:saveDomToString. "+f.getMessage());
             throw new Exception(f.getMessage());
         }
        catch(IllegalArgumentException iae){
             System.out.println("domer:saveDomToString. "+iae.getMessage());
            throw new Exception(iae.getMessage());
        }
        catch(Exception e){
            System.out.println("domer:saveDomToString. "+e.getMessage());
            throw new Exception(e.getMessage());
        }
    }
    
    
    /** 
     * Saves a dom tree to file after a transformation.
      * @param doc The document(DOM)
      * @param theUri Where it goes
      * @param transUri The URI for the transformation
      * @param parameters The parameters that will be set for the transformation
      * @throws Exception when save is not accomplished
      */
    public static void saveTransformedDom(Document doc,URI theUri,URI transUri,
                                          HashMap<String,String> parameters)
    throws Exception{
        if(doc==null)
            throw(new Exception("No_dom_to_save"));
        try{
            TransformerFactory transFac=TransformerFactory.newInstance();                                   
            
            DocumentBuilderFactory dFactory=DocumentBuilderFactory.newInstance();
            // must be set true for transformations
            dFactory.setNamespaceAware(true); 
            DocumentBuilder docBuilder=dFactory.newDocumentBuilder();
            Document xslDoc=docBuilder.parse(transUri.toString());
            DOMSource xslDomSource=new DOMSource(xslDoc);
            xslDomSource.setSystemId(theUri.toString());// ?? transUri
            DOMSource xmlDomSource=new DOMSource(doc);
            Transformer trans=transFac.newTransformer(xslDomSource);
            
            // all transformation properties are assumed set by transformation author
            // so no: trans.setOutputProperty(OutputKeys.xxx,"");
            // it would be possible to overrun properties by global options ?
            
            // deal with parameters. Simply set them as is
            if(parameters!=null){
                for( Iterator it=parameters.keySet().iterator(); it.hasNext();){
                    String key=(String)it.next();
                    trans.setParameter(key,parameters.get(key));
                }
            }
            
            
            // set outputstreamwriter
            // make sure file exists
            if(!accessutils.makeCatalog(theUri)){
                throw new Exception("Cant make file");
            }
            
            java.io.FileOutputStream out=new java.io.FileOutputStream(theUri.getPath());
            java.io.OutputStreamWriter os=new java.io.OutputStreamWriter(out,trans.getOutputProperty(OutputKeys.ENCODING)); 
            java.io.BufferedWriter bos=new java.io.BufferedWriter(os);
            StreamResult outStream=new StreamResult(bos);
            
            // do it            
            trans.transform(xmlDomSource,outStream);
         }
        catch(java.io.UnsupportedEncodingException ex){
            System.out.println("domer:saveTransformedDom: "+ex.getMessage());
            throw new Exception(ex.getMessage());
        }
        catch(TransformerConfigurationException tce){
            System.out.println("domer:saveTransformedDom: "+tce.getMessage());
            throw new Exception(tce.getMessage());
        }
        catch(TransformerException te){
            System.out.println("domer:saveTransformedDom: "+te.getMessage());
            throw new Exception(te.getMessage());
        }
         catch(FactoryConfigurationError f){
             System.out.println("domer:saveTransformedDom: "+f.getMessage());
             throw new Exception(f.getMessage());
         }
        catch(IOException ioe){
            System.out.println("domer:saveTransformedDom: "+ioe.getMessage());
            throw new Exception(ioe.getMessage());
        }
        catch(SAXException saxe){
            System.out.println("domer:saveTransformedDom: "+saxe.getMessage());
            throw new Exception(saxe.getMessage());
        }
        catch(IllegalArgumentException iae){
             System.out.println("domer:saveTransformedDom: "+iae.getMessage());
            throw new Exception(iae.getMessage());
        }
        catch(Exception e){
            System.out.println("domer:saveTransformedDom: "+e.getMessage());
            throw new Exception(e.getMessage());
        }
    }
    
        /** 
      * Saves a dom tree to a string after a transformation.
      * @param doc The document(DOM)
      * @param theUri Where it goes. Can be any name ?
      * @param transUri The URI for the transformation
      * @param parameters The parameters that will be set for the transformation
      * @throws Exception when save is not accomplished
      */
    public static String saveTransformedDomToString(Document doc,URI theUri,URI transUri,
                                          HashMap<String,String> parameters)
    throws Exception{
        if(doc==null)
            throw(new Exception("No_dom_to_save"));
        try{
            TransformerFactory transFac=TransformerFactory.newInstance();                                   
            
            DocumentBuilderFactory dFactory=DocumentBuilderFactory.newInstance();
            
            // must be set true for transformations
            dFactory.setNamespaceAware(true); 
            DocumentBuilder docBuilder=dFactory.newDocumentBuilder();
            Document xslDoc=docBuilder.parse(transUri.toString());
            
            DOMSource xslDomSource=new DOMSource(xslDoc);
            // where urls will be resolved. Nomeaning if we dont need
            // to access other files during transformation
            xslDomSource.setSystemId(theUri.toString());// ?? transUri | anything
            DOMSource xmlDomSource=new DOMSource(doc);
            Transformer trans=transFac.newTransformer(xslDomSource);
            
            // all transformation properties are assumed set by transformation author
            // so no: trans.setOutputProperty(OutputKeys.xxx,"");
            // it would be possible to overrun properties by global options ?
            
            // deal with parameters. Simply set them as is
            if(parameters!=null){
                for( Iterator it=parameters.keySet().iterator(); it.hasNext();){
                    String key=(String)it.next();
                    trans.setParameter(key,parameters.get(key));
                }
            }
            
            java.io.StringWriter out = new java.io.StringWriter();
            java.io.BufferedWriter bos=new java.io.BufferedWriter(out);
            StreamResult outStream=new StreamResult(bos);
            trans.transform(xmlDomSource,outStream);
            bos.flush();
            // while testing
            String tst=out.toString().toString();
            
            return out.toString().toString();

         }
        catch(java.io.UnsupportedEncodingException ex){
            System.out.println("domer:saveTransformedDom: "+ex.getMessage());
            throw new Exception(ex.getMessage());
        }
        catch(TransformerConfigurationException tce){
            System.out.println("domer:saveTransformedDom: "+tce.getMessage());
            throw new Exception(tce.getMessage());
        }
        catch(TransformerException te){
            System.out.println("domer:saveTransformedDom: "+te.getMessage());
            throw new Exception(te.getMessage());
        }
         catch(FactoryConfigurationError f){
             System.out.println("domer:saveTransformedDom: "+f.getMessage());
             throw new Exception(f.getMessage());
         }
        catch(IOException ioe){
            System.out.println("domer:saveTransformedDom: "+ioe.getMessage());
            throw new Exception(ioe.getMessage());
        }
        catch(SAXException saxe){
            System.out.println("domer:saveTransformedDom: "+saxe.getMessage());
            throw new Exception(saxe.getMessage());
        }
        catch(IllegalArgumentException iae){
             System.out.println("domer:saveTransformedDom: "+iae.getMessage());
            throw new Exception(iae.getMessage());
        }
        catch(Exception e){
            System.out.println("domer:saveTransformedDom: "+e.getMessage());
            throw new Exception(e.getMessage());
        }
    }


    /**
      * Produce a list of all Processing Instructions with a
      * certain name below a node in a tree.
     * 
      * @param n The node defining the subtree we will investigate
      * @param target The name of the PIs we are looking for
      * @return a list of PI-nodes
      */
     public static Vector<Node> getPIs(Node n,String target){
        // If there are any children, visit each one
        Vector<Node> result=new Vector<Node>(10,10);
        Node nod=n.getFirstChild();
        while(nod!=null){
            if((nod.getNodeType()==Node.PROCESSING_INSTRUCTION_NODE)
             &&(nod.getNodeName().compareToIgnoreCase(target)==0))
                result.add(nod);
            else
                result.addAll(getPIs(nod,target));
            nod=nod.getNextSibling();
        }
        return result;
    }


     /**
     * Find a list of all Processing Instructions below a node in a tree.
      * 
     * @param n The node defining the subtree
     * @return a list of PI-nodes
     */    
    public static Vector<Node> getPIs(Node n){
         // If there are any children, visit each one
        Vector<Node> result=new Vector<Node>(10,10);
        Node nod=n.getFirstChild();
        while(nod!=null){
            if(nod.getNodeType()==Node.PROCESSING_INSTRUCTION_NODE)
                result.add(nod);
            else
                result.addAll(getPIs(nod));
            nod=nod.getNextSibling();
        }
        return result;
    }
    
    /**
     * Query a Document for a NodeList.
     * 
     * @param doc The Document we will query
     * @param xp The XPath expression
     * @return a list of nodes
     * @throws java.lang.Exception
     */
    public static NodeList performXPathQuery(Document doc,String xp)
    throws Exception{
        makeXPath();
        XPathExpression expr=m_xpath.compile(xp);
        return (NodeList)expr.evaluate(doc,XPathConstants.NODESET); 
    }
    
    /**
     * Get an element and all its children which are elements.
     * 
     * @param elt The root
     * @return a list of elements
     */
    public static Vector<Element> getElementsPreorder(Element elt)
    {
        Vector<Element> res=new Vector<Element>();
        res.add(elt);
        Node nod=elt.getFirstChild();
        while(nod!=null)
        {
            if(nod.getNodeType()==Node.ELEMENT_NODE)
                res.addAll(getElementsPreorder((Element)nod));
            nod=nod.getNextSibling();
        }
        return res;
    }
    
     /**
     * Try to establish a documentfragment from a string.
      * 
     * @param theText The text we will attempt to interpret as XML
     * @param enc The encoding we want, defaults to default_encoding
     * @return A documenfragment if succeeded, null otherwise
     * @throws Exception when the String can not be parsed
     */
     public static DocumentFragment produceDocFragmentFromString(String theText,String enc)
     throws Exception{
        if (theText==null)
             return null;
        if(enc==null)
             enc=default_encoding;
        theText=theText.trim();
        if(!theText.startsWith("<?xml"))
        {
            theText="<?xml version=\"1.0\" encoding=\""+
                    enc+
                    "\"?><root>"+
                    System.getProperty("line.separator")+
                    theText+
                    System.getProperty("line.separator")+
                    "</root>";
            //?? does not seem to help
            byte[] data=theText.getBytes(enc);
            theText=new String(data,enc);
        }
         
         try{
             Document doc=domer.makeDomFromString(theText);
             doc.normalize();
             DocumentFragment df=doc.createDocumentFragment();
             NodeList nlist=doc.getDocumentElement().getChildNodes();
             //int L=nlist.getLength();
             for(int ix=0;ix<nlist.getLength();ix++)
             {
                 String tmp=nlist.item(ix).getNodeName();
                 df.appendChild(nlist.item(ix).cloneNode(true));
             }
             
             //NodeList tmplist=df.getChildNodes();
             //int tmpL=tmplist.getLength();
             
             return df;
         }

          catch(DOMException e){
             throw(new Exception(e.getMessage()));
         }

         catch(Exception e){
             throw(new Exception(e.getMessage()));
         }
    }
    /**
     * Try to produce an XTML page form a fragment
     * 
     * @param content The fragment as a string
     * @param encoding The encoding used
     * @param motherDoc The Doument where we steal the header element
     * @return A Document if we succed, otherwise null
     */
     public static Document produceHTMLPageFromContent(String content,String encoding,Document motherDoc)
     {
         if(encoding==null)
             encoding=default_encoding;
         String XMLHeader="<?xml version=\"1.0\" encoding=\""+encoding+"\" standalone=\"yes\"?>";
         String html="<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">";
         String HTMLProlog=html+"<head><title>popped</title></head><body>";
         String HTMLEpilog="</body></html>";
         content=content.trim();
         Document Doc=null;
         
         // is it already ok
         if( content.startsWith("<xml") || content.startsWith("<html"))
         {
             try{
                 Doc= makeDomFromString(content);
             }
             catch(Exception ex) 
             {
                 Doc=null;
             }
         }
         
         
         // we must try to make it
         if((Doc==null) && content.startsWith("<html"))
         {
             String tmp=XMLHeader+content;
             try{
                 Doc= makeDomFromString(tmp);
             }
             catch(Exception ex) 
             {
                 Doc=null;
             }
         }
         
         // we must try again
         if(Doc==null)
         {
             String tmp=XMLHeader+HTMLProlog+content+HTMLEpilog;
             try{
                 Doc= makeDomFromString(tmp);
             }
             catch(Exception ex) 
             {
                 String what=ex.getMessage();
                 Doc=null;
             }
         }
         // give up ?
         if(Doc==null)
         {
             return null;
         }
         
         // modify Doc by stealing modules head-element
         if((motherDoc.getElementsByTagName("head")!=null) && 
            (Doc.getElementsByTagName("head")!=null))
         {
             try{
                 Node headNodeOut=Doc.getElementsByTagName("head").item(0);
                 Node headNodeIn=Doc.importNode(motherDoc.getElementsByTagName("head").item(0),true);
                 headNodeOut.getParentNode().replaceChild(headNodeIn,headNodeOut);
                 return Doc;
             } 
             catch(Exception ex)
             {
                 return Doc;
             }
         }
         return Doc;
     }
}

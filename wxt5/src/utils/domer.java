package utils;

import reporting.validationErrorHandler;
import reporting.reporter;
import content.Definitions;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
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
 * @author bs
 */
public class domer extends java.lang.Object {

    static XPath m_xpath;
    static private String default_encoding = "UTF-8";
    static Definitions m_Def=null;;

    public static void setDefaultEncoding(String s) {
        default_encoding = s;
    }

    public static String getDefaultEncoding() {
        return default_encoding;
    }

    public static void setDefinitions(Definitions def) {
        m_Def = def;
    }

    public domer() {
    }

    /** 
     * Make a documentbuilder.
     * 
     * @return a documentbuilder
     * @throws ParserConfigurationException when bad parser configuration
     * @throws FactoryConfigurationError when bad factory configuration
     */
    static public DocumentBuilder makeDocBuilder(boolean namespaceAware)
            throws ParserConfigurationException, FactoryConfigurationError {
        DocumentBuilderFactory dFact = DocumentBuilderFactory.newInstance();
        // we can set up validation and other things here
        dFact.setValidating(false);
        dFact.setIgnoringComments(false);
        dFact.setIgnoringElementContentWhitespace(false);
        // avoid html-namespace disrupts xpath search in adresse calculation ???
        dFact.setNamespaceAware(namespaceAware);

        // try to avoid search for dtds
        // this is imperative to speed !!!!!!
        // TODO: check feature
        dFact.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        dFact.setAttribute("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);

        //dFact.setAttribute("http://apache.org/xml/features/allow-java-encodings",new Boolean(true));
        dFact.setAttribute("http://apache.org/xml/features/dom/defer-node-expansion", false);

        dFact.setExpandEntityReferences(false);
        return dFact.newDocumentBuilder();
    }

    /** 
     * Make an XPath object.
     *  
     * @return Xpath object
     * @throws Exception
     */
    static public XPath makeXPath()
            throws Exception {
        if (m_xpath == null) {
            XPathFactory factory = XPathFactory.newInstance();
            m_xpath = factory.newXPath();
        }
        return m_xpath;
    }

    /** 
     * Establish a Document from a Source.
     * 
     * @param S the source that should be parsed
     * @return the document built
     * @throws Exception when no Document is established
     */
    public static Document makeDomFromSource(InputSource S)
            throws Exception {
        DocumentBuilder dBuilder = null;

        try {
            dBuilder = makeDocBuilder(false);
            Document doc = dBuilder.parse(S);
            return doc;
        } catch (SAXParseException exception) {
            throw new Exception(exception.getMessage());
        } catch (Exception e) {
            // for debugging only
            throw e;
            //throw new Exception(e.getMessage());
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
        StringReader sr = null;
        InputSource is = null;
        DocumentBuilder dBuilder = null;

        try {
            dBuilder = makeDocBuilder(false);
            sr = new StringReader(S);
            is = new InputSource(sr);
            Document doc = dBuilder.parse(is);
            return doc;
        } catch (SAXParseException exception) {
            throw new Exception(exception.getMessage());
        } catch (IOException iox) {
            throw iox;
        } catch (Exception e) {
            // for debugging only
            throw e;
            //throw new Exception(e.getMessage());
        } finally {
            if (sr != null) {
                sr.close();
            }
        }
    }

    /**
     * Establish a Document from a String. Clean if possible and necessary.
     *
     * @param S the XML-string that should be parsed
     * @param encoding the encoding we want if not available
     * @param theReporter where to report
     * @return the document built
     * @throws Exception when no Document is established
     */
    public static Document makeDomFromStringSomeHow(String S, String encoding, reporter theReporter)
            throws Exception {
        if (encoding == null) {
            encoding = default_encoding;
        }
        // add header if necessary
        S = S.trim();
        if (!S.startsWith("<?xml")) {
            S = "<?xml version=\"1.0\" encoding=\"" + encoding + "\" ?>\n" + S;
        }


        try {
            return makeDomFromString(S);
        } catch (IOException iox) {
            // nothing more we can do
            throw iox;
        } catch (Exception exception) {
            if ((m_Def != null)&&(m_Def.getSoupEngine() != null)) {
                try {
                    // --------- attempt to tidy, final attempt ----------
                    S = m_Def.getSoupEngine().makeXmlStringFromString(S, encoding);

                    return makeDomFromString(S);
                } catch (Exception sex3) {
                    // nothing more we can do
                    throw sex3;
                }
            } else {
                // we will do nothing
                throw new Exception("no tidy");
            }
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
            throws Exception {
        try {
            DocumentBuilder dBuilder = makeDocBuilder(false);
            return dBuilder.parse(theUri.toString());
        } catch (SAXException se) {
            if (theUri.getScheme().startsWith("file")) {
                File f = new File(theUri.toURL().getFile());
                if ((f != null) && (f.exists())) {
                    FileReader fr = new FileReader(f);
                    fr.close();
                }
            }
            throw se;
        } catch (IOException ioex) {
            throw ioex;
        } catch (IllegalArgumentException iaex) {
            throw iaex;
        } catch (Exception sex) {
            throw new Exception(sex.toString());
        }
    }

    /** 
     * Establish a Document from a XML-file, one way or another.
     * 
     * @param theUri Where the XML-file is located
     * @param encoding The encoding we want if not available in XML-header
     * @param theReporter where we want to report
     * @return the document built
     * @throws Exception when no Document is established
     */
    public static Document makeDomFromUriSomeHow(URI theUri, String encoding, reporter theReporter)
            throws Exception {
        if (encoding == null) {
            encoding = default_encoding;
        }
        if (theUri == null) {
            throw new Exception("Missing URI");
        }

        // ----- strait ----
        try {
            return makeDomFromUri(theUri);
        } catch (IOException ioex) {
            // nothing more we can do
            //throw ioex;
            throw new Exception("File not found or loaded");
        } catch (Exception se) {
            theReporter.pushSimpleMessage("\t" + se.getMessage() + "\n\tWarning: Attempt to load text and tidy");
            // ------- we must get the source -------
            System.out.println("load source: " + theUri.toString());
            String source = null;
            try {
                // should this be made BOMsafe:
                //source=accessutils.getBOMSafeTextFile(theUri, encoding);
                source = accessutils.getTextFile(theUri, encoding);
                source = source.trim();
                return makeDomFromStringSomeHow(source, encoding, theReporter);
            } catch (Exception sex) {
                // nothing more we can do 
                throw sex;
            }
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
    public static Document makeTransformedDomFromStringSomeHow(String theString, URI transUri,
            HashMap<String, String> parameters, String encoding, reporter theReporter)
            throws Exception {
        if (encoding == null) {
            encoding = default_encoding;
        }
        theString = theString.trim();
        if (!theString.startsWith("<?xml")) {
            theString = "<?xml version=\"1.0\" encoding=\"" + encoding + "\" ?>\n" + theString;
        }

        try {
            return makeTransformedDomFromString(theString, transUri, parameters);
        } catch (IOException iox) {
            //nothing more we can do
            throw iox;
        } catch (Exception ex) {
            if ((m_Def != null) &&(m_Def.getSoupEngine() != null)) {
                // --------- attempt to tidy, final attempt ----------
                try {
                    theString = m_Def.getSoupEngine().makeXmlStringFromString(theString, encoding);
                    return makeTransformedDomFromString(theString, transUri, parameters);
                } catch (Exception sex3) {
                    // nothing more we can do
                    throw sex3;
                }
            } else {
                // we will do nothing
                throw new Exception("no tidy");
            }
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
    public static Document makeTransformedDomFromString(String theString, URI transUri, HashMap<String, String> parameters)
            throws Exception {

        try {
            TransformerFactory transFac = TransformerFactory.newInstance();

            //DocumentBuilderFactory dFactory=DocumentBuilderFactory.newInstance();
            //dFactory.setNamespaceAware(true); 
            //DocumentBuilder docBuilder=dFactory.newDocumentBuilder();

            DocumentBuilder docBuilder = makeDocBuilder(true);
            Document xslDoc = docBuilder.parse(transUri.toString());
            DOMSource xslDomSource = new DOMSource(xslDoc);
            xslDomSource.setSystemId(transUri.toString());

            DOMResult xmlDomResult = new DOMResult();
            //xmlDomResult.setSystemId(theUri.toString()); // ????

            //String ad=theUri.getPath();
            //StreamSource inStream=new StreamSource(ad); 

            StreamSource inStream = new StreamSource(new StringReader(theString));


            //StreamSource inStream=new StreamSource(theUri.toString()); 

            Transformer trans = transFac.newTransformer(xslDomSource);

            if (parameters != null) {
                for (Iterator<String> it = parameters.keySet().iterator(); it.hasNext();) {
                    String key = it.next();
                    trans.setParameter(key, parameters.get(key));
                }
            }
            trans.transform(inStream, xmlDomResult);
            return (Document) xmlDomResult.getNode();
        } catch (FactoryConfigurationError f) {
            throw new Exception(f.getMessage());
        } catch (IOException ioe) {
            throw ioe;
        } catch (SAXException saxe) {
            throw new Exception(saxe.getMessage());
        } catch (IllegalArgumentException iae) {
            throw new Exception(iae.getMessage());
        } catch (Exception e) {
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
    public static String makeTransformedStringFromString(String theString, URI transUri, HashMap<String, String> parameters)
            throws Exception {

        try {
            TransformerFactory transFac = TransformerFactory.newInstance();

            //DocumentBuilderFactory dFactory=DocumentBuilderFactory.newInstance();
            //dFactory.setNamespaceAware(true); 
            //DocumentBuilder docBuilder=dFactory.newDocumentBuilder();
            DocumentBuilder docBuilder = makeDocBuilder(true);

            Document xslDoc = docBuilder.parse(transUri.toString());

            DOMSource xslDomSource = new DOMSource(xslDoc);
            xslDomSource.setSystemId(transUri.toString());

            DOMResult xmlDomResult = new DOMResult();
            //xmlDomResult.setSystemId(theUri.toString()); // ????

            //String ad=theUri.getPath();
            //StreamSource inStream=new StreamSource(ad); 

            StreamSource inStream = new StreamSource(new StringReader(theString));

            //StreamSource inStream=new StreamSource(theUri.toString()); 

            Transformer trans = transFac.newTransformer(xslDomSource);

            if (parameters != null) {
                for (Iterator<String> it = parameters.keySet().iterator(); it.hasNext();) {
                    String key = it.next();
                    trans.setParameter(key, parameters.get(key));
                }
            }


            java.io.StringWriter s = new java.io.StringWriter();

            StreamResult out = new StreamResult(s);

            trans.transform(inStream, out);
            return s.toString();
        } catch (FactoryConfigurationError f) {
            throw new Exception(f.getMessage());
        } catch (IOException ioe) {
            throw new Exception(ioe.getMessage());
        } catch (SAXException saxe) {
            throw new Exception(saxe.getMessage());
        } catch (IllegalArgumentException iae) {
            throw new Exception(iae.getMessage());
        } catch (Exception e) {
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
    public static Document makeTransformedDomFromUri(URI theUri, URI transUri, HashMap<String, String> parameters)
            throws Exception {

        try {
            TransformerFactory transFac = TransformerFactory.newInstance();

            //DocumentBuilderFactory dFactory=DocumentBuilderFactory.newInstance();
            //dFactory.setNamespaceAware(true); 
            //DocumentBuilder docBuilder=dFactory.newDocumentBuilder();
            DocumentBuilder docBuilder = makeDocBuilder(true);
            Document xslDoc = docBuilder.parse(transUri.toString());
            DOMSource xslDomSource = new DOMSource(xslDoc);
            xslDomSource.setSystemId(transUri.toString());

            DOMResult xmlDomResult = new DOMResult();
            xmlDomResult.setSystemId(theUri.toString()); // ????

            //String ad=theUri.getPath();
            //StreamSource inStream=new StreamSource(ad); 

            StreamSource inStream = new StreamSource(theUri.toString());

            Transformer trans = transFac.newTransformer(xslDomSource);

            if (parameters != null) {
                for (Iterator<String> it = parameters.keySet().iterator(); it.hasNext();) {
                    String key = it.next();
                    trans.setParameter(key, parameters.get(key));
                }
            }



            trans.transform(inStream, xmlDomResult);
            return (Document) xmlDomResult.getNode();
        } catch (FactoryConfigurationError f) {
            throw new Exception(f.getMessage());
        } catch (IOException ioe) {
            throw ioe;
        } catch (SAXException saxe) {
            throw new Exception(saxe.getMessage());
        } catch (IllegalArgumentException iae) {
            throw new Exception(iae.getMessage());
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * Attempt to make a transformed document somehow, from URI, from String
     * from tidied string
     * @param theUri The Uri of the document we want to transform
     * @param transUri The Uri of the transformation
     * @param parameters The parameters to the transformation
     * @param encoding Encoding in the document if not available in header
     * @param theReporter Where to report
     * @return The ransformed document
     * @throws java.lang.Exception if we for some reason fails
     */
    public static Document makeTransformedDomFromUriSomeHow(URI theUri, URI transUri,
            HashMap<String, String> parameters, String encoding,
            reporter theReporter)
            throws Exception {
        if (encoding == null) {
            encoding = default_encoding;
        }
        if (theUri == null) {
            throw new Exception("Missing URI");
        }

        // ----- strait ----
        try {
            return makeTransformedDomFromUri(theUri, transUri, parameters);
        } catch (IOException ioex) {
            // nothing more we can do
            throw ioex;
        } catch (Exception se) {
            // ------- we must get the source -------
            String source = null;
            try {
                // should this be made BOMsafe:
                //source=accessutils.getBOMSafeTextFile(theUri, encoding);
                source = accessutils.getTextFile(theUri, encoding);
                source = source.trim();
                return makeTransformedDomFromStringSomeHow(source, transUri, parameters, encoding, theReporter);
            } catch (Exception sex) {
                // nothing more we can do since we dont get the source
                throw sex;
            }
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
    public static void saveDom(Document doc, URI theUri, String encoding, boolean bIndent, String output)
            throws Exception {
        if (doc == null) {
            throw (new Exception("No_dom_to_save"));
        }
        if (theUri == null) {
            throw (new Exception("No_uri_to_save_to"));
        }
        output = output.toLowerCase().trim();
        if ((output.compareToIgnoreCase("xml") != 0)
                && (output.compareToIgnoreCase("html") != 0)
                && (output.compareToIgnoreCase("text") != 0)
                && (output.compareToIgnoreCase("xhtml") != 0)) {
            throw (new Exception("unknown_output_method" + ": " + output));
        }
        if (encoding == null) {
            encoding = doc.getXmlEncoding();
        }
        if (encoding == null) {
            encoding = default_encoding;
        }

        String ds = saveDomToString(doc, encoding, bIndent, output);
        accessutils.saveTFile(theUri, ds, encoding);
        // It would be slightly faster to transform directly to URI
        // but making the string first gives better control and one place to
        // work with the rather tricky code

    }

    /** 
     * Writes a dom tree to string with an identity transformation.
     * 
     * @param doc The document(DOM)
     * @param encoding The encoding we want
     * @return the prepared string
     * @throws Exception when save is not accomplished
     */
    public static String saveDomToString(Document doc, String encoding, boolean bIndent, String output)
            throws Exception {
        if (doc == null) {
            throw (new Exception("No_dom_to_save"));
        }
        // find out how we should form output
        // make sure we have some encoding
        if (encoding == null) {
            encoding = doc.getXmlEncoding();
        }
        if (encoding == null) {
            encoding = default_encoding;
        }

        try {
            DOMSource xmlDomSource = new DOMSource(doc);

            // make an identity transformation
            TransformerFactory transFac = TransformerFactory.newInstance();
            Transformer trans = transFac.newTransformer();
            // set transformatoion properties
            // this is an identity transformation
            // and no properties are set exept defaults

            //NOTE: we have a problem with HTML5 which wants a simple header: 
            // <!DOCTYPE HTML>
            // we assume that the template is set accordingly
            // This will not give us PublicID and SystemID as null
            // we will have to detect this situastion before we return
            // the text, see below, after transformation

            // problem:
            // this will kill other transformed input that make html root
            // use explicit: html5 output option ? (yes)

            DocumentType dt = doc.getDoctype();
            String pid = null;
            String sid = null;
            String name = null;
            if (dt != null) {
                pid = dt.getPublicId();
                sid = dt.getSystemId();
                name = dt.getName();
            }


            boolean is_HTML5 = false;
            if (output.compareToIgnoreCase("text") == 0) {
                trans.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/plain; charset=" + encoding);
            } else if (output.compareToIgnoreCase("xml") == 0) {
                trans.setOutputProperty(OutputKeys.METHOD, "xml");
                trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
                if (pid != null) {
                    trans.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, pid);
                }
                if (sid != null) {
                    trans.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, sid);
                }

            } else if (output.compareToIgnoreCase("xhtml") == 0) {
                trans.setOutputProperty(OutputKeys.METHOD, "xml");
                trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                if (pid != null) {
                    trans.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, pid);
                }
                if (sid != null) {
                    trans.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, sid);
                }
            } else if ((output.compareToIgnoreCase("html5") == 0) || (output.compareToIgnoreCase("html") == 0)) {
                trans.setOutputProperty(OutputKeys.METHOD, "xml");
                trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                is_HTML5 = true;
            } else {
                throw new Exception("bad output type: " + output);
            }

            trans.setOutputProperty(OutputKeys.STANDALONE, "yes");
            trans.setOutputProperty(OutputKeys.ENCODING, encoding);

            if (bIndent) {
                trans.setOutputProperty(OutputKeys.INDENT, "yes");
                trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "0");
            } else {
                // use no to avoid prince-problems with toc targets ?????
                trans.setOutputProperty(OutputKeys.INDENT, "no");
            }

            java.io.StringWriter out = new java.io.StringWriter();
            java.io.BufferedWriter bos = new java.io.BufferedWriter(out);
            StreamResult outStream = new StreamResult(bos);
            trans.transform(xmlDomSource, outStream);
            bos.flush();

            // this is where we can clean up a doctype
            // prepared for html 5

            String result = out.toString().toString().trim();
            if (is_HTML5 && ((result.startsWith("<html")) || (result.startsWith("<HTML")))) {
                result = "<!DOCTYPE HTML>\n" + result;
            }

            return result;
        } catch (java.io.UnsupportedEncodingException ex) {
            System.out.println("domer:saveDomToString. " + ex.getMessage());
            throw new Exception(ex.getMessage());
        } catch (TransformerConfigurationException tce) {
            System.out.println("domer:saveDomToString. " + tce.getMessage());
            throw new Exception(tce.getMessage());
        } catch (TransformerException te) {
            System.out.println("domer:saveDomToString. " + te.getMessage());
            throw new Exception(te.getMessage());
        } catch (FactoryConfigurationError f) {
            System.out.println("domer:saveDomToString. " + f.getMessage());
            throw new Exception(f.getMessage());
        } catch (IllegalArgumentException iae) {
            System.out.println("domer:saveDomToString. " + iae.getMessage());
            throw new Exception(iae.getMessage());
        } catch (Exception e) {
            System.out.println("domer:saveDomToString. " + e.getMessage());
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
    public static void saveTransformedDom(Document doc, URI theUri, URI transUri,
            HashMap<String, String> parameters)
            throws Exception {
        if (doc == null) {
            throw (new Exception("No_dom_to_save"));
        }
        try {
            TransformerFactory transFac = TransformerFactory.newInstance();

            //DocumentBuilderFactory dFactory=DocumentBuilderFactory.newInstance();
            // must be set true for transformations
            //dFactory.setNamespaceAware(true); 
            //DocumentBuilder docBuilder=dFactory.newDocumentBuilder();
            DocumentBuilder docBuilder = makeDocBuilder(true);
            Document xslDoc = docBuilder.parse(transUri.toString());
            DOMSource xslDomSource = new DOMSource(xslDoc);
            xslDomSource.setSystemId(theUri.toString());// ?? transUri
            DOMSource xmlDomSource = new DOMSource(doc);
            Transformer trans = transFac.newTransformer(xslDomSource);

            // all transformation properties are assumed set by transformation author
            // so no: trans.setOutputProperty(OutputKeys.xxx,"");
            // it would be possible to overrun properties by global options ?

            // deal with parameters. Simply set them as is
            if (parameters != null) {
                for (Iterator<String> it = parameters.keySet().iterator(); it.hasNext();) {
                    String key = it.next();
                    trans.setParameter(key, parameters.get(key));
                }
            }


            // set outputstreamwriter
            // make sure file exists
            if (!accessutils.makeCatalog(theUri)) {
                throw new Exception("Cant make file");
            }

            java.io.FileOutputStream out = new java.io.FileOutputStream(theUri.getPath());
            java.io.OutputStreamWriter os = new java.io.OutputStreamWriter(out, trans.getOutputProperty(OutputKeys.ENCODING));
            java.io.BufferedWriter bos = new java.io.BufferedWriter(os);
            StreamResult outStream = new StreamResult(bos);

            // do it            
            trans.transform(xmlDomSource, outStream);
        } catch (java.io.UnsupportedEncodingException ex) {
            System.out.println("domer:saveTransformedDom: " + ex.getMessage());
            throw new Exception(ex.getMessage());
        } catch (TransformerConfigurationException tce) {
            System.out.println("domer:saveTransformedDom: " + tce.getMessage());
            throw new Exception(tce.getMessage());
        } catch (TransformerException te) {
            System.out.println("domer:saveTransformedDom: " + te.getMessage());
            throw new Exception(te.getMessage());
        } catch (FactoryConfigurationError f) {
            System.out.println("domer:saveTransformedDom: " + f.getMessage());
            throw new Exception(f.getMessage());
        } catch (IOException ioe) {
            System.out.println("domer:saveTransformedDom: " + ioe.getMessage());
            throw new Exception(ioe.getMessage());
        } catch (SAXException saxe) {
            System.out.println("domer:saveTransformedDom: " + saxe.getMessage());
            throw new Exception(saxe.getMessage());
        } catch (IllegalArgumentException iae) {
            System.out.println("domer:saveTransformedDom: " + iae.getMessage());
            throw new Exception(iae.getMessage());
        } catch (Exception e) {
            System.out.println("domer:saveTransformedDom: " + e.getMessage());
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
    public static String saveTransformedDomToString(Document doc, URI theUri, URI transUri,
            HashMap<String, String> parameters)
            throws Exception {
        if (doc == null) {
            throw (new Exception("No_dom_to_save"));
        }
        try {
            TransformerFactory transFac = TransformerFactory.newInstance();

            //DocumentBuilderFactory dFactory=DocumentBuilderFactory.newInstance();
            // must be set true for transformations
            //dFactory.setNamespaceAware(true); 
            //DocumentBuilder docBuilder=dFactory.newDocumentBuilder();

            DocumentBuilder docBuilder = makeDocBuilder(true);
            Document xslDoc = docBuilder.parse(transUri.toString());

            DOMSource xslDomSource = new DOMSource(xslDoc);
            // where urls will be resolved. Nomeaning if we dont need
            // to access other files during transformation
            xslDomSource.setSystemId(theUri.toString());// ?? transUri | anything
            DOMSource xmlDomSource = new DOMSource(doc);
            Transformer trans = transFac.newTransformer(xslDomSource);

            // all transformation properties are assumed set by transformation author
            // so no: trans.setOutputProperty(OutputKeys.xxx,"");
            // it would be possible to overrun properties by global options ?

            // deal with parameters. Simply set them as is
            if (parameters != null) {
                for (Iterator<String> it = parameters.keySet().iterator(); it.hasNext();) {
                    String key = it.next();
                    trans.setParameter(key, parameters.get(key));
                }
            }

            java.io.StringWriter out = new java.io.StringWriter();
            java.io.BufferedWriter bos = new java.io.BufferedWriter(out);
            StreamResult outStream = new StreamResult(bos);
            trans.transform(xmlDomSource, outStream);
            bos.flush();
            // while testing
            String tst = out.toString().toString();

            return out.toString().toString();

        } catch (java.io.UnsupportedEncodingException ex) {
            System.out.println("domer:saveTransformedDom: " + ex.getMessage());
            throw new Exception(ex.getMessage());
        } catch (TransformerConfigurationException tce) {
            System.out.println("domer:saveTransformedDom: " + tce.getMessage());
            throw new Exception(tce.getMessage());
        } catch (TransformerException te) {
            System.out.println("domer:saveTransformedDom: " + te.getMessage());
            throw new Exception(te.getMessage());
        } catch (FactoryConfigurationError f) {
            System.out.println("domer:saveTransformedDom: " + f.getMessage());
            throw new Exception(f.getMessage());
        } catch (IOException ioe) {
            System.out.println("domer:saveTransformedDom: " + ioe.getMessage());
            throw new Exception(ioe.getMessage());
        } catch (SAXException saxe) {
            System.out.println("domer:saveTransformedDom: " + saxe.getMessage());
            throw new Exception(saxe.getMessage());
        } catch (IllegalArgumentException iae) {
            System.out.println("domer:saveTransformedDom: " + iae.getMessage());
            throw new Exception(iae.getMessage());
        } catch (Exception e) {
            System.out.println("domer:saveTransformedDom: " + e.getMessage());
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
    public static List<Node> getPIs(Node n, String target) {
        // If there are any children, visit each one
        List<Node> result = new ArrayList<Node>(10);
        Node nod = n.getFirstChild();
        while (nod != null) {
            if ((nod.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE)
                    && (nod.getNodeName().compareToIgnoreCase(target) == 0)) {
                result.add(nod);
            } else {
                result.addAll(getPIs(nod, target));
            }
            nod = nod.getNextSibling();
        }
        return result;
    }

    /**
     * Find a list of all Processing Instructions below a node in a tree.
     * 
     * @param n The node defining the subtree
     * @return a list of PI-nodes
     */
    public static List<Node> getPIs(Node n) {
        // If there are any children, visit each one
        List<Node> result = new ArrayList<Node>(10);
        Node nod = n.getFirstChild();
        while (nod != null) {
            if (nod.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
                result.add(nod);
            } else {
                result.addAll(getPIs(nod));
            }
            nod = nod.getNextSibling();
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
    public static NodeList performXPathQuery(Document doc, String xp)
            throws Exception {
        try {
            makeXPath();
            XPathExpression expr = m_xpath.compile(xp);
            return (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        } catch (Exception ex) {
            throw new Exception(reporter.getBundleString("Could_not_evaluate_xpath", xp));
        }
    }

    /**
     * Query a DocumentFragment for a images.
     *
     * @param df The DocumentFragment we will query
     * @return a list of nodes
     * @throws java.lang.Exception
     */
    public static NodeList findAllImages(DocumentFragment df)
            throws Exception {
        // troubblewith expath in some dfs
        // do it the hard way:
        try {
            String domstring = "<?xml version=\"1.0\" encoding=\"utf-8\"?><root></root>";
            Document doc = makeDomFromString(domstring);
            doc.getDocumentElement().appendChild(doc.importNode(df, true));
            return doc.getElementsByTagName("img");
        } catch (Exception ex) {
            throw new Exception(reporter.getBundleString("Could_not_extract_images", ex.getMessage()));
        }
    }

    /**
     * Get an element and all its children which are elements.
     * 
     * @param elt The root
     * @return a list of elements
     */
    public static List<Element> getElementsPreorder(Element elt) {
        List<Element> res = new ArrayList<Element>();
        res.add(elt);
        Node nod = elt.getFirstChild();
        while (nod != null) {
            if (nod.getNodeType() == Node.ELEMENT_NODE) {
                res.addAll(getElementsPreorder((Element) nod));
            }
            nod = nod.getNextSibling();
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
    public static DocumentFragment produceDocFragmentFromString(String theText, String enc, reporter theReporter)
            throws Exception {
        if (theText == null) {
            return null;
        }
        if (enc == null) {
            enc = default_encoding;
        }
        theText = theText.trim();
        if (!theText.startsWith("<?xml")) {
            theText = "<?xml version=\"1.0\" encoding=\"" + enc + "\"?><root>" + theText + "</root>";
            //?? does not seem to help
            byte[] data = theText.getBytes(enc);
            theText = new String(data, enc);
        }
        try {
            Document doc = domer.makeDomFromStringSomeHow(theText, enc, theReporter);
            //Document doc=domer.makeDomFromString(theText);
            doc.normalize();
            DocumentFragment df = doc.createDocumentFragment();
            NodeList nlist = doc.getDocumentElement().getChildNodes();
            //int L=nlist.getLength();
            for (int ix = 0; ix < nlist.getLength(); ix++) {
                String tmp = nlist.item(ix).getNodeName();
                String tmpval = nlist.item(ix).getNodeValue();
                df.appendChild(nlist.item(ix).cloneNode(true));
            }

            //NodeList tmplist=df.getChildNodes();
            //int tmpL=tmplist.getLength();

            return df;
        } catch (DOMException e) {
            throw (new Exception(e.getMessage()));
        } catch (Exception e) {
            throw (new Exception(e.getMessage()));
        }
    }

    /**
     * Validate a Document against a schema
     * @return OK if ok , errormessage otherwise
     * @param errors An errorhandler
     * @param document The document we want to validate
     * @param fileString The path to the schema we want to validate against
     */
    public static String validateDom(Document document, String fileString, validationErrorHandler errors) {

        SchemaFactory factory =
                SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema;


        if (!new File(fileString).exists()) {
            return reporter.getBundleString("Cannot_locate_schema", fileString);
        }
        try {
            // make a file URI
            //fileString=fileString.replace(" ", "%20");
            //String uriString="file:///"+fileString.replace('\\','/');
            //URI theSchema=new URI(uriString);
            URI theSchema = accessutils.makeUri("file:///" + fileString);

            schema = factory.newSchema(theSchema.toURL());
            Validator validator = schema.newValidator();
            validator.setErrorHandler(errors);


            validator.validate(new DOMSource(document));
            return "OK";

        } catch (SAXException ex) {
            return ex.getMessage();

        } catch (IOException iex) {
            return iex.getMessage();
        } catch (Exception uex) {
            return uex.getMessage();
        }
    }

    /**
     * Validate a Document against a schema
     * @return null if ok , errormessage otherwise
     * @param errors An errorhandler
     * @param document The document we want to validate
     * @param schemaAsString The schema we want to validate against
     */
    public static String validateDomFromString(Document document, String schemaAsString, validationErrorHandler errors) {

        SchemaFactory factory =
                SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema;


        try {
            Source s = new StreamSource(new StringReader(schemaAsString));
            schema = factory.newSchema(s);

            Validator validator = schema.newValidator();
            validator.setErrorHandler(errors);

            validator.validate(new DOMSource(document));
            return null;

        } catch (SAXException ex) {
            return ex.getMessage();
        } catch (IOException iex) {
            return iex.getMessage();
        } catch (Exception sex) {
            return sex.toString();
        }
    }
}

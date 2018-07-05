package xmldom;


import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import wxttesting.testHTML;


/**
 * This class implements a static library for handling DOM-actions.
 * <p>
 * Building Document from String etc.
 * @author bs
 */
public class domer extends java.lang.Object {

    static XPath m_xpath;
    static private String default_encoding = "UTF-8";



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
    public static Document makeDomFromStringSomeHow(String S, String encoding)
            throws Exception {
        if (encoding == null) {
            encoding = default_encoding;
        }
        // add header if necessary
        S = S.trim();
        /*if (!S.startsWith("<?xml")) {
            S = "<?xml version=\"1.0\" encoding=\"" + encoding + "\" ?>\n" + S;
        }*/
        try {
            return makeDomFromString(S);
        } catch (IOException iox) {
            // nothing more we can do
            throw iox;
        } catch (Exception ex) {
                try {
                    System.out.println("tidy attempt because: "+ex.getMessage());
                    S = testHTML.tidyAndMakeXML(S, encoding);
                    return makeDomFromString(S);
                } catch (Exception sex3) {
                    // nothing more we can do
                    throw sex3;
                }

        }
    }
public static void printDocument(Document doc, OutputStream out) 
        throws IOException, TransformerException {
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer transformer = tf.newTransformer();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
    transformer.setOutputProperty(OutputKeys.METHOD, "html");
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,"yes");
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,"HTML");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

    transformer.transform(new DOMSource(doc), 
         new StreamResult(new OutputStreamWriter(out, "UTF-8")));
}
}

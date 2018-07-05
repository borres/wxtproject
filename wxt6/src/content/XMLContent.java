package content;

import java.net.URI;
import java.util.HashMap;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.NodeList;
import reporting.wxtError;
import utils.PIcommand;
import utils.accessutils;
import xmldom.domer;

/**
 * This class stores a content object which will be handled as a XML-document.
 * <p>
 * If it has a transformation, this transformation will be applied before
 * contents are extracted.
 */
public class XMLContent extends Content {

    /** the dom without any transformation*/
    Document m_theDoc = null;

    /**
     * Constructing a XMLContent object from a command
     *
     * @param cmd The command that describes the neccessary options
     * @param owner The Module that owns this content
     * @throws exception if we fail
     */
    public XMLContent(Module owner, PIcommand cmd)
            throws Exception {
        super(owner, cmd);
        // and we attempt to establish the untransformed DOM
        try {
            m_theDoc = domer.makeDomFromUri(m_absoluteLoadUri,false, m_encoding);
        } catch (Exception ex) {
            throw ex;
        }

    }

    /**
     * Get the content with actual xpath, encoding and transformation
     * If no transformatoin, we reuse DOM
     * @param mod The module
     * @param cmd The command
     * @return A documentfragment
     */
    @Override
    public DocumentFragment getContent(Module mod, PIcommand cmd) {
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        // xpath is mandatory
        if (!cmd.paramExist(PIcommand.XPATH)) {
            // should not happen
            wxtError.makeErrorMsgForDebugging(mod, cmd, "Missing XPATH in XMLContent:getContent");
            mod.getReporter().pushMessage("missing_xpath_in_command", cmd.toString());
            return Content.getFailedContent(mod, cmd);
        }
        String a_xpath = cmd.getValue(PIcommand.XPATH);

        // any change in encoding
        String a_encoding = m_encoding;
        if (cmd.paramExist(PIcommand.ENCODING)) {
            a_encoding = cmd.getValue(PIcommand.ENCODING);
        }

        // We have established the DOM for a non-transformed version in the constructor
        // If we do not have a transformation we go for it directly
        // We ignore any transformation set in constructor
        if (!cmd.paramExist(PIcommand.TRANSFORMATION)) {
            try {
                NodeList nlist = domer.performXPathQuery(m_theDoc, a_xpath);
                for (int ix = 0; ix < nlist.getLength(); ix++) {
                    df.appendChild(mod.getDoc().importNode(nlist.item(ix), true));
                }
                return df;
            } catch (Exception xpex) {
                mod.getReporter().pushMessage("could_not_do_xml", m_absoluteLoadUri.toString(), xpex.getMessage());
                return Content.getFailedContent(mod, cmd);
            }
        }

        //----------------------------
        // we have to deal with a transformation
        String tmp = cmd.getValue(PIcommand.TRANSFORMATION).trim();
        tmp = mod.getDefinitions().substituteFragments(tmp);
        Transformation a_transformation = Transformation.findTransformation(tmp, mod);
        HashMap<String, String> a_transParameters = null;
        try {
            a_transParameters = accessutils.unpackTransformationParameters(tmp);
        } catch (Exception e) {
            m_Owner.getReporter().pushMessage("bad_transformation", tmp);
            return Content.getFailedContent(mod, cmd);
        }

        try {
            Document doc = domer.makeTransformedDomFromUri(m_absoluteLoadUri,false,
                    a_transformation.getabsoluteURI(),
                    a_transParameters,
                    a_encoding);
            // debugging:
            // String domT=domer.saveDomToString(doc,"utf-8", true, "xml");

            NodeList nlist = domer.performXPathQuery(doc, a_xpath);

            for (int ix = 0; ix < nlist.getLength(); ix++) {
                df.appendChild(mod.getDoc().importNode(nlist.item(ix), true));
            }
            return df;
        } catch (Exception xpex) {
            mod.getReporter().pushMessage("could_not_do_xml", m_absoluteLoadUri.toString(), xpex.getMessage());
            return Content.getFailedContent(mod, cmd);
        }

    }

    @Override
    public String toString() {
        return "\n\tXMLContent at " + m_absoluteLoadUri.toString();
    }

    //--------------------------------------------------------------
    // the material below is static methods that are all selfcontained
    // and are not dependant of instances of XMLContent.
    /**
     * Establish and extract content according to parameters in PIcommand.
     * <p>
     * The method is completely selfcontained and does not rely on any fields in the object.
     * It is used to effectuate importxml PIs which are not related to content-elements in the script.
     * This method is used from class producer
     * @param mod The module that has requested this content
     * @param cmd The PIcommand that describes what we want
     * @return  A DocumentFragment
     */
    static public DocumentFragment getStaticContent(Module mod, PIcommand cmd) {

        // first we must pick up all possible parameters from cmd
        // those parameters are: 
        // uri, transformation, xpath
        // uri is mandatory
        // Transformation in this context is interpreted such:
        // The uri is supposed to contain XML, and the transformation is applied first.
        // The result of the transformation is assumed to be XML.
        // xpath is applied to this result

        DocumentFragment df = mod.getDoc().createDocumentFragment();

        URI absoluteUri = null;
        Transformation trans = null;
        HashMap<String, String> transParameters = null;
        String xpath = null;
        String source_encoding = null;


        // what do we have ?
        // LOCATION
        if (!cmd.paramExist(PIcommand.LOCATION)) {
            mod.getReporter().pushMessage("bad_processing_instruction", cmd.toString());
            return Content.getFailedContent(mod, cmd);
        }
        String tmp = cmd.getValue(PIcommand.LOCATION);
        try {
            absoluteUri = accessutils.makeAbsoluteURI(tmp, mod.getCatalog());
        } catch (Exception ex) {
            mod.getReporter().pushMessage("bad_processing_instruction", cmd.toString());
            return Content.getFailedContent(mod, cmd);
        }

        // do dependency
        mod.addDependency(absoluteUri);

        // transformation
        // TODO implement load and use of transformation
        // or demand predefined transformation
        if (cmd.paramExist(PIcommand.TRANSFORMATION)) {
            String t = cmd.getValue(PIcommand.TRANSFORMATION);
            String transName = t;
            int parpos = t.indexOf('(');
            if (parpos != -1) {
                // we have parameters
                transName = t.substring(0, parpos);
                try {
                    transParameters = accessutils.unpackTransformationParameters(t);
                } catch (Exception e) {
                    mod.getReporter().pushMessage("bad_processing_instruction", cmd.toString());
                    return Content.getFailedContent(mod, cmd);
                }
            }
            trans = mod.getDefinitions().getTransformation(transName);
            if (trans == null) {
                mod.getReporter().pushMessage("transformation_not_defined", cmd.toString());
                return Content.getFailedContent(mod, cmd);
            } else {
                // do dependency
                mod.addDependency(trans.getabsoluteURI());
            }
        }


        // xpath
        xpath = "child::*";
        if (cmd.paramExist(PIcommand.XPATH)) {
            xpath = cmd.getValue(PIcommand.XPATH);
        }

        // do it
        // source_encoding is not set
        if (cmd.paramExist(PIcommand.ENCODING)) {
            source_encoding = cmd.getValue(PIcommand.ENCODING);
        }
        Document doc = null;
        try {
            if (trans != null) {
                doc = domer.makeTransformedDomFromUri(absoluteUri,false, trans.getabsoluteURI(), transParameters,
                        source_encoding);
            } else {
                doc = domer.makeDomFromUri(absoluteUri, false,source_encoding);
            }
        } catch (Exception lex) {
            mod.getReporter().pushMessage("could_not_do_xml", absoluteUri.toString(), lex.getMessage());
            return Content.getFailedContent(mod, cmd);
        }

        try {
            NodeList nlist = domer.performXPathQuery(doc, xpath);

            for (int ix = 0; ix < nlist.getLength(); ix++) {
                df.appendChild(mod.getDoc().importNode(nlist.item(ix), true));
            }
            return df;
        } catch (Exception xpex) {
            mod.getReporter().pushMessage("could_not_do_xml", absoluteUri.toString(), xpex.getMessage());
            return Content.getFailedContent(mod, cmd);
        }
    }
}

package content;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import utils.PIcommand;
import utils.accessutils;
import programcode.CodeBasics;
import wxt.Options;
import xmldom.domer;

/**
 * This class stores a content object which will be  handled as text
 * <p>
 * If it has a transformation, this transformation will be applied before
 * contents are extracted.
 */
public class TXTContent extends Content {

    /** the raw unhandled and untransformed text*/
    protected String m_RawText;

    /**
     * Constructing a TXTContent fram a cmd
     * @param owner The module that owns the content
     * @param cmd Describing the request
     * @throws java.lang.Exception When we dont have an effective source
     */
    public TXTContent(Module owner, PIcommand cmd)
            throws Exception {
        super(owner, cmd);

        try {
            m_RawText = accessutils.getBOMSafeTextFile(m_absoluteLoadUri, m_encoding);
        } catch (Exception ex) {
            m_Owner.getReporter().pushSimpleMessage("\t" + ex.getMessage());
            throw new Exception();
        }
    }

    /**
     * Get the content prepared according to cmd
     * @param mod
     * @param cmd
     * @return A document fragment which may be without children
     */
    @Override
    public DocumentFragment getContent(Module mod, PIcommand cmd) {
        DocumentFragment df = mod.getDoc().createDocumentFragment();

        // start out assumption that this is a strait import
        String a_RawText = m_RawText;
        String a_encoding = m_encoding;

        // we make sure that we have a correctly encoded rawtext from normal source
        if (cmd.paramExist(PIcommand.ENCODING)
                && (cmd.getValue(PIcommand.ENCODING).compareTo(m_encoding) != 0)) {
            a_encoding = cmd.getValue(PIcommand.ENCODING);
            try {
                a_RawText = accessutils.getBOMSafeTextFile(m_absoluteLoadUri, a_encoding);
            } catch (Exception ex) {
                m_Owner.getReporter().pushSimpleMessage("\t" + ex.getMessage());
                return df;
            }
        }

        //-----------------------
        // handle possible transformation
        if (cmd.paramExist(PIcommand.TRANSFORMATION)) {
            String tmp = cmd.getValue(PIcommand.TRANSFORMATION).trim();
            tmp = mod.getDefinitions().substituteFragments(tmp);
            Transformation a_transformation = Transformation.findTransformation(tmp, mod);
            String a_transformationString = tmp;
            HashMap<String, String> a_transParameters = null;
            try {
                a_transParameters = accessutils.unpackTransformationParameters(tmp);
            } catch (Exception e) {
                m_Owner.getReporter().pushMessage("bad_transformation", tmp);
                return Content.getFailedContent(mod, cmd);
            }
            try {
                a_RawText = domer.makeTransformedStringFromString(a_RawText,
                        a_transformation.getabsoluteURI(),
                        a_transParameters,a_encoding);
            } catch (Exception ex) {
                m_Owner.getReporter().pushMessage("bad_transformation", tmp);
                return Content.getFailedContent(mod, cmd);
            }
        }
        if ((a_RawText == null) || (a_RawText.isEmpty())) {
            return df;
        }

        // selection colorcoding etc...
        String result = prepareTheText(a_RawText, mod, cmd);

        if (cmd.paramExist(PIcommand.LANG)) {
            // prepare it as if quoted directly in text
            Element preElt = mod.getDoc().createElement("pre");
            String ctxt = CodeBasics.PRETTYPRINT + " " + cmd.getValue(PIcommand.LANG);
            preElt.setAttribute("class", ctxt);
            preElt.setAttribute("id", mod.getScriptHandler().getANewId(mod.getID()));
            result = CodeBasics.cleanString(result);
            //result=codeGoogleFormatter.cleanCode(result);
            preElt.appendChild(mod.getDoc().createTextNode(result));
            df.appendChild(preElt);
            return df;
        }
        //--------------------------
        // we have the text prepared according to cmd
        // we may want to wrap it so it is treated as html-code
        if (cmd.paramExist(PIcommand.PARSE)) {
            try {
                DocumentFragment tmpdf = domer.produceDocFragmentFromString(result, mod.getEncoding(), mod.getReporter());
                df = (DocumentFragment) mod.getDoc().importNode(tmpdf, true);
            } catch (Exception domex) {
                mod.getReporter().pushMessage("pi_produced_no_result", cmd.toString());
                return Content.getFailedContent(mod, cmd);
            }
        } else {
            df.appendChild(mod.getDoc().createTextNode(result));
        }

        return df;

    }

    /**
     * Helper to prepare text according to specs given in parameter cmd
     *
     * @param loadedText The rwa text we should work on
     * @param mod The module that have requested this stuff
     * @param cmd The PIcommand that describes the request
     * @return The found text
     */
    static protected String prepareTheText(String loadedText, Module mod, PIcommand cmd) {
        String leftpar = null;
        String rightpar = null;
        String select;
        String target_encode;
        HashMap<String, String> replaces;

        //--------------
        // parenthesis
        if (cmd.paramExist(PIcommand.LEFTPAR)) {
            leftpar = cmd.getValue(PIcommand.LEFTPAR);
        }
        if (cmd.paramExist(PIcommand.RIGHTPAR)) {
            rightpar = cmd.getValue(PIcommand.RIGHTPAR);
        }

        //-----------------
        // selection
        select = PIcommand.ALL;
        if (cmd.paramExist(PIcommand.SELECT)) {
            select = cmd.getValue(PIcommand.SELECT);
        }

        //-------------
        // replaces
        // we allow multiple replaces: replaces or replace0, replace1, replace2 etc
        // assume replaces="out | in"
        // store them in a hashmap
        String splitRegex = "\\Q" + PIcommand.REPLACESPLITTER + "\\E";
        replaces = new HashMap<>();
        if ((cmd.paramExist(PIcommand.REPLACE)) || (cmd.paramExist(PIcommand.REPLACE0))) {
            String rplc = PIcommand.REPLACE;
            String tmp = cmd.getValue(rplc);
            if (tmp != null) {
                String[] parts = tmp.split(splitRegex);
                if (parts.length == 2) {
                    replaces.put(parts[0], parts[1]);
                } else {
                    mod.getReporter().pushMessage("bad_replace_in_pi", cmd.toString());
                }
            }
            boolean more = true;
            int ix = 0;
            while (more) {
                tmp = PIcommand.REPLACE + ix;
                if (cmd.paramExist(tmp)) {
                    String[] parts = cmd.getValue(tmp).split(splitRegex);
                    if (parts.length == 2) {
                        replaces.put(parts[0], parts[1]);
                    } else {
                        mod.getReporter().pushMessage("bad_replace_in_pi", cmd.toString());
                    }
                } else if (ix > 1) // allow som slack in PI-param
                {
                    more = false;
                }
                ix++;
            }
        }

        // ------------------------
        // we have what we need
        //-------------------------
        // we pick up contents in parenthesis, lefpar, rightpar
        List<String> parts = new ArrayList<String>();
        if ((leftpar != null) || (rightpar != null)) {
            if (leftpar == null) {
                int pos = loadedText.indexOf(rightpar);
                if (pos != -1) {
                    parts.add(loadedText.substring(0, pos));
                }
            } else if (rightpar == null) {
                int pos = loadedText.indexOf(leftpar);
                if (pos != -1) {
                    parts.add(loadedText.substring(pos + leftpar.length()));
                }

            } else {
                // both are present, maybe manty times

                int leftpos = loadedText.indexOf(leftpar);
                while (leftpos != -1) {
                    loadedText = loadedText.substring(leftpos + leftpar.length());
                    int rightpos = loadedText.indexOf(rightpar);
                    if (rightpos != -1) {
                        parts.add(loadedText.substring(0, rightpos));
                    }
                    loadedText = loadedText.substring(rightpos + rightpar.length());
                    leftpos = loadedText.indexOf(leftpar);
                }
            }
        } else {
            parts.add(loadedText);
        }

        // which of the parts do we want
        String result = "";
        if (parts.size() > 0) {
            if (parts.size() == 1) {
                // we take the one we have found
                // dont bother about select, this may be a bad solution ????
                result = parts.get(0);
            } else {
                if (select.compareToIgnoreCase(PIcommand.ALL) == 0) {
                    // take them all
                    for (String s : parts) {
                        result += s;
                    }
                } else if (select.compareToIgnoreCase(PIcommand.RANDOM) == 0) {
                    int pos = new Random().nextInt(parts.size());
                    result = parts.get(pos);
                } else {
                    // there are one or more we are interested in
                    // assume a commaseparated list of integers
                    // or a pythonlike slice
                    List<Integer> list = accessutils.getIntegerList(select, parts.size());
                    if (list == null) {
                        mod.getReporter().pushMessage("cannot_select_with_selector", select);
                    } else {
                        for (int ix : list) {
                            ix = ix - 1;
                            if ((ix > -1) && (ix < parts.size())) {
                                result += parts.get(ix);
                            }
                        }
                    }
                }

            }
        }

        if (result.isEmpty()) {
            return result;
        }

        // we do all the replaces
        Set<String> outs = replaces.keySet();
        for (String out : outs) {
            String tmpreg = "\\Q" + out + "\\E";
            result = result.replaceAll(tmpreg, replaces.get(out));
        }

        return result;
    }

    @Override
    public String toString() {
        return "\n\tTXTContent at " + m_absoluteUri.toString();
    }

    /**
     * Helper to do textaccess. If we parse or do colorcoding
     * the returned text contains elements
     * 
     * @param mod The module that have requested this stuff
     * @param cmd The PIcommand that describes the request
     * @throws exception If we fail
     * @return The found text
     */
    static public String getFormattedText(Module mod, PIcommand cmd)
            throws Exception {
        // to do this job we need the following parameters form cmd:
        // transformation, location, leftpar, rightpar, select, replaces, source_encode
        URI absoluteSourceUri = null;
        Transformation trans = null;
        HashMap<String, String> transParameters = null;

        String source_encode = null;
        String target_encode = null;
        HashMap<String, String> replaces = null;

        // let us see what we have
        // we may have location (LOCATION) and sourcelocation (SOURCELOCATION)
        // we will read text from sourcelocation if it exists,
        // prepare it and write it to location
        // location is the one mentioned in popup and expand 

        String sourceUriStr = null;
        String targetUriStr = null;

        // encoding
        source_encode = mod.getScriptHandler().getOption(Options.DEFAULT_ENCODING);
        //source_encode=mod.getEncoding();
        if (cmd.paramExist(PIcommand.ENCODING)) {
            source_encode = cmd.getValue(PIcommand.ENCODING);
        }
        target_encode = mod.getEncoding();


        // if sourcelocation exists this  will be read
        // otherwise location is considered as source
        if (cmd.paramExist(PIcommand.SOURCELOCATION)) {
            sourceUriStr = cmd.getValue(PIcommand.SOURCELOCATION);
            if (cmd.paramExist(PIcommand.LOCATION)) {
                targetUriStr = cmd.getValue(PIcommand.LOCATION);
            }
        } else if (cmd.paramExist(PIcommand.LOCATION)) {
            sourceUriStr = cmd.getValue(PIcommand.LOCATION);
            targetUriStr = sourceUriStr;
        }

        if (sourceUriStr != null) {
            sourceUriStr = mod.getDefinitions().substituteFragments(sourceUriStr);
        }
        if (targetUriStr != null) {
            targetUriStr = mod.getDefinitions().substituteFragments(targetUriStr);
        }

        // We must have a location to load from
        if (sourceUriStr == null) {
            mod.getReporter().pushMessage("bad_processing_instruction", cmd.toString());
            throw new Exception();
        }
        try {
            absoluteSourceUri = accessutils.makeAbsoluteURI(sourceUriStr, mod.getCatalog());
            // do dependency
            mod.addDependency(absoluteSourceUri);
        } catch (Exception ex) {
            mod.getReporter().pushMessage("bad_processing_instruction", cmd.toString());
            throw new Exception();
        }

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
                    return "-";
                }
            }
            trans = mod.getDefinitions().getTransformation(transName);
            if (trans == null) {
                mod.getReporter().pushMessage("transformation_not_defined", cmd.toString());
                return "-";
            }
        }

        // ------------------------
        // we have what we need
        // attempt to load the text
        String T;
        try {
            //T=accessutils.getTextFile(absoluteSourceUri,source_encode);
            T = accessutils.getBOMSafeTextFile(absoluteSourceUri, source_encode);

        } catch (Exception e) {
            //mod.getReporter().pushMessage("cannot_load_text", absoluteSourceUri.toString());
            //throw new Exception(e.getMessage());
            mod.getReporter().pushSimpleMessage("\t" + e.getMessage());
            throw new Exception();
        }

        // we transform
        if (trans != null) {
            T = domer.makeTransformedStringFromString(T, trans.getabsoluteURI(), transParameters,target_encode);
        }

        String result = prepareTheText(T, mod, cmd);


        // possible parse is not done here, 
        // it will be done by the receiver of this string

        return result;
    }
}

package Formulas;

import content.Definitions;
import content.Module;
import java.io.File;
import java.net.URI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import utils.PIcommand;
import utils.accessutils;
import utils.domer;
import reporting.reporter;
import reporting.validationErrorHandler;
import wxt4.Scripthandler;
import wxtresources.resourceHandler;

/**
 * statics establishing formulas in definitions holder
 * 
 * @author Administrator
 */
public class FormulaUtils {

    /**
     * Adding formulas from an texformulas file
     * @param theReporter Where to report
     * @param absUri Location of the file
     * @param shandler The Scripthandler involved
     * @param def The Definitions element involved
     * @throws java.lang.Exception when we miss
     */
    public static void addFormulasFromFormulaFile(reporter theReporter, URI absUri,
            Scripthandler shandler,
            Definitions def)
            throws Exception {
        Document contentDoc = null;

        try {
            contentDoc = domer.makeDomFromUriSomeHow(absUri, null, theReporter);

            // validation the file
            String schemaString = resourceHandler.getFormulaSchema();
            validationErrorHandler eHandler = new validationErrorHandler(true);
            String result = domer.validateDomFromString(contentDoc, schemaString, eHandler);
            String rep = eHandler.getReport();
            if (result != null) {
                theReporter.pushSimpleMessage("Formulas validation failed:" + absUri.toString() + "\n");
            } else {
                theReporter.pushSimpleMessage("Formulas validation:" + absUri.toString() + "\n" + rep + "\n");
            }
        } catch (Exception ex1) {
            // cannot find or parse content.xml
            // System.out.println(ex1.getMessage());
            theReporter.pushMessage("cannot_inspect", absUri.getPath());
            throw new Exception("formulas not loaded");
        }

        // correct for possible catalog attribute
        if (contentDoc.getDocumentElement().hasAttribute("catalog")) {
            String srcCatStr = contentDoc.getDocumentElement().getAttribute("catalog");
            srcCatStr = def.substituteFragments(srcCatStr);
            //srcCatStr=srcCatStr.replace('\\', '/');
            //srcCatStr=srcCatStr.replaceAll(" ", "%20");
            // we must try to make sourceUri from the scrCatStr
            try {
                absUri = accessutils.makeAbsoluteURI(srcCatStr, absUri.getPath());
            } catch (Exception lex) {
                // anything we can do about this ?
                throw lex;
            }

        }

        NodeList flist = contentDoc.getElementsByTagName("formula");
        for (int ix = 0; ix < flist.getLength(); ix++) {
            // what type of formula: latex or image or mathml
            Element elt = (Element) flist.item(ix);

            String type = elt.getAttribute(PIcommand.TYPE);
            try {
                if (type.compareTo(PIcommand.LATEX) == 0) {
                    LaTexFormula laf = new LaTexFormula(def, elt);
                    def.registerNewFormula(laf);
                } else if (type.compareTo(PIcommand.GOOGLE) == 0) {
                    GoogleTexFormula gf = new GoogleTexFormula(def, elt);
                    def.registerNewFormula(gf);
                } else if (type.compareTo(PIcommand.IMAGE) == 0) {
                    ImageFormula imf = new ImageFormula(elt, absUri);
                    def.registerNewFormula(imf);
                } else if (type.compareTo(PIcommand.MATHML) == 0) {
                    MathMLFormula mf = new MathMLFormula(def, elt);
                    def.registerNewFormula(mf);
                } else {
                    // should not happen
                    theReporter.pushSimpleMessage("Unknown formulatype." + type);
                    continue;
                }
            } catch (Exception ex) {
                theReporter.pushSimpleMessage(ex.getMessage());
                continue;
            }
        }
    }

    /**
     * produce from a PI with source attribute
     * @param source The formula
     * @param mod The module requesting the formula
     * @param cmd The request
     * @return The produced mathml or latex formula
     * @throws Exception When we cannot produce
     */
    public static Formula makeFormulaFromSource(String source, Module mod, PIcommand cmd)
            throws Exception {
        Formula fma = null;
        if (source.indexOf("<math") != -1) {
            // this must be mathml
            String id = cmd.getValue(PIcommand.ID);

            String subtext = id;
            if (cmd.paramExist(PIcommand.SUBTEXT)) {
                subtext = cmd.getValue(PIcommand.SUBTEXT);
            }
            fma = new MathMLFormula(mod.getDefinitions(), id, source, subtext, true);
            return fma;
        }
        // this must be latex
        cmd.setParameter(PIcommand.LATEX, source);
        fma = new LaTexFormula(mod, cmd);

        return fma;
    }

    /**
     * Extracting mathml-formulas from an odt-document
     * @param theReporter Where to report
     * @param theLoc The location of the ODT-file
     * @param tmpCat The temporary catalog to unpack in
     * @param def The active Definition object
     * @throws Exception When we fail to load
     */
    public static void makeMathMlFormulasFromOdt(reporter theReporter, URI theLoc, String tmpCat, Definitions def)
            throws Exception {
        // assume file-uri
        File f = new File(theLoc.getPath());
        //g_sourcecatalog=tmpCat;


        File outdir = accessutils.UnzipODT(theLoc, tmpCat);
        if (outdir == null) {
            theReporter.pushMessage("cannot_inspect", theLoc.getPath());
            throw new Exception("formulas not loaded");
        }

        // now we are ready to inspect and localize the formulas
        // we start by opening the content.xml file
        Document contentDoc = null;
        try {
            URI theUri = accessutils.makeUri("file:///" + tmpCat + "/" + "content.xml");
            //contentDoc=domer.makeDomFromUri(theUri);
            contentDoc = domer.makeDomFromUriSomeHow(theUri, null, theReporter);
        } catch (Exception ex1) {
            // cannot find or parse content.xml
            //System.out.println(ex1.getMessage());
            theReporter.pushMessage("cannot_inspect", theLoc.getPath());
            throw new Exception("formulas not loaded");
        }
        // start searching for formula ids and formulas
        // a formula id (text:p with content _fxxxxx)
        // will identify the first following text:p which contains a draw:object
        NodeList pList = contentDoc.getElementsByTagName("text:p");
        String fid = null;
        for (int ix = 0; ix < pList.getLength(); ix++) {
            Element pElt = (Element) pList.item(ix);
            String t = pElt.getTextContent().trim();
            if ((t != null) && (t.startsWith("_f"))) {
                // we note the fid and continue to look for the following formula
                fid = t;
                continue;
            }
            // do we have av draw:object in this text:p
            if (fid != null) {
                NodeList dList = pElt.getElementsByTagName("draw:object");
                if (dList.getLength() > 0) {
                    Element adElt = (Element) dList.item(0);
                    // does it have link to an object
                    if (adElt.hasAttribute("xlink:href")) {
                        // get the address attribute in this element
                        String ad = adElt.getAttribute("xlink:href");
                        if (ad.startsWith(".")) {
                            ad = ad.replace(".", tmpCat);
                        }

                        //String result=storeFormula(fid,ad,theReporter,def);
                        ad = ad + File.separator + "content.xml";
                        ad = ad.replace("\\", "/");

                        String annotation = "";
                        URI theUri = accessutils.makeUri("file:///" + ad);

                        // get the actual text
                        String mathS = accessutils.getTextFile(theUri, "utf-8");
                        if (mathS.indexOf("math:math xmlns:math") == -1) {
                            // not a formula
                            theReporter.pushSimpleMessage(fid + " identifies no formula");
                            continue;
                        }
                        try {
                            MathMLFormula mf = new MathMLFormula(def, fid, mathS, null, false);
                            def.registerNewFormula(mf);
                        } catch (Exception fex) {
                            theReporter.pushSimpleMessage(fex.getMessage());
                            continue;
                        }
                    }
                }
            }
        }
    }
}

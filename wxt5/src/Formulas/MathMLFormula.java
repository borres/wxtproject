package Formulas;

import content.Definitions;
import content.Module;
import converter.mathmlToImage;
import java.awt.Dimension;
import java.net.URI;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import utils.PIcommand;
import utils.accessutils;
import utils.domer;

/**
 * Describing a MathML formula
 * @author bs
 */
public class MathMLFormula extends Formula {


    String m_backupCatalog = null;

    /**
     * Construct a formula from an element in a formulas file
     * @param elt The element describing the formula
     * @throws Exception When we cannot do it
     */
    public MathMLFormula(Definitions def, Element elt)
            throws Exception {
        super(elt);

        try {
            m_formulaSource =
                    prepareSource(((Element) (elt.getElementsByTagName(PIcommand.VALUE).item(0))).getTextContent().trim());
        } catch (Exception ex) {
            throw new Exception("Formula need both id and value(MathML)");
        }
        m_madeOnTheFly = false;
        m_formulaType = PIcommand.MATHML;
        m_backupCatalog = def.getBuildCatalog() + "/" + Definitions.MATHMLBACKUP;
        convertToLocal(null);
        leaveMessage();
    }

    /**
     * Constructing from a PI
     * @param mod The module that request the formula
     * @param cmd The command that describes the request
     * @throws Exception when we cannot make a formula
     */
    public MathMLFormula(Module mod, PIcommand cmd)
            throws Exception {
        super(mod, cmd);
        m_madeOnTheFly = true;
        m_formulaType = PIcommand.MATHML;
        m_backupCatalog = mod.getDefinitions().getBuildCatalog() + "/" + Definitions.MATHMLBACKUP;
        if (cmd.paramExist(PIcommand.SOURCE)) {
            String tmp = cmd.getValue(PIcommand.SOURCE);
            tmp = tmp.replace("\r", "");
            tmp = tmp.replace("\n", "");
            m_formulaSource = prepareSource(tmp);
            convertToLocal(cmd);
            leaveMessage();
        } else {
            // location
            String location = cmd.getValue(PIcommand.LOCATION);
            URI abslocation = accessutils.makeAbsoluteURI(location, mod.getAbsoluteUri().toString());
            m_formulaSource = prepareSource(accessutils.getTextFile(abslocation, "utf-8"));
            convertToLocal(cmd);
            leaveMessage();
        }
    }

    /**
     * Construct from translation from odt-formula file
     * @param id
     * @param formula
     * @throws Exception
     */
    public MathMLFormula(Definitions def, String id, String formula, String subtext, boolean onthefly)
            throws Exception {
        if ((id == null) || (formula == null)) {
            throw new Exception("Formula need both id and value (mathml string)");
        }
        // take away XML-header
        formula = formula.trim();
        int start = formula.indexOf("<math");
        if (start > 0) {
            formula = formula.substring(start);
        }
        m_id = id;
        m_formulaSource = prepareSource(formula);
        m_madeOnTheFly = onthefly;
        m_formulaType = PIcommand.MATHML;
        if (subtext == null) {
            m_subtext = m_id;
        } else {
            m_subtext = subtext;
        }

        if (m_subtext.startsWith("_f")) {
            m_subtext = m_subtext.substring(2);
        }
        m_backupCatalog = def.getBuildCatalog() + "/" + Definitions.MATHMLBACKUP;
        convertToLocal(null);
        leaveMessage();
    }

    /**
     * Getting the MathMl code representing the formula
     * @param mod The Module making the request
     * @return The mathml code as a string
     */
    @Override
    public String getFormulaLocation(Module mod) {
        // we try to backup a locally stored copy
        try {
            URI absuri = accessutils.makeAbsoluteURI(m_backupCatalog + "/" + m_id + ".txt", mod.getCatalog());
            URI relUri = accessutils.makeRelativeURI(mod.getAbsoluteUri(), absuri);
            return relUri.toString();
        } catch (Exception ex) {
            return m_backupCatalog + "/" + m_id + ".txt";
        }
    }




    /**
     * Producing the span-element that will represent the formula
     * @param mod The Module making the request
     * @return The produced span-element
     */
    @Override
    public DocumentFragment produceIntextElement(Module mod, PIcommand cmd) {
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        Element wrapper = mod.getDoc().createElement("span");
        wrapper.setAttribute("class", PIcommand.WXTSTYLEPREFIX + PIcommand.MATHML);
        if ((cmd != null) && cmd.paramExist(PIcommand.STYLE)) {
            wrapper.setAttribute("style", cmd.getValue(PIcommand.STYLE));
        }
        boolean bshowImage=false;
        if((cmd!=null)&&(cmd.paramExist(PIcommand.SHOWIMAGE))){
            bshowImage=cmd.getValue(PIcommand.SHOWIMAGE).compareTo(PIcommand.YES)==0;
        }
        
        //bshowImage=true;
        
        
        if(!bshowImage){
            try {
                DocumentFragment df2 = domer.produceDocFragmentFromString(getFormulaSource(mod),
                        mod.getEncoding(),
                        mod.getDefinitions().getReporter());

                wrapper.appendChild(mod.getDoc().importNode(df2, true));
                df.appendChild(wrapper);
                return df;
            } catch (Exception ex) {
                Element preElt = mod.getDoc().createElement("pre");
                preElt.appendChild(mod.getDoc().createTextNode(m_formulaSource));
                df.appendChild(preElt);
                return df;
            }
        }
        else
        {
            // show as image
            try{
                Element imgElt=mod.getDoc().createElement("img");
                String uristr="file:///" + m_backupCatalog + "/" + m_id + ".png";
                uristr = uristr.replace(' ', '_').replace('\\','/');
                URI imgUri=accessutils.makeUri(uristr);
                URI relativeUri=accessutils.makeRelativeURI(mod.getAbsoluteUri(), imgUri);
                imgElt.setAttribute("src", relativeUri.toString());
                df.appendChild(imgElt);
            }catch(Exception ex){
                
            }
            return df;
        }
    }



    /**
     * Clean source and prepare for strait HTML5
     * remove xmlns and namespace prefix
     */
    private String prepareSource(String S) {
        S = S.replace("xmlns:math=\"http://www.w3.org/1998/Math/MathML\"", "");
        return S.replace("math:", "");
    }

    /**
     * Makes a  copy of mathml fomula as string, no XML-header
     * (in case you will use them or translate them)
     * and save
     * And save the formula as an image
     * @param cmd Is not used
     */
    private void convertToLocal(PIcommand cmd) {
        // pick up the source
        try {
            String uristr = "file:///" + m_backupCatalog + "/" + m_id + ".txt";
            uristr = uristr.replace(' ', '_');
            URI localUri = accessutils.makeUri(uristr);
            accessutils.saveTFile(localUri, m_formulaSource, "utf-8");
             
            uristr="file:///" + m_backupCatalog + "/" + m_id + ".png";
            uristr = uristr.replace(' ', '_').replace('\\','/');
            URI imgUri=accessutils.makeUri(uristr);
            Dimension dim=mathmlToImage.buildImage(localUri, imgUri, cmd);
             
            
        } catch (Exception ex) {
            System.out.println("cannot write . "+ex.getMessage());
        }

    }

    /**
     * Leave a textfile in the backupcatalog
     */
    private void leaveMessage() {
        String msg = "These files are not used by WXT.\n"
                + "They are produced for inspection, and possible conversion\n"
                + "with any tool at your disposal\n"
                + "bs";
        try {
            String uristr = "file:///" + m_backupCatalog + "/readme.txt";
            URI localUri = accessutils.makeUri(uristr);
            if (!accessutils.resourceExists(localUri)) {
                accessutils.saveTFile(localUri, msg, "utf-8");
            }
        } catch (Exception ex) {
            System.out.println("cannot write");
        }

    }

    @Override
    public String getCompareValue() {
        return m_id;
    }
}

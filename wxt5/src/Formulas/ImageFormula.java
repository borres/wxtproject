package Formulas;

import content.Module;
import java.net.URI;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import utils.PIcommand;
import utils.accessutils;

/**
 * Handling a formula described as an image
 * @author Administrator
 */
public class ImageFormula extends Formula {

    URI m_abslocation = null;
    

    /**
     * Constructing a formula from an element in a formula file
     * @param elt The element
     * @param absCatalog The absolute catalog for the formula file
     * @throws Exception when we cannot make a formula
     */
    public ImageFormula(Element elt, URI absCatalog)
            throws Exception {
        super(elt);
        String location = ((Element) (elt.getElementsByTagName(PIcommand.VALUE).item(0))).getTextContent().trim();
        m_abslocation = accessutils.makeAbsoluteURI(location, absCatalog.toString());
        m_formulaType = PIcommand.IMAGE;
        m_formulaSource=null;
    }

    /**
     * Constructing from a PI
     * @param mod The module that request the formula
     * @param cmd The command that describes the request
     * @throws Exception when we cannot make a formula
     */
    public ImageFormula(Module mod, PIcommand cmd)
            throws Exception {
        super(mod, cmd);
        if (!cmd.paramExist(PIcommand.LOCATION)) {
            throw new Exception("Missing location");
        }
        String location = cmd.getValue(PIcommand.LOCATION);
        m_abslocation = accessutils.makeAbsoluteURI(location, mod.getAbsoluteUri().toString());
        m_formulaType = PIcommand.IMAGE;
        m_madeOnTheFly = true;
        m_formulaSource=null;
    }

    /**
     * Getting the location for the image representing the formula
     * @param mod The Module making the request
     * @return The location as a string representing an URI
     */
    @Override
    public String getFormulaLocation(Module mod) {
        return accessutils.makeRelativeURI(mod.getAbsoluteUri(), m_abslocation).toString();
    }

    /**
     * Getting the string that describes the formula
     * @param mod
     * @return 
     */
    @Override
    public String getFormulaSource(Module mod) {
        return null;
    }

    /**
     * Producing the img-element that will represent the formula
     * @param mod The Module making the request
     * @return The produced img-element
     */
    @Override
    public DocumentFragment produceIntextElement(Module mod, PIcommand cmd) {
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        Element imElt = mod.getDoc().createElement("img");
        imElt.setAttribute("src", getFormulaLocation(mod));
        imElt.setAttribute("alt", m_id);
        if ((cmd != null) && (cmd.paramExist(PIcommand.STYLE))) {
            imElt.setAttribute("style", cmd.getValue(PIcommand.STYLE));
        }
        df.appendChild(imElt);
        return df;
    }
    


    @Override
    public String getCompareValue() {
        return m_id;
    }
}

package Formulas;

import content.Module;
import indexing.IndexItem;
import indexing.indexable;
import java.util.List;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import reporting.reporter;
import utils.PIcommand;

/**
 * Base class for holding formulas
 * ImageFormula, GoogleTexFormula,MathMLFormula,LaTexFormula
 * @author bs
 */

public abstract class Formula implements indexable {
    
    // common to all formulas

    /** the id of the formula */
    String m_id = null;
    /** the subtext, if set */
    String m_subtext = null;
    /** made from PIcommand or def-element*/
    boolean m_madeOnTheFly = false;
    /** type of formula*/
    String m_formulaType = "Formula";
    /** the actual location of the image backup*/
    String m_formulaImageLocation;
    /** the source of the fomula*/
    String m_formulaSource;

    /**
     * 
     */
    public Formula() {
    }

    /**
     * Constructing from a PI
     * @param mod The module that request the formula
     * @param cmd The command that describes the request
     * @throws Exception when we cannot make a formula
     */
    public Formula(Module mod, PIcommand cmd)
            throws Exception {
        m_id = cmd.getValue(PIcommand.ID);
        m_subtext = m_id;
        if (cmd.paramExist(PIcommand.SUBTEXT)) {
            m_subtext = cmd.getValue(PIcommand.SUBTEXT);
        }
        m_madeOnTheFly = true;

    }

    /**
     * Constructing a formula from an element in a formulas file
     * @param elt
     * @throws Exception when we cannot construct a formula
     */
    public Formula(Element elt)
            throws Exception {
        //--- id
        if (elt.hasAttribute(PIcommand.ID)) {
            m_id = elt.getAttribute(PIcommand.ID);
            m_subtext = m_id;//default
        } else {
            throw new Exception(reporter.getBundleString("Formula_need_id_and_value"));
        }

        // --- subtext
        try {
            m_subtext = ((Element) (elt.getElementsByTagName(PIcommand.SUBTEXT).item(0))).getTextContent().trim();
        } catch (Exception ex) {
            m_subtext = m_id;//default
        }

        m_madeOnTheFly = false;

    }

    /**
     * Get the id for this formula
     * @return The id
     */
    public String getId() {
        return m_id;
    }

    /**
     * get the subtext for this formula
     * @return The subtext, or default id if subtext not set
     */
    public String getSubText() {
        return m_subtext;
    }

    /**
     * Get the formula type
     * @return mathml, image, latex
     */
    public String getFormulaType() {
        return m_formulaType;
    }

    /**
     * Getting the  location of the formula
     * @param mod The Module making the request
     * @return The location of the Image representing the formula
     */
    public String getFormulaLocation(Module mod) {
        return m_formulaImageLocation;
    }

    /**
     * Getting the  code representing the formula
     * @param mod The Module making the request
     * @return teh code describing the formula
     */
    public String getFormulaSource(Module mod) {
        return m_formulaSource;
    }

    /**
     * Is this formula made from a command or is it defined
     * @return 
     */
    @Override
    public boolean madeOnTheFly() {
        return m_madeOnTheFly;
    }

    /**
     * Producing a div-element that will represent the absence of a formula
     * @param mod The Module making the request
     * @param cmd Not used
     * @return The produced div-element
     */
    @Override
    public DocumentFragment produceIntextElement(Module mod, PIcommand cmd) {
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        Element divElt = mod.getDoc().createElement("div");
        divElt.appendChild(mod.getDoc().createTextNode("basic formula"));
        df.appendChild(divElt);
        return df;
    }

    /**
     * Producing a tr element with two td-elements,
     * for the formula and the description and links to using modules
     * @param mod The module making the request
     * @param cmd The command dewscribing the request
     * @param users The modules using the formula
     * @return The prepared tr-element
     */
    @Override
    public DocumentFragment produceListElement(Module mod, PIcommand cmd, List<IndexItem> users) {
        Element wrapper = mod.getDoc().createElement("li");
        wrapper.setAttribute("class", PIcommand.WXTSTYLEPREFIX + PIcommand.FORMULA);

        // description 
        Element nameDiv = mod.getDoc().createElement("div");
        nameDiv.setAttribute("class", PIcommand.TEXT);
        nameDiv.appendChild(mod.getDoc().createTextNode(m_subtext));
        wrapper.appendChild(nameDiv);

        // the formula
        wrapper.appendChild(produceIntextElement(mod, cmd));

        // pagelinks
        for (IndexItem ie : users) {
            //Element elt=mod.getProducer().makeXLink(mod, ie.getMod(), "div", "link",Producer.NO_LEVEL_ADJUST,true);
            Element elt = mod.makeXLink(ie.getMod(), "div", "link", -999, true);
            wrapper.appendChild(elt);
        }
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        df.appendChild(wrapper);
        return df;
        //return wrapper;
    }

    @Override
    public int compareTo(indexable o) {
        return o.getCompareValue().compareTo(o.getCompareValue());
    }
}
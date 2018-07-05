package content;

import java.net.URI;
import org.w3c.dom.Element;
import utils.PIcommand;
import utils.accessutils;

/**
 *
 * @author Administrator
 */
public class ImageFormula extends Formula
{
    URI m_abslocation=null;

    /**
     * Constructing a formula from an element in a formula file
     * @param elt The element
     * @param absCatalog The absolute catalog for the formula file
     * @throws Exception when we cannot make a formula
     */
    public ImageFormula(Element elt,URI absCatalog)
    throws Exception
    {
       super(elt);
       String location=((Element)(elt.getElementsByTagName(PIcommand.VALUE).item(0))).getTextContent().trim();
       m_abslocation=accessutils.makeAbsoluteURI(location, absCatalog.toString());
       m_formulaType=PIcommand.IMAGE;
    }

    /**
     * Constructing from a PI
     * @param mod The module that request the formula
     * @param cmd The command that describes the request
     * @throws Exception when we cannot make a formula
     */
    public ImageFormula(Module mod, PIcommand cmd)
            throws Exception
    {
        super(mod,cmd);
        if(!cmd.paramExist(PIcommand.LOCATION))
            throw new Exception("Missing location");
        String location=cmd.getValue(PIcommand.LOCATION);
        m_abslocation=accessutils.makeAbsoluteURI(location,mod.getAbsoluteUri().toString());
        m_formulaType=PIcommand.IMAGE;
        m_madeOnTheFly=true;
    }


    /**
     * Getting the location for the image representing the formula
     * @param mod The Module making the request
     * @param param1 Not used
     * @param param2 Not used
     * @return The location as a string representing an URI
     */
    @Override
    public String getFormulaSource(Module mod)
    {
        return accessutils.makeRelativeURI(mod.getAbsoluteUri(), m_abslocation).toString();
    }

   /**
    * Producing the img-element that will represent the formula
    * @param mod The Module making the request
    * @param param1 Not used
    * @param param2 Not used
    * @return The produced img-element
    */
    @Override
    public Element getDisplayElement(Module mod,PIcommand cmd)
    {
         Element imElt=mod.getDoc().createElement("img");        
         imElt.setAttribute("src",getFormulaSource(mod));        
         imElt.setAttribute("alt",m_id);
         if((cmd!=null)&&(cmd.paramExist(PIcommand.STYLE)))
             imElt.setAttribute("style",cmd.getValue(PIcommand.STYLE));
         return imElt;
    }
 }

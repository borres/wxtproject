

package content;

import java.awt.Dimension;
import java.net.URI;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.PIcommand;
import utils.accessutils;
import utils.domer;
import utils.reporter;
import utils.useJEuclid;

/**
 * Holds a Formula. a Formula may be represented as a
 * Mathml fragment  or as an image
 * jEuclid is used to convert from Mml to image
 *
 * Formulas may be generated in one of the following ways:
 * 1 from a Formula file in Definitions
 * 2 from a formulas file (odf) in definitions
 * 3 from import odf with formulas
 * 4 from an on-the-fly Formula PI with location (image or Mml)
 *
 * Formulas may be presented as
 * 1 a fomula PI
 * 2 as part of a formulalist PI
 *
 * @author bs
 */
public class Formula
{
    /** the unique id for this Formula*/
    String m_id=null;
    /** the permanent location for this Formula as an image*/
    URI m_absImageLocation=null;
    /** the temporary location of this Formula as mml*/
    URI m_absMmlLocation=null;
    /** has image been built*/
    boolean m_isImageBuilt=false;
    boolean m_failedtobuildImage=false;

    /** the complete mathml as a string*/
    String m_MmlString=null;
    
    /**
     * constructor 
     */
    public Formula(String id,String mmllocation,String imglocation,reporter rep)
    throws Exception{
        if(id==null)
            throw new Exception();
        m_id=id;

        // should we load the mathml string immediately and use this ?
        // or should we stay with the location ?
        // location will make sure we get updated version on multiple builds
        // string will make sure we do not mix up temporary storage

        if(mmllocation!=null)
            m_absMmlLocation=new URI(mmllocation);
        if(imglocation!=null)
            m_absImageLocation=new URI(imglocation);
    }
    
    public String getID(){return m_id;}
    public URI getImageLocation(){return m_absImageLocation;}
    public URI getMmlLocation(){return m_absMmlLocation;}

    public DocumentFragment getAsImageElement(PIcommand cmd,Module mod)
    {
        DocumentFragment df=mod.getDoc().createDocumentFragment();
        Dimension imageDimension;
        if(!m_isImageBuilt && !m_failedtobuildImage)
        {
            //attempt to build image by means of jeuclid
            try
            {
                imageDimension=useJEuclid.buildImage(m_absMmlLocation,m_absImageLocation,cmd);
                m_isImageBuilt=true;
            }
            catch(Exception ex)
            {
                m_failedtobuildImage=true;
                mod.getReporter().pushSimpleMessage("Could not build image for formula");

            }

        }
        if(m_isImageBuilt)
        {
            Element imelt=mod.getDoc().createElement("img");
            URI relUri=accessutils.makeRelativeURI(mod.getAbsoluteUri(), m_absImageLocation);
            imelt.setAttribute("src", relUri.toString());
            imelt.setAttribute("alt", "image:"+m_id);
            df.appendChild(imelt);
        }
        else
        {
            df.appendChild(mod.getProducer().makeFailureComment(mod.getDoc(),cmd));
        }

        return df;
    }

    public DocumentFragment getAsMmlElement(PIcommand cmd,Module mod)
    {
        DocumentFragment df=mod.getDoc().createDocumentFragment();
        //get the correct content.xml - file
        Document contentDoc=null;
        try{
            String mathS=accessutils.getTextFile(m_absMmlLocation, "utf-8");
            if(mathS.indexOf("xmlns:math")==-1)
            {
                // this is not a math object
                throw new Exception("not a math object");
            }
            // ??
            mathS=mathS.replaceAll("math:", "");
            mathS=mathS.replace("xmlns:math","xmlns");
            contentDoc=domer.makeDomFromStringSomeHow(mathS,null,mod.getReporter());
        }
        catch(Exception ex1)
        {
            mod.getReporter().pushMessage("bad_processing_instruction", cmd.toString(),ex1.getMessage());
            df.appendChild(mod.getProducer().makeFailureComment(mod.getDoc(),cmd));
            // cannot find or parse content.xml
            //System.out.println(ex1.getMessage());
            return df;
        }

        //we have the single Formula in content
        // remove annotation ?
        NodeList alist=contentDoc.getElementsByTagName("annotation");
        if(alist.getLength()>0)
        {
            Node n=alist.item(0);
            n.getParentNode().removeChild(n);
        }

        //wrap it
        Element spanElt=contentDoc.createElement("div");
        spanElt.setAttribute("class",cmd.getCommandStyleName());
        spanElt.appendChild(contentDoc.getDocumentElement());
        df.appendChild(mod.getDoc().importNode(spanElt,true));

        return df;

    }

}

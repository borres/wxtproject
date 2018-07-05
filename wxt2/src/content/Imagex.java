package content;
import java.awt.image.BufferedImage;
import java.net.URI;

import java.util.Vector;
import javax.imageio.ImageIO;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.PIcommand;
import utils.accessutils;
/**
 * Holding an image which is registered
 * or will be registered
 * @author bs
 */
public class Imagex
{
    /** default thumbheight*/
    static final int DEFAULT_THUMBHEIGHT=100;
    /**Max value for expected thumb size */
    static public int THUMBHEIGHT=100;
    /**The id of this image*/
    String m_id;
    /** the absolute URI for this image*/
    URI m_absoluteUri;
    /** the location string as read*/
    String m_uriString;
    /** the fixed display width of this image*/
    String m_width;
    int m_width_NUM=-1;
    /** the fixed display height of this image*/
    String m_height;
    int m_height_NUM=-1;
    /** the alt text */
    String m_alt;
    /** the real width of this image*/
    String m_real_width;
    int m_real_width_NUM=-1;
    /** the real height of this image*/
    String m_real_height;
    int m_real_height_NUM=-1;
    /** ratio for real values*/
    float m_wh_ratio;
    /** the subtext of this image*/
    String m_subText;
    /** styled width of subtext*/
    String m_subTxtWidth;
    /** if generated from cmd (on the fly)*/
    boolean m_on_the_fly;


    public Imagex()
    {
        m_id=null;
        m_absoluteUri=null;
        m_uriString=null;
        m_width=null;
        m_height=null;
        m_alt=null;
        m_real_width=null;
        m_real_height=null;
        m_subText=null;
        m_wh_ratio=1.0f;
        m_subTxtWidth="100px";
        m_on_the_fly=false;
    }
     /**
     * Constructing an image from an element in an images file
     * id and URI must be present in element
     * @param elt The element describing the image
     * @param basecatalog The basecatalog that all addresses are relative to
     * @throws java.lang.Exception when adressing goes wrong or id is missing
     */
    public Imagex(Element elt,URI basecatalog,Definitions def)
     throws Exception   
    {
        this();

        // check that we have an id
       if(elt.hasAttribute("id"))
            m_id=elt.getAttribute("id");
        else // should not happen
            throw new Exception("Image element: missing id");

        // read all from element
        NodeList nlist=elt.getChildNodes();
        for(int ix=0;ix<nlist.getLength();ix++)
        {
            if(nlist.item(ix).getNodeType()==Node.ELEMENT_NODE)
            {
                Element e=(Element)nlist.item(ix);
                if(e.getTagName().compareTo(PIcommand.LOCATION)==0)
                    m_uriString=e.getTextContent().trim();
                else if(e.getTagName().compareTo(PIcommand.WIDTH)==0)
                    m_width=e.getTextContent().trim();
                else if(e.getTagName().compareTo(PIcommand.HEIGHT)==0)
                    m_height=e.getTextContent().trim();
                else if(e.getTagName().compareTo(PIcommand.ALT)==0)
                    m_alt=e.getTextContent().trim();
                else if(e.getTagName().compareTo(PIcommand.SUBTEXT)==0)
                    m_subText=e.getTextContent().trim();
            }
        }
        //----------------------------
        // we have extracted it all, check it
        if(m_uriString==null)
            throw new Exception("Image element: missing location");
        m_uriString=def.substituteFragments(m_uriString);
        m_absoluteUri=accessutils.makeAbsoluteURI(m_uriString, basecatalog.toString());

        Control();
    }

    /**
     * Constructing an image from a PI, on the fly
     * id and URI must be present in cmd
     * @param elt The element describing the image
     * @param basecatalog The basecatalog that all addresses are relative to
     * @throws java.lang.Exception when adressing goes wrong or id is missing
     */
    public Imagex(PIcommand cmd,URI basecatalog,Definitions def)
     throws Exception
    {
        this();
        m_on_the_fly=true;
        // get what we need from cmd
        // check that we have an id
       if(cmd.paramExist(PIcommand.ID))
            m_id=cmd.getValue(PIcommand.ID);
        else
            throw new Exception("Image element: missing id");

        // read all from element
        if(cmd.paramExist(PIcommand.LOCATION))
            m_uriString=cmd.getValue(PIcommand.LOCATION);
        if(cmd.paramExist(PIcommand.WIDTH))
            m_width=cmd.getValue(PIcommand.WIDTH);
        if(cmd.paramExist(PIcommand.HEIGHT))
            m_height=cmd.getValue(PIcommand.HEIGHT);
        if(cmd.paramExist(PIcommand.ALT))
            m_alt=cmd.getValue(PIcommand.ALT);
        if(cmd.paramExist(PIcommand.SUBTEXT))
            m_subText=cmd.getValue(PIcommand.SUBTEXT);

        //----------------------------
        // we have extracted it all, check it
        if(m_uriString==null)
            throw new Exception("Image element: missing location");
        m_uriString=def.substituteFragments(m_uriString);
        m_absoluteUri=accessutils.makeAbsoluteURI(m_uriString, basecatalog.toString());

        Control();
    }

    /**
     * Control and justify all values
     */
    private void Control()
    {

        // fix the alt value
        if(m_alt==null)
        {
            // make it from the uri
            m_alt=accessutils.getFileNameFromPathString(m_uriString);
        }
        // fix the subtext
        if(m_subText==null)
            m_subText="";

        // check values for dimensions
        // nonumerics in the end ?
        if(m_width!=null)
        {
            m_width=m_width.trim();
            m_width_NUM=accessutils.getNumericStart(m_width);
            if(Character.isDigit(m_width.charAt(m_width.length()-1)))
                 m_width=m_width+"px";
         }
        if(m_height!=null)
        {
            m_height=m_height.trim();
            m_height_NUM=accessutils.getNumericStart(m_height);
            if(Character.isDigit(m_height.charAt(m_height.length()-1)))
                 m_height=m_height+"px";
       }
        // get and set the real image size
       calculateRealSize();
    }


    /**
     * Calculate the real size of the image
     */
    private void calculateRealSize()
    {
        try
        {
            // load the image
            BufferedImage img = ImageIO.read(m_absoluteUri.toURL());
            m_real_width_NUM=img.getWidth();
            m_real_width=m_real_width_NUM+"px";
            m_real_height_NUM=img.getHeight();
            m_real_height=m_real_height_NUM+"px";
            m_wh_ratio=1.0f*img.getWidth()/img.getHeight();
        }
        catch (Exception ex)
        {
            //cannot get it, test existance with positive when use
            m_real_width_NUM=-1;
            m_real_height_NUM=-1;

        }
    }

    // the area we should use for subtext
    private int calculateTextWidth()
    {
       int subtextwidth=100;
       if(m_width!=null)
           subtextwidth=m_width_NUM;
       else if(m_height!=null)
       {
           if(m_wh_ratio < Integer.MAX_VALUE)
               subtextwidth=Math.round(m_wh_ratio*m_height_NUM)-10;
       }
       else
       {
           if(m_real_width_NUM > 0)
               subtextwidth=m_real_width_NUM;
       }
       return subtextwidth;
    }

     // the area we should use for subtext
    private int calculatePackageWidth(String W,String H)
    {
       int packagetwidth=100;
       if(W!=null)
           packagetwidth=accessutils.getNumericStart(W);
       else if(H!=null)
       {
           if(m_wh_ratio < Integer.MAX_VALUE)
               packagetwidth=Math.round(m_wh_ratio*accessutils.getNumericStart(H))-10;
       }
       else // what else could we do
       {
           if(m_real_width_NUM > 0)
               packagetwidth=m_real_width_NUM;
       }
       return packagetwidth;
    }


    /**
     * the id
     * @return the id
     */
    public String getID()
    {
        return m_id;
    }

    /**
     * Is this image made from a cmd
     * @return true if made from cmd, false otherwise
     */
    public boolean madeOnTheFly()
    {
        return m_on_the_fly;
    }

    /**
     * Show an image in the running text
     * @param mod The module requesting the reference
     * @param cmd The PI-command describing the reference-request
     * @return The result as a fragment
     */
    public DocumentFragment showImage(Module mod, PIcommand cmd)
    {
        // parameter display checked to be: 0:dont display, 1: as image
        // 2:image with frame, 3:image with subtext, 4 image with frame and subtext

        // default
        String display="4";
        if(cmd.paramExist(PIcommand.DISPLAY))
            display=cmd.getValue(PIcommand.DISPLAY);
        
        DocumentFragment df=mod.getDoc().createDocumentFragment();
        
        //=============== no display ==================
        if(display.compareTo("0")==0)
            return df;
        
        // prepare the image element
        // find size(actualWidth and/or actualHeight),
        // must be corrected from cmd

        // pick up regeistered values
        String actualWidth=null;
        if(m_width!=null)
            actualWidth=m_width;
        String actualHeight=null;
        if(m_height!=null)
            actualHeight=m_height;
        // correct from actual cmd
        if(cmd.paramExist(PIcommand.WIDTH))
        {
            String sW=cmd.getValue(PIcommand.WIDTH).trim();
            if( Character.isDigit(sW.charAt(sW.length()-1)) )
                actualWidth=sW+"px";
            else
                actualWidth=sW;

            actualHeight=null;
        }
        if(cmd.paramExist(PIcommand.HEIGHT))
        {
            String sH=cmd.getValue(PIcommand.HEIGHT).trim();
            if( Character.isDigit(sH.charAt(sH.length()-1)) )
                actualHeight=sH+"px";
            else
                actualHeight=sH;

            if(!cmd.paramExist(PIcommand.WIDTH))
                actualWidth=null;
        }

        // prepare image element
        Element imElt=mod.getDoc().createElement("img");
        imElt.setAttribute("src",getRelativeSRC(mod, m_absoluteUri));
        if(actualWidth!=null)
            imElt.setAttribute("width",actualWidth);
        if(actualHeight!=null)
            imElt.setAttribute("height",actualHeight);

        // m_alt has always a value, either an alt spec or the filename
        imElt.setAttribute("alt",m_alt);       
        if(m_subText.isEmpty())
            imElt.setAttribute("title",m_alt);
        else
            imElt.setAttribute("title",m_subText);
       
        // =============== strait img display ===================
        if(display.compareTo("1")==0)
        {
            df.appendChild(imElt);
            return df;
        }
        
        // make frame, how wide ?
        boolean setWidthForSubText=true;

        int width=calculatePackageWidth(actualWidth,actualHeight);
        String packageWidth=width+16+"px"; // allow for some horisontal spacing
        if(actualWidth!=null && actualWidth.endsWith("%"))
        {
            // let package set the width and fill it completely with image
            packageWidth=actualWidth;
            imElt.setAttribute("width","100%");
            setWidthForSubText=false;
        }
        else if(actualHeight!=null && actualHeight.endsWith("%"))
        {
            packageWidth=""+(Math.round(m_wh_ratio/accessutils.getNumericStart(actualHeight))-10)+"%";
            //packageWidth=actualHeight;
            packageWidth=null;
        }


        Element wrapper=mod.getDoc().createElement("div");
        if(packageWidth!=null)
            wrapper.setAttribute("style", "width:"+packageWidth);
        
        // ============= image with styled frame ==============
        if(display.compareTo("2")==0)
        {
            wrapper.setAttribute("class", cmd.getCommandStyleName());//wxtimage
            wrapper.appendChild(imElt);
            df.appendChild(wrapper);
            return df;
        }

        // make subtext
        Element stElt=mod.getDoc().createElement("div");
        if(setWidthForSubText)
            stElt.setAttribute("style", "width:inherit");
        stElt.appendChild(mod.getDoc().createTextNode(m_subText));

        // ============== image with subtext in neutral frame ===============
        if(display.compareTo("3")==0)
        {
            // neutral wrapper
            wrapper.appendChild(imElt);
            wrapper.appendChild(stElt);
            df.appendChild(wrapper);
            return df;
        }

        // ========== display with classed frame and subtext ========
        // display.compareTo("4")==0
        wrapper.setAttribute("class", cmd.getCommandStyleName());//wxtimage
        wrapper.appendChild(imElt);
         if(!m_subText.isEmpty())
           wrapper.appendChild(stElt);

        df.appendChild(wrapper);
        return df;
    }
    
    /** Calculating relative src
     * @param owner The modules absolute uri
     * @param abs The registered absolte uri
     * @return The relative uri
     */
    private String getRelativeSRC(Module owner,URI abs)
    {
       return accessutils.makeRelativeURI(owner.getAbsoluteUri(), abs).toString();
    }

    /**
     * Show an image in a thumb list
     * @param mod The module requesting the reference
     * @param cmd The PI-command describing the reference-request
     * @return The result as a fragment     */
    public DocumentFragment produceThumbImage(Module mod, PIcommand cmd)
    {
        // produce thumb according to this pattern
        /* 
          <span class="wxtthumbwrapper">
	    <img src="d2.jpg" style="height:100px" alt="d2" onmouseover="expandImage(this,'im6')">
          </span>
         */

        // decide if we want to use actual thumbheigth or use real height
        // for image smaller than the thumb
        String useHeight=THUMBHEIGHT+"px";
        if((m_real_height_NUM > 0) && (m_real_height_NUM < THUMBHEIGHT))
        {
            useHeight=m_real_height_NUM+"px";
        }

        
        DocumentFragment df=mod.getDoc().createDocumentFragment();
        Element spanElt=mod.getDoc().createElement("span");
        spanElt.setAttribute("class", "wxtthumbwrapper"); 
        Element imgElt=mod.getDoc().createElement("img");
        imgElt.setAttribute("src", getRelativeSRC(mod, m_absoluteUri));
        if(useHeight!=null)
            imgElt.setAttribute("style", "height:"+useHeight);
        imgElt.setAttribute("alt", m_alt);
        imgElt.setAttribute("onmouseover", "expandImage(this,'"+m_id+"')");
        
        spanElt.appendChild(imgElt);
        df.appendChild(spanElt);
        return df;
    }
    
     /**
     * Show an image as stored to be fetched from a mouseover
     * @param mod The module requesting the reference
     * @param cmd The PI-command describing the reference-request
     * @return The result as a fragment
      */
    public DocumentFragment produceHiddenImage(Module mod, PIcommand cmd)
    {
        // produce imagestorage according to this pattern
        /* 
	<div id="im6" >
	<div  class="wxtimcontainer" >
		<div>
		<img src="d2.jpg" alt="d2" />
		<div>This is an image</div>
		</div>
	</div>
	</div>
         */
       
       //------------------------------------------
       // will make sure we get correct width of subtext
       m_subTxtWidth=calculateTextWidth()+"px";

       //----------------------------------------
       // want a list of all modules that has used this image
       Vector<Module> modules=mod.getDefinitions().getImageHolder().getAllModulesUsing(m_id);

       //----------------------
        DocumentFragment df=mod.getDoc().createDocumentFragment();
        Element wrapper=mod.getDoc().createElement("div");
        wrapper.setAttribute("id", m_id);
            Element classDiv=mod.getDoc().createElement("div");
            classDiv.setAttribute("class", "wxtimcontainer");
                Element innerDiv=mod.getDoc().createElement("div");
                //innerDiv.setAttribute("class","inner");
                    Element imElt=mod.getDoc().createElement("img");
                    //imElt.setAttribute("class", "imexplain");
                    imElt.setAttribute("src",getRelativeSRC(mod, m_absoluteUri));
                    imElt.setAttribute("alt", m_alt);
                    if(m_width!=null)
                        imElt.setAttribute("style","width:"+m_width);
                    if(m_height!=null)
                        imElt.setAttribute("style","height:"+m_height);

                    Element expDiv=mod.getDoc().createElement("div");
                    //expDiv.setAttribute("class","explain");
                    expDiv.setAttribute("style","width:"+ m_subTxtWidth);
                    // more here ?
                    expDiv.appendChild(mod.getDoc().createTextNode(m_subText));
                    // ref all module which has used this image
                    for(Module m:modules)
                    {
                        Element aElt=mod.getDoc().createElement("a");
                        aElt.setAttribute("href", accessutils.makeRelativeURI(mod.getAbsoluteUri(), m.getAbsoluteUri()).toString());
                        aElt.appendChild(mod.getDoc().createTextNode(m.getName()));
                        aElt.setAttribute("title", m.getDescription());
                        expDiv.appendChild(mod.getDoc().createElement("br"));
                        expDiv.appendChild(aElt);
                    }
                innerDiv.appendChild(imElt);
                innerDiv.appendChild(expDiv);
             classDiv.appendChild(innerDiv);
        wrapper.appendChild(classDiv);

        df.appendChild(wrapper);
        return df;        
    }
}

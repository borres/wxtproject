package content;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.List;

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
public class Image
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
    /** if generated from wiki or odt import*/
    boolean m_on_import_wiki_or_odt;
    /** use this flag to avoid recalc*/
    boolean has_calculated_real_size=false;


    public Image()
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
        m_on_import_wiki_or_odt=false;
    }
     /**
     * Constructing an image from an element in an images file
     * id and URI must be present in element
     * @param elt The element describing the image
     * @param basecatalog The basecatalog that all addresses are relative to
     * @throws java.lang.Exception when adressing goes wrong or id is missing
     */
    public Image(Element elt,URI basecatalog,Definitions def)
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
     * @param cmd The PI command describing the image
     * @param basecatalog The basecatalog that all addresses are relative to
     * @param def The Definitions object involved i building
     * @throws java.lang.Exception when adressing goes wrong or id is missing
     */
    public Image(PIcommand cmd,URI basecatalog,Definitions def)
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
     * Construct from a documentfragment
     * @param absuristring
     * @param width
     * @param height
     * @param alt
     * @param id
     * @throws Exception
     */
    public Image(String absuristring,String width,String height,String alt,String id)
    throws Exception
    {
        this();
        m_on_the_fly=true;
        m_on_import_wiki_or_odt=true;
        m_id=id;
        m_uriString=absuristring;
        if(width!=null && !width.isEmpty())
            m_width=width;
        if(height!=null &&!height.isEmpty())
            m_height=height;
        if(alt!=null && !alt.isEmpty())
            m_alt=alt;
        m_subText=alt;
        //----------------------------
        // we have extracted it all, check it
        if(m_uriString==null)
            throw new Exception("Image element: missing location");
        m_absoluteUri=new URI(absuristring);

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
       //calculateRealSize();
    }


    /**
     * Calculate the real size of the image
     */
    private void calculateRealSize()
    {
        if(has_calculated_real_size)
            return;
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
        has_calculated_real_size=true;
    }

    // the area we should use for subtext
    private int calculateTextWidth()
    {
       calculateRealSize();
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
       calculateRealSize();
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
    public DocumentFragment produceImage(Module mod, PIcommand cmd)
    {
        // parameter display checked to be: 0:dont display, 1: as image
        // 2:image with frame, 3:image with subtext, 4 image with frame and subtext

        // default
        String display="4";
        if(cmd.paramExist(PIcommand.DISPLAY))
            display=cmd.getValue(PIcommand.DISPLAY);
        
        DocumentFragment df=mod.getDoc().createDocumentFragment();
        
        //=============== no display ==================
        // taken care of in producer:
        if(display.compareTo("0")==0)
        {
            return df;
        }
        
        // prepare the image element
        // find size(actualWidth and/or actualHeight),
        // must be corrected from cmd
        // prepare image element
        
        // pick up original values
        String actualWidth=null;
        if(m_width!=null)
            actualWidth=m_width;
        String actualHeight=null;
        if(m_height!=null)
            actualHeight=m_height;
       
        Element imElt=mod.getDoc().createElement("img");
        imElt.setAttribute("src",getRelativeSRC(mod, m_absoluteUri));

        // pick up registered values
        if(cmd.paramExist(PIcommand.STYLE))
        {
            // is style is set we only use this and forget 
            // width and height
            imElt.setAttribute("style", cmd.getValue(PIcommand.STYLE));
        }
        else
        {
            // we pick up dimensions from cmd.
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
            }


            // new to avoid width and height attributes
            if((actualWidth!=null) && (actualHeight!=null))
                imElt.setAttribute("style", "width:"+actualWidth+";height:"+actualHeight);
            else if(actualWidth!=null)
                imElt.setAttribute("style", "width:"+actualWidth);
            else if(actualHeight!=null)
                imElt.setAttribute("style", "height:"+actualHeight);
        }
        

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
            stElt.setAttribute("style", "width:inherit;text-align:center");
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
          <div class="wxtthumbwrapper">
	    <a href="module" title="mod description">
             <img src="imref" alt="subtext"/>
            </a>
          </div>
         */
        

        List<Module> modules=mod.getDefinitions().getImageHolder().getAllModulesUsing(m_id);
        
        DocumentFragment df=mod.getDoc().createDocumentFragment();
        Element divElt=mod.getDoc().createElement("div");
        divElt.setAttribute("class", PIcommand.WXTTHUMBWRAPPER);
        
        Element aElt=mod.getDoc().createElement("a");
        aElt.setAttribute("href", m_alt);
        // only first module using this image
        Module m=modules.get(0);
        aElt.setAttribute("href", accessutils.makeRelativeURI(mod.getAbsoluteUri(), m.getAbsoluteUri()).toString());
        aElt.setAttribute("title", m.getName()+":"+m.getDescription());
        
        Element imgElt=mod.getDoc().createElement("img");
        imgElt.setAttribute("src", getRelativeSRC(mod, m_absoluteUri));
        if(m_subText!=null)
            imgElt.setAttribute("alt", m_subText);
        else
            imgElt.setAttribute("alt", "");
        
        aElt.appendChild(imgElt);
        divElt.appendChild(aElt);
        df.appendChild(divElt);
        return df;
    }
    
    /**
     * Show an image in a thumb list
     * @param mod The module requesting the reference
     * @param cmd The PI-command describing the reference-request
     * @return The result as a fragment    
     */
    public DocumentFragment produceSimpleThumbImage(Module mod, PIcommand cmd)
    {
        // produce thumb according to this pattern
        /* 
          <div class="wxtthumbwrapper">
             <img src="imref" alt="subtext"/>
          </div>
         */       
        DocumentFragment df=mod.getDoc().createDocumentFragment();
        Element divElt=mod.getDoc().createElement("div");
        //divElt.setAttribute("class", cmd.getCommandStyleName());   
        divElt.setAttribute("class", PIcommand.WXTTHUMBWRAPPER); 
        
        Element imgElt=mod.getDoc().createElement("img");
        imgElt.setAttribute("src", getRelativeSRC(mod, m_absoluteUri));
        if(m_subText!=null)
            imgElt.setAttribute("alt", m_subText);
        else
            imgElt.setAttribute("alt", "");
        
        // override styleshhet ?
        if(cmd.paramExist(PIcommand.STYLE))
            imgElt.setAttribute("style", cmd.getValue(PIcommand.STYLE));

        
        divElt.appendChild(imgElt);
        df.appendChild(divElt);
        return df;
    }
    
 

}

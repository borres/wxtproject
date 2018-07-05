/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package content;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.net.URI;
import utils.PIcommand;
import utils.accessutils;

import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JLabel;
import org.w3c.dom.Element;
import utils.reporter;

/**
 *
 * @author bs
 */
public class LaTexFormula extends Formula
{
    String m_original_formula=null;
    String m_display_formula=null;
    // saving images of LaTex formulas
    // these are actually used
    String m_backupCatalog=null;
    
     /**
     * Constructing from a PI
     * @param mod The module that request the formula
     * @param cmd The command that describes the request
     * @throws Exception when we cannot make a formula
     */
    public LaTexFormula(Module mod, PIcommand cmd)
            throws Exception
    {
        super(mod, cmd);
        m_madeOnTheFly=true;
        m_formulaType=PIcommand.LATEX;
        m_backupCatalog=mod.getDefinitions().getBuildCatalog()+"/"+Definitions.JLATEXBACKUP;
        if(cmd.paramExist(PIcommand.SOURCE))
        {
            m_original_formula=cmd.getValue(PIcommand.SOURCE);
            m_display_formula=cleanAndEncodeFormula(m_original_formula);
            convertToLocal(cmd,mod.getReporter());
            leaveMessage();
        }
        else
        {
            // location
            String location=cmd.getValue(PIcommand.LOCATION);
            URI abslocation=accessutils.makeAbsoluteURI(location, mod.getAbsoluteUri().toString());
            m_display_formula=accessutils.getTextFile(abslocation, "utf-8");           
        }
     }
 
     /**
     * Constructing a formula from en alement in a formula file
     * @param elt
     * @throws Exception when we cannot construct a formula
     */
    public LaTexFormula(Definitions def,Element elt)
        throws Exception
    {
       super(elt);

        m_original_formula=((Element)(elt.getElementsByTagName(PIcommand.VALUE).item(0))).getTextContent().trim();
        m_display_formula=cleanAndEncodeFormula(m_original_formula);
        m_madeOnTheFly=false;
        m_formulaType=PIcommand.LATEX;
        m_backupCatalog=def.getBuildCatalog()+"/"+Definitions.JLATEXBACKUP;
        convertToLocal(null,def.getReporter());
        leaveMessage();
    }

    
    
    /**
     * URL-encode the string
     * @param S The string to encode
     * @return The encoded string (always utf-8)
     */
    private String cleanAndEncodeFormula(String S)
    {
        return S;
    }
    
   /**
    * Producing the img-element that will represent the formula
    * @param mod The Module making the request
    * @param param1 Used as color
    * @param param2 Used as size modifier
    * @return The produced img-element
    */
    @Override
    public Element getDisplayElement(Module mod,PIcommand cmd)
    {
        // if we have parameters size,color,background
        // we must make a new specialized img
        if((cmd!=null)&&((cmd.paramExist(PIcommand.COLOR))||
           (cmd.paramExist(PIcommand.SIZE))||
           (cmd.paramExist(PIcommand.BACKCOLOR))))
        {
            String path=convertToLocal(cmd,mod.getReporter());
            Element imElt=mod.getDoc().createElement("img");
            imElt.setAttribute("alt", m_id);
            try
            {
                URI absuri=accessutils.makeAbsoluteURI(path, mod.getCatalog());
                URI relUri=accessutils.makeRelativeURI(mod.getAbsoluteUri(), absuri);
                imElt.setAttribute("src",relUri.toString());
                if(cmd.paramExist(PIcommand.STYLE))
                    imElt.setAttribute("style",cmd.getValue(PIcommand.STYLE));
                return imElt;
            }
            catch(Exception ex)
            {
                // just go on with std display
            }
         }
        
        Element imElt=mod.getDoc().createElement("img");
        imElt.setAttribute("alt", m_id);
        imElt.setAttribute("src",getFormulaSource(mod));
        if((cmd!=null)&&(cmd.paramExist(PIcommand.STYLE)))
            imElt.setAttribute("style",cmd.getValue(PIcommand.STYLE));
        return imElt;
    }
    
    
  /**
     * Getting the location for the image representing the formula
     * @param mod The Module making the request
     * @param color The color of the formula
     * @param size Size prefix
     * @return The location as a string representing an URI
     */
    @Override
    public String getFormulaSource(Module mod)
    {
        // we use a locally stored copy 
        try
        {
            URI absuri=accessutils.makeAbsoluteURI(m_backupCatalog+"/"+m_id+".png", mod.getCatalog());
            URI relUri=accessutils.makeRelativeURI(mod.getAbsoluteUri(), absuri);
            return relUri.toString();
        }
        catch(Exception ex)
        {
            return m_backupCatalog+"/"+m_id+".png";
        }
    }

    /**
     * Makes a local copy of latextfomulas
     */
    private String convertToLocal(PIcommand cmd,reporter rep)
    {
       // as in
       // http://forge.scilab.org/index.php/p/jlatexmath/source/tree/master/examples/Basic/Example1.java
       // see also
        //http://jmathtex.sourceforge.net/
       TeXFormula formula=null;
       String pathmodifier="";
       try {
           if (cmd!=null)
           {
               String color=null;
               String backcolor=null;
               String size=null;
               if(cmd.paramExist(PIcommand.COLOR)){
                   color=cmd.getValue(PIcommand.COLOR);
                   if(!color.startsWith("#"))
                       color="#"+color;
                   pathmodifier+=color;
               }
               if(cmd.paramExist(PIcommand.BACKCOLOR)){
                   backcolor=cmd.getValue(PIcommand.BACKCOLOR);
                  if(!backcolor.startsWith("#"))
                       backcolor="#"+backcolor;
                   pathmodifier+=backcolor;
               }
               if(cmd.paramExist(PIcommand.SIZE)){
                   size=cmd.getValue(PIcommand.SIZE);
                   if(!size.startsWith("\\"))
                       size="\\"+size;
                   pathmodifier+=size;
               }

               pathmodifier=pathmodifier.replace("#", "");
               pathmodifier=pathmodifier.replace("\\", "");
               pathmodifier=pathmodifier.replace("/", "");
               pathmodifier=pathmodifier.replace(" ", "");

               if(size!=null) 
                   formula = new TeXFormula(size+" "+m_display_formula);
               else
                   formula = new TeXFormula(m_display_formula);

               if(color!=null)
                   formula.setColor(Color.decode(color));

               if(backcolor!=null)
                   formula.setBackground(Color.decode(backcolor));

            }
           else
                formula = new TeXFormula(m_display_formula);


           TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 20);
           icon.setInsets(new Insets(0, 0, 0, 0));
           BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
           Graphics2D g2 = image.createGraphics();
           g2.setColor(Color.white);
           g2.fillRect(0,0,icon.getIconWidth(),icon.getIconHeight());
           JLabel jl = new JLabel();
           jl.setForeground(new Color(0, 0, 0));
           icon.paintIcon(jl, g2, 0, 0);
           String path=m_backupCatalog+"/"+m_id+pathmodifier+".png";
           File file = new File(path);
           accessutils.makeCatalog(m_backupCatalog);

            ImageIO.write(image, "png", file.getAbsoluteFile());
            return path;
       } 
       catch (Exception ex) {
           System.out.println("could_not_copy_LaTexFormula: "+m_original_formula);
           rep.pushMessage("could_not_copy_LaTexFormula", m_id,ex.getMessage());
           return null;
       }
    }
    
    
    private void leaveMessage()
    {
       String msg="These files are used by WXT.\n"+
                   "They are produced by WXT using JLaTeXMath\n"+
                   "http://forge.scilab.org/index.php/p/jlatexmath/"+"\n"+
                   "bs";
       try{
            String uristr="file:///"+m_backupCatalog+"/readme.txt";
            URI localUri=accessutils.makeUri(uristr);
            if(!accessutils.resourceExists(localUri))
                accessutils.saveTFile(localUri, msg, "utf-8");
        }
        catch(Exception ex)
        {
            System.out.println("cannot write");
        }

    }
}

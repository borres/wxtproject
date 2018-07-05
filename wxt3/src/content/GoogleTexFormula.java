package content;

import java.net.URI;
import java.net.URLEncoder;
import org.w3c.dom.Element;
import utils.PIcommand;
import utils.accessutils;

/**
 * as from:
 * http://en.wikipedia.org/wiki/Help:Displaying_a_formula
 * and
 * http://code.google.com/apis/chart/docs/gallery/formulas.html
 * and
 * http://code.google.com/apis/chart/infographics/docs/formulas.html
 * @author Administrator
 */
public class GoogleTexFormula extends Formula
{
    // chf makes it transparent always ?
    static final String GOOGLECHARTURI="http://chart.apis.google.com/chart?cht=tx";
    //static final String GOOGLECHARTPATH="http://chart.apis.google.com/chart?cht=tx&chf=bg,s,FFFFFF00&chl=";
    static final String GOOGLELATEX="&chl=";//+m_display_formula
    static final String GOOGLEBACKGROUND="&chf=bg,s,";//+FFFFFF
    static final String GOOGLECHARTCOLOR="&chco=";//+FFFFFF
    
    String m_original_formula=null;

    // saving copies of google formulas
    String m_backupCatalog=null;


    /**
     * Constructing from a PI
     * @param mod The module that request the formula
     * @param cmd The command that describes the request
     * @throws Exception when we cannot make a formula
     */
    public GoogleTexFormula(Module mod, PIcommand cmd)
            throws Exception
    {
        super(mod, cmd);
        m_madeOnTheFly=true;
        m_formulaType=PIcommand.LATEX;
        m_backupCatalog=mod.getDefinitions().getBuildCatalog()+"/"+Definitions.GOOGLEBACKUP;
        if(cmd.paramExist(PIcommand.SOURCE))
        {
            m_original_formula=cmd.getValue(PIcommand.SOURCE);
            convertToLocal(cmd);
            leaveMessage();
        }
        else
        {
            // location
            String location=cmd.getValue(PIcommand.LOCATION);
            URI abslocation=accessutils.makeAbsoluteURI(location, mod.getAbsoluteUri().toString());
            m_original_formula=accessutils.getTextFile(abslocation, "utf-8");                       
        }

     }

    /**
     * Constructing a formula from en alement in a formula file
     * @param elt
     * @throws Exception when we cannot construct a formula
     */
    public GoogleTexFormula(Definitions def,Element elt)
        throws Exception
    {
       super(elt);

        m_original_formula=((Element)(elt.getElementsByTagName(PIcommand.VALUE).item(0))).getTextContent().trim();
        m_madeOnTheFly=false;
        m_formulaType=PIcommand.LATEX;
        m_backupCatalog=def.getBuildCatalog()+"/"+Definitions.GOOGLEBACKUP;
        convertToLocal(null);
        leaveMessage();
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
            // loosing color and Tex-size from command
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
            String path=convertToLocal(cmd);
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
         if((cmd!=null)&& cmd.paramExist(PIcommand.STYLE))
             imElt.setAttribute("style",cmd.getValue(PIcommand.STYLE));

         //convertToLocal();
         return imElt;
    }

    /**
     * URL-encode the string
     * @param S The string to encode
     * @return The encoded string (always utf-8)
     */
    private String cleanAndEncodeFormula(String S)
    {
        try{
            S=URLEncoder.encode(S, "utf-8");
        }
        catch(Exception ex){
            return S;
        }
        return S;
    }

    /**
     * Makes a local copy of google fomrula
     * (if google lets us down)
     */
    private String convertToLocal(PIcommand cmd)
    {
           String pathmodifier="";
           String tmp_formula_source=GOOGLECHARTURI;
           String tmp_formula=m_original_formula;
           if (cmd!=null)
           {
               String color=null;
               String backcolor=null;
               String size=null;
               if(cmd.paramExist(PIcommand.COLOR)){
                   color=cmd.getValue(PIcommand.COLOR);
                   if(color.startsWith("#"))
                       color=color.substring(1);
                   pathmodifier+=color;
               }
               if(cmd.paramExist(PIcommand.BACKCOLOR)){
                   backcolor=cmd.getValue(PIcommand.BACKCOLOR);
                  if(backcolor.startsWith("#"))
                       backcolor=backcolor.substring(1);
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
                   tmp_formula = size+tmp_formula;

               if(color!=null)
                   tmp_formula_source=tmp_formula_source+GOOGLECHARTCOLOR+color;

               if(backcolor!=null)
                   tmp_formula_source=tmp_formula_source+GOOGLEBACKGROUND+backcolor;
               
               String googleSource=tmp_formula_source+GOOGLELATEX+tmp_formula;
                googleSource=googleSource.replace(' ', '_');

                // store back up in scriptcatalog/googleformulas/<id>.png
                String path=m_backupCatalog+"/"+m_id+pathmodifier+".png";
                boolean done=accessutils.copyImageFromURI(googleSource, path);
                if(!done)
                {
                    System.out.println("could_not_copy_GoogleFormula: "+googleSource);
                    return null;
                }
                return path;

            }
        // pick up the source
        String googleSource=GOOGLECHARTURI+GOOGLELATEX+cleanAndEncodeFormula(m_original_formula);
        googleSource=googleSource.replace(' ', '_');

        // store back up in scriptcatalog/googleformulas/<id>.png
        String path=m_backupCatalog+"/"+m_id+".png";
        boolean done=accessutils.copyImageFromURI(googleSource, path);
        if(!done)
        {
            System.out.println("could_not_copy_GoogleFormula: "+googleSource);
            return null;
        }
        return path;
    }

    private void leaveMessage()
    {
       String msg="These files are used by WXT. They are copied from Google each time we buid.\n"+
                  "As in http://code.google.com/apis/chart/infographics/docs/formulas.html\n"+
                   "They are produced for inspection, and possible use as backup-images\n"+
                   "If you will do some work off-line or if Google abandons us (again)\n"+
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

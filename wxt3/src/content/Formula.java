package content;

import java.io.File;
import java.net.URI;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import utils.PIcommand;
import utils.accessutils;
import utils.domer;
import utils.reporter;
import utils.validationErrorHandler;
import wxt3.Scripthandler;

/**
 * Base class for holding formulas
 * ImageFormula, GoogleTexFormula,MathMLFormula
 * @author bs
 */
public class Formula
{
    // common to all formulas
    String m_id=null;
    String m_subtext=null;
    boolean m_madeOnTheFly=false;
    String m_formulaType="Formula";

    public Formula()
    {

    }

    /**
     * Constructing from a PI
     * @param mod The module that request the formula
     * @param cmd The command that describes the request
     * @throws Exception when we cannot make a formula
     */
    public Formula(Module mod, PIcommand cmd)
           throws Exception
    {
        m_id=cmd.getValue(PIcommand.ID);
        m_subtext=m_id;
        if(cmd.paramExist(PIcommand.SUBTEXT))
            m_subtext=cmd.getValue(PIcommand.SUBTEXT);
        m_madeOnTheFly=true;

    }

    /**
     * Constructing a formula from an element in a formulas file
     * @param elt
     * @throws Exception when we cannot construct a formula
     */
    public Formula(Element elt)
            throws Exception
    {
       //--- id
        if(elt.hasAttribute(PIcommand.ID))
        {
            m_id=elt.getAttribute(PIcommand.ID);
            m_subtext=m_id;//default
        }
        else
            throw new Exception("Formula need both id and value(tex string)");

       // --- subtext
        try{
            m_subtext=((Element)(elt.getElementsByTagName(PIcommand.SUBTEXT).item(0))).getTextContent().trim();
        }
        catch(Exception ex)
        {
             m_subtext=m_id;//default
        }

        m_madeOnTheFly=false;

    }

    public String getId(){ return m_id;}
    public String getSubText(){ return m_subtext;}
    public boolean madeOnTheFly(){return m_madeOnTheFly;}
    public String getFormulaType(){return  m_formulaType;}

   /**
    * Producing a div-element that will represent the absence of a formula
    * @param mod The Module making the request
    * @param param1 Not used
    * @param param2 Not used
    * @return The produced div-element
    */
    public Element getDisplayElement(Module mod,PIcommand cmd)
    {
         Element divElt=mod.getDoc().createElement("div");
         divElt.appendChild(mod.getDoc().createTextNode("basic formula"));
         return divElt;
    }

    /**
     * Getting the  code representing the formula
     * @param mod The Module making the request
     * @param color Not used
     * @param type Not used
     * @return null
     */
    public String getFormulaSource(Module mod){return null;}

    

    
    /**
     * Producing a tr element with two td-elements,
     * for the formula and the description and links to using modules
     * @param mod The module making the request
     * @param cmd The command dewscribing the request
     * @param users The modules using the formula
     * @return The prepared tr-element
     */
    public Element produceFormulaListElement(Module mod, PIcommand cmd,List<Module> users)
    {
        String type=null;
        String color=null;
        if(cmd.paramExist(PIcommand.COLOR))color=cmd.getValue(PIcommand.COLOR);
        if(cmd.paramExist(PIcommand.TYPE))type=cmd.getValue(PIcommand.TYPE);
 
       
        Element wrapper=mod.getDoc().createElement("li");
        wrapper.setAttribute("class", PIcommand.WXTSTYLEPREFIX+PIcommand.FORMULA);
        
        // description 
        Element nameDiv=mod.getDoc().createElement("div");
        nameDiv.setAttribute("class", PIcommand.TEXT);
        nameDiv.appendChild(mod.getDoc().createTextNode(m_subtext));
        wrapper.appendChild(nameDiv);
        
        // the formula
        wrapper.appendChild(getDisplayElement(mod, null));

        // pagelinks
        for(Module m:users)
        {
            Element elt=mod.getProducer().makeXLink(mod, m, "div", "link",mod.getProducer().NO_LEVEL_ADJUST,true);
            wrapper.appendChild(elt);
        }

        return wrapper;
    }


    //------------------------------------------------------
    // statics establishing formulas in definitions holder
    //------------------------------------------------------
    /**
     * Adding formulas from an texformulas file
     * @param theReporter Where to report
     * @param absUri Location of the file
     * @throws java.lang.Exception when we miss
     */
    public static void addFormulasFromFormulaFile(reporter theReporter,URI absUri,
                                               Scripthandler shandler,
                                               Definitions def)
            throws Exception
    {
        Document contentDoc=null;
        
        try
        {
            contentDoc=domer.makeDomFromUriSomeHow(absUri,null,theReporter);
            
            //validation the file
            String schemaString=shandler.getResourceHandler().getFormulaSchema();
            validationErrorHandler eHandler=new validationErrorHandler(true);
            String result=domer.validateDomFromString(contentDoc, schemaString, eHandler);
            String rep=eHandler.getReport();
            if(result!=null)
                theReporter.pushSimpleMessage("Texformulas validation failed:"+absUri.toString()+"\n");
            else
                theReporter.pushSimpleMessage("Texformulas validation:"+absUri.toString()+"\n"+rep+"\n");
        }
        catch(Exception ex1)
        {
            // cannot find or parse content.xml
            //System.out.println(ex1.getMessage());
            theReporter.pushMessage("cannot_inspect", absUri.getPath());
            throw new Exception("formulas not loaded");
        }

        // correct for possible catalog attribute
        // need this for image formulas
        if(contentDoc.getDocumentElement().hasAttribute("catalog"))
        {
            String srcCatStr=contentDoc.getDocumentElement().getAttribute("catalog");
            srcCatStr=def.substituteFragments(srcCatStr);
            //srcCatStr=srcCatStr.replace('\\', '/');
            //srcCatStr=srcCatStr.replaceAll(" ", "%20");
            // we must try to make sourceUri from the scrCatStr
            try{
                absUri=accessutils.makeAbsoluteURI(srcCatStr,absUri.getPath());
            }
            catch(Exception lex)
            {
                // anything we can do about this ?
                throw lex;
            }

         }


        NodeList flist=contentDoc.getElementsByTagName("formula");
        for(int ix=0;ix<flist.getLength();ix++)
        {
            // what type of formula: latex or image or mathml
            Element elt=(Element)flist.item(ix);

            String type=elt.getAttribute(PIcommand.TYPE);
            try{
                if(type.compareTo(PIcommand.LATEX)==0)
                {
                    LaTexFormula laf=new LaTexFormula(def,elt);
                    def.registerFormula(laf);
                }
                else if(type.compareTo(PIcommand.GOOGLE)==0)
                {
                   GoogleTexFormula gf=new GoogleTexFormula(def,elt);
                   def.registerFormula(gf);
                }
                else if(type.compareTo(PIcommand.IMAGE)==0)
                {
                   ImageFormula imf=new ImageFormula(elt,absUri);
                   def.registerFormula(imf);
                }
                else if(type.compareTo(PIcommand.MATHML)==0)
                {
                    MathMLFormula mf=new MathMLFormula(def,elt);
                    def.registerFormula(mf);
                }
                else
                {
                    // should not happen
                    continue;
                }
            }
            catch(Exception ex)
            {
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
    public static Formula makeFormulaFromSource(String source,Module mod,PIcommand cmd)
    throws Exception
    {
            Formula fma=null;
            if(source.indexOf("<math")!=-1)
            {
                // this must be mathml
                String id=cmd.getValue(PIcommand.ID);

                String subtext=id;
                if(cmd.paramExist(PIcommand.SUBTEXT))
                    subtext=cmd.getValue(PIcommand.SUBTEXT);
                fma=new MathMLFormula(mod.getDefinitions(),id,source,subtext,true);
                return fma;
            }
            // this must be latex
            cmd.setParameter(PIcommand.LATEX, source);
            fma=new LaTexFormula(mod,cmd);

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
    public static void makeMathMlFormulasFromOdt(reporter theReporter,URI theLoc,String tmpCat,Definitions def)
            throws Exception
    {
         // assume file-uri
        File f=new File(theLoc.getPath());
        //g_sourcecatalog=tmpCat;


        File outdir=accessutils.UnzipODT(theLoc,tmpCat);
        if(outdir==null)
        {
            theReporter.pushMessage("cannot_inspect", theLoc.getPath());
            throw new Exception("formulas not loaded");
        }

        // now we are ready to inspect and localize the formulas
        // we start by opening the content.xml file
        Document contentDoc=null;
        try{
            URI theUri=accessutils.makeUri("file:///"+tmpCat+"/"+"content.xml");
            //contentDoc=domer.makeDomFromUri(theUri);
            contentDoc=domer.makeDomFromUriSomeHow(theUri,null,theReporter);
        }
        catch(Exception ex1)
        {
            // cannot find or parse content.xml
            //System.out.println(ex1.getMessage());
            theReporter.pushMessage("cannot_inspect", theLoc.getPath());
            throw new Exception("formulas not loaded");
        }
        // start searching for formula ids and formulas
        // a formula id (text:p with content _fxxxxx)
        // will identify the first following text:p which contains a draw:object
        NodeList pList=contentDoc.getElementsByTagName("text:p");
        String fid=null;
        for(int ix=0;ix<pList.getLength();ix++)
        {
            Element pElt=(Element)pList.item(ix);
            String t=pElt.getTextContent().trim();
            if((t!=null) &&(t.startsWith("_f")))
            {
                fid=t;
                continue;
            }
            // do we have av draw:object in this text:p
            if(fid!=null)
            {
                NodeList dList=pElt.getElementsByTagName("draw:object");
                if(dList.getLength()>0)
                {
                    Element adElt=(Element)dList.item(0);
                    // does it have link to an object
                    if(adElt.hasAttribute("xlink:href"))
                    {
                        // get the address attribute in this element
                        String ad=adElt.getAttribute("xlink:href");
                        if(ad.startsWith("."))
                            ad=ad.replace(".", tmpCat);

                        //String result=storeFormula(fid,ad,theReporter,def);
                        ad=ad+File.separator+"content.xml";
                        ad=ad.replace("\\", "/");

                        String annotation="";
                        URI theUri=accessutils.makeUri("file:///"+ad);

                        // get the actual text
                        String mathS=accessutils.getTextFile(theUri, "utf-8");
                        if(mathS.indexOf("math:math xmlns:math")==-1)
                        {
                            // not a formula
                            theReporter.pushSimpleMessage(fid +" identifies no formula");
                            continue;
                        }
                        try{
                            MathMLFormula mf=new MathMLFormula(def,fid,mathS,null,false);
                            def.registerFormula(mf);
                        }
                        catch(Exception fex)
                        {
                            theReporter.pushSimpleMessage(fex.getMessage());
                            continue;
                        }
                    }
                }
            }
        }
    }


}

package content;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.PIcommand;
import utils.accessutils;
import utils.domer;
import utils.reporter;

/**
 * This class has the lifetime of scripthandler
 * It reads and stores formulas described in MathMl as found in
 * OpenOffice documents
 * @author bs
 */
public class ODTFormulas
{
    /** The stored formulas*/
    HashMap<String,DocumentFragment>m_Formulas;
    /** as we save the annotation  <span class=*/
    static final String MATH_ANNOTATION="math-annotation";
    /** as we wrap the formula  <div class=*/
    static final String FORMULA="formula";
    /** when we encounter a no math object*/
    static final String NO_MATH_OBJECT="not a math object";



    /**
     * Constructing a ODTFormulas object
     */
    public ODTFormulas()
    {
        m_Formulas=new HashMap<String,DocumentFragment>();
    }


    /**
     * Adding formulas from an odt-file
     * @param theReporter Where to report
     * @param theLoc Location of the file
     * @throws java.lang.Exception when we miss
     */
    public void addFormulasFromDocument(reporter theReporter,URI theLoc,String tmpCat)
            throws Exception
    {
        //g_absoluteUri=theLoc;
        // if this is a non-file URI we must download it and save temporay

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
            //URI theUri=new URI("file:///"+tmpCat.replace("\\", "/")+"/"+"content.xml");
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
                        String ad=adElt.getAttribute("xlink:href");
                        if(ad.startsWith("."))
                            ad=ad.replace(".", tmpCat);
                        String result=storeFormula(fid,ad,theReporter);

                        if(result!=null)
                            theReporter.pushMessage("cannot_store_formula", result);
                        else
                            fid=null; // used
                    }
                }
            }

        }
    }

    /**
     * access a formulafil, clean and store it
     * @param id The id we willl give it
     * @param absAdresse Where it is, relative to main catalog
     * @return null if everything is ok, some message otherwise
     */
    private String storeFormula(String id,String absAdresse,reporter theReporter)
    {
        // typically: ./object 1
        absAdresse=absAdresse+File.separator+"content.xml";
        absAdresse=absAdresse.replace("\\", "/");
        try{
            String annotation="";
            //URI theUri=new URI("file:///"+absAdresse.replaceAll(" ", "%20"));
            URI theUri=accessutils.makeUri("file:///"+absAdresse);
            //Document mathDoc=domer.makeDomFromUri(theUri);

            String mathS=accessutils.getTextFile(theUri, "utf-8");
            if(mathS.indexOf("xmlns:math")==-1)
            {
                // this is not a math object
                return NO_MATH_OBJECT;
            }
            // remove these 2 replaces ?
            mathS=mathS.replaceAll("math:", "");
            mathS=mathS.replace("xmlns:math","xmlns");

            Document mathDoc=domer.makeDomFromStringSomeHow(mathS,"utf-8",theReporter);

            NodeList mlist=mathDoc.getElementsByTagName("annotation");
            if(mlist.getLength()>0)
            {
                annotation=((Element)mlist.item(0)).getTextContent();
                mlist.item(0).getParentNode().removeChild(mlist.item(0));
            }

            DocumentFragment mdf=mathDoc.createDocumentFragment();

            //wrap it in a span element
            Element wrapper=mathDoc.createElement("div");
            wrapper.setAttribute("class", FORMULA);
            wrapper.appendChild(mathDoc.getDocumentElement());
            mdf.appendChild(wrapper);


            // without  span wrapper
            //mdf.appendChild(mathDoc.getDocumentElement());


            //add annotation as a span at end
            if(annotation.length()>0)
            {
                Element spanElt=mathDoc.createElement("span");
                spanElt.setAttribute("class", MATH_ANNOTATION);
                spanElt.appendChild(mathDoc.createTextNode(annotation));
                mdf.appendChild(spanElt);
            }
            
            
            if(m_Formulas.containsKey(id))
                return "duplicate id: "+id;

            m_Formulas.put(id, mdf);
            return null;

        }
        catch(Exception ex)
        {
            return ex.getMessage();
        }

        //System.out.println(id+" : "+absAdresse);

    }

    /**
     * Get a prestored formula
     * @param mod The Module that wants it
     * @param cmd The command that describes the request
     * @return A documentFragment
     */
    public DocumentFragment getFormula(Module mod,PIcommand cmd)
    {
        // we locate the formula in stored formulas m_Formulas
        // they have already been wrapped properly in a <div class=formula
        // see storeFormula
        DocumentFragment df=mod.getDoc().createDocumentFragment();

        String id="";
        if(cmd.paramExist(PIcommand.ID))
            id=cmd.getValue(PIcommand.ID);
        else
        {
            mod.getScriptHandler().getReporter().pushMessage("missing_id");
            return df;
        }
        if(!m_Formulas.containsKey(id))
        {
            mod.getScriptHandler().getReporter().pushMessage("unknown_id",id);
            df.appendChild(mod.getScriptHandler().getProducer().makeFailureComment(mod.getDoc(), cmd));
            return df;
        }


        // fixed
        boolean removeAnnotation=true;
        try{
            DocumentFragment mdf=m_Formulas.get(id);
            df.appendChild(mod.getDoc().importNode(mdf, true));

            if(removeAnnotation)
            {
                Node n=df.getFirstChild();
                while(n!=null)
                {
                    if(n.getNodeType()==Node.ELEMENT_NODE)
                    {
                        Element e=(Element)n;
                        if((e.getTagName().compareTo("span")==0)&&
                           (e.getAttribute("class").compareTo(MATH_ANNOTATION)==0))
                        {
                            n.getParentNode().removeChild(n);
                            n=null;
                        }
                        else
                           n=n.getNextSibling();
                    }
                    else
                        n=n.getNextSibling();
                }
            }
            return df;
        }
        catch(Exception ex)
        {
            mod.getScriptHandler().getReporter().pushMessage("formula-failed",id,ex.getMessage());
            df.appendChild(mod.getProducer().makeFailureComment(mod.getDoc(),cmd));
            return df;
        }
    }


    /**
     * get a formula from a single formula file
     * @param mod The module that wants it
     * @param cmd The command that descibes the request
     * @return A documentfragment
     */
    public DocumentFragment getFormulaDirect(Module mod,PIcommand cmd)
    {
        // where is it, we know that cmd has location
        String location=cmd.getValue(PIcommand.LOCATION);
        DocumentFragment df=mod.getDoc().createDocumentFragment();
        // this is relative to mod
        URI absoluteUri;
        try{
           absoluteUri = accessutils.makeAbsoluteURI(location, mod.getCatalog());
        }
        catch(Exception uex){
            mod.getReporter().pushMessage("bad_processing_instruction", cmd.toString(),uex.getMessage());
            df.appendChild(mod.getProducer().makeFailureComment(mod.getDoc(),cmd));
            return df;                      
        }
        // ok, we know where
        File f=new File(absoluteUri.getPath());
        String sourcecatalog=mod.getDefinitions().getTempCatalog();


        File outdir=accessutils.UnzipODT(absoluteUri,sourcecatalog);
        if(outdir==null)
        {
            mod.getReporter().pushMessage("cannot_inspect", absoluteUri.getPath());
            return df;
        }

        //get the correct content.xml - file
        Document contentDoc=null;
        try{
            //URI theUri=new URI("file:///"+sourcecatalog.replace("\\", "/")+"/"+"content.xml");
            URI theUri=accessutils.makeUri("file:///"+sourcecatalog+"/"+"content.xml");
            String mathS=accessutils.getTextFile(theUri, "utf-8");
            if(mathS.indexOf("xmlns:math")==-1)
            {
                // this is not a math object
                throw new Exception(NO_MATH_OBJECT);
            }
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

        //we have the single formula in content
        // remove annotation
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

package content;

import java.io.File;
import java.net.URI;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.PIcommand;
import utils.accessutils;
import utils.domer;

/**
 * This class administrates content from an Open Office text document
 */
public class xODTContent extends Content
{
    // if we use a local tempcatalog, which we dont
    static public final String TEMPCATALOG="_delete_me";
    // wrapping formulas with this classs
    static public final String FORMULA="formula";
    // unpack catalog
    static public final String ODTDOCS="odt_docs";
    // the unpacked catalog
    String targetCatalogStr;

    public xODTContent(Module owner,PIcommand cmd)
    throws Exception
    {
       super(owner,cmd);
    }


    /**
     * Produce a fragment
     * @param mod The module requesting the fragment
     * @param cmd The command describing the request
     * @return a fragment with the actual wikiaddress (uri) as childnode, null if no success
     */
    @Override
    public DocumentFragment getContent(Module mod,PIcommand cmd)
    {
        DocumentFragment df=mod.getDoc().createDocumentFragment();
        // unwrap and clean
        Document cDoc=unWrapAndCleanDocument(mod,cmd);
        if(cDoc==null)
        {
            return Content.getFailedContent(mod, cmd);
        }
        
        // we have the dom in a simplified xml form and are ready to extract

        DPath theDPath=null;
        // check for dpath
        theDPath=null;
        if(cmd.paramExist(PIcommand.DPATH))
        {
            String dpString=cmd.getValue(PIcommand.DPATH);
            try{
                theDPath=new DPath(dpString);
            }
            catch(Exception dex)
            {
                // we wanted dpath, but could not make it
                return Content.getFailedContent(mod, cmd);
            }
        }


        // check for xpath and use default if nothing else found
        String xpath="//body";
        if(cmd.paramExist(PIcommand.XPATH))
            xpath=cmd.getValue(PIcommand.XPATH);


        // wrap it
        Element tmpWrapper=mod.getDoc().createElement("div");
        tmpWrapper.setAttribute("class", cmd.getCommandStyleName());

        if(theDPath!=null)
        {
            tmpWrapper.appendChild(theDPath.getExtract(cDoc, mod, cmd));
        }
        else
        {
            // we use xpath, which is always set, defaults to //body
            try{
                NodeList list=domer.performXPathQuery(cDoc, xpath);
                if(list.getLength()==0)
                {
                   // we have not found anything
                   return Content.getFailedContent(mod, cmd);

                }
                for(int ix=0;ix<list.getLength();ix++)
                    tmpWrapper.appendChild(mod.getDoc().importNode(list.item(ix), true));

            }
            catch(Exception ex)
            {
                return null;
            }
        }

       df.appendChild(tmpWrapper);
       return df;


    }

    /**
     * Unpack and clean, establish unpackcatalog
     * @param mod The module requesting the data
     * @param cmd The command describing the request
     * @return A DOM or null if failed
     */
    private Document unWrapAndCleanDocument(Module mod,PIcommand cmd)
    {
         // where is it ?
        String sourcetUriStr=cmd.getValue(PIcommand.LOCATION);       
        sourcetUriStr=mod.getDefinitions().substituteFragments(sourcetUriStr);       
        URI absoluteSourceUri=null;
        try{
            absoluteSourceUri = accessutils.makeAbsoluteURI(sourcetUriStr, mod.getCatalog());
        }
        catch(Exception uex)
        {
            mod.getReporter().pushMessage("bad_processing_instruction", cmd.toString());
            return null;
        }
        // we know where it is

        // where should we unwrap it
        String tmpc=mod.getCatalog();
        targetCatalogStr="";
        if(tmpc.endsWith("/"))
            targetCatalogStr=tmpc+ODTDOCS;
        else
            targetCatalogStr=tmpc+"/"+ODTDOCS;
        String tmpf=sourcetUriStr;
        int ix=tmpf.lastIndexOf(".");
        if(ix!=-1)
            tmpf=tmpf.substring(0, ix);

        // we must unzip it and store it in  a temporary catalog
        targetCatalogStr=targetCatalogStr+"/"+accessutils.cleanStringForUseAsFilePath(tmpf);
        File f=new File(absoluteSourceUri.getPath());

        File targetCatalog=accessutils.UnzipODT(absoluteSourceUri,targetCatalogStr);
        if(targetCatalog==null)
        {
            mod.getReporter().pushMessage("cannot_inspect", absoluteSourceUri.getPath());
            return null;
        }
        //-------------------
        // we have unpacked the stuff in targetCatalog
        // we go for the main content.xml file, loads it and transforms it

        // get transformation and save it temporary, see Transformation constructor
        Transformation to=null;
        Document cDoc=null;
        String transString=mod.getScriptHandler().getResourceHandler().getOdtToWiki();
        try
        {
            String tmp=mod.getDefinitions().getTempCatalog();
            to=new Transformation("odt2wiki",transString,tmp);
            // register it
            mod.getDefinitions().addTransformation("odt2wiki", to);
        }
        catch(Exception tx)
        {
             mod.getReporter().pushMessage("cannot_inspect", absoluteSourceUri.getPath());
            return null;
        }

        // transform it
        try{
            //String tmp=targetCatalog.toString().replace('\\','/');
            //URI contentUri=new URI("file:///"+tmp.replaceAll(" ", "%20")+"/content.xml");
            URI contentUri=accessutils.makeUri("file:///"+targetCatalog.toString()+"/content.xml");
            cDoc=domer.makeTransformedDomFromUri(contentUri,
                    mod.getDefinitions().getTransformation("odt2wiki").getabsoluteURI(),
                    null);
            // TODO: when testing and want to see what the transformation produce:
            //String tst=domer.saveDomToString(cDoc, "utf-8", true, "xml");
            //String tst2="";
        }
        catch(Exception ex)
        {
             mod.getReporter().pushMessage("cannot_inspect", absoluteSourceUri.getPath());
             return null;
        }

        //--------------------------------------
        // we have the document describing the main content in cDoc
        // the document is in a simplified wiki-format.

        // everything odd appears as img since OO makes a display
        // of objects as some kind of image

        // we must take care of components that are unknown to wiki
        // formulas are among the objects that appear as image:
        //<img alt="Objekt1" class="image" src="./ObjectReplacements/Object 1" title="Objekt1">


        // we run all elements
        NodeList eList=cDoc.getDocumentElement().getElementsByTagName("*");
        for(int eix=eList.getLength()-1;eix>=0;eix--)
        {
            Element e=(Element)eList.item(eix);
            // we could do something with table,ul,a here
            // we only care for img
            if (e.getNodeName().compareTo("img")==0)
            {
                // we have a special interest in images
                // that serves as display elements (placeholders) for objects
                // Objectreplacements is a flag that tells us that this is not a simple image
                if((e.hasAttribute("src")&&(e.getAttribute("src").indexOf("ObjectReplacements")!=-1)))
                 {
                    // we must do something with this one
                    try{
                        // access the objects content.xml
                        String tmp=e.getAttribute("src").replace("./ObjectReplacements/", "")+"/content.xml";
                        //String srcFile=targetCatalog.toString().replace('\\','/')+"/"+tmp;
                        //URI srcUri=new URI("file:///"+srcFile.replace(" ", "%20"));
                        String srcFile=targetCatalog.toString()+"/"+tmp;
                        URI srcUri=accessutils.makeUri("file:///"+srcFile);

                        String src=accessutils.getTextFile(srcUri, "utf-8");
                        // formula in mathML ?
                        if(src.indexOf("xmlns:math")!=-1)
                        {
                            // this is where we could do a transform of formula to image
                            // and put the image ref in the file as well with an alternative
                            // class
                            
                            // prepare the formula and put it into the main content-file
                            src=src.replaceAll("math:", "");
                            src=src.replace("xmlns:math","xmlns");

                            Document formulaDoc=domer.makeDomFromStringSomeHow(src,null,mod.getReporter());
                            // we have the single formula in content
                            // remove annotation
                            NodeList alist=formulaDoc.getElementsByTagName("annotation");
                            if(alist.getLength()>0)
                            {
                                Node n=alist.item(0);
                                n.getParentNode().removeChild(n);
                            }
                            //wrap it
                            Element wrapElt=formulaDoc.createElement("div");
                            wrapElt.setAttribute("class",FORMULA);
                            wrapElt.appendChild(formulaDoc.getDocumentElement());
                            e.getParentNode().replaceChild(cDoc.importNode(wrapElt, true), e);
                        }
                        else
                        {
                            // is an object but not a formula
                            // and we dont know what to do with it yet
                            e.getParentNode().removeChild(e);
                        }
                    }
                    catch(Exception ex)
                    {
                        System.out.println(ex.getMessage());
                    }
                }
                else
                {
                    // we have an image, but not an object
                    // assume it is a normal image
                    // make it accessible from module
                    String origsrc=e.getAttribute("src");
                    try{
                        URI absUri=new URI("file://"+targetCatalogStr+"/"+origsrc);
                        URI relUri=accessutils.makeRelativeURI(mod.getAbsoluteUri(), absUri);
                        e.setAttribute("src", relUri.toString());
                    }
                    catch(Exception uex)
                    {
                        System.out.println(uex.getMessage());
                    }
                    
                }
             }
        }
        return cDoc;
    }


}

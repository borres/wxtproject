package xmldom;

import content.Module;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.parser.Parser;
import utils.PIcommand;
import utils.accessutils;

/**
 * Serves as an engine for HTMLContent and RemoteHTMLContent
 * @author Administrator
 */
public class JSoupEngine{
    static final String DEFAULT_ENCODING="UTF-8";
    
    // set up a dom from a string
    public static org.jsoup.nodes.Document makeSoupDoc(String source,String encoding){
        InputStream in = new ByteArrayInputStream(source.getBytes());
        if(encoding==null) {
            encoding=DEFAULT_ENCODING;
        }
        org.jsoup.parser.Parser par=Parser.xmlParser();//html ?
        try{
            org.jsoup.nodes.Document doc=Jsoup.parse(in,encoding,"",par);
            in.close();
            return doc;
        }
        catch(Exception ex){
            return null;
        }
    }
    
    // make a string from a doc
    public static String getSoupDocAsString(org.jsoup.nodes.Document doc,String encoding){
        if(encoding==null) {
            encoding=DEFAULT_ENCODING;
        }        
        doc.outputSettings(makeOutSettings(encoding));
        String result=doc.toString();
        result=cleanText(result);
        return result;
    }
    
    
   // getting contents by a CSS-selector in a file
    public static String findElementsWithSelector(org.jsoup.nodes.Document doc ,
            String selector,
            boolean keepRefs,
            String encoding){
       if(encoding==null) {
            encoding=DEFAULT_ENCODING;
        }
       String wrapper="<!doctype html><head><meta charset=\""+encoding+"\"/></head><body><div id=\"found\"></div></body></html>";
       // or HTML-parser
       try{
           org.jsoup.select.Elements divs=doc.select(selector);
           
           org.jsoup.nodes.Document wrapperdoc=makeSoupDoc(wrapper,encoding);            
           org.jsoup.nodes.Element wrapperElt=wrapperdoc.select("#found").first();
            // wrap it in the wrapper div
            for (Element d : divs) {
                wrapperElt.appendChild(d);
            }
            
            // clean things suited for JSoup here
            cleanResult(wrapperElt,keepRefs);
            
            
            wrapperdoc.outputSettings(makeOutSettings(encoding));
            //System.out.println(wrapperdoc);
                        
            doc.outputSettings(makeOutSettings(encoding));
            String result="";
            for(Element d : divs) {
               result+=d.toString();
           }
           result=cleanText(result);
           // while debugging:System.out.println(result);
            return result;

         }
        catch(Exception ex){
            System.out.println(ex.toString());
            return null;
        }
    }
    
     /**
     * produce backup content, saving and referring images in backupcatalog
     * @param mod The module that request the content
     * @param cmd The PIcommand describing the request
     * @param doc The dom to store
     * @param absBackupCat The catalog containing the backup
     * @throws java.lang.Exception When bad adressing of location or transformation     
     */
    public static boolean makeBackup(Module mod, PIcommand cmd, 
            org.jsoup.nodes.Document doc,
            URI absBackupCat){
        
        if (absBackupCat == null) {
            return false;
        }
        // make the catalog
        new File(absBackupCat.toString()).mkdirs();

        // run through the material and prepare all img-tags for
        // "paralell" save of images, and save images
        org.jsoup.select.Elements imList=doc.getElementsByTag("img");
        for(Element imElt : imList){
            String src = imElt.attr("src");
            // we must make sure it is absolute
            try {
                URI testAbs = accessutils.makeUri(src);
                if (!testAbs.isAbsolute()) {
                    String fromCat = cmd.getValue(PIcommand.LOCATION);
                    fromCat = accessutils.removeFilePartFromPathstring(fromCat);
                    testAbs = accessutils.makeAbsoluteURI(src, fromCat);
                    //testAbs = accessutils.makeAbsoluteURI(src, mod.getCatalog());
                    src = testAbs.toString();
                }
            } catch (Exception ex) {
                continue;
            }
            String newFileName;
            URI absImage;
            int pos = src.lastIndexOf("/");
            if (pos != -1) {
                newFileName = src.substring(pos + 1);
                newFileName = accessutils.cleanFilePath(newFileName);
                // move file to this new filename
                File f = new File(absBackupCat.getPath());
                boolean imageCopied = accessutils.copyImageFromURI(src, f.getPath() + "/" + newFileName);
                if (!imageCopied) {
                    mod.getReporter().pushMessage("could_not_copy_image", src);
                } else {
                    // correct adressing by simple putting them
                    // in the catalog
                    try {
                        //absImage = accessutils.makeUri(absBackupCat.toString() + "/" + newFileName);
                        //URI relImage = accessutils.makeRelativeURI(mod.getAbsoluteUri(), absImage);
                        //imElt.attr("src", relImage.toString());
                        imElt.attr("src", newFileName);

                    } catch (Exception ex) {
                        // do nothing: leave absolute image path as was
                    }
                }

            }
        }

        // produce and save the material
        String encoding=mod.getEncoding();
        if(encoding==null) {
            encoding=DEFAULT_ENCODING;
        }
        try {
            doc.outputSettings(makeOutSettings(encoding));
            String s=doc.toString();
            URI saveAt=accessutils.makeUri(absBackupCat.toString() + "/content.html");
            accessutils.saveTFile(saveAt, s, encoding);

        } catch (Exception ex) {
            System.out.println("should not happen: " + ex.getMessage());
            return false;
        }

        try {
            //URI tmpUri=new URI("file:///"+result.getPath().replace('\\', '/')+"/readme.txt");
            URI tmpUri = new URI(absBackupCat.toString() + "/readme.txt");
            String T = "This directory is used (and reused) for storing back up of HTMLpages. \n"
                    + "WXT will produce a catalog for each file that is used as contentprovider\n"
                    + "The main file is found in content.html\n"
                    + "Images are referenced directly from the built Module and should not be removed";
            accessutils.saveTFile(tmpUri, T, encoding);
        } catch (Exception uex) {
            //should not happen
            System.out.println(uex.getMessage());
        }
        return true;       
    }
    
    /**
     * read and parse backup content
     * @param mod The module that request the content
     * @param cmd The PIcommand describing the request
     * @param absBackupCat The catalog containing the backup
     * @throws java.lang.Exception When bad adressing of location or transformation     
     */
    public static org.jsoup.nodes.Document retrieveBackup(Module mod, PIcommand cmd,URI absBackupCat){
         try {

            //URI absBackupCat = makeBackupCatalog(mod, cmd);
            //URI contentUri=new URI(absBackupCat.toString()+"/content.xml");
            URI contentUri = accessutils.makeUri(absBackupCat.toString() + "/content.html");
            String encoding = mod.getEncoding();
            if (cmd.paramExist(PIcommand.ENCODING)) {
                encoding = cmd.getValue(PIcommand.ENCODING);
            }
            String source=accessutils.getTextFile(contentUri, encoding);
            org.jsoup.nodes.Document doc = makeSoupDoc(source,encoding);
            return doc;
        } catch (Exception rex) {
            System.out.println("soup:retrieve says: "+rex.getMessage());
            return null;
        }
       
    }
    
     /**
     * Do basic cleaning and remove references if so wanted
     * 
     * @param rootElt The element with the selected elements as children
     * @param keepReferences Trur is we want to keep references, False otherwise
     */
    private static  void cleanResult(Element rootElt, boolean keepReferences) {
        
        // clean away  stuff which are not wanted anyhow        
        org.jsoup.select.Elements aList;
        
        // remove the header element
        // no because that may be what the user wants
        /*aList=rootElt.getElementsByTag("head");
        for(Element a : aList){
            a.remove();
        }*/
        
        // remove locals refs
        aList=rootElt.getElementsByTag("a"); 
        for(Element a : aList){
            // remove anchors
            if (a.hasAttr("name")){
                a.remove();
            }
            // remove local refs
            if (a.hasAttr("href")) {
                if (a.attr("href").startsWith("#")) {
                    a.remove();
                }
            }
        }
       
        // only keep scripts with absolute address and scripts without src-attribute
        aList=rootElt.getElementsByTag("script");
        for(Element a : aList){
            if(a.hasAttr("src")){
                String tmp=a.attr("src");
                try{
                    URI adr=new URI(tmp);
                    if(!adr.isAbsolute()) {
                        a.remove();
                    }                     
                }
                catch(Exception ex){
                    a.remove();
                }              
            }
        }        
        
        // remove TOCs
        aList=rootElt.getElementsByTag("table");
        for(Element a : aList){
            if((a.hasAttr("id"))&& (a.attr("id").equals("toc"))) {
                a.remove();
            }
        }
        
        aList=rootElt.getElementsByTag("div");
        for(Element a : aList){
            if((a.hasAttr("class"))&& (a.attr("class").equals("magnify"))) {
                a.remove();
            }
        }
        
        // remove edit entries in wiki(pedia)
        aList=rootElt.getElementsByTag("span");
        for(Element a : aList){
            if((a.hasAttr("class"))&& (a.attr("class").indexOf("edit")!=-1)) {
                a.remove();
            }
        }
        

       
    }

    
    
    private static Document.OutputSettings makeOutSettings(String encoding){
        Document.OutputSettings outSet=new Document.OutputSettings();
        outSet.charset(encoding);
        outSet.escapeMode(Entities.EscapeMode.xhtml);
        outSet.prettyPrint(true);
        return outSet;
    }
    
    private static String cleanText(String T){
        // remove wrapping of xml-element and PIs
        // do we expose other elements ?
        T=T.replaceAll("<!--\\?", "<\\?").replaceAll("\\?-->", "\\?>");
        // following is set as entities:
        // lt, gt, amp, apos, quot
        T=T.replaceAll("&quot;", "\"");
        T=T.replaceAll("&apos;", "'");
        //T=T.replaceAll("&amp;", "&");
        return T;
    }

    
}
    

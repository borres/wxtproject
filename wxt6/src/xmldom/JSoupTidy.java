/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xmldom;

import java.net.URI;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import reporting.reporter;
import utils.accessutils;

/**
 *
 * @author Administrator
 */
public class JSoupTidy implements ITidyXML{
    String m_defaultEncoding;
   
    public JSoupTidy(String encoding){       
        if(encoding==null) {
            m_defaultEncoding="UTF-8";
        }
        else {
            m_defaultEncoding=encoding;
        }
    }
    
    @Override
    public String tidyAndMakeXML(String source, String encoding, reporter theReporter) 
            throws Exception {
         if(encoding==null) {
                    encoding=m_defaultEncoding;
         }
         try{
            org.jsoup.nodes.Document doc=org.jsoup.Jsoup.parse(source);

            doc.outputSettings(makeOutSettings(encoding));
            String result;
            // if we have inserted an artificial root node, we are only interested
            // in the fragment (nodes) inside ( will ignore possible xml-header and
            // head and body tags
            // usage: domer.produceFragementFromString
            org.jsoup.select.Elements elts=doc.getElementsByTag(domer.WXTROOT);
            if(elts.size()>0){
                result=elts.first().toString();
            }           
            else{
                result= doc.toString();
            } 
            result=cleanText(result);        
            return result;           
        }
        catch(Exception ex){
            System.out.println(ex.toString());
            return "error: "+ex.getMessage();
        }     

    }

    @Override
    public String tidyAndMakeXML(URI theuri, String encoding, reporter theReporter) 
            throws Exception {
         try{
             //String source=getTextFile(theuri, encoding);
             String source=accessutils.getBOMSafeTextFile(theuri, encoding);
             theReporter.pushMessage("attempt_to_tidy");
             return tidyAndMakeXML(source,encoding,theReporter);
         }
         catch(Exception ex)
         {
             throw new Exception(ex.getMessage());
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

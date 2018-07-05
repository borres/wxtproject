package wxttesting;

import filing.readwrite;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.parser.Parser;
import org.jsoup.parser.Tag;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Administrator
 */
public class testHTML {
    
    // if we read a file with XML-header
    // this header will be formed as <!--? ?-->
    // same with PIs
    // could we handle this with a strait replace ?
    // or could we do something with doc ?
    public static String parse(String fname,String encoding){
          try{
            String s=readwrite.getTextFile(fname);
            org.jsoup.nodes.Document doc=Jsoup.parse(s);

            
            OutputSettings outSet=new OutputSettings();
            outSet.charset(encoding);
            outSet.escapeMode(Entities.EscapeMode.xhtml);
            outSet.prettyPrint(false);
            doc.outputSettings(outSet);
            
            String result= doc.toString(); 
            result=result.replaceAll("<!--\\?", "<\\?").replaceAll("\\?-->", "\\?>");
            // following is set as entities:
            // lt, gt, amp, apos, quot
            result=result.replaceAll("&quot;", "\"");
            result=result.replaceAll("&apos;", "'");
            result=result.replaceAll("&amp;", "&");
            return result;           
        }
        catch(Exception ex){
            System.out.println(ex.toString());
            return "error: "+ex.getMessage();
        }     
    }
    
    public static String tidyAndMakeXML(String S, String encoding){
      Parser par=Parser.xmlParser();
      try{
            // load the wrapper from text above
            InputStream in = new ByteArrayInputStream(S.getBytes());
            org.jsoup.nodes.Document wrapperdoc=Jsoup.parse(in,encoding,"",par);
            in.close();            
            
                        
            OutputSettings outSet=new OutputSettings();
            outSet.charset("UTF-8");
            outSet.escapeMode(Entities.EscapeMode.xhtml);
            outSet.prettyPrint(true);
            wrapperdoc.outputSettings(outSet);
            
            String res=wrapperdoc.toString();
            // clean string
            res=res.replaceAll("<!--\\?", "<\\?").replaceAll("\\?-->","\\?>");
            res=res.replaceAll("&quot;", "\"");
            res=res.replaceAll("&apos;", "'");
            res=res.replaceAll("&amp;", "&");

           
            return res;
         }
        catch(Exception ex){
            System.out.println(ex.toString());
            return null;
        }

    }

    // getting contents by a CSS-selector in a file
    public static String findElementsWithSelector(String fname,String selector){
       String wrapper="<!doctype html><head><meta charset=\"UTF-8\"/></head><body><div id=\"found\"></div></body></html>";
       Parser par=Parser.xmlParser();
       try{
            // load the wrapper from text above
            InputStream in = new ByteArrayInputStream(wrapper.getBytes());
            org.jsoup.nodes.Document wrapperdoc=Jsoup.parse(in,"UTF-8","",par);
            in.close();            
            
            org.jsoup.nodes.Element wrapperElt=wrapperdoc.select("#found").first();
            
            // load the file we want to extract from
            String s=readwrite.getTextFile(fname);
            in=new ByteArrayInputStream(s.getBytes());
            org.jsoup.nodes.Document doc=Jsoup.parse(in,"UTF-8","",par);
            in.close();

            // get a list of elements according to selector
            org.jsoup.select.Elements divs=doc.select(selector);
            
            // wrap it in the wrapper div
            for (Element d : divs) {
                wrapperElt.appendChild(d);
            }            
            //System.out.println(collector); 
            System.out.println(wrapperdoc);
                        
            OutputSettings outSet=new OutputSettings();
            outSet.charset("UTF-8");
            outSet.escapeMode(Entities.EscapeMode.xhtml);
            outSet.prettyPrint(true);
            wrapperdoc.outputSettings(outSet);

            return wrapperdoc.toString();
         }
        catch(Exception ex){
            System.out.println(ex.toString());
            return null;
        }
    }


     // reading from file
    public static void testACleanXMLFile(String fname){
      try{
           String s=readwrite.getTextFile(fname);
           org.jsoup.nodes.Document doc=Jsoup.parse(s);
            org.jsoup.select.Elements wines = doc.getElementsByTag("wine");
            for (Element w : wines) {
                org.jsoup.nodes.Attributes ats=w.attributes();
                for(org.jsoup.nodes.Attribute a:ats){
                    String Name=a.getKey();
                    String val=a.getValue();
                    //System.out.println(Name+" : "+val); 
                }
           
            } 
            System.out.println(doc.toString());
        }
        catch(Exception ex){
            System.out.println(ex.toString());
        }
    }
    
 }

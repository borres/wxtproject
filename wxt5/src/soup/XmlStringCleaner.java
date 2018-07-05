package soup;

import java.net.URI;
import org.jsoup.Jsoup;
import reporting.reporter;
import utils.accessutils;

/**
 * Takes a string or an uri or a file 
 * parse/clean it as xml 
 * undress hidden <??> tags and return a string
 * @author bs
 */
public class XmlStringCleaner {
   /** default encoding */
    String m_default_encoding="utf-8";
    /** reporter to use */
    reporter m_reporter=null;
    
    /**
     * constructor
     * @param encoding Default encoding
     * @param rep default reporter
     */
    public XmlStringCleaner(String encoding,reporter rep){
        m_default_encoding=encoding;
        m_reporter=rep;
    }
    
    /**
     * Change reporter
     * @param rep New default reporter 
     */
    public void setReporter(reporter rep){
        m_reporter=rep;
    }
    
       
    /**
     * produce an Xml-string from a given source string
     * @param source The source
     * @param encoding The encoding to use
     * @return An Xml-string
     * @throws Exception 
     */
    public String makeXmlStringFromString(String source,String encoding)
    throws Exception{
        if(encoding==null)
            encoding=m_default_encoding;
        org.jsoup.nodes.Document doc=
                Jsoup.parse(source,encoding,org.jsoup.parser.Parser.xmlParser());
        // alternative is htmlParser
       doc.outputSettings()
                        .charset(encoding)
                        .prettyPrint(true)
                        .indentAmount(4)
                        .escapeMode(org.jsoup.nodes.Entities.EscapeMode.xhtml);      
        // fix xml-header and  reestablish PI's
        String T=doc.toString().replaceAll("<!--\\?", "<\\?").replaceAll("\\?-->","\\?>");
        // some fuzz in headerelement with PI and script
        // necessary when using htmlparser:
        //T=T.replaceAll("&lt;\\?", "<\\?").replaceAll("\\?&gt;","\\?>");
        // quotes in scripttags ?:
        T=T.replaceAll("&quot;", "\"");        
        return T;

     }
    


     /**
     * produce an Xml-string from an uri
     * @param theuri The uri we will load from, can be file ?
     * @param encoding The encoding we will use
     * @return An Xml-string
     * @throws Exception 
     */
    public  String makeXmlStringFromUri(URI theuri,String encoding)
     throws Exception{
         if(encoding==null)
             encoding=m_default_encoding;
         try{
             // make it BOMsafe
             String source=accessutils.getBOMSafeTextFile(theuri, encoding);
             m_reporter.pushSimpleMessage("--cleaning :"+theuri.toString());
             return makeXmlStringFromString(source,encoding);
         }
         catch(Exception ex)
         {
             throw new Exception(ex.getMessage());
         }
     }


     /**
     * Produce an Xml-string from an absolute filepath
     * @param path The filepath
     * @param encoding The encoding
     * @return an Xml-string
     * @throws Exception 
     */
    public  String makeXmlStringFromFile(String path,String encoding)
     throws Exception{
        if(encoding==null)
             encoding=m_default_encoding;
         try{
             // make it BOM safe
             URI theuri=accessutils.makeUriFromAbsoluteFilePath(path);
             String source=accessutils.getBOMSafeTextFile(theuri, encoding);
             m_reporter.pushSimpleMessage("--cleaning :"+theuri.toString());
             return makeXmlStringFromString(source,encoding);
         }
         catch(Exception ex)
         {
             throw new Exception(ex.getMessage());
         }
     }
 

}

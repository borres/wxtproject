package tidying;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.Properties;
import java.util.Set;
import org.w3c.tidy.Tidy;
import reporting.reporter;
import utils.accessutils;

/**
 *
 * @author BS
 * See:
 * http://tidy.sourceforge.net/docs/quickref.html
 * http://jtidy.sourceforge.net/howto.html
 * http://www.w3.org/People/Raggett/tidy/
 * http://www.webmasterworld.com/forum46/88.htm
 */
public class tidyEngine 
{
    /** absolute URI for user supplied attributes*/
    URI m_attributeUri=null;
    /** default encoding */
    String m_default_encoding="utf-8";
    /** the tidy object */
    Tidy m_Tidy=null;
    /** name of stringbundle that holds all strings */
    static String attBundle="tidyAttributes";
    /** reporter to use */
    reporter m_reporter=null;
    
   

    public tidyEngine(String default_encoding,URI uri,reporter rep)
    {
        m_default_encoding=default_encoding;
        m_attributeUri=uri;
        m_reporter=rep;
        m_Tidy=null;
    }  
    
    /**
     * Set up the tidy object with attributes from
     * - std attributes as in properties (tidyAttributes.properties)
     * or
     * - attributes from file (m_attributeUri)
     */
    private void setUpTidy()
    {
        m_Tidy=new Tidy();
        
        // allways:
        m_Tidy.setInputEncoding(m_default_encoding);
        m_Tidy.setOutputEncoding(m_default_encoding);
        m_Tidy.setOnlyErrors(false);        
        m_Tidy.setShowWarnings(false);
        
        m_Tidy.setXmlOut(true);
        m_Tidy.setNumEntities(false);
               
        Properties Props=new Properties();
        if(m_attributeUri!=null)
        {
            // from file 
            // pick up users file and use it
            try{
                String tmp=accessutils.getBOMSafeTextFile(m_attributeUri, m_default_encoding);
                String[]atts=tmp.split("\n");
                for(String a : atts)
                {
                    if((a.length()< 3)||(a.trim().startsWith("//"))||(a.trim().startsWith("#")))
                        continue;
                    String[] parts=a.split(":");
                    if(parts.length!=2)
                    {
                        m_reporter.pushSimpleMessage("Bad tidy attribute line: "+a);
                        continue;
                    }
                    Props.put(parts[0].trim(), parts[1].trim());
                }
            }
            catch(Exception ex)
            {
                m_reporter.pushSimpleMessage(ex.getMessage());
            }            
        }
        else
        {
            // attempt some std/adaptive/default settings based on properties
            java.util.ResourceBundle R=
                    java.util.PropertyResourceBundle.getBundle(attBundle);
            Set<String>Keys=R.keySet();
            for(String s : Keys)
                Props.put(s, R.getString(s));          
        }
        m_Tidy.setConfigurationFromProps(Props); 
        
    }
     
    /**
      * Tidy a string 
      * @param source The string to tidy
      * @param encoding The encoding we use
      * @param theReporter The reporter we will report to
      * @return The cleaned (tidied) string
      * @throws java.lang.Exception when cannot do it
      */
     public  String tidyAndMakeXML(String source,String encoding,reporter theReporter)
     throws Exception
     {
         if(encoding==null)
             encoding=m_default_encoding;
        source=source.trim();
        while( (!source.startsWith("<")) && (source.length()>10) )
              source=source.substring(1);           //StringBufferInputStream sbis=new StringBufferInputStream(source);
        ByteArrayInputStream bis=new ByteArrayInputStream(source.getBytes());
        ByteArrayOutputStream bos=new ByteArrayOutputStream();

        if(m_Tidy==null)
            setUpTidy();        
        
        //theReporter.pushSimpleMessage("\tWarning: --Tidy ");

        boolean reportFromTidy=true;
                
        if(reportFromTidy)
        {
            StringWriter sw=new StringWriter();
            PrintWriter pr=new PrintWriter(sw);        
            m_Tidy.setErrout(pr);

            m_Tidy.setQuiet(true);
            
            m_Tidy.parse(bis, bos);
            
            String report=sw.toString();
            String[]lines=report.split("\n");
            for(int ix=0;ix < lines.length;ix++)
            {
                String line=lines[ix];
                // replace to avoid message confusion for artificial <root> in
                // production of xml from fragemnt
                // see domer:produceDocFragmentFromString()
                line=line.replace("<root>", "rootnode");
                if(line.indexOf("Error")!=-1)
                    theReporter.pushSimpleMessage("\tTidy:"+line);
                //if((line.indexOf("Warning")!=-1) ||(line.indexOf("Error")!=-1))
                //    theReporter.pushSimpleMessage(line);
            }

        }
        else
        {
            m_Tidy.parse(bis, bos); 
        }
        source=bos.toString();
        source=source.trim();

        // add xml prolog if necessary
        if(!source.startsWith("<?xml"))
        {
            String pro="<?xml version=\"1.0\" encoding=\""+encoding+"\"?>\n";
            source=pro+source;
        }
        return source;
     }
     
     
     /**
      * Tidy the string read from an url 
      * @param theuri The uri to read from
      * @param encoding The encoding we use
      * @param theReporter The reporter we will report to
      * @return The cleaned (tidied) string
      * @throws java.lang.Exception when cannot do it
      */
     public  String tidyAndMakeXML(URI theuri,String encoding,reporter theReporter)
     throws Exception{
         try{
             //String source=getTextFile(theuri, encoding);
             String source=accessutils.getBOMSafeTextFile(theuri, encoding);
             theReporter.pushSimpleMessage("--Tidy :"+theuri.toString());
             return tidyAndMakeXML(source,encoding,theReporter);
         }
         catch(Exception ex)
         {
             throw new Exception(ex.getMessage());
         }
     }
    
}

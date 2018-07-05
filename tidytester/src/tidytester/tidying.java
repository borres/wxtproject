package tidytester;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import org.w3c.tidy.Tidy;

/**
 *
 * @author BS
 * see: http://www.w3.org/People/Raggett/tidy/
 *      http://tidy.sourceforge.net/
 */
public class tidying {
    static final String default_encoding="utf-8";
    // attribute file if any
    String attributeFile=null;
    /**the Locale for this reporter*/
    static Locale currentLocale =new Locale(System.getProperty("user.language"));
    /** the stringbundle that holds all strings */
    static String attBundle="Tidytester/attributes";   
    
    public tidying(String attFile)
    {
        attributeFile=attFile;

    }
    
     /**
      * Tidy a string 
      * @param source The string to tidy
      * @param encoding The encoding we use
      * @param theReporter The reporter we will report to
      * @return The cleaned (tidied) string
      * @throws java.lang.Exception when cannot do it
      */
    private String doTidy(String source,String encoding)
     throws Exception
     {
         // http://jtidy.sourceforge.net/howto.html
         if(encoding==null)
             encoding=default_encoding;
        source=source.trim();
        while( (!source.startsWith("<")) && (source.length()>10) )
              source=source.substring(1);           
        ByteArrayInputStream bis=new ByteArrayInputStream(source.getBytes());
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        
        
        
        //theReporter.pushSimpleMessage("\tWarning: --Tidy ");

        Tidy tidy=new Tidy();
        
        // allways:
        tidy.setInputEncoding(encoding);
        tidy.setOutputEncoding(encoding);
        tidy.setOnlyErrors(false);        
        tidy.setShowWarnings(true); 
       
        attributeFile=null;
        if(attributeFile!=null)
        {

            tidy.setXmlOut(true);
            tidy.setNumEntities(false);
            tidy.setIndentContent(true);  
            // rely on users config file
            // pick up users file and use it
            tidy.setConfigurationFromFile(attributeFile);
            
        }
        else
        {
            // attempt some std/adaptive/default settings based on properties
            Properties Props=new Properties();
            java.util.ResourceBundle R=
                    java.util.PropertyResourceBundle.getBundle(attBundle);
            Set<String>Keys=R.keySet();
            for(String s : Keys)
            {
                Props.put(s, R.getString(s));
            }
            tidy.setConfigurationFromProps(Props);   
        }


        boolean reportFromTidy=true;                
        if(reportFromTidy)
        {
            StringWriter sw=new StringWriter();
            PrintWriter pr=new PrintWriter(sw);        
            tidy.setErrout(pr);

            tidy.setQuiet(true);
            
            tidy.parse(bis, bos);
            
            String report=sw.toString();
            String[]lines=report.split("\n");
            for(int ix=0;ix < lines.length;ix++)
            {
                // filter some warnings here
                String line=lines[ix];
                System.out.println(line);// ???????
            }            
        }
        else
        {
            tidy.parse(bis, bos); 
        }
        
        String result=bos.toString();
        
        //System.out.print(result);
        
        // add xml prolog if necessary
        if(!result.startsWith("<?xml"))
        {
            String pro="<?xml version=\"1.0\" encoding=\""+encoding+"\"?>\n";
            result=pro+result;
        }
        return result;
     }
     

    
    public void doTheJob(String inFile,String outFile)
    {
        if(attributeFile!= null)
            System.out.println(inFile+" -> "+outFile+" with "+attributeFile);
        else
            System.out.println(inFile+" -> "+outFile);
        URI inUri=null;
        URI outUri=null;
        try{
            if(!inFile.startsWith("http"))
                inFile="file:///"+inFile;
            inUri=filing.makeUri(inFile);
            outUri=filing.makeUri("file:///"+outFile);
        }
        catch(Exception ex1)
        {
            System.out.println(ex1.getMessage());
        }
        String src,result;
        try{
            src=filing.getTextFile(inUri, "UTF-8");
            // do the job
            result=doTidy(src,"UTF-8");
            filing.saveTFile(outUri, result, "UTF-8");
        }
        catch(Exception ex2)
        {
            System.out.println(ex2.getMessage());
        }
    }
    
}

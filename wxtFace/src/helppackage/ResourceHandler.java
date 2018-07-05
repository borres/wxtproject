package helppackage;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;

/**
 * This class access the following files:
 * - resources/version.txt. Packed in jarfile
 * - wxtrunneversion.txt. as distributed
 * @author Administrator
 */
public class ResourceHandler {


    static public final String DIST_FACE_VERSION_ADDRESS=
            "http://www.it.hiof.no/wxt/wxtsite/wxt/wxtrunversion.txt";
    static public final String FACE_DOC_ADDRESS=
            "http://www.it.hiof.no/wxt/wxtsite/wxt/gui.html";
    static public final String WXT_DOC_ADDRESS=
            "http://www.it.hiof.no/wxt/wxtsite/wxt/documentation.html";
    static public final String WXT_PROJECT_HOME=
            "http://www.it.hiof.no/wxt/wxtsite/";

    

    /**
     * Loading a resource from the jar
     * @param path The path to load
     * @return The string loaded
     * @throws java.lang.Exception when something goes wrong
     */
    private static String loadResource(String path)
    throws Exception{
        InputStream in=ResourceHandler.class.getResourceAsStream(path);
        if(in!=null)
        {
            Reader reader=null;
            try{
                reader = new BufferedReader(new InputStreamReader(in));
                StringBuffer buf=new StringBuffer();
                int ch;
                while ((ch = reader.read()) > -1) {
                    buf.append((char) ch);
                }
                reader.close();
                String t=buf.toString();
                return t;
            }
            catch(Exception e)
            {
                if(reader!=null)
                    reader.close();
                throw new Exception();
            }
        }
        else
        {
            return null;
        }
    }

     /**
      * Read a String from an URI.
      *
     * @param uri The URI to read from
     * @return the string read
     * @throws Exception when the text cannot be loaded or the URI is not wellformed
     */
    private static String getTextFile(URI uri,String encoding)
    throws Exception{
        URL url=uri.toURL();
        if(encoding == null) {
            encoding="utf-8";
        }
        // while testing: System.out.println(url.toString());
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(),encoding));
        StringBuffer result=new StringBuffer();
        String str;
        //String eol=System.getProperty("line.separator");
        String eol="\n";
        while ((str = in.readLine()) != null) {
            result=result.append(str+eol);
        }
        in.close();

        return result.toString();
    }


    /**
     * Get the version string: v.yyyy.mm.dd
     * @return The version as a string
     */
    public static String getVersion()
    {
       try{
            String s=loadResource("/versioning/version.txt").trim();
            return s;
        }
        catch(Exception ex)
        {
            return null;
        }    
    }
    
    public static String getFaceDocHome(){
       return FACE_DOC_ADDRESS;          
    }

    public static String getEngineDocHome(){
       return WXT_DOC_ADDRESS;           
    }
    
    public static String getWXTProjectHome(){
        return WXT_PROJECT_HOME;
    }

    /**
     * Get version at download page
     */
    public static String getLatestDistVersion()
    {
       try{
            URI theUri=new URI(DIST_FACE_VERSION_ADDRESS);
            return getTextFile(theUri, null).trim();
         }
        catch(Exception ex)
        {
            return null;
        }
    }


   
}

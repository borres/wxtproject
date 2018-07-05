package wxt2utils;

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
*/
public class resourceHandler
{
    /** resources, kept for reload */
    private String m_version;
    private String m_latestVersion;
    private String m_latestWxtVersion;

    private final String DIST_VERSION_ADDRESS=
            "http://www.it.hiof.no/wxt/wxtsite/wxt/wxtrunversion.txt";
    private final String DIST_WXT_VERSION_ADDRESS=
            "http://www.it.hiof.no/wxt/wxtsite/wxt/wxtversion.txt";

    /**
     * Constructing a resourceHandler
     * @param rep A reporter to report errors to
     */
    public resourceHandler()
    {
        m_version=null;
        m_latestVersion=null;
        m_latestWxtVersion=null;
    }

    /**
     * Loading a resource from the jar
     * @param path The path to load
     * @return The string loaded
     * @throws java.lang.Exception when something goes wrong
     */
    private String loadResource(String path)
    throws Exception{
        InputStream in=getClass().getResourceAsStream(path);


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
    public static String getTextFile(URI uri,String encoding)
    throws Exception{
        URL url=uri.toURL();
        if(encoding == null)
            encoding="utf-8";
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
    public String getVersion()
    {
        if(m_version!=null)
            return m_version;
        try{
            m_version=loadResource("/versioning/version.txt");
            m_version=m_version.trim();
            return m_version;
        }
        catch(Exception ex)
        {
            return null;
        }    
    }

    /**
     * Get version at download page
     */
    public String getLatestDistVersion()
    {
       if(m_latestVersion!=null)
           return m_latestVersion;
       try{
            URI theUri=new URI(DIST_VERSION_ADDRESS);
            String v=getTextFile(theUri, null);
            m_latestVersion= v.trim();
            return m_latestVersion;
         }
        catch(Exception ex)
        {
            return null;
        }
    }

     /**
     * Get version of wxt engine at download page
     */
    public String getLatestWxtDistVersion()
    {
       if(m_latestWxtVersion!=null)
           return m_latestWxtVersion;
       try{
            URI theUri=new URI(DIST_WXT_VERSION_ADDRESS);
            String v=getTextFile(theUri, null);
            m_latestWxtVersion= v.trim();
            return m_latestWxtVersion;
         }
        catch(Exception ex)
        {
            return null;
        }
    }

}

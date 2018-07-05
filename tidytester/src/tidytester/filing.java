package tidytester;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URL;

/**
 * @author BS
 * Loading / savingtext-files
 */
public class filing {
 
     /**
     * Attempts to make an URI from a string. Spaces are replaced by %20
     * @param source The string we will interpret as an URI
     * @return The prepared UIR
     * @throws java.lang.Exception
     */
    public static URI makeUri(String source)
    throws Exception{
        source=source.trim();
        source=source.replace('\\', '/');
        source=source.replaceAll(" ", "%20");
        if(source.startsWith("http"))
        {
           try{
               //must locate and urlencode querypart ?
               int pos=source.indexOf('?');
               if(pos != -1)
               {
                    String p1=source.substring(0, pos);
                    String p2=source.substring(pos);
                    //p2=p2.replaceAll("=", "%3D");
                    //p2=p2.replaceAll(",", "%2C");
                    p2=p2.replaceAll("&", "&amp;");
                    String tmp=p1+p2;
                    URI tstUri=new URI(tmp);
                    return tstUri;
               }
           }
           catch(Exception e)
           {
                return new URI(source);
           }

        }
        return new URI(source);
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
        BufferedReader in=null;
        try{
            URL url=uri.toURL();
            if(encoding == null)
                encoding="UTF-8";
            // while testing: System.out.println(url.toString());
            in = new BufferedReader(new InputStreamReader(url.openStream(),encoding));
            StringBuffer result=new StringBuffer();
            String str;
            //String eol=System.getProperty("line.separator");
            String eol="\n";
            while ((str = in.readLine()) != null) {
                result=result.append(str).append(eol);
            }
            in.close();

            return result.toString();
        }
        catch(Exception ex)
        {
            throw new Exception(ex.getMessage());
        }
        finally{
            if(in!=null)
                in.close();
        }
    }

      /** Save a text file.
     * 
     * @param theUri The absolute URI to the file
     * @param text The text to write
     * @param encoding The encoding to use
     * @return true if save goes ok, false otherwise
     */
     public static boolean saveTFile(URI theUri, String text, String encoding)
    {
        File f;
        FileOutputStream fos=null;
        OutputStreamWriter osw=null;
        try {
            f = new File(theUri);

            fos=new FileOutputStream(f);
            if(encoding!=null)
                osw=new OutputStreamWriter(fos,encoding);
            else
                osw=new OutputStreamWriter(fos);
            osw.write(text);
            osw.close();
            return true;
        } 
        catch (Exception ex) 
        {
            System.out.println("File not written: " + theUri.toString()+" : "+ex.getMessage());
            return false;
        }

    }

    
    
}

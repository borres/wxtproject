/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package filing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URI;
import java.net.URL;

/**
 *
 * @author Administrator
 */
public class readwrite {
    
        /**
     * Read a String from an URI.
     * 
     * @param uri The URI to read from
     * @param encoding The encoding
     * @return the string read
     * @throws Exception when the text cannot be loaded or the URI is not wellformed
     */
    public static String getTextFile(URI uri, String encoding)
            throws Exception {
        BufferedReader in = null;
        try {
            URL url = uri.toURL();
            // while testing: System.out.println(url.toString());
            in = new BufferedReader(new InputStreamReader(url.openStream(), encoding));
            StringBuffer result = new StringBuffer();
            String str;
            //String eol=System.getProperty("line.separator");
            String eol = "\n";
            while ((str = in.readLine()) != null) {
                result = result.append(str).append(eol);
            }
            in.close();

            return result.toString();
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }
    
    public static String getTextFile(String filename){
        FileReader FR=null;
        BufferedReader BR= null;
        StringBuilder result=new StringBuilder();
        try{
            FR= new FileReader(filename);
            BR= new BufferedReader(FR);
        
            String line;
            while((line=BR.readLine()) != null){
                result.append(line);
                result.append("\n");
            }
            BR.close();
            }
        catch(Exception ex)
        {
            System.out.println(ex.toString()); 
        }
        return result.toString();       
    }

    
    public static boolean writeTextFile(String txt, String filename){
        try{
            File f=new File(filename);
            // maybe we dont have the files parent directory
            f.getParentFile().mkdirs();
            PrintStream out=new PrintStream(f);
            out.print(txt);
            out.flush();
            out.close();
            return true;
         }
       catch(Exception ex)
        {
            System.out.println(ex.toString());
            return false;
        }
      
    }

    
}

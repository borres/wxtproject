package wxtresources;

import reporting.reporter;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import utils.accessutils;

/**
 * This class access resources. That is files packed i jar-file
 * under catalog: wxtresources.
 * The files are:<br>
 *  wxt4script.xsd, for validating a script<br>
 *  refs.xsd, for validating a reference file<br>
 *  images.xsd, for validating a file with imagedefinitions<br>
 *  fragments.xsd, for validating a file with fragmentdefinitions<br>
 *  totable.xsl, for transforming databaseoutput as table<br>
 *  odt2wiki.xsl, for transforming odt to wiki<br>
 *  wxthome.txt, the url where wxt and download page is<br>
 *  version.txt, this version
 * 
 * Also load the latest distributed version from an url, see
 * getLatestDistVersion()
 * 
 * All static methods
 * actual reporter is set from ScriptHandler
 */
public class resourceHandler
{
    /** a reporter */
    public static reporter theReporter;
    private static final String RESOURCECATALOG="/wxtresources/";
     
  

    /**
     * Loading a resource from the jar
     * @param path The path to load
     * @return The string loaded
     * @throws java.lang.Exception when something goes wrong
     */
    private static  String loadResource(String path)
    throws Exception{
        InputStream in=resourceHandler.class.getResourceAsStream(RESOURCECATALOG+path);
        //InputStream in=getClass().getResourceAsStream(RESOURCECATALOG+path);


        if(in!=null)
        {
            Reader reader=null;
            try{
                reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder buf=new StringBuilder();
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
                theReporter.pushMessage("Cannot_load_resource", path);
                throw new Exception();
            }
        }
        else
        {
            theReporter.pushMessage("Cannot_find_resource",path);
            return null;
        }
    }


    /**
     * Get the schema for validating the script
     * @return The schema as a string
     */
    public static String getScriptSchema()
    {
        try{
            return loadResource("wxtscript.xsd");
        }
        catch(Exception ex)
        {
            return null;
        }
    }

     /**
     * Get the schema for validating formulas
     * @return The schema as a string
     */
    public static String getFormulaSchema()
    {
        try{
            return loadResource("formula.xsd");
        }
        catch(Exception ex)
        {
            return null;
        }

    }

    /**
     * Get the schema for validating a reference file
     * @return The schema as a string
     */
    public static String getReferenceSchema()
    {
        try{
            return loadResource("refs.xsd");
        }
        catch(Exception ex)
        {
            return null;
        }
    }

     /**
     * Get the schema for validating a Images file
     * @return The schema as a string
     */
    public static String getImagesSchema()
    {
        try{
            return loadResource("images.xsd");
        }
        catch(Exception ex)
        {
            return null;
        }
    }

     /**
     * Get the schema for validating a fragments file
     * @return The schema as a string
     */
    public static String getFragmentsSchema()
    {
        try{
            return loadResource("fragments.xsd");
        }
        catch(Exception ex)
        {
            return null;
        }
    }


    /**
     * Get the transformation to make tables from databaseresult
     * @return The transformation as a string
     */
    public static String getDBaseToTable()
    {
        try{
            return loadResource("totable.xsl");
        }
        catch(Exception ex)
        {
            return null;
        }
    }
    
    /**
     * Get the transformation from odt content to wiki
     * @return The transformation as a string
     */
    public static String getOdtToWiki()
    {
        try{
            return loadResource("odt2wiki.xsl");
        }
        catch(Exception ex)
        {
            return null;
        }
    }

     /**
     * Get the url to the wxt homepage
     * @return The url as a string
     */
    public static String getWxtHome()
    {
        try{
            return loadResource("wxthome.txt");
        }
        catch(Exception ex)
        {
            return null;
        }
    }

    /**
     * Get the version string: v.yyyy.mm.dd
     * @return The version as a string
     */
    public static String getVersion()
    {
        try{
            return loadResource("version.txt").trim();
        }
        catch(Exception ex)
        {
            return null;
        }    
    }


    /**
     * get version for latest distributed wxt
     * @return the string describing the latest version distribute
     */
    public static String getLatestDistVersion()
    {
       try{
            URI theUri=new URI(getWxtHome()+"wxtversion.txt");
            String v=accessutils.getTextFile(theUri, null);
            return  v.trim();
         }
        catch(Exception ex)
        {
            return null;
        }
    }
}

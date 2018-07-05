package utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;

/**
 * This class access resources. That is files packed i jar-file
 * under catalog: resources.
 * The files are:
 *  resources/wxt2script.xsd, for validating a script
 *  resources/refs.xsd, for validating a reference file
 *  resources/images.xsd, for validating a file with imagedefinitions
 *  resources/totable.xsl, for transforming databaseoutput as table
 *  resources/odt2wiki.xsl, for transforming odt to wiki
 *  resources/wxthome.txt, the url where wxt and download page is
 *  resources/version.txt, the last version
 */
public class resourceHandler
{
    /** a reporter */
    private reporter m_Reporter;
    /** resources, kept for reload */
    private String m_wxt2script;
    private String m_refs;
    private String m_images;
    private String m_totable;
    private String m_odt2wiki;
    private String m_wxthome;
    private String m_fragments;
    private String m_authors;

    private String m_version;
    private String m_latestVersion;

    /*
     private final String m_distVersionAddress=
            "http://www.it.hiof.no/wxt/wxtsite/wxt/wxtversion.txt";
     */

    /**
     * Constructing a resourceHandler
     * @param rep A reporter to report errors to
     */
    public resourceHandler(reporter rep)
    {
        m_Reporter=rep;
        m_wxt2script=null;
        m_refs=null;
        m_totable=null;
        m_images=null;
        m_odt2wiki=null;
        m_wxthome=null;
        m_fragments=null;

        m_version=null;
        m_latestVersion=null;
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
                m_Reporter.pushMessage("Cannot_load_resource", path);
                throw new Exception();
            }
        }
        else
        {
            m_Reporter.pushMessage("Cannot_find_resource",path);
            return null;
        }
    }


    /**
     * Get the schema for validating the script
     * @return The schema as a string
     */
    public String getScriptSchema()
    {
        if(m_wxt2script!=null)
            return m_wxt2script;
        try{
            m_wxt2script=loadResource("/resources/wxt2script.xsd");
            return m_wxt2script;
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
    public String getReferenceSchema()
    {
        if(m_refs!=null)
            return m_refs;
        try{
            m_refs=loadResource("/resources/refs.xsd");
            return m_refs;
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
    public String getImagesSchema()
    {
        if(m_images!=null)
            return m_images;
        try{
            m_images=loadResource("/resources/images.xsd");
            return m_images;
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
    public String getFragmentsSchema()
    {
        if(m_fragments!=null)
            return m_fragments;
        try{
            m_fragments=loadResource("/resources/fragments.xsd");
            return m_fragments;
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
    public String getDBaseToTable()
    {
        if(m_totable!=null)
            return m_totable;
        try{
            m_totable=loadResource("/resources/totable.xsl");
            return m_totable;
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
    public String getOdtToWiki()
    {
        if(m_odt2wiki!=null)
            return m_odt2wiki;
        try{
            m_odt2wiki=loadResource("/resources/odt2wiki.xsl");
            return m_odt2wiki;
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
    public String getWxtHome()
    {
        if(m_wxthome!=null)
            return m_wxthome;
        try{
            m_wxthome=loadResource("/resources/wxthome.txt");
            return m_wxthome;
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
    public String getVersion()
    {
        if(m_version!=null)
            return m_version;
        try{
            m_version=loadResource("/resources/version.txt");
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
    /*public String oldgetLatestDistVersion()
    {
       if(m_latestVersion!=null)
           return m_latestVersion;
       try{
            URI theUri=new URI(m_distVersionAddress);
            String v=accessutils.getTextFile(theUri, null);
            m_latestVersion= v.trim();
            return m_latestVersion;
         }
        catch(Exception ex)
        {
            return null;
        }
    }*/

    public String getLatestDistVersion()
    {
       if(m_latestVersion!=null)
           return m_latestVersion;
       try{
            URI theUri=new URI(getWxtHome()+"wxtversion.txt");
            String v=accessutils.getTextFile(theUri, null);
            m_latestVersion= v.trim();
            return m_latestVersion;
         }
        catch(Exception ex)
        {
            return null;
        }
    }


}

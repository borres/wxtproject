/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wxt5run;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.util.HashMap;
import javax.swing.tree.DefaultTreeModel;
import wxt5utils.wxt5rundomer;
import wxt5utils.treeMaker;
import wxt5utils.validationErrorHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author bs
 */
public class Script {
    
    protected String m_filePath;
    protected String m_script;
    protected Document m_doc;
    protected boolean m_isDirty;
    protected URI m_uri;
    protected long m_lastTimeSeen;
    protected String m_message;
    protected String m_encoding;
    protected String m_schemapath;
    static public final String UTF8_BOM_MARK="ï»¿";
    
    
    // these must match the constants in wxt:definitions
    public static final String DEFAULT_ENCODING="default-encoding";
    public static final String INDENT_OUTPUT="indent-output";
    public static final String EXPAND_ALL="expand-all";
    public static final String OUTPUT_FORMAT="output-format";
    public static final String REFERENCE_FORM="reference-form";
    public static final String REFERENCE_INDEXING="reference-indexing";
    public static final String USE_COPY="use-copy";
    public static final String DROP_BOOKS="drop-books";
    public static final String PREFORMAT_LANGUAGE="preformat-language";
    public static final String VERBOSE="verbose";

    
    public static final String IEEE="ieee";
    public static final String HARVARD="harvard";
    public static final String SIMPLE="simple";
    public static final String YES="yes";
    public static final String NO="no"; 
    public static final String LOCAL="local";
    public static final String GLOBAL="global";
    public static final String DROP_NONE="____";

    
    public Script()
    {
        m_filePath=null;
        m_script="";
        m_doc=null;
        m_isDirty=false; 
        m_uri=null;
        m_lastTimeSeen=0;
        m_message=null;
        m_schemapath=null;
        m_encoding="UTF-8";
    }
    
    
    /**
     * Construct a new script
     * @param path The filepath to the script
     * @param wxtpth The filepath to wxt2
     * @throws java.lang.Exception Throws an exception when the file does not exist 
     * or when the script is not a legal wxtscript
     */
    public Script(String path,String wxtpth)
    throws Exception
    {
        this();
        m_filePath=path;
        File tmp=new File(wxtpth);
        wxtpth=tmp.getParent().toString();
        m_schemapath=wxtpth+System.getProperty("file.separator")+
                                        "resources"+
                                        System.getProperty("file.separator")+
                                        "wxt5script.xsd";
        m_schemapath=m_schemapath.replace(" ", "%20");
        try {
            BufferedReader in = new BufferedReader(new FileReader(m_filePath));
            m_script="";
            String t;
            while ((t = in.readLine()) != null) {
              m_script+=t+"\n";
            }
            in.close();
        } 
        catch (Exception e) {
            throw new Exception("Could not load script: "+path);
        }
        // clean BOM, if any
        m_script=cleanMarks(m_script);
        // a script is (probably) a legal wxtscript when we 
        // 1 can make the dom(legal xml)
        // 2 the rootnode is wscript
        try{
            String tmpx=m_filePath.replace('\\','/');
            tmpx=tmpx.replace(" ", "%20");
            m_uri=new URI("file:///"+tmpx);
            //m_uri=new URI("file:///"+m_filePath.replace('\\','/'));
            m_doc=wxt5rundomer.makeDomFromUri(m_uri);
        }
        catch(Exception exp)
        {
            throw new Exception("not legal XML: \n"+exp.getMessage());
        }
        if(m_doc.getDocumentElement().getNodeName().compareTo("wscript")!=0)
            throw new Exception("not legal wxtcript: no wscript-tag");                
    }
        
    /**
     * Get all options as they are by default and corrected for options found
     * in the script
     * @return The options
     */
    public HashMap<String,String> getActiveOptions()
    {
        // first we set defaults as defined in wxt
        // should be exactly as in wxt:definitions
        HashMap op=new HashMap<String,String>(4);
        op.put(EXPAND_ALL, NO);
        op.put(INDENT_OUTPUT,NO);
        op.put(OUTPUT_FORMAT,"html");
        op.put(PREFORMAT_LANGUAGE,NO);
        op.put(REFERENCE_INDEXING,LOCAL);
        op.put(DEFAULT_ENCODING,"utf-8");
          
        // then we correct for options set in script
        if(m_doc!=null)
        {
            NodeList nl=m_doc.getElementsByTagName("option");
            for(int ix=0;ix<nl.getLength();ix++)
            {
                Element elt=(Element)nl.item(ix);
                if((elt.hasAttribute("name"))&&(elt.hasAttribute("value")))
                {
                    String nam=elt.getAttribute("name");
                    String val=elt.getAttribute("value");
                    op.put(nam, val);
                }
            }
        }
        return op;
    }
    
    
    public String getPath()             {return m_filePath;}
    public URI getUri()                 {return m_uri;}
    public String getScript()           {return m_script;}
    public void setScript(String s)     {m_script=s; m_isDirty=true;}
    public void setSchemaPath(String s) {m_schemapath=s;}
    public String getSchemaPath()       {return m_schemapath;}
    public boolean isDirty()            {return m_isDirty;}
    public void setDirty(boolean dirty) {m_isDirty=dirty;}
    public String getMessage()          {if(m_message==null)return ""; return m_message;}
    public long getLastTimeSeen()       {return m_lastTimeSeen;}   
    public void setLastTimeSeen()       {m_lastTimeSeen=System.currentTimeMillis();}
    public Document getDoc()            { return m_doc; }
    

    /**
     * Produce an empty tree
     * @return An empty Tree
     */
    static public DefaultTreeModel getEmptyTree()
    {
        return treeMaker.getEmptyTree();
    }
    
    /**
     * Set up a tree for this script
     * @return The tree
     */
    public DefaultTreeModel getModuleTree()
    {
        try{
            treeMaker tm=new treeMaker(m_doc);
            return tm.getModel();
        }
        catch(Exception ex)
        {
            return getEmptyTree();
        }        
    }
    

    /**
     * Validate this script
     * @return The report from the validation
     */
    public String validateScript()
    {
       try{
            // find the schema
            if(!new File(m_schemapath).exists())
                return "Cannot locate schema\n"+m_schemapath;
            else
            {
                String uristr="file:///"+m_schemapath;
                uristr=uristr.replace('\\', '/');
                URI theUri=new URI(uristr);
                validationErrorHandler eHandler=new validationErrorHandler();
                String result=wxt5rundomer.ValidateString(m_script,theUri,eHandler);
                if(result.compareToIgnoreCase("OK")!=0)
                {
                    throw new Exception(result);
                }
                String rep=eHandler.getReport();
                return rep+'\n';
            }       
        }
        catch(Exception vex)
        {
           return vex.getMessage(); 
        }
    }

    /**
     * Make sure we dont have any text before XML-header
     * BOM is the main target for this cleaning
     * @param t The string we investigate
     * @return The cleaned string
     */
    protected String cleanMarks(String t)
    {
        String s=t;
        while((!s.startsWith("<")) &&(s.length()>4))
            s=s.substring(1);
        return s;
    }
    
    @Override
    public String toString(){return m_filePath;}
}
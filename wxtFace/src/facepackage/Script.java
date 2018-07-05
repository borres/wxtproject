package facepackage;

import faceutils.FaceDomer;
import faceutils.TreeMaker;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URI;
import java.util.HashMap;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultTreeModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Holding a script
 * The script is parsed and documenmt element is checked
 * No complete validation is done in face
 * Complete validation is done when script is used against WXT-engine
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
    static public final String UTF8_BOM_MARK="ï»¿";
    
    
     //----------------------------------------------------
    // Options, must be exactly equal in 
    // GUI apps Script-class an WXTengines Options-class
    public static final String DEFAULT_ENCODING = "default-encoding";
        // any lega encoding
    
    public static final String TIDY_INPUT = "tidy-input";
        public static final String NO = "no";
        public static final String YES = "yes";

    
    public static final String REFERENCE_FORM = "reference-form";
        public static final String HARVARD = "harvard";
        public static final String IEEE = "ieee";
        public static final String SIMPLE = "simple";
    
    public static final String INDENT_OUTPUT = "indent-output";
        // YES
        // NO
    
    public static final String PREFORMAT_LANGUAGE = "preformat-language";
        // YES
        // NO
   
    public static final String CONTROL_REFERENCES = "control-references";
        // YES
        // NO
    
    public static final String DROP_BOOKS = "drop-books";
        // comma separated list of book attributes
        public static final String DROP_NONE = "____";
    
    public static final String VERBOSE = "verbose";
        // YES
        // NO    
    
    public static final String REFERENCE_INDEXING = "reference-indexing";
        public static final String GLOBAL = "global";
        public static final String LOCAL = "local";
    
    public static final String EXPAND_ALL = "expand-all";
        // YES
        // NO    
        public static final String NEUTRAL="*";
    
    public static final String OUTPUT_FORMAT = "output-format";
        public static final String XML = "xml";
        public static final String XHTML = "xhtml";
        public static final String TEXT = "text";
        public static final String HTML = "html";
        public static final String HTML5 = "html5";
    
   public static final String USE_COPY = "use-copy";  
        // YES
        // NO
 
    public static final String SCRIPT="script";
        // a script path
        public static final String NO_SCRIPT="no_script";
    
    public static final String MODULES="modules";
        // commaseparated list of module ids
        public static final String ALL_MODULES = "All_Modules";
        public static final String NO_MODULES="no_smodules";
        
  // when we attempt to access an option that does not exist
   public static final String NO_VALUE="no_value";
  //------------------------------------------------------ 
    
    public Script()
    {
        m_filePath=null;
        m_script="";
        m_doc=null;
        m_isDirty=false; 
        m_uri=null;
        m_lastTimeSeen=0;
        m_message=null;
        m_encoding="UTF-8";
    }
    
    
    /**
     * Construct a new script
     * @param path The filepath to the script
     * @throws java.lang.Exception Throws an exception when the file does not exist 
     * or when the script is not a legal wxtscript
     */
    public Script(String path)
    throws Exception
    {
        this();
        m_filePath=path;
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
            m_doc=FaceDomer.makeDomFromUri(m_uri);
        }
        catch(Exception exp)
        {
            throw new Exception("not legal XML: \n"+exp.getMessage());
        }
        if(m_doc.getDocumentElement().getNodeName().compareTo("wscript")!=0) {
            throw new Exception("not legal wxtcript: no wscript-tag");
        }                
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
        return TreeMaker.getEmptyTree();
    }
    
    /**
     * Set up a tree for this script
     * @return The tree
     */
    public DefaultTreeModel getModuleTree()
    {
        try{
            TreeMaker tm=new TreeMaker(m_doc);
            return tm.getModel();
        }
        catch(Exception ex)
        {
            return getEmptyTree();
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
        while((!s.startsWith("<")) &&(s.length()>4)) {
            s=s.substring(1);
        }
        return s;
    }
    
    
    
    @Override
    public String toString(){return m_filePath;}
    
    /**
     * Load a script with a filedialog
     * @return  The path to the script or null
     */
    static String loadScript(JFrame parent){
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Load script");
        fc.setAcceptAllFileFilterUsed(true);
        FileNameExtensionFilter jarFilter=new FileNameExtensionFilter("xmlfiles","xml");
        fc.setFileFilter(jarFilter);

        int result=fc.showOpenDialog(parent);
        if (result==JFileChooser.APPROVE_OPTION)
        {
           String scriptPath=fc.getSelectedFile().toString(); 
           return scriptPath;
        }
        else {
            return null;
        }       

    }
}
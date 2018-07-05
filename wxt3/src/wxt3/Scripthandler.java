package wxt3;


import utils.accessutils;
import utils.domer;
import utils.reporter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import content.Module;
import content.Definitions;
import content.indexHolder;
import content.producer;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.w3c.dom.NodeList;
import utils.PIcommand;
import utils.encoderutils;
import utils.idprovider;
import utils.resourceHandler;
import utils.validationErrorHandler;
import utils.wxtError;


/**
 * A scripthandler handles actions related to a script: parsing and building.
 * A scripthandler is the only connection between a GUI and the wxt-engine.
 * The GUI knows the scripthandler, but not vica versa.
 * The interface from the GUI is:
 * <ul>
 *  <li>construct Scripthandler(HashMap<String,String>arguments)
 *    This parse the script and set up the module structure
 *    arguments are options, by name and value
 * </li><li>
 * buildModules(HashMap<String,String>arguments)
 *    This build modules, all or list
 *    arguments are options by name and value
 *    and moduleids as a commaseparated string, name: modules
 *</li><li>
 * updatemodules(HashMap<String,String>arguments)
 *    This updates the modules that has had one of its components changed
 *    arguments are options by name and value
 * </li><li>
 *String getReport()
 *    returns the report from the recent action (above)
 * </li><li>
 *String getModuleDescription(String mid)
 *    returns efficient properties of a Module as HTML-formatted string
 * </li>
 * </ul>
 *
 * Options are treated according to the following priority
 * <ol>
 * <li>options sent to methods above</li>
 * <li>options set in script</li>
 * <li>default values, see documentation</li>
 * </ol>
 */
public class Scripthandler {

    
    /** the path to the script as given in constructor*/
    private String m_scriptPath;
    
    /** the directorypath for the script*/
    private String m_scriptCatalog;
    
    /** the absolute URI to the script */
    private URI m_absoluteScriptUri;   
        
    /** the absolute URI to the scripts directory*/
    private String m_absoluteScriptCatalogUri;
        
    /** the script as document */
    private Document m_scriptDoc;
    
    /** the top-level modules */
    private List<Module> m_RootModules;
        
    /** all definitions, the one and only definitions object */
    private Definitions m_Def;
            
    /** the reporter */
    private reporter m_Reporter;
    
    /** the producer */
    private producer m_Producer;
    
    /** the indexholder */
    private indexHolder m_IndexHolder;
    
    /** arguments from main (commandline) that controls the building*/
    HashMap<String,String>m_arguments;

    /** when we traverse next and prev, result of getAllModules(null)*/
    List<Module>m_linearModuleList;

    /** the resourcehandler*/
    resourceHandler m_resourceHandler;

    /** when we are busy building*/
    boolean m_buildingAll=false;

    /** idprovider */
    private idprovider m_idprovider;
    
    

   /**
     * Constructing a new Scripthandler.
    * 
     * @param arguments The arguments could be given at the commandline or in a GUI
     * @throws java.lang.Exception when we cannot establish the scriptDOM or the Modulestructure
     */
    public Scripthandler(HashMap<String,String>arguments)
    throws Exception 
    {
        // the arguments used are:
        // script : scriptpath (mandatory)
        // options by their name
        // color-code: yes/no
        // expand-all: yes/no
        // indent-output: yes /no
        // output-format:text/html/xml/xhtml
        // default-encoding: utf-8/iso-8859-1/etc
        // reference-form: harvard/ieee/simple
        // reference-indexing: local/global
        // avoid-books: csv

      
        //-------------------------------------
        // options are selected because we need them in definitions
        // options set here will override defaults and script set options
        
        m_arguments=arguments;
        // set up a reporter
        m_Reporter=new reporter();
        m_Reporter.clearMessages();

        // set up a resourcehandler
        m_resourceHandler=new resourceHandler(m_Reporter);


        m_Reporter.pushSimpleMessage("WXT version: "+getVersion());


        //-------------------------------------
        // we must/should have a decent java version, better than: 1.6.0_xx
        String targetversion="1.6.0";
        String cv=System.getProperty("java.version");
        cv=cv.substring(0,cv.indexOf("_"));
        if(cv.compareTo(targetversion)<0)
        {
            m_Reporter.pushMessage("java-upgrade", targetversion+"_++ ");
            System.out.println("Too old java version");
            System.out.println("Should be at least: "+targetversion+"_++ ");

            // we dont die, since most of the job can be done with an older version
        }


        
        // control options
        controlOptions(arguments);
        
        
        m_scriptPath=m_arguments.get("script");
        m_scriptPath=m_scriptPath.replace(" ", "%20");
        
        //System.out.println("Scripthandler: starting constructor");

 
        try {
            m_scriptCatalog=new File(m_scriptPath).getParent();

            
            m_scriptPath=m_scriptPath.replace('\\', '/');
            //System.out.println("Scripthandler: script is "+m_scriptPath);
          
            /*
             if(!m_scriptPath.startsWith("file:"))
                m_absoluteScriptUri=accessutils.makeUri("file:///"+m_scriptPath);
             */
            /*
            // or allow filepath relative to working directory ?
            String curDir = System.getProperty("user.dir");
            m_absoluteScriptUri=accessutils.makeAbsoluteURI("file:///"+scriptPath, curDir);
            */
            m_absoluteScriptUri=accessutils.makeUri(m_scriptPath);
            

            String scheme=m_absoluteScriptUri.getScheme();
            
           
            if((scheme!=null)&&(scheme.length() < 2))
            {
                // this probably is a windows absolute filepath 
                // starting with drive-letter
                //m_absoluteScriptUri=new URI("file:///"+m_absoluteScriptUri.toString());
                m_absoluteScriptUri=accessutils.makeUri("file:///"+m_absoluteScriptUri.toString());
            }

           
            // no stringsource or tidy here
            m_scriptDoc =domer.makeDomFromUri(m_absoluteScriptUri);

            if(m_scriptDoc.getDocumentElement().getNodeName().compareTo("wscript")!=0)
                throw new Exception("not legal wxtcript: no wscript-tag");
            //System.out.println("Scripthandler has built dom");            
            // need the catalog, file is assumed (not http-scheme)
            m_absoluteScriptCatalogUri=new File(m_absoluteScriptUri.toString()).getParent().replace('\\','/');
            
            // change local in reporter here if we want anything but: 
            // System.getProperty("user.language")
            
            // set up a producer
            m_Producer =new producer();
            // change locale in producer  here if we want anything but: 
            // System.getProperty("user.language")
            
            // set up indexholder
            m_IndexHolder=new indexHolder();
            
            
            // remove all outdated elements (and outdated elements children)
            removeOutdatedElements(m_scriptDoc.getDocumentElement());
            
            // take care of all definitions
            //m_Def=collectDefinitions();
        }
        catch(Exception dex)
        {
            
            String s=reporter.getBundleString("cannot_build_script_dom");
            throw new Exception(s+":"+dex.getMessage());
        }
    
        // validate the script
       try{
            // find the schema
           String schemaString=m_resourceHandler.getScriptSchema();
           validationErrorHandler eHandler=new validationErrorHandler(true);

           String result=domer.validateDomFromString(m_scriptDoc,schemaString,eHandler);
           if(result!=null)
            {
                throw new Exception(result);
            }
            String rep="Scriptvalidation:\n"+eHandler.getReport();
            m_Reporter.pushMessage(rep+'\n');

        }
        catch(Exception vex)
        {
            String s=reporter.getBundleString("cannot_validate_script");
            throw new Exception(s+":"+vex.getMessage());
        }


         // take care of all definitions
        m_Def=collectDefinitions();

        try{           
            //---------------------
            // set up the Module tree
           
            m_Reporter.pushMessage("start_parsing", m_scriptPath);
            
            // the following three methodcalls modify the script DOM 
            // listelements are replaced by lists of elements
            ScriptListParser.parseXMLContentLists(m_scriptDoc.getDocumentElement(),this);
            ScriptListParser.parseTXTContentLists(m_scriptDoc.getDocumentElement(),this);
            ScriptListParser.parseModuleLists(m_scriptDoc.getDocumentElement(),this);
            
            // set up the datastructure with all modules
            establishStructure();

            m_linearModuleList=getAllModules(null);
            
            m_Reporter.pushMessage("stop_parsing", m_scriptPath);
           
        } 
        catch (Exception e) 
        {
            
            String s=reporter.getBundleString("cannot_build_module_structure");
            throw new Exception(s+":"+e.getMessage());
        }

    }
    
    /**
     * Control all options in a hashmap
     * remove those who are bad
     * @param options
     */
    public void controlOptions(HashMap<String,String>options)
    {
        
        if(options.containsKey(Definitions.EXPAND_ALL))
        {
            String tmp=(options.get(Definitions.EXPAND_ALL));
            tmp=tmp.toLowerCase();
            if((tmp.compareTo(Definitions.YES)!=0) &&(tmp.compareTo(Definitions.NO)!=0))
            {
                m_Reporter.pushMessage("bad_option",Definitions.EXPAND_ALL+"="+tmp);
                // set default
                options.put(Definitions.EXPAND_ALL, Definitions.NO);
            }
        }
       if(options.containsKey(Definitions.PREFORMAT_LANGUAGE))
        {
            String tmp=(options.get(Definitions.PREFORMAT_LANGUAGE));
            tmp=tmp.toLowerCase();
            if((tmp.compareTo(Definitions.YES)!=0) &&(tmp.compareTo(Definitions.NO)!=0))
            {
                m_Reporter.pushMessage("bad_option",Definitions.PREFORMAT_LANGUAGE+"="+tmp);
                // set default
                options.put(Definitions.PREFORMAT_LANGUAGE, Definitions.NO);
            }
        }
       if(options.containsKey(Definitions.INDENT_OUTPUT))
        {
            String tmp=(options.get(Definitions.INDENT_OUTPUT));
            tmp=tmp.toLowerCase();
            if((tmp.compareTo(Definitions.YES)!=0) &&(tmp.compareTo(Definitions.NO)!=0))
            {
                m_Reporter.pushMessage("bad_option",Definitions.INDENT_OUTPUT+"="+tmp);
                // set default
                options.put(Definitions.INDENT_OUTPUT, Definitions.NO);
            }
        }
        if(options.containsKey(Definitions.OUTPUT_FORMAT))
        {
            String tmp=(options.get(Definitions.OUTPUT_FORMAT));
            tmp=tmp.toLowerCase();
            if((tmp.compareTo("xml")!=0)&&
               (tmp.compareTo("html")!=0)&&
               (tmp.compareTo("text")!=0)&&
               (tmp.compareTo("xhtml")!=0))
            {
                m_Reporter.pushMessage("bad_option",Definitions.OUTPUT_FORMAT+"="+tmp);
                // set default
                options.put(Definitions.OUTPUT_FORMAT, "html");

            }           
        }
        if(options.containsKey(Definitions.DEFAULT_ENCODING))
        {
            // is this a legal and known encoding
            String tmp=encoderutils.getLegalEncoding(options.get(Definitions.DEFAULT_ENCODING));
            if(tmp==null)
            {
                m_Reporter.pushMessage("bad_option",Definitions.DEFAULT_ENCODING+"="+tmp);
                // set default
                options.put(Definitions.DEFAULT_ENCODING, "utf-8");
            }
            else
                options.put(Definitions.DEFAULT_ENCODING, tmp);
        }
        if(options.containsKey(Definitions.REFERENCE_FORM))
        {
            String tmp=(options.get(Definitions.REFERENCE_FORM).toLowerCase());
            tmp=tmp.toLowerCase();
            if((tmp.compareTo(Definitions.IEEE)!=0)&&
               (tmp.compareTo(Definitions.HARVARD)!=0)&&
               (tmp.compareTo(Definitions.SIMPLE)!=0))
            {
                m_Reporter.pushMessage("bad_option",Definitions.REFERENCE_FORM+"="+tmp);
                // set default
                options.put(Definitions.REFERENCE_FORM, Definitions.SIMPLE);
            }
            else
            options.put(Definitions.REFERENCE_FORM, tmp);
        }
        if(options.containsKey(Definitions.REFERENCE_INDEXING))
        {
            String tmp=(options.get(Definitions.REFERENCE_INDEXING).toLowerCase());
            tmp=tmp.toLowerCase();
            if((tmp.compareTo(Definitions.LOCAL)!=0)&&
               (tmp.compareTo(Definitions.GLOBAL)!=0))
            {
                m_Reporter.pushMessage("bad_option",Definitions.REFERENCE_INDEXING+"="+tmp);
                // set default
                options.put(Definitions.REFERENCE_INDEXING, Definitions.LOCAL);
            }
            else
            options.put(Definitions.REFERENCE_INDEXING, tmp);
        }
        if(options.containsKey(Definitions.USE_COPY))
        {
            String tmp=(options.get(Definitions.USE_COPY).toLowerCase());
            tmp=tmp.toLowerCase();
            if((tmp.compareTo(Definitions.NO)!=0)&&
               (tmp.compareTo(Definitions.YES)!=0)&&
               (tmp.compareTo("*")!=0))
            {
                m_Reporter.pushMessage("bad_option",Definitions.USE_COPY+"="+tmp);
                // set default
                options.put(Definitions.USE_COPY, Definitions.NO);
            }
            else
            options.put(Definitions.USE_COPY, tmp);
        }
        if(options.containsKey(Definitions.DROP_BOOKS))
        {
            String tmp=(options.get(Definitions.DROP_BOOKS));
            if(tmp.length()<2)
                options.put(Definitions.DROP_BOOKS, Definitions.DROP_NONE);
        }
        if(options.containsKey(Definitions.TIDY_INPUT))
        {
            String tmp=(options.get(Definitions.TIDY_INPUT).toLowerCase());
            if((tmp.compareTo(Definitions.YES)==0)||(tmp.compareTo(Definitions.NO)==0))
                options.put(Definitions.TIDY_INPUT, tmp);
            else
            {
                // it is supposed to be a filename and is left untouched
            }
        }
        
       
    }
    
     /**
     * Identify the definitions node and establish an object of type {@link Definitions}.
      * 
     * @return A reference to the Definitions Object
     */
    private Definitions collectDefinitions()
    {
       Node n = m_scriptDoc.getDocumentElement().getFirstChild();
       while (n != null) {
            if ((n.getNodeType() == Node.ELEMENT_NODE) &&
                    (n.getNodeName().compareToIgnoreCase("definitions") == 0))
            {
                        Definitions def=new Definitions(this,(Element)n);
                        return def;
            }

            n = n.getNextSibling();
       }
       // no definitions, but we set up an object anyhow
       // thus we avoid null-testing in all definition-access
       // and we get defaults in place (ie. pathfragment _scriptpath)
       return new Definitions(this);
    }
    
      
    /**
     * Remove elements which are outdated by a recursive traversal.
     * 
     * @param elt The element we are investigating
     */
    private void removeOutdatedElements(Element elt)
    {
       String firstdate,lastdate;
       if(!elt.hasChildNodes())
           return;
       Node n = elt.getFirstChild();
       Node next;
       while (n != null) 
       {
            next=n.getNextSibling();
            if (n.getNodeType() == Node.ELEMENT_NODE)
            {
                Element theElement=(Element)n;
                if((theElement.hasAttribute("lastdate"))||(theElement.hasAttribute("firstdate")))
                {
                    firstdate=null;
                    lastdate=null;
                    if(theElement.hasAttribute("firstdate"))
                        firstdate=theElement.getAttribute("firstdate");
                    if(theElement.hasAttribute("lastdate"))
                        lastdate=theElement.getAttribute("lastdate");

                    boolean removeIt=false;
                    try{
                        removeIt=!accessutils.legalDating(firstdate, lastdate);
                     }
                    catch(Exception ex)
                    {
                        m_Reporter.pushMessage("bad_date_format_in", n.getNodeName());
                        // we dont remove the element if format is bad
                        removeIt=false;
                    }
                    if(removeIt)
                        n.getParentNode().removeChild(n);
                    else
                        removeOutdatedElements((Element)n);
                }
                else
                    removeOutdatedElements((Element)n);
            }
            n = next;
       }
    }
    
    
    /**
     * Access a argumentvalue.
     * 
     * @param key The key we are looking for
     * @return The associated value if it exists, else null
     */
    public String getArgument(String key)
    {
        return m_arguments.get(key);
    }
    
       
    /**
     * Return the XML-document, the DOM of the script.
     * 
     * @return the script as a Document
     */
    public Document getScriptDoc() {
        return m_scriptDoc;
    }
    
     /**
      * Return the {@link Definitions} object created from the script.
      * 
      * @return the definitions object created from the script
      */
    public Definitions getDefinitions() {
        return m_Def;
    }

    /**
     * Get the resourcehandler
     * @return the resourcehandler set up for this script
     */
    public resourceHandler getResourceHandler()
    {
        return m_resourceHandler;
    }


   /**
    * Return the list of top-level Modules.
    * 
    * @return The root Modules
    */
    public List<Module> getRootModules() {
        return m_RootModules;
    }
    
    /**
     * Return the first module
     * @return first element i rootmodules
     */
    public Module getFirstRootModule()
    {
        //return m_RootModules.firstElement();
        return m_RootModules.get(0);
    }
   
    /**
     * Return the active {@link reporter}.
     * 
     * @return The reporter for this Scripthandler
     */
    public reporter getReporter()
    {
        return m_Reporter;
    }
    
    /**
     * Get the object that manages all indexes.
     * 
     * @return the indexholder
     */
    public indexHolder getIndexHolder()
    {
        return m_IndexHolder;
    }

     /**
      * Return the active {@link producer}.
      * 
      * @return The producer for this Scripthandler
      */
    public producer getProducer()
    {
        return m_Producer;
    }

    /**
     * Return the absolute URI to the current script.
     * 
     * @return The absolute URI to the current script
     */
      public URI getScriptAbsoluteUri()
      {
           return m_absoluteScriptUri;
      }
    
     /**
     * Return the absolute path to the current scripts catalog as an URI-string.
      * 
     * @return The path to the current script
     */
      public String getScriptCatalogAsUriString()
      {
           return m_absoluteScriptCatalogUri;
      }
      
     /**
     * Return the absolute path to the current scripts catalog.
      * 
     * @return The path to the current script
     */
     public String getScriptCatalog()
      {
          return m_scriptCatalog;
      }
     

     /**
      * Get a list of all modules, as traversed
      * @return The list of all modules
      */
     public List<Module>getLinearModuleList()
     {
         return m_linearModuleList;
     }

     
   
    /**
     * Setting up the complete structure of modules.
     * <p>
     * No building , only Module tree and Module attributes are set
     */
    private void establishStructure() {
        m_RootModules = new ArrayList<Module>(1);
        Node n = m_scriptDoc.getDocumentElement().getFirstChild();
        while (n != null) {
            if ((n.getNodeType() == Node.ELEMENT_NODE) &&
                (n.getNodeName().compareToIgnoreCase("Module") == 0)) 
            {
                // establish Module tree                                      
                try {
                    Module m = new Module(null,(Element) n,this);
                    m_RootModules.add(m);
                    establishModuleStructure(m);
                } catch (Exception e) {
                    m_Reporter.pushMessage("root_module_failed");                    
                }
            }
            n = n.getNextSibling();
        }
    }

 
    /**
     * Try to establish, recursively,  a Module structure rooted in one Module.
     * <p>
     * Abandon all children if establishing the module fails.
     * 
     * @param mod The Module we shall build children to
     * @throws java.lang.Exception when we cannot build a module in this subtree
     */
    private void establishModuleStructure(Module mod)
            throws Exception {

        Element elt = mod.getScriptElement();
        if (!elt.hasChildNodes()) {
            return;
        }
        Node n = elt.getFirstChild();
        while (n != null) {
            if ((n.getNodeType() == Node.ELEMENT_NODE) &&
                    (n.getNodeName().compareToIgnoreCase("Module") == 0)) 
            {
                // establish Module subtree
                Module m=null;
                try {
                    m = new Module(mod,(Element) n,this);
                    mod.add(m);
                    establishModuleStructure(m);
                } 
                catch (Exception e) {
                    m_Reporter.pushMessage("module_failed"); 
                    throw new Exception("failed_to_establish_module_structure");
                }
            }
            n = n.getNextSibling();
        }
    }
    
   
  /**
   * Building modules.
   * The arguments given controls the building.
   * @param arguments The arguments given from the commandline or from a GUI
   */
    public void  buildModules(HashMap<String,String>arguments) 
    {
        // The argument used is 
        // modules : commaseparated modulelist, those we want to build (optional)
        // options :by their name, all optional, as defined in definitions

        long startTime=System.currentTimeMillis();


        
        String[] modulelist=null;        
        if(arguments!=null)
        {
            // copy all arguments, add and replace
            // move options to definitions, override existing

            Set<String> keys=arguments.keySet();
            for (String key :keys)
            {
                m_arguments.put(key, arguments.get(key));
                if((key.compareTo(Definitions.EXPAND_ALL)==0) ||
                   (key.compareTo(Definitions.INDENT_OUTPUT)==0) ||
                   (key.compareTo(Definitions.DEFAULT_ENCODING)==0)||
                   (key.compareTo(Definitions.OUTPUT_FORMAT)==0)||
                   (key.compareTo(Definitions.REFERENCE_FORM)==0)||
                   (key.compareTo(Definitions.REFERENCE_INDEXING)==0)||
                   (key.compareTo(Definitions.USE_COPY)==0)||
                   (key.compareTo(Definitions.DROP_BOOKS)==0)||
                   (key.compareTo(Definitions.PREFORMAT_LANGUAGE)==0)||
                   (key.compareTo(Definitions.CONTROL_REFERENCES)==0)
                   )
                {
                    String val=arguments.get(key);
                    System.out.println("Option: "+key+"="+val);
                    if(val.length()>=2)
                        m_Def.setOption(key, val);
                    else
                        m_Reporter.pushSimpleMessage("Bad option: "+key+"="+val);

                }
            }
            //-------------------------------------
            // options are selected when we need them
            // control them
            controlOptions(m_Def.getOptions());


            // module list
            if(m_arguments.containsKey("modules"))
            {
                System.out.println("buildModules: "+m_arguments.get("modules"));            
                modulelist=m_arguments.get("modules").split(",");
            }

            if((modulelist!=null)&&(modulelist.length==1)&&(modulelist[0].compareTo("SCRIPT")==0))
                modulelist=null; // build all
            
            m_buildingAll=(modulelist==null);
                
        }
        
        if (m_scriptDoc == null)
        {
            // should no happen
            System.out.println("buildModules: Script is not parsed");
            m_Reporter.pushSimpleMessage("ERROR: Script is not parsed");
            return;
        }
        
        
        
        m_Reporter.clearMessages();
        m_Reporter.pushSimpleMessage("\n--------------------------\n");
        if(modulelist==null)
            m_Reporter.pushSimpleMessage("Start building all modules");
        else
            m_Reporter.pushSimpleMessage("Start building selected module(s):"+m_arguments.get("modules"));



        
        // clear used global references
        m_Def.initUsedReferences();
        // clear used images and formulas "made og the fly"
        m_Def.removeImagesMadeOntheFly();
        m_Def.removeFormulasMadeOntheFly();

        // clear accumulated indexes if we build all modules
        if(modulelist==null)
            m_IndexHolder=new indexHolder();

        // set up a new idprovider
        m_idprovider=new idprovider();

        // clear secondphase mark in all modules
        for (Module mod : m_RootModules)
        {
            Enumeration all=mod.preorderEnumeration();
            while(all.hasMoreElements())
                 ((Module)all.nextElement()).clearSecondPhaseMark();
         }

        
        // build
        for (Module mod : m_RootModules) 
        {
            build(mod,modulelist);
        }
        
        m_Reporter.pushSimpleMessage("Stop building\n-----------------");
        
        // we must do a second phase to pick upp all PI's
        // that depends on all modules being built
        
        doSecondPhase();
        
        //------ remove temporary files ---
        m_Def.removeTempCatalog();
        //---------------------------------

        m_Reporter.pushSimpleMessage("----------------------\n"+(System.currentTimeMillis()-startTime)+"ms");

        System.out.println("---------  "+(System.currentTimeMillis()-startTime)+"ms  ---------------");
        System.out.println("---------  finnished  ---------------");
    }

    /**
     * Building module mod and all children (recursively).
     * 
     * @param mod. The root to start building from.
     * @param modulelist. The modules to build, by id.
     */
    private void build(Module mod,String[]mids) 
    {

        // if we have a modulelist that tells us what to build,
        int doit=1; // we start with a the belief that we should do it
        if(mids!=null)
            doit=accessutils.indexOfNameInList(mod.getID(), mids);

        if(doit >=0)
        {
            // check if it should be avoided
            // is option DROP_BOOKS set
            String avoid=m_Def.getOption(Definitions.DROP_BOOKS);
            if(avoid.compareTo(Definitions.DROP_NONE)!=0)
            {
                if(accessutils.compareCSV(mod.getBooks(),avoid))
                    doit=-1;
            }
        }

        
        if(doit>=0)
            mod.build();

        // build all children as well

        if (mod.getChildCount() == 0)
            return;        

        Module m = (Module) mod.getFirstChild();
        while (m != null) {                
            build(m,mids);
            m = (Module) m.getNextSibling();
        }                
    }
    
    
  /**
   * Building modules that should be updated.
   * The arguments given controls the building.
   * @param arguments The arguments given from the commandline or from a GUI
   */
    public void  updateModules(HashMap<String,String>arguments) 
    {
        // The argument used is 
        // options by their name, all optional
        // only options are interesting
        
        if(arguments!=null)
        {
            // copy all arguments, add and replace
            // move options to definitions, override existing

            Set<String> keys=arguments.keySet();
            for (String key :keys)
            {
                m_arguments.put(key, arguments.get(key));
                if((key.compareTo(Definitions.EXPAND_ALL)==0) ||
                   (key.compareTo(Definitions.INDENT_OUTPUT)==0) ||
                   (key.compareTo(Definitions.DEFAULT_ENCODING)==0)||
                   (key.compareTo(Definitions.OUTPUT_FORMAT)==0)||
                   (key.compareTo(Definitions.REFERENCE_FORM)==0)||
                   (key.compareTo(Definitions.USE_COPY)==0)||
                   (key.compareTo(Definitions.PREFORMAT_LANGUAGE)==0)||
                   (key.compareTo(Definitions.DROP_BOOKS)==0))

                {
                    String val=arguments.get(key);
                    if(val.length()>2)
                        m_Def.setOption(key, val);
                    else
                        m_Reporter.pushSimpleMessage("Bad option: "+key+"="+val);
                }
            }
            //-------------------------------------
            // options are selected when we need them
            // control them
            controlOptions(m_Def.getOptions());

                 
        }
        
        if (m_scriptDoc == null) {
            // should no happen
            wxtError.makeErrorMsgForDebugging(null, null, "buildModules: Script is not parsed in ScriptHandler:updateModules");
            //System.out.println("buildModules: Script is not parsed");
            m_Reporter.pushSimpleMessage("ERROR: Script is not parsed");
            return;
        }
        
        
        m_Reporter.clearMessages();
        m_Reporter.pushSimpleMessage("\n--------------------------\n");
        m_Reporter.pushSimpleMessage("Start updating module(s)");
        
        for (Module mod : m_RootModules) {
            update(mod);
         } 
        m_Reporter.pushSimpleMessage("Stop updating module(s)\n-----------------");
        
        // we must do a second phase to pick upp all PI's
        // that depends on all modules being built
        
        doSecondPhase();
    }

     /**
     * Building module mod and all children (recursively).
     * if some of the components have been changed since last save
     * 
     * @param mod. The root to start building from.
     */
    private void update(Module mod) 
    {

        // should we do this one
        if(mod.shouldBeUpdated()) 
            mod.build();
        if (mod.getChildCount() == 0) {
            return; 
        }
        
        // inspect all children as well
        Module m = (Module) mod.getFirstChild();
        while (m != null) {                
            update(m);
            m = (Module) m.getNextSibling();
        }                
    }

    /**
     * Start running through all modules for
     * those who are in need of a second phase.
     */
    private void doSecondPhase()
    {

        m_Reporter.pushSimpleMessage("Start building second phase");       
        for (Module mod : m_RootModules) {
            buildSecondPhase(mod);
         } 
        m_Reporter.pushSimpleMessage("Stop building second phase");
       
    }
     
    /**
     * Do a second phase build for a module and all it children (recursively).
     * <p>
     * Build only modules that are marked for second phase.
     * 
     * @param mod The module to build (if it is marked for second phase).
     */
    private void buildSecondPhase(Module mod) {
        if(mod.getSecondPhase())
            mod.buildSecondPhase();
        
        if (mod.getChildCount() == 0)
        {
            return; 
        }

        Module m = (Module) mod.getFirstChild();
        while (m != null) {                
            buildSecondPhase(m);
            m = (Module) m.getNextSibling();
        }                
    }

    
     /**
     * Find a module by its id.
      * 
     * @param mid The id we are looking for
     * @return the found module, if any. Otherwise null
     */  
    public Module getModuleById(String mid)
    {
        for (Module mod : m_RootModules) {
            Enumeration mods=mod.depthFirstEnumeration();
            while(mods.hasMoreElements())
            {
                Module m=(Module)mods.nextElement();
                if(m.getID().compareTo(mid)==0)
                    return m;
            }
         }
        // not found
        return null;
    }
    
    /**
     * Produce the module that is next to another module in rootlist.
     * 
     * @param mod the module for which we want the next
     * @return The next module, or null if mod is the last
     */
    public Module getNextRoot(Module mod)
    {
        for(int ix=0;ix < m_RootModules.size();ix++) 
        {
            Module m=m_RootModules.get(ix);
            if(m.equals(mod) && (ix !=  m_RootModules.size()-1))
            {
                return m_RootModules.get(ix+1);
            }

         }
        // not found, return null
        return null;       
    }
    
     /**
     * Produce the module that is previous to another module in rootlist.
     * 
     * @param mod the module for which we want the previuos
     * @return The previous module, or null if mod is the first
     */   
    public Module getPreviousRoot(Module mod)
    {
        for(int ix=0;ix < m_RootModules.size();ix++) 
        {
            Module m=m_RootModules.get(ix);
            if(m.equals(mod) && (ix !=  0))
            {
                return m_RootModules.get(ix-1);
            }

         }
        // not found, return null
        return null;       
    }

    /**
     * Get all modules as found in a preorder traversal of the (subtree of) modules.
     * 
     * @param rootid The id of the module that will be the root (null is all)
     * @return The list of modules
     */
    public List<Module> getAllModules(String rootid)
    {
        List<Module> result =new ArrayList<Module>();
        if(rootid==null)
        {
           // get all modules
            for (Module mod : m_RootModules) 
            {
                // add the root and its subtree in preorder sequence
                Enumeration all=mod.preorderEnumeration();
                while(all.hasMoreElements())
                     result.add((Module)all.nextElement());
             }
             return result;
        }

        // we go for the modules in the subtree defined by module with id rootid
        Module root=getModuleById(rootid);
        if(root==null)
            return null;
        // add the root and its subtree in preorder sequence
        Enumeration all=root.preorderEnumeration();
        // include the root
        result.add(root);
        all.nextElement();
        while(all.hasMoreElements())
             result.add((Module)all.nextElement());

        return result;
    }

    /**
     * Get all children of a module with a given id
     * @param rootid The id of the root
     * @return a list of modules
     */
    public List<Module> getAllChildModules(String rootid)
    {
        List<Module> result =new ArrayList<Module>();
        if(rootid==null)
           return null;

        // we go for the modules in the subtree defined by module with id rootid
        Module root=getModuleById(rootid);
        if(root==null)
            return null;
        // add the roots subtree in preorder sequence
        Enumeration all=root.preorderEnumeration();
        // jump the root
        all.nextElement();

        while(all.hasMoreElements())
             result.add((Module)all.nextElement());

        return result;
    }

    /**
     * Get a list of module by using an xpath on the script
     * @param xp The xpath
     * @return A list of modules
     */
    public List<Module> getModuleListByXpath(String xp)
    {
        List<Module> result =new ArrayList<Module>();
        try{
            NodeList mlist=domer.performXPathQuery(m_scriptDoc, xp);
            if((mlist==null)||(mlist.getLength()==0))
                    return null;
            for(int ix=0;ix<mlist.getLength();ix++)
            {
                Element modElt=(Element)mlist.item(ix);
                for(Module m:m_linearModuleList)
                {
                    if(m.getElement().equals(modElt))
                    {
                        result.add(m);
                        break;
                    }
                }
            }
            return result;
        }
        catch(Exception ex)
        {
            m_Reporter.pushMessage("could_not_do_xpath", xp, ex.getMessage());
            return null;
        }
    }
   
     /**
     * Get all modules with given id's as listed or preordered.
     * @param ids array with module ids
     * @return The list of modules
     */
    public List<Module> getModuleListById(String[] ids,boolean aslisted)
    {
        List<Module> result =new ArrayList<Module>();
        if(aslisted)
        {
            // get them as they are listed in ids
            for(int ix=0;ix<ids.length;ix++)
            {
                Module m=getModuleById(ids[ix]);
                if(m==null)
                    continue;
                if(m.getBooks().indexOf(PIcommand.NEVER)==-1)
                    result.add(m);
            }
            return result;
        }
        // get them preordered
        for (Module mod : m_RootModules)
        {
            Enumeration all=mod.preorderEnumeration();
            while(all.hasMoreElements())
            {
                Module m=(Module)all.nextElement();
                String mid=m.getID();
                String mib=m.getBooks();
                if((mib.compareTo(PIcommand.NEVER)!=0)&&
                   (accessutils.indexOfNameInList(mid,ids)!= -1))
                        result.add(m);
            }
         }
         return result;     
     }

    
    /**
     * Get all modules belongning to a list of books.
     * 
     * @param bklist The books we are looking for
     * @return A list of modules
     */
    public List<Module> getModulesInBook(String[] bklist) 
    {
        List<Module> result =new ArrayList<Module>();
         
        for (Module mod : m_RootModules) 
        {
            Module m=mod;
            while(m!=null)
            {
                String mib=m.getBooks();               
                if(accessutils.isAnyNameInList(mib.split(","),bklist))
                        result.add(m);
                m=(Module)m.getNextNode();
            }
         }
         return result;        
    }

     /**
      * Check if current build is building the complete script
      * @return true if all, otherwise false
      */
     public boolean isBuildingAll()
     {
         return m_buildingAll;
     }
    
    /**
     * Get the report as collected in the reporter.
     * 
     * @return The report as a string
     */
    public String getReport()
    {
        // parameter is any string that is usefull for
        // identifiing the script when displayed.
        return m_Reporter.getReport(m_scriptPath);
               
    }
    
    /**
     * Generates a string describing a Module
     * @param mid The id of the module we look for
     * @return The generated string
     */
    public String getModuleDescription(String mid)
    {
        Module m=getModuleById(mid);
        if(m!=null)
            return m.Inspector();
        else
            return reporter.getBundleString("unknown_module_posible_list");
    }

    /**
     * Get the absolute URI for a module
     * @param mid The id of the module
     * @return The absolute URI as a string
     */
    public String getModuleAddress(String mid)
    {
        Module m=getModuleById(mid);
        if(m!=null)
            return m.getAbsoluteUri().toString();
        else
            return "";

    }


    /**
     * Produce a string with a modules id tab a modules absolute address
     * on each line
     * @return a string with one line pr module
     */
    public String getModuleLocations()
    {
        String result="";
        for(Module m:m_linearModuleList)
        {
            result+=m.getID()+"\t"+m.getAbsoluteUri()+"\n";
        }
        return result;
    }

    /**
     * get a unique id for a module
     * @param modid The id of the module
     * @return an id modid+i
     */
    public String getANewId(String modid)
    {
        return m_idprovider.getUniqueID(modid);
    }

    /**
     * Access the version as a string: v.yyyy.mm.dd
     * @return The version compiled
     */
    public String getVersion()
    {
        return m_resourceHandler.getVersion();
    }



    @Override
    public String toString() {
        
        String s = "\nScripthandler: "+m_scriptPath; 
        
        s += "\n" + m_Def.toString(); 
               
        
        for (Module m: m_RootModules)
            s+= "\n" + m.toString(); 
        

        
        return s;
    }
 }


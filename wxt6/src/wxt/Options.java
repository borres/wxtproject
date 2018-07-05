package wxt;

import java.util.HashMap;
import java.util.Set;
import reporting.reporter;
import utils.encoderutils;

/**
 * Hold the options that are usable for building
 * Options are either 
 * set in the script as option elements
 * or 
 * set as parameters in the run command or set from a GUI
 * The parameters rule
 * 
 * Parameter  'script' an 'modules' are also kept as option
 * @author Administrator
 */
public class Options {
   
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
        
    
    // Default set of optione
    HashMap<String, String> OP;
    
    reporter REP;
    
    public Options(reporter rep){
        REP=rep;
        // initialize with defaults
        OP=new HashMap<>();
        OP.put(DEFAULT_ENCODING,"UTF-8");
        OP.put(INDENT_OUTPUT,YES);
        OP.put(EXPAND_ALL,NO);
        OP.put(OUTPUT_FORMAT,HTML);
        OP.put(REFERENCE_FORM,SIMPLE);
        OP.put(REFERENCE_INDEXING,LOCAL);
        OP.put(USE_COPY,YES);
        OP.put(DROP_BOOKS,DROP_NONE);
        OP.put(CONTROL_REFERENCES,NO);
        OP.put(PREFORMAT_LANGUAGE,NO);
        OP.put(TIDY_INPUT,NO);
        OP.put(VERBOSE,YES);
        
        OP.put(MODULES,NO_MODULES);
        OP.put(SCRIPT, NO_SCRIPT);
    }
    
    public Options(Options op){
        REP=op.getReporter();
        OP=new HashMap<>();
        OP.putAll(op.getAllOptions());
    }
    
    /**
     * Get the reporter
     */
    public reporter getReporter(){ 
        return REP;
    }

    public void setDefault(String option){
        switch (option){
            case DEFAULT_ENCODING:      OP.put(DEFAULT_ENCODING, "UTF-8");break;
            case INDENT_OUTPUT:         OP.put(INDENT_OUTPUT,YES);break;
            case EXPAND_ALL:            OP.put(EXPAND_ALL,NO);break;
            case OUTPUT_FORMAT:         OP.put(OUTPUT_FORMAT,HTML);break;
            case REFERENCE_FORM:        OP.put(REFERENCE_FORM,SIMPLE);break;
            case REFERENCE_INDEXING:    OP.put(REFERENCE_INDEXING,LOCAL);break;
            case USE_COPY:              OP.put(USE_COPY,YES);break;
            case DROP_BOOKS:            OP.put(DROP_BOOKS,DROP_NONE);break;
            case CONTROL_REFERENCES:    OP.put(CONTROL_REFERENCES,NO);break;
            case PREFORMAT_LANGUAGE:    OP.put(PREFORMAT_LANGUAGE,NO);break;
            case TIDY_INPUT:            OP.put(TIDY_INPUT,NO);break;
            case VERBOSE:               OP.put(VERBOSE,YES);
            case SCRIPT:                OP.put(SCRIPT, NO_SCRIPT);
            case MODULES:               OP.put(MODULES, NO_MODULES);
            default:                    ;
        }
                
    }
    
    /**
     * Get value of an option
     */
    public String getOption(String key){
        if(isLegalOption(key))
                return OP.get(key);
        REP.pushMessage("bad_option",key);
        return NO_VALUE;
    }
    
    /**
     * Check if an option is known and legal
     * @param  key The option to check
     */
    public boolean isLegalOption(String key){
        return OP.containsKey(key);
    }
    
     /**
     * Get a copy of all options
     * @return A hashmap
     */
    public HashMap<String,String> getAllOptions(){
        return OP;
    }
    
    /**
     * Set options from a hashmap
     * Typically called from Build or Update in scripthandler
     * The hashmap is not necessary complete 
     * and only value that are different are changed
     * @param hm The hashmap to copy
     */
    public void setAllOptions(HashMap<String,String> hm){
        //OP.putAll(co);
        // do this with control
        Set<String> keys=hm.keySet();
        for(String k:keys){
            setOption(k,hm.get(k));
        }

    }
    
    /**
     * Set new value to an option
     * Does a rudimantary control of values
     * @param key The option to set
     * @param value The value to set
     * @return the old value if ok, null otherwise
     */
    public String setOption(String key,String value){
       if(!OP.containsKey(key)) {
           REP.pushMessage("bad_option",key);
           return null;
        }
        
       //must not spoil module IDs or drop book values
       if(!key.equals(MODULES) && !key.equals(DROP_BOOKS)) {
            value=value.toLowerCase();
        }   
       
       String oldvalue=OP.get(key);
        // only yes and no are legal for those
        if(key.equals(INDENT_OUTPUT)||
                key.equals(EXPAND_ALL)||
                key.equals(CONTROL_REFERENCES)||
                key.equals(PREFORMAT_LANGUAGE)||
                key.equals(VERBOSE))
        {
            if(value.equals(NO) || value.equals(YES)){
                OP.put(key, value);
                return oldvalue;              
            }
            REP.pushMessage("bad_option",key+"="+value);
            setDefault(key);
            return null;
        }
        
        if(key.equals(USE_COPY)){
            if(value.equals(NO) || value.equals(YES) || value.equals(NEUTRAL)){
                OP.put(key, value);
                return oldvalue;              
            }
            REP.pushMessage("bad_option",key+"="+value);
            setDefault(key);
            return null;
           
        }
        
        if(key.equals(TIDY_INPUT)){
            if(value.equals(NO) || value.equals(YES)){
                OP.put(key, value);
                return oldvalue;                
            }
            REP.pushMessage("bad_option",key+"="+value);
            setDefault(key);
            return null;              
            
        }
        if(key.equals(REFERENCE_INDEXING)){
            if(value.equals(GLOBAL)||value.equals(LOCAL)){
                OP.put(key, value);
                return oldvalue;              
            }
            REP.pushMessage("bad_option",key+"="+value);
            setDefault(key);
            return null;
        }
       if(key.equals(REFERENCE_FORM)){
            if(value.equals(SIMPLE)||value.equals(IEEE)||value.equals(HARVARD)){
                OP.put(key, value);
                return oldvalue;              
            }
            REP.pushMessage("bad_option",key+"="+value);
            setDefault(key);
            return null;
        }
       if(key.equals(OUTPUT_FORMAT)){
            if(value.equals(HTML)||value.equals(XHTML)||value.equals(TEXT)||value.equals(XML)){
                OP.put(key, value);
                return oldvalue;             
            }
            REP.pushMessage("bad_option",key+"="+value);
            setDefault(key);
            return null;
        }
        if(key.equals(DEFAULT_ENCODING)) {        
            // is this a legal and known encoding
            String tmp = encoderutils.getLegalEncoding(value);
            if (tmp == null) {
                REP.pushMessage("bad_option", DEFAULT_ENCODING + "=" + tmp);
                // set default
                setDefault(key);
                return null;
            } 
            else {
                OP.put(key, tmp);
                return oldvalue;
            }
        }

        // drop books, script
       OP.put(key, value);
       return oldvalue;                    
   }
    
    public static String getHTMLDescription(){
        StringBuffer sb=new StringBuffer(1000);
sb.append("<html><div style=\"margin-left:10px\"");
sb.append("<h3>Expand All</h3>");
sb.append("<p>If selected, all expansions will be done, regardless of the<br> ");
sb.append("attribute expanded in the expand PI.</p>");

sb.append("<h3>Use Backup</h3>");
sb.append("<p>If selected WXT will import (wiki and odt) material from a local copy.<br>");
sb.append("Otherwise WXT will access the material at the original source, produce a <br>");
sb.append("local copy and fetch the result.<br>");
sb.append("Overriding the use-copy attribute in PI importwiki and PI importodt</p>");

sb.append("<h3>Output Format</h3>");
sb.append("<p>Possible values are <em>xml</em>, <em>text</em>,<em>html</em> and <em>xhtml</em>. Default is <em>html</em>.<br>");
sb.append("This will produce modules <strong>without</strong> xml-header in a format that is safe for most browsers. <br>");
sb.append("<em>xhtml</em> will produce the same as <em>xml</em>,");
sb.append("but without xml-header.</p>");

sb.append("<h3>Default Encoding</h3>");
sb.append("<p>Default is <em>utf-8</em>. This encoding is used<br>");
sb.append("when there are no other way to decide encoding.</p>");

sb.append("<h3>Preformat Languges</h3>");
sb.append("<p>Possible values are <em>yes</em>, <em>no</em>.<br>");
sb.append("Default is <em>no</em>. Google's prettyprint will be used.<br> ");
sb.append("<em>yes</em> wil turn on WXT's own colorcoding which is less flexible but will prepare<br>");
sb.append("material independant of webbrowser. Suited for PDF conversion.</p>");

sb.append("<h3>Reference Indexing</h3>");
sb.append("<p>Possible values are <em>local</em>, <em>global</em>.<br>");
sb.append("<em>local</em> will produce reference indices for each module.<br>");
sb.append("<em>global</em> will index references as encountered during the<br> ");
sb.append("whole building. Default is <em>local</em>.</p>");

sb.append("<h3>Drop Books</h3>");
sb.append("<p>Modules with the given books-attribute are excluded from build, even if they are selected<br>");
sb.append("Usefull when you are building scripts with timeconsuming, low frequency modules<br>");
sb.append("The books - value may be a commaseparated list.");

sb.append("<h3>Verbose</h3>");
sb.append("<p>The building report will contain more information of empty and failed constructs<br>");
sb.append("Default is off</p></div>");
           
        return sb.toString();
    }
    
    
    @Override
    public String toString(){
        StringBuilder sb=new StringBuilder();
        Set<String> keys=OP.keySet();
        for(String k:keys){
            sb.append(k).append("  :  ").append(OP.get(k)).append("\n");
        }
        return sb.toString();
    }

}

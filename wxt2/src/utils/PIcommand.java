package utils;

import content.Definitions;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Parses and stores the content of a Processing Instruction node according
 * to a name-value, attribute-like, philosophy.
 * <p>
 * Example: &lt;?_wxt import uri=""?&gt;, 
 * in which case import is the main command, m_Cmd, of this PI.
 * </p>
 */
public class PIcommand extends java.lang.Object 
{
    // define constants for PI handling
    // define commands
    public static final String IMPORTXML="importxml";
    public static final String IMPORTTXT="importtxt";
    public static final String IMPORTWIKI="importwiki";
    public static final String IMPORTDB="importdb";
    public static final String IMPORTODF="importodf";
    public static final String REF="ref";
    public static final String REFLIST="reflist";
    public static final String REFERENCETEST="referencetest";
    public static final String FRAGMENT="fragment";
    public static final String POPFRAGMENT="popfragment";
    public static final String XLINK="xlink";
    public static final String MODULEMAP="modulemap";
    public static final String XLINKLIST="xlinklist";
    public static final String PATH="path";
    public static final String MODULETOC="moduletoc";
    public static final String MODULETOCFINAL="moduletocfinal";
    public static final String IXWORD="ixword";
    public static final String IXTABLE="ixtable";
    public static final String COLLECT="collect";
    public static final String DATE="date";
    public static final String TIME="time";
    public static final String STAMP="stamp";
    public static final String MODULEMENU="modulemenu";
    public static final String COLLECTSUMMARY="collectsummary";
    public static final String FOOTNOTE="footnote";
    public static final String DEMOLINK="demolink";
    public static final String EXPAND="expand";
    public static final String EXPANDSIMPLE="expandsimple";
    public static final String FORMULA="formula";
    public static final String IMAGE="image";
    public static final String IMAGELIST="imagelist";
    public static final String BOOKONDEMAND="bookondemand";
    public static final String RESET="reset";
    public static final String COLLECTREMOTE="collect-remote";
    public static final String AUTHORLIST="authorlist";
    public static final String AUTHORS="authors";
    public static final String GADGET="gadget";
    
    // define parameternames
    public static final String LOCATION="location";
    public static final String BACKUPLOCATION="backuplocation";
    public static final String BACKUP="backup";
    public static final String URI="uri";
    public static final String SQLFILE="sqlfile";
    public static final String SOURCELOCATION="sourcelocation";
    public static final String TARGETLOCATION="targetlocation";
    public static final String TEMPLATE="_template";
    public static final String FRAGMENTID="fragmentid";
    
    public static final String XPATH="xpath";
    public static final String SCRIPTPATH="scriptpath";
    public static final String TRANSFORMATION="transformation";
    public static final String LEFTPAR="leftpar";
    public static final String RIGHTPAR="rightpar";
    public static final String SELECT="select";
    public static final String REPLACE="replace";
    public static final String REPLACE0="replace0";
    public static final String CODE="code";
    public static final String PARSE="parse";
    public static final String ID="id";
    public static final String BOOKS="books"; 
    public static final String IDLIST="idlist";
    public static final String ROOT="root";
    public static final String DIVIDER="divider";
    public static final String TAGS="tags";
    public static final String COLS="cols";
    public static final String WORD="word";
    public static final String CATEGORY="category";
    public static final String COMMENT="comment";
    public static final String COUNTRY="country";
    public static final String FORM="form";
    public static final String ENCODING="encoding";
    public static final String EXPANDED="expanded";
    public static final String POPUP="popup";
    public static final String ACTUALDATE="actualdate";
    public static final String TEXT="text";
    public static final String TITLE="title";
    public static final String CONNECT="connection";
    public static final String SQL="sql";
    public static final String DRIVER="driver";
    public static final String STYLE="style";
    public static final String TARGET="target";
    public static final String FIRSTDATE="firstdate";
    public static final String LASTDATE="lastdate";
    public static final String CLOCK="clock";
    public static final String LSUMMARY="summary";
    public static final String INDEX="index";
    public static final String SCOPE="scope";
    public static final String DISPLAY="display";
    public static final String MARKWORD="markword";
    public static final String DPATH="dpath";
    public static final String LINK="link";
    public static final String NEWID="newid";
    public static final String USECOPY="usecopy";
    public static final String SUBTEXT="subtext";
    public static final String ALT="alt";
    public static final String CLASS="class";
    public static final String DROPDEFAULT="dropdefault";
    public static final String POSITION="position";
    public static final String MOVABLE="movable";
    public static final String THUMBSIZE="thumbsize";
    public static final String SPLIT="split";
    // formulas as image
    public static final String COLOR="color";
    public static final String BACKCOLOR="backcolor";
    public static final String FONTSIZE="fontsize";
    //public static final String NAMEONLY="nameonly";
    
    
    // define parameter value constants
    public static final String CONTENT="_content";
    public static final String YES="yes";
    public static final String NO="no";
    public static final String DESCRIPTION="_description";
    public static final String NAME="_name";
    public static final String PREV="_prev";
    public static final String NEXT="_next";
    public static final String NONE="_none";
    public static final String PARENT="_parent";
    public static final String NEXTSIBLING="_nextsibling";
    public static final String PREVSIBLING="_prevsibling";
    public static final String SIBLINGS="_siblings";
    public static final String CHILDREN="_children";
    public static final String ALL="_all";
    public static final String RANDOM="_random";
    public static final String LEAVE="leave";
    public static final String SUMMARY="_summary";    
    public static  String REPLACESPLITTER="|";
    public static final String REMOVE="remove";
    public static final String NORMAL="normal";
    public static final String SHOW="show";
    public static final String TABLE="_table";
    public static final String HOME="_home";
    public static final String VROOT="_root";
    public static final String KEEPREFERENCES="keeprefs";
    public static final String NEVER="_never";    
    public static final String LOCAL="local";
    public static final String GLOBAL="global";
    public static final String SIMPLE="simple";
    public static final String SHORT="short";
    public static final String MEDIUM="medium";
    public static final String LONG="long";
    public static final String FULL="full";
    public static final String WIDTH="width";
    public static final String HEIGHT="height";
    public static final String AUTHOR="_author";
    public static final String AUTHORSHORT="_authorshort";
    public static final String LEFT="left";
    public static final String TOP="top";

    // define allowed code types
    public static final String XMLCODE="xmlcode";
    public static final String MLCODE="mlcode";
    public static final String JAVACODE="javacode";
    public static final String JAVASCRIPTCODE="javascriptcode";
    public static final String CCODE="ccode";
    public static final String CPPCODE="cppcode";
    public static final String CSHARPCODE="csharpcode";
    public static final String PYTHONCODE="pythoncode";
    public static final String XSLTCODE="xsltcode";
    public static final String ASPXCODE="aspxcode";
    public static final String CS3CODE="cs3code";
    public static final String MATLABCODE="matlabcode";
    public static final String HTMLCODE="htmlcode";
    public static final String CSSCODE="csscode";

    public static final String WXTSTYLEPREFIX="wxt";

    public static final String SKIPTRANSLATE="skiptranslate";


    
      
    /**The main command, what to do (ie. importtxt)*/
    protected String m_Cmd=null;    
    /**The string as we receive it in the constructor */
    protected String m_originalData=null;   
    /**The parameters as name value pairs*/
    protected HashMap<String,String> m_params;
    /** if want access to definitions */
    protected Definitions m_def;
    
    /** constant for flagging ok format in controlPI()*/
    public static final String OK="OK";
    
         
     /**
     * Creates a new instance of command.
     * 
     * @param data is the content of a PI, except the name(_wxt)
     * @param theDef The Definitions object that are actual
     * @throws Exception when parse error
     */
    public PIcommand(String data,Definitions theDef) 
    throws Exception {
        m_originalData=data;
        if(!parse(data))
            throw new Exception("bad_format");
        m_def=theDef;
        if(!nonEmptyValues())
            throw new Exception("bad_format");
        //check dating
        String first="1030:08:29";
        String last ="3000:01:01";
        if(paramExist(FIRSTDATE))
            first=getValue(FIRSTDATE);
        if(paramExist(LASTDATE))
            last=getValue(LASTDATE);
        try{
            if(!accessutils.legalDating(first,last))
                throw new Exception(reporter.getBundleString("pi_out_of_date"));
            
            // PI control
            String report=controlPI();

            if(report.compareTo(OK)!=0)
                throw new Exception("\n\t"+report);
         }       
        catch(Exception e){
            // bad format
            throw new Exception(e.getMessage());
        }
            
    }
    
    /**
     * Check if a code parameter is legal
     * @param v the code to check
     * @return true if it is ok, false otherwise
     */
    private boolean legalCodeValue(String v)
    {
        return (v.compareTo(CODE)==0)||
               (v.compareTo(XMLCODE)==0) ||
               (v.compareTo(MLCODE)==0) ||
               (v.compareTo(JAVACODE)==0) ||
               (v.compareTo(JAVASCRIPTCODE)==0) ||
               (v.compareTo(CCODE)==0) ||
               (v.compareTo(CPPCODE)==0) ||
               (v.compareTo(CSHARPCODE)==0) ||
               (v.compareTo(PYTHONCODE)==0) ||
               (v.compareTo(XSLTCODE)==0) ||
               (v.compareTo(ASPXCODE)==0) ||
               (v.compareTo(CS3CODE)==0) ||
               (v.compareTo(HTMLCODE)==0) ||
               (v.compareTo(CSSCODE)==0) ||
               (v.compareTo(MATLABCODE)==0);
    }
    

    /**
     * Returns the main command.
     * 
     * @return The main command (ie. import)
     */
    public String getCommand(){
        return m_Cmd;
    }

     /**
     * Returns the main command perfixed with wxt for stylenaming.
     *
     * @return The main command (ie. import)
     */
    public String getCommandStyleName(){
        return WXTSTYLEPREFIX+m_Cmd;
    }

    
    /** Returns a value.
     * 
     * @param n The name to look for
     * @return The value associated with the name, null if not found
     */  
    public String getValue(String n){
        return m_params.get(n);
    }
    
    /** 
     * Returns an integer value.
     * 
     * @param n The name we are looking for
     * @return The matching value as an integer, -1 if not found or not integer
     */  
    public int getValueAsInteger(String n){
        if(paramExist(n)){
            try{
                return Integer.parseInt(getValue(n));
            }
            catch(NumberFormatException e)
            {
                return Integer.MIN_VALUE;
            }
        }
        return Integer.MIN_VALUE;
    }
    
    /** 
     * Investigates if a parameter exists.
     * 
     * @param n The name to look for
     * @return true if the string n exists as name, false otherwise
     */  
    public boolean paramExist(String n){
        return m_params.containsKey(n);
    }
    
    /** 
     * Parse the content of a PI.
     * <p>
     * Expected input: command name1="value1" name2="value2"
     * 
     * @param data The string to parse.
     * @return true if successful, false otherwise
     */
     protected boolean parse(String data){
         m_params=new HashMap<String,String>();
         if(data==null)
             return false;
        data=data.trim();
        if(data.length()==0)
             return false;
        
        // we dont want linebreaks or tabs
        data=data.replaceAll("\r\n"," ");
        data=data.replaceAll("\n"," ");
        data=data.replaceAll("\r"," ");
        data=data.replaceAll("\t"," ");
         
        // pick up main command
        int pos=data.indexOf(' ');
        if(pos ==-1){
            // no name-value pairs
            m_Cmd=data;
            return true;
        }
        m_Cmd=data.substring(0,pos).trim();
        data=data.substring(pos).trim()+" ";
        
        // only name value pairs left
        m_params.putAll(accessutils.parseNameValues(data,'\"'));
        if(paramExist(accessutils.NONAME)) // parseerror indicator
            return false;
        return true;
    }
     
     /**
      * Add or overwrite a parameter: name and value.
      * 
      * @param name The name, key
      * @param  value The value
      */
     public void setParameter(String name, String value)
     {
        if(name!=null)
            m_params.put(name, value);
     }
     
    
    /**
     * Check all m_params for empty value.
     * 
     * @return false if we have an empty value, true otherwise
     */
    protected boolean nonEmptyValues()
    {
        // iterate all params
        Set keys=m_params.keySet();
        for(Iterator adit=keys.iterator();adit.hasNext();)
        {
            String theKey=(String)adit.next();
            String theValue=getValue(theKey);
            if ((theValue==null)||(theValue.length()==0))
                return false;
        }
        return true;
    }
    
    /**
     * Produce the parameterlist as name="value" pairs.
     * 
     * @return A string containg the parameterlist
     */
    public String getParamList()
    {
        String T=" ";
        Set keys=m_params.keySet();
        for(Iterator adit=keys.iterator();adit.hasNext();){
            String theKey=(String)adit.next();
            String theValue=getValue(theKey);
            T+=theKey+"=\""+theValue+"\" ";
        }
        return T;
   }
    
    /**
     * Returns the original PI, as received in constructor.
     * 
     * @return Returns the original data as received in constructor as a string
     */
    public String getOriginalData()
    {
        return m_originalData;
    }
    
    /**
     * Returns the complete reconstructed PIcommand as a string.
     * 
     * @return The reconstructed PI
     */
    @Override public String toString()
    {
        //return m_originalData;
        //return "<?_wxt "+m_originalData+"?>";
        
        // returned reconstructed data
        String T="<?_wxt "+m_Cmd+" "+getParamList()+"?>";
        return T;
     }

    /**
     * Remove a parameter
     * @param value The key we want to remove
     */
    public void removeParameter(String value)
    {
        m_params.remove(value);
    }

    private boolean positiveInteger(String s)
    {
        try{
            return Integer.parseInt(s)>=0;
        }
        catch(Exception x){return false;}
    }
    
    /**
     * Control consistency of PI
     * Check that:<br>
     * 1 mandatory parameters are present<br>
     * 2 parameters with predefined value sets are correct
     * (typically yes or no)<br>
     * @return "OK" if it is ok, an error report otherwise
     */
    public String controlPI()
    {
        // ---- importXML
        if(m_Cmd.compareToIgnoreCase(IMPORTXML)==0)
        {
            if(!m_params.containsKey(XPATH))
                return " missing "+XPATH;
            return OK;
        }
        
        // ---- importTXT
        if(m_Cmd.compareToIgnoreCase(IMPORTTXT)==0)
        {
            if(m_params.containsKey(PARSE))
                if( (m_params.get(PARSE).compareTo(YES)!=0) &&(m_params.get(PARSE).compareTo(NO)!=0))
                    return  PARSE+" must be yes or no";
            if(m_params.containsKey(CODE))
                if(!legalCodeValue(m_params.get(CODE)))
                    return " unknown "+CODE;
            return OK;

        }
        
        // ---- importWIKI
        if(m_Cmd.compareToIgnoreCase(IMPORTWIKI)==0)
        {
            //if(!m_params.containsKey(LOCATION))
            //    return " missing "+LOCATION;
            if(m_params.containsKey(KEEPREFERENCES))
                if ((m_params.get(KEEPREFERENCES).compareTo(YES)!=0)&&(m_params.get(KEEPREFERENCES).compareTo(NO)!=0))
                    return  KEEPREFERENCES+" must be yes or no";
            if(m_params.containsKey(USECOPY))
                if ((m_params.get(USECOPY).compareTo(YES)!=0)&&(m_params.get(USECOPY).compareTo(NO)!=0))
                    return  USECOPY+" must be yes or no";
            if((!m_params.containsKey(XPATH))&&(!m_params.containsKey(DPATH)))
                return DPATH +" or "+ XPATH +" must be set";
            return OK;
        }

        //--- import odf--->
        if((m_Cmd.compareToIgnoreCase(IMPORTODF)==0))
        {
           //if(!m_params.containsKey(LOCATION))
           //     return LOCATION+" must be set";

           if(!m_params.containsKey(DPATH))
                return DPATH+" must be set";

           if(m_params.containsKey(USECOPY))
                if ((m_params.get(USECOPY).compareTo(YES)!=0)&&(m_params.get(USECOPY).compareTo(NO)!=0))
                    return  USECOPY+" must be yes or no";
           return OK;

        }
        
        // ---- importDB
        if(m_Cmd.compareToIgnoreCase(IMPORTDB)==0)
        {
            //if(!m_params.containsKey(CONNECT))
            //    return " missing "+CONNECT;
            if((!m_params.containsKey(SQL))&&(!m_params.containsKey(SQLFILE)))
                return " either "+SQL+" or "+SQLFILE+" must be set";
            if(m_params.containsKey(PARSE))
                if ((m_params.get(PARSE).compareTo(YES)!=0)&&(m_params.get(PARSE).compareTo(NO)!=0))
                    return  PARSE+" must be yes or no";
            return OK;        
        }
        
        // ----- collect
        if(m_Cmd.compareToIgnoreCase(COLLECT)==0)
        {
            if(!m_params.containsKey(XPATH))
                return " missing "+XPATH;
            return OK;
        }

        // ----- collect-remote
        if(m_Cmd.compareToIgnoreCase(COLLECTREMOTE)==0)
        {
            if(!m_params.containsKey(XPATH))
                return " missing "+XPATH;
            if(!m_params.containsKey(LOCATION))
                return " missing "+LOCATION;
            if((!m_params.containsKey(SCRIPTPATH)) &&
               (!m_params.containsKey(IDLIST))&&
               (!m_params.containsKey(BOOKS)))
                return " missing one (or more) of: "+SCRIPTPATH+","+IDLIST+","+BOOKS;
            return OK;
        }

        // ----- collectsummary
        if(m_Cmd.compareToIgnoreCase(COLLECTSUMMARY)==0)
        {
            if(m_params.containsKey(XLINK))
                if((m_params.get(XLINK).compareTo(YES)!=0)&&(m_params.get(XLINK).compareTo(NO)!=0))
                    return XLINK+" must be yes or no";
            if(m_params.containsKey(SELECT))
                if ((m_params.get(SELECT).compareTo(SIBLINGS)!=0) && (m_params.get(SELECT).compareTo(CHILDREN)!=0))
                    return SELECT+ " must be "+SIBLINGS+" or "+CHILDREN;
            return OK;
        }
        
        // ------- demolink
        if(m_Cmd.compareToIgnoreCase(DEMOLINK)==0)
        {
            if((!m_params.containsKey(URI)) && (!m_params.containsKey(LOCATION)))
                return " missing "+URI+" or "+LOCATION;
            return OK;
        }

        // ------- expandsimple
        if(m_Cmd.compareToIgnoreCase(EXPANDSIMPLE)==0)
        {
            if(!m_params.containsKey(LOCATION))
                return " missing "+LOCATION;
            
            if(m_params.containsKey(SOURCELOCATION))
                return " no "+SOURCELOCATION+ "allowed";
            
            
            if(m_params.containsKey(CODE))
                if(!legalCodeValue(m_params.get(CODE)))
                    return " unknown "+CODE;
            
            if(m_params.containsKey(EXPANDED))
                if((m_params.get(EXPANDED).compareTo(YES)!=0)&&(m_params.get(EXPANDED).compareTo(NO)!=0))
                    return EXPANDED+" must be yes or no";
            return OK;
        }

        // ------- fragment
        if(m_Cmd.compareToIgnoreCase(FRAGMENT)==0)
        {
            if(!m_params.containsKey(ID))
                return " missing "+ID;
            if(m_params.containsKey(FORM))
            {
                String tmp=m_params.get(FORM);
                if((tmp.compareTo(ID)!=0) &&
                   (tmp.compareTo(SHORT)!=0) &&
                   (tmp.compareTo(FULL)!=0))
                    return FORM+" must be "+ID+" , "+SHORT+" or "+ FULL;
            }

            return OK;
        }
        
        // ------- popfragment
        if(m_Cmd.compareToIgnoreCase(POPFRAGMENT)==0)
        {
            if(!m_params.containsKey(ID))
                return " missing "+ID;
            return OK;
        }

        // ---- author list
        if(m_Cmd.compareToIgnoreCase(AUTHORLIST)==0)
        {
           if(m_params.containsKey(SELECT))
                if((m_params.get(SELECT).compareTo(CHILDREN)!=0)&&(m_params.get(SELECT).compareTo(SIBLINGS)!=0))
                    return SELECT+ " must be "+SIBLINGS+" or "+CHILDREN;
           if(m_params.containsKey(DROPDEFAULT))
           {
                if((m_params.get(DROPDEFAULT).compareTo(YES)!=0)&&(m_params.get(DROPDEFAULT).compareTo(NO)!=0))
                    return DROPDEFAULT+" must be yes or no";
           }

           return OK;

        }
        
        // ------- modulemap
        if(m_Cmd.compareToIgnoreCase(MODULEMAP)==0)
        {
            if(m_params.containsKey(SUMMARY))
                if((m_params.get(SUMMARY).compareTo(YES)!=0)&&(m_params.get(SUMMARY).compareTo(NO)!=0))
                    return SUMMARY+" must be yes or no";
           if(m_params.containsKey(SELECT))
                if((m_params.get(SELECT).compareTo(CHILDREN)!=0)&&(m_params.get(SELECT).compareTo(SIBLINGS)!=0))
                    return SELECT+ " must be "+SIBLINGS+" or "+CHILDREN;
            if(m_params.containsKey(COLS))
            {
                if(!positiveInteger(m_params.get(COLS)))
                    return COLS+" must be an integer >= 0";
            }
           return OK;
        }

        // ------- modulemenu
        if(m_Cmd.compareToIgnoreCase(MODULEMENU)==0)
        {
            if(m_params.containsKey(SUMMARY))
                if((m_params.get(SUMMARY).compareTo(YES)!=0)&&(m_params.get(SUMMARY).compareTo(NO)!=0))
                    return SUMMARY+" must be yes or no";
           if(m_params.containsKey(SELECT))
                if((m_params.get(SELECT).compareTo(CHILDREN)!=0)&&(m_params.get(SELECT).compareTo(SIBLINGS)!=0))
                    return SELECT+ " must be "+SIBLINGS+" or "+CHILDREN;
            if(m_params.containsKey(COLS))
            {
                if(!positiveInteger(m_params.get(COLS)))
                    return COLS+" must be an integer >= 0";
            }
            
           return OK;
        }


       // ------- popup
        if(m_Cmd.compareToIgnoreCase(POPUP)==0)
        {
            if(!m_params.containsKey(LOCATION))
                return " missing "+LOCATION;
            
            if(((m_params.containsKey(LEFTPAR))||
               (m_params.containsKey(RIGHTPAR))||
               (m_params.containsKey(REPLACE))||
               (m_params.containsKey(SELECT))||
               (m_params.containsKey(PARSE))||
               (m_params.containsKey(CODE)))
               &&(!m_params.containsKey(SOURCELOCATION)))
                 return " missing "+SOURCELOCATION;
            
            String tmp1=m_params.get(LOCATION);
            String tmp2=m_params.get(SOURCELOCATION);
            
            if((tmp2!=null) && (tmp1.compareTo(tmp2)==0))
                return " cannot have identical "+LOCATION+" and "+SOURCELOCATION;
            
            if(m_params.containsKey(CODE))
                if(!legalCodeValue(m_params.get(CODE)))
                    return " unknown "+CODE;
            
            return OK;
        }

        // ------- expand as expandajax
        if(m_Cmd.compareToIgnoreCase(EXPAND)==0)
        {
            if(!m_params.containsKey(LOCATION))
                return " missing "+LOCATION;

            if(((m_params.containsKey(LEFTPAR))||
               (m_params.containsKey(RIGHTPAR))||
               (m_params.containsKey(REPLACE))||
               (m_params.containsKey(SELECT))||
               (m_params.containsKey(PARSE))||
               (m_params.containsKey(CODE)))
               &&(!m_params.containsKey(SOURCELOCATION)))
                 return " missing "+SOURCELOCATION;

            String tmp1=m_params.get(LOCATION);
            String tmp2=m_params.get(SOURCELOCATION);

            if((tmp2!=null) && (tmp1.compareTo(tmp2)==0))
                return " cannot have identical "+LOCATION+" and "+SOURCELOCATION;

            if(m_params.containsKey(CODE))
                if(!legalCodeValue(m_params.get(CODE)))
                    return " unknown "+CODE;

            return OK;
        }

       // ------- xlink
       if(m_Cmd.compareToIgnoreCase(XLINK)==0)
       {
            if(!m_params.containsKey(ID))
                return " missing "+ID;
            
            if(m_params.containsKey(SUMMARY))
                if((m_params.get(SUMMARY).compareTo(YES)!=0)&&(m_params.get(SUMMARY).compareTo(NO)!=0))
                    return SUMMARY+" must be yes or no";
            return OK;

        }
               
        //--- reference -->
        if(m_Cmd.compareToIgnoreCase(REF)==0)
        {
            if(!m_params.containsKey(ID))
                return " missing "+ID;
 
            return OK;

        }

        //--- reference list -->
        if(m_Cmd.compareToIgnoreCase(REFLIST)==0)
        {
           if(m_params.containsKey(SCOPE))
           {
                if((m_params.get(SCOPE).compareTo(GLOBAL)!=0)&&(m_params.get(SCOPE).compareTo(LOCAL)!=0))
                    return SCOPE+" must be global or local";           
           }
           if(m_params.containsKey(RESET))
           {
                if((m_params.get(RESET).compareTo(YES)!=0)&&(m_params.get(RESET).compareTo(NO)!=0))
                    return RESET+" must be yes or no";           
           }

            return OK;

        }

        //--- ixword --->
        if((m_Cmd.compareToIgnoreCase(IXWORD)==0))
        {
           if(!m_params.containsKey(WORD))
           {
                return WORD+" must be set";
           }
           return OK;

        }

        //--- formula--->
        if((m_Cmd.compareToIgnoreCase(FORMULA)==0))
        {
           if((!m_params.containsKey(ID))&&(!m_params.containsKey(LOCATION)))
           {
                return ID + "or"+ LOCATION+" must be set";
           }
           return OK;

        }
        //--- image--->
        if(m_Cmd.compareToIgnoreCase(IMAGE)==0)
        {
           if(!m_params.containsKey(ID))
           {
                return ID +" must be set";
           }
           if (m_params.containsKey(DISPLAY))
           {
               String tmp=m_params.get(DISPLAY);
               if((tmp.compareTo("0")!=0)&&
                  (tmp.compareTo("1")!=0)&&
                  (tmp.compareTo("2")!=0)&&
                  (tmp.compareTo("3")!=0)&&
                  (tmp.compareTo("4")!=0))
                    return DISPLAY + "must be: 0 or 1 or 2 or 3 or 4";
           }
               
           return OK;

        }
        //--- imagelist --->
        if(m_Cmd.compareToIgnoreCase(IMAGELIST)==0)
        {
              if(paramExist(SPLIT))
                if((m_params.get(SPLIT).compareTo(YES)!=0)&&
                   (m_params.get(SPLIT).compareTo(NO)!=0))
                    return SPLIT+" must be yes or no";
              return OK;
        }
        
        //--- footnote --->
        if(m_Cmd.compareToIgnoreCase(FOOTNOTE)==0)
        {
            if(m_params.containsKey(FORM))
            {
                String f=m_params.get(FORM);
                if( (f.compareTo(NORMAL)==0)||
                       (f.compareTo(SHOW)==0)||
                       (f.compareTo(REMOVE)==0))
                       return OK;
                else
                    return FORM+" must be one of: "+NORMAL+","+SHOW+","+REMOVE;
            }
            else
                return OK;
        }
        //--- date --->
       if(m_Cmd.compareToIgnoreCase(DATE)==0)
        {
            if(m_params.containsKey(FORM))
            {
                String f=m_params.get(FORM);
                if( (f.compareTo(SHORT)==0)||
                       (f.compareTo(MEDIUM)==0)||
                       (f.compareTo(LONG)==0)||
                       (f.compareTo(FULL)==0))
                       return OK;
                else
                    return FORM+" must be one of: "+SHORT+","+MEDIUM+","+LONG+","+FULL;
            }
            else
                return OK;
        }
        //--- time --->
       if(m_Cmd.compareToIgnoreCase(TIME)==0)
        {
            if(m_params.containsKey(FORM))
            {
                String f=m_params.get(FORM);
                if( (f.compareTo(SHORT)==0)||
                       (f.compareTo(MEDIUM)==0)||
                       (f.compareTo(LONG)==0)||
                       (f.compareTo(FULL)==0))
                       return OK;
                else
                    return FORM+" must be one of: "+SHORT+","+MEDIUM+","+LONG+","+FULL;
            }
            else
                return OK;
        }
        //-- ixtable
        if(m_Cmd.compareToIgnoreCase(IXTABLE)==0)
        {
            if(m_params.containsKey(COLS))
            {
                if(!positiveInteger(m_params.get(COLS)))
                    return COLS+" must be an integer >= 0";
            }
            return OK;
        }
        //-- moduletoc
        if(m_Cmd.compareToIgnoreCase(MODULETOC)==0)
        {
            if(m_params.containsKey(COLS))
            {
                if(!positiveInteger(m_params.get(COLS)))
                    return COLS+" must be an integer >= 0";
            }
            return OK;
        }
        //-- moduletoc final
        if(m_Cmd.compareToIgnoreCase(MODULETOCFINAL)==0)
        {
            if(m_params.containsKey(COLS))
            {
                if(!positiveInteger(m_params.get(COLS)))
                    return COLS+" must be an integer >= 0";
            }
            return OK;
        }
        //-- authors
        if(m_Cmd.compareToIgnoreCase(AUTHORS)==0)
        {
            if(m_params.containsKey(FORM))
            {
                String tmp=m_params.get(FORM);
                if((tmp.compareTo(ID)!=0) &&
                   (tmp.compareTo(SHORT)!=0) &&
                   (tmp.compareTo(FULL)!=0))
                    return FORM+" must be "+ID+" , "+SHORT+" or "+ FULL;
            }
            return OK;
        }
        //-- gadget
        if(m_Cmd.compareToIgnoreCase(GADGET)==0)
        {
            if((!paramExist(LOCATION))&&(!paramExist(FRAGMENTID)))
                return GADGET+" must have "+ LOCATION+" or "+FRAGMENTID;
            if(!paramExist(WIDTH))
                return GADGET+" must have "+WIDTH;
            if(!paramExist(HEIGHT))
                return GADGET+" must have "+HEIGHT;
            if(paramExist(EXPANDED))
                if((m_params.get(EXPANDED).compareTo(YES)!=0)&&
                   (m_params.get(EXPANDED).compareTo(NO)!=0))
                    return EXPANDED+" must be yes or no";
            if(paramExist(POSITION))
                if( (m_params.get(POSITION).compareTo("absolute")!=0)&&
                    (m_params.get(POSITION).compareTo("fixed")!=0)&&
                    (m_params.get(POSITION).compareTo("relative")!=0)&&
                    (m_params.get(POSITION).compareTo("static")!=0)&&
                    (m_params.get(POSITION).compareTo("inherited")!=0))
                        return POSITION+" must be a legal CSS-value";
              if(paramExist(MOVABLE))
                if((m_params.get(MOVABLE).compareTo(YES)!=0)&&
                   (m_params.get(MOVABLE).compareTo(NO)!=0))
                    return MOVABLE+" must be yes or no";


            return OK;
        }



        
        // ---------- the rest
        if((m_Cmd.compareToIgnoreCase(PATH)==0)||
           (m_Cmd.compareToIgnoreCase(STAMP)==0)||
           (m_Cmd.compareToIgnoreCase(REFERENCETEST)==0))
           return OK;
        
        return " unknown PI ";
   }
    /**
     *  allowed code types
    XMLCODE="xmlcode";
    MLCODE="mlcode";
    JAVACODE="javacode";
    JAVASCRIPTCODE="javascriptcode";
    CCODE="ccode";
    CPPCODE="cppcode";
    CSHARPCODE="csharpcode";
    PYTHONCODE="pythoncode";
    XSLTCODE="xsltcode";
    ASPXCODE="aspxcode";
    CS3CODE="cs3code";
    MATLABCODE="matlabcode";
    HTMLCODE="htmlcode";
    CSSCODE="csscode";

     * @param c Current class value
     * @return new classvalue
     */
    public static String translateCodeClass(String c)
    {
        if(c.compareToIgnoreCase(XMLCODE)==0) return "prettyprint lang-xml";
        if(c.compareToIgnoreCase(MLCODE)==0) return "prettyprint lang-ml";
        if(c.compareToIgnoreCase(JAVACODE)==0) return "prettyprint lang-j";
        if(c.compareToIgnoreCase(JAVASCRIPTCODE)==0) return "prettyprint lang-js";
        if(c.compareToIgnoreCase(CCODE)==0) return "prettyprint lang-c";
        if(c.compareToIgnoreCase(CPPCODE)==0) return "prettyprint lang-cpp";
        if(c.compareToIgnoreCase(CSHARPCODE)==0) return "prettyprint lang-csh";
        if(c.compareToIgnoreCase(PYTHONCODE)==0) return "prettyprint lang-py";
        if(c.compareToIgnoreCase(XSLTCODE)==0) return "prettyprint lang-xml";
        if(c.compareToIgnoreCase(ASPXCODE)==0) return "prettyprint lang-asp";
        if(c.compareToIgnoreCase(CS3CODE)==0) return "prettyprint lang-cs3";
        if(c.compareToIgnoreCase(MATLABCODE)==0) return "prettyprint lang-mal";
        if(c.compareToIgnoreCase(CSSCODE)==0) return "prettyprint lang-css";


        return "prettyprint";
    }

}


package wxt4utils;

/**
 *
 * @author Administrator
 */
/**Handling errors from a script-validation
 */
public class validationErrorHandler extends org.xml.sax.helpers.DefaultHandler {
    
    /**counting errors*/
    int m_countErrors;
    /**counting warnings*/
    int m_countWarnings;
    /** where to report*/
    String report="";
    /** write to System.out too*/
    boolean dumpToSystem=true;

    
    /**
     * Creates a new instance of ValidationErrorHandler
     */
    public validationErrorHandler() {
        m_countErrors=0;
        m_countWarnings=0;

    }
    
    /**
     * Set the feature that we want all errors and warnings on System.out
     * @param dump True if we want toget the dump, false otherwise
     */
    public void setSystemDump(boolean dump)
    {
        dumpToSystem=dump;
    }
    
    /** return all errors as a string*/
    public String getReport()
    {
       String head="Validation:\t";
       head+="Errors: " +m_countErrors+"\t";
       head+="Warnings: " +m_countWarnings;
       
       return head+report;
    }
    
    /**report a fatal error
     *@param e The exception that has occurred
     */
    @Override public void fatalError(org.xml.sax.SAXParseException e) {
        if(dumpToSystem) System.out.println("Fatal error: "+e.getMessage());
        report+="\n"+e.getMessage();
        m_countErrors++;

    }
    
    /**report an error
     *@param e The exception that has occurred
     */
    @Override public void error(org.xml.sax.SAXParseException e){
       if(dumpToSystem) System.out.println("Error at line: "+e.getLineNumber()+" : "+e.getMessage());
       report+="\n"+e.getMessage();
       m_countErrors++;
    }

    /**report an warning
     *@param e The exception that has occurred
     */
    @Override public void warning(org.xml.sax.SAXParseException e){
       if(dumpToSystem) System.out.println("Warning: "+e.getMessage());
       report+="\n"+e.getMessage();
       m_countWarnings++;
    }
    
    /**report the start of a document
     */
    @Override public void startDocument() {
      if(dumpToSystem) System.out.println("Start validation");  
    }

    /**report the end of a document
     */
    @Override public void endDocument(){
       if(dumpToSystem) System.out.println("Finish validation"); 
    }

    
}

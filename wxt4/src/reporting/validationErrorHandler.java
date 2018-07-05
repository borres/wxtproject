package reporting;

/**
 * Handling errors from validation
 */
public class validationErrorHandler extends org.xml.sax.helpers.DefaultHandler {

    /**counting errors*/
    int m_countErrors;
    /**counting warnings*/
    int m_countWarnings;
    /** where to report*/
    String report = "";
    /** write to System.out too*/
    boolean dumpToSystem = true;

    /**
     * Creates a new instance of ValidationErrorHandler
     * @param dump true if we want to write to system.out
     */
    public validationErrorHandler(boolean dump) {
        m_countErrors = 0;
        m_countWarnings = 0;
        dumpToSystem = dump;

    }

    /**
     * Set the feature that we want all errors and warnings on System.out
     * @param dump True if we want toget the dump, false otherwise
     */
    public void setSystemDump(boolean dump) {
        dumpToSystem = dump;
    }

    /** 
     * Get the report from validation
     * @return The report
     */
    public String getReport() {
        String head = "Validation:\t";
        head += "Errors: " + m_countErrors + "\t";
        head += "Warnings: " + m_countWarnings;

        return head + report;
    }

    /**report a fatal error
     *@param e The exception that has occurred
     */
    @Override
    public void fatalError(org.xml.sax.SAXParseException e) {
        if (dumpToSystem) {
            System.out.println("Fatal error: " + e.getMessage());
        }
        report += "\n\t" + e.getMessage();
        m_countErrors++;

    }

    /**report an error
     *@param e The exception that has occurred
     */
    @Override
    public void error(org.xml.sax.SAXParseException e) {
        if (dumpToSystem) {
            System.out.println("Error at line: " + e.getLineNumber() + " : " + e.getMessage());
        }
        String tmp = e.getMessage();
        int pos = tmp.indexOf(':');
        if (pos != -1) {
            tmp = tmp.substring(pos + 1).trim();
        }
        report += "\n\t" + tmp;
        m_countErrors++;
    }

    /**report an warning
     *@param e The exception that has occurred
     */
    @Override
    public void warning(org.xml.sax.SAXParseException e) {
        if (dumpToSystem) {
            System.out.println("Warning: " + e.getMessage());
        }
        String tmp = e.getMessage();
        int pos = tmp.indexOf(':');
        if (pos != -1) {
            tmp = tmp.substring(pos + 1).trim();
        }
        report += "\n\t" + tmp;
        m_countWarnings++;
    }

    /**report the start of a document
     */
    @Override
    public void startDocument() {
        if (dumpToSystem) {
            System.out.println("Start validation");
        }
    }

    /**report the end of a document
     */
    @Override
    public void endDocument() {
        if (dumpToSystem) {
            System.out.println("Finish validation");
        }
    }
}

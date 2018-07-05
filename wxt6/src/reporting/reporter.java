package reporting;

import java.net.URI;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import utils.accessutils;

/**
 * This class handles reporting from the building process.
 * <p>
 * Each script, via the scripthandler, has a reporter-object.
 * Reporter is used as a stack. Messages are pushed and popped
 * This facilitates a strategy that prepares for detailed
 * reporting or removing of details if everything goes ok.
 * <p>
 * Reporter also holds general utilities: getMergedStrings
 * which operates as sentences with 1 or 2 insertion points
 * %1 and %2 respectively.
 * <p>
 * Strings are fetched from resources if necessary.
 * No report strings are administrated outside this class.
 * 
 */
public class reporter extends java.lang.Object {

    /**Holding the stack*/
    List<String> m_Stack = new ArrayList<>(10);
    /** Marks the stack for poplimit */
    int m_popMark = 0;
    /**the Locale for this reporter*/
    //static Locale m_Current_Locale =new Locale(System.getProperty("user.language"));
    static Locale m_Current_Locale = new Locale("en");
    /** the URI for the current logfile*/
    URI m_logUri = null;
    /** the stringbundle that holds all strings */
    static String m_Reportbundle = "reporting/reportstrings";
    /** max length in bytes for logfile*/
    long m_max_logfile_length = 100000;

    /** Creates a new instance of reporter */
    public reporter() {
        m_Stack = new ArrayList<String>(10);
        m_popMark = 0;
        //m_Current_Locale =new Locale(System.getProperty("user.language"));
        m_Current_Locale = new Locale("en");
        m_logUri = null;
        m_max_logfile_length = 100000;
        m_Reportbundle = "reporting/reportstrings";
    }

    /** Set locale.
     * 
     * @param lang String defining the locale
     */
    public static void setLocale(String lang) {
        m_Current_Locale = new Locale(lang);
    }

    /** Set stringbundle.
     * 
     * @param bundle String defining the locale
     */
    public static void setBundle(String bundle) {
        m_Reportbundle = bundle;
    }

    /**
     * Get the logfilepath.
     * 
     * @return the URI of the logfile
     */
    public URI getLogFileUri() {
        return m_logUri;
    }

    /**
     * Set the logfilepath.
     * 
     * @param theUri the new URI for the logfile
     */
    public void setLogFileURI(URI theUri) {
        m_logUri = theUri;
    }

    /**
     * Get the max length of the logfile.
     * 
     * @return max length of the logfile
     */
    public long getLogFileMaxlength() {
        return m_max_logfile_length;
    }

    /**
     * Set the max length of the logfile.
     * 
     * @param m the new max length of the logfile
     */
    public void setLogFileMaxlength(long m) {
        m_max_logfile_length = m;
    }

    /** 
     * Sets the popmark. 
     */
    public void markStack() {
        m_popMark = m_Stack.size();
    }

    /** 
     * Get stacksize.
     * 
     * @return the size of the stack
     */
    public int getStackSize() {
        return m_Stack.size();
    }

    /**
     * Clear all message and kill stack
     */
    public void clearMessages() {
        m_Stack.clear();
        m_popMark = 0;
    }

    /**
     * Push a simple, non constructed, non looked up, message to the stack.
     * 
     * @param m The message
     */
    public void pushSimpleMessage(String m) {
        try {
            //String lastMsg=m_Stack.lastElement();
            String lastMsg = m_Stack.get(m_Stack.size() - 1);
            if (lastMsg.compareTo(m) != 0) {
                m_Stack.add(m);
            }
        } catch (Exception e) {
            m_Stack.add(m);
        }
    }

    /** 
     * Push a message from resourcebundle to the stack.
     * 
     * @param m The string used as key in the resourcebundle. If m is not a key, m is pushed
     */
    public void pushMessage(String m) {
        String s = null;
        try {
            s = java.util.ResourceBundle.getBundle(m_Reportbundle, m_Current_Locale).getString(m);
            pushSimpleMessage(s);
        } catch (Exception e) {
            pushSimpleMessage(m);
        }
    }

    /** 
     * Push message from resourcebundle and compose with 1 insertion.
     * 
     * @param m The string used as key. If m is not a key, m is pushed
     * @param t1 The string used as %1 parameter
     */
    public void pushMessage(String m, String t1) {
        String s = null;
        try {
            s = java.util.ResourceBundle.getBundle(m_Reportbundle, m_Current_Locale).getString(m);
            pushSimpleMessage(getMergedString(s, t1));
        } catch (Exception e) {
            pushSimpleMessage(m);
        }
    }

    /** 
     * Push message from resourcebundle and compose with 2 insertions.
     * 
     * @param m The string used as key. If m is not a key, m is pushed
     * @param t1 The string used as %1 parameter
     * @param t2 The second string, used as %2 parameter
     */
    public void pushMessage(String m, String t1, String t2) {
        String s = null;
        try {
            s = java.util.ResourceBundle.getBundle(m_Reportbundle, m_Current_Locale).getString(m);
            pushSimpleMessage(getMergedString(s, t1, t2));
        } catch (Exception e) {
            pushSimpleMessage(m);
        }
    }

    /** 
     * Removes the top of the stack.
     * 
     * @return the top of the stack
     */
    public String popMessage() {
        if (m_Stack.isEmpty()) {
            System.out.println("reporter.popMessage: underflow");
        }
        if (m_popMark > m_Stack.size()) {
            return null;
        }
        //String ret=m_Stack.lastElement();
        //m_Stack.removeElementAt(m_Stack.size()-1);
        String ret = m_Stack.get(m_Stack.size() - 1);
        m_Stack.remove(m_Stack.size() - 1);
        return ret;
    }

    /**
     * Clear the currently selected logfile
     */
    public void clearLogFile() {
        if (m_logUri != null) {
            accessutils.appendToTextFile(m_logUri, ":", 1L);
        }
    }

    /**
     * Append a new report to the currently selected logfile.
     * 
     * @param text  The report to append
     * @param scriptpath  The path to the active script generating the report
     */
    public void writeLogFile(String text, String scriptpath) {
        String toLogfile = text;
        Date dat = new Date(System.currentTimeMillis());
        DateFormat datform = DateFormat.getDateInstance(DateFormat.FULL, m_Current_Locale);
        DateFormat timeform = DateFormat.getTimeInstance(DateFormat.LONG, m_Current_Locale);
        toLogfile = System.getProperty("line.separator")
                + "===================================="
                + System.getProperty("line.separator")
                + datform.format(dat)
                + " - "
                + timeform.format(dat)
                + " / "
                + scriptpath
                + System.getProperty("line.separator")
                + toLogfile;
        if (m_logUri != null) {
            if (text.indexOf("WXT version:") == -1) {
                // we report parsing and we do not kill logfile
                accessutils.appendToTextFile(m_logUri, toLogfile, -1);
            } else {
                accessutils.appendToTextFile(m_logUri, toLogfile, m_max_logfile_length);
            }
        }
    }

    /**
     * Retrieves the total report as the stack is at the moment.
     * <p>
     * Adds this report to the actual file-log, if any, as a side-effect.
     * 
     * @param scriptpath The path to the acive script, or any string to identify the script
     * @return A String with all messages as lines
     */
    public String getReport(String scriptpath) {
        String ret = "";
        String lastMessage = "";
        for (int ix = 0; ix < m_Stack.size(); ix++) {
            String thisMessage = m_Stack.get(ix);
            if ((thisMessage != null) && (lastMessage != null) && (!thisMessage.equals(lastMessage))) {
                ret += thisMessage + System.getProperty("line.separator");
            }
            lastMessage = thisMessage;
        }
        // append to reportlog here
        if (m_logUri != null) {
            writeLogFile(ret, scriptpath);
        }

        // will be used in the GUI or commandwindow
        return ret;
    }

    /** 
     * Utility that compose a string from a base and one insertion at %1.
     * 
     * @param S The string that is supposed to contain %1
     * @param t1 The string that will replace %1
     * @return The composed string or the input string if no entrypoint
     */
    private static String getMergedString(String S, String t1) {
        StringBuilder sb = new StringBuilder(S);
        if ((t1 == null) || (t1.isEmpty())) {
            t1 = " ";
        }
        try {
            int pos = sb.indexOf("%1");
            if (pos != -1) {
                sb.replace(pos, pos + 2, t1);
            }
            int pos2 = sb.indexOf("%2");
            if (pos2 != -1) {
                sb.replace(pos2, pos2 + 2, " ");
            }

            return sb.toString();
        } catch (Exception ex) {
            return sb.toString();
        }
    }

    /** 
     * Utility that compose a string from a base and  insertions at %1 and %2.
     * 
     * @param S The string that is supposed to contain %1
     * @param t1 The string that will replace %1
     * @param t2 The string that will replace %2
     * @return The composed string or the input string if no entrypoints
     */
    private static String getMergedString(String S, String t1, String t2) {
        StringBuilder sb = new StringBuilder(S);
        if ((t2 == null) || (t2.isEmpty())) {
            t2 = " ";
        }
        try {
            int pos = sb.indexOf("%2");
            if (pos != -1) {
                sb.replace(pos, pos + 2, t2);
            }
            return getMergedString(sb.toString(), t1);
        } catch (Exception ex) {
            return sb.toString();
        }
    }

    /**
     * Gets a string from a resource bundle.
     * 
     * @param S The key
     * @return The string found with S, or S if nothing found 
     */
    public static String getBundleString(String S) {
        String retS = "";
        try {
            retS = java.util.ResourceBundle.getBundle(m_Reportbundle, m_Current_Locale).getString(S);
        } catch (Exception e) {
            return S;
        }
        return retS;
    }

    /**
     * Gets a string from a resource bundle combined with a parameter at %1.
     * 
     * @param S The key
     * @param t1 The string that will be inserted for %1 in the string found
     * @return The modified string found with S, or S if nothing found 
     */
    public static String getBundleString(String S, String t1) {
        String retS = "";
        try {
            retS = java.util.ResourceBundle.getBundle(m_Reportbundle, m_Current_Locale).getString(S);
        } catch (Exception e) {
            return S + t1;
        }
        return getMergedString(retS, t1);
    }

    /**
     * Gets a string from a resource bundle combined with a parameter at %1 and one at %2.
     * 
     * @param S The key
     * @param t1 The string that will be inserted for %1 in the string found
     * @param t2 The string that will be inserted for %2 in the string found
     * @return The modified string found with S, or S if nothing found 
     */
    public static String getBundleString(String S, String t1, String t2) {
        String retS = "";
        try {
            retS = java.util.ResourceBundle.getBundle(m_Reportbundle, m_Current_Locale).getString(S);
        } catch (Exception e) {
            return S + t1 + t2;
        }
        return getMergedString(retS, t1, t2);
    }
}

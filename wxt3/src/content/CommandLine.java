package content;

import utils.commandExecutor;
import utils.reporter;


/**
 * Storing and executing a potential commandline as read
 * from the scripts definitions element as element: command.
 */
public class CommandLine {
    /** the name of the command */
    private String m_id;
    /** the commandline we want to execute */
    private String m_line=null;
    /** flagging if we want the command to finnish before wxt continues.*/
    private boolean m_wait=true;
    
    /**
     * Creates a new instance of commandDescription.
     *
     * @param id The name of the command
     * @param line The string describing the command
     * @param waitToFinish true if we want to wait for the command to finnish
     */
    public CommandLine(String id, String line,boolean waitToFinish) {
        m_id = id;
        m_line=line;
        m_wait=waitToFinish;
    }
    
    /**
     * Execute the command.
     * 
     * @param reportMaker The reporter we will use.
     */
     public void executeCommand(reporter reportMaker)
     {
         commandExecutor exec=new commandExecutor(m_line,m_wait,reportMaker);
         exec.doit();
     }
    
    /**
     * Get the commandline.
     * 
     * @return the commandline
     */
    public String getCommandLine(){return m_line;}
    
    /**
     * Get the wait-condition.
     * 
     * @return true if we want to wait
     */
    public boolean getWaitToFinish(){return m_wait;}
    
    /**
     * Set the commandline.
     * 
     * @param line the string describing the command
     */
    public void setCommandLine(String line){m_line=line;}

    /**
     * Set the wait-condition
     * 
     * @param waitToFinish true if we want to wait
     */
    public void setWaitToFinish(boolean waitToFinish){m_wait=waitToFinish;}
    
    
    @Override
    public String toString() {
        return "id:" +m_id + ", line: " + m_line + ", waitToFinish:" + m_wait;
    }
    
}

/*
 */
package wxt2utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

/**
 * 
 * @author bs
 */
public class settings{
    /** for picking wxtpath*/
    public final static String WXTPATH="WXTPATH";
    /** for picking last used scriptpath*/
    public final static String SCRIPTPATH="SCRIPTPATH";
    // for picking look and feel"
    public static String LOOK_AND_FEEL="LOOK_AND_FEEL";

     /** the file to store settings in */
    String m_filename;
    /** storing any setting in general, not recent scriptsfiles*/
    HashMap<String,String> m_values=new HashMap<String,String>();
    /** storing scriptcfiles */
    Vector<String>m_scripts=new Vector<String>();
    
    /**
     * Constructing a setting which resides at user.home
     */
    public settings()
    {
        //m_filename="wxt2runsettings.txt";
        m_filename=System.getProperty("user.home")+File.separatorChar+"wxt2runsettings.txt";
    }
     
    /**
     * Constructing a setting which resides at a given path
     * @param fn Path to the file
     */
     public settings(String fn)
    {
        m_filename=fn;

    }   
    
    /**
     * Get a setting
     * @param key The key to look for
     * @return The found value if exists, else null
     */
    public String getEntry(String key)
    {
        return m_values.get(key);
    }
    
    /**
     * Set a setting
     * @param key The key
     * @param value The value
     */
    public void setEntry(String key,String value)
    {
        if(value==null)
            m_values.remove(key);
        else
            m_values.put(key, value);
    }
     
    /**
     * Set a scriptpath
     * @param s The path
     */
    public void setScript(String s)
    {
        m_scripts.remove(s);
        m_scripts.add(s);
    }
    
    /**
     * remove a scriptpath
     * @param s The path
     */
    public void removeScript(String s)
    {
        m_scripts.remove(s);
    }

    public void removeAllScripts()
    {
        m_scripts.removeAllElements();
    }
    
    /**
     * Get all scripth paths
     * @return A list of all paths
     */public Vector<String> getScripts()
    {
        return m_scripts;
    }
  
    
    /**
     * Save settings
     * @return true if this is ok, otherwise false
     */
     public boolean save()
     {
        String T="";
        Set<String> keys=m_values.keySet();
        for(String k : keys)
            T+=k+"\t"+m_values.get(k)+"\n";
        for(int ix=0;ix<m_scripts.size();ix++)
            T+="script"+ix+"\t"+m_scripts.get(ix)+"\n";
        
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(m_filename));
            out.write(T);
            out.close();
            return true;
        } 
        catch (Exception e) {
            return false;
        }
     }
    
    /**
     * Load settings
     * @return true if that is ok, otherwise false
     */
    public boolean load()
    {
        try {
            BufferedReader in = new BufferedReader(new FileReader(m_filename));
            String tmp;
            while ((tmp = in.readLine()) != null) {
              String[] parts=tmp.split("\t");
              if(parts.length==2)
              {
                  String id=parts[0];
                  if (id.startsWith("script"))
                      m_scripts.add(parts[1]);
                  else
                      m_values.put(parts[0],parts[1]);
              }
            }
            in.close();
            return true;
        } 
        catch (Exception e) {
            return false;
        }

    }

}

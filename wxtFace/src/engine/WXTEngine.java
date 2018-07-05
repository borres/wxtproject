package engine;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Connection to a WXT engine, a WXT jar file
 * @author Administrator
 */
public class WXTEngine {
    /** the absolute file path to wxt*/
    String m_wxtpath;
    
    /** the established scripthandler object*/
    Object m_currentScripthandlerObject;
    /** The wxt.Scripthandler class*/
    Class m_Scripthandler=null;
    /** The constructor for wxt.Scripthandler*/
    Constructor m_Constructor=null;
    /** The method for wxt.Scripthandler.BuildModules()*/
    Method m_BuildModules=null;
    /** The method for updating modules with changed content*/
    Method m_UpdateModules=null;
    /** The method for wxt.Scripthandler.GetReport()*/
    Method m_GetReport=null;
    /** The method for wxt.Scripthandler.getModuleDescription()*/
    Method m_getModuleDescription=null;
    /** The method for wxt.Scripthandler.getModuleLocations()*/
    Method m_getModuleLocations=null;
    /** The method for wxt.Scripthandler.m_getModuleAddress()*/
    Method m_getModuleAddress=null;
    /** The method for accessing the current wxt version */
    Method m_getCurWxtVersion=null;
    /** the method for accessing the latest distributed version */
    Method m_getLatestWxtVersion=null;
    /** the method for accessing a html description of options */
    Method m_getOptionDescription=null;
    
    /**
     * Constructor
     * @param wxtpath The absolute filepath to wxt?.jar
     * @throws exception if something goes wrong
     */
    public WXTEngine(String wxtpath) 
            throws Exception{
        if(wxtpath==null || wxtpath.isEmpty()) {
                      throw new Exception("empty wxt path");
        }
        m_wxtpath=wxtpath;
        URL urls[]={};
        JarFileLoader jfl=new JarFileLoader(urls);
        String oadr=wxtpath;
        jfl.addFile(oadr);
        // find main class as jar-file
        int index1=wxtpath.lastIndexOf(System.getProperty("file.separator"));
        String wxtclass=wxtpath.substring(index1+1).replace(".jar","");
        if(wxtclass.equals("wxt4")) {
             m_Scripthandler=jfl.loadClass(wxtclass+".Scripthandler");//wxt4.Scripthandler
        }
         else {
            m_Scripthandler=jfl.loadClass("wxt.Scripthandler");
         }


        m_Constructor=m_Scripthandler.getDeclaredConstructor(HashMap.class);
        m_Constructor.setAccessible(true);       
        m_BuildModules=m_Scripthandler.getMethod("buildModules", HashMap.class);
        m_UpdateModules=m_Scripthandler.getMethod("updateModules", HashMap.class);
        m_GetReport=m_Scripthandler.getMethod("getReport", (Class[])null);
        m_getModuleDescription=m_Scripthandler.getMethod("getModuleDescription", String.class);
        m_getModuleLocations=m_Scripthandler.getMethod("getModuleLocations", (Class[])null);
        m_getModuleAddress=m_Scripthandler.getMethod("getModuleAddress", String.class);
        m_getCurWxtVersion=m_Scripthandler.getMethod("getVersion", (Class[])null);
        m_getLatestWxtVersion=m_Scripthandler.getMethod("getLatestVersion", (Class[])null);
        m_getOptionDescription=m_Scripthandler.getMethod("getOptionDescription", (Class[])null);

    }
    
    /**
     * Set up constructor
     * Done when a script is selected in the scriptlist
     * @param script The scipt that will be parameter
     * @throws  an excetion if we fail 
     */
    public void constructScript(String script)
            throws Exception{
        try{      
            HashMap<String,String> params=new HashMap<>();
            String tmp=script;
            tmp=tmp.replace('\\', '/');
            params.put("script", tmp);       
            Object[] pars=new Object[]{params};
            //System.out.println("constructor :"+m_Constructor.toString());
            m_currentScripthandlerObject=m_Constructor.newInstance(pars);
        }
        catch(InstantiationException ex){
            throw new Exception("WXT Could not establish Script because:(1) "+ex.getCause());
        }
        catch(IllegalAccessException ex){
            throw new Exception("WXT Could not establish Script because:(2) "+ex.getCause());
        }
        catch(IllegalArgumentException ex){
            throw new Exception("WXT Could not establish Script because:(3) "+ex.getCause());
        }
        catch(InvocationTargetException ex){
            throw new Exception("WXT Could not establish Script because:(4) "+ex.getCause());
        }
        catch(Exception ex){
            throw new Exception("WXT Could not establish Script because:(5) "+ex.getCause());
        }
        
    }
    

    /**
     * 
     * @return The path to the WXT engine
     */
    public String getPath(){
        return m_wxtpath;
    }
    
    /**
     * Building modules with the establishe scriptoject
     * @param params Is "script"(scriptpath), 
     *                  "modules"(list of modules), 
     *                  and all options as separat entries 
     */
    public void buildModules(HashMap<String,String> params) 
            throws Exception{
       Object[]pars={params}; 
       m_BuildModules.invoke(m_currentScripthandlerObject,pars); 
    }
    
     /**
     * Update modules with the establishe scriptoject
     * WXT has taken notes of changed module components
     * @param params Is "script"(scriptpath), 
     *                  and all options as separat entries 
     */
    public void updatedModules(HashMap<String,String> params) 
            throws Exception{
       Object[]pars={params}; 
       m_UpdateModules.invoke(m_currentScripthandlerObject,pars); 
    }   
    
    /**
     * Get a report from WXT
     */
    public String getReport()
            throws Exception{
        return (String)m_GetReport.invoke(m_currentScripthandlerObject,(Object[]) null);
    }

     /**
     * Get a description of a module
     * @param ID The id of the module
     */
    public String getModuleDescription(String ID)
            throws Exception{       
        return (String)m_getModuleDescription.invoke(m_currentScripthandlerObject,ID);
    }
    
    /**
     * Get a modules location, a list of modules
     * @param ID The id of the module
     */
    public String getModuleAddress(String ID)
            throws Exception{
        return (String)m_getModuleAddress.invoke(m_currentScripthandlerObject,ID);
    }
   
    /**
     * Get modules location, a list of modules
     */
    public String getModuleLocations()
            throws Exception{
        return (String)m_getModuleLocations.invoke(m_currentScripthandlerObject, (Object[]) null);
    }
    
    /**
     * Get used wxt version
     */
    public String getWXTVersion(){
        try{
            return (String)m_getCurWxtVersion.invoke(m_currentScripthandlerObject, (Object[]) null);
        }
        catch(Exception ex)
        {
            return "not available";
        }
    }    
    
     /**
     * Get latest distributed wxt-version
     */
    public String getLatestWXTVersion(){
        try{
            return (String)m_getLatestWxtVersion.invoke(m_currentScripthandlerObject, (Object[]) null);
        }
        catch (Exception ex)
        {
            return "not available";
        }
    }
    
    public String getOptionDescription(){
         try{
            return (String)m_getOptionDescription.invoke(m_currentScripthandlerObject, (Object[]) null);
        }
        catch (Exception ex)
        {
            return null;
        }
       
    }
    
    
    
    //------------------------------------------------ 
    /**
     * Use a dialog to set up a new wxtengine
     * @param  parent The owning frame
     * @return A new WXTEngine object or null
     */
    static public WXTEngine locateWXT(JFrame parent){
        String wxtpath;
        WXTEngine wxtEngine;
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("WXT5 - path");
        fc.setAcceptAllFileFilterUsed(true);
        FileNameExtensionFilter jarFilter=new FileNameExtensionFilter("jarfiles","jar");
        fc.setFileFilter(jarFilter);

        int result=fc.showOpenDialog(parent);
        if (result==JFileChooser.APPROVE_OPTION)
        {
           wxtpath=fc.getSelectedFile().toString();        
           // set up wxt contact
           try{
               wxtEngine=new WXTEngine(wxtpath);
               return wxtEngine;
           }
           catch(Exception ex){
               return null;
           }
        }
        else {
            return null;
        }       
    }
 }

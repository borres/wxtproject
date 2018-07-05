package helppackage;

import engine.WXTEngine;
import java.net.URI;
import javax.swing.JFrame;

/**
 *
 * @author Administrator
 */
public class Helper {

    private URI m_WXTHome;
    private URI m_WXTHomeDOC;
    private URI m_GUIHomeDoc;
    
    WXTEngine m_engine=null;
    
    // main frame
    private JFrame m_GUI;
    ResourceHandler m_resourceHandler;


    
    public Helper(JFrame GUI,WXTEngine engine){
        m_GUI=GUI;
        m_engine=engine;
        m_resourceHandler=new ResourceHandler();
              

        m_WXTHomeDOC=null;
        m_GUIHomeDoc=null;
        m_WXTHome=null;
        try{
            m_WXTHomeDOC=new URI(m_resourceHandler.getEngineDocHome());
            m_GUIHomeDoc=new URI(m_resourceHandler.getFaceDocHome());
            m_WXTHome=new URI(m_resourceHandler.getWXTProjectHome());
        }
        catch(Exception ex){
            System.out.println("Setting up web adddresses in Helper :\n"+ex.getMessage());
        }
    }
    
    public void doAbout(){
        AboutDialog D=new AboutDialog(m_GUI,true,this);
        D.setVisible(true);
    }
    public void doVersions(){
       VersionDialog V=new VersionDialog(m_GUI,true,this);
       V.setVisible(true);
    }
    
    public void doWXTHelp(){
        helppackage.DesktopHandler.launchUri(m_WXTHomeDOC);
    }
    
    public void doGUIHelp(){
        helppackage.DesktopHandler.launchUri(m_GUIHomeDoc);
        
    }
    
    public void doWXTHome(){
        helppackage.DesktopHandler.launchUri(m_WXTHome);
    }
    

    public String getFaceVersion() {
        return m_resourceHandler.getVersion();
    }

    public String getLatestFaceVersion() {
        return m_resourceHandler.getLatestDistVersion();
    }

    public String getWXTVersion() {
        return m_engine.getWXTVersion();
    }    

    public String getLatestWXTVersion() {
        return m_engine.getLatestWXTVersion();
    }

}

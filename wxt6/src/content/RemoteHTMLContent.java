package content;

import java.net.URI;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.PIcommand;
import utils.accessutils;
import wxt.Options;
import xmldom.JSoupEngine;
import xmldom.domer;

/**
 * Handling HTML-source with a backup catalog
 * Allways store backup of source and image.
 * Only selection key is cssselector
 * @author Administrator
 */
public class RemoteHTMLContent extends HTMLContent{
    //catalog
    static final String BACK = "backup";
    URI m_backupCatalog=null;
    
     /**
     * Constructs content from a PIcommand
     * @param owner The module that request the content
     * @param cmd The PIcommand describing the request
     * @throws java.lang.Exception When bad adressing in super
     */   
     public RemoteHTMLContent(Module owner, PIcommand cmd)
            throws Exception{
        // constructor in parent, HTMLContent, will supress immediate load of dom
        super(owner,cmd);
        if(m_encoding==null) {
            m_encoding=DEFAULT_ENCODNG;
        }       
    }
    
    /**
     * Redefined because we have moved it to a backup catalog
     */
    @Override
    public URI getActualSourceLocation(){
        try{
            return new URI(m_backupCatalog.toString() + "/content.html");
        }
        catch(Exception ex){
            return null;
        }
    }
    
    /**
     * Get content according to parameters in PIcommand.
     * This method is used from class producer
     * @param mod The module that has requested this content
     * @param cmd The PIcommand that describes what we want
     * @return  A DocumentFragment
     */
    @Override
    public DocumentFragment getContent(Module mod, PIcommand cmd) {
        m_backupCatalog=makeBackupCatalog(mod, cmd);
        
        // should be checked in PI-control
        if(!cmd.paramExist(PIcommand.CSSSELECTOR)){
            mod.getReporter().pushMessage("missing_selector_in_command",cmd.toString());
            return Content.getFailedContent(mod, cmd);
        }
        
        
        // default
        boolean useBackup=true;
        
        // according to cmd
        if(cmd.paramExist(PIcommand.USECOPY)){
            useBackup = cmd.getValue(PIcommand.USECOPY).equals(PIcommand.YES);            
        }
        // Modified by global option ?
        String globalCommand = mod.getScriptHandler().getOption(Options.USE_COPY);
        switch (globalCommand) {
            case Options.YES:
                useBackup = true;
                break;
            case Options.NO:
                useBackup = false;
                break;
        }
        
        // should we keep links
        boolean keepLinks=true;
        if(cmd.paramExist(PIcommand.KEEPLINKS)){
            keepLinks=cmd.getValue(PIcommand.KEEPLINKS).equals(PIcommand.YES);
        }
                      
        boolean keepStyle=true;
        if(cmd.paramExist(PIcommand.KEEPSTYLE)) {
            keepStyle=cmd.getValue(PIcommand.KEEPSTYLE).equals(PIcommand.YES);
        }
        
        // here we must work with  m_SoupDoc
        m_SoupDoc= null;

        if (useBackup) {
            // go for the backup directly
            m_SoupDoc = JSoupEngine.retrieveBackup(mod, cmd, m_backupCatalog);
        }

        // Either we dont want the backup or it is not found/loaded
        if (m_SoupDoc == null) {
            try{
                m_theSource=accessutils.getTextFile(m_absoluteLoadUri, m_encoding);
                m_SoupDoc = JSoupEngine.makeSoupDoc(m_theSource,m_encoding);
                if (m_SoupDoc == null) {
                    return Content.getFailedContent(mod, cmd);
                }
            }
            catch(Exception ex){
                m_SoupDoc=null;
            }

            //save the complete doc as backup, unconditionally
            JSoupEngine.makeBackup(mod, cmd, m_SoupDoc,m_backupCatalog);
            // then we try again
            m_SoupDoc = JSoupEngine.retrieveBackup(mod, cmd, m_backupCatalog);
        }

        // went wrong
        if (m_SoupDoc == null) {
            return Content.getFailedContent(mod, cmd);
        }

        //-----------------------------
        // we have a complete raw document without extraction
        // clean it. This is partly wikirelated
        // we must not do cleaning that destroys the cssselctors work
        //JSoupEngine.cleanDoc(m_SoupDoc, keepLinks);
        
        // prepare a wrapper
        Element tmpWrapper = mod.getDoc().createElement("div");
        tmpWrapper.setAttribute("class", cmd.getCommandStyleName());
       
        // we have checked that we have a  cssselector
        String cssselector=cmd.getValue(PIcommand.CSSSELECTOR);
        // do selection according to cssselector
        try{
            // this selection does also clean JSoup things
            String res=JSoupEngine.findElementsWithSelector(m_SoupDoc, cssselector,keepLinks, m_encoding);
            DocumentFragment resdf=domer.produceDocFragmentFromString(res, m_encoding, mod.getReporter());
            NodeList nlist=resdf.getChildNodes();
            for (int ix = 0; ix < nlist.getLength(); ix++) {
                Node n=mod.getDoc().importNode(nlist.item(ix), true);
                tmpWrapper.appendChild(n.cloneNode(true));
            }
        }
        catch(Exception ex){
           return Content.getFailedContent(mod, cmd);
        }
               
        cleanOneElement(mod, cmd,tmpWrapper,keepLinks,keepStyle);

        // use what we have found
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        df.appendChild(mod.getDoc().importNode(tmpWrapper, true));
        return df;
    }


    /**
     * Make a backupcatalog for the extracted material and all images
     * @param mod The module who wants this material
     * @param cmd The command that describes the request
     * @return The catalog as an URI if successfull, otherwise null
     */
    private static URI makeBackupCatalog(Module mod, PIcommand cmd) {
        // determine backup catalog
        String bckparam = BACK;
        if (cmd.paramExist(PIcommand.BACKUP)) {
            bckparam = cmd.getValue(PIcommand.BACKUP);
        }

        URI absModUri = mod.getAbsoluteUri();
        URI absBackupCat = null;
        try {
            absBackupCat = accessutils.makeAbsoluteURI(bckparam, absModUri.toString());
            // add the subcatalog for this extraction
            String tmp1 = accessutils.cleanStringForUseAsFilePath(cmd.getValue(PIcommand.LOCATION));
            // reduce this to avoid unneccesary long paths
            tmp1 = tmp1.replace("wiki.xml", "");

            // when using main temp catalog
            tmp1 = tmp1.replace(accessutils.cleanStringForUseAsFilePath(mod.getDefinitions().getTempCatalog()), "");

            while (tmp1.endsWith("_")) {
                tmp1 = tmp1.substring(0, tmp1.length() - 1);
            }
            while (tmp1.startsWith("_")) {
                tmp1 = tmp1.substring(1);
            }
            tmp1 = tmp1.replace('.', '_');
            
            absBackupCat = accessutils.makeUri(absBackupCat.toString() + "/" + tmp1);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            mod.getReporter().pushMessage("could_not_make_backup");
        }
        return absBackupCat;
    }
}

package content;

import java.net.URI;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.PIcommand;
import utils.accessutils;
import xmldom.JSoupEngine;
import xmldom.domer;

/**
 * Handles HTML
 * either by normal parsing or jsoup parsing to achive the possibility
 * to use cssselector
 * Can use xpath or cssselector
 * cssselector is dominant, xpath is alternative default and defaults to //body
 * @author borres
 */
public class HTMLContent extends Content {
    /** the dom */
    Document m_theDoc = null;
    /** alternative is jsoup document*/
    org.jsoup.nodes.Document m_SoupDoc;
    /** the source **/
    String m_theSource;
    

    /**
     * Constructor
     * @param owner The module owning this content
     * @param cmd The command describing the request
     * @throws Exception if we fail
     */
    public HTMLContent(Module owner, PIcommand cmd)
            throws Exception {
        super(owner, cmd);
        // neither
        m_theDoc=null;
        m_SoupDoc=null;
        if(!(this instanceof RemoteHTMLContent)){
            m_theSource=accessutils.getTextFile(m_absoluteUri, m_encoding);
            // and we attempt to establish the DOM and the source string
            // address and encoding is set in super.super
            try {
                m_theSource=accessutils.getTextFile(m_absoluteUri, m_encoding);
                // if we want to do CSSselect we goo for a a jsoup dom
                // otherwise we go for a "normal" dom
                if(cmd.paramExist(PIcommand.XPATH)){
                    m_theDoc = domer.makeDomFromString(m_theSource,false, m_encoding);
                }
                else{
                    m_SoupDoc=JSoupEngine.makeSoupDoc(m_theSource,m_encoding);
                }
            } 
            catch (Exception ex) {
                throw ex;
            }
        }

   }
    
     /**
     * Get the content with actual xpath, encoding and transformation
     * If no transformatoin, we reuse DOM
     * @param mod The module
     * @param cmd The command
     * @return A documentfragment
     */
    @Override
    public DocumentFragment getContent(Module mod, PIcommand cmd){
        DocumentFragment df=mod.getDoc().createDocumentFragment();
        // we should be able to work with xpath or ccsselect here
        // depending on what is set in the constructor
        if(cmd.paramExist(PIcommand.ENCODING)) {
            m_encoding=cmd.getValue(PIcommand.ENCODING);
        }
        if(m_encoding==null) {
            m_encoding=DEFAULT_ENCODNG;
        }
        
        boolean keepLinks=true;
        if(cmd.paramExist(PIcommand.KEEPLINKS)) {
            keepLinks=cmd.getValue(PIcommand.KEEPLINKS).equals(PIcommand.YES);
        }
 
        boolean keepStyle=true;
        if(cmd.paramExist(PIcommand.KEEPSTYLE)) {
            keepStyle=cmd.getValue(PIcommand.KEEPSTYLE).equals(PIcommand.YES);
        }

        if(m_theDoc!=null){
            // DOM and xpath case
            // we have a xpath or no selector at all
            String xpath;
            if (cmd.paramExist(PIcommand.XPATH)) {
                xpath=cmd.getValue(PIcommand.XPATH);
            }
            else {
                xpath="//p";
            }
            // we do the xpath
            try {
                NodeList nlist = domer.performXPathQuery(m_theDoc, xpath);
                for (int ix = 0; ix < nlist.getLength(); ix++) {
                    df.appendChild(mod.getDoc().importNode(nlist.item(ix), true));
                }
                
                cleanFragment(mod,cmd,df,keepLinks,keepStyle);
                return df;
                
            } catch (Exception xpex) {
                mod.getReporter().pushMessage("could_not_do_xpath", m_absoluteLoadUri.toString(), xpex.getMessage());
                return Content.getFailedContent(mod, cmd);
            }            
        }
        else if(m_SoupDoc!=null){
            // JSoupDom and css case
            // this is the tricky part. 
            // produce text
            String cssselect;
            if(cmd.paramExist(PIcommand.CSSSELECTOR)) {
                cssselect=cmd.getValue(PIcommand.CSSSELECTOR);
            }
            else {
                cssselect="body p";
            }
            //keep references here
            String res=JSoupEngine.findElementsWithSelector(m_SoupDoc,cssselect,true,m_encoding);
            // display the text directly
            /*df.appendChild(mod.getDoc().createTextNode(res));
            return df;*/
            // or reparse it as a fragment
            try{
                DocumentFragment resdf=domer.produceDocFragmentFromString(res, m_encoding, mod.getReporter());
                NodeList nlist=resdf.getChildNodes();
                for (int ix = 0; ix < nlist.getLength(); ix++) {
                    Node n=mod.getDoc().importNode(nlist.item(ix), true);
                    df.appendChild(n.cloneNode(true));
                }
                mod.getDoc().importNode(df, true);              
                
                cleanFragment(mod,cmd,df,keepLinks,keepStyle);
               
                return df;
            }
            catch(Exception ex){
                System.out.println(ex.getMessage());
            }
            
        }
        else{
            // no dom, neither normal or jsoup is set
            System.out.println("NO DOM, this should not happen in HTMLContent");
        }
        df.appendChild(mod.getDoc().createTextNode("<div>NO HTMLContent DOM</div>"));

        return df;
    }
    
     /**
     * Final cleaning before we export it, recursive
     * @param mod The module that has requested this content
     * @param cmd The PIcommand that describes what we want
     * @param elt The element to clean
     */      
    @Override
    public  void  cleanOneElement(Module mod, PIcommand cmd,Element elt,boolean keepLinks,boolean keepStyle){
        
        // clean styling
        if(!keepStyle){
            if (elt.hasAttribute("style")) {
                elt.removeAttribute("style");
            }
            // should we do this ?
            if (elt.hasAttribute("class")) {
                elt.removeAttribute("class");
            } 
        }
        
        // clean a href
        if (elt.hasAttribute("href")) {
            boolean done=false;
            if(keepLinks){
                try{
                    URI ref=new URI(elt.getAttribute("href"));
                    if(ref.isAbsolute()) {
                        done=true;
                    }
                    if(!done){
                        // can we make it absolute ?
                        URI loc=new URI(cmd.getValue(PIcommand.LOCATION));
                        URI newhref;
                        newhref=accessutils.makeAbsoluteURI(ref.toString(), loc.toString());
                        if(newhref.isAbsolute()){
                            elt.setAttribute("href", newhref.toString());
                            done=true;
                        }
                    }
                }
                catch(Exception ex){
                    System.out.println(ex.getMessage());
                }
            }
            if(!done){
                // either not keep or  we have failed to keep it
                Node parent = elt.getParentNode();
                Node fchild;
                if (elt.hasChildNodes()) {
                    fchild = elt.getFirstChild();
                } else {
                    fchild = mod.getDoc().createTextNode(" ");
                }

                // remove reference []
                if (fchild.getNodeType() == Node.TEXT_NODE) {
                    String tmp = fchild.getNodeValue().trim();
                    // removing edit stuff from wikipedia
                    if (tmp.startsWith("[") && tmp.endsWith("]")) {
                        fchild = mod.getDoc().createTextNode(" ");
                    }
                } else if (fchild.getNodeType() == Node.ELEMENT_NODE) {
                    if ((parent.getNodeType() == Node.ELEMENT_NODE)
                            && (parent.getNodeName().equalsIgnoreCase("sup"))) {
                        fchild = mod.getDoc().createTextNode(" ");
                    }
                }
                parent.replaceChild(fchild, elt);           
            }
        }
        // do all children
        NodeList linklist = elt.getChildNodes();
        for(int cix=linklist.getLength() - 1;cix>-1;cix--){
            Node n=linklist.item(cix);            
            if(n instanceof Element) {
                cleanOneElement(mod, cmd,(Element) linklist.item(cix),keepLinks,keepStyle);
            }
        }

      
    }
    
     /**
     * Final cleaning before we export it
     * @param mod The module that has requested this content
     * @param cmd The PIcommand that describes what we want
     * @param frag The documentfragment owning the elements to clean
     */   
    public  void  cleanFragment(Module mod, PIcommand cmd,DocumentFragment frag,boolean keepLinks,boolean keepStyle){      
        // remove or modify links
        NodeList linklist = frag.getChildNodes();
        for(int cix=linklist.getLength() - 1;cix>-1;cix--){
            Node n=linklist.item(cix);            
            if(n instanceof Element) {
                cleanOneElement(mod, cmd,(Element) linklist.item(cix),keepLinks,keepStyle);
            }
        }
     }
}

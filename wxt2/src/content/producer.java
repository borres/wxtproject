package content;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URI;
import java.text.DateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;
import javax.swing.tree.TreeNode;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.PIcommand;
import utils.accessutils;
import utils.domer;
import utils.reporter;
import utils.wxtError;

/**
 * Produce content based on PI's
 * All PI-production is done under control of this class
 * 
 */
public class producer {

    /**the Locale for this producer*/
    Locale m_Current_Locale = new Locale(System.getProperty("user.language"));
    /** signalling that we dont want to adjust level on CSS-class in maxeXLink*/
    final int NO_LEVEL_ADJUST = -9999;

    public producer() {
    }

    /**
     * Produce a comment that identifies an omitted PI production.
     * 
     * @param doc The DOM of the Module requesting the PI
     * @param cmd The PI
     * @return A comment element describing what we did not do
     */
    public Comment makeFailureComment(Document doc, PIcommand cmd) {
        Comment cmt = doc.createComment(cmd.toString() + " is not produced");
        return cmt;
    }

     /**
     * Produce a comment stating an empty, not error, comment
     * This will prevent collapse of doc-structure and error on CSS
     *
     * @param doc The Document that will own the Element produced
     * @param C The commandstring
     * @return A documentfragment with a 1 space text childs
     */
    private Comment makeEmptyComment(Document doc,PIcommand cmd)
    {
        Comment cmt=doc.createComment("empty: "+cmd.getCommand());
        return cmt;
    }

     
    /**
     * Produce a visible "deprecated" Element.
     * 
     * @param doc The Document that will own the Element produced
     * @param cmd The command
     * @return A Element that says not implemented
     */
    private Element makeNotLegalAnyMore(Document doc, PIcommand cmd)
    {
        Element elt = doc.createElement("span");
        elt.setAttribute("style", "color:yellow");
        elt.appendChild(doc.createTextNode("not legal: " + cmd.getOriginalData()));
        return elt;
    }


    /** 
     * Set locale.
     * @param lang String defining the locale
     */
    public void setLocale(String lang) {
        m_Current_Locale = new Locale(lang);
    }

    /**
     * Make a link from one module to another.
     * 
     * @param from The Module to link from
     * @param to The Module to link to, the target
     * @param element_type The type of element we want (span, div,?)
     * @param css_className The CSS class we want
     * @param css_base_level if different from NO_LEVEL_ADJUST we use the value to correct CSS-class levels
     * @return A prepared element
     */
    private Element makeXLink(Module from, Module to, String element_type, 
                              String css_className, int css_base_level,
                              boolean bSummary) 
    {
        String link = "";
        if (!from.equals(to))
        {
            try {
                URI relUri = accessutils.makeRelativeURI(from.getAbsoluteUri(),
                                                         to.getAbsoluteUri());
                if (relUri != null) {
                    link = relUri.toString();
                }
            } 
            catch (Exception e)
            {
                from.getReporter().pushMessage("bad_cross_ref", to.getName());
            }
        }
        Element eElt = from.getDoc().createElement(element_type);

        // adjust style for level
        if ((css_className!=null)&&(css_base_level != NO_LEVEL_ADJUST))
            css_className = css_className + (to.getLevel() + 1);

        if (from.equals(to))
        {
            if(css_className!=null)
                eElt.setAttribute("class", css_className);
            eElt.appendChild(from.getDoc().createTextNode(to.getName()));
            return eElt;

        }

        // set style
        if(css_className!=null)
            eElt.setAttribute("class", css_className);
        
        // find a unique id to avoid mixup of multiple ref on one module
        // must start with a letter to avoid validation error ?
        String useID=from.getScriptHandler().getANewId(from.getID());

        Element aElt = from.getDoc().createElement("a");
        if(bSummary)
        {           
            aElt.setAttribute("onmouseover","javascript:document.getElementById('"+useID+"').style.visibility='visible'");
            aElt.setAttribute("onmouseout","javascript:document.getElementById('"+useID+"').style.visibility='hidden'");
            aElt.setAttribute("onclick","javascript:document.getElementById('"+useID+"').style.visibility='hidden'");
        }
        else
            aElt.setAttribute("title", to.getDescription());
        
        aElt.setAttribute("href", link);
        aElt.appendChild(from.getDoc().createTextNode(to.getName()));
        eElt.appendChild(aElt);
        
        if(bSummary)
        {
            // define the popup-like element
            Element div1=from.getDoc().createElement("div");
            div1.setAttribute("style", "position: absolute; z-index: 10");
            Element div2=from.getDoc().createElement("div");
            div2.setAttribute("id", useID);
            div2.setAttribute("class", PIcommand.WXTSTYLEPREFIX+"summary");
            div2.setAttribute("style", "visibility:hidden;");
            
            // produce the fragment with a header            
            Element headElt=from.getDoc().createElement("div");
            headElt.setAttribute("class", "header");
            headElt.appendChild(from.getDoc().createTextNode(to.getName()));            
            div2.appendChild(headElt);
            
            DocumentFragment tmp=to.getSummary().getFragment();
            DocumentFragment theFragmentDF= (DocumentFragment) (from.getDoc().importNode(tmp, true));
            correctAddressing(theFragmentDF,to,from.getAbsoluteUri());
            
            NodeList nl=theFragmentDF.getChildNodes();
            for(int nix=0;nix<nl.getLength();nix++)
                div2.appendChild(nl.item(nix).cloneNode(true));
             
            div1.appendChild(div2);
            eElt.appendChild(div1);
        }
        return eElt;
    }

    /**
     * Produce a toc or other element set in one row or as columns in a table.
     * 
     * @param doc The document that will own the result
     * @param entrylist The list of entries
     * @param columns The wanted number of columns
     * @param divider A string separating entries in a horizontal list (columss=0)
     * @return An Element with the produced content
     */
    private static Element displayElementsInTable(Document doc, Vector<elementWrapper> entrylist, 
                                                  int columns,String divider) 
    {
        Element wrapElt;
        if (columns == 0) 
        {
            // produce a horizontal list, a span
            wrapElt = doc.createElement("span");
            for (elementWrapper tc : entrylist) {
                wrapElt.appendChild(tc.getElement());
                // separator: | or blank or what
                wrapElt.appendChild(doc.createTextNode(divider));
            }
            // remove the last divider
            wrapElt.removeChild(wrapElt.getLastChild());
            return wrapElt;
        }
        
        // single column in a div
        wrapElt = doc.createElement("div");
        if (columns == 1) 
        {
            for (elementWrapper tc : entrylist) {
                Element basicElt=tc.getElement();
                if(basicElt.getNodeName().compareToIgnoreCase("span")==0)
                {
                    Element wrap = doc.createElement("div");
                    wrap.appendChild(basicElt);
                    wrapElt.appendChild(wrap);                   
                }
                else
                    wrapElt.appendChild(basicElt);
            }
            return wrapElt;
        }

        // table , columns > 1
        // setup column breaks
        accessutils.makeColumnBreaks(entrylist, columns);
        
        Element tableElt = doc.createElement("table");
        Element trElt = doc.createElement("tr");
        tableElt.appendChild(trElt);

        Element tdElt = doc.createElement("td");
        tdElt.setAttribute("valign", "top");
        for (elementWrapper tc : entrylist) 
        {
            if (tc.getBreakBefore() && tdElt.hasChildNodes()) 
            {
                trElt.appendChild(tdElt);
                tdElt = doc.createElement("td");
                tdElt.setAttribute("valign", "top");

            }
            Element basicElt=tc.getElement();
            if(basicElt.getNodeName().compareToIgnoreCase("span")==0)
            {
                Element wrapDiv = doc.createElement("div");
                wrapDiv.appendChild(basicElt);
                tdElt.appendChild(wrapDiv);
            }
            else
                tdElt.appendChild(basicElt);

        }
        if (tdElt.hasChildNodes()) {
            trElt.appendChild(tdElt);
        }
        wrapElt.appendChild(tableElt);
        return wrapElt;

    }

    /**
     * Correct all addressing in a document fragment
     * This means:<br>
     * 1 Find all PI's with some kind of address and correct those addresses<br>
     * 2 Find all elements marked for readdressing and corrects the actual attributes
     *   Realize the intention of the addressing element in the script
     * 
     * @param topElement The root of all nodes we will correct
     * @param target The Module that will eventually own this tree
     * @param sourceURI The absolute address of the content that originally owns this tree
     */
    public void correctAddressing(Element topElement, Module target, URI sourceURI) 
    {
        // Assume that addressing in this fragment is done according to sourceURI
        // Should be changed so addressing match target
        // Candidates for changing are:
        // PIs: importxml, importtxt, importdb, xlink, and what more
        // And in general: a href, script src etc. as defined in Definitions.getAddressMap()

        //------------------------
        // do PI's
        // all potential addresses must be adjusted
        Vector<Node> nlist = domer.getPIs(topElement, "_wxt");
        // target uri
        URI targetUri=target.getAbsoluteUri();
        // the params that are candidates for corrections
        Vector<String> params = new Vector<String>();
        params.add(PIcommand.LOCATION);
        params.add(PIcommand.BACKUPLOCATION);
        params.add(PIcommand.TARGETLOCATION);
        params.add(PIcommand.SOURCELOCATION);
        params.add(PIcommand.SQLFILE);
        params.add(PIcommand.URI);
        params.add(PIcommand.LINK);
        //params.add(PIcommand.BACKUP);
        //dont:params.add(PIcommand.TRANSFORMATION);

        // run all commands with potential for readdressing
        for (Node pinode : nlist) {
            try {
                String T = pinode.getNodeValue().trim();
                // check all actual parameters
                for (String param : params) {
                    // do some fast filtering of actual PI's
                    if (T.indexOf(param) != -1) 
                    {
                        PIcommand cmd=null;
                        try{
                            cmd = new PIcommand(T, target.getDefinitions());
                        }
                        catch(Exception cex)
                        {
                            // message here will make double message since imprt will also react
                            //target.getReporter().pushMessage("drop PI: "+T);
                            continue;
                        }
                        String s = cmd.getValue(param);
                        // we dont want to change PI's that has LOCATION=_content since this means
                        // that we should look in modules content children as defined in script
                        // optional value PIcommand.content ("_content") actually means no uri
                        // as does PIcommand.content ("yes") and PIcommand.content ("no")
                        // actual in image link=""
                        if ((s != null) 
                             &&(s.compareToIgnoreCase(PIcommand.CONTENT) != 0)
                             &&(s.compareToIgnoreCase(PIcommand.YES) != 0)
                             &&(s.compareToIgnoreCase(PIcommand.NO) != 0))
                        {
                            // allow pathfragments
                            s = target.getDefinitions().substituteFragments(s);

                            String sourcecat = accessutils.removeFilePart(sourceURI).toString();
                            URI absUri = accessutils.makeAbsoluteURI(s, sourcecat);
                            URI relUri = accessutils.makeRelativeURI(targetUri, absUri);

                            cmd.setParameter(param, relUri.toString());
                            T = cmd.getCommand() + " " + cmd.getParamList();
                            // set back the new value
                            pinode.setNodeValue(T);
                        }
                    }
                }
            } 
            catch (Exception e) 
            {
                target.getReporter().pushMessage("could_not_readdress", e.getMessage());
            }
        }

        //--------------------------
        // do all general addressing as defined in Definitons.getAddressing
        // This means: correct all href-attributes in all a-tags, src-attributes in img, etc
        HashMap<String, String> adr = target.getDefinitions().getAddressMap();
        Set<String> keys = adr.keySet();
        for (Iterator<String> k = keys.iterator(); k.hasNext();) 
        {
            String tag = k.next();
            String att = adr.get(tag);

            NodeList nodlist = topElement.getElementsByTagName(tag);

            for (int ix = 0; ix < nodlist.getLength(); ix++) 
            {
                Element thisElt = (Element) nodlist.item(ix);
                if (!thisElt.hasAttribute(att)) {
                    continue;
                }
                try {
                    String s = thisElt.getAttribute(att).trim();
                    if (s.length() > 0) 
                    {
                        // allow pathfragments even here 
                        s = target.getDefinitions().substituteFragments(s);
                        
                        if(sourceURI!=null)
                        {
                            String sourcecat = accessutils.removeFilePart(sourceURI).toString();
                            URI absUri = accessutils.makeAbsoluteURI(s, sourcecat);
                            URI relUri = accessutils.makeRelativeURI(targetUri, absUri);

                            thisElt.setAttribute(att, relUri.toString());
                        }

                    }
                } 
                catch (Exception e) 
                {
                    String tmp = e.getMessage();
                    target.getReporter().pushMessage("error_in_readdressing", tmp);
                }
            }
        }
    }
    
    /**
     * Correct all addressing in a document fragment
     * This means:<br>
     * 1 Find all PI's with some kind of address and correct those addresses<br>
     * 2 Find all elements marked for readdressing and corrects the actual attributes
     *   Realize the intention of the addressing element in the script
     * 
     * @param fragment The documentfragment containing all nodes we will correct
     * @param target The Module that will eventually own this tree
     * @param sourceURI The absolute address of the content that originally owns this tree
     */
    public void correctAddressing(DocumentFragment fragment, Module target, URI sourceURI) 
    {
        // we will wrap the fragments children in a temporary element
        Element wrapper=fragment.getOwnerDocument().createElement("div");
        NodeList list=fragment.getChildNodes();
        for(int ix=0;ix<list.getLength();ix++)
            wrapper.appendChild(list.item(ix).cloneNode(true));
        
        correctAddressing(wrapper,target,sourceURI);
        
        // take them back ??
        list=wrapper.getChildNodes();
        NodeList children=fragment.getChildNodes();
        for(int ix=children.getLength()-1;ix>-1;ix--)
            fragment.removeChild(children.item(ix));
        for(int ix=0;ix<list.getLength();ix++)
            fragment.appendChild(list.item(ix).cloneNode(true));

    }


    /**
     * Produce a list of Modulerefs according to the selection criteria
     * given in the command, cmd. The list is basically preorder.
     * Modifications due to selection criteria creates "holes", but
     * preorder is maintained.
     * 
     * @param mod The module that request the list 
     * @param cmd The PIcommand that describes therequest
     * @return A list of selected modules
     */
    static Vector<Module> getModuleList(Module mod, PIcommand cmd) {
        Vector<Module> resultList;
        // we start with IDLIST
        // IDLIST will overrun all other selections, will define containment and sequence
        if (cmd.paramExist(PIcommand.IDLIST)) 
        {
            String ids = cmd.getValue(PIcommand.IDLIST);
            if (ids != null) {
                resultList = mod.getScriptHandler().getModuleListById(ids.split(","),true);
                if (resultList.size() == 0) {
                    mod.getReporter().pushMessage("empty_module_selection", cmd.toString());
                    return null;
                }
                return resultList;
            } else {
                mod.getReporter().pushMessage("empty_module_selection", cmd.toString());
                return null;
            }
        }

        //-----------------------
        // no id-list and we turn to other selection criteria
        // we start with all (rooted) modules in preorder sequence
        if (cmd.paramExist(PIcommand.ROOT)) 
        {
            resultList = mod.getScriptHandler().getAllModules(cmd.getValue(PIcommand.ROOT));
            if ((resultList==null) ||(resultList.size() == 0)) {
                mod.getReporter().pushMessage("empty_module_selection", cmd.toString());
                return null;
            }
        } 
        else 
        {
            // we start with all
            resultList = mod.getScriptHandler().getAllModules(null);

        }
        // then we reduce according to params in cmd
        // books,select( _sibling, _children), xpath(?)
        // the selection is done by AND-ing the criteria,
        // which means the common subset, the intersection

        // we try an xpath in script first
        if(cmd.paramExist(PIcommand.SCRIPTPATH))
        {
            String xp=cmd.getValue(PIcommand.SCRIPTPATH);
            if(xp!=null)
            {
                resultList =mod.getScriptHandler().getModuleListByXpath(xp);
                if (resultList.size() == 0) {
                    mod.getReporter().pushMessage("empty_module_selection", cmd.toString());
                    return null;
                }
            }
            else
            {
                mod.getReporter().pushMessage("empty_module_selection", cmd.toString());
                return null;
            }
        }


        // books 
        if (cmd.paramExist(PIcommand.BOOKS)) {
            String[] bks = cmd.getValue(PIcommand.BOOKS).split(",");
            Vector<Module> resMods = mod.getScriptHandler().getModulesInBook(bks);
            int ix = 0;
            while (ix < resultList.size()) 
            {
                if (!resMods.contains(resultList.elementAt(ix))) 
                    resultList.removeElement(resultList.elementAt(ix));
                else 
                    ix++;
            }
            // and we go for the intersection
            ix = 0;
            while (ix < resultList.size()) {
                if (!resMods.contains(resultList.elementAt(ix)))
                    resultList.removeElement(resultList.elementAt(ix));
                else
                    ix++;
            }

        }
        // now we have the modules in a certain book ordered preorder

        // we turn to siblings or children
        if (cmd.paramExist(PIcommand.SELECT)) {
            // we expect siblings or children
            String selection = cmd.getValue(PIcommand.SELECT);
            Vector<Module> resMods = new Vector<Module>();
            if (selection.compareTo(PIcommand.SIBLINGS) == 0) {
                Module parMod = (Module) mod.getParent();
                if (parMod == null) {
                    resMods = mod.getScriptHandler().getRootModules();
                } else {
                    Enumeration modenum = parMod.children();
                    while (modenum.hasMoreElements()) {
                        Module m = (Module) modenum.nextElement();
                        resMods.add(m);
                    }
                }
            } else if (selection.compareTo(PIcommand.CHILDREN) == 0) {
                Enumeration modenum = mod.children();
                while (modenum.hasMoreElements()) {
                    Module m = (Module) modenum.nextElement();
                    resMods.add(m);
                }
            } 
            else 
            {
                mod.getReporter().pushMessage("bad_processing_instruction", cmd.toString()," ");
                return null;
            }
            // and we go for the intersection
            int ix = 0;
            while (ix < resultList.size()) {
                if (!resMods.contains(resultList.elementAt(ix))) 
                    resultList.removeElement(resultList.elementAt(ix));
                else 
                    ix++;            
            }
            

        }

        // then we remove those who should _never be seen
        int ix=0;
        while(ix<resultList.size()) 
        {
            if(!resultList.elementAt(ix).getLinkable())
                resultList.removeElementAt(ix);
            else
                ix++;
        }
        
        // now we have ANDed SCRIPTPATH BOOKS and SELECT and they are all in preorder
        if (resultList.size() == 0) {
            mod.getReporter().pushMessage("empty_module_selection", cmd.toString());
            return null;
        }
        return resultList;
    }
    
    
    /**
     * Produce a documentfragment with content from a non-scriptet xml import
     * 
     * @param mod The Module requesting the content
     * @param cmd The PIcommand describing the request
     * @return a DocumentFragment, always
     */
    public DocumentFragment makePI_XMLContent(Module mod, PIcommand cmd)
    {
        //PI control:xpath exists

        DocumentFragment df = mod.getDoc().createDocumentFragment();
        URI found_uri = null;
        if (!cmd.paramExist(PIcommand.LOCATION))
        {
            // should not happen
            mod.getReporter().pushMessage("bad_processing_instruction", "Missing " + PIcommand.LOCATION);
            //df.appendChild(makeFailureComment(mod.getDoc(), cmd));
            df.appendChild(wxtError.makeErrorMsgForDebugging(mod.getDoc(), cmd,"missing location in Producer:XMLContent"));
            return df;
        }

        try {
            //found_uri = new URI(cmd.getValue(PIcommand.LOCATION));
            found_uri=accessutils.makeUri(cmd.getValue(PIcommand.LOCATION));
        }
        catch (Exception e)
        {
            mod.getReporter().pushMessage("bad_processing_instruction",cmd.toString(), e.getMessage());
            df.appendChild(makeFailureComment(mod.getDoc(), cmd));
            return df;
        }

        //--------- reuse content if possible ------------
        Content theContent=mod.getRegisteredContent(found_uri.toString());
        if(theContent==null)
        {
            try{
                theContent=new XMLContent(mod,cmd);
                mod.registerContent(found_uri.toString(), theContent);
            }
            catch(Exception ex)
            {
                mod.getReporter().pushMessage("bad_processing_instruction",cmd.toString(), ex.getMessage());
                df.appendChild(makeFailureComment(mod.getDoc(), cmd));
                return df;
            }
            
        }
        df=theContent.getContent(mod, cmd);

        if(!df.hasChildNodes())
        {
            // report only when not from template
            if(!cmd.paramExist(PIcommand.TEMPLATE))
                mod.getReporter().pushMessage("empty_xml_selection",cmd.toString());
            df.appendChild(makeEmptyComment(mod.getDoc(), cmd));
            return df;
        }
        String cat=accessutils.removeFilePart(mod.getAbsoluteUri()).toString();
        try{
            URI abs_uri=accessutils.makeAbsoluteURI(found_uri.toString(), cat);
            correctAddressing(df, mod, abs_uri);
        }
        catch(Exception ex)
        {
            mod.getReporter().pushMessage("bad_processing_instruction", cmd.toString(),ex.getMessage());
            df.appendChild(makeFailureComment(mod.getDoc(), cmd));
            return df;          
        }
       return df;
    }
    
    /**
     * Produce a documentfragment with content from a non-scriptet wiki import
     * 
     * @param mod The Module requesting the content
     * @param cmd The PIcommand describing the request
     * @return a DocumentFragment, always
     */
    public DocumentFragment makePI_WIKIContent(Module mod, PIcommand cmd) 
    {
        // PI control: LOCATION exists
        // if KEEPREFERENCES -> yes or no
        // if USECOPY -> yes or no
        // DPATH  or XPATH exists
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        URI abs_uri = null;
        if (!cmd.paramExist(PIcommand.LOCATION))
        {
            // should not happen
            mod.getReporter().pushMessage("bad_processing_instruction", "Missing " + PIcommand.LOCATION);
            df.appendChild(wxtError.makeErrorMsgForDebugging(mod.getDoc(), cmd,"missing location in Producer:WIKIContent"));
            //df.appendChild(makeFailureComment(mod.getDoc(), cmd));
            return df;
        }
        try
        {
            abs_uri = accessutils.makeUri(cmd.getValue(PIcommand.LOCATION));
        }           
        catch (Exception e)          
        {
            mod.getReporter().pushMessage("bad_processing_instruction",cmd.getOriginalData(), e.getMessage());
            df.appendChild(makeFailureComment(mod.getDoc(), cmd));
            return df;
         }

        
        // go for it
        //------------------------
        try{
            //WIKIContent wik=new WIKIContent(mod, cmd);
            xWIKIContent wik=new xWIKIContent(mod, cmd);
            df=wik.getContent(mod, cmd);
        }
        catch(Exception ex)
        {
            df=null;
        }
        //-----------------------*/

        /* ------alternative: rename getContent and make it static ----
         df = WIKIContent.getStaticContent(mod, cmd);
        //-----------------------*/

        if(df==null)
        {
            df = mod.getDoc().createDocumentFragment();
            df.appendChild(makeFailureComment(mod.getDoc(), cmd));
            return df;
        }
        if(!df.hasChildNodes())
            df.appendChild(makeEmptyComment(mod.getDoc(), cmd));
        return df;
    }

    
    /**
     * Produce a documentfragment with content from a OpenOffice document
     * @param mod The Module requesting the content
     * @param cmd The PIcommand describing the request
     * @return a DocumentFragment, always
     */
    public DocumentFragment makePI_ODFContent(Module mod, PIcommand cmd)
    {
        // PI control:location dpath exists
        //            if USECOPY >yes or no"
        DocumentFragment df=mod.getDoc().createDocumentFragment();
          xODTContent odtc=null;
            try{
                odtc=new xODTContent(mod,cmd);
            }
            catch(Exception ex)
            {
                // should not happen
                df.appendChild(wxtError.makeErrorMsgForDebugging(mod.getDoc(), cmd,ex.getMessage()+" in producer:ODFContent"));
                mod.getReporter().pushMessage("pi_produced_no_result", cmd.getOriginalData(), ex.getMessage());
                return df;
            }
            DocumentFragment tmp=odtc.getContent(mod, cmd);
            if((tmp==null) || (!tmp.hasChildNodes()))
            {
                mod.getReporter().pushMessage("pi_produced_no_result", cmd.getOriginalData());
                return df; //empty
            }
            return tmp;
    }


    /**
     * Produce a documentfragment with a mathml formula from a OpenOffice document
     *
     * @param mod The Module requesting the content
     * @param cmd The PIcommand describing the request
     * @return a DocumentFragment, always
     */
    public DocumentFragment makePI_ODTFormula(Module mod, PIcommand cmd)
    {
        // PI:control: location or id
        ODTFormulas formulas=mod.getDefinitions().getFormulas();
        DocumentFragment df;
        if(cmd.paramExist(PIcommand.ID))
        {
            // we get it from the store formulas
            df=formulas.getFormula(mod, cmd);
        }
        else if(cmd.paramExist(PIcommand.LOCATION))
        {
            // we will load a formula directly from a
            // odf-file with only one formula
            df=formulas.getFormulaDirect(mod, cmd);
        }
        else
        {
            // should not happen
            df=mod.getDoc().createDocumentFragment();
            df.appendChild(wxtError.makeErrorMsgForDebugging(mod.getDoc(), cmd, "formula without location and id in producer:ODTFormula"));
            //df.appendChild(makeFailureComment(mod.getDoc(), cmd));
        }
        if(!df.hasChildNodes())
            df.appendChild(makeEmptyComment(mod.getDoc(), cmd));
        return df;
    }



     /**
     * Produce a documentfragment with a gadget
     *
     * @param mod The Module requesting the content
     * @param cmd The PIcommand describing the request
     * @return a DocumentFragment, always
     */
    public DocumentFragment makePI_Gadget(Module mod, PIcommand cmd)
    {
        // PI controlled
        /* pattern:
         <div class gadget
            <div class on/off
            <div class content
         */
        DocumentFragment df=mod.getDoc().createDocumentFragment();
        // we have either a location for an iframe
        // or we have a fragmentid for strait content
        
        URI iframeSrctUri=null;
        DocumentFragment contentFragment=null;
        if(cmd.paramExist(PIcommand.LOCATION))
        {
            // fix address to iframe source
            String targetUriStr=cmd.getValue(PIcommand.LOCATION);
            targetUriStr=mod.getDefinitions().substituteFragments(targetUriStr);
            URI absoluteTargetUri=null;

            try{
                absoluteTargetUri = accessutils.makeAbsoluteURI(targetUriStr, mod.getCatalog());
                iframeSrctUri=accessutils.makeRelativeURI(mod.getAbsoluteUri(), absoluteTargetUri);
            }
            catch(Exception uex)
            {
                mod.getReporter().pushMessage("bad_processing_instruction", cmd.toString());
                df.appendChild(makeFailureComment(mod.getDoc(),cmd));
                return df;

            }
        }
        else if(cmd.paramExist(PIcommand.FRAGMENTID))
        {
            String fid=cmd.getValue(PIcommand.FRAGMENTID);
            Fragment f=mod.getDefinitions().getFragments().get(fid);
            if(f==null)
            {
                mod.getReporter().pushMessage("no_fragment_produced_from", cmd.toString());
                df.appendChild(makeFailureComment(mod.getDoc(),cmd));
                return df;
            }
            contentFragment=f.getFragment();
        }
        else
        {
            // should not happen
            // no location and no fragment id
            return df;
        }

        // adjust header width with +5
        String W=cmd.getValue(PIcommand.WIDTH).trim();
        String hW=W;
        if(W.endsWith("px"))
        {
            W=W.substring(0, W.length()-2);
            try{hW=""+(Integer.valueOf(W)+5)+"px"; hW=hW.trim();}
            catch(Exception ex)
            {}
            W=W+"px";
        }

        // CSS properties: left, top
        String left=null;
        String top=null;
        if(cmd.paramExist(PIcommand.LEFT))
            left=cmd.getValue(PIcommand.LEFT);
        if(cmd.paramExist(PIcommand.TOP))
            top=cmd.getValue(PIcommand.TOP);

        // height
        String H=null;
        if(cmd.paramExist(PIcommand.HEIGHT))
            H=cmd.getValue(PIcommand.HEIGHT).trim();

        // title
        String Title="_";
        if(cmd.paramExist(PIcommand.TITLE))
            Title=cmd.getValue(PIcommand.TITLE);

        // expanded
        boolean expanded=false;
        if(cmd.paramExist(PIcommand.EXPANDED))
            expanded=cmd.getValue(PIcommand.EXPANDED).trim().compareTo(PIcommand.YES)==0;
        
        // position
        String position=null;
        if(cmd.paramExist(PIcommand.POSITION))
            position=cmd.getValue(PIcommand.POSITION);
        
        // movable
        boolean movable=false;
        if(cmd.paramExist(PIcommand.MOVABLE))
                movable=cmd.getValue(PIcommand.MOVABLE).compareTo(PIcommand.YES)==0;
        if(movable && (position==null))
            position="absolute";

        //id
        String id=Title.replace(' ', '_');
        if(cmd.paramExist(PIcommand.ID))
            id=cmd.getValue(PIcommand.ID);        
        
        
        // ready to produce element
        // wrapper
        Element wrapper=mod.getDoc().createElement("div");
        wrapper.setAttribute("class", cmd.getCommandStyleName());
        String styleS="";
        if(left!=null)
            styleS=styleS+PIcommand.LEFT+":"+left+";";
        if(top!=null)
            styleS=styleS+PIcommand.TOP+":"+top+";";
        styleS=styleS+PIcommand.WIDTH+":"+hW+";";
        if(position!=null)
            styleS=styleS+PIcommand.POSITION+":"+position;
        if(styleS.length()>1)
            wrapper.setAttribute("style", styleS);

        /*
         if(position==null)
            wrapper.setAttribute("style", "width:"+hW); // added 5
        else
            wrapper.setAttribute("style", "width:"+hW+";position:"+position); // added 5
         */
        wrapper.setAttribute("id", id);
        

        //header
        Element header=mod.getDoc().createElement("div");
        header.setAttribute("class", "header");
        if(movable)
            header.setAttribute("style", "cursor:move");
        
        Element spanElt=mod.getDoc().createElement("span");       
        if(expanded)
            spanElt.setAttribute("class","on");
        else
            spanElt.setAttribute("class","off");        
        spanElt.setAttribute("onclick", "toggleGadget(this);");
        spanElt.appendChild(mod.getDoc().createTextNode("_"));
        
        header.appendChild(spanElt);
        header.appendChild(mod.getDoc().createTextNode(Title));

        // content
        Element content=mod.getDoc().createElement("div");
        content.setAttribute("class", "content");
        if(expanded)
            content.setAttribute("style", "display:block");
        else
            content.setAttribute("style", "display:none");

        if(iframeSrctUri!=null) 
        {
            //iframe
            Element iframe=mod.getDoc().createElement("iframe");
            //iframe.setAttribute("src",cmd.getValue(PIcommand.LOCATION));
            iframe.setAttribute("src",iframeSrctUri.toString());
            iframe.setAttribute("width",W);
            if(H!=null)
                iframe.setAttribute("height",H);            
            iframe.appendChild(mod.getDoc().createTextNode(" "));
            content.appendChild(iframe);
        }
        else  
        {
            //direct content
            content.appendChild(mod.getDoc().importNode(contentFragment, true));
        }
        

        wrapper.appendChild(header);
        wrapper.appendChild(content);

        // initmove
        if(movable)
        {
            Element scriptElt=mod.getDoc().createElement("script");
            scriptElt.setAttribute("type", "text/javascript");
            scriptElt.appendChild(mod.getDoc().createTextNode("dragDrop.initElement('"+id+"');"));
            wrapper.appendChild(scriptElt);
        }

        
        df.appendChild(wrapper);
        return df;
    }

    /**
     * Produce a documentfragment with an image
     *
     * @param mod The Module requesting the content
     * @param cmd The PIcommand describing the request
     * @return a DocumentFragment, always
     */
    public DocumentFragment makePI_Image(Module mod, PIcommand cmd)
    {
        DocumentFragment df=mod.getDoc().createDocumentFragment();

        // attempt to register this image on the fly
        // if location is set, it is considered as new
        // and all attributes as in an image file is allowed
        if(cmd.paramExist(PIcommand.LOCATION))
        {
            Imagex theImage=null;
            try{
               theImage=new Imagex(cmd,mod.getAbsoluteUri(),mod.getDefinitions());
               mod.getDefinitions().registerNewImage(theImage);
               // we let the cmd contain what we want to use below
               // which is only the id, which is already there
            }
            catch(Exception ex)
            {
                // error and return
                return df;
            }
        }
        //-----------------------------------------
        // use existing, which may be a newly reistered above
        if(cmd.paramExist(PIcommand.ID))
        {
            String sid=cmd.getValue(PIcommand.ID);

            // notify globally, and return index(-1 if not exist)
            int globalindex=mod.getDefinitions().getImageHolder().addImageReference(sid,mod)+1;

            // only globally;
            // transport index in cmd
            cmd.setParameter(PIcommand.INDEX, ""+globalindex);

        }
        else
        {
            mod.getReporter().pushMessage("bad_processing_instruction", "missing id");
            df.appendChild(wxtError.makeErrorMsgForDebugging(mod.getDoc(), cmd, "missing id in producer:Image"));
            //df.appendChild(makeFailureComment(mod.getDoc(),cmd));
            return df;
        }


        String imStr=cmd.getValue(PIcommand.ID);
        Imagex im=mod.getDefinitions().getImage(imStr);
        if(im==null)
        {
            mod.getReporter().pushMessage("bad_processing_instruction", "missing image:"+imStr);
            df.appendChild(makeFailureComment(mod.getDoc(),cmd));
            return df;
        }
        return im.showImage(mod, cmd);
    }

    
    /**
     * Produce a documentfragment with an imagelist
     *
     * @param mod The Module requesting the content
     * @param cmd The PIcommand describing the request
     * @return a DocumentFragment, always
     */
    public DocumentFragment makePI_ImageList(Module mod, PIcommand cmd)
    {
        DocumentFragment df=mod.getDoc().createDocumentFragment();
        // get a list of all modules we want to investigate
        // cmd attributes as in modulemap etc.
        Vector<Module> moduleList = null;
        moduleList = getModuleList(mod, cmd);
        if(moduleList==null || moduleList.size()==0)
        {
            df.appendChild(makeFailureComment(mod.getDoc(), cmd));
            mod.getReporter().pushMessage("empty_module_selection", cmd.toString());
            df.appendChild(makeEmptyComment(mod.getDoc(), cmd));
            return df;
        }

        // find height of thumbs
        Imagex.THUMBHEIGHT=Imagex.DEFAULT_THUMBHEIGHT;
        if(cmd.paramExist(PIcommand.THUMBSIZE))
        {
            String tmp=cmd.getValue(PIcommand.THUMBSIZE);
            int val=accessutils.getNumericStart(tmp);
            if(val!=-1)
            {
                Imagex.THUMBHEIGHT=val;
            }
        }

        // get all used images
        //Vector<String> usedIds=mod.getDefinitions().getImageHolder().getUniqueReferenceIds();
        Vector<String> usedIds=mod.getDefinitions().getImageHolder().getAllReferenceIds();
        if(usedIds.size()==0)
        {
            // empty message
            return df;
        }

        // reduce the idlist so it only contains images on the actual modulelist
        for(int ix=usedIds.size()-1;ix >=0;ix--)
        {
            Module ixmod=mod.getDefinitions().getImageHolder().getModuleAtIndex(ix);
            boolean found=false;
            for(Module m : moduleList)
                found= found || (m==ixmod);
            if(!found)
                usedIds.removeElementAt(ix);
        }

        // reduce duplicates in usedIds
        for(int ix=0;ix<usedIds.size();ix++)
        {
            String ixS=usedIds.elementAt(ix);
            int ix2=ix+1;
            while(ix2 < usedIds.size())
            {
                if(usedIds.elementAt(ix2).compareTo(ixS)==0)
                    usedIds.removeElementAt(ix2);
                else
                    ix2++;
            }
        }
        // do we want to split. insert break, on new module
        Boolean breakOnModule=cmd.paramExist(PIcommand.SPLIT) &&
                (cmd.getValue(PIcommand.SPLIT).compareTo(PIcommand.YES)==0);
        Element mainWrapper=mod.getDoc().createElement("div");
        
        Element thumbWrapper=mod.getDoc().createElement("div"); // or span
        thumbWrapper.setAttribute("class", cmd.getCommandStyleName());
        Module thisM=null;
        for(String id : usedIds)
        {
            Imagex im=mod.getDefinitions().getImage(id);
            Module m=mod.getDefinitions().getImageHolder().getFirstModuleUsing(id);
            // break the line on new module
            if(breakOnModule &&((thisM==null)||(thisM!=m)))
            {
                thumbWrapper.appendChild(mod.getDoc().createElement("br"));
                thisM=m;
            }
            thumbWrapper.appendChild(im.produceThumbImage(mod, cmd));
        }
        //---------------
        Element storageWrapper=mod.getDoc().createElement("div");
        storageWrapper.setAttribute("style", "display:block");
        for(String id : usedIds)
        {
            Imagex im=mod.getDefinitions().getImage(id);
            storageWrapper.appendChild(im.produceHiddenImage(mod, cmd));
        }
        
        df.appendChild(thumbWrapper);
        df.appendChild(storageWrapper);
                
        return df;
    }

    /**
     * Produce an image with content from a non-scriptet txt import
     * 
     * @param mod The Module requesting the content
     * @param cmd The PIcommand describing the request
     * @return a DocumentFragment, always
     */
    public DocumentFragment makePI_TXTContent(Module mod, PIcommand cmd) 
    {
        // PI control:if PARSE -> yes or no
        //            if code: legal code
        if(!cmd.paramExist(PIcommand.LOCATION))
        {
            // should not happen
            mod.getReporter().pushMessage("bad_processing_instruction", "Missing " + PIcommand.LOCATION);
            DocumentFragment df=mod.getDoc().createDocumentFragment();
            df.appendChild(wxtError.makeErrorMsgForDebugging(mod.getDoc(), cmd, "missing location in producer:TXTContent"));
            //df.appendChild(mod.getProducer().makeFailureComment(mod.getDoc(),cmd));
            return df;           
        }



        //--------- reuse content if possible ------------
        DocumentFragment df=mod.getDoc().createDocumentFragment();
        Content theContent=mod.getRegisteredContent(cmd.getValue(PIcommand.LOCATION));
        if(theContent==null)
        {
            try{
                theContent=new TXTContent(mod,cmd);
                mod.registerContent(cmd.getValue(PIcommand.LOCATION), theContent);
            }
            catch(Exception ex)
            {
                mod.getReporter().pushMessage("bad_processing_instruction",cmd.toString(), ex.getMessage());
                df.appendChild(makeFailureComment(mod.getDoc(), cmd));
                return df;
            }
        }
        df=theContent.getContent(mod, cmd);


        if(!df.hasChildNodes())
        {
            // report only when not updating template
            if(!cmd.paramExist(PIcommand.TEMPLATE))
                    mod.getReporter().pushMessage("pi_produced_no_result", cmd.toString());
            df.appendChild(mod.getProducer().makeEmptyComment(mod.getDoc(),cmd));
        }
        return df;
    }
    
    /**
     * Produce a popup element
     * Depending on javascript functio: simplePopup
     * 
     * @param mod The Module that request the popup
     * @param cmd The PICommand that describes the list of modules and the collection
     * @return A documentFragment
     */
    public DocumentFragment makePI_Popup(Module mod, PIcommand cmd) 
    {        
       // PI control:
        // location exists
        // if(LEFTPAR or RIGHTPAR or REPLACE or SELECT or PARSE or CODE)-> sourcelocation exists
        // location != sourcelocation
        // if CODE -> a legal code
        DocumentFragment df=mod.getDoc().createDocumentFragment();
        // can we use it at all        
        if(!cmd.paramExist(PIcommand.LOCATION))
        {
            // should not happen
            mod.getReporter().pushMessage("bad_processing_instruction", "Missing " + PIcommand.LOCATION);
            df.appendChild(wxtError.makeErrorMsgForDebugging(mod.getDoc(), cmd, "missing location in producer:Popup"));
            //df.appendChild(makeFailureComment(mod.getDoc(),cmd));
            return df;           
        }
        String targetUriStr=cmd.getValue(PIcommand.LOCATION);
        targetUriStr=mod.getDefinitions().substituteFragments(targetUriStr);
        URI absoluteTargetUri=null;
        URI relativeTargetUri=null;
        try{
            absoluteTargetUri = accessutils.makeAbsoluteURI(targetUriStr, mod.getCatalog());
            relativeTargetUri=accessutils.makeRelativeURI(mod.getAbsoluteUri(), absoluteTargetUri);
        }
        catch(Exception uex){
            mod.getReporter().pushMessage("bad_processing_instruction", cmd.toString(),uex.getMessage());
            df.appendChild(makeFailureComment(mod.getDoc(),cmd));
            return df;           
           
        }
        // there is no reason to load and prepare anything if 
        // dont have a load - prepare - save strategy
        // the dependancy sourcelocation and textchanges are controlled in PIcommand:control
        if(cmd.paramExist(PIcommand.SOURCELOCATION))
        {

            // we attempt to get the text, with encoding, selection and replaces,
            // and possiblly transformation
            String theText="";
            try{
                theText=TXTContent.getFormattedText(mod,cmd);
            }
            catch(Exception e)
            {
               mod.getReporter().pushMessage("bad_processing_instruction", cmd.toString(),e.getMessage());
               return Content.getFailedContent(mod, cmd);
            }

            if(theText.isEmpty())
            {
                mod.getReporter().pushMessage("pi_produced_no_result", cmd.toString());
                df.appendChild(makeEmptyComment(mod.getDoc(),cmd));
                return df;
            }

            //--------------------------
            // we have the text prepared according to cmd
            // we save it to the location it will be popped from
            if((cmd.paramExist(PIcommand.CODE)) || (cmd.paramExist(PIcommand.PARSE)))
            {
                try {
                    // we have a case with colorcoded or parsed stuff
                    // and since we want a popup, we try to make a complete file
                    // ------- THIS IS VERY SHAKY ------------
                    // steal head from module, so we get a likely stylesheet
                    // NOTE that this will not readdress referenced files in the header
                    Document popDoc=domer.produceHTMLPageFromContent(theText,mod.getEncoding(),mod);

                    // use outputformat of module ?
                    domer.saveDom(popDoc, absoluteTargetUri,mod.getEncoding(), false,mod.getOutputFormat());
                } 
                catch (Exception ex) 
                {
                    mod.getReporter().pushMessage("bad_processing_instruction", cmd.toString(),ex.getMessage());
                    return Content.getFailedContent(mod, cmd);
                }
            }
            else
            {
                // we simply save as is
                accessutils.saveTFile(absoluteTargetUri, theText,mod.getEncoding());
            }

        }
        
        // prepare the documentfragment to return
        Element elt=mod.getDoc().createElement("span");
        elt.setAttribute("class",cmd.getCommandStyleName());
        if(cmd.paramExist(PIcommand.TITLE))
            elt.setAttribute("title", cmd.getValue(PIcommand.TITLE));
        else
            elt.setAttribute("title", "popup");
        String tmpT=relativeTargetUri.toString();
        if(cmd.paramExist(PIcommand.TEXT))
            tmpT=cmd.getValue(PIcommand.TEXT);
        elt.appendChild(mod.getDoc().createTextNode(tmpT));
        String tmps="javascript:simplepopup('"+relativeTargetUri.toString()+"','"+"popup"+"','*')";
        elt.setAttribute("onclick", tmps);
        df=mod.getDoc().createDocumentFragment();
        df.appendChild(elt);
        return df;               
    }

    
    /**
     * Produce an on/off expansion based on an ajax request.
     * PI is expand
     * Depending on javascript functions: expand and unexpand
     * 
     * @param mod The module that makes the request
     * @param cmd The PIcommand that describes the request
     * @return A documentFragment
     */
    public DocumentFragment makePI_ExpandAJAX(Module mod, PIcommand cmd)
    {
        // PI control:
        // location exists
        // if(LEFTPAR or RIGHTPAR or REPLACE or SELECT or PARSE or CODE)-> sourcelocation exists
        // location != sourcelocation
        // if CODE -> a legal code
        // pattern
        /*
         <div class="expand">
            <div class on/off onclick="">title</div>
            <div class content
            </div>
        </div>
         */

        DocumentFragment df=mod.getDoc().createDocumentFragment();

        // can we use it at all
        if(!cmd.paramExist(PIcommand.LOCATION))
        {
            // should not happen
            mod.getReporter().pushMessage("bad_processing_instruction", "Missing " + PIcommand.LOCATION);
            df.appendChild(wxtError.makeErrorMsgForDebugging(mod.getDoc(), cmd, "missing location in producer:ExpandAJAX"));
            //df.appendChild(makeFailureComment(mod.getDoc(),cmd));
            return df;           
        }
        String targetUriStr=cmd.getValue(PIcommand.LOCATION);
        targetUriStr=mod.getDefinitions().substituteFragments(targetUriStr);
        URI absoluteTargetUri=null;
        URI relativeTargetUri=null;
        try{
            absoluteTargetUri = accessutils.makeAbsoluteURI(targetUriStr, mod.getCatalog());
            relativeTargetUri=accessutils.makeRelativeURI(mod.getAbsoluteUri(), absoluteTargetUri);
        }
        catch(Exception uex){
            mod.getReporter().pushMessage("bad_processing_instruction", cmd.toString());
            df.appendChild(makeFailureComment(mod.getDoc(),cmd));
            return df;           
           
        }

        // do we have a title
        String title=" ";
        if(cmd.paramExist(PIcommand.SOURCELOCATION))
            title=cmd.getValue(PIcommand.SOURCELOCATION);
        else
            title=relativeTargetUri.toString();
        if(cmd.paramExist(PIcommand.TITLE))
            title=cmd.getValue(PIcommand.TITLE);

        // we attempt to get the text, ready with encoding, selection and replaces, 
        String theText="";
        try{
            theText=TXTContent.getFormattedText(mod,cmd);
        }
        catch(Exception e)
        {
           mod.getReporter().pushMessage("bad_processing_instruction", cmd.toString(),e.getMessage());
           return Content.getFailedContent(mod, cmd);
        }

        if(theText==null || theText.isEmpty())
        {
            mod.getReporter().pushMessage("pi_produced_no_result", cmd.toString());
            df.appendChild(makeFailureComment(mod.getDoc(),cmd));
            return df;
        }

        //--------------------------
        // we have the text prepared according to cmd
        // we save it if this is asked for, that is if we have fetched it from a sourcelocation
        if((absoluteTargetUri!=null)&&(cmd.paramExist(PIcommand.SOURCELOCATION)))  
        {
            if(!accessutils.saveTFile(absoluteTargetUri,theText,mod.getEncoding()))
            {
                mod.getReporter().pushMessage("could_not_save_temp_file", cmd.toString());
                // we do not give up since file may have been saved before
                // meaningfull since we may build on a place we dont have write access
                // as in collect
                //df.appendChild(makeFailureComment(mod.getDoc(),cmd));
                //return df;
           }   
        }
        try{
            df=(DocumentFragment)mod.getDoc().importNode(domer.produceDocFragmentFromString(theText, mod.getEncoding(),mod.getReporter()),true);
        }
        catch(Exception dex)
        {
            mod.getReporter().pushMessage("pi_produced_no_result", cmd.toString());
            df.appendChild(makeFailureComment(mod.getDoc(),cmd));
            return df;          
        }

        
        // prepare the element
        boolean expandit=false;
        // expanded or not
        if(cmd.paramExist(PIcommand.EXPANDED))
        {
            String expandvalue=cmd.getValue(PIcommand.EXPANDED);           
            expandit=expandvalue.compareToIgnoreCase(PIcommand.YES)==0;
        }
        // correct for global option, overriding cmd
        // as set in script
        String exall=mod.getDefinitions().getOption(Definitions.EXPAND_ALL);
        if( (exall!=null)&& (exall.compareToIgnoreCase("yes")==0))
            expandit=true;
        
        // general expand option (expand-all) should override the attribute if set to yes
        // as parameter to scripthandler (GUI-set option)
        if(!expandit)
        {
            String tmp=mod.getScriptHandler().getArgument(Definitions.EXPAND_ALL);
            if((tmp != null) && (tmp.compareToIgnoreCase(PIcommand.YES)==0))
                expandit=true;
        }


        Element wrapper=mod.getDoc().createElement("div");
        wrapper.setAttribute("class", cmd.getCommandStyleName());

        Element header=mod.getDoc().createElement("div");
        header.setAttribute("class","header");
        Element spanElt=mod.getDoc().createElement("span");
        spanElt.setAttribute("onclick", "javascript:toggleExpandAjax('"+relativeTargetUri+"',this)");
        spanElt.appendChild(mod.getDoc().createTextNode("_"));
        Element content=mod.getDoc().createElement("div");
        content.setAttribute("class", "content");
        if(expandit)
        {
            spanElt.setAttribute("class", "on");
            content.appendChild(df.cloneNode(true));

        }
        else
        {
            spanElt.setAttribute("class", "off");
            content.appendChild(mod.getDoc().createTextNode(" "));
        }
        header.appendChild(spanElt);
        header.appendChild(mod.getDoc().createTextNode(title));
        
        
        wrapper.appendChild(header);
        wrapper.appendChild(content);

        df=mod.getDoc().createDocumentFragment();
        df.appendChild(wrapper);
        return df;
    }

     /**
     * Produce an on/off expansion basedon style change
     * No AJAX involved, no javascript involved
     * PI is expandsimple
     * 
     * @param mod The module that makes the request
     * @param cmd The PIcommand that describes the request
     * @return A documentFragment
     */
    public DocumentFragment makePI_ExpandSimple(Module mod, PIcommand cmd)
    {
        // PI control: location exists, sourcelocation NOT exists
        // if expanded -> yes or no
        // if code -> it is a legal code
        /*
         <div class expandsimple
            <div class on/off
            <div class content 
         </div>
         */

        DocumentFragment df=mod.getDoc().createDocumentFragment();
        DocumentFragment stuff=mod.getDoc().createDocumentFragment();
        // can we use it at all        
        if(!cmd.paramExist(PIcommand.LOCATION))
        {
            // should not happen
            mod.getReporter().pushMessage("bad_processing_instruction", "Missing " + PIcommand.LOCATION);
            df.appendChild(wxtError.makeErrorMsgForDebugging(mod.getDoc(), cmd, "missing location in producer:ExpandSimple"));
            //df.appendChild(makeFailureComment(mod.getDoc(),cmd));
            return df;           
        }
        // source location is illegal here and may do some damage in getFailedContent
        // we do this since getFailedContent will save in location if sourcelocation exists
        // it should be catched in PI:control, but anyhow:
        if(cmd.paramExist(PIcommand.SOURCELOCATION))
        {
            // should not happen
            cmd.setParameter(PIcommand.LOCATION, cmd.getValue(PIcommand.SOURCELOCATION));
            df.appendChild(wxtError.makeErrorMsgForDebugging(mod.getDoc(), cmd, "source location not allowed (removed) in producer:ExpandSimple"));
            cmd.removeParameter(PIcommand.SOURCELOCATION);
        }
        String targetUriStr=cmd.getValue(PIcommand.LOCATION);
        targetUriStr=mod.getDefinitions().substituteFragments(targetUriStr);
        URI absoluteTargetUri=null;
        URI relativeTargetUri=null;
        try{
            absoluteTargetUri = accessutils.makeAbsoluteURI(targetUriStr, mod.getCatalog());
            relativeTargetUri=accessutils.makeRelativeURI(mod.getAbsoluteUri(), absoluteTargetUri);
        }
        catch(Exception uex){
            mod.getReporter().pushMessage("bad_processing_instruction", cmd.toString());
            df.appendChild(makeFailureComment(mod.getDoc(),cmd));
            return df;           
           
        }

        String title=relativeTargetUri.toString();
        if(cmd.paramExist(PIcommand.TITLE))
            title=cmd.getValue(PIcommand.TITLE);

        // we attempt to get the text, ready with encoding, selection and replaces, 
        String theText="";
        try{
            theText=TXTContent.getFormattedText(mod,cmd);
        }
        catch(Exception e)
        {
           mod.getReporter().pushMessage("bad_processing_instruction", cmd.toString(),e.getMessage());
           return Content.getFailedContent(mod, cmd);
        }

        if(theText==null || theText.isEmpty())
        {
            mod.getReporter().pushMessage("pi_produced_no_result", cmd.toString());
            df.appendChild(makeFailureComment(mod.getDoc(),cmd));
            return df;
        }

        //--------------------------
        // we have the text prepared according to cmd
        try{
            stuff=(DocumentFragment)mod.getDoc().importNode(domer.produceDocFragmentFromString(theText, mod.getEncoding(),mod.getReporter()),true);
        }
        catch(Exception dex)
        {
            mod.getReporter().pushMessage("pi_produced_no_result", cmd.toString());
            df.appendChild(makeFailureComment(mod.getDoc(),cmd));
            return df;          
        }

        
        // prepare the element
        boolean expandit=false;
        // expanded or not
        if(cmd.paramExist(PIcommand.EXPANDED))
        {
            String expandvalue=cmd.getValue(PIcommand.EXPANDED);           
            expandit=expandvalue.compareToIgnoreCase(PIcommand.YES)==0;
        }
        // correct for global option, overriding cmd
        String exall=mod.getDefinitions().getOption(Definitions.EXPAND_ALL);
        if( (exall!=null)&& (exall.compareToIgnoreCase("yes")==0))
            expandit=true;
        
        // general expand option (expand-all) should override the attribute if set to yes
        if(!expandit)
        {
            String tmp=mod.getScriptHandler().getArgument(Definitions.EXPAND_ALL);
            if((tmp != null) && (tmp.compareToIgnoreCase(PIcommand.YES)==0))
                expandit=true;
        }


        // new
        Element wrapper=mod.getDoc().createElement("div");
        wrapper.setAttribute("class", cmd.getCommandStyleName());

        Element header=mod.getDoc().createElement("div");
        header.setAttribute("class", "header");
        Element spanElt=mod.getDoc().createElement("span");
        spanElt.setAttribute("onclick", "javascript:toggleExpandSimple(this)");
        spanElt.appendChild(mod.getDoc().createTextNode("_"));
        header.appendChild(spanElt);
        header.appendChild(mod.getDoc().createTextNode(title));
        Element content=mod.getDoc().createElement("div");
        content.setAttribute("class", "content");
        content.appendChild(stuff);
        if(expandit)
        {
            spanElt.setAttribute("class", "on");
            content.setAttribute("style", "display:block");
        }
        else
        {
            spanElt.setAttribute("class", "off");
            content.setAttribute("style", "display:none");
        }
        wrapper.appendChild(header);
        wrapper.appendChild(content);
        df.appendChild(wrapper);
        return df; 
    }
    
    
    /**
     * Inserts a reference from a reference list. 
     * 
     * @param mod The module requesting the reference 
     * @param cmd The PI-command describing the reference-request 
     * @return The resulting nodecollection in a fragment 
     */
    public DocumentFragment makePI_Reference(Module mod, PIcommand cmd) 
    {
        // PI control: id exists
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        
        if(!cmd.paramExist(PIcommand.ID))
        {
            // should not happen
            mod.getReporter().pushMessage("missing_id_in_referenece");
            df.appendChild(wxtError.makeErrorMsgForDebugging(mod.getDoc(), cmd, "missing id in producer:reference"));
            return df;
        }
        String id=cmd.getValue(PIcommand.ID);
        Reference r=mod.getDefinitions().getReference(id);
        if(r==null)
        {
            mod.getReporter().pushMessage("no_referenece_found",id);
            df.appendChild(makeFailureComment(mod.getDoc(), cmd));
            return df;       
        }
        
        
        // ok, its there, produce it with the selected style
        Element elt=r.produceIntextReference(mod,cmd,mod.getDefinitions().getOption(Definitions.REFERENCE_FORM));
        df.appendChild(elt);
        
        return df;

    }



    /**
     * Producing a global (more than one module) referencelist
     * Assuming option reference-indexing:local
     * and the cmd has attribute type:global
     * @param mod The module requesting the reference 
     * @param cmd The PI-command describing the reference-request
     * @return The result as a fragment
     */
    private DocumentFragment makeModuleSortedRefereceList(Module mod, PIcommand cmd)
    {
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        // all odules are candidates
        Vector<Module>ModList = mod.getScriptHandler().getAllModules(null);
        // harvard
       if(mod.getDefinitions().getOption(Definitions.REFERENCE_FORM).compareTo(Definitions.HARVARD)==0)
       {
           for(Module m : ModList)
           {
                Vector<String> refs=m.getReferenceHolder().getSortedReferenceIds();
                // any refs ?
                if(refs.size()==0)
                    continue;
                Element ModWrapper=mod.getDoc().createElement("div");
                ModWrapper.setAttribute("class", PIcommand.WXTSTYLEPREFIX+"modref");

                // refereenc from mod to m
                Element e = makeXLink(mod, m, "div", cmd.getCommandStyleName(), NO_LEVEL_ADJUST,true);
                ModWrapper.appendChild(e);
                df.appendChild(ModWrapper);

                Element wrapper=mod.getDoc().createElement("div");
                wrapper.setAttribute("class", PIcommand.WXTSTYLEPREFIX+"harvardlist");
                for(int rix=0;rix<refs.size();rix++)
                {
                    String ids=refs.elementAt(rix);
                    Reference r=mod.getDefinitions().getReference(ids);
                    wrapper.appendChild(r.produceListElement(mod,mod.getDefinitions().getOption(Definitions.REFERENCE_FORM),rix+1));
                    df.appendChild(wrapper);
                }
            }
        return df;
       }
        // simple or ieee
        for(Module m : ModList)
            {
                Vector<String> ModRefs=m.getReferenceHolder().getAllReferenceIds();
                // any refs ?
                if(ModRefs.size()==0)
                    continue;
                Element ModWrapper=mod.getDoc().createElement("div");
                ModWrapper.setAttribute("class", PIcommand.WXTSTYLEPREFIX+"modref");
                // refereenc from mod to m
                Element e = makeXLink(mod, m, "div", cmd.getCommandStyleName(), NO_LEVEL_ADJUST,true);
                ModWrapper.appendChild(e);
                df.appendChild(ModWrapper);



                Element reflistdiv=mod.getDoc().createElement("div");
                reflistdiv.setAttribute("class", PIcommand.WXTSTYLEPREFIX+"ieeelist");
                Element olElt=mod.getDoc().createElement("ol");
                for(int ix=0;ix<ModRefs.size();ix++)
                {
                    String ids=ModRefs.elementAt(ix);
                    Reference r=mod.getDefinitions().getReference(ids);
                    Element liElt=mod.getDoc().createElement("li");
                    liElt.appendChild(r.produceListElement(mod,mod.getDefinitions().getOption(Definitions.REFERENCE_FORM), ix+1));
                    olElt.appendChild(liElt);
                }
                reflistdiv.appendChild(olElt);
                df.appendChild(reflistdiv);
            }           
        return df;
    }

    /**
     * Inserts a referenselist 
     * 
     * @param mod The module requesting the referencelist 
     * @param cmd The PI-command describing the reference-request 
     * @return The resulting nodecollection in a fragment 
     */
    public DocumentFragment makePI_ReferenceList(Module mod, PIcommand cmd)
    {
        //PI control: if SCOPE-> GLOBAL or LOCAL
        //            if RESET-> yes or no
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        boolean is_local=true;
        if(cmd.paramExist(PIcommand.SCOPE))
            is_local=cmd.getValue(PIcommand.SCOPE).compareTo(PIcommand.LOCAL)==0;

        // if we have local indexing and this asks for
        // a global index, we sort indexing on modules
        if((mod.getDefinitions().getOption(Definitions.REFERENCE_INDEXING).compareTo(Definitions.LOCAL)==0)
            &&(!is_local))
            {
                df=makeModuleSortedRefereceList(mod,cmd);
                // reset global index?   
                if(( cmd.paramExist(PIcommand.RESET)) &&
                       (cmd.getValue(PIcommand.RESET).compareTo(PIcommand.YES)==0))
                    mod.getDefinitions().initUsedReferences();                
                if(!df.hasChildNodes())
                    df.appendChild(makeEmptyComment(mod.getDoc(), cmd));
                return df;
            }
        
        //-----------------
        // we pick up the actual list as either global or local
        Vector<String>refs=null;
         // what type is it
        
        // get correct list
        if(is_local)
        {
            refs=mod.getReferenceHolder().getSortedReferenceIds();
        }
        else
        {
            refs=mod.getDefinitions().getReferenceHolder().getSortedReferenceIds();
        }
        
        // we have a list in referenced order
        Element wrapper=null;
        String form=mod.getDefinitions().getOption(Definitions.REFERENCE_FORM);
        if((form.compareTo(Definitions.IEEE)==0)||(form.compareTo(Definitions.SIMPLE)==0))
        {
            // do IEEE
            // loop references, with som list-like table wrapping
            wrapper=mod.getDoc().createElement("table");
            //wrapper.setAttribute("cellpadding","2px");
            //wrapper.setAttribute("cellspacing","0px");
            wrapper.setAttribute("class", PIcommand.WXTSTYLEPREFIX+"ieeelist");
            for(int ix=0;ix<refs.size();ix++)
            {
                String ids=refs.elementAt(ix);
                Reference r=mod.getDefinitions().getReference(ids);
                int index=ix+1;            
                if(mod.getDefinitions().getOption(Definitions.REFERENCE_INDEXING).compareTo(Definitions.GLOBAL)==0)
                {    
                    // if we are working with global referenceorder
                    // we must find out where this ref is placed in the global referenceIndexHolder          
                    index=mod.getDefinitions().getReferenceHolder().theIndexOfReference(r,0)+1;
                }
                Element tr=mod.getDoc().createElement("tr");
                Element td1=mod.getDoc().createElement("td");
                td1.setAttribute("valign", "top");
                Element td2=mod.getDoc().createElement("td");
                Element div1Elt=mod.getDoc().createElement("div");
                div1Elt.setAttribute("class", "index");
                div1Elt.appendChild(mod.getDoc().createTextNode(""+index));
                td1.appendChild(div1Elt);
                td2.appendChild(r.produceListElement(mod,mod.getDefinitions().getOption(Definitions.REFERENCE_FORM),index));

                tr.appendChild(td1);
                tr.appendChild(td2);

                wrapper.appendChild(tr);
            }
        }
        else
        {
            wrapper=mod.getDoc().createElement("div");
            wrapper.setAttribute("class", PIcommand.WXTSTYLEPREFIX+"harvardlist");
            for(int ix=0;ix<refs.size();ix++)
            {
                String ids=refs.elementAt(ix);
                Reference r=mod.getDefinitions().getReference(ids);
                wrapper.appendChild(r.produceListElement(mod,mod.getDefinitions().getOption(Definitions.REFERENCE_FORM),ix+1));
            }           
        }
        
        // do we have anything at all ?
        if(wrapper.hasChildNodes())
            df.appendChild(wrapper);
        else
            df.appendChild(makeEmptyComment(mod.getDoc(), cmd));

        
        // reset global index?   
        if(( cmd.paramExist(PIcommand.RESET)) &&
               (cmd.getValue(PIcommand.RESET).compareTo(PIcommand.YES)==0))
            mod.getDefinitions().initUsedReferences();


        // return it
        return df;
    }

    /**
     * Produce a documentfragment which displays a sequence of references with accessstatus
     * This is web adeveloper utility
     *
     * @param mod The Module requesting the content
     * @param cmd The PIcommand describing the request
     * @return a DocumentFragment, always
     */
    public DocumentFragment makePI_ReferenceTest(Module mod, PIcommand cmd)
    {
        HashMap<String,Reference> allrefs=mod.getDefinitions().getReferences();
        DocumentFragment df = mod.getDoc().createDocumentFragment();

        Element wrapper=mod.getDoc().createElement("div");
        wrapper.setAttribute("class", cmd.getCommandStyleName());

        Set<String> keys= allrefs.keySet();
        for(String k:keys)
        {
            Reference R=allrefs.get(k);
            URI theUri=null;
            String tmp=R.getURI();
            if(tmp==null)
                continue;
            try{
                theUri=new URI(R.getURI());
            }
            catch(Exception ex)
            {
                mod.getReporter().pushMessage("bad_uri_in_reference", R.getid(), tmp);
                continue;
            }
            int result=-999;
            try{
                HttpURLConnection uc=(HttpURLConnection)theUri.toURL().openConnection();
                result=uc.getResponseCode();
            }
            catch(Exception ex)
            {
                mod.getReporter().pushMessage("bad_uri_in_reference", R.getid(), tmp);
                continue;
            }
            Element divElt=mod.getDoc().createElement("div");
            Element spanElt=mod.getDoc().createElement("span");
            if(result==200)
                spanElt.setAttribute("style","color:gray");
            else
                spanElt.setAttribute("style","color:red");
            spanElt.appendChild(mod.getDoc().createTextNode(""+result));
            divElt.appendChild(spanElt);

            divElt.appendChild(mod.getDoc().createTextNode(" : "+R.getid()+" : "));

            Element aElt=mod.getDoc().createElement("a");
            aElt.setAttribute("href", theUri.toString());
            aElt.appendChild(mod.getDoc().createTextNode(theUri.toString()));
            divElt.appendChild(aElt);

            wrapper.appendChild(divElt);
        }        
        df.appendChild(wrapper);
        return df;
    }

    
    /**
     * Produce a documentfragment with content as described in a fragment 
     * defined in the script.
     * 
     * @param mod The Module requesting the content
     * @param cmd The PIcommand describing the request
     * @return a DocumentFragment, always
     */
    public DocumentFragment makePI_Fragment(Module mod, PIcommand cmd)
    {
        // PI control:id exists

        DocumentFragment df = mod.getDoc().createDocumentFragment();

        // nothing to do if we dont have and id
        if (!cmd.paramExist(PIcommand.ID)) 
        {
            // should not happen
            df.appendChild(wxtError.makeErrorMsgForDebugging(mod.getDoc(), cmd, "missing id in producer:Fragment"));
            //df.appendChild(makeFailureComment(mod.getDoc(), cmd));
            mod.getReporter().pushMessage("no_fragment_produced_from", cmd.toString());
            return df;
        }
        // ok, we have an id
        String id = cmd.getValue(PIcommand.ID);

        // what type of display do we want
        String displayType=PIcommand.FULL; // default
        if(cmd.paramExist(PIcommand.FORM))
        {
            String tmp=cmd.getValue(PIcommand.FORM);
            if(tmp.compareTo(PIcommand.ID)==0)
                displayType=tmp;
            if(tmp.compareTo(PIcommand.SHORT)==0)
                displayType=tmp;               
        }
          

        Fragment frag = mod.getDefinitions().getFragments().get(id);
        if (frag != null) 
        {
            if(displayType.compareTo(PIcommand.ID)==0)
            {
                df.appendChild(mod.getDoc().createTextNode(frag.getId()));
                return df;            
            }
            if(displayType.compareTo(PIcommand.SHORT)==0)
            {
                df.appendChild(mod.getDoc().createTextNode(frag.getShort()));
                return df;
            }

            df = frag.getFragment();
            
            if (df != null) 
            {
                df=(DocumentFragment)df.cloneNode(true);
                correctAddressing(df, mod, frag.getReferenceUri());
                return (DocumentFragment) (mod.getDoc().importNode(df, true));
            } 
            else 
            {
                mod.getReporter().pushMessage("no_fragment_produced_from", cmd.toString());
                df.appendChild(makeFailureComment(mod.getDoc(), cmd));
                return df;
            }
        } 
        else 
        {
            // this must be a fragment based on the requesting modules attributes
            String val = "";
            if (id.compareToIgnoreCase(PIcommand.DESCRIPTION) == 0)
            {
                val = mod.getDescription();
            } 
            else if (id.compareToIgnoreCase(PIcommand.NAME) == 0)
            {
                val = mod.getName();
            } 
            else if (id.compareToIgnoreCase(PIcommand.ID) == 0)
            {
                val = mod.getID();
            } 
            else if (id.compareToIgnoreCase(PIcommand.SUMMARY) == 0)
            {
                return makeSummary(mod, cmd);
            }
            else if(id.compareToIgnoreCase(PIcommand.AUTHOR) == 0)
            {
               Fragment au=mod.getAuthor();
               if(au==null)
               {
                    // should not happen
                    df.appendChild(makeFailureComment(mod.getDoc(), cmd));
                    mod.getReporter().pushMessage("module_has_no_author", mod.getID());
                    return df;
               }
               if(displayType.compareTo(PIcommand.ID)==0)
               {
                  // id
                   df.appendChild(mod.getDoc().createTextNode(au.getId()));
                   return df;
               }
               if( (displayType.compareTo(PIcommand.SHORT)==0))
               {
                   // short
                   df.appendChild(mod.getDoc().createTextNode(au.getShort()));
                   return df;
               }
               // full
               return au.getFragment();

            }
            else 
            {
                // does not exist at all
                df.appendChild(makeFailureComment(mod.getDoc(), cmd));
                mod.getReporter().pushMessage("unknown_fragment", id);
                return df;
            }
            df.appendChild(mod.getDoc().createTextNode(val));
            return df;
        }
    }
    
    /**
     * Produce a "pop-up" like fragment
     * @param mod The Module requesting the content
     * @param cmd The PIcommand describing the request
     * @return a DocumentFragment, always
     */
    public DocumentFragment makePI_PopFragment(Module mod, PIcommand cmd)
    {
        //PI control: id exist
        DocumentFragment df=mod.getDoc().createDocumentFragment();
        // <?_wxt popfragment id text?>
        
        
        // get the fragment
        DocumentFragment theFragmentDF=null;
        String fragId=null;
        if (!cmd.paramExist(PIcommand.ID)) 
        {
            // should not happen
            df.appendChild(makeFailureComment(mod.getDoc(), cmd));
            mod.getReporter().pushMessage("no_fragment_produced_from", cmd.toString());
            df.appendChild(wxtError.makeErrorMsgForDebugging(mod.getDoc(), cmd, "missing id in producer:PopFragment"));
            //df.appendChild(mod.getDoc().createTextNode("missing id"));
            return df;
        }
        // ok, we have an id
        fragId = cmd.getValue(PIcommand.ID);
        // decide text
        String innerTxt=fragId;
        if(cmd.paramExist(PIcommand.TEXT))
            innerTxt=cmd.getValue(PIcommand.TEXT);

        Fragment frag = mod.getDefinitions().getFragments().get(fragId);
        if (frag != null)
        {
            DocumentFragment tmp = frag.getFragment();            
            if (tmp != null) 
            {
                correctAddressing(tmp, mod, mod.getScriptHandler().getScriptAbsoluteUri());
                theFragmentDF= (DocumentFragment) (mod.getDoc().importNode(tmp, true));
            } 
            else {
                mod.getReporter().pushMessage("no_fragment_produced_from", cmd.toString());
                df.appendChild(mod.getDoc().createTextNode(innerTxt));
                return df;
            }
        } 
        if(theFragmentDF==null)
        {
                mod.getReporter().pushMessage("no_fragment_produced_from", cmd.toString());
                df.appendChild(mod.getDoc().createTextNode(innerTxt));
                return df;            
        }
 
        // make a random elementid?        
        String eltId=mod.getScriptHandler().getANewId(mod.getID());
        //String eltId=cmd.getCommand()+fragId;
        
        Element spanElt=mod.getDoc().createElement("span");
        //spanElt.setAttribute("onmouseout", "changestyle('"+eltId+"','hidden');");
        //spanElt.setAttribute("onmouseover", "changestyle('"+eltId+"','visible');");
        
        spanElt.setAttribute("onmouseout","javascript:document.getElementById('"+eltId+"').style.visibility='hidden'");
        spanElt.setAttribute("onmouseover","javascript:document.getElementById('"+eltId+"').style.visibility='visible'");

        
        spanElt.setAttribute("class",cmd.getCommandStyleName());
        spanElt.appendChild(mod.getDoc().createTextNode(innerTxt));
        
        Element outerFragWrapper=mod.getDoc().createElement("div");
        outerFragWrapper.setAttribute("style", "position: absolute; z-index: 10;");
        
        Element innerFragWrapper=mod.getDoc().createElement("div");
        innerFragWrapper.setAttribute("class", PIcommand.WXTSTYLEPREFIX+"popsummary");
        innerFragWrapper.setAttribute("style", "visibility: hidden;");
        innerFragWrapper.setAttribute("id", eltId);
        
        //innerFragWrapper.appendChild(theFragmentDF);
        NodeList nl=theFragmentDF.getChildNodes();
        int tmpl=nl.getLength();
        for(int nix=0;nix<nl.getLength();nix++)
            innerFragWrapper.appendChild(nl.item(nix).cloneNode(true));
        
        outerFragWrapper.appendChild(innerFragWrapper);
        
        df.appendChild(spanElt);
        df.appendChild(outerFragWrapper);
        
        return df;

    }

    /**
     * Produce a documentfragment which is a copy of the modules summary.
     * 
     * @param mod The Module requesting the content
     * @param cmd The PIcommand describing the request
     * @return a DocumentFragment, always
     */
    public DocumentFragment makeSummary(Module mod, PIcommand cmd)
    {
        if(mod.getSummary()!=null)
        {
            DocumentFragment tmpdf = mod.getSummary().getFragment();
            if (tmpdf != null) 
            {
                return (DocumentFragment) (mod.getDoc().importNode(tmpdf, true));
            }
        }
        DocumentFragment df=mod.getDoc().createDocumentFragment();
        df.appendChild(mod.getDoc().createTextNode(mod.getDescription()));
        return df;
    }
    
    /**
     * Produce an external reference with screen and print display option.
     * 
     * @param mod The Module requesting the content
     * @param cmd The PIcommand describing the request
     * @return a DocumentFragment, always
     */
    public DocumentFragment makePI_DemoLink(Module mod, PIcommand cmd)
    {
        //PI control: uri or location exists
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        // we need url/location and may have text, style, title and target
        String uri=cmd.getValue(PIcommand.URI);
        if(uri==null)
            uri=cmd.getValue(PIcommand.LOCATION);
        if(uri==null)
        {
            // should not happen
            df.appendChild(wxtError.makeErrorMsgForDebugging(mod.getDoc(), cmd, "missing location/uri in producer:demolink"));
            //df.appendChild(makeFailureComment(mod.getDoc(), cmd));
            mod.getReporter().pushMessage("missing_URI", cmd.toString());
            return df;            
        }

        uri=mod.getDefinitions().substituteFragments(uri);

        String txt=null;
        if(cmd.paramExist(PIcommand.TEXT))
            txt=cmd.getValue(PIcommand.TEXT);

        String style="demo";
        if(cmd.paramExist(PIcommand.STYLE))
            style=cmd.getValue(PIcommand.STYLE);

        String title="external link";
        if(cmd.paramExist(PIcommand.TITLE))
            title=cmd.getValue(PIcommand.TITLE);

        String target=null;
        if(cmd.paramExist(PIcommand.TARGET))
            title=cmd.getValue(PIcommand.TARGET); 
                    
        // calculate absolute uri for the referenced page
        // uri is relative uri
        String absUriStr=uri;
        String absmodpub=mod.getAbsolutePublishAddress();
        if(absmodpub!=null)
        {
            try{absUriStr=accessutils.makeAbsoluteURI(uri, absmodpub).toString();}
            catch(Exception ex){}
        }


        Element aelt=mod.getDoc().createElement("a");
        aelt.setAttribute("href", uri);
        aelt.setAttribute("title", title);
        aelt.setAttribute("class", style);
        if(target!=null)
            aelt.setAttribute("target", target);
        
        Element t_elt=mod.getDoc().createElement("span");
        t_elt.setAttribute("class", "screen");
        if(txt!=null)
            t_elt.appendChild(mod.getDoc().createTextNode(txt));
        else
            t_elt.appendChild(mod.getDoc().createTextNode(uri));
        aelt.appendChild(t_elt);
        
        Element l_elt=mod.getDoc().createElement("span");
        l_elt.setAttribute("class", "print");
        if(txt==null)
            txt="";
        l_elt.appendChild(mod.getDoc().createTextNode(absUriStr)); //txt+": "+absUriStr
        aelt.appendChild(l_elt);
        
        df.appendChild(aelt);
        return df;
    }
            

    /**
     * Produce a documentfragment with a reference to another module in the script.
     * 
     * @param mod The Module requesting the content
     * @param cmd The PIcommand describing the request
     * @return a DocumentFragment, always
     */
    public DocumentFragment makePI_XLink(Module mod, PIcommand cmd) 
    {
        //PI control: ID exists
        //            if SUMMARY -> yes or no
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        String mid = null;
        DocumentFragment showFrag=null;
        
        if (cmd.paramExist(PIcommand.ID))
        {
            mid = cmd.getValue(PIcommand.ID);
        }
        if (mid == null) {           
            // should not happen
            df.appendChild(wxtError.makeErrorMsgForDebugging(mod.getDoc(), cmd, "missing id value i producer:XLink"));
            //df.appendChild(makeFailureComment(mod.getDoc(), cmd));
            mod.getReporter().pushMessage("missing_id", cmd.toString());
            return df;
        }
        
        Module refMod = null;
        Vector<Module>mList=mod.getScriptHandler().getAllModules(null);
        int modix=mList.indexOf(mod);


        if (mid.compareTo(PIcommand.NEXT) == 0)
        {
            refMod = null;
            int ix=modix+1;
            while((ix<mList.size())&&(!mList.elementAt(ix).getLinkable())) ix++;
            if(ix < mList.size())
                refMod=mList.elementAt(ix);

            if (refMod == null)
                refMod = mod;

        } 
        else if (mid.compareTo(PIcommand.PREV) == 0)
        {
            refMod = null;
            int ix=modix-1;
            while((ix>=0)&&(!mList.elementAt(ix).getLinkable())) ix--;
            if(ix >=0)
                refMod=mList.elementAt(ix);
            if (refMod == null) 
                refMod = mod;

        } 
        else if (mid.compareTo(PIcommand.PARENT) == 0)
        {
            if(mod.getParent()!=null)
                refMod = (Module) mod.getParent();
            else
                refMod=mod;
        } 
        else if (mid.compareTo(PIcommand.NEXTSIBLING) == 0)
        {
            refMod=mod;
            do{refMod = (Module) mod.getNextSibling();}
            while( (refMod!=null) && (!refMod.getLinkable()));
            
            if (refMod == null)
                refMod = mod;

        } 
        else if (mid.compareTo(PIcommand.PREVSIBLING) == 0)
        {
            refMod=mod;
            do{refMod = (Module) mod.getPreviousSibling();}
            while( (refMod!=null) && (!refMod.getLinkable()));

        } 
        else if(mid.compareTo(PIcommand.HOME) == 0)
        {
            refMod=mod.getScriptHandler().getFirstRootModule();
        } 
        else if(mid.compareTo(PIcommand.VROOT) == 0)
        {
            if(mod.getParent()==null)
                refMod=mod;
            else
                refMod=(Module)mod.getRoot();
        } 
        else         
            refMod = mod.getScriptHandler().getModuleById(mid);


        if (refMod == null)
        {
            df.appendChild(makeFailureComment(mod.getDoc(), cmd));
            mod.getReporter().pushMessage("cannot_find_reffered_module", cmd.toString());
            return df;
        }
        
        // can have summary and fragment
        // a summary will pop summary on mouseover
        // a fragment will replace modules name in link. The fragment may be _summary
        
        boolean bSummary=((cmd.paramExist(PIcommand.LSUMMARY) && 
                         (cmd.getValue(PIcommand.LSUMMARY).compareToIgnoreCase(PIcommand.YES)==0)));
        
        if(cmd.paramExist(PIcommand.FRAGMENT))
        {           
            String fragKey=cmd.getValue(PIcommand.FRAGMENT);
            if(fragKey.compareToIgnoreCase(PIcommand.SUMMARY)==0)
            {
                showFrag=mod.getDoc().createDocumentFragment();
                DocumentFragment tmp=refMod.getSummary().getFragment();
                showFrag.appendChild(mod.getDoc().importNode(tmp,true));
                correctAddressing(showFrag, mod, mod.getScriptHandler().getScriptAbsoluteUri());
            }
            else
            {
                if(mod.getDefinitions().getFragments().containsKey(fragKey))
                {
                    showFrag=mod.getDoc().createDocumentFragment();
                    showFrag.appendChild(mod.getDoc().importNode(mod.getDefinitions().getFragments().get(fragKey).getFragment(),true));
                    correctAddressing(showFrag, mod, mod.getScriptHandler().getScriptAbsoluteUri());
                }
                else
                {
                    mod.getReporter().pushMessage("fragment_does_not_exist", fragKey);
                }
            }
        }
        
        if(cmd.paramExist(PIcommand.LSUMMARY))
        {
             if(cmd.getValue(PIcommand.LSUMMARY).compareToIgnoreCase(PIcommand.YES)==0)
                bSummary=true;
           
        }

        Element e = makeXLink(mod, refMod, "span", cmd.getCommandStyleName(), NO_LEVEL_ADJUST,bSummary);
        if(showFrag!=null)
        {
            if(mod==refMod)
                e.replaceChild(showFrag, e.getFirstChild());
            else
                e.getFirstChild().replaceChild(showFrag, e.getFirstChild().getFirstChild());
        }
        df.appendChild(e);
        return df;
    }

    /**
     * Produce a modulemap with selected modules.
     * 
     * @param mod The module that will use the map
     * @param cmd PI describing the request
     * @return A documentfragment
     */
    public DocumentFragment makePI_ModuleMap(Module mod, PIcommand cmd)
    {
        // PI control: if SUMMARY->yes or no
        //             if SELECT -> SIBLINGS or CHILDREN;
        //             COLs is nonegative integer

        DocumentFragment df = mod.getDoc().createDocumentFragment();
        
        Vector<Module> moduleList = null;
        moduleList = getModuleList(mod, cmd);
        if(moduleList==null || moduleList.size()==0)
        {
            df.appendChild(makeFailureComment(mod.getDoc(), cmd));
            mod.getReporter().pushMessage("empty_module_selection", cmd.toString());
            df.appendChild(makeEmptyComment(mod.getDoc(), cmd));
            return df;
        }
        // how many cols
        int cols = 1;
        if (cmd.paramExist(PIcommand.COLS)) {
            cols = Math.max(cmd.getValueAsInteger(PIcommand.COLS),0);
        }
        
        // can have summary and fragment
        // a summary will pop summary on mouseover
        // a fragment will replace modules name in link. The fragment may be _summary
        
        boolean bSummary=((cmd.paramExist(PIcommand.LSUMMARY) && 
                         (cmd.getValue(PIcommand.LSUMMARY).compareToIgnoreCase(PIcommand.YES)==0)));
        
         
        // wrap links so we can display them in cols
        Vector<elementWrapper> entrylist = new Vector<elementWrapper>();
        for (Module refMod : moduleList) {
            // fragment for each since it may be summary
            DocumentFragment showFrag=null;
            if(cmd.paramExist(PIcommand.FRAGMENT))
            {           
                String fragKey=cmd.getValue(PIcommand.FRAGMENT);
                if(fragKey.compareToIgnoreCase(PIcommand.SUMMARY)==0)
                {
                    showFrag=mod.getDoc().createDocumentFragment();
                    DocumentFragment tmp=refMod.getSummary().getFragment();
                    showFrag.appendChild(mod.getDoc().importNode(tmp,true));
                    correctAddressing(showFrag, mod, mod.getScriptHandler().getScriptAbsoluteUri());
                   //bSummary=true;
                }
                else
                {
                    if(mod.getDefinitions().getFragments().containsKey(fragKey))
                    {
                        showFrag=mod.getDoc().createDocumentFragment();
                        showFrag.appendChild(mod.getDoc().importNode(mod.getDefinitions().getFragments().get(fragKey).getFragment(),true));
                        correctAddressing(showFrag, mod, mod.getScriptHandler().getScriptAbsoluteUri());
                    }
                    else
                    {
                        mod.getReporter().pushMessage("fragment_does_not_exist", fragKey);
                    }
                }
            }
            Element e;
            if(cols==0)
            {
                e = makeXLink(mod, refMod, "span", "level", 0,bSummary);
            }
            else
            {
                e = makeXLink(mod, refMod, "div", "level", 0,bSummary); 
            }
            // insert fragment for name
            if(showFrag!=null)
            {
                if(mod==refMod)
                    e.replaceChild(showFrag, e.getFirstChild());
                else
                    e.getFirstChild().replaceChild(showFrag, e.getFirstChild().getFirstChild());
            }

            // wrap it
            entrylist.add(new elementWrapper(e, refMod.getLevel()));
        }
        String divider=" | ";
        if(cmd.paramExist(PIcommand.DIVIDER))
            divider=cmd.getValue(PIcommand.DIVIDER);
        
        Element result = displayElementsInTable(mod.getDoc(), entrylist, cols,divider);
        result.setAttribute("class", cmd.getCommandStyleName());
        df.appendChild(result);
        return df;
    }


    /**
     * Produce a simple path from top to this module.
     * 
     * @param mod The module which has requested this path
     * @param cmd The PIcommand that describes the request
     * @return A DocumentFragment containg the links
     */
    public DocumentFragment makePI_Path(Module mod, PIcommand cmd)
    {
        //PI control: none
        DocumentFragment df = mod.getDoc().createDocumentFragment();

        String divider = " | ";
        if (cmd.paramExist(PIcommand.DIVIDER)) {
            divider = cmd.getValue(PIcommand.DIVIDER);
        }

        TreeNode[] plist = mod.getPath();
        if (plist.length < 2) // drop it if it is empty or only one long
        {
            df.appendChild(makeEmptyComment(mod.getDoc(),cmd));
            return df;
        }
        Element wrapper = mod.getDoc().createElement("span");
        wrapper.setAttribute("class",cmd.getCommandStyleName());
        for (int ix = 0; ix < plist.length; ix++) {
            Module m = (Module) plist[ix];
            Element e = makeXLink(mod, m, "span", "level", plist.length-2,false);
            wrapper.appendChild(e);
            wrapper.appendChild(mod.getDoc().createTextNode(divider));
        }
        wrapper.removeChild(wrapper.getLastChild());
        if(wrapper.hasChildNodes())
        {
            df.appendChild(wrapper);
            return df;
        }
        else
        {
            df.appendChild(makeEmptyComment(mod.getDoc(),cmd));
            return df;
        }
    }

    /**
     * Produce a list of all authors
     * @param mod The module that will use the map
     * @param cmd PI describing the request
     * @return A documentfragment
     */
    public DocumentFragment makePI_SimpleAuthorList(Module mod, PIcommand cmd)
    {
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        Document doc = mod.getDoc();
        // get all authors
        Fragment[] authors=mod.getDefinitions().getAllSortedFragments();

        // what type of display do we want
        String displayType=PIcommand.FULL; // default
        if(cmd.paramExist(PIcommand.FORM))
        {
            String tmp=cmd.getValue(PIcommand.FORM);
            if(tmp.compareTo(PIcommand.ID)==0)
                displayType=tmp;
            if(tmp.compareTo(PIcommand.SHORT)==0)
                displayType=tmp;               
        }
        


        Element wrapper=doc.createElement("div");
        wrapper.setAttribute("class", cmd.getCommandStyleName());
        // run each author select modules that the author has written
        Element ulelt=doc.createElement("ul");
        for (int ix=0;ix<authors.length;ix++)
        {
            Fragment au=authors[ix];
            // is this an aouthor
            if(!au.isAuthor())
                continue;
            // set up author fragment
            Element auelt=doc.createElement("li");
            if(displayType.compareTo(PIcommand.FULL)==0)
            {
                auelt.appendChild(doc.importNode(au.getFragment(),true));
            }
            else
            {
                Element divElt=doc.createElement("div");
                divElt.setAttribute("class", "authorname"+" "+PIcommand.SKIPTRANSLATE);
                if(displayType.compareTo(PIcommand.ID)==0)
                    divElt.appendChild(doc.createTextNode(au.getId()));
                else
                    divElt.appendChild(doc.createTextNode(au.getShort()));
                auelt.appendChild(divElt);
            }
            ulelt.appendChild(auelt);
        }
        wrapper.appendChild(ulelt);

        df.appendChild(wrapper);
        return df;

    }

    /**
     * Produce a list of modules for each author
     * @param mod The module that will use the map
     * @param cmd PI describing the request
     * @return A documentfragment
     */
    public DocumentFragment makePI_ModulesFromAuthor(Module mod, PIcommand cmd)
    {
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        Document doc = mod.getDoc();
        // set up the modules to choose from
        Vector<Module> resultList = null;
        resultList = getModuleList(mod, cmd);
        if(resultList.size()==0)
        {
            // no modules and we retun an empty df
            return df;
        }

        // should we drop default author
        boolean dropDefault=true;
        if(cmd.paramExist(PIcommand.DROPDEFAULT))
            dropDefault=cmd.getValue(PIcommand.DROPDEFAULT).compareToIgnoreCase(PIcommand.YES)==0;

        Element wrapper=doc.createElement("dl");
        wrapper.setAttribute("class", cmd.getCommandStyleName());

        // any filter on authors
        String[] filter=null;
        if (cmd.paramExist("author"))
            filter=cmd.getValue("authors").split(",");



        // get all authors
        Fragment[] authors=mod.getDefinitions().getAllSortedFragments();

        // run each author select modules that the author has written
        for (int ix=0;ix<authors.length;ix++)
        {
            Fragment au=authors[ix];
            if(!au.isAuthor())
                continue;
            // skip the authors that are not selected
            if(filter!=null)
            {
                // there is a selection
                boolean useit=false;
                for(int ixx=0;ixx<filter.length;ixx++)
                {
                    if(au.getId().compareTo(filter[ixx])==0)
                        useit=true;
                }
                if(!useit)
                    continue;
            }
            // if drop default is set
            if(dropDefault && (au.getId().compareTo(Definitions.DEFAULTAUTHOR)==0) )
                continue;

            // has this author written any of the selected modules
            if(!au.HasWrittenAnyOf(resultList))
                continue;

            // set up author fragment
            Element listhead=doc.createElement("dt");
            //listhead.appendChild(au.getDisplayFragment(mod, cmd));
            listhead.appendChild(doc.importNode(au.getFragment(),true));
            listhead.setAttribute("class", PIcommand.SKIPTRANSLATE);
            wrapper.appendChild(listhead);


            Element ddelt=mod.getDoc().createElement("dd");
            // then run list of potential modules
            for(Module m : resultList)
            {
                if(m.getAuthor().getId().compareTo(au.getId())==0)
                {
                    // use this module since it is written by the author
                    Element listelt=makeXLink(mod, m, "div", null, NO_LEVEL_ADJUST, true);
                    ddelt.appendChild(listelt);
                }
            }
            wrapper.appendChild(ddelt);
        }

        df.appendChild(wrapper);
        return df;
    }

    

    /**
     * Produce an in-module table of contents.
     * 
     * @param mod The module
     * @param cmd The description of the toc
     * @return A DocumentFragment
     */
    public DocumentFragment makePI_ModuleToc(Module mod, PIcommand cmd)
    {
        // PI control: COLs is a nonegtaive integer
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        Document doc = mod.getDoc();
        // set tags we want to build toc for
        // all html-headings are default
        String[] tag_list = new String[]{
            "h1", "h2", "h3", "h4", "h5", "h6"
        };
        if (cmd.paramExist(PIcommand.TAGS)) {
            tag_list = cmd.getValue(PIcommand.TAGS).split(",");
        }
        Vector<Element> elt_list = domer.getElementsPreorder(doc.getDocumentElement());
        Vector<elementWrapper> entrylist = new Vector<elementWrapper>();
        // we must do som effort to make sure that the refed names are unique
        for (Element targetElt : elt_list) {
            String eltname = targetElt.getTagName();
            int eltnamepos = accessutils.indexOfNameInList(eltname, tag_list);
            if (eltnamepos != -1)
            {
                targetElt.normalize();
                // it is in the taglist so we use it
                // we must establish an element that links to this element, and
                // we must mark this element with an anchor (name)                
                String originalrefname = targetElt.getTextContent().trim();
                // we must find out if this target already contains
                // a name tag. If this is so we reuse it.
                String refName="";
                NodeList alist=targetElt.getElementsByTagName("a");
                if(alist.getLength()>0)
                {
                    Element theFoundA=(Element)alist.item(0);
                    if(theFoundA.hasAttribute("name"))
                        refName=theFoundA.getAttribute("name");
                }
                if(refName.isEmpty())
                {
                    // we must produce it and the surronding elt
                    refName=mod.getScriptHandler().getANewId(mod.getID());
                    Element aElt = doc.createElement("a");
                    aElt.setAttribute("name", refName);
                    String content=targetElt.getTextContent();
                    aElt.appendChild(mod.getDoc().createTextNode(content));
                    // could it be that we take away to much here ?
                    while(targetElt.hasChildNodes())
                        targetElt.removeChild(targetElt.getLastChild());
                    targetElt.appendChild(aElt);
                }

                // ok we are ready for the referer
                // make the referer
                Element spanElt = doc.createElement("span");
                spanElt.setAttribute("class", "level"+ (eltnamepos + 1));
                Element refElt = doc.createElement("a");
                refElt.setAttribute("href", "#" + refName);
                refElt.appendChild(doc.createTextNode(originalrefname));
                spanElt.appendChild(refElt);

                entrylist.add(new elementWrapper(spanElt, eltnamepos + 1));
            }
        }
        // we have all the prepared tocEntries in entrylist
        int cols = 0;
        if (cmd.paramExist(PIcommand.COLS)) {
            cols = Math.max(cmd.getValueAsInteger(PIcommand.COLS),0);
        }
        if (!entrylist.isEmpty())
        {
            String divider=" | ";
            if(cmd.paramExist(PIcommand.DIVIDER))
                divider=cmd.getValue(PIcommand.DIVIDER);
            Element result = displayElementsInTable(doc, entrylist, cols,divider);
            result.setAttribute("class", cmd.getCommandStyleName());
            df.appendChild(result);        
        }
        else
            df.appendChild(makeEmptyComment(mod.getDoc(),cmd));

        return df;
    }

    /**
     * Produce an in-module table of contents.
     * <p>
     * same as makePI_ModuleToc, but is called in second phase of a build
     * All old moduletocs are ceared
     * 
     * @param mod The module
     * @param cmd The description of the toc
     * @return A DocumentFragment
     */
    public DocumentFragment makePI_ModuleTocFinal(Module mod, PIcommand cmd)
    {
        // PI control: COLs is a nonegtaive integer
        //clear old moduletocs
        NodeList divList=mod.getDoc().getElementsByTagName("*");
        for(int ix=0;ix<divList.getLength();ix++)
        {
            Element d=(Element)divList.item(ix);
            // note hardcoded classname:moduletoc
            // set in makePIModuleToc as: cmd.getCommand()
            String targetClassName="moduletoc";
            if (d.hasAttribute("class")
                    &&(d.getAttribute("class").compareTo(targetClassName)==0))
            {
                //remove the whole thing
                d.getParentNode().removeChild(d);

                // alternative is to clear only refs
                /*
                 NodeList alist=d.getElementsByTagName("a");
                for(int aix=alist.getLength()-1;aix>=0;aix--)
                {
                    Element a=(Element)alist.item(aix);
                    a.getParentNode().replaceChild(mod.getDoc().createTextNode(a.getTextContent()), a);
                }
                */
            }

        }

        // make the new
        return makePI_ModuleToc(mod, cmd);
    }

    /**
     * Produce a word for indexing.
     * 
     * @param mod The Module that ownd the word
     * @param cmd The command that describes the request
     * @return a marker, name tag, on the module if that is what we want
     */
    public DocumentFragment makePI_IXWord(Module mod, PIcommand cmd)
    {
        // PI control: WORD exist
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        // can substitue or supress the word
        // if markWord exists and markWord == _none, we leave no mark
        // if markedword exists and is not _none, we leave markword in text

        String word = " ";
        if (cmd.paramExist(PIcommand.WORD)) {
            word = cmd.getValue(PIcommand.WORD);
        }

        String markWord=word;
        if(cmd.paramExist(PIcommand.MARKWORD))
            markWord=cmd.getValue(PIcommand.MARKWORD);


        String comment="";
        if (cmd.paramExist(PIcommand.COMMENT)) {
            comment = cmd.getValue(PIcommand.COMMENT);
        }

        String wid="";
        if(markWord.compareToIgnoreCase(PIcommand.NONE)!=0)
        {
            // produce a unique id for this word
            wid=mod.getID()+word+comment.length();
            wid=wid.replace(' ', '_');
            wid=wid.replace('>', '_');
            wid=wid.replace('<', '_');
            wid=wid.replace('&', '_');
            wid=wid.replace('(', '_');
            wid=wid.replace(')', '_');
        }
        // put it into the indexholder which will pick up from cmd
        mod.getScriptHandler().getIndexHolder().insertWord(mod, cmd, wid);

        if(markWord.compareToIgnoreCase(PIcommand.NONE)!=0)
        {
            // a name holder
            Element nameElt=mod.getDoc().createElement("a");
            nameElt.setAttribute("name", wid);
            // produce the element holding the word
            Element welt = mod.getDoc().createElement("span");
            //welt.setAttribute("id", wid);

            welt.setAttribute("class", cmd.getCommandStyleName());


            welt.appendChild(mod.getDoc().createTextNode(markWord));

            nameElt.appendChild(welt);
            df.appendChild(nameElt);
        }
         return df;
    }

    /**
     * Creating an index table. This is part of a second-phase build operation
     * 
     * @param mod The Module that request the table
     * @param cmd The command that describes the table
     * @return The table as a DocumentFragment
     */
    public DocumentFragment makePI_IXTable(Module mod, PIcommand cmd)
    {
        // PI control if (COLS) it is a nonnegative number
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        // elemet production is delegated to indexholder
        Vector<Element> ixelements =
                mod.getScriptHandler().getIndexHolder().makeIxElementList(mod, cmd);

        // set columns we want in result
        int cols = 1;
        if (cmd.paramExist(PIcommand.COLS)) {
            cols = Math.max(cmd.getValueAsInteger(PIcommand.COLS),0);
        }

        // wrap elements so we can split them on columns
        Vector<elementWrapper> entrylist = new Vector<elementWrapper>();
        for (Element e : ixelements) {
            entrylist.add(new elementWrapper(e, 1));
        }
        if(entrylist.size()==0)
        {
            mod.getReporter().pushMessage("empty_index_table");
            df.appendChild(makeEmptyComment(mod.getDoc(), cmd));
            return df;
        }
        // produce the table
        String divider=" | ";
        if(cmd.paramExist(PIcommand.DIVIDER))
            divider=cmd.getValue(PIcommand.DIVIDER);
        Element result = displayElementsInTable(mod.getDoc(), entrylist, cols,divider);
        result.setAttribute("class", cmd.getCommandStyleName());
        df.appendChild(result);
        return df;
    }

    /**
     * Collect material from a set of modules
     * 
     * @param mod The Module that request the collection
     * @param cmd The PICommand that describes the list of modules and the collection
     * @return A documentFragment
     */
    public DocumentFragment makePI_Collect(Module mod, PIcommand cmd) 
    {
        // PI control : xpath exists
        DocumentFragment df = mod.getDoc().createDocumentFragment();

        // pick up a list of the modules we want to collect from
        Vector<Module> mods = getModuleList(mod, cmd);
        if (mods == null)
        {
            df.appendChild(makeEmptyComment(mod.getDoc(), cmd));
            return df;
        }
        // we have a list of modules to collect from
        // we must have an xpath to select with from all modules
        if (!cmd.paramExist(PIcommand.XPATH))
        {
            // should no happen
            df.appendChild(makeFailureComment(mod.getDoc(), cmd));
            mod.getReporter().pushMessage("missing_xpath_in_command", cmd.toString());
            return df;
        }
        String xp = cmd.getValue(PIcommand.XPATH);

        // do the collection
        Element wrapper=mod.getDoc().createElement("div");
        wrapper.setAttribute("class", cmd.getCommandStyleName());
        
        for (Module m : mods) 
        {

            // drop self
            if (m.equals(mod)) 
                continue;            

            if (m.getDoc() == null) // if not built, build it
                m.build();

            if (m.getDoc() == null) 
                mod.getReporter().pushMessage("cannot_establish_dom_in_collect", m.getName());
            else 
            {
                try {
                    NodeList nlist = domer.performXPathQuery(m.getDoc(), xp);
                    for (int ix = 0; ix < nlist.getLength(); ix++) {
                        Node nod = mod.getDoc().importNode(nlist.item(ix), true);
                        if (nod.getNodeType() == Node.ELEMENT_NODE) {
                            correctAddressing((Element) nod, mod, m.getAbsoluteUri());
                        }
                        wrapper.appendChild(nod);
                    }
                } 
                catch (Exception e) 
                {
                    mod.getReporter().pushMessage("cannot_collect_from", mod.getName());
                }
            }
        }
        if(!wrapper.hasChildNodes())
            wrapper.appendChild(makeEmptyComment(mod.getDoc(),cmd));
        df.appendChild(wrapper);
        return df;
    }
    
    /**
     * Collect material from a set of remote modules
     * 
     * @param mod The Module that request the collection
     * @param cmd The PICommand that describes the list of modules and the collection
     * @return A documentFragment
     */
    public DocumentFragment makePI_CollectRemote(Module mod, PIcommand cmd) 
    {
        // PI control : location exists
        //              one or more of SCRIPTPATH,IDLIST,BOOKS
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        
        // location, already checked and reported
        String scriptLocation=cmd.getValue(PIcommand.LOCATION);
        if(scriptLocation==null)
        {
            //should not happen
            df.appendChild(wxtError.makeErrorMsgForDebugging(mod.getDoc(), cmd, "missing script location in producer:CollectRemote"));
            //df.appendChild(makeFailureComment(mod.getDoc(), cmd));
            return df;
        }
        // xpath, already checked and reported
        String theExpath=cmd.getValue(PIcommand.XPATH);
        if(theExpath==null)
        {
            //should not happen
            df.appendChild(wxtError.makeErrorMsgForDebugging(mod.getDoc(), cmd, "missing xpath in producer:CollectRemote"));
            //df.appendChild(makeFailureComment(mod.getDoc(), cmd));
            return df;
        }

        
        // encoding
        String theEncoding=mod.getEncoding();
        if(cmd.paramExist(PIcommand.ENCODING))
            theEncoding=cmd.getValue(PIcommand.ENCODING);
               
        
        // establish remote site holder
        RemoteSite theSite=null;
        try{ theSite=new RemoteSite(scriptLocation);
        }
        catch (Exception e2){
           mod.getReporter().pushSimpleMessage("\t"+e2.getMessage()+" in: "+ scriptLocation);
           df.appendChild(makeFailureComment(mod.getDoc(), cmd));
           return df;
        }
        // should we correct encoding according to
        // the default encoding in the remote script
        if(theSite.getEncoding()!=null)
            theEncoding=theSite.getEncoding();

        // select modules according to command attributes
        Vector<Element> actualModules=theSite.getSelectedModuleList(cmd);
        if(actualModules.size()==0)
        {
            mod.getReporter().pushSimpleMessage("no modules selected");
            df.appendChild(makeEmptyComment(mod.getDoc(), cmd));
            return df;
        }

        // a wrapper to hold and move what we select
        Element wrapper=mod.getDoc().createElement("div");

        // loop modules, now selected and in correct order
        for(int wix=0;wix<actualModules.size();wix++)
        {
            Element e=actualModules.elementAt(wix);
            URI theURI=null;
            // the URI for the remote module we want to import from
            Document remoteDoc=null;
            try{
                theURI=new URI(e.getAttribute(PIcommand.LOCATION));
            }
            catch(Exception uriex){
               continue;
            }
            try{
                remoteDoc=domer.makeDomFromUriSomeHow(theURI,theEncoding,mod.getReporter());
            }
            catch(Exception domex3)
            {
                    mod.getReporter().pushSimpleMessage("\tcannot_parse: "+ theURI.toString());
                    continue;
            }
            
            //---------------
            // we have the DOM, this is where we should do the job of correcting
            // a href's so we only have absolutes, alive or dead depending of
            // wrapper, like demolink, xlink, ref etc
            // and images ??
            theSite.reAddress(remoteDoc,e.getAttribute("location"));

            
            //----------------
            // ok we go for an import with the given expath
            try{
                NodeList reslist=domer.performXPathQuery(remoteDoc, theExpath);
                // add the result to the wrapper
                for(int rix=0;rix<reslist.getLength();rix++)
                    wrapper.appendChild(mod.getDoc().importNode(reslist.item(rix), true));
            }
            catch(Exception expex)
            {
                mod.getReporter().pushSimpleMessage("\tcannot_access: "+ theExpath);
            }
        }
        // add wrapper to df
        if(wrapper.hasChildNodes())
            df.appendChild(wrapper);
        else
            df.appendChild(makeEmptyComment(mod.getDoc(),cmd));
        return df;
    }
        

    /**
     * Produce a list of summaries for selected modules
     * 
     * @param mod The Module that request the popup
     * @param cmd The PICommand that describes the list of modules and the collection
     * @return A documentFragment
     */
    public DocumentFragment makePI_CollectSummaries(Module mod, PIcommand cmd)
    {
        //PI control:if xlink->yes or no
        //           if select->siblings or children
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        // pick up a list of the modules we want to collect from
        Vector<Module> mods = getModuleList(mod, cmd);
        if (mods == null)
        {
            df.appendChild(makeEmptyComment(mod.getDoc(), cmd));
            mod.getReporter().pushMessage("empty_module_selection", cmd.toString());
            return df;
        }
        // we have a list of modules to collect summaries from
        // do we want to include a link to the module:
        boolean makeLink=(cmd.paramExist(PIcommand.XLINK) &&
                         (cmd.getValue(PIcommand.XLINK).compareToIgnoreCase(PIcommand.YES)==0));
        for (Module m : mods) 
        {
            // drop self ?
            Element elt = mod.getDoc().createElement("div");
            elt.setAttribute("class", cmd.getCommandStyleName());

            Element heading=null;
            if(!makeLink)
            {
                heading = mod.getDoc().createElement("div");
                heading.appendChild(mod.getDoc().createTextNode(m.getName()));               
            }
            else
            {
                heading = makeXLink(mod, m, "div", "summarylink", NO_LEVEL_ADJUST,false);
            }
            heading.setAttribute("class", cmd.getCommandStyleName()+"heading");

            Element content = mod.getDoc().createElement("div");
            content.setAttribute("class", cmd.getCommandStyleName()+"content");
            
            try {
                DocumentFragment tmp=m.getSummary().getFragment();
                DocumentFragment mdf = (DocumentFragment)mod.getDoc().importNode(tmp, true);
                correctAddressing(mdf, mod, m.getAbsoluteUri());
                content.appendChild(mdf);
                
            }            
            catch (Exception e) {
                content.appendChild(mod.getDoc().createTextNode(reporter.getBundleString("cannot_make", cmd.getCommand())));
            }
            elt.appendChild(heading);
            elt.appendChild(content);

            df.appendChild(elt.cloneNode(true));
        }
        if(!df.hasChildNodes())
            df.appendChild(makeEmptyComment(mod.getDoc(),cmd));
        return df;
    }


    /**
     * Produce a span-element with the text wxt.
     * 
     * @param mod The module that wants this
     * @param cmd The PICommand that describes the list of modules and the collection
     * @return A documentFragment
     */
    public DocumentFragment makePI_Stamp(Module mod, PIcommand cmd)
    {
        //PI control: none
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        //TODO: add link and possible logo
        Element spanElt = mod.getDoc().createElement("span");
        spanElt.setAttribute("class", cmd.getCommandStyleName());
        Element aelt=mod.getDoc().createElement("a");
        aelt.setAttribute("href", "http://www.it.hiof.no/wxt/wxtsite/");
        aelt.setAttribute("class", "external");
        aelt.appendChild(mod.getDoc().createTextNode("WXT"));
        spanElt.appendChild(aelt);
        df.appendChild(spanElt);
        return df;

    }

    /**
     * Make a date - stamp
     * 
     * @param mod The Module that request the collection
     * @param cmd The PICommand that describes the list of modules and the collection
     * @return A documentFragment
     */
    public DocumentFragment makePI_DateStamp(Module mod, PIcommand cmd) 
    {
        //PI control:if FORM ->SHORT or MEDIUM or LONG orFULL
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        // defaults
        Locale loc = m_Current_Locale;
        String form = "MEDIUM";
        if (cmd.paramExist(PIcommand.COUNTRY)) {
            loc = new Locale(cmd.getValue(PIcommand.COUNTRY));
            if (loc == null) {
                loc = m_Current_Locale;
            }
        }
        if (cmd.paramExist(PIcommand.FORM)) {
            form = cmd.getValue(PIcommand.FORM);        
        }
        // default is todays date
        Date dat = new Date(System.currentTimeMillis());

        // an other date ?
        if (cmd.paramExist(PIcommand.ACTUALDATE)) 
        {
            String dt = cmd.getValue(PIcommand.ACTUALDATE);
            dt.replace('-', ':');
            dt.replace(',', ':');
            dt.replace('|', ':');
            String[] parts = dt.split(":");
            try {
                GregorianCalendar gc = new GregorianCalendar();
                gc.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
                dat = new Date(gc.getTimeInMillis());
            } catch (Exception e) {
                mod.getReporter().pushMessage("bad_date", cmd.toString());
                df.appendChild(makeFailureComment(mod.getDoc(), cmd));
                return df;
            }
        }

        DateFormat datform;
        if (form.compareToIgnoreCase("SHORT") == 0) {
            datform = DateFormat.getDateInstance(DateFormat.SHORT, loc);
        } else if (form.compareToIgnoreCase("LONG") == 0) {
            datform = DateFormat.getDateInstance(DateFormat.LONG, loc);
        } else if (form.compareToIgnoreCase("FULL") == 0) {
            datform = DateFormat.getDateInstance(DateFormat.FULL, loc);
        } else {
            datform = DateFormat.getDateInstance(DateFormat.MEDIUM, loc);
        }
        Element spanElt = mod.getDoc().createElement("span");
        spanElt.setAttribute("class", cmd.getCommandStyleName());
        spanElt.appendChild(mod.getDoc().createTextNode(datform.format(dat)));
        df.appendChild(spanElt);
        return df;

    }

    /**
     * Make a time - stamp
     * 
     * @param mod The Module that request the collection
     * @param cmd The PICommand that describes the list of modules and the collection
     * @return A documentFragment
     */
    public DocumentFragment makePI_TimeStamp(Module mod, PIcommand cmd) 
    {
        // PI control:if FORM-> SHORT or MEDIUM or LONG or FULL
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        // defaults
        Locale loc = m_Current_Locale;
        String form = "MEDIUM";
        if (cmd.paramExist(PIcommand.COUNTRY)) 
        {
            loc = new Locale(cmd.getValue(PIcommand.COUNTRY));
            if (loc == null) {
                loc = m_Current_Locale;
            }
        }
        if (cmd.paramExist(PIcommand.FORM)) 
            form = cmd.getValue(PIcommand.FORM);        

        // now
        Date dat = new Date(System.currentTimeMillis());

        DateFormat timeform;
        if (form.equals("SHORT")) {
            timeform = DateFormat.getTimeInstance(DateFormat.SHORT, loc);
        } else if (form.equals("LONG")) {
            timeform = DateFormat.getTimeInstance(DateFormat.LONG, loc);
        } else if (form.equals("FULL")) {
            timeform = DateFormat.getTimeInstance(DateFormat.FULL, loc);
        } else {
            timeform = DateFormat.getTimeInstance(DateFormat.MEDIUM, loc);
        }
        Element spanElt = mod.getDoc().createElement("span");
        spanElt.setAttribute("class", cmd.getCommandStyleName());
        spanElt.appendChild(mod.getDoc().createTextNode(timeform.format(dat)));
        df.appendChild(spanElt);
        return df;
    }
    


    /**
     * Produce a static module menu for a certain module,
     * typically left columns menu.
     * 
     * @param mod The module requesting the menu
     * @param cmd The PIcommand describing the request
     * @return A Documentfragment with the menu
     */
    public DocumentFragment makePI_ModuleMenu(Module mod, PIcommand cmd)
    {
        // PI control: if SUMMARY->yes or no
        //             if SELECT -> SIBLINGS or CHILDREN;
        //             COLs is nonegative integer

        DocumentFragment df = mod.getDoc().createDocumentFragment();
        Vector<Module> resultList = null;
        resultList = getModuleList(mod, cmd);
        Module root = null;
        if (cmd.paramExist(PIcommand.ROOT)) {
            root = mod.getScriptHandler().getModuleById(cmd.getValue(PIcommand.ROOT));
        }
        if (resultList == null)
        {
            df.appendChild(makeFailureComment(mod.getDoc(), cmd));
            mod.getReporter().pushMessage("empty_module_selection", cmd.toString());
            return df;
        }
        if (resultList.size()==0)
        {
            df.appendChild(makeEmptyComment(mod.getDoc(), cmd));
            mod.getReporter().pushMessage("empty_module_selection", cmd.toString());
            return df;
        }
        
        int cols = 1;
        if (cmd.paramExist(PIcommand.COLS)) {
            cols = Math.max(cmd.getValueAsInteger(PIcommand.COLS),0);
        }
        String basicElt="div";
        // make sure we get a span if we have a line
        if(cols==0)
            basicElt="span";

        boolean bSummary=((cmd.paramExist(PIcommand.LSUMMARY) &&
                         (cmd.getValue(PIcommand.LSUMMARY).compareToIgnoreCase(PIcommand.YES)==0)));

        // reduce it, that is leave only ancestors, siblings, ancestors siblings, children and roots
        Vector<elementWrapper> entrylist = new Vector<elementWrapper>();
        for (Module m : resultList) {
            // this is the crucial part
            if ((mod.isNodeAncestor(m)) ||
                (mod.isNodeSibling(m)) ||
                ((mod.getParent()!=null)&&(m.isNodeSibling(mod.getParent())))|| //??
                (mod.isNodeChild(m)) ||
                (mod.getScriptHandler().getRootModules().contains(m)))
            {
                // use it
                Element e;
                // make css class relative to root
                if (root != null) {
                    e = makeXLink(mod, m, basicElt, "level", root.getDepth(),bSummary);
                } else {
                    e = makeXLink(mod, m, basicElt, "level", 0,bSummary);
                }
                
                
                entrylist.add(new elementWrapper(e, m.getLevel()));
            }
        }

        String divider=" | ";
        if(cmd.paramExist(PIcommand.DIVIDER))
            divider=cmd.getValue(PIcommand.DIVIDER);
        Element result = displayElementsInTable(mod.getDoc(), entrylist, cols,divider);
        result.setAttribute("class", cmd.getCommandStyleName());
        df.appendChild(result);

        return df;
    }

    /**
     * Produce a fragment resulting from a database query
     * 
     * @param mod The module that request the material
     * @param cmd The PIcommand describing the request
     * @return a documentfragment
     */
    public DocumentFragment makePI_DBContent(Module mod, PIcommand cmd) {
        //PI control: connect and (sql or sqlfile)
        //            if parse it is yes or no

        DocumentFragment df=mod.getDoc().createDocumentFragment();
        
        //--------- reuse content if possible ------------
        if(!cmd.paramExist(PIcommand.CONNECT))
        {
            // should not happen
            mod.getReporter().pushMessage("bad_processing_instruction", "Missing " + PIcommand.CONNECT);
            df.appendChild(wxtError.makeErrorMsgForDebugging(mod.getDoc(), cmd,"missing location in Producer:XMLContent"));
            return df;
        }
        String constring=cmd.getValue(PIcommand.CONNECT);

        Content theContent=mod.getRegisteredContent(constring);
        if(theContent==null)
        {
            try{
                theContent=new DBContent(mod,cmd);
                mod.registerContent(constring, theContent);
            }
            catch(Exception ex)
            {
                mod.getReporter().pushMessage("bad_processing_instruction",cmd.toString(), ex.getMessage());
                df.appendChild(makeFailureComment(mod.getDoc(), cmd));
                return df;
            }
            
        }
        df=theContent.getContent(mod, cmd);
       //---------------------------------
              

        /*--------------- no reuse------------
        DBContent db=null;
        try{
            db=new DBContent(mod, cmd);
        }
        catch(Exception ex)
        {
            mod.getReporter().pushMessage(ex.getMessage(),cmd.toString());
            return df;
        }
        df=db.getContent(mod, cmd);
        if(!df.hasChildNodes())
            df.appendChild(makeEmptyComment(mod.getDoc(), cmd));
        ------------------------------*/
        return df;

    }

     /**
     * Produce a footnote fragment by scanning the DOM for span class fn
     * 
     * @param mod The module that request the material
     * @param cmd The PIcommand describing the request
     * @return a documentfragment
     */
    public DocumentFragment makePI_FootNote(Module mod, PIcommand cmd) 
    {
        // PI control: if FORM->(NORMAL or SHOW or REMOVE)
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        // do we simply want to remove all traces of footnotes
        
        String form=PIcommand.NORMAL;
        if( cmd.paramExist(PIcommand.FORM))
            form=cmd.getValue(PIcommand.FORM).toLowerCase();
        // normal | show | remove
        

        // collecting footnotes
        Vector<String>foots=new Vector<String>();
        // scan for actual elements: spans
        NodeList spanlist=mod.getDoc().getElementsByTagName("span");
        // run the list and look for spans with class=fn
        int counter=1;
        for(int ix=0;ix<spanlist.getLength();ix++)
        {
            Element spElt=(Element)spanlist.item(ix);
            if((spElt.hasAttribute("class")) && (spElt.getAttribute("class").compareToIgnoreCase("fn")==0))
            {
                if(form.compareToIgnoreCase(PIcommand.REMOVE)==0)
                {
                    // insert an empty task comment
                    Comment mark=mod.getDoc().createComment("fn-removed");
                    spElt.getParentNode().replaceChild(mark, spElt);
                }
                else if(form.compareToIgnoreCase(PIcommand.SHOW)==0)
                {
                    // display as a neutral span
                    Element marker=mod.getDoc().createElement("span");
                    marker.appendChild(mod.getDoc().createTextNode("( "+spElt.getTextContent()+" )"));
                    spElt.getParentNode().replaceChild(marker, spElt);
                }
                else // normal
                {
                    // assume the content string
                    String tmp=spElt.getTextContent();
                    foots.add(tmp);

                    // mark the footnote with a number
                    Element marker=mod.getDoc().createElement("span");
                    marker.setAttribute("class", PIcommand.WXTSTYLEPREFIX+"fnmarker");
                    marker.appendChild(mod.getDoc().createTextNode(String.valueOf(counter)));
                    spElt.getParentNode().insertBefore(marker, spElt);
                    ix++;

                    counter++;
                }
             }
        }
        // we have the list of footnote fragments in foots
        // we produce the documentfragment
        
         
        Element wrapper=mod.getDoc().createElement("div");
        wrapper.setAttribute("class", cmd.getCommandStyleName());
        
        Element listElt=mod.getDoc().createElement("ol");
        for(String t:foots)
        {
            Element oneFoot=mod.getDoc().createElement("li");
            oneFoot.appendChild(mod.getDoc().createTextNode(t));
            listElt.appendChild(oneFoot);
        }
        if(!listElt.hasChildNodes())
        {
            df.appendChild(makeEmptyComment(mod.getDoc(),cmd));
            return df;
        }
        wrapper.appendChild(listElt);
        df.appendChild(wrapper);
        return df;
    }
 
     /**
     * Fetch all content from nnnContent elements named in the script as children of a module.
     * realize the location-less and connect-less PIs by menas of the modules
     * content elements.
     * Modify cmd and pass on as "normal" import
     *
     * @param mod The module which owns the content and ordered this
     * @param cmd The PI involved
     * @return The resulting nodecollection in a DocumentFragment
     */
    public  DocumentFragment import_ContentToTemplate(Module mod, PIcommand cmd,String contentType)
    {
        DocumentFragment df=mod.getDoc().createDocumentFragment();
        Vector<ScriptContent> list = mod.getScriptContent();
        String cmdId=null;
        if(cmd.paramExist(PIcommand.ID))
            cmdId=cmd.getValue(PIcommand.ID);
        for (ScriptContent cont : list)
        {
            // we are looking for a certain content type
            if((cont.getType()).compareTo(contentType)!=0)
                continue;
            // and we filter by id, if it exists
            if( (cmdId==null) || (cmdId.compareTo(cont.getId())==0))
            {
                // use it
                if(contentType.compareTo(Module.DBCONTENT)==0)
                {
                    // set connectionstring as found in script
                    cmd.setParameter(PIcommand.CONNECT, cont.getConnectionString());
                }
                else
                {   // all but db
                    // set LOCATION as found in script
                    URI loc=cont.getAbsoluteEffectiveUri();
                    if(loc==null)
                    {
                        mod.getReporter().pushSimpleMessage("dropping: "+cmd.toString());
                        continue;
                    }
                    cmd.setParameter(PIcommand.LOCATION,loc.toString());
                }
                // Transformation in PI override transformation in script
                if((cont.getTransformationString()!=null) && (!cmd.paramExist(PIcommand.TRANSFORMATION)))
                    cmd.setParameter(PIcommand.TRANSFORMATION, cont.getTransformationString());

                // perform
                // mark this so we reckognize it in makePI_???Content
                cmd.setParameter(PIcommand.TEMPLATE, "ison");
                DocumentFragment tmpdf=null;
                if(contentType.compareTo(Module.TXTCONTENT)==0)
                    tmpdf=makePI_TXTContent(mod, cmd);
                else if(contentType.compareTo(Module.XMLCONTENT)==0)
                    tmpdf=makePI_XMLContent(mod, cmd); 
                else if(contentType.compareTo(Module.WIKICONTENT)==0)
                    tmpdf=makePI_WIKIContent(mod, cmd);                
                else if(contentType.compareTo(Module.ODFCONTENT)==0)
                    tmpdf=makePI_ODFContent(mod, cmd);                
                else if(contentType.compareTo(Module.DBCONTENT)==0)
                    tmpdf=makePI_DBContent(mod, cmd);                
                // no readdressing

                //collect
                if(tmpdf!=null)
                {
                    df.appendChild((DocumentFragment)mod.getDoc().importNode(tmpdf, true));
                }
            }
         }

        return df;
    }


}

package content;

import fragments.Fragment;
import references.SimpleReference;
import wimages.Image;
import Formulas.LaTexFormula;
import Formulas.ImageFormula;
import Formulas.Formula;
import Formulas.MathMLFormula;
import Formulas.GoogleTexFormula;
import fragments.AuthorFragment;
import indexing.IndexItem;
import indexing.indexable;
import java.net.HttpURLConnection;
import java.net.URI;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.swing.tree.TreeNode;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.PIcommand;
import utils.accessutils;
import programcode.CodeBasics;
import programcode.CodeFormatter;
import programcode.CodeGoogleFormatter;
import utils.domer;
import reporting.reporter;
import reporting.wxtError;
import words.Word;

/**
 * Produce content based on PI's
 * All PI-production is done under control of this class
 * 
 */
public class Producer {

    /**the Locale for this Producer*/
    Locale m_Current_Locale = new Locale(System.getProperty("user.language"));

    /** signalling that we dont want to adjust level on CSS-class in maxeXLink*/
    //public static final int NO_LEVEL_ADJUST = -9999;
    public Producer() {
    }

    /** 
     * Set locale.
     * @param lang String defining the locale
     */
    public void setLocale(String lang) {
        m_Current_Locale = new Locale(lang);
    }

    /**
     * Produce a html-comment that identifies an omitted PI production.
     * @param doc The DOM of the Module requesting the PI
     * @param cmd The PI
     * @return A comment element describing what we did not do
     */
    private Comment makeFailureComment(Module mod, PIcommand cmd) {
        Comment cmt = mod.getDoc().createComment(cmd.toString() + " is not produced");
        wxtError.reportNotice(mod, cmd.toString() + " is not produced");
        return cmt;
    }

    /**
     * Produce a html-comment stating an empty, not error, comment
     * This will prevent collapse of doc-structure and error on CSS
     * @param doc The Document that will own the Element produced
     * @param C The commandstring
     * @return A documentfragment with a 1 space text childs
     */
    private Comment makeEmptyComment(Module mod, PIcommand cmd) {
        Comment cmt = null;
        if ((cmd.paramExist(PIcommand.REMOVEPARENT))
                && (cmd.getValue(PIcommand.REMOVEPARENT).compareTo(PIcommand.YES) == 0)) {
            cmt = mod.getDoc().createComment(" " + PIcommand.REMOVEPARENT + " empty: " + cmd.toString() + " ");
        } else {
            cmt = mod.getDoc().createComment(" empty: " + cmd.toString() + " ");
        }

        wxtError.reportNotice(mod, " empty: " + cmd.toString());
        return cmt;
    }

    /**
     * Produce a toc or other element set in one row or as columns.
     * Columns is applied by CSS in th eusing instance. This 
     * method only produce a list
     * @param doc The document that will own the result
     * @param entrylist The list of entries
     * @param columns The wanted number of columns
     * @param divider A string separating entries in a horizontal list (columss=0)
     * @return An Element with the produced content
     */
    private static Element displayElementsInColumns(Document doc, List<Element> entrylist,
            int columns, String divider) {
        Element wrapElt;
        if (columns == 0) {
            // produce a horizontal list, a span
            wrapElt = doc.createElement("span");
            for (Element tc : entrylist) {
                wrapElt.appendChild(tc);
                // separator: | or blank or what
                wrapElt.appendChild(doc.createTextNode(divider));
            }
            // remove the last divider
            wrapElt.removeChild(wrapElt.getLastChild());
            return wrapElt;
        }

        // wrap all in a div
        wrapElt = doc.createElement("div");

        for (Element tc : entrylist) {
            if (tc.getNodeName().compareToIgnoreCase("span") == 0) {
                Element wrap = doc.createElement("div");
                wrap.appendChild(tc);
                wrapElt.appendChild(wrap);
            } else {
                wrapElt.appendChild(tc);
            }
        }
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
    public static void correctAddressing(Element topElement, Module target, URI sourceURI) {
        // Assume that addressing in this fragment is done according to sourceURI
        // Should be changed so addressing match target
        // Candidates for changing are:
        // PIs: importxml, importtxt, importdb, xlink, and what more
        // And in general: a href, script src etc. as defined in Definitions.getAddressMap()

        // add an artificial topelt since the topelt itself may be candidate 
        // for changes ?

        //------------------------
        // do PI's
        // all potential addresses must be adjusted
        List<Node> nlist = domer.getPIs(topElement, "_wxt");
        // target uri
        URI targetUri = target.getAbsoluteUri();
        // the params that are candidates for corrections
        List<String> params = new ArrayList<String>();
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
                    if (T.indexOf(param) != -1) {
                        PIcommand cmd = null;
                        try {
                            cmd = new PIcommand(T, target.getDefinitions());
                        } catch (Exception cex) {
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
                                && (s.compareToIgnoreCase(PIcommand.CONTENT) != 0)
                                && (s.compareToIgnoreCase(PIcommand.YES) != 0)
                                && (s.compareToIgnoreCase(PIcommand.NO) != 0)) {
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
            } catch (Exception e) {
                target.getReporter().pushMessage("could_not_readdress", e.getMessage());
            }
        }

        //--------------------------
        // do all general addressing as defined in Definitons.getAddressing
        // This means: correct all href-attributes in all a-tags, src-attributes in img, etc
        HashMap<String, String> adr = target.getDefinitions().getAddressMap();
        Set<String> keys = adr.keySet();
        for (Iterator<String> k = keys.iterator(); k.hasNext();) {
            String tag = k.next();
            String att = adr.get(tag);

            NodeList nodlist = topElement.getElementsByTagName(tag);

            for (int ix = 0; ix < nodlist.getLength(); ix++) {
                Element thisElt = (Element) nodlist.item(ix);
                if (!thisElt.hasAttribute(att)) {
                    continue;
                }
                try {
                    String s = thisElt.getAttribute(att).trim();
                    if (s.length() > 0) {
                        // allow pathfragments even here 
                        s = target.getDefinitions().substituteFragments(s);

                        if (sourceURI != null) {
                            String querypart = null;
                            String sourcecat = accessutils.removeFilePart(sourceURI).toString();
                            if (s.trim().startsWith("http")) {
                                // absolute, do we have a query-part ?
                                int pos = s.indexOf("?");
                                if (pos != -1) {
                                    querypart = s.substring(pos);
                                    s = s.substring(0, pos);

                                }
                            }
                            URI absUri = accessutils.makeAbsoluteURI(s, sourcecat);
                            URI relUri = accessutils.makeRelativeURI(targetUri, absUri);
                            if (querypart != null) {
                                thisElt.setAttribute(att, relUri.toString() + querypart);
                            } else {
                                thisElt.setAttribute(att, relUri.toString());
                            }
                        }

                    }
                } catch (Exception e) {
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
    public static void correctAddressing(DocumentFragment fragment, Module target, URI sourceURI) {
        // we will wrap the fragments children in a temporary element
        Element wrapper = fragment.getOwnerDocument().createElement("div");
        NodeList list = fragment.getChildNodes();
        for (int ix = 0; ix < list.getLength(); ix++) {
            wrapper.appendChild(list.item(ix).cloneNode(true));
        }
        correctAddressing(wrapper, target, sourceURI);


        // take them back ??
        list = wrapper.getChildNodes();
        NodeList children = fragment.getChildNodes();
        for (int ix = children.getLength() - 1; ix > -1; ix--) {
            fragment.removeChild(children.item(ix));
        }
        for (int ix = 0; ix < list.getLength(); ix++) {
            fragment.appendChild(list.item(ix).cloneNode(true));
        }
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
    static List<Module> getModuleList(Module mod, PIcommand cmd) {
        List<Module> resultList;
        // we start with IDLIST
        // IDLIST will overrun all other selections, will define containment and sequence
        if (cmd.paramExist(PIcommand.IDLIST)) {
            String ids = cmd.getValue(PIcommand.IDLIST);
            if (ids != null) {
                resultList = mod.getScriptHandler().getModuleListById(ids.split(","), true);
                if (resultList.isEmpty()) {
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
        if (cmd.paramExist(PIcommand.ROOT)) {
            resultList = mod.getScriptHandler().getAllModules(cmd.getValue(PIcommand.ROOT));
            if ((resultList == null) || (resultList.isEmpty())) {
                mod.getReporter().pushMessage("empty_module_selection", cmd.toString());
                return null;
            }
        } else {
            // we start with all
            resultList = mod.getScriptHandler().getAllModules(null);
        }
        // then we reduce according to params in cmd
        // books,select( _sibling, _children), xpath(?)
        // the selection is done by AND-ing the criteria,
        // which means the common subset, the intersection

        // we try an xpath in script first
        if (cmd.paramExist(PIcommand.SCRIPTPATH)) {
            String xp = cmd.getValue(PIcommand.SCRIPTPATH);
            if (xp != null) {
                resultList = mod.getScriptHandler().getModuleListByXpath(xp);
                if (resultList.isEmpty()) {
                    mod.getReporter().pushMessage("empty_module_selection", cmd.toString());
                    return null;
                }
            } else {
                mod.getReporter().pushMessage("empty_module_selection", cmd.toString());
                return null;
            }
        }


        // books 
        if (cmd.paramExist(PIcommand.BOOKS)) {
            String[] bks = cmd.getValue(PIcommand.BOOKS).split(",");
            List<Module> resMods = mod.getScriptHandler().getModulesInBook(bks);
            int ix = 0;
            while (ix < resultList.size()) {
                if (!resMods.contains(resultList.get(ix))) {
                    resultList.remove(ix);
                } else {
                    ix++;
                }
            }
            // and we go for the intersection
            ix = 0;
            while (ix < resultList.size()) {
                if (!resMods.contains(resultList.get(ix))) {
                    resultList.remove(ix);
                } else {
                    ix++;
                }
            }

        }
        // now we have the modules in a certain book ordered preorder

        // we turn to siblings or children
        if (cmd.paramExist(PIcommand.SELECT)) {
            // we expect siblings or children
            String selection = cmd.getValue(PIcommand.SELECT);
            List<Module> resMods = new ArrayList<Module>();
            if (selection.compareTo(PIcommand.SIBLINGS) == 0) {
                Module parMod = (Module) mod.getParent();
                if (parMod == null) {
                    resMods = mod.getScriptHandler().getRootModules();
                } else {
                    @SuppressWarnings("unchecked")
                    Enumeration<Module> modenum = parMod.children();
                    while (modenum.hasMoreElements()) {
                        Module m = modenum.nextElement();
                        resMods.add(m);
                    }
                }
            } else if (selection.compareTo(PIcommand.CHILDREN) == 0) {
                @SuppressWarnings("unchecked")
                Enumeration<Module> modenum = mod.children();
                while (modenum.hasMoreElements()) {
                    Module m = modenum.nextElement();
                    resMods.add(m);
                }
            } else {
                mod.getReporter().pushMessage("bad_processing_instruction", cmd.toString(), " ");
                return null;
            }
            // and we go for the intersection
            int ix = 0;
            while (ix < resultList.size()) {
                if (!resMods.contains(resultList.get(ix))) {
                    resultList.remove(ix);
                } else {
                    ix++;
                }
            }
        }

        // then we remove those who should _never be seen
        int ix = 0;
        while (ix < resultList.size()) {
            if (!resultList.get(ix).getLinkable()) {
                resultList.remove(ix);
            } else {
                ix++;
            }
        }

        // now we have ANDed SCRIPTPATH BOOKS and SELECT and they are all in preorder
        if (resultList.isEmpty()) {
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
    public DocumentFragment makePI_XMLContent(Module mod, PIcommand cmd) {
        //PI control:xpath exists

        DocumentFragment df = mod.getDoc().createDocumentFragment();
        URI found_uri = null;
        if (!cmd.paramExist(PIcommand.LOCATION)) {
            // should not happen
            mod.getReporter().pushMessage("bad_processing_instruction", "Missing " + PIcommand.LOCATION);
            df.appendChild(wxtError.makeErrorMsgForDebugging(mod, cmd, "missing location in Producer:XMLContent"));
            return df;
        }

        try {
            found_uri = accessutils.makeUri(cmd.getValue(PIcommand.LOCATION));
        } catch (Exception e) {
            mod.getReporter().pushMessage("bad_processing_instruction", cmd.toString(), e.getMessage());
            df.appendChild(makeFailureComment(mod, cmd));
            return df;
        }

        //--------- reuse content if possible ------------
        Content theContent = mod.getRegisteredContent(found_uri.toString());
        if (theContent == null) {
            try {
                theContent = new XMLContent(mod, cmd);
                mod.registerContent(found_uri.toString(), theContent);
            } catch (Exception ex) {
                mod.getReporter().pushMessage("bad_processing_instruction", cmd.toString(), ex.getMessage());
                df.appendChild(makeFailureComment(mod, cmd));
                return df;
            }
        }
        df = theContent.getContent(mod, cmd);

        if (!df.hasChildNodes()) {
            // report only when not from template
            if (!cmd.paramExist(PIcommand.TEMPLATE)) {
                mod.getReporter().pushMessage("empty_xml_selection", cmd.toString());
            }
            df.appendChild(makeEmptyComment(mod, cmd));
            return df;
        }
        String cat = accessutils.removeFilePart(mod.getAbsoluteUri()).toString();
        try {
            URI abs_uri = accessutils.makeAbsoluteURI(found_uri.toString(), cat);
            correctAddressing(df, mod, abs_uri);
        } catch (Exception ex) {
            mod.getReporter().pushMessage("bad_processing_instruction", cmd.toString(), ex.getMessage());
            df.appendChild(makeFailureComment(mod, cmd));
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
    public DocumentFragment makePI_WIKIContent(Module mod, PIcommand cmd) {
        // PI control: LOCATION exists
        // if KEEPREFERENCES -> yes or no
        // if USECOPY -> yes or no
        // DPATH  or XPATH exists
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        URI abs_uri = null;
        if (!cmd.paramExist(PIcommand.LOCATION)) {
            // should not happen
            mod.getReporter().pushMessage("bad_processing_instruction", "Missing " + PIcommand.LOCATION);
            df.appendChild(wxtError.makeErrorMsgForDebugging(mod, cmd, "missing location in Producer:WIKIContent"));
            //df.appendChild(makeFailureComment(mod.getDoc(), cmd));
            return df;
        }
        try {
            abs_uri = accessutils.makeUri(cmd.getValue(PIcommand.LOCATION));
        } catch (Exception e) {
            mod.getReporter().pushMessage("bad_processing_instruction", cmd.getOriginalData(), e.getMessage());
            df.appendChild(makeFailureComment(mod, cmd));
            return df;
        }


        // go for it
        //------------------------
        try {
            //WIKIContent wik=new WIKIContent(mod, cmd);
            WIKIContent wik = new WIKIContent(mod, cmd);
            df = wik.getContent(mod, cmd);
        } catch (Exception ex) {
            df = null;
        }
        //-----------------------*/

        /* ------alternative: rename getContent and make it static ----
        df = WIKIContent.getStaticContent(mod, cmd);
        //-----------------------*/

        if (df == null) {
            df = mod.getDoc().createDocumentFragment();
            df.appendChild(makeFailureComment(mod, cmd));
            return df;
        }
        if (!df.hasChildNodes()) {
            df.appendChild(makeEmptyComment(mod, cmd));
        }

        // register all images in df
        registerAllImages(df, mod);


        return df;
    }

    /**
     * Produce a documentfragment with content from a OpenOffice document
     * @param mod The Module requesting the content
     * @param cmd The PIcommand describing the request
     * @return a DocumentFragment, always
     */
    public DocumentFragment makePI_ODFContent(Module mod, PIcommand cmd) {
        // PI control:location dpath exists
        //            if USECOPY >yes or no"
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        ODTContent odtc = null;
        try {
            odtc = new ODTContent(mod, cmd);
        } catch (Exception ex) {
            // should not happen
            df.appendChild(wxtError.makeErrorMsgForDebugging(mod, cmd, ex.getMessage() + " in producer:ODFContent"));
            mod.getReporter().pushMessage("pi_produced_no_result", cmd.getOriginalData(), ex.getMessage());
            return df;
        }

        // get it
        df = odtc.getContent(mod, cmd);

        if (df == null) {
            df = mod.getDoc().createDocumentFragment();
            df.appendChild(makeFailureComment(mod, cmd));
            return df;
        }
        if (!df.hasChildNodes()) {
            df.appendChild(makeEmptyComment(mod, cmd));
            return df;
        }

        // register all images in df
        registerAllImages(df, mod);

        return df;
    }

    /**
     * Produce a documentfragment with a formula
     *
     * @param mod The Module requesting the content
     * @param cmd The PIcommand describing the request
     * @return a DocumentFragment, always
     */
    public DocumentFragment makePI_Formula(Module mod, PIcommand cmd) {
        // PI:control: type or id

        DocumentFragment df = mod.getDoc().createDocumentFragment();

        String id = null;
        if (cmd.paramExist(PIcommand.ID)) {
            id = cmd.getValue(PIcommand.ID);
        }

        // id is controlled
        // and we have either source or location together with type
        Formula fma = null;
        //-------------------------
        // type
        if (cmd.paramExist(PIcommand.TYPE)) {
            String typ = cmd.getValue(PIcommand.TYPE);
            try {
                if (typ.compareTo(PIcommand.GOOGLE) == 0) {
                    fma = new GoogleTexFormula(mod, cmd);
                } else if (typ.compareTo(PIcommand.LATEX) == 0) {
                    fma = new LaTexFormula(mod, cmd);
                } else if (typ.compareTo(PIcommand.IMAGE) == 0) {
                    fma = new ImageFormula(mod, cmd);
                } else if (typ.compareTo(PIcommand.MATHML) == 0) {
                    fma = new MathMLFormula(mod, cmd);
                } else {
                    // should not happen
                    df.appendChild(wxtError.makeErrorMsgForDebugging(mod, cmd, "unknown formula type"));
                    df.appendChild(makeFailureComment(mod, cmd));
                    throw new Exception("unknown formula type");
                }
            } catch (Exception tex) {
                mod.getReporter().pushMessage("cannot_establish_formula", cmd.toString() + " : " + tex.getMessage());
            }
            // register it
            if (fma != null) {
                mod.getDefinitions().registerNewFormula(fma);
            }
        }

        // formula is established and registered
        //----------------------------------------------
        // we get it from the stored formulas, old or as produced above
        if (id != null) {
            // notify globally
            int globalindex = 0;
            if (mod.getDefinitions().getFormula(id) != null) {
                globalindex = mod.getDefinitions().getFormulaHolder().add(mod.getDefinitions().getFormula(id), id, mod) + 1;
            }

            // only globally;
            // transport index in cmd
            cmd.setParameter(PIcommand.INDEX, "" + globalindex);
        } else {
            // should not happen
            df.appendChild(wxtError.makeErrorMsgForDebugging(mod, cmd, "formula without latex and id in producer:ODTFormula"));
            df.appendChild(makeFailureComment(mod, cmd));
        }

        Formula F = mod.getDefinitions().getFormula(id);
        if (F != null) {
            Element fspan = mod.getDoc().createElement("span");
            fspan.setAttribute("class", cmd.getCommandStyleName());
            fspan.appendChild(F.produceIntextElement(mod, cmd));
            df.appendChild(fspan);
        } else {
            mod.getReporter().pushMessage("cannot_find_item", cmd.getValue(PIcommand.ID), cmd.getOriginalData());
            df.appendChild(makeFailureComment(mod, cmd));
        }
        if (!df.hasChildNodes()) {
            df.appendChild(makeEmptyComment(mod, cmd));
        }
        return df;
    }

    /**
     * Produce a documentfragment with a gadget
     *
     * @param mod The Module requesting the content
     * @param cmd The PIcommand describing the request
     * @return a DocumentFragment, always
     */
    public DocumentFragment makePI_Gadget(Module mod, PIcommand cmd) {
        // PI controlled
        // location or id
        // expanded,width,height,title,text,id,movable,position, left, top
        /* pattern:
        <div class wxtgadget
        <div class header
        <span class on/off>
        </div>
        <div class content
        </div>
        </div>
         */
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        // we have either a location for an iframe
        // or we have a fragmentid for strait content        
        URI iframeSrctUri = null;
        DocumentFragment contentFragment = null;
        if (cmd.paramExist(PIcommand.FRAGMENTID)) {
            String fid = cmd.getValue(PIcommand.FRAGMENTID);
            //Fragment f=mod.getDefinitions().getFragments().get(fid);
            Fragment f = mod.getDefinitions().getFragment(fid);
            if (f == null) {
                mod.getReporter().pushMessage("no_fragment_produced_from", cmd.toString());
                df.appendChild(makeFailureComment(mod, cmd));
                return df;
            }
            contentFragment = f.getFragment();
        } else {
            // we have a location
            String targetUriStr = cmd.getValue(PIcommand.LOCATION);
            targetUriStr = mod.getDefinitions().substituteFragments(targetUriStr);
            URI absoluteTargetUri = null;

            try {
                absoluteTargetUri = accessutils.makeAbsoluteURI(targetUriStr, mod.getCatalog());
                iframeSrctUri = accessutils.makeRelativeURI(mod.getAbsoluteUri(), absoluteTargetUri);
            } catch (Exception uex) {
                mod.getReporter().pushMessage("bad_processing_instruction", cmd.toString());
                df.appendChild(makeFailureComment(mod, cmd));
                return df;
            }
        }

        // adjust header width with +5
        String W = cmd.getValue(PIcommand.WIDTH).trim();
        String hW = W;
        if (W.endsWith("px")) {
            W = W.substring(0, W.length() - 2);
            try {
                hW = "" + (Integer.valueOf(W) + 5) + "px";
                hW = hW.trim();
            } catch (Exception ex) {
            }
            W = W + "px";
        }

        // CSS properties: left, top
        String left = null;
        String top = null;
        if (cmd.paramExist(PIcommand.LEFT)) {
            left = cmd.getValue(PIcommand.LEFT);
        }
        if (cmd.paramExist(PIcommand.TOP)) {
            top = cmd.getValue(PIcommand.TOP);
        }

        // height
        String H = null;
        if (cmd.paramExist(PIcommand.HEIGHT)) {
            H = cmd.getValue(PIcommand.HEIGHT).trim();
        }

        // title (title is deprecated, use text)
        String Title = "_";
        if (cmd.paramExist(PIcommand.TITLE)) {
            Title = cmd.getValue(PIcommand.TITLE);
        }
        if (cmd.paramExist(PIcommand.TEXT)) {
            Title = cmd.getValue(PIcommand.TEXT);
        }

        // expanded
        boolean expanded = false;
        if (cmd.paramExist(PIcommand.EXPANDED)) {
            expanded = cmd.getValue(PIcommand.EXPANDED).trim().compareTo(PIcommand.YES) == 0;
        }

        // position
        String position = null;
        if (cmd.paramExist(PIcommand.POSITION)) {
            position = cmd.getValue(PIcommand.POSITION);
        }

        // movable
        boolean movable = false;
        if (cmd.paramExist(PIcommand.MOVABLE)) {
            movable = cmd.getValue(PIcommand.MOVABLE).compareTo(PIcommand.YES) == 0;
        }
        if (movable && (position == null)) {
            position = "absolute";
        }

        //id Use fragment id, but can be overruled to avoid conficts
        String id = Title.replace(' ', '_');
        if (cmd.paramExist(PIcommand.ID)) {
            id = cmd.getValue(PIcommand.ID);
        }


        // ready to produce element
        // innerwrapper
        Element wrapper = mod.getDoc().createElement("div");
        wrapper.setAttribute("class", cmd.getCommandStyleName());
        String styleS = "";
        if (left != null) {
            styleS = styleS + PIcommand.LEFT + ":" + left + ";";
        }
        if (top != null) {
            styleS = styleS + PIcommand.TOP + ":" + top + ";";
        }
        styleS = styleS + PIcommand.WIDTH + ":" + hW + ";";
        if (position != null) {
            styleS = styleS + PIcommand.POSITION + ":" + position;
        }
        if (styleS.length() > 1) {
            wrapper.setAttribute("style", styleS);
        }

        wrapper.setAttribute("id", id);


        //header
        Element header = mod.getDoc().createElement("div");
        header.setAttribute("class", "header");
        if (movable) {
            header.setAttribute("style", "cursor:move");
        }

        // span in header
        Element spanElt = mod.getDoc().createElement("span");
        if (expanded) {
            spanElt.setAttribute("class", "on");
        } else {
            spanElt.setAttribute("class", "off");
        }
        spanElt.setAttribute("onclick", "toggleExpand(this);");
        spanElt.appendChild(mod.getDoc().createTextNode("_"));

        header.appendChild(spanElt);
        header.appendChild(mod.getDoc().createTextNode(Title));

        // content
        Element content = mod.getDoc().createElement("div");
        content.setAttribute("class", "content");
        if (expanded) {
            content.setAttribute("style", "display:block");
        } else {
            content.setAttribute("style", "display:none");
        }

        if (iframeSrctUri != null) {
            //iframe
            Element iframe = mod.getDoc().createElement("iframe");
            //iframe.setAttribute("src",cmd.getValue(PIcommand.LOCATION));
            iframe.setAttribute("src", iframeSrctUri.toString());


            if (Character.isDigit(W.charAt(W.length() - 1))) {
                W = W + "px";
            }
            if (H != null) {
                if (Character.isDigit(H.charAt(H.length() - 1))) {
                    H = H + "px";
                }
                iframe.setAttribute("style", "width:" + W + ";height:" + H);
            } else {
                iframe.setAttribute("style", "width:" + W);
            }

            iframe.appendChild(mod.getDoc().createTextNode(" "));
            content.appendChild(iframe);
        } else {
            //direct content
            content.appendChild(mod.getDoc().importNode(contentFragment, true));
        }


        wrapper.appendChild(header);
        wrapper.appendChild(content);

        // initmove
        if (movable) {
            Element scriptElt = mod.getDoc().createElement("script");
            scriptElt.setAttribute("type", "text/javascript");
            scriptElt.appendChild(mod.getDoc().createTextNode("dragDrop.initElement('" + id + "');"));
            wrapper.appendChild(scriptElt);
        }


        df.appendChild(wrapper);
        return df;
    }

    /**
     * Produce a documentfragment with an image
     * or an (expandable) image thumb
     *
     * @param mod The Module requesting the content
     * @param cmd The PIcommand describing the request
     * @return a DocumentFragment, always
     */
    public DocumentFragment makePI_Image(Module mod, PIcommand cmd) {
        DocumentFragment df = mod.getDoc().createDocumentFragment();

        // attempt to register this image on the fly
        // if location is set, it is considered as new
        // and all attributes as in an image file is allowed
        Image theImage = null;
        if (cmd.paramExist(PIcommand.LOCATION)) {
            try {
                theImage = new Image(cmd, mod.getAbsoluteUri(), mod.getDefinitions());
                mod.getDefinitions().registerNewImage(theImage);
                // we let the cmd contain what we want to use below
                // which is only the id, which is already there
            } catch (Exception ex) {
                // error and return
                return df;
            }
        }
        //-----------------------------------------
        // use existing, which may be a newly registered above
        if (cmd.paramExist(PIcommand.ID)) {
            String sid = cmd.getValue(PIcommand.ID);
            Image im = mod.getDefinitions().getImage(sid);

            // notify globally
            int globalindex = 0;
            if (mod.getDefinitions().getImage(sid) != null) {
                globalindex = mod.getDefinitions().getImageHolder().add(im, sid, mod) + 1;
            }

            // only globally;
            // transport index in cmd
            cmd.setParameter(PIcommand.INDEX, "" + globalindex);

        } else {
            mod.getReporter().pushMessage("bad_processing_instruction", "missing id");
            df.appendChild(wxtError.makeErrorMsgForDebugging(mod, cmd, "missing id in producer:Image"));
            //df.appendChild(makeFailureComment(mod.getDoc(),cmd));
            return df;
        }


        String imID = cmd.getValue(PIcommand.ID);
        Image im = mod.getDefinitions().getImage(imID);
        if (im == null) {
            mod.getReporter().pushMessage("bad_processing_instruction", "missing image:" + imID);
            df.appendChild(makeFailureComment(mod, cmd));
            return df;
        }


        // no display ?
        if ((cmd.paramExist(PIcommand.DISPLAY))
                && (cmd.getValue(PIcommand.DISPLAY).compareTo("0") == 0)) {
            df.appendChild(makeEmptyComment(mod, cmd));
            return df;
        }

        // an image or an imagethumb
        // will display
        return im.produceIntextElement(mod, cmd);


    }

    /**
     * Produce a documentfragment with a formulalist
     * based on TEXFormulas only
     *
     * @param mod The Module requesting the content
     * @param cmd The PIcommand describing the request
     * @return a DocumentFragment, always
     */
    public DocumentFragment makePI_FormulaList(Module mod, PIcommand cmd) {
        // moduleselection
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        // get a list of all modules we want to investigate
        // cmd attributes as in modulemap etc.
        List<Module> moduleList = null;
        moduleList = getModuleList(mod, cmd);
        if (moduleList == null || moduleList.isEmpty()) {
            df.appendChild(makeFailureComment(mod, cmd));
            mod.getReporter().pushMessage("empty_module_selection", cmd.toString());
            df.appendChild(makeEmptyComment(mod, cmd));
            return df;
        }
        // get all used formulas
        List<String> usedIds = mod.getDefinitions().getFormulaHolder().getAllReferenceIds();
        if (usedIds.isEmpty()) {
            // empty message
            df.appendChild(makeEmptyComment(mod, cmd));
            return df;
        }

        // reduce the idlist so it only contains formulas on the actual modulelist
        for (int ix = usedIds.size() - 1; ix >= 0; ix--) {
            Module ixmod = mod.getDefinitions().getFormulaHolder().getModuleAtIndex(ix);
            boolean found = false;
            for (Module m : moduleList) {
                found = found || (m == ixmod);
            }
            if (!found) {
                usedIds.remove(ix);
            }
        }

        // reduce duplicates in usedIds
        for (int ix = 0; ix < usedIds.size(); ix++) {
            String ixS = usedIds.get(ix);
            int ix2 = ix + 1;
            while (ix2 < usedIds.size()) {
                if (usedIds.get(ix2).compareTo(ixS) == 0) {
                    usedIds.remove(ix2);
                } else {
                    ix2++;
                }
            }
        }

        if (usedIds.isEmpty()) {
            // empty message
            df.appendChild(makeEmptyComment(mod, cmd));
            return df;
        }


        int cols = 1;
        if (cmd.paramExist(PIcommand.COLS)) {
            cols = Math.max(cmd.getValueAsInteger(PIcommand.COLS), 0);
        }
        Element outerWrapper = mod.getDoc().createElement("div");
        outerWrapper.setAttribute("class", cmd.getCommandStyleName() + String.valueOf(cols));

        Element wrapper = mod.getDoc().createElement("ul");
        wrapper.setAttribute("class", cmd.getCommandStyleName() + "inner");

        outerWrapper.appendChild(wrapper);

        for (String id : usedIds) {
            Formula frm = mod.getDefinitions().getFormula(id);
            //Element box=frm.produceListElement(mod, cmd,mod.getDefinitions().getFormulaHolder().getAllItemsUsingID(id));
            //wrapper.appendChild(box);
            wrapper.appendChild(frm.produceListElement(mod, cmd, mod.getDefinitions().getFormulaHolder().getAllItemsUsingID(id)));
        }
        df.appendChild(outerWrapper);
        return df;

    }

    /**
     * Produce a documentfragment with an imagelist
     *
     * @param mod The Module requesting the content
     * @param cmd The PIcommand describing the request
     * @return a DocumentFragment, always
     */
    public DocumentFragment makePI_ImageList(Module mod, PIcommand cmd) {
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        // get a list of all modules we want to investigate
        // cmd attributes as in modulemap etc.
        List<Module> moduleList = null;
        moduleList = getModuleList(mod, cmd);
        if (moduleList == null || moduleList.isEmpty()) {
            df.appendChild(makeFailureComment(mod, cmd));
            mod.getReporter().pushMessage("empty_module_selection", cmd.toString());
            df.appendChild(makeEmptyComment(mod, cmd));
            return df;
        }

        // find height of thumbs
        Image.THUMBHEIGHT = Image.DEFAULT_THUMBHEIGHT;
        if (cmd.paramExist(PIcommand.THUMBSIZE)) {
            String tmp = cmd.getValue(PIcommand.THUMBSIZE);
            int val = accessutils.getNumericStart(tmp);
            if (val != -1) {
                Image.THUMBHEIGHT = val;
            }
        }

        // get all used images
        List<String> usedIds = mod.getDefinitions().getImageHolder().getAllReferenceIds();
        if (usedIds.isEmpty()) {
            // empty message
            return df;
        }

        // reduce the idlist so it only contains images on the actual modulelist
        for (int ix = usedIds.size() - 1; ix >= 0; ix--) {
            Module ixmod = mod.getDefinitions().getImageHolder().getModuleAtIndex(ix);
            boolean found = false;
            for (Module m : moduleList) {
                found = found || (m == ixmod);
            }
            if (!found) {
                usedIds.remove(ix);
            }
        }

        // reduce duplicates in usedIds
        for (int ix = 0; ix < usedIds.size(); ix++) {
            String ixS = usedIds.get(ix);
            int ix2 = ix + 1;
            while (ix2 < usedIds.size()) {
                if (usedIds.get(ix2).compareTo(ixS) == 0) {
                    usedIds.remove(ix2);
                } else {
                    ix2++;
                }
            }
        }
        // do we want to split. insert break, on new module
        Boolean breakOnModule = cmd.paramExist(PIcommand.SPLIT)
                && (cmd.getValue(PIcommand.SPLIT).compareTo(PIcommand.YES) == 0);
        Element mainWrapper = mod.getDoc().createElement("div");

        Element thumbWrapper = mod.getDoc().createElement("div"); // or span
        thumbWrapper.setAttribute("class", cmd.getCommandStyleName());
        Module thisM = null;
        for (String id : usedIds) {
            Image im = mod.getDefinitions().getImage(id);
            Module m = mod.getDefinitions().getImageHolder().getFirstModuleUsing(id);
            // break the line on new module
            if (breakOnModule && ((thisM == null) || (thisM != m))) {
                thumbWrapper.appendChild(mod.getDoc().createElement("br"));
                thisM = m;
            }
            thumbWrapper.appendChild(im.produceListElement(mod, cmd, null));
        }


        df.appendChild(thumbWrapper);

        return df;
    }

    /**
     * Produce a fragment with content from a non-scriptet txt import
     * 
     * @param mod The Module requesting the content
     * @param cmd The PIcommand describing the request
     * @return a DocumentFragment, always
     */
    public DocumentFragment makePI_TXTContent(Module mod, PIcommand cmd) {
        // PI control:if PARSE -> yes or no
        //            if code: legal code
        if (!cmd.paramExist(PIcommand.LOCATION)) {
            // should not happen
            mod.getReporter().pushMessage("bad_processing_instruction", "Missing " + PIcommand.LOCATION);
            DocumentFragment df = mod.getDoc().createDocumentFragment();
            df.appendChild(wxtError.makeErrorMsgForDebugging(mod, cmd, "missing location in producer:TXTContent"));
            //df.appendChild(mod.getProducer().makeFailureComment(mod.getDoc(),cmd));
            return df;
        }

        //--------- reuse content if possible ------------
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        Content theContent = mod.getRegisteredContent(cmd.getValue(PIcommand.LOCATION));
        if (theContent == null) {
            try {
                theContent = new TXTContent(mod, cmd);
                mod.registerContent(cmd.getValue(PIcommand.LOCATION), theContent);
            } catch (Exception ex) {
                mod.getReporter().pushMessage("bad_processing_instruction", cmd.toString(), ex.getMessage());
                df.appendChild(makeFailureComment(mod, cmd));
                return df;
            }
        }
        df = theContent.getContent(mod, cmd);


        if (!df.hasChildNodes()) {
            // report only when not updating template
            if (!cmd.paramExist(PIcommand.TEMPLATE)) {
                mod.getReporter().pushMessage("pi_produced_no_result", cmd.toString());
            }
            df.appendChild(mod.getProducer().makeEmptyComment(mod, cmd));
        }
        return df;
    }

    /**
     * Produce a popup element
     * Depending on javascript function: simplePopup
     * 
     * @param mod The Module that request the popup
     * @param cmd The PICommand that describes the list of modules and the collection
     * @return A documentFragment
     */
    public DocumentFragment makePI_Popup(Module mod, PIcommand cmd) {
        // PI control:
        // location exists
        // if(LEFTPAR or RIGHTPAR or REPLACE or SELECT or PARSE or LANG)-> sourcelocation exists
        // location != sourcelocation
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        // can we use it at all        
        if (!cmd.paramExist(PIcommand.LOCATION)) {
            // should not happen
            mod.getReporter().pushMessage("bad_processing_instruction", "Missing " + PIcommand.LOCATION);
            df.appendChild(wxtError.makeErrorMsgForDebugging(mod, cmd, "missing location in producer:Popup"));
            //df.appendChild(makeFailureComment(mod.getDoc(),cmd));
            return df;
        }
        String targetUriStr = cmd.getValue(PIcommand.LOCATION);
        targetUriStr = mod.getDefinitions().substituteFragments(targetUriStr);
        URI absoluteTargetUri = null;
        URI relativeTargetUri = null;
        try {
            absoluteTargetUri = accessutils.makeAbsoluteURI(targetUriStr, mod.getCatalog());
            relativeTargetUri = accessutils.makeRelativeURI(mod.getAbsoluteUri(), absoluteTargetUri);
        } catch (Exception uex) {
            mod.getReporter().pushMessage("bad_processing_instruction", cmd.toString(), uex.getMessage());
            df.appendChild(makeFailureComment(mod, cmd));
            return df;

        }
        // there is no reason to load and prepare anything if 
        // dont have a load - prepare - save strategy
        // the dependancy sourcelocation and textchanges are controlled in PIcommand:control
        if (cmd.paramExist(PIcommand.SOURCELOCATION)) {

            // we attempt to get the text, with encoding, selection and replaces,
            // and possiblly transformation
            String theText = "";
            try {
                theText = TXTContent.getFormattedText(mod, cmd);
            } catch (Exception e) {
                mod.getReporter().pushMessage("bad_processing_instruction", cmd.toString(), e.getMessage());
                return Content.getFailedContent(mod, cmd);
            }

            if (theText.isEmpty()) {
                mod.getReporter().pushMessage("pi_produced_no_result", cmd.toString());
                df.appendChild(makeEmptyComment(mod, cmd));
                return df;
            }

            //--------------------------
            // we have the text prepared according to cmd

            // we must decide which template to put it int
            Template the_template = null;
            Document popDoc = null;
            if (cmd.paramExist(PIcommand.TEMPLATE)) {
                the_template = mod.getDefinitions().getTemplate(PIcommand.TEMPLATE);
            }
            if (the_template == null) {
                the_template = mod.getTemplate();
            }

            // try to use it
            try {
                popDoc = the_template.getDoc(mod);
                // clean it: empty everything within body-tag
                Node bodyNod = popDoc.getElementsByTagName("body").item(0);
                Node c = bodyNod.getLastChild();
                while (c != null) {
                    Node n = c.getPreviousSibling();
                    bodyNod.removeChild(c);
                    c = n;
                }
            } catch (Exception tex) {
                mod.getReporter().pushMessage("pi_produced_no_result", cmd.toString());
                df.appendChild(makeEmptyComment(mod, cmd));
                return df;
            }

            // now we must readdress everything in the header, scripts and stylelinks
            URI sourceURI = the_template.getabsoluteURI();
            Node headNod = popDoc.getElementsByTagName("head").item(0);
            Node c = headNod.getFirstChild();
            while (c != null) {
                try {
                    if (c.getNodeType() == Node.ELEMENT_NODE) {
                        Element elt = (Element) c;
                        if (elt.getNodeName().compareTo("title") == 0) {
                            elt.replaceChild(popDoc.createTextNode("WXT-popup"), elt.getFirstChild());
                        } else if (elt.hasAttribute("src")) {
                            String tmp = elt.getAttribute("src");
                            tmp = mod.getDefinitions().substituteFragments(tmp);

                            if (sourceURI != null) {
                                String sourcecat = accessutils.removeFilePart(sourceURI).toString();
                                URI absUri = accessutils.makeAbsoluteURI(tmp, sourcecat);
                                URI relUri = accessutils.makeRelativeURI(absoluteTargetUri, absUri);

                                elt.setAttribute("src", relUri.toString());
                            }

                        } else if (elt.hasAttribute("href")) {
                            String tmp = elt.getAttribute("href");
                            tmp = mod.getDefinitions().substituteFragments(tmp);

                            if (sourceURI != null) {
                                String sourcecat = accessutils.removeFilePart(sourceURI).toString();
                                URI absUri = accessutils.makeAbsoluteURI(tmp, sourcecat);
                                URI relUri = accessutils.makeRelativeURI(absoluteTargetUri, absUri);

                                elt.setAttribute("href", relUri.toString());
                            }
                        }
                    }
                } catch (Exception adex) {
                    continue;
                }

                c = c.getNextSibling();
            }

            // now we must put the text into the body element of popDoc
            // parsed, coded or just text
            Element bodyElt = (Element) popDoc.getElementsByTagName("body").item(0);
            if (cmd.paramExist(PIcommand.LANG)) {
                // try to clean and fix code text
                theText = CodeBasics.cleanString(theText);

                Element preElt = popDoc.createElement("pre");
                String ID = mod.getScriptHandler().getANewId(mod.getID());
                preElt.setAttribute("id", ID);
                preElt.setAttribute("class", PIcommand.SKIPTRANSLATE + " "
                        + CodeBasics.PRETTYPRINT + " "
                        + cmd.getValue(PIcommand.LANG));
                preElt.appendChild(popDoc.createTextNode(theText));
                bodyElt.appendChild(preElt);
                //String tmp=bodyElt.getAttribute("onload");
                //bodyElt.setAttribute("onload", "preparePage('"+ID+"'); "+tmp);
                //bodyElt.setAttribute("onload", "prettyPrint();");
            } else if ((cmd.paramExist(PIcommand.PARSE))
                    && (cmd.getValue(PIcommand.PARSE).compareTo(PIcommand.YES) == 0)) { // should parse the text, just copy now
                DocumentFragment tmpd = null;
                try {
                    tmpd = domer.produceDocFragmentFromString(theText, popDoc.getXmlEncoding(), mod.getReporter());
                    bodyElt.appendChild(popDoc.importNode(tmpd, true));
                } catch (Exception dex) {
                    bodyElt.appendChild(popDoc.createTextNode(theText));
                }
            } else { // simple text
                bodyElt.appendChild(popDoc.createTextNode(theText));
            }

            //----------------------------
            // Even if it is not important we try to be consistant
            // color code already classmarked pre node(s) even in popup windows
            if (mod.getDefinitions().getOption(Definitions.PREFORMAT_LANGUAGE).compareTo(Definitions.NO) == 0) {
                popDoc = CodeGoogleFormatter.expandGoogleCodeFragments(popDoc, popDoc.getXmlEncoding(), mod);
            } else {
                popDoc = CodeFormatter.expandCodeFragments(popDoc, popDoc.getXmlEncoding(), mod);
            }

            // save it
            try {
                domer.saveDom(popDoc, absoluteTargetUri, mod.getEncoding(), false, mod.getOutputFormat());
            } catch (Exception ex) {
                mod.getReporter().pushMessage("bad_processing_instruction", cmd.toString(), ex.getMessage());
                return Content.getFailedContent(mod, cmd);
            }
        }

        // prepare the documentfragment to return
        Element elt = mod.getDoc().createElement("span");
        elt.setAttribute("class", cmd.getCommandStyleName());

        if (cmd.paramExist(PIcommand.TITLE)) {
            elt.setAttribute("title", cmd.getValue(PIcommand.TITLE));
        } else {
            elt.setAttribute("title", "popup");
        }

        String tmpT = relativeTargetUri.toString();
        if (cmd.paramExist(PIcommand.TEXT)) {
            tmpT = cmd.getValue(PIcommand.TEXT);
        }
        elt.appendChild(mod.getDoc().createTextNode(tmpT));
        String tmps = "javascript:simplepopup('" + relativeTargetUri.toString() + "','" + "popup" + "','*')";
        elt.setAttribute("onclick", tmps);
        df = mod.getDoc().createDocumentFragment();
        df.appendChild(elt);
        return df;
    }

    /**
     * Produce an on/off expansion based on an ajax request.
     * PI expand is removed, is now expandable
     *
     * @param mod The module that makes the request
     * @param cmd The PIcommand that describes the request
     * @return A documentFragment
     */
    public DocumentFragment makePI_ExpandAJAX(Module mod, PIcommand cmd) {
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        // PI expand is removed
        mod.getReporter().pushMessage("bad_processing_instruction", "Removed: " + cmd.getCommand());
        df.appendChild(wxtError.makeErrorMsgForDebugging(mod, cmd, "missing location in producer:ExpandAJAX"));
        df.appendChild(makeFailureComment(mod, cmd));
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
    public DocumentFragment makePI_Expandable(Module mod, PIcommand cmd) {
        // PI control: location exists, sourcelocation NOT exists
        // if expanded -> yes or no
        // if code -> it is a legal code
        // location,fragmentid, title,text,expanded,transformation,leftpar,rightpar,select
        // replace, encoding, lang, parse
        /*
        <div class expandable
        <div class header
        <span class on/off
        </div
        <div class content
        </div
        </div>
         */

        DocumentFragment df = mod.getDoc().createDocumentFragment();
        DocumentFragment stuff = mod.getDoc().createDocumentFragment();

        // critical errors in missing attributes are picked up in PIcommand

        String title = "XX";

        //-----------------------
        // we go for the content, as text
        String theText = "";
        if (cmd.paramExist(PIcommand.FRAGMENTID)) {
            String fid = cmd.getValue(PIcommand.FRAGMENTID);
            //Fragment f=mod.getDefinitions().getFragments().get(fid);
            Fragment f = mod.getDefinitions().getFragment(fid);
            if (f == null) {
                mod.getReporter().pushMessage("no_fragment_produced_from", cmd.toString());
                df.appendChild(makeFailureComment(mod, cmd));
                return df;
            }
            theText = f.getFragmentAsString(); // or fill stuffa at once ?
            title = fid;
        } else {
            // we have a location
            String targetUriStr = cmd.getValue(PIcommand.LOCATION);
            targetUriStr = mod.getDefinitions().substituteFragments(targetUriStr);
            URI absoluteTargetUri = null;
            URI relativeTargetUri = null;
            try {
                absoluteTargetUri = accessutils.makeAbsoluteURI(targetUriStr, mod.getCatalog());
                relativeTargetUri = accessutils.makeRelativeURI(mod.getAbsoluteUri(), absoluteTargetUri);
            } catch (Exception uex) {
                mod.getReporter().pushMessage("bad_processing_instruction", cmd.toString());
                df.appendChild(makeFailureComment(mod, cmd));
                return df;

            }

            title = relativeTargetUri.toString();
            try {
                theText = TXTContent.getFormattedText(mod, cmd);
            } catch (Exception e) {
                mod.getReporter().pushMessage("bad_processing_instruction", cmd.toString(), e.getMessage());
                return Content.getFailedContent(mod, cmd);
            }
        }

        if (cmd.paramExist(PIcommand.TEXT)) {
            title = cmd.getValue(PIcommand.TEXT);
        }

        if (cmd.paramExist(PIcommand.TITLE)) {
            title = cmd.getValue(PIcommand.TITLE);
        }


        if (theText == null || theText.isEmpty()) {
            mod.getReporter().pushMessage("pi_produced_no_result", cmd.toString());
            df.appendChild(makeFailureComment(mod, cmd));
            return df;
        }

        //--------------------------
        // we have the text prepared according to cmd

        // treat code special
        // produce something that will be picked up as if it
        // were quoted directly
        String codeID = mod.getScriptHandler().getANewId(mod.getID());
        if (cmd.paramExist(PIcommand.LANG)) {
            Element preElt = mod.getDoc().createElement("pre");
            // is picked up in Module as premarked pre elt
            preElt.setAttribute("class", CodeBasics.PRETTYPRINT + " " + cmd.getValue(PIcommand.LANG));
            theText = CodeBasics.cleanString(theText);
            preElt.appendChild(mod.getDoc().createTextNode(theText));

            stuff.appendChild(preElt);
        } else {
            try {
                stuff = (DocumentFragment) mod.getDoc().importNode(domer.produceDocFragmentFromString(theText, mod.getEncoding(), mod.getReporter()), true);
            } catch (Exception dex) {
                mod.getReporter().pushMessage("pi_produced_no_result", cmd.toString());
                df.appendChild(makeFailureComment(mod, cmd));
                return df;
            }
        }

        //--------------
        // should we expand it
        // we can only turn on expansion by option( in script or in parameter), not off
        boolean expandit = false;
        // expanded or not
        if (cmd.paramExist(PIcommand.EXPANDED)) {
            String expandvalue = cmd.getValue(PIcommand.EXPANDED);
            expandit = expandvalue.compareToIgnoreCase(PIcommand.YES) == 0;
        }
        // correct for global option, overriding cmd
        String exall = mod.getDefinitions().getOption(Definitions.EXPAND_ALL);
        if ((exall != null) && (exall.compareToIgnoreCase("yes") == 0)) {
            expandit = true;
        }

        // general expand option (expand-all) should override the attribute if set to yes
        if (!expandit) {
            String tmp = mod.getScriptHandler().getArgument(Definitions.EXPAND_ALL);
            if ((tmp != null) && (tmp.compareToIgnoreCase(PIcommand.YES) == 0)) {
                expandit = true;
            }
        }
        //--------------

        //------------------
        // prepare the element        
        Element wrapper = mod.getDoc().createElement("div");
        wrapper.setAttribute("class", cmd.getCommandStyleName());

        Element header = mod.getDoc().createElement("div");
        header.setAttribute("class", "header");
        Element spanElt = mod.getDoc().createElement("span");
        spanElt.setAttribute("onclick", "javascript:toggleExpand(this)");
        spanElt.appendChild(mod.getDoc().createTextNode("_"));
        header.appendChild(spanElt);
        header.appendChild(mod.getDoc().createTextNode(title));
        Element content = mod.getDoc().createElement("div");
        content.setAttribute("class", "content");
        content.appendChild(stuff);
        if (expandit) {
            spanElt.setAttribute("class", "on");
            content.setAttribute("style", "display:block");
        } else {
            spanElt.setAttribute("class", "off");
            content.setAttribute("style", "display:none");
        }
        wrapper.appendChild(header);
        wrapper.appendChild(content);
        df.appendChild(wrapper);
        return df;
    }

    /**
     * Inserts a reference from a reference list
     * as intext. 
     * 
     * @param mod The module requesting the reference 
     * @param cmd The PI-command describing the reference-request 
     * @return The resulting nodecollection in a fragment 
     */
    public DocumentFragment makePI_Reference(Module mod, PIcommand cmd) {
        // cmd is carrying index, added in calling module
        // PI control: id exists
        DocumentFragment df = mod.getDoc().createDocumentFragment();

        if (!cmd.paramExist(PIcommand.ID)) {
            // should not happen
            mod.getReporter().pushMessage("missing_id_in_referenece");
            df.appendChild(wxtError.makeErrorMsgForDebugging(mod, cmd, "missing id in producer:reference"));
            return df;
        }
        String id = cmd.getValue(PIcommand.ID);
        SimpleReference r = mod.getDefinitions().getReference(id);
        if (r == null) {
            mod.getReporter().pushMessage("no_referenece_found", id);
            df.appendChild(makeFailureComment(mod, cmd));
            return df;
        }


        // ok, its there, produce it with the selected style
        // and the transferred index

        return r.produceIntextElement(mod, cmd);

    }

    /**
     * Make a list of refernces sorted, without Module references
     * @param mod The Module requesting the list
     * @param cmd The command describing the request
     * @return A document fragment
     */
    private DocumentFragment makeCompactSortedReferenceList(Module mod, PIcommand cmd) {
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        // all modules are candidates
        List<Module> ModList = mod.getScriptHandler().getAllModules(null);
        //String style=mod.getDefinitions().getOption(Definitions.REFERENCE_FORM);
        int cols = 1;
        if (cmd.paramExist(PIcommand.COLS)) {
            cols = Math.max(cmd.getValueAsInteger(PIcommand.COLS), 1);
        }
        Element outerWrapper = mod.getDoc().createElement("div");
        if (cols > 0) {
            outerWrapper.setAttribute("class", cmd.getCommandStyleName() + String.valueOf(cols));
        } else {
            outerWrapper.setAttribute("class", cmd.getCommandStyleName());
        }
        Element innerWrapper = mod.getDoc().createElement("div");
        innerWrapper.setAttribute("class", cmd.getCommandStyleName() + "inner" + " " + mod.getDefinitions().getOption(Definitions.REFERENCE_FORM));
        outerWrapper.appendChild(innerWrapper);
        List<indexable> usedRefs = new ArrayList<indexable>();

        Element listWrapper = mod.getDoc().createElement("ul");
        for (Module m : ModList) {
            List<IndexItem> refs = m.getReferenceHolder().getSortedIndex();

            // any refs ?
            if (refs.isEmpty()) {
                continue;
            }

            for (int rix = 0; rix < refs.size(); rix++) {
                String ids = refs.get(rix).getId();
                SimpleReference r = mod.getDefinitions().getReference(ids);
                if (usedRefs.contains(r)) {
                    continue;
                }
                usedRefs.add(r);
            }
        }
        // sort ?
        Collections.sort(usedRefs, new Comparator<indexable>() {

            @Override
            public int compare(indexable t, indexable t1) {
                return t.getCompareValue().compareToIgnoreCase(t1.getCompareValue());
            }
        });

        for (indexable ix : usedRefs) {
            listWrapper.appendChild(ix.produceListElement(mod, cmd, null));
        }

        innerWrapper.appendChild(listWrapper);
        df.appendChild(outerWrapper);
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
    private DocumentFragment makeModuleSortedReferenceList(Module mod, PIcommand cmd) {
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        // all modules are candidates
        List<Module> ModList = mod.getScriptHandler().getAllModules(null);
        //String style=mod.getDefinitions().getOption(Definitions.REFERENCE_FORM);
        int cols = 1;
        if (cmd.paramExist(PIcommand.COLS)) {
            cols = Math.max(cmd.getValueAsInteger(PIcommand.COLS), 1);
        }
        Element outerWrapper = mod.getDoc().createElement("div");
        if (cols > 0) {
            outerWrapper.setAttribute("class", cmd.getCommandStyleName() + String.valueOf(cols));
        } else {
            outerWrapper.setAttribute("class", cmd.getCommandStyleName());
        }
        Element innerWrapper = mod.getDoc().createElement("div");
        innerWrapper.setAttribute("class", cmd.getCommandStyleName() + "inner" + " " + mod.getDefinitions().getOption(Definitions.REFERENCE_FORM));
        outerWrapper.appendChild(innerWrapper);

        for (Module m : ModList) {
            List<IndexItem> refs = m.getReferenceHolder().getSortedIndex();

            // any refs ?
            if (refs.isEmpty()) {
                continue;
            }

            // refereenc from mod to m
            Element e = mod.makeXLink(m, "div", PIcommand.WXTSTYLEPREFIX + PIcommand.XLINK, -999, true);

            innerWrapper.appendChild(e);

            Element listWrapper = mod.getDoc().createElement("ol");
            for (int rix = 0; rix < refs.size(); rix++) {
                String ids = refs.get(rix).getId();
                SimpleReference r = mod.getDefinitions().getReference(ids);
                listWrapper.appendChild(r.produceListElement(mod, cmd, null));
            }
            innerWrapper.appendChild(listWrapper);
        }
        df.appendChild(outerWrapper);
        return df;
    }

    /**
     * Inserts a referencelist 
     * 
     * @param mod The module requesting the referencelist 
     * @param cmd The PI-command describing the reference-request 
     * @return The resulting nodecollection in a fragment 
     */
    public DocumentFragment makePI_ReferenceList(Module mod, PIcommand cmd) {
        //PI control: if SCOPE-> GLOBAL or LOCAL

        DocumentFragment df = mod.getDoc().createDocumentFragment();
        boolean is_local = true;
        if (cmd.paramExist(PIcommand.SCOPE)) {
            is_local = cmd.getValue(PIcommand.SCOPE).compareTo(PIcommand.LOCAL) == 0;
        }

        boolean is_compact = false;
        if (cmd.paramExist(PIcommand.COMPACT)) {
            is_compact = cmd.getValue(PIcommand.COMPACT).compareTo(PIcommand.YES) == 0;
        }

        //NEW:
        // if we have local indexing and want a global, compact reference list
        if (is_compact) {
            df = makeCompactSortedReferenceList(mod, cmd);

            if (!df.hasChildNodes()) {
                df.appendChild(makeEmptyComment(mod, cmd));
            }

            return df;
        }
        // END NEW


        // if we have local indexing and this asks for
        // a global index, we sort indexing on modules
        if ((mod.getDefinitions().getOption(Definitions.REFERENCE_INDEXING).compareTo(Definitions.LOCAL) == 0)
                && (!is_local)) {
            df = makeModuleSortedReferenceList(mod, cmd);

            if (!df.hasChildNodes()) {
                df.appendChild(makeEmptyComment(mod, cmd));
            }

            return df;
        }


        //-----------------

        // what type of reference is used       
        String refType = mod.getDefinitions().getOption(Definitions.REFERENCE_FORM);

        // prepare HTML wrapper
        int cols = 1;
        if (cmd.paramExist(PIcommand.COLS)) {
            cols = Math.max(cmd.getValueAsInteger(PIcommand.COLS), 1);
        }

        Element outerWrapper = mod.getDoc().createElement("div");
        if (cols > 0) {
            outerWrapper.setAttribute("class", cmd.getCommandStyleName() + String.valueOf(cols));
        } else {
            outerWrapper.setAttribute("class", cmd.getCommandStyleName());
        }

        Element innerWrapper = mod.getDoc().createElement("div");
        innerWrapper.setAttribute("class", cmd.getCommandStyleName() + "inner" + " " + refType);
        outerWrapper.appendChild(innerWrapper);


        // we pick up the actual list as either global or local
        List<IndexItem> refs = new ArrayList<IndexItem>();
        if (is_local) {
            refs = mod.getReferenceHolder().getSortedIndex();
        } else {
            refs = mod.getDefinitions().getReferenceHolder().getSortedIndex();
        }

        if ((refs == null) || (refs.isEmpty())) {
            df.appendChild(makeEmptyComment(mod, cmd));
            return df;

        }


        if (!is_local) {
            // global and we go for one list
            Element olElement = mod.getDoc().createElement("ol");
            olElement.setAttribute("start", String.valueOf(1));
            for (int ix = 0; ix < refs.size(); ix++) {
                String ids = refs.get(ix).getId();
                SimpleReference r = mod.getDefinitions().getReference(ids);
                olElement.appendChild(r.produceListElement(mod, cmd, null));
                innerWrapper.appendChild(olElement);
            }

        } else {
            for (int ix = 0; ix < refs.size(); ix++) {
                // local and we must have a list for each entry
                int index = ix + 1;
                String ids = refs.get(ix).getId();
                SimpleReference r = mod.getDefinitions().getReference(ids);
                if (mod.getDefinitions().getOption(Definitions.REFERENCE_INDEXING).compareTo(Definitions.GLOBAL) == 0) {
                    // if we are working with global referenceorder
                    // we must find out where this ref is placed in the global referenceIndexHolder          
                    index = mod.getDefinitions().getReferenceHolder().getIndexOfItem(r) + 1;
                }
                Element olElement = mod.getDoc().createElement("ol");
                olElement.setAttribute("start", String.valueOf(index));
                olElement.appendChild(r.produceListElement(mod, cmd, null));
                innerWrapper.appendChild(olElement);
            }
        }

        // do we have anything at all ?
        if (outerWrapper.hasChildNodes()) {
            df.appendChild(outerWrapper);
        } else {
            df.appendChild(makeEmptyComment(mod, cmd));
        }

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
    public DocumentFragment makePI_ReferenceTest(Module mod, PIcommand cmd) {
        HashMap<String, SimpleReference> allrefs = mod.getDefinitions().getReferences();
        DocumentFragment df = mod.getDoc().createDocumentFragment();

        Element wrapper = mod.getDoc().createElement("div");
        wrapper.setAttribute("class", cmd.getCommandStyleName());

        Set<String> keys = allrefs.keySet();
        List<String> done = new ArrayList<String>();
        for (String k : keys) {
            SimpleReference R = allrefs.get(k);
            URI theUri = null;
            String tmp = R.getURI();
            if (tmp == null) {
                continue;
            }

            if (done.contains(tmp)) {
                continue;
            }

            done.add(tmp);


            System.out.println(tmp);


            try {
                theUri = new URI(R.getURI());
            } catch (Exception ex) {
                mod.getReporter().pushMessage("bad_uri_in_reference", R.getid(), tmp);
                continue;
            }
            int result = -999;
            try {
                HttpURLConnection uc = (HttpURLConnection) theUri.toURL().openConnection();
                result = uc.getResponseCode();
                uc.disconnect();
            } catch (Exception ex) {
                mod.getReporter().pushMessage("bad_uri_in_reference", R.getid(), tmp);
                continue;
            }
            Element divElt = mod.getDoc().createElement("div");
            Element spanElt = mod.getDoc().createElement("span");
            if (result == 200) {
                spanElt.setAttribute("style", "color:gray");
            } else {
                spanElt.setAttribute("style", "color:red");
            }
            spanElt.appendChild(mod.getDoc().createTextNode("" + result));
            divElt.appendChild(spanElt);

            divElt.appendChild(mod.getDoc().createTextNode(" : " + R.getid() + " : "));

            Element aElt = mod.getDoc().createElement("a");
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
     * defined in the script or one of the standardfragments
     * 
     * @param mod The Module requesting the content
     * @param cmd The PIcommand describing the request
     * @return a DocumentFragment, always
     */
    public DocumentFragment makePI_Fragment(Module mod, PIcommand cmd) {
        // PI control:id exists and module fragments are correct
        // id, form
        DocumentFragment df = mod.getDoc().createDocumentFragment();

        // we have an id since the cmd is controlled
        String id = cmd.getValue(PIcommand.ID);

        //Fragment frag = mod.getDefinitions().getFragments().get(id);
        Fragment frag = mod.getDefinitions().getFragment(id);
        // this is not an attempt to find mod's author, see belowe
        if (frag == null) //frag =mod.getDefinitions().getAuthors().get(id);
        {
            frag = mod.getDefinitions().getAuthor(id);
        }

        if (frag != null) {
            df = frag.produceIntextElement(mod, cmd);
            correctAddressing(df, mod, frag.getReferenceUri());
            return df;

        } else {
            // the requsted fragment (by id) is not found in 
            // Defs fragmet or author storage
            // it must be a fragment based on the requesting modules attributes
            String val = "";
            if (id.compareToIgnoreCase(PIcommand.DESCRIPTION) == 0) {
                df.appendChild(mod.getDoc().createTextNode(mod.getDescription()));
                return df;
            }
            if (id.compareToIgnoreCase(PIcommand.NAME) == 0) {
                df.appendChild(mod.getDoc().createTextNode(mod.getName()));
                return df;
            }
            if (id.compareToIgnoreCase(PIcommand.FID) == 0) {
                df.appendChild(mod.getDoc().createTextNode(mod.getID()));
                return df;
            }
            if (id.compareToIgnoreCase(PIcommand.SUMMARY) == 0) {
                if (mod.getSummary() != null) {
                    DocumentFragment tmpdf = mod.getSummary().getFragment();
                    if (tmpdf != null) {
                        return (DocumentFragment) (mod.getDoc().importNode(tmpdf, true));
                    }
                }
                df.appendChild(mod.getDoc().createTextNode(mod.getDescription()));
                return df;
            }
            if (id.compareToIgnoreCase(PIcommand.AUTHOR) == 0) {
                Fragment au = mod.getAuthor();
                df = au.produceIntextElement(mod, cmd);
                return df;
            }
            // does not exist at all, not defined and not a module fragment
            df.appendChild(makeFailureComment(mod, cmd));
            mod.getReporter().pushMessage("unknown_fragment", id);
            return df;
        }
    }

    /**
     * Produce a "pop-up" like fragment
     * @param mod The Module requesting the content
     * @param cmd The PIcommand describing the request
     * @param piNode The processing instruction
     * @return a DocumentFragment, always
     */
    public DocumentFragment makePI_PopFragment(Module mod, PIcommand cmd, Node piNode) {
        //PI control: id exist
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        // <?_wxt popfragment id text?>


        // get the fragment
        DocumentFragment theFragmentDF = null;
        String fragId = null;

        // this is aleady controlled in PIcommand
        if (!cmd.paramExist(PIcommand.ID)) {
            // should not happen
            df.appendChild(makeFailureComment(mod, cmd));
            mod.getReporter().pushMessage("no_fragment_produced_from", cmd.toString());
            df.appendChild(wxtError.makeErrorMsgForDebugging(mod, cmd, "missing id in producer:PopFragment"));
            //df.appendChild(mod.getDoc().createTextNode("missing id"));
            return df;
        }


        // ok, we have an id
        fragId = cmd.getValue(PIcommand.ID);

        // decide text
        String visibleTxt = fragId;
        if (cmd.paramExist(PIcommand.TEXT)) {
            visibleTxt = cmd.getValue(PIcommand.TEXT);
        }

        //Fragment frag = mod.getDefinitions().getFragments().get(fragId);
        Fragment frag = mod.getDefinitions().getFragment(fragId);
        if (frag != null) {
            DocumentFragment tmp = frag.getFragment();
            if (tmp != null) {
                correctAddressing(tmp, mod, mod.getScriptHandler().getScriptAbsoluteUri());
                theFragmentDF = (DocumentFragment) (mod.getDoc().importNode(tmp, true));
            } else {
                mod.getReporter().pushMessage("no_fragment_produced_from", cmd.toString());
                df.appendChild(mod.getDoc().createTextNode(visibleTxt));
                return df;
            }
        }
        if (theFragmentDF == null) {
            mod.getReporter().pushMessage("no_fragment_produced_from", cmd.toString());
            df.appendChild(mod.getDoc().createTextNode(visibleTxt));
            return df;
        }

        // get a unique id
        String eltId = mod.getScriptHandler().getANewId(mod.getID());

        // this is the element that should replace the PI
        Element spanElt = mod.getDoc().createElement("span");
        spanElt.setAttribute("onmouseout", "javascript:popunpop('" + eltId + "',event);");
        spanElt.setAttribute("onmouseover", "javascript:popunpop('" + eltId + "',event);");
        spanElt.setAttribute("class", cmd.getCommandStyleName());
        spanElt.appendChild(mod.getDoc().createTextNode(visibleTxt));


        // this is the element that should pop        
        Element fragWrapper = mod.getDoc().createElement("div");
        fragWrapper.setAttribute("class", PIcommand.WXTSTYLEPREFIX + "popsummary");
        fragWrapper.setAttribute("style", "display: none;");
        fragWrapper.setAttribute("id", eltId);
        fragWrapper.appendChild(mod.getDoc().importNode(theFragmentDF, true));

        //place the popstuff
        piNode.getParentNode().getParentNode().insertBefore(fragWrapper, piNode.getParentNode().getNextSibling());

        // return the trigger
        df.appendChild(spanElt);
        return df;
    }

    /**
     * Produce an external reference with screen and print display option.
     * 
     * @param mod The Module requesting the content
     * @param cmd The PIcommand describing the request
     * @return a DocumentFragment, always
     */
    public DocumentFragment makePI_DemoLink(Module mod, PIcommand cmd) {
        //PI control: uri or location exists
        // uri (or location)
        // text, style, title,target
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        // we need url/location and may have text, style, title and target
        String uri = cmd.getValue(PIcommand.URI);
        if (uri == null) {
            uri = cmd.getValue(PIcommand.LOCATION);
        }
        if (uri == null) {
            // should not happen
            df.appendChild(wxtError.makeErrorMsgForDebugging(mod, cmd, "missing location/uri in producer:demolink"));
            //df.appendChild(makeFailureComment(mod.getDoc(), cmd));
            mod.getReporter().pushMessage("missing_URI", cmd.toString());
            return df;
        }

        uri = mod.getDefinitions().substituteFragments(uri);

        String txt = null;
        if (cmd.paramExist(PIcommand.TEXT)) {
            txt = cmd.getValue(PIcommand.TEXT);
        }

        String style = "demo";
        if (cmd.paramExist(PIcommand.STYLE)) {
            style = cmd.getValue(PIcommand.STYLE);
        }

        String title = "external link";
        if (cmd.paramExist(PIcommand.TITLE)) {
            title = cmd.getValue(PIcommand.TITLE);
        }

        String target = null;
        if (cmd.paramExist(PIcommand.TARGET)) {
            title = cmd.getValue(PIcommand.TARGET);
        }

        // calculate absolute uri for the referenced page
        // uri is relative uri
        String absUriStr = uri;
        String absmodpub = mod.getAbsolutePublishAddress();
        if (absmodpub != null) {
            try {
                absUriStr = accessutils.makeAbsoluteURI(uri, absmodpub).toString();
            } catch (Exception ex) {
            }
        }


        Element aelt = mod.getDoc().createElement("a");
        aelt.setAttribute("href", uri);
        aelt.setAttribute("title", title);
        aelt.setAttribute("class", style);
        if (target != null) {
            aelt.setAttribute("target", target);
        }

        Element t_elt = mod.getDoc().createElement("span");
        t_elt.setAttribute("class", "screen");
        if (txt != null) {
            t_elt.appendChild(mod.getDoc().createTextNode(txt));
        } else {
            t_elt.appendChild(mod.getDoc().createTextNode(uri));
        }
        aelt.appendChild(t_elt);

        Element l_elt = mod.getDoc().createElement("span");
        l_elt.setAttribute("class", "print");
        if (txt == null) {
            txt = "";
        }
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
    public DocumentFragment makePI_XLink(Module mod, PIcommand cmd) {
        //PI control: ID exists
        //            if SUMMARY -> yes or no
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        String mid = null;
        DocumentFragment showFrag = null;

        if (cmd.paramExist(PIcommand.ID)) {
            mid = cmd.getValue(PIcommand.ID);
        }
        if (mid == null) {
            // should not happen
            df.appendChild(wxtError.makeErrorMsgForDebugging(mod, cmd, "missing id value i producer:XLink"));
            //df.appendChild(makeFailureComment(mod.getDoc(), cmd));
            mod.getReporter().pushMessage("missing_id", cmd.toString());
            return df;
        }

        Module refMod = null;
        List<Module> mList = mod.getScriptHandler().getAllModules(null);
        int modix = mList.indexOf(mod);


        if (mid.compareTo(PIcommand.NEXT) == 0) {
            refMod = null;
            int ix = modix + 1;
            while ((ix < mList.size()) && (!mList.get(ix).getLinkable())) {
                ix++;
            }
            if (ix < mList.size()) {
                refMod = mList.get(ix);
            }

            if (refMod == null) {
                refMod = mod;
            }

        } else if (mid.compareTo(PIcommand.PREV) == 0) {
            refMod = null;
            int ix = modix - 1;
            while ((ix >= 0) && (!mList.get(ix).getLinkable())) {
                ix--;
            }
            if (ix >= 0) {
                refMod = mList.get(ix);
            }
            if (refMod == null) {
                refMod = mod;
            }

        } else if (mid.compareTo(PIcommand.PARENT) == 0) {
            if (mod.getParent() != null) {
                refMod = (Module) mod.getParent();
            } else {
                refMod = mod;
            }
        } else if (mid.compareTo(PIcommand.NEXTSIBLING) == 0) {
            refMod = mod;
            do {
                refMod = (Module) mod.getNextSibling();
            } while ((refMod != null) && (!refMod.getLinkable()));

            if (refMod == null) {
                refMod = mod;
            }

        } else if (mid.compareTo(PIcommand.PREVSIBLING) == 0) {
            refMod = mod;
            do {
                refMod = (Module) mod.getPreviousSibling();
            } while ((refMod != null) && (!refMod.getLinkable()));

            if (refMod == null) {
                refMod = mod;
            }

        } else if (mid.compareTo(PIcommand.HOME) == 0) {
            refMod = mod.getScriptHandler().getFirstRootModule();
        } else if (mid.compareTo(PIcommand.VROOT) == 0) {
            if (mod.getParent() == null) {
                refMod = mod;
            } else {
                refMod = (Module) mod.getRoot();
            }
        } else {
            refMod = mod.getScriptHandler().getModuleById(mid);
        }


        if (refMod == null) {
            df.appendChild(makeFailureComment(mod, cmd));
            mod.getReporter().pushMessage("cannot_find_reffered_module", cmd.toString());
            return df;
        }

        // can have summary and fragment
        // a summary will pop summary on mouseover
        // a fragment will replace modules name in link. The fragment may be _summary

        boolean bSummary = ((cmd.paramExist(PIcommand.LSUMMARY)
                && (cmd.getValue(PIcommand.LSUMMARY).compareToIgnoreCase(PIcommand.YES) == 0)));

        if (cmd.paramExist(PIcommand.FRAGMENT)) {
            String fragKey = cmd.getValue(PIcommand.FRAGMENT);
            if (fragKey.compareToIgnoreCase(PIcommand.SUMMARY) == 0) {
                showFrag = mod.getDoc().createDocumentFragment();
                DocumentFragment tmp = refMod.getSummary().getFragment();
                showFrag.appendChild(mod.getDoc().importNode(tmp, true));
                correctAddressing(showFrag, mod, mod.getScriptHandler().getScriptAbsoluteUri());
            } else {
                //if(mod.getDefinitions().getFragments().containsKey(fragKey))
                if (mod.getDefinitions().getFragment(fragKey) != null) {
                    showFrag = mod.getDoc().createDocumentFragment();
                    //showFrag.appendChild(mod.getDoc().importNode(mod.getDefinitions().getFragments().get(fragKey).getFragment(),true));
                    showFrag.appendChild(mod.getDoc().importNode(mod.getDefinitions().getFragment(fragKey).getFragment(), true));
                    correctAddressing(showFrag, mod, mod.getScriptHandler().getScriptAbsoluteUri());
                } else {
                    mod.getReporter().pushMessage("fragment_does_not_exist", fragKey);
                }
            }
        }

        if (cmd.paramExist(PIcommand.LSUMMARY)) {
            if (cmd.getValue(PIcommand.LSUMMARY).compareToIgnoreCase(PIcommand.YES) == 0) {
                bSummary = true;
            }

        }

        //Element e = makeXLink(mod, refMod, "span", cmd.getCommandStyleName(), NO_LEVEL_ADJUST,bSummary);
        Element e = mod.makeXLink(refMod, "span", cmd.getCommandStyleName(), -999, bSummary);

        // give separate class to self
        if (mod == refMod) {
            if (e.hasAttribute("class")) {
                e.setAttribute("class", e.getAttribute("class") + "self");
            }
        }


        if (showFrag != null) {
            if (mod == refMod) {
                e.replaceChild(showFrag, e.getFirstChild());
            } else {
                e.getFirstChild().replaceChild(showFrag, e.getFirstChild().getFirstChild());
            }
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
    public DocumentFragment makePI_ModuleMap(Module mod, PIcommand cmd) {
        // PI control: if SUMMARY->yes or no
        //             if SELECT -> SIBLINGS or CHILDREN;
        //             COLs is nonegative integer

        DocumentFragment df = mod.getDoc().createDocumentFragment();

        List<Module> moduleList = null;
        moduleList = getModuleList(mod, cmd);
        if (moduleList == null || moduleList.isEmpty()) {
            df.appendChild(makeFailureComment(mod, cmd));
            mod.getReporter().pushMessage("empty_module_selection", cmd.toString());
            df.appendChild(makeEmptyComment(mod, cmd));
            return df;
        }
        // how many cols
        int cols = 1;
        if (cmd.paramExist(PIcommand.COLS)) {
            cols = Math.max(cmd.getValueAsInteger(PIcommand.COLS), 0);
        }

        // can have summary and fragment
        // a summary will pop summary on mouseover
        // a fragment will replace modules name in link. The fragment may be _summary

        boolean bSummary = ((cmd.paramExist(PIcommand.LSUMMARY)
                && (cmd.getValue(PIcommand.LSUMMARY).compareToIgnoreCase(PIcommand.YES) == 0)));


        // wrap links so we can display them in cols
        List<Element> eList = new ArrayList<Element>();

        for (Module refMod : moduleList) {
            // fragment for each since it may be summary
            DocumentFragment showFrag = null;
            if (cmd.paramExist(PIcommand.FRAGMENT)) {
                String fragKey = cmd.getValue(PIcommand.FRAGMENT);
                if (fragKey.compareToIgnoreCase(PIcommand.SUMMARY) == 0) {
                    showFrag = mod.getDoc().createDocumentFragment();
                    DocumentFragment tmp = refMod.getSummary().getFragment();
                    showFrag.appendChild(mod.getDoc().importNode(tmp, true));
                    correctAddressing(showFrag, mod, mod.getScriptHandler().getScriptAbsoluteUri());
                    //bSummary=true;
                } else {
                    //if(mod.getDefinitions().getFragments().containsKey(fragKey))
                    if (mod.getDefinitions().getFragment(fragKey) != null) {
                        showFrag = mod.getDoc().createDocumentFragment();
                        //showFrag.appendChild(mod.getDoc().importNode(mod.getDefinitions().getFragments().get(fragKey).getFragment(),true));
                        showFrag.appendChild(mod.getDoc().importNode(mod.getDefinitions().getFragment(fragKey).getFragment(), true));
                        correctAddressing(showFrag, mod, mod.getScriptHandler().getScriptAbsoluteUri());
                    } else {
                        mod.getReporter().pushMessage("fragment_does_not_exist", fragKey);
                    }
                }
            }
            Element e;
            if (cols == 0) {
                //e = makeXLink(mod, refMod, "span", "level", 0,bSummary);
                e = mod.makeXLink(refMod, "span", "level", 0, bSummary);
            } else {
                //e = makeXLink(mod, refMod, "div", "level", 0,bSummary); 
                e = mod.makeXLink(refMod, "div", "level", 0, bSummary);
            }
            // insert fragment for name
            if (showFrag != null) {
                if (mod == refMod) {
                    e.replaceChild(showFrag, e.getFirstChild());
                } else {
                    e.getFirstChild().replaceChild(showFrag, e.getFirstChild().getFirstChild());
                }
            }

            // wrap it
            eList.add(e);

        }
        String divider = " | ";
        if (cmd.paramExist(PIcommand.DIVIDER)) {
            divider = cmd.getValue(PIcommand.DIVIDER);
        }


        Element inner = displayElementsInColumns(mod.getDoc(), eList, cols, divider);
        inner.setAttribute("class", cmd.getCommandStyleName() + "inner");

        Element outer = null;
        if (cols > 0) {
            outer = mod.getDoc().createElement("div");
            outer.setAttribute("class", cmd.getCommandStyleName() + String.valueOf(cols));
        } else {
            outer = mod.getDoc().createElement("span");
            outer.setAttribute("class", cmd.getCommandStyleName());
        }

        outer.appendChild(inner);

        df.appendChild(outer);
        return df;


    }

    /**
     * Produce a simple path from top to this module.
     * 
     * @param mod The module which has requested this path
     * @param cmd The PIcommand that describes the request
     * @return A DocumentFragment containg the links
     */
    public DocumentFragment makePI_Path(Module mod, PIcommand cmd) {
        //PI control: none
        DocumentFragment df = mod.getDoc().createDocumentFragment();

        String divider = " | ";
        if (cmd.paramExist(PIcommand.DIVIDER)) {
            divider = cmd.getValue(PIcommand.DIVIDER);
        }

        TreeNode[] plist = mod.getPath();
        if (plist.length < 2) // drop it if it is empty or only one long
        {
            df.appendChild(makeEmptyComment(mod, cmd));
            return df;
        }
        Element wrapper = mod.getDoc().createElement("span");
        wrapper.setAttribute("class", cmd.getCommandStyleName());
        for (int ix = 0; ix < plist.length; ix++) {
            Module m = (Module) plist[ix];
            //Element e = makeXLink(mod, m, "span", "level", plist.length-2,false);
            Element e = mod.makeXLink(m, "span", "level", plist.length - 2, false);
            wrapper.appendChild(e);
            wrapper.appendChild(mod.getDoc().createTextNode(divider));
        }
        wrapper.removeChild(wrapper.getLastChild());
        if (wrapper.hasChildNodes()) {
            df.appendChild(wrapper);
            return df;
        } else {
            df.appendChild(makeEmptyComment(mod, cmd));
            return df;
        }
    }

    /**
     * Produce a simple list of all authors
     * @param mod The module that will use the map
     * @param cmd PI describing the request
     * @return A documentfragment
     */
    public DocumentFragment makePI_AuthorSimpleList(Module mod, PIcommand cmd) {
        // PI authors
        // form, cols
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        Document doc = mod.getDoc();
        // get a list of all authors that has written anything
        List<String> AuthorIds = mod.getDefinitions().getAuthorHolder().getSortedIndexIds();

        // columns
        int cols = 1;
        if (cmd.paramExist(PIcommand.COLS)) {
            cols = Math.max(cmd.getValueAsInteger(PIcommand.COLS), 1);
        }


        Element wrapperOuter = null;
        wrapperOuter = doc.createElement("div");
        wrapperOuter.setAttribute("class", cmd.getCommandStyleName() + String.valueOf(cols));


        Element wrapperInner = doc.createElement("div");
        wrapperInner.setAttribute("class", cmd.getCommandStyleName() + "inner");
        wrapperOuter.appendChild(wrapperInner);

        // run each author select modules that the author has written
        Element ulelt = doc.createElement("ul");
        // to drop duplicates
        String last = "anything odd name";
        for (String a : AuthorIds) {
            if (a.equals(last)) {
                continue;
            }
            // we get an authorfragment
            Element liElt = mod.getDoc().createElement("li");
            //Fragment au=mod.getDefinitions().getAuthors().get(a);
            Fragment au = mod.getDefinitions().getAuthor(a);
            liElt.appendChild(au.produceIntextElement(mod, cmd));
            ulelt.appendChild(liElt);
            last = a;
        }
        wrapperInner.appendChild(ulelt);
        df.appendChild(wrapperOuter);
        return df;

    }

    /**
     * Produce a list of modules sorted on author
     * @param mod The module that will use the map
     * @param cmd PI describing the request
     * @return A documentfragment
     */
    public DocumentFragment makePI_AuthorOfModules(Module mod, PIcommand cmd) {
        // PI: authorlist
        // moduleselctors: idlist,root,books,select,scriptpath
        // authors, dropdefault
        // cols, show
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        Document doc = mod.getDoc();
        // set up the modules to choose from
        List<Module> resultList = null;
        resultList = getModuleList(mod, cmd);
        if (resultList.isEmpty()) {
            // no modules and we retun an empty df
            return df;
        }

        // should we drop default author
        boolean dropDefault = true;
        if (cmd.paramExist(PIcommand.DROPDEFAULT)) {
            dropDefault = cmd.getValue(PIcommand.DROPDEFAULT).compareToIgnoreCase(PIcommand.YES) == 0;
        }


        // any filter on authors
        String[] filter = null;
        if (cmd.paramExist("author")) {
            filter = cmd.getValue("authors").split(",");
        }

        // columns
        int cols = 1;
        if (cmd.paramExist(PIcommand.COLS)) {
            cols = Math.max(cmd.getValueAsInteger(PIcommand.COLS), 1);
        }

        // short
        boolean shortform = true;
        if (cmd.paramExist(PIcommand.SHOW)) {
            shortform = cmd.getValue(PIcommand.SHOW).compareToIgnoreCase(PIcommand.SHORT) == 0;
        }

        Element wrapper = null;
        if (cols > 0) {
            wrapper = doc.createElement("div");
            wrapper.setAttribute("class", cmd.getCommandStyleName() + String.valueOf(cols));
        } else {
            wrapper = doc.createElement("span");
            wrapper.setAttribute("class", cmd.getCommandStyleName() + String.valueOf(cols));
        }



        Element innerwrapper = doc.createElement("dl");
        innerwrapper.setAttribute("class", cmd.getCommandStyleName() + "inner");
        wrapper.appendChild(innerwrapper);


        // get all authors
        // or better: only those registered in modules
        Fragment[] authors = mod.getDefinitions().getAllSortedAuthors();

        // run each author select modules that the author has written
        for (int ix = 0; ix < authors.length; ix++) {
            Fragment auf = authors[ix];
            if (!(auf instanceof AuthorFragment)) {
                continue;
            }

            AuthorFragment au = (AuthorFragment) auf;
            // skip the authors that are not selected
            if (filter != null) {
                // there is a selection
                boolean useit = false;
                for (int ixx = 0; ixx < filter.length; ixx++) {
                    if (au.getId().compareTo(filter[ixx]) == 0) {
                        useit = true;
                    }
                }
                if (!useit) {
                    continue;
                }
            }
            // if drop default is set
            if (dropDefault && (au.getId().compareTo(Definitions.DEFAULTAUTHOR) == 0)) {
                continue;
            }

            // has this author written any of the selected modules
            List<IndexItem> mUsing = mod.getDefinitions().getAuthorHolder().getAllItemsUsingID(au.getId());
            for (int mix = mUsing.size() - 1; mix >= 0; mix--) {
                if (!resultList.contains(mUsing.get(mix).getMod())) {
                    mUsing.remove(mix);
                }
            }
            if (mUsing.isEmpty()) {
                continue;
            }

            DocumentFragment oneAuthorFrag = au.produceListElement(mod, cmd, mUsing);
            innerwrapper.appendChild(oneAuthorFrag);
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
    public DocumentFragment makePI_ModuleToc(Module mod, PIcommand cmd) {
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
        List<Element> elt_list = domer.getElementsPreorder(doc.getDocumentElement());
        List<Element> eList = new ArrayList<Element>();

        // we must do som effort to make sure that the refed names are unique
        for (Element targetElt : elt_list) {
            String eltname = targetElt.getTagName();
            int eltnamepos = accessutils.indexOfNameInList(eltname, tag_list);
            if (eltnamepos != -1) {
                targetElt.normalize();
                // it is in the taglist so we use it
                // we must establish an element that links to this element, and
                // we must mark this element with an anchor (name)                
                String originalrefname = targetElt.getTextContent().trim();
                // we must find out if this target already contains
                // an id tag. If this is so we reuse it.
                String idName = "";
                if (targetElt.hasAttribute("id")) {
                    idName = targetElt.getAttribute("id");
                } else {
                    // we must produce it and set it
                    idName = mod.getScriptHandler().getANewId(mod.getID());
                    targetElt.setAttribute("id", idName);
                }

                // ok we are ready for the referer
                // make the referer
                Element spanElt = doc.createElement("span");
                spanElt.setAttribute("class", "level" + (eltnamepos + 1));
                Element refElt = doc.createElement("a");
                refElt.setAttribute("href", "#" + idName);
                refElt.appendChild(doc.createTextNode(originalrefname));
                spanElt.appendChild(refElt);

                eList.add(spanElt);
            }
        }
        // we have all the prepared tocEntries in entrylist
        int cols = 0;
        if (cmd.paramExist(PIcommand.COLS)) {
            cols = Math.max(cmd.getValueAsInteger(PIcommand.COLS), 0);
        }

        String divider = " | ";
        if (cmd.paramExist(PIcommand.DIVIDER)) {
            divider = cmd.getValue(PIcommand.DIVIDER);
        }


        if (!eList.isEmpty()) {
            Element inner = displayElementsInColumns(mod.getDoc(), eList, cols, divider);
            inner.setAttribute("class", cmd.getCommandStyleName() + "inner");
            Element outer = null;
            if (cols > 0) {
                outer = mod.getDoc().createElement("div");
                outer.setAttribute("class", cmd.getCommandStyleName() + String.valueOf(cols));
            } else {
                outer = mod.getDoc().createElement("span");
                outer.setAttribute("class", cmd.getCommandStyleName());
            }
            outer.appendChild(inner);

            df.appendChild(outer);
        } else {
            df.appendChild(makeEmptyComment(mod, cmd));
        }

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
    public DocumentFragment makePI_ModuleTocFinal(Module mod, PIcommand cmd) {
        // PI control: COLs is a nonegtaive integer
        //clear old moduletocs
        NodeList divList = mod.getDoc().getElementsByTagName("*");
        for (int ix = 0; ix < divList.getLength(); ix++) {
            Element d = (Element) divList.item(ix);
            // note hardcoded classname:moduletoc
            // set in makePIModuleToc as: cmd.getCommand()
            String targetClassName = "moduletoc";
            if (d.hasAttribute("class")
                    && (d.getAttribute("class").compareTo(targetClassName) == 0)) {
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
    public DocumentFragment makePI_IXWord(Module mod, PIcommand cmd) {
        // PI control: WORD exist
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        // can substitue or supress the word
        // if markWord exists and markWord == _none, we leave no mark
        // if markedword exists and is not _none, we leave markword in text
        String word = "?";
        if (cmd.paramExist(PIcommand.WORD)) {
            word = cmd.getValue(PIcommand.WORD);
        }
        Word w = mod.getDefinitions().getWord(word);
        if (w == null) {
            w = new Word(mod, cmd);
            mod.getDefinitions().registerNewWord(w);
        }
        int ix = mod.getDefinitions().getWordHolder().add(w, word, mod);
        IndexItem ixItem = mod.getDefinitions().getWordHolder().getItemAt(ix);

        String cat = PIcommand.NO_CATEGORY;
        if (cmd.paramExist(PIcommand.CATEGORY)) {
            cat = cmd.getValue(PIcommand.CATEGORY);
        }
        String comment = PIcommand.NO_COMMENT;
        if (cmd.paramExist(PIcommand.COMMENT)) {
            comment = cmd.getValue(PIcommand.COMMENT);
        }

        ixItem.setCategory(cat);
        ixItem.setComment(comment);

        df.appendChild(w.produceIntextElement(mod, cmd));

        return df;
    }

    /**
     * Creating an index table. This is part of a second-phase build operation
     * 
     * @param mod The Module that request the table
     * @param cmd The command that describes the table
     * @return The table as a DocumentFragment
     */
    public DocumentFragment makePI_IXTable(Module mod, PIcommand cmd) {
        //System.out.println("Making IX-Table");
        //System.out.println(mod.getDefinitions().getWordHolder());
        // PI control if (COLS) it is a nonnegative number
        DocumentFragment df = mod.getDoc().createDocumentFragment();

        HashMap<String, Word> allW = mod.getDefinitions().getAllWords();
        Set<String> keys = allW.keySet();
        String[] skeys = keys.toArray(new String[0]);
        Arrays.sort(skeys);

        String cat = PIcommand.NO_CATEGORY;
        if (cmd.paramExist(PIcommand.CATEGORY)) {
            cat = cmd.getValue(PIcommand.CATEGORY);
        }


        List<Element> wordelements = new ArrayList<Element>();

        for (String word : skeys) {
            Word theWord = mod.getDefinitions().getWord(word);
            String wcat = theWord.getCategory();
            if (wcat.indexOf(cat) != -1) {
                List<IndexItem> usedIn = mod.getDefinitions().getWordHolder().getAllItemsUsingID(word);
                //Element oneWordElt=allW.get(word).produceListElement(mod, cmd, usedIn);
                //wordelements.add(oneWordElt);
                Element oneWordElt = (Element) allW.get(word).produceListElement(mod, cmd, usedIn).getFirstChild();
                wordelements.add(oneWordElt);
            }
        }

        if (wordelements.isEmpty()) {
            mod.getReporter().pushMessage("empty_index_table");
            df.appendChild(makeEmptyComment(mod, cmd));
            return df;
        }


        // set columns we want in result
        int cols = 1;
        if (cmd.paramExist(PIcommand.COLS)) {
            cols = Math.max(cmd.getValueAsInteger(PIcommand.COLS), 1);
        }



        String divider = "  ";
        // take away
        //if(cmd.paramExist(PIcommand.DIVIDER))
        //    divider=cmd.getValue(PIcommand.DIVIDER);

        Element inner = displayElementsInColumns(mod.getDoc(), wordelements, cols, divider);
        inner.setAttribute("class", cmd.getCommandStyleName() + "inner");

        Element outer = mod.getDoc().createElement("div");
        outer.setAttribute("class", cmd.getCommandStyleName() + cols);

        outer.appendChild(inner);
        df.appendChild(outer);
        return df;
    }

    /**
     * Collect material from a set of modules
     * 
     * @param mod The Module that request the collection
     * @param cmd The PICommand that describes the list of modules and the collection
     * @return A documentFragment
     */
    public DocumentFragment makePI_Collect(Module mod, PIcommand cmd) {
        // PI control : xpath exists
        // moduleselectors
        // xpath
        DocumentFragment df = mod.getDoc().createDocumentFragment();

        // pick up a list of the modules we want to collect from
        List<Module> mods = getModuleList(mod, cmd);
        if (mods == null) {
            df.appendChild(makeEmptyComment(mod, cmd));
            return df;
        }
        // we have a list of modules to collect from
        // we must have an xpath to select with from all modules
        if (!cmd.paramExist(PIcommand.XPATH)) {
            // should no happen
            df.appendChild(makeFailureComment(mod, cmd));
            mod.getReporter().pushMessage("missing_xpath_in_command", cmd.toString());
            return df;
        }
        String xp = cmd.getValue(PIcommand.XPATH);

        // do the collection
        Element wrapper = mod.getDoc().createElement("div");
        wrapper.setAttribute("class", cmd.getCommandStyleName());

        for (Module m : mods) {

            // drop self
            if (m.equals(mod)) {
                continue;
            }

            if (m.getDoc() == null) // if not built, build it
            {
                m.build();
            }

            if (m.getDoc() == null) {
                mod.getReporter().pushMessage("cannot_establish_dom_in_collect", m.getName());
            } else {
                try {
                    NodeList nlist = domer.performXPathQuery(m.getDoc(), xp);
                    if (nlist.getLength() != 0) {
                        DocumentFragment tmpDF = mod.getDoc().createDocumentFragment();
                        for (int ix = 0; ix < nlist.getLength(); ix++) {
                            tmpDF.appendChild(mod.getDoc().importNode(nlist.item(ix), true));
                        }
                        correctAddressing(tmpDF, mod, m.getAbsoluteUri());
                        wrapper.appendChild(tmpDF);
                    }
                } catch (Exception e) {
                    mod.getReporter().pushMessage("cannot_collect_from", mod.getName());
                }
            }
        }
        if (!wrapper.hasChildNodes()) {
            wrapper.appendChild(makeEmptyComment(mod, cmd));
        }
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
    public DocumentFragment makePI_CollectRemote(Module mod, PIcommand cmd) {
        // PI control : location exists
        //              one or more of SCRIPTPATH,IDLIST,BOOKS
        // location, moduleselectors
        // xpath, encoding
        DocumentFragment df = mod.getDoc().createDocumentFragment();

        // location, already checked and reported
        String scriptLocation = cmd.getValue(PIcommand.LOCATION);
        if (scriptLocation == null) {
            //should not happen
            df.appendChild(wxtError.makeErrorMsgForDebugging(mod, cmd, "missing script location in producer:CollectRemote"));
            //df.appendChild(makeFailureComment(mod.getDoc(), cmd));
            return df;
        }
        // xpath, already checked and reported
        String theExpath = cmd.getValue(PIcommand.XPATH);
        if (theExpath == null) {
            //should not happen
            df.appendChild(wxtError.makeErrorMsgForDebugging(mod, cmd, "missing xpath in producer:CollectRemote"));
            //df.appendChild(makeFailureComment(mod.getDoc(), cmd));
            return df;
        }


        // encoding
        String theEncoding = mod.getEncoding();
        if (cmd.paramExist(PIcommand.ENCODING)) {
            theEncoding = cmd.getValue(PIcommand.ENCODING);
        }


        // establish remote site holder
        RemoteSite theSite = null;
        try {
            theSite = new RemoteSite(scriptLocation);
        } catch (Exception e2) {
            mod.getReporter().pushSimpleMessage("\t" + e2.getMessage() + " in: " + scriptLocation);
            df.appendChild(makeFailureComment(mod, cmd));
            return df;
        }
        // should we correct encoding according to
        // the default encoding in the remote script
        if (theSite.getEncoding() != null) {
            theEncoding = theSite.getEncoding();
        }

        // select modules according to command attributes
        List<Element> actualModules = theSite.getSelectedModuleList(cmd);
        if (actualModules.isEmpty()) {
            mod.getReporter().pushSimpleMessage("no modules selected");
            df.appendChild(makeEmptyComment(mod, cmd));
            return df;
        }

        // a innerwrapper to hold and move what we select
        Element wrapper = mod.getDoc().createElement("div");

        // loop modules, now selected and in correct order
        for (int wix = 0; wix < actualModules.size(); wix++) {
            Element e = actualModules.get(wix);
            URI theURI = null;
            // the URI for the remote module we want to import from
            Document remoteDoc = null;
            try {
                theURI = new URI(e.getAttribute(PIcommand.LOCATION));
            } catch (Exception uriex) {
                continue;
            }
            try {
                remoteDoc = domer.makeDomFromUriSomeHow(theURI, theEncoding, mod.getReporter());
            } catch (Exception domex3) {
                mod.getReporter().pushSimpleMessage("\tcannot_parse: " + theURI.toString());
                continue;
            }

            //---------------
            // we have the DOM, this is where we should do the job of correcting
            // a href's so we only have absolutes, alive or dead depending of
            // innerwrapper, like demolink, xlink, ref etc
            // and images ??
            theSite.reAddress(remoteDoc, e.getAttribute("location"));


            //----------------
            // ok we go for an import with the given xpath
            try {
                NodeList reslist = domer.performXPathQuery(remoteDoc, theExpath);
                // add the result to the innerwrapper
                for (int rix = 0; rix < reslist.getLength(); rix++) {
                    wrapper.appendChild(mod.getDoc().importNode(reslist.item(rix), true));
                }
            } catch (Exception expex) {
                mod.getReporter().pushSimpleMessage("\tcannot_access: " + theExpath);
            }
        }
        // add innerwrapper to df
        if (wrapper.hasChildNodes()) {
            df.appendChild(wrapper);
        } else {
            df.appendChild(makeEmptyComment(mod, cmd));
        }
        return df;
    }

    /**
     * Produce a list of summaries for selected modules
     * 
     * @param mod The Module that request the popup
     * @param cmd The PICommand that describes the list of modules and the collection
     * @return A documentFragment
     */
    public DocumentFragment makePI_CollectSummaries(Module mod, PIcommand cmd) {
        //PI control:if xlink->yes or no
        //           if select->siblings or children
        // moduleselectors
        // xlink
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        // pick up a list of the modules we want to collect from
        List<Module> mods = getModuleList(mod, cmd);
        if (mods == null) {
            df.appendChild(makeEmptyComment(mod, cmd));
            mod.getReporter().pushMessage("empty_module_selection", cmd.toString());
            return df;
        }
        // we have a list of modules to collect summaries from
        // do we want to include a link to the module:
        boolean makeLink = (cmd.paramExist(PIcommand.XLINK)
                && (cmd.getValue(PIcommand.XLINK).compareToIgnoreCase(PIcommand.YES) == 0));
        for (Module m : mods) {
            // drop self ?
            Element elt = mod.getDoc().createElement("div");
            elt.setAttribute("class", cmd.getCommandStyleName());

            Element heading = null;
            if (!makeLink) {
                heading = mod.getDoc().createElement("div");
                heading.appendChild(mod.getDoc().createTextNode(m.getName()));
            } else {
                //heading = makeXLink(mod, m, "div", "summarylink", NO_LEVEL_ADJUST,false);
                heading = mod.makeXLink(m, "div", "summarylink", -999, false);
            }
            heading.setAttribute("class", cmd.getCommandStyleName() + "heading");

            Element content = mod.getDoc().createElement("div");
            content.setAttribute("class", cmd.getCommandStyleName() + "content");

            try {
                DocumentFragment tmp = m.getSummary().getFragment();
                DocumentFragment mdf = (DocumentFragment) mod.getDoc().importNode(tmp, true);
                correctAddressing(mdf, mod, m.getAbsoluteUri());
                content.appendChild(mdf);

            } catch (Exception e) {
                content.appendChild(mod.getDoc().createTextNode(reporter.getBundleString("cannot_make", cmd.getCommand())));
            }
            elt.appendChild(heading);
            elt.appendChild(content);

            df.appendChild(elt.cloneNode(true));
        }
        if (!df.hasChildNodes()) {
            df.appendChild(makeEmptyComment(mod, cmd));
        }
        return df;
    }

    /**
     * Produce a span-element with the text wxt.
     * 
     * @param mod The module that wants this
     * @param cmd The PICommand that describes the list of modules and the collection
     * @return A documentFragment
     */
    public DocumentFragment makePI_Stamp(Module mod, PIcommand cmd) {
        //PI control: none
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        //TODO: add possible logo
        Element spanElt = mod.getDoc().createElement("span");
        spanElt.setAttribute("class", cmd.getCommandStyleName());
        Element aelt = mod.getDoc().createElement("a");
        aelt.setAttribute("href", "http://www.it.hiof.no/wxt/wxtsite/");
        aelt.setAttribute("class", "external");
        String version = mod.getScriptHandler().getResourceHandler().getVersion();
        aelt.appendChild(mod.getDoc().createTextNode("WXT " + version.substring(0, 1)));

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
    public DocumentFragment makePI_DateStamp(Module mod, PIcommand cmd) {
        //PI control:if FORM ->SHORT or MEDIUM or LONG orFULL
        // actualdate yyyy:mm:dd
        // country
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
        if (cmd.paramExist(PIcommand.ACTUALDATE)) {
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
                df.appendChild(makeFailureComment(mod, cmd));
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
    public DocumentFragment makePI_TimeStamp(Module mod, PIcommand cmd) {
        // PI control:if FORM-> SHORT or MEDIUM or LONG or FULL
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
     * typically left columns menu. A single column
     * 
     * @param mod The module requesting the menu
     * @param cmd The PIcommand describing the request
     * @return A Documentfragment with the menu
     */
    public DocumentFragment makePI_ModuleMenu(Module mod, PIcommand cmd) {
        // PI control: if SUMMARY->yes or no
        //             if SELECT -> SIBLINGS or CHILDREN;
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        List<Module> resultList = null;
        resultList = getModuleList(mod, cmd);
        Module root = null;
        if (cmd.paramExist(PIcommand.ROOT)) {
            root = mod.getScriptHandler().getModuleById(cmd.getValue(PIcommand.ROOT));
        }
        if (resultList == null) {
            df.appendChild(makeFailureComment(mod, cmd));
            mod.getReporter().pushMessage("empty_module_selection", cmd.toString());
            return df;
        }
        if (resultList.isEmpty()) {
            df.appendChild(makeEmptyComment(mod, cmd));
            mod.getReporter().pushMessage("empty_module_selection", cmd.toString());
            return df;
        }

        String basicEltType = "div";
        Element rootElt = mod.getDoc().createElement("div");
        rootElt.setAttribute("class", cmd.getCommandStyleName());

        boolean bSummary = ((cmd.paramExist(PIcommand.LSUMMARY)
                && (cmd.getValue(PIcommand.LSUMMARY).compareToIgnoreCase(PIcommand.YES) == 0)));

        // reduce it, that is leave only ancestors, siblings, ancestors siblings, children and roots
        for (Module m : resultList) {
            // this is the crucial part
            if ((mod.isNodeAncestor(m))
                    || (mod.isNodeSibling(m))
                    || ((mod.getParent() != null) && (m.isNodeSibling(mod.getParent()))) || //??
                    (mod.isNodeChild(m))
                    || (mod.getScriptHandler().getRootModules().contains(m))) {
                // use it
                Element e;
                // make css class relative to root
                if (root != null) {
                    //e = makeXLink(mod, m, basicEltType, "level", root.getDepth(),bSummary);
                    e = mod.makeXLink(m, basicEltType, "level", root.getDepth(), bSummary);
                } else {
                    //e = makeXLink(mod, m, basicEltType, "level", 0,bSummary);
                    e = mod.makeXLink(m, basicEltType, "level", 0, bSummary);
                }

                rootElt.appendChild(e);
            }
        }
        df.appendChild(rootElt);
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
        // connection, sql, sqlfile,
        // targetlocation,encoding, xpath, driver
        // parse, transformation
        DocumentFragment df = mod.getDoc().createDocumentFragment();

        //--------- reuse content if possible ------------
        if (!cmd.paramExist(PIcommand.CONNECT)) {
            // should not happen
            mod.getReporter().pushMessage("bad_processing_instruction", "Missing " + PIcommand.CONNECT);
            df.appendChild(wxtError.makeErrorMsgForDebugging(mod, cmd, "missing location in Producer:XMLContent"));
            return df;
        }
        String constring = cmd.getValue(PIcommand.CONNECT);

        Content theContent = mod.getRegisteredContent(constring);
        if (theContent == null) {
            try {
                theContent = new DBContent(mod, cmd);
                mod.registerContent(constring, theContent);
            } catch (Exception ex) {
                mod.getReporter().pushMessage("bad_processing_instruction", cmd.toString(), ex.getMessage());
                df.appendChild(makeFailureComment(mod, cmd));
                return df;
            }

        }
        df = theContent.getContent(mod, cmd);
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
    public DocumentFragment makePI_FootNote(Module mod, PIcommand cmd) {
        // PI control: if FORM->(NORMAL or SHOW or REMOVE)
        // form
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        // do we simply want to remove all traces of footnotes

        String form = PIcommand.NORMAL;
        if (cmd.paramExist(PIcommand.FORM)) {
            form = cmd.getValue(PIcommand.FORM).toLowerCase();
        }
        // normal | show | remove


        // collecting footnotes
        List<Element> footsE = new ArrayList<Element>();
        // scan for actual elements: spans
        NodeList spanlist = mod.getDoc().getElementsByTagName("span");
        // run the list and look for spans with class=fn
        int counter = 1;
        for (int ix = 0; ix < spanlist.getLength(); ix++) {
            Element spElt = (Element) spanlist.item(ix);
            if ((spElt.hasAttribute("class")) && (spElt.getAttribute("class").compareToIgnoreCase("fn") == 0)) {
                if (form.compareToIgnoreCase(PIcommand.REMOVE) == 0) {
                    // insert an empty task comment
                    Comment mark = mod.getDoc().createComment("fn-removed");
                    spElt.getParentNode().replaceChild(mark, spElt);
                } else if (form.compareToIgnoreCase(PIcommand.SHOW) == 0) {
                    // display as a neutral span
                    spElt.removeAttribute("class");
                } else // normal
                {
                    footsE.add((Element) mod.getDoc().importNode(spElt, true));

                    // mark the footnote with a number
                    Element marker = mod.getDoc().createElement("span");
                    marker.setAttribute("class", PIcommand.WXTSTYLEPREFIX + "fnmarker");
                    marker.appendChild(mod.getDoc().createTextNode(String.valueOf(counter)));
                    spElt.getParentNode().insertBefore(marker, spElt);
                    ix++;

                    counter++;
                }
            }
        }
        // we have the list of footnote fragments in foots
        // we produce the documentfragment


        Element wrapper = mod.getDoc().createElement("div");
        wrapper.setAttribute("class", cmd.getCommandStyleName());

        Element listElt = mod.getDoc().createElement("ol");

        //new
        for (Element e : footsE) {
            Element oneFoot = mod.getDoc().createElement("li");
            if (e.hasAttribute("class")) {
                e.setAttribute("class", e.getAttribute("class").replace("fn", "fndisplay"));
            }
            oneFoot.appendChild(e);
            listElt.appendChild(oneFoot);
        }

        /*
        for(String t:foots)
        {
        Element oneFoot=mod.getDoc().createElement("li");
        oneFoot.appendChild(mod.getDoc().createTextNode(t));
        listElt.appendChild(oneFoot);
        }
         */

        if (!listElt.hasChildNodes()) {
            df.appendChild(makeEmptyComment(mod, cmd));
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
     * @param contentType The type of contet we want to import
     * @param mod The module which owns the content and ordered this
     * @param cmd The PI involved
     * @return The resulting nodecollection in a DocumentFragment
     */
    public DocumentFragment import_ContentToTemplate(Module mod, PIcommand cmd, String contentType) {
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        List<ScriptContent> list = mod.getScriptContent();
        String cmdId = null;
        if (cmd.paramExist(PIcommand.ID)) {
            cmdId = cmd.getValue(PIcommand.ID);
        }
        for (ScriptContent cont : list) {
            // we are looking for a certain content type
            if ((cont.getType()).compareTo(contentType) != 0) {
                continue;
            }
            // and we filter by id, if it exists
            if ((cmdId == null) || (cmdId.compareTo(cont.getId()) == 0)) {
                // use it
                if (contentType.compareTo(Module.DBCONTENT) == 0) {
                    // set connectionstring as found in script
                    cmd.setParameter(PIcommand.CONNECT, cont.getConnectionString());
                } else {   // all but db
                    // set LOCATION as found in script
                    URI loc = cont.getAbsoluteEffectiveUri();
                    if (loc == null) {
                        mod.getReporter().pushSimpleMessage("dropping: " + cmd.toString());
                        continue;
                    }
                    cmd.setParameter(PIcommand.LOCATION, loc.toString());
                }
                // Transformation in PI override transformation in script
                if ((cont.getTransformationString() != null) && (!cmd.paramExist(PIcommand.TRANSFORMATION))) {
                    cmd.setParameter(PIcommand.TRANSFORMATION, cont.getTransformationString());
                }

                // perform
                // mark this so we reckognize it in makePI_???Content
                cmd.setParameter(PIcommand.TEMPLATE, "ison");
                DocumentFragment tmpdf = null;
                if (contentType.compareTo(Module.TXTCONTENT) == 0) {
                    tmpdf = makePI_TXTContent(mod, cmd);
                } else if (contentType.compareTo(Module.XMLCONTENT) == 0) {
                    tmpdf = makePI_XMLContent(mod, cmd);
                } else if (contentType.compareTo(Module.WIKICONTENT) == 0) {
                    tmpdf = makePI_WIKIContent(mod, cmd);
                } else if (contentType.compareTo(Module.ODFCONTENT) == 0) {
                    tmpdf = makePI_ODFContent(mod, cmd);
                } else if (contentType.compareTo(Module.DBCONTENT) == 0) {
                    tmpdf = makePI_DBContent(mod, cmd);
                }
                // no readdressing

                //collect
                if (tmpdf != null) {
                    df.appendChild((DocumentFragment) mod.getDoc().importNode(tmpdf, true));
                }
            }
        }

        return df;
    }

    /**
     * Register all neutral, strait, images and associate them with a module
     * @param df The documentfragment that contains the images
     * @param mod The Module using it
     */
    private void registerAllImages(DocumentFragment df, Module mod) {
        // make and register all images

        NodeList imList = null;

        try {
            imList = domer.findAllImages(df);
        } catch (Exception xex) {
            mod.getScriptHandler().getReporter().pushSimpleMessage(xex.getMessage());
        }

        for (int ix = 0; ix < imList.getLength(); ix++)//drop dummy
        {
            Element imgElt = (Element) imList.item(ix);
            String tmpid = accessutils.cleanStringForUseAsFilePath(imgElt.getAttribute("src"));
            String relfilepath = imgElt.getAttribute("src");
            String docCat = accessutils.removeFilePartFromPathstring(mod.getAbsoluteUri().toString());

            try {
                Image theImage = new Image(docCat + "/" + relfilepath,
                        imgElt.getAttribute("width"),
                        imgElt.getAttribute("height"),
                        imgElt.getAttribute("alt"),
                        tmpid);
                mod.getDefinitions().registerNewImage(theImage);
                mod.getDefinitions().getImageHolder().add(theImage, tmpid, mod);
            } catch (Exception rex) {
                System.out.println(rex.getMessage());
            }
        }
    }
}

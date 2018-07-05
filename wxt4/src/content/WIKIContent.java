package content;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.PIcommand;
import utils.accessutils;
import utils.domer;

/**
 * Handles WIKI
 * Strategy is to:
 * make backup if something found
 * use backup anyhow
 * backup strategy may be changed:
 * backup="no" -> produce no backup, load directly
 * local="yes" -> use back, without attempting to load
 * @author borres
 */
public class WIKIContent extends Content {
    // the Dpath we will use if any

    DPath theDPath;
    // wiki dependant tags
    static final String MW_HEADLINE = "mw-headline";
    //catalog
    static final String BACK = "wikiback";

    /**
     * Constructor
     * @param owner The module owning this content
     * @param cmd The command describing the request
     * @throws Exception if we fail
     */
    public WIKIContent(Module owner, PIcommand cmd)
            throws Exception {
        super(owner, cmd);
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
        // first we check if we want to access the backup directly
        // go for the premade backup ?
        // global is set to * (neutral) by default
        boolean useBackup = (cmd.paramExist(PIcommand.USECOPY)
                && (cmd.getValue(PIcommand.USECOPY).compareTo(PIcommand.YES) == 0));
        String globalCommand = mod.getDefinitions().getOption(Definitions.USE_COPY);
        if (globalCommand.compareTo(Definitions.YES) == 0) {
            useBackup = true;
        } else if (globalCommand.compareTo(Definitions.NO) == 0) {
            useBackup = false;
        }

        // should we keep references
        boolean keepRefs = (cmd.paramExist(PIcommand.KEEPREFERENCES)
                && (cmd.getValue(PIcommand.KEEPREFERENCES).compareTo(PIcommand.YES) == 0));

        Document wDoc = null;

        if (useBackup) {
            // go for the backup directly
            wDoc = retrieveBackup(mod, cmd);
        }

        // Either we dont want the backup or it is not found/loaded
        if (wDoc == null) {
            wDoc = loadDocument(mod, cmd);
            if (wDoc == null) {
                return null;
            }

            //save the complete doc as backup, unconditionally
            makeBackup(mod, cmd, wDoc);
            // then we try again
            wDoc = retrieveBackup(mod, cmd);
        }

        // went wrong
        if (wDoc == null) {
            return Content.getFailedContent(mod, cmd);
        }

        // we have a complete raw document, unparsed and without extraction


        //clean it. This is wikirelated
        cleanDoc(wDoc, keepRefs);



        // check for dpath
        theDPath = null;
        if (cmd.paramExist(PIcommand.DPATH)) {
            String dpString = cmd.getValue(PIcommand.DPATH);
            try {
                theDPath = new DPath(dpString);
            } catch (Exception dex) {
                // we wanted dpath, but could not make it
                return Content.getFailedContent(mod, cmd);
            }
        }


        // check for xpath and use default if nothing else found
        String xpath = "//body";
        if (cmd.paramExist(PIcommand.XPATH)) {
            xpath = cmd.getValue(PIcommand.XPATH);
        }


        // wrap it
        Element tmpWrapper = mod.getDoc().createElement("div");
        tmpWrapper.setAttribute("class", cmd.getCommandStyleName());


        if (theDPath != null) {
            tmpWrapper.appendChild(theDPath.getExtract(wDoc, mod, cmd));
        } else {
            // we use xpath, which is always set, defaults to //body
            try {
                NodeList list = domer.performXPathQuery(wDoc, xpath);
                if (list.getLength() == 0) {
                    // we have not found anything
                    return Content.getFailedContent(mod, cmd);

                }
                for (int ix = 0; ix < list.getLength(); ix++) {
                    tmpWrapper.appendChild(mod.getDoc().importNode(list.item(ix), true));
                    if (tmpWrapper.getLastChild().getNodeName().compareToIgnoreCase("img") == 0) {
                        // make this a wxt image look alike
                        // or not
                        //((Element)tmpWrapper.getLastChild()).setAttribute("class", "wxtimage");
                    }
                }

            } catch (Exception ex) {
                return null;
            }
        }

        // content of tmpWrapper is now produced
        // need the abs uri
        URI abs_uri = null;
        if (cmd.paramExist(PIcommand.LOCATION)) {
            try {
                abs_uri = accessutils.makeUri(cmd.getValue(PIcommand.LOCATION));
            } catch (Exception e) {
                //should not happen
                return null;
            }
        }

        // remove or modify links
        NodeList linklist = tmpWrapper.getElementsByTagName("a");
        //backwards to avoid concatenation of text destroying DOM ?
        for (int ix = linklist.getLength() - 1; ix > -1; ix--) {
            Element aelt = (Element) linklist.item(ix);
            if (!aelt.hasAttribute("href")) {
                continue;
            }
            Node parent = aelt.getParentNode();
            Node fchild = null;
            if (aelt.hasChildNodes()) {
                fchild = aelt.getFirstChild();
            } else {
                fchild = mod.getDoc().createTextNode(" ");
            }
            // remove reference []
            if (fchild.getNodeType() == Node.TEXT_NODE) {
                String tmp = fchild.getNodeValue().trim();
                if (tmp.startsWith("[") && tmp.endsWith("]")) {
                    fchild = mod.getDoc().createTextNode(" ");
                }
            } else if (fchild.getNodeType() == Node.ELEMENT_NODE) {
                if ((parent.getNodeType() == Node.ELEMENT_NODE)
                        && (parent.getNodeName().compareToIgnoreCase("sup") == 0)) {
                    fchild = mod.getDoc().createTextNode(" ");
                }
            }

            try {
                String href = aelt.getAttribute("href");
                URI newUri = abs_uri.resolve(href);
                aelt.setAttribute("href", newUri.toString());
                aelt.setAttribute("class", "external");
            } catch (Exception e) {
                parent.replaceChild(fchild, aelt);
            }
        }

        // final cleaning 
        NodeList eList = tmpWrapper.getElementsByTagName("*");
        for (int eix = eList.getLength() - 1; eix >= 0; eix--) {
            Element e = (Element) eList.item(eix);
            //--------------------------
            // we should remove ids to avoid conflict ?
            //if(e.hasAttribute("id"))
            //    e.removeAttribute("id");

            // should we do this ?
            if (e.hasAttribute("style")) {
                e.removeAttribute("style");
            }
            // should we do this ?
            if (e.getNodeName().compareTo("sup") == 0) {
                e.getParentNode().removeChild(e);
            }
        }

        // use what we have found
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        df.appendChild(mod.getDoc().importNode(tmpWrapper, true));
        return df;

    }

    /**
     * get the content from backup
     * @param mod The module that has requested this content
     * @param cmd The PIcommand that describes what we want
     * @return a Document if found, otherwise null
     */
    private static Document retrieveBackup(Module mod, PIcommand cmd) {
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        try {

            URI absBackupCat = makeBackupCatalog(mod, cmd);
            //URI contentUri=new URI(absBackupCat.toString()+"/content.xml");
            URI contentUri = accessutils.makeUri(absBackupCat.toString() + "/content.xml");
            //Document contentDoc=domer.makeDomFromUri(contentUri);
            String encoding = mod.getEncoding();
            if (cmd.paramExist(PIcommand.ENCODING)) {
                encoding = cmd.getValue(PIcommand.ENCODING);
            }
            Document contentDoc = domer.makeDomFromUriSomeHow(contentUri, encoding, mod.getReporter());
            return contentDoc;
        } catch (Exception rex) {
            return null;
        }
    }

    /**
     * Make a backup of the extracted material and all images
     * @param mod The module that want this material
     * @param cmd The command that describes the request
     * @param content The content as we have found it
     * @return tru if we can make the backup, false otherwise
     */
    private static boolean makeBackup(Module mod, PIcommand cmd, Document wDoc) {
        // determine backup catalog
        URI absBackupCat = makeBackupCatalog(mod, cmd);
        if (absBackupCat == null) {
            return false;
        }
        // make the catalog
        new File(absBackupCat.toString()).mkdirs();

        // run through the material and prepare all img-tags for
        // "paralell" save of images, and save images
        NodeList imList = wDoc.getElementsByTagName("img");
        for (int imix = 0; imix < imList.getLength(); imix++) {
            Element imElt = (Element) imList.item(imix);
            String src = imElt.getAttribute("src");
            // we must make sure it is absolute
            try {
                URI testAbs = accessutils.makeUri(src);
                if (!testAbs.isAbsolute()) {
                    String fromCat = cmd.getValue(PIcommand.LOCATION);
                    fromCat = accessutils.removeFilePartFromPathstring(fromCat);
                    testAbs = accessutils.makeAbsoluteURI(src, fromCat);
                    //testAbs = accessutils.makeAbsoluteURI(src, mod.getCatalog());
                    src = testAbs.toString();
                }
            } catch (Exception ex) {
                continue;
            }
            String newFileName = src;
            URI absImage = null;
            int pos = src.lastIndexOf("/");
            if (pos != -1) {
                newFileName = src.substring(pos + 1);
                newFileName = accessutils.cleanFilePath(newFileName);
                // move file to this new filename
                File f = new File(absBackupCat.getPath());
                boolean imageCopied = accessutils.copyImageFromURI(src, f.getPath() + "/" + newFileName);
                if (!imageCopied) {
                    mod.getReporter().pushMessage("could_not_copy_image", src);
                } else {
                    // correct adressing
                    try {
                        absImage = accessutils.makeUri(absBackupCat.toString() + "/" + newFileName);
                        URI relImage = accessutils.makeRelativeURI(mod.getAbsoluteUri(), absImage);
                        imElt.setAttribute("src", relImage.toString());

                    } catch (Exception ex) {
                        // do nothing: leave absolute image path as was
                    }
                }

            }
        }

        // produce and save the material
        try {
            domer.saveDom(wDoc, accessutils.makeUri(absBackupCat.toString() + "/content.xml"), "utf-8", true, "xml");
        } catch (Exception ex) {
            System.out.println("should not happen: " + ex.getMessage());
            return false;
        }

        try {
            //URI tmpUri=new URI("file:///"+result.getPath().replace('\\', '/')+"/readme.txt");
            URI tmpUri = new URI(absBackupCat.toString() + "/readme.txt");
            String T = "This directory is used (and reused) for storing back up of WIKIpages. \n"
                    + "WXT will produce a catalog for each WIKI-file that is used as contentprovider\n"
                    + "The main file is found in content.xml\n"
                    + "Images are referenced directly from the built Module and should not be removed";
            accessutils.saveTFile(tmpUri, T, "utf-8");
        } catch (Exception uex) {
            //should not happen
            System.out.println(uex.getMessage());
        }
        return true;
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
        // we dont want backup at all ?
        /*
        if(bckparam.compareTo(PIcommand.NO)==0)
        return null;
         */

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
            /*
            String tmp2="";
            if(cmd.paramExist(PIcommand.DPATH))
            {
            String dp=cmd.getValue(PIcommand.DPATH);
            int ix=dp.indexOf('[');
            if(ix!=-1)
            dp=dp.substring(0, ix-1);
            tmp2=accessutils.cleanStringForUseAsFilePath(dp);
            }
            else if(cmd.paramExist(PIcommand.XPATH))
            tmp2=accessutils.cleanStringForUseAsFilePath(cmd.getValue(PIcommand.XPATH));
            //absBackupCat=new URI(absBackupCat.toString()+"/"+tmp1+"/"+tmp2);
            absBackupCat=accessutils.makeUri(absBackupCat.toString()+"/"+tmp1+"/"+tmp2);
             */
            absBackupCat = accessutils.makeUri(absBackupCat.toString() + "/" + tmp1);

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            mod.getReporter().pushMessage("could_not_make_backup");
        }

        return absBackupCat;
    }

    /**
     * Find the necessary parametrs and set up DOM
     * @param mod The module that request this content
     * @param cmd The command describing the request
     * @return The DOM if possible, otherwise null
     */
    static private Document loadDocument(Module mod, PIcommand cmd) {
        URI absoluteUri = null;
        Transformation trans = null;
        HashMap<String, String> transParameters = null;
        String source_encoding = null;


        // what do we have ?
        // LOCATION is mandatory
        if (!cmd.paramExist(PIcommand.LOCATION)) {
            mod.getReporter().pushMessage("bad_processing_instruction", cmd.toString());
            return null;
        }
        String tmp = cmd.getValue(PIcommand.LOCATION);
        try {
            absoluteUri = accessutils.makeAbsoluteURI(tmp, mod.getCatalog());
        } catch (Exception ex) {
            mod.getReporter().pushMessage("bad_processing_instruction", cmd.toString());
            return null;
        }


        // transformation
        if (cmd.paramExist(PIcommand.TRANSFORMATION)) {
            String t = cmd.getValue(PIcommand.TRANSFORMATION);
            String transName = t;
            int parpos = t.indexOf('(');
            if (parpos != -1) {
                // we have parameters
                transName = t.substring(0, parpos);
                try {
                    transParameters = accessutils.unpackTransformationParameters(t);
                } catch (Exception e) {
                    mod.getReporter().pushMessage("bad_processing_instruction", cmd.toString());
                    return null;
                }
            }
            trans = mod.getDefinitions().getTransformation(transName);
            if (trans == null) {
                mod.getReporter().pushMessage("transformation_not_defined", cmd.toString());
                return null;
            }
        }
        // encoding
        source_encoding = mod.getDefinitions().getOption(Definitions.DEFAULT_ENCODING);
        if (cmd.paramExist(PIcommand.ENCODING)) {
            source_encoding = cmd.getValue(PIcommand.ENCODING);
        }



        // do it
        Document doc = null;
        String source = "";

        try {
            if (trans != null) {
                doc = domer.makeTransformedDomFromStringSomeHow(source, trans.getabsoluteURI(),
                        transParameters, source_encoding, mod.getReporter());
            } else {
                doc = domer.makeDomFromUriSomeHow(absoluteUri, source_encoding, mod.getReporter());
            }

            return doc;
        } catch (Exception ex) {
            mod.getReporter().pushMessage("bad_processing_instruction", cmd.toString(), ex.getMessage());
            return null;

        }

    }

    /**
     * Do basic cleaning and remove references if so wanted
     * @param wDoc The document
     * @param keepReferences Trur is we want to keep references, False otherwise
     */
    private void cleanDoc(Document wDoc, boolean keepReferences) {
        // clean away  stuff that may complicate
        // and which are not wanted anyhow
        NodeList alist = wDoc.getElementsByTagName("*");
        for (int ix = alist.getLength() - 1; ix >= 0; ix--) {
            Element e = (Element) alist.item(ix);
            // remove anchors
            if ((e.getTagName().compareTo("a") == 0) && (e.hasAttribute("name"))) {
                e.getParentNode().removeChild(e);
            }
            // remove some refs
            if ((e.getTagName().compareTo("a") == 0) && (e.hasAttribute("href"))) {
                // remove local refs
                if (e.getAttribute("href").startsWith("#")) {
                    e.getParentNode().removeChild(e);
                }
            }
            // remove javascriptstuff
            if (e.getTagName().compareTo("script") == 0) {
                e.getParentNode().removeChild(e);
            }

            // remove header element
            if (e.getTagName().compareTo("head") == 0) {
                e.getParentNode().removeChild(e);
            }
            // remove table of content, toc
            if ((e.getTagName().compareTo("table") == 0)
                    && ((e.hasAttribute("id") && (e.getAttribute("id").compareTo("toc") == 0)))) {
                e.getParentNode().removeChild(e);
            }
            // remove maginify
            if ((e.getTagName().compareTo("div") == 0)
                    && (e.hasAttribute("class"))
                    && (e.getAttribute("class").compareTo("magnify") == 0)) {
                e.getParentNode().removeChild(e);
            }
            // clean away spans inside hx -tags
            if (e.getTagName().startsWith("h") || (e.getTagName().startsWith("H"))) {
                String tmp = e.getTagName();
                if ((tmp.length() == 2)
                        && (tmp.endsWith("1") || tmp.endsWith("2") || tmp.endsWith("3")
                        || tmp.endsWith("4") || tmp.endsWith("5") || tmp.endsWith("6"))) {
                    NodeList spanlist = e.getElementsByTagName("span");
                    for (int six = spanlist.getLength() - 1; six >= 0; six--) {
                        Element spanElt = (Element) spanlist.item(six);
                        if (spanElt.hasAttribute("class")) {
                            if (spanElt.getAttribute("class").compareTo(MW_HEADLINE) == 0) //wraps the headline text in wiki
                            {
                                spanElt.getParentNode().replaceChild(wDoc.createTextNode(spanElt.getTextContent().trim()), spanElt);
                            } else {
                                spanElt.getParentNode().removeChild(spanElt);
                            }
                        }
                    }
                }
            }

        }

        if (!keepReferences) {
            NodeList aList = wDoc.getElementsByTagName("a");
            for (int aix = aList.getLength() - 1; aix >= 0; aix--) {
                Element aelt = (Element) aList.item(aix);
                Node aParent = aelt.getParentNode();
                NodeList cList = aelt.getChildNodes();
                DocumentFragment cf = wDoc.createDocumentFragment();
                for (int cix = 0; cix < cList.getLength(); cix++) {
                    if (cList.item(cix).getNodeType() == Node.ELEMENT_NODE) {
                        String s = ((Element) cList.item(cix)).getTagName();
                        // anything to analyze
                    } else if (cList.item(cix).getNodeType() == Node.TEXT_NODE) {
                        String s = cList.item(cix).getNodeValue();
                        // drop [] refs
                        if (s.startsWith("[") && s.endsWith("]")) {
                            continue;
                        }
                    }

                    cf.appendChild(cList.item(cix).cloneNode(true));
                }
                aParent.replaceChild(cf, aelt);
            }

            // sups (with reference class)
            aList = wDoc.getElementsByTagName("sup");
            for (int aix = aList.getLength() - 1; aix >= 0; aix--) {
                Element aelt = (Element) aList.item(aix);
                Node aParent = aelt.getParentNode();
                //if((aelt.hasAttribute("class"))&& (aelt.getAttribute("class").compareToIgnoreCase("reference")==0))
                aParent.removeChild(aelt);

            }
        }// eof not keepreferences


    }
}

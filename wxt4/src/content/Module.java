package content;

import fragments.Fragment;
import commands.CommandLine;
import fragments.SummaryFragment;
import indexing.Index;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import reporting.reporter;

import javax.swing.tree.DefaultMutableTreeNode;
import wxt4.Scripthandler;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import references.SimpleReference;
import utils.PIcommand;
import utils.accessutils;
import programcode.CodeFormatter;
import programcode.CodeGoogleFormatter;
import utils.domer;
import reporting.wxtError;

/**
 * A Module is a description of a unit that are built by wxt. It is equivalent to a web-page.
 * <p>
 * A Module may contain other modules in the sense that modules appear in a tree-structure.<br/>
 * This structure is reflected in tables of content
 * @author maa
 */
public class Module extends DefaultMutableTreeNode {

    /** defing constants for contenttypes in script*/
    public static final String XMLCONTENT = "xmlcontent";
    public static final String TXTCONTENT = "txtcontent";
    /** to come */
    public static final String WIKICONTENT = "wikicontent";
    public static final String ODFCONTENT = "odfcontent";
    public static final String DBCONTENT = "dbcontent";
    // only to avoid warnings
    // this is not the beginning of a serious and planned implementation of serialization
    private static final long serialVersionUID = 271246642172271246L;
    // max number of iterations in PI-expand-loop
    private static int MAXDEPTH = 4;
    // these are set up by constructor
    /** The scripthandler that holds the Module strucure*/
    private Scripthandler m_scriptHandler;
    /** The module that owns this module*/
    private Module m_father;
    /** Element in script describing this Module */
    private Element m_scriptElement;
    /** the reporter we should use */
    private reporter m_Reporter;
    /** the Producer we should use */
    private Producer m_Producer;
    /** definitions available */
    private Definitions m_Definitions;
    /** how the module is stored: xml,html,text*/
    private String m_outputFormat;
    /** the name as found in attribute name*/
    private String m_name;
    /** the id of this module as found in attribute id, or name*/
    private String m_id;
    /** Location as found in attribute location*/
    private String m_location;
    /** The description as found in attribute description, or name */
    private String m_description;
    /** the template object as identified from the template attribute*/
    private Template m_template;
    /** the transformation object as identified from the transformation attribute*/
    private Transformation m_transformation;
    /** books (collections) books attribute, stored as a commaseparated string*/
    private String m_books;
    /** the Command object as identified from the runbefore attribute*/
    private CommandLine m_commandBefore;
    /** the Command object as identified from the runafter attribute*/
    private CommandLine m_commandAfter;
    /** parameters used in the transformation parsed from the transformation attribute */
    private HashMap<String, String> m_transParameters;
    /** the module as a DOM tree */
    private Document m_Doc;
    /** list of all contents as found in script and accepted */
    private List<ScriptContent> m_ScriptContent;
    /** collection of all content addressed by PI's in this module*/
    private HashMap<String, Content> m_PIContent;
    // these are calculated
    /** absolute LOCATION calculated from parents anchorcatalog  and the location attribute*/
    private URI m_absoluteUri;
    /** A string with a catalogpath. Inherited from parent by default*/
    private String m_anchorCat;
    /** A xmlfragment holding the summary*/
    private SummaryFragment m_summary;
    /** If this module is target for a second phase build, set when doing PIs*/
    private boolean m_doSecondPhase;
    /** if this module has a footnote PI, leads to a scan for footnotes after all build **/
    private Node m_FootNoteNode;
    /** If we find moduletocs we remember them */
    private List<Node> m_ModTocs;
    /** The last time we saved this module in this session*/
    private long m_lastTimeSaved;
    /** the primary sources we detect for this module*/
    private List<URI> m_sourceUris;
    /** hoilding locally used references */
    //private ReferenceIndexHolder m_referenceHolder;
    private Index m_referenceHolder;
    /** need to remember reflist PI's so we can do them in the end*/
    private List<Node> m_foundRefLists;
    /** the author */
    private Fragment m_author = null;

    /**
     * Common initialization
     * 
     * @param scriptH The scripthandler that owns the module
     */
    private Module(Scripthandler scriptH) {
        m_scriptHandler = scriptH;

        m_Reporter = m_scriptHandler.getReporter();
        m_Producer = m_scriptHandler.getProducer();
        m_Definitions = m_scriptHandler.getDefinitions();
        m_Doc = null;
        m_doSecondPhase = false;
        m_FootNoteNode = null;
        m_outputFormat = null;

        m_ModTocs = null;

        m_anchorCat = null;

        m_name = null;
        m_absoluteUri = null;
        m_location = null;
        m_description = null;
        m_id = null;
        m_template = null;
        m_books = null;
        m_commandBefore = null;
        m_commandAfter = null;


        m_ScriptContent = new ArrayList<ScriptContent>(1);
        m_PIContent = new HashMap<String, Content>(2);

        m_summary = null;

        m_template = null;
        m_transformation = null;
        m_transParameters = null;


        m_lastTimeSaved = Long.MAX_VALUE;
        m_sourceUris = new ArrayList<URI>(10);
        m_referenceHolder = new Index();
        m_foundRefLists = new ArrayList<Node>();

        m_author = null;
    }

    /**
     * Constructing a new Module.
     * 
     * @param elt The node in the scriptTree that describes this module
     * @param scriptH The Scripthandler that holds the Modulestructure
     * @param father The module that is parent of this module
     * @throws java.lang.Exception when we cannot establish the module
     */
    public Module(Module father, Element elt, Scripthandler scriptH)
            throws Exception {
        this(scriptH);

        m_scriptElement = elt;
        m_father = father;

        // ---------------------
        // pickup, check and calculate things related to  attributes
        // Attributes are: 
        // name, description, id, location, template, anchor, transformation, book
        // All, except name, are optional. This means that a module with only a name
        // will appear as a simple heading.
        // Must remember this possibility when we try to reach it (link to it) 
        // ------------------------
        // set anchoring by inheritage. 
        // This is the absolute reference for this modules children
        // and all content locations

        if (m_father != null) {
            m_anchorCat = m_father.getAnchorCat();
        } else {
            m_anchorCat = m_scriptHandler.getScriptCatalog();
        }


        // all attributes of module that involves locations 
        //(location, [template,transformation]) should be calculated according to anchorCat

        // remember  inheritedAnchorCat so we can calculate new anchor relative
        // to it if this module has anchor

        String inheritedAnchorCat = m_anchorCat;

        // there is allways a name:
        m_name = "NO_NAME";
        // pick up the elements attribute: name
        if (m_scriptElement.hasAttribute("name")) {
            m_name = m_scriptElement.getAttribute("name");
        }

        m_Reporter.pushSimpleMessage(m_name);

        // changing the anchor ?
        if (m_scriptElement.hasAttribute("anchor")) {
            m_anchorCat = m_Definitions.substituteFragments(m_scriptElement.getAttribute("anchor"));
            if (m_anchorCat == null) {
                m_Reporter.pushMessage("bad_anchor", m_scriptElement.getAttribute("anchor"));
                throw new Exception();
            }
            //anchors which are not absolute should be built on fathers anchor: inheritedAnchorCat
            try {
                //URI tmpUri=new URI(m_anchorCat);
                URI tmpUri = accessutils.makeUri(m_anchorCat);
                if (!tmpUri.isAbsolute()) {
                    m_anchorCat = accessutils.makeAbsoluteURI(m_anchorCat, inheritedAnchorCat).toString();
                }
            } catch (Exception ex) {
                m_Reporter.pushMessage("bad_anchor", m_anchorCat);
                throw new Exception();
            }
        }

        // pick up description and id. Both defaults to name
        m_description = m_name;
        m_id = m_name;
        if (m_scriptElement.hasAttribute("description")) {
            m_description = m_scriptElement.getAttribute("description");
        }
        if (m_scriptElement.hasAttribute("id")) {
            m_id = m_scriptElement.getAttribute("id");
        }

        // pick up author
        // and register it and index it
        if (m_scriptElement.hasAttribute("author")) {
            String auid = m_scriptElement.getAttribute("author");
            //m_author=m_Definitions.getAuthors().get(auid);
            m_author = m_Definitions.getAuthor(auid);
        }
        // will allways have an author
        if (m_author == null) // m_author=m_Definitions.getAuthors().get(Definitions.DEFAULTAUTHOR);
        {
            m_author = m_Definitions.getAuthor(Definitions.DEFAULTAUTHOR);
        }

        // mark this module as written by the author

        m_Definitions.getAuthorHolder().add(m_author, m_author.getId(), this);

        // pickup location
        if (m_scriptElement.hasAttribute("location")) {
            m_location = m_scriptElement.getAttribute("location");
            m_location = m_Definitions.substituteFragments(m_location);
            if (m_location != null) {
                // location will be calculated according to anchor
                m_absoluteUri = accessutils.makeAbsoluteURI(m_location, m_anchorCat);
                if ((m_absoluteUri == null) || (!m_absoluteUri.isAbsolute())) {
                    m_Reporter.pushMessage("bad_module_location", m_scriptElement.getAttribute("location"));
                    throw new Exception();
                }
            } else {
                m_Reporter.pushMessage("bad_module_location", m_scriptElement.getAttribute("location"));
                throw new Exception();
            }
        }

        // pickup template
        if (m_scriptElement.hasAttribute("template")) {
            String tmp = m_scriptElement.getAttribute("template").trim();
            tmp = m_Definitions.substituteFragments(tmp);
            m_template = m_Definitions.getTemplate(tmp);
            if (m_template == null) {
                // this is a template which is not defined. 
                // we expect to find an LOCATION which is absolute or relative to anchor
                try {
                    URI tmpUri = accessutils.makeAbsoluteURI(tmp, m_anchorCat);
                    m_template = new Template(tmpUri.toString(), tmpUri);
                    m_Definitions.addTemplate(tmpUri.toString(), m_template);
                } catch (Exception tex) {
                    m_Reporter.pushMessage("bad_template", tmp);
                    throw new Exception("unknown template");
                }
            }
        }

        // pickup transformation
        if (m_scriptElement.hasAttribute("transformation")) {
            String tmp = m_scriptElement.getAttribute("transformation").trim();
            try {
                String tmpTID = tmp;
                m_transParameters = accessutils.unpackTransformationParameters(tmp);
                if (m_transParameters != null) {
                    tmpTID = tmp.substring(0, tmp.indexOf('('));
                }
                m_transformation = m_Definitions.getTransformation(tmpTID);
            } catch (Exception e) {
                m_Reporter.pushMessage("bad_transformation", tmp);
                throw new Exception("unknown transformation");
            }

            if (m_transformation == null) {
                // this is a transformation which is not defined.
                // we expect to find an LOCATION which is absolute or relative to parents anchor
                try {
                    URI tmpUri = accessutils.makeAbsoluteURI(tmp, m_anchorCat);
                    m_transformation = new Transformation(tmpUri.toString(), tmpUri);
                    m_Definitions.addTransformation(tmpUri.toString(), m_transformation);
                } catch (Exception tex) {
                    m_Reporter.pushMessage("bad_transformation", tmp);
                    throw new Exception("unknown transformation");
                }
            }
        }

        // pickup outputmethod
        pickUpOutputFormat();

        // pickup commands 
        if (m_scriptElement.hasAttribute("runbefore")) {
            String name = m_scriptElement.getAttribute("runbefore").trim();
            m_commandBefore = m_Definitions.getCommand(name);
            if (m_commandBefore == null) {
                m_Reporter.pushMessage("missing_command", name);
                //throw new Exception("unknown command");
            }
        }

        if (m_scriptElement.hasAttribute("runafter")) {
            String name = m_scriptElement.getAttribute("runafter").trim();
            m_commandAfter = m_Definitions.getCommand(name);
            if (m_commandAfter == null) {
                m_Reporter.pushMessage("missing_command", name);
                //throw new Exception("unknown command");
            }
        }

        // pickup books
        // there is allways a book
        m_books = "_all,";
        if (m_scriptElement.hasAttribute("books")) {
            m_books = m_scriptElement.getAttribute("books");
        }



        //----------------------------------------------
        // collect all content elements
        // There are 6 types of content-elements
        //   1 xmlcontent, which are treated via its DOM only
        //   2 txtcontent, which are a versatile type that is textbased
        //   3 wikicontent
        //   4 dbcontent
        //   5 odfcontent
        // There may be any number of all these 5 types
        // These content elements are matched in the template by
        // processing instructions:
        //   importxml, importtxt, importwiki, importdb, importodf
        // respectively
        //  6 summary (picked up when needed in getSummary)


        //---------------------------------------------
        // collect content elements in a Vector: m_ScriptContent
        //  m_XMLContent and m_TXTContent
        if (m_scriptElement.hasChildNodes()) {
            Node nod = m_scriptElement.getFirstChild();
            while (nod != null) {
                if (nod.getNodeType() == Node.ELEMENT_NODE) {
                    Element celt = (Element) nod;
                    if (celt.getNodeName().compareToIgnoreCase(XMLCONTENT) == 0) {
                        try {
                            m_ScriptContent.add(new ScriptContent(celt, this));
                        } catch (Exception e) {
                            m_Reporter.pushMessage("cannot_establish_xmlcontent");
                        }
                    } else if (celt.getNodeName().compareToIgnoreCase(TXTCONTENT) == 0) {
                        try {
                            m_ScriptContent.add(new ScriptContent(celt, this));
                        } catch (Exception e) {
                            m_Reporter.pushMessage("cannot_establish_txtcontent");
                        }
                    } else if (celt.getNodeName().compareToIgnoreCase(WIKICONTENT) == 0) {
                        try {
                            m_ScriptContent.add(new ScriptContent(celt, this));
                        } catch (Exception e) {
                            m_Reporter.pushMessage("cannot_establish_wikicontent");
                        }
                    } else if (celt.getNodeName().compareToIgnoreCase(ODFCONTENT) == 0) {
                        try {
                            m_ScriptContent.add(new ScriptContent(celt, this));
                        } catch (Exception e) {
                            m_Reporter.pushMessage("cannot_establish_odfcontent");
                        }
                    } else if (celt.getNodeName().compareToIgnoreCase(DBCONTENT) == 0) {
                        try {
                            m_ScriptContent.add(new ScriptContent(celt, this));
                        } catch (Exception e) {
                            m_Reporter.pushMessage("cannot_establish_dbcontent", e.getMessage());
                        }
                    } // pick up summary                    
                    else if (celt.getNodeName().compareToIgnoreCase("summary") == 0) {
                        try {
                            URI tmpuri = null;
                            if (celt.hasAttribute("location")) {
                                String tmploc = celt.getAttribute("location");
                                tmploc = m_Definitions.substituteFragments(tmploc);
                                tmpuri = accessutils.makeAbsoluteURI(tmploc, m_anchorCat);
                            }
                            m_summary = new SummaryFragment(celt.getTextContent(), this);
                        } catch (Exception e) {
                            m_Reporter.pushMessage("cannot_establish_summary", e.getMessage());
                        }
                    }
                }
                nod = nod.getNextSibling();
            }
        }

        // if summary is not set, we use description as summary
        if (m_summary == null) {
            try {
                // make a fragment, do not save it in definitions.fragments
                m_summary = new SummaryFragment(m_description, this);
            } catch (Exception e) {
                m_Reporter.pushMessage("cannot_establish_summary", e.getMessage());
            }
        }
    }

    /**
     * Find the wanted output format
     * either from definitions (common for the script)
     * or special for this module
     * allowed values are:html,xml,text,xhtml
     * xhtml is XML without XML-header
     */
    private void pickUpOutputFormat() {
        // pickup outputmethod
        m_outputFormat = m_Definitions.getOption(Definitions.OUTPUT_FORMAT);
        if (m_scriptElement.hasAttribute(Definitions.OUTPUT_FORMAT)) {
            String tmp = m_scriptElement.getAttribute(Definitions.OUTPUT_FORMAT).trim();
            tmp = tmp.toLowerCase();
            if ((tmp.compareTo("xml") != 0)
                    && (tmp.compareTo("html") != 0)
                    && (tmp.compareTo("txt") != 0)
                    && (tmp.compareTo("xhtml") != 0)) {
                m_Reporter.pushMessage("bad_output_format", tmp);
            } else {
                m_outputFormat = tmp;
            }
        }
    }

    /**
     * Build the Module.
     * <p>
     * Getting the template, filling in content and performing Processing instructions
     * 
     */
    public void build() {
        // reset these to make sure we can build a second time
        // on the same established structure
        m_Doc = null;
        m_doSecondPhase = false;
        m_FootNoteNode = null;
        m_ModTocs = null;
        m_lastTimeSaved = Long.MAX_VALUE;
        m_sourceUris.clear();
        // must redo this since we may have new options when building
        pickUpOutputFormat();
        //m_referenceHolder=new ReferenceIndexHolder(m_Definitions);
        m_referenceHolder = new Index();
        m_foundRefLists = null;
        // must clear readymade content
        m_PIContent = new HashMap<String, Content>(2);

        //---------------

        m_Reporter.pushSimpleMessage(m_name);




        // first we do possible before - command
        if (m_commandBefore != null) {
            m_commandBefore.executeCommand(m_Reporter);
        }

        // if we dont have a template, there is not much to do
        // We could easily interpret first XMLContent as template
        // but it does not seem to be much sense in that, or ?
        if (m_template == null) {
            // warning/message
            m_Reporter.pushMessage("no_template", m_name);

            // it should be possible to run after command ?
            if (m_commandAfter != null) {
                m_commandAfter.executeCommand(m_Reporter);
            }

            return;
        }


        // --------------------------------------------------------------------
        // ok, we load the template
        try {
            m_Doc = m_template.getDoc(this);
        } catch (Exception e) {
            m_Reporter.pushMessage("cannot_load_template", m_name);
            return;
        }

        //------------ dependencies ----------------------
        // add template to dependencies so we can "build changed modules"
        addDependency(m_template.getabsoluteURI());
        // add transformation to dependencies
        if (m_transformation != null) {
            addDependency(m_transformation.getabsoluteURI());
        }
        // add content to dependencies
        for (ScriptContent xc : m_ScriptContent) {
            addDependency(xc.getAbsoluteUri());
        }


        // --------------------------------------------------------------------
        // the Document for this module is loaded and we are ready to fill it

        // We have to correct all addressing in the template itself
        Producer.correctAddressing(m_Doc.getDocumentElement(), this, m_template.getabsoluteURI());

        // no signal for second phase yet
        m_doSecondPhase = false;

        // Get all importxml, importwiki importtxt, importodf PIs without location or location=content
        // and all importdb without connection
        // These refers to contentelements as given in script
        // These are legal content in a module and we place them on the page
        // All addresses in PIs in the material identifyed by these PIs must be corrected
        // so must all other addresses img, a etc
        // We will meet those again later
        List<Node> pilist = domer.getPIs(m_Doc, "_wxt");
        String piValue = "";
        for (Node pi_node : pilist) {
            PIcommand cmd;
            try {
                piValue = (pi_node.getNodeValue());
                cmd = new PIcommand(piValue, m_Definitions);
                String cmd_cmd = cmd.getCommand();
                // import XML
                if (cmd_cmd.compareToIgnoreCase(PIcommand.IMPORTXML) == 0) {
                    // we are only interested if this anonymous
                    // that is it has no location or location is _content
                    if ((!cmd.paramExist(PIcommand.LOCATION))
                            || (cmd.getValue(PIcommand.LOCATION).compareToIgnoreCase(PIcommand.CONTENT) == 0)) {
                        DocumentFragment df = m_Producer.import_ContentToTemplate(this, cmd, XMLCONTENT);
                        insertDF(df, pi_node);
                        //pi_node.getParentNode().replaceChild(df, pi_node);
                    }
                } // import txt
                else if (cmd_cmd.compareToIgnoreCase(PIcommand.IMPORTTXT) == 0) {
                    if ((!cmd.paramExist(PIcommand.LOCATION))
                            || (cmd.getValue(PIcommand.LOCATION).compareToIgnoreCase(PIcommand.CONTENT) == 0)) {
                        DocumentFragment df = m_Producer.import_ContentToTemplate(this, cmd, TXTCONTENT);
                        insertDF(df, pi_node);
                        //pi_node.getParentNode().replaceChild(df, pi_node);
                    }
                } // import wiki
                else if (cmd_cmd.compareToIgnoreCase(PIcommand.IMPORTWIKI) == 0) {
                    if ((!cmd.paramExist(PIcommand.LOCATION))
                            || (cmd.getValue(PIcommand.LOCATION).compareToIgnoreCase(PIcommand.CONTENT) == 0)) {
                        DocumentFragment df = m_Producer.import_ContentToTemplate(this, cmd, WIKICONTENT);
                        insertDF(df, pi_node);
                        //pi_node.getParentNode().replaceChild(df, pi_node);
                    }
                } // import ODF
                else if (cmd_cmd.compareToIgnoreCase(PIcommand.IMPORTODF) == 0) {
                    if ((!cmd.paramExist(PIcommand.LOCATION))
                            || (cmd.getValue(PIcommand.LOCATION).compareToIgnoreCase(PIcommand.CONTENT) == 0)) {
                        DocumentFragment df = m_Producer.import_ContentToTemplate(this, cmd, ODFCONTENT);
                        insertDF(df, pi_node);
                        //pi_node.getParentNode().replaceChild(df, pi_node);
                    }
                } // import DB
                else if (cmd_cmd.compareToIgnoreCase(PIcommand.IMPORTDB) == 0) {
                    if ((!cmd.paramExist(PIcommand.CONNECT))) {
                        DocumentFragment df = m_Producer.import_ContentToTemplate(this, cmd, DBCONTENT);
                        insertDF(df, pi_node);
                        //pi_node.getParentNode().replaceChild(df, pi_node);
                    }
                }
            } catch (Exception e) {
                m_Reporter.pushMessage("bad_processing_instruction", piValue, e.getMessage());
                continue;
            }
        }


        //------------------------------------------------
        // get all our remaining PI's and take care of them
        // all adressing should now be correct(ed)
        // we open up for "recursive" job. Imports (typically fragments)
        // may containg PI's. 
        // doItAgain is now set for: importxml,importtxt,fragment
        boolean doItAgain = true;
        int count = 0;
        while (doItAgain && (count < MAXDEPTH)) {
            doItAgain = false;
            count++;
            pilist = domer.getPIs(m_Doc, "_wxt");

            for (Node pi_node : pilist) {
                PIcommand cmd;
                piValue = (pi_node.getNodeValue());
                try {
                    cmd = new PIcommand(piValue, m_Definitions);
                } catch (Exception e) {
                    m_Reporter.pushMessage("bad_processing_instruction", piValue, e.getMessage());
                    continue;
                }

                //-------------------------------------
                // XML content: <?_wxt importxml location xpath
                if (cmd.getCommand().compareToIgnoreCase(PIcommand.IMPORTXML) == 0) {
                    DocumentFragment df = m_Producer.makePI_XMLContent(this, cmd);
                    insertDF(df, pi_node);
                    doItAgain = true;
                } //-------------------------------------
                // WIKI content: <?_wxt importwiki location ...
                else if (cmd.getCommand().compareToIgnoreCase(PIcommand.IMPORTWIKI) == 0) {
                    DocumentFragment df = m_Producer.makePI_WIKIContent(this, cmd);
                    insertDF(df, pi_node);
                } //-------------------------------------
                // ODF content: <?_wxt importodf location ...
                else if (cmd.getCommand().compareToIgnoreCase(PIcommand.IMPORTODF) == 0) {
                    DocumentFragment df = m_Producer.makePI_ODFContent(this, cmd);
                    insertDF(df, pi_node);
                } //---------------------------------------
                // TXT content: <?_wxt importtxt location ....
                else if (cmd.getCommand().compareToIgnoreCase(PIcommand.IMPORTTXT) == 0) {
                    DocumentFragment df = m_Producer.makePI_TXTContent(this, cmd);
                    insertDF(df, pi_node);
                    doItAgain = true;
                } //---------------------------------------
                // DB content: <?_wxt importdb  ....
                else if (cmd.getCommand().compareToIgnoreCase(PIcommand.IMPORTDB) == 0) {
                    DocumentFragment df = m_Producer.makePI_DBContent(this, cmd);
                    insertDF(df, pi_node);
                } //---------------------------------------
                // fragment: <?_wxt fragment id ?>
                else if (cmd.getCommand().compareToIgnoreCase(PIcommand.FRAGMENT) == 0) {
                    DocumentFragment df = m_Producer.makePI_Fragment(this, cmd);
                    insertDF(df, pi_node);
                    doItAgain = true;
                } //------------------------------------
                // formula: <?_wxt formula  ....
                else if (cmd.getCommand().compareToIgnoreCase(PIcommand.FORMULA) == 0) {
                    DocumentFragment df = m_Producer.makePI_Formula(this, cmd);
                    insertDF(df, pi_node);
                } //---------------------------------------
                // popfragment: <?_wxt popfragment id text?>
                else if (cmd.getCommand().compareToIgnoreCase(PIcommand.POPFRAGMENT) == 0) {
                    // we send pi_node since we want to place a
                    // popping fragment relative to this
                    DocumentFragment df = m_Producer.makePI_PopFragment(this, cmd, pi_node);
                    insertDF(df, pi_node);
                } //---------------------------------------
                // xlinx: <?_wxt xlink id 
                else if (cmd.getCommand().compareToIgnoreCase(PIcommand.XLINK) == 0) {
                    DocumentFragment df = m_Producer.makePI_XLink(this, cmd);
                    insertDF(df, pi_node);
                } //---------------------------------------
                // modulemap: <?_wxt map  
                else if (cmd.getCommand().compareToIgnoreCase(PIcommand.MODULEMAP) == 0) {
                    DocumentFragment df = m_Producer.makePI_ModuleMap(this, cmd);
                    insertDF(df, pi_node);
                } //---------------------------------------
                // path: <?_wxt path pathdivider
                else if (cmd.getCommand().compareToIgnoreCase(PIcommand.PATH) == 0) {
                    DocumentFragment df = m_Producer.makePI_Path(this, cmd);
                    insertDF(df, pi_node);
                } //---------------------------------------
                // moduletoc: <?_wxt moduletoc
                else if (cmd.getCommand().compareToIgnoreCase(PIcommand.MODULETOC) == 0) {
                    if (m_ModTocs == null) {
                        m_ModTocs = new ArrayList<Node>();
                    }
                    if (!m_ModTocs.contains(pi_node)) {
                        m_ModTocs.add(pi_node);
                    }
                } //---------------------------------------
                // moduletocfinal: <?_wxt moduletocfinal
                else if (cmd.getCommand().compareToIgnoreCase(PIcommand.MODULETOCFINAL) == 0) {
                    // we will not do this now,
                    // it wil be done after possible collection of built modules
                    // We postpone it to second phase build
                    m_doSecondPhase = true;
                } //--------------------------------------
                // indexword: <?_wxt ixword word category
                else if (cmd.getCommand().compareToIgnoreCase(PIcommand.IXWORD) == 0) {
                    DocumentFragment df = m_Producer.makePI_IXWord(this, cmd);
                    insertDF(df, pi_node);
                } //----------------------------------------
                // <_wxt reftest
                else if (cmd.getCommand().compareToIgnoreCase(PIcommand.REFERENCETEST) == 0) {

                    DocumentFragment df = m_Producer.makePI_ReferenceTest(this, cmd);
                    insertDF(df, pi_node);
                } //--------------------------------------
                // indexlist: <?_wxt ixtable category root
                else if (cmd.getCommand().compareToIgnoreCase(PIcommand.IXTABLE) == 0) {
                    // we cannot do this now,
                    // since the indexlists depends on all modules being built
                    // We postpone it to second phase build
                    m_doSecondPhase = true;
                } //--------------------------------------
                // collect: <?_wxt collect 
                else if (cmd.getCommand().compareToIgnoreCase(PIcommand.COLLECT) == 0) {
                    // we cannot do this now,
                    // since the collection depends on all modules being built
                    // We postpone it to second phase build
                    m_doSecondPhase = true;
                } //--------------------------------------
                // collect remote: <?_wxt collect-remote
                else if (cmd.getCommand().compareToIgnoreCase(PIcommand.COLLECTREMOTE) == 0) {
                    // we cannot do this now,
                    // since the collection depends on all modules being built
                    // We postpone it to second phase build
                    m_doSecondPhase = true;
                } //--------------------------------------
                // collectsummary: <?_wxt collectsummary            
                else if (cmd.getCommand().compareToIgnoreCase(PIcommand.COLLECTSUMMARY) == 0) {
                    // we cannot do this now,
                    // since the collection depends on all modules being built
                    // We postpone it to second phase build
                    m_doSecondPhase = true;
                } //---------------------------------------
                // <?_wxt imagethumb
                else if (cmd.getCommand().compareToIgnoreCase(PIcommand.IMAGETHUMB) == 0) {
                    DocumentFragment df = m_Producer.makePI_Image(this, cmd);
                    insertDF(df, pi_node);
                } //--------------------------------------
                // ref: <?_wxt image
                else if (cmd.getCommand().compareToIgnoreCase(PIcommand.IMAGE) == 0) {
                    DocumentFragment df = m_Producer.makePI_Image(this, cmd);
                    insertDF(df, pi_node);
                } //--------------------------------------
                // ref: <?_wxt imagelist
                else if (cmd.getCommand().compareToIgnoreCase(PIcommand.IMAGELIST) == 0) {
                    //DocumentFragment df=m_Producer.makePI_ImageList(this, cmd);
                    //pi_node.getParentNode().replaceChild(df, pi_node);
                    m_doSecondPhase = true;

                } //--------------------------------------
                // ref: <?_wxt formulalist
                else if (cmd.getCommand().compareToIgnoreCase(PIcommand.FORMULALIST) == 0) {
                    //DocumentFragment df=m_Producer.makePI_ImageList(this, cmd);
                    //pi_node.getParentNode().replaceChild(df, pi_node);
                    m_doSecondPhase = true;

                } //--------------------------------------
                // ref: <?_wxt ref             
                else if (cmd.getCommand().compareToIgnoreCase(PIcommand.REF) == 0) {
                    if (cmd.paramExist(PIcommand.ID)) {
                        String sid = cmd.getValue(PIcommand.ID);
                        SimpleReference theRef = m_Definitions.getReference(sid);

                        // notify globally, and return index(-1 if not exist)
                        int globalindex = m_Definitions.getReferenceHolder().add(theRef, sid, this) + 1;
                        // notify locally, and return index(-1 if not exist)
                        int localindex = m_referenceHolder.add(theRef, sid, this) + 1;

                        int selectedindex = localindex;
                        // we will use globalindex or localindex, depending on the
                        // order we want
                        if (m_Definitions.getOption(Definitions.REFERENCE_INDEXING).compareTo(Definitions.GLOBAL) == 0) {
                            selectedindex = globalindex;
                        }

                        // transport index in cmd
                        // leave to Producer to produce something for missing ref (selectedindex=-1)
                        cmd.setParameter(PIcommand.INDEX, "" + selectedindex);

                        DocumentFragment df = m_Producer.makePI_Reference(this, cmd);
                        insertDF(df, pi_node);
                    } else {
                        m_Reporter.pushMessage("missing_id_in_referenece");
                    }
                } //--------------------------------------
                // ref: <?_wxt reflist                         
                else if (cmd.getCommand().compareToIgnoreCase(PIcommand.REFLIST) == 0) {
                    // wait until module is finnished
                    if (m_foundRefLists == null) {
                        m_foundRefLists = new ArrayList<Node>();
                    }
                    if (!m_foundRefLists.contains(pi_node)) {
                        m_foundRefLists.add(pi_node);
                    }
                } //----------------------------------
                // date: <?_wxt date 
                else if (cmd.getCommand().compareToIgnoreCase(PIcommand.DATE) == 0) {
                    DocumentFragment df = m_Producer.makePI_DateStamp(this, cmd);
                    insertDF(df, pi_node);
                } //----------------------------------
                // date: <?_wxt time 
                else if (cmd.getCommand().compareToIgnoreCase(PIcommand.TIME) == 0) {
                    DocumentFragment df = m_Producer.makePI_TimeStamp(this, cmd);
                    insertDF(df, pi_node);
                } //----------------------------------
                // date: <?_wxt stamp 
                else if (cmd.getCommand().compareToIgnoreCase(PIcommand.STAMP) == 0) {
                    DocumentFragment df = m_Producer.makePI_Stamp(this, cmd);
                    insertDF(df, pi_node);
                } //----------------------------------
                // modulemenu: <?_wxt modulemenu
                else if (cmd.getCommand().compareToIgnoreCase(PIcommand.MODULEMENU) == 0) {
                    DocumentFragment df = m_Producer.makePI_ModuleMenu(this, cmd);
                    insertDF(df, pi_node);
                } //----------------------------------
                // authorlist: <?_wxt authorlist
                else if (cmd.getCommand().compareToIgnoreCase(PIcommand.AUTHORLIST) == 0) {
                    DocumentFragment df = m_Producer.makePI_AuthorOfModules(this, cmd);
                    insertDF(df, pi_node);
                } //----------------------------------
                // modulemenu: <?_wxt authors
                else if (cmd.getCommand().compareToIgnoreCase(PIcommand.AUTHORS) == 0) {
                    DocumentFragment df = m_Producer.makePI_AuthorSimpleList(this, cmd);
                    insertDF(df, pi_node);
                } //------------------------------------
                // gadget: <?_wxt gadget location width height title
                else if (cmd.getCommand().compareToIgnoreCase(PIcommand.GADGET) == 0) {
                    DocumentFragment df = m_Producer.makePI_Gadget(this, cmd);
                    insertDF(df, pi_node);
                } //----------------------------------
                // popup: <?_wxt popup 
                else if (cmd.getCommand().compareToIgnoreCase(PIcommand.POPUP) == 0) {
                    DocumentFragment df = m_Producer.makePI_Popup(this, cmd);
                    insertDF(df, pi_node);
                } //----------------------------------
                // expand: <?_wxt expand 
                else if (cmd.getCommand().compareToIgnoreCase(PIcommand.EXPAND) == 0) {
                    m_Reporter.pushMessage("PI_is_removed", PIcommand.EXPAND, PIcommand.EXPANDABLE);
                    DocumentFragment df = m_Producer.makePI_ExpandAJAX(this, cmd);
                    insertDF(df, pi_node);
                } //----------------------------------
                // expandsimple: <?_wxt expandsimple
                else if (cmd.getCommand().compareToIgnoreCase(PIcommand.EXPANDSIMPLE) == 0) {
                    m_Reporter.pushMessage("PI_is_deprecated", PIcommand.EXPANDSIMPLE, PIcommand.EXPANDABLE);
                    DocumentFragment df = m_Producer.makePI_Expandable(this, cmd);
                    insertDF(df, pi_node);
                } //----------------------------------
                // expandsimple: <?_wxt expandable
                else if (cmd.getCommand().compareToIgnoreCase(PIcommand.EXPANDABLE) == 0) {
                    DocumentFragment df = m_Producer.makePI_Expandable(this, cmd);
                    insertDF(df, pi_node);
                } //----------------------------------
                // demolink: <?_wxt demolink 
                else if (cmd.getCommand().compareToIgnoreCase(PIcommand.DEMOLINK) == 0) {
                    DocumentFragment df = m_Producer.makePI_DemoLink(this, cmd);
                    insertDF(df, pi_node);
                } //------------------------------------
                // footnote: <?_wxt footnote
                // note that we use only one footnotes
                // if more present all but the last found is ignored
                else if (cmd.getCommand().compareToIgnoreCase(PIcommand.FOOTNOTE) == 0) {
                    m_FootNoteNode = pi_node;
                } // etc for any PI we invent
                //---------------------------------------
                // unknow PI 
                // this is the place to identify extensions
                else {
                    m_Reporter.pushMessage("unknown_pi", cmd.toString());
                }
            }

        }// while doItAgain


        //----------------------------
        // color code already classmarked pre nodes
        if (getDefinitions().getOption(Definitions.PREFORMAT_LANGUAGE).compareTo(Definitions.NO) == 0) {
            m_Doc = CodeGoogleFormatter.expandGoogleCodeFragments(m_Doc, m_Doc.getXmlEncoding(), this);
        } else {
            m_Doc = CodeFormatter.expandCodeFragments(m_Doc, m_Doc.getXmlEncoding(), this);
        }




        //----------------------------
        // do footnotes
        if (m_FootNoteNode != null) {
            PIcommand cmd;
            piValue = (m_FootNoteNode.getNodeValue());
            try {
                cmd = new PIcommand(piValue, m_Definitions);
                DocumentFragment df = m_Producer.makePI_FootNote(this, cmd);
                insertDF(df, m_FootNoteNode);
                //m_FootNoteNode.getParentNode().replaceChild(df, m_FootNoteNode);
            } catch (Exception e) {
                m_Reporter.pushMessage("bad_processing_instruction", piValue, e.getMessage());
            }

        }

        //-------------------------
        // do module tocs
        if (m_ModTocs != null) {
            for (Node n : m_ModTocs) {
                PIcommand cmd;
                piValue = (n.getNodeValue());
                try {
                    cmd = new PIcommand(piValue, m_Definitions);
                } catch (Exception e) {
                    m_Reporter.pushMessage("bad_processing_instruction", piValue, e.getMessage());
                    continue;
                }
                DocumentFragment df = m_Producer.makePI_ModuleToc(this, cmd);
                insertDF(df, n);
            }
        }

        //------------------------
        // do reflists
        if (m_foundRefLists != null) {
            for (Node n : m_foundRefLists) {
                PIcommand cmd;
                piValue = (n.getNodeValue());
                try {
                    cmd = new PIcommand(piValue, m_Definitions);
                } catch (Exception e) {
                    m_Reporter.pushMessage("bad_processing_instruction", piValue, e.getMessage());
                    continue;
                }
                DocumentFragment df = m_Producer.makePI_ReferenceList(this, cmd);
                insertDF(df, n);
            }
        }




        //-------------------------------------------
        // save it
        // drop save if we have marked for second phase ?
        // keep save since commandafter may need it ?
        save();

        // the last we do is a possible after - command
        // but we must do it later if we are in for a second phase
        if ((m_commandAfter != null) && (!m_doSecondPhase)) {
            //save();
            m_commandAfter.executeCommand(m_Reporter);
        }
    }

    /**
     * This build pick up all PI's that depends on all modules beeing built.
     * <p>
     * Cases are: collect, indextable
     */
    public void buildSecondPhase() {
        m_Reporter.pushSimpleMessage(m_name);
        if (m_Doc == null) {
            build();
            // should not happen ?
            wxtError.makeErrorMsgForDebugging(null, null, "Module must rebuild DOM in second phase in Module:buildSecondPhase");
        }
        if (m_Doc == null) {
            m_Reporter.pushMessage("cannot_establish_dom_in_2phase", m_id);
        }

        //------------------------------------------------
        // get all our remaining PI's and take care of them
        // Sequence is essential:
        // 1 collect
        // 2 ixtable and moduletocfinal
        List<Node> pilist = domer.getPIs(m_Doc, "_wxt");
        for (Node pi_node : pilist) {
            PIcommand cmd;
            String piValue = (pi_node.getNodeValue());
            try {
                cmd = new PIcommand(piValue, m_Definitions);
            } catch (Exception e) {
                m_Reporter.pushMessage("bad_processing_instruction", piValue, e.getMessage());
                continue;
            }

            // collect: <?_wxt collect
            if (cmd.getCommand().compareToIgnoreCase(PIcommand.COLLECT) == 0) {
                DocumentFragment df = m_Producer.makePI_Collect(this, cmd);
                insertDF(df, pi_node);
            }
            //--------------------------------------
            // collect remote: <?_wxt collect-remote
            if (cmd.getCommand().compareToIgnoreCase(PIcommand.COLLECTREMOTE) == 0) {
                DocumentFragment df = m_Producer.makePI_CollectRemote(this, cmd);
                insertDF(df, pi_node);
            } //--------------------------------------
            // collectsummary: <?_wxt collectsummary
            else if (cmd.getCommand().compareToIgnoreCase(PIcommand.COLLECTSUMMARY) == 0) {
                DocumentFragment df = m_Producer.makePI_CollectSummaries(this, cmd);
                insertDF(df, pi_node);
            }
        }
        // make list again, since sequence is essential
        pilist = domer.getPIs(m_Doc, "_wxt");
        for (Node pi_node : pilist) {
            PIcommand cmd;
            String piValue = (pi_node.getNodeValue());
            try {
                cmd = new PIcommand(piValue, m_Definitions);
            } catch (Exception e) {
                m_Reporter.pushMessage("bad_processing_instruction", piValue, e.getMessage());
                continue;
            }
            //--------------------------------------
            // indexlist: <?_wxt ixtable category root
            if (cmd.getCommand().compareToIgnoreCase(PIcommand.IXTABLE) == 0) {
                DocumentFragment df = m_Producer.makePI_IXTable(this, cmd);
                insertDF(df, pi_node);
            } //--------------------------------------
            // imagelist
            else if (cmd.getCommand().compareToIgnoreCase(PIcommand.IMAGELIST) == 0) {
                DocumentFragment df = m_Producer.makePI_ImageList(this, cmd);
                insertDF(df, pi_node);
            } //--------------------------------------
            // formulalist
            else if (cmd.getCommand().compareToIgnoreCase(PIcommand.FORMULALIST) == 0) {
                DocumentFragment df = m_Producer.makePI_FormulaList(this, cmd);
                insertDF(df, pi_node);
            } //-------------------------------------
            // moduletocfinal:<?_wxt moduletocfinal
            else if (cmd.getCommand().compareToIgnoreCase(PIcommand.MODULETOCFINAL) == 0) {
                DocumentFragment df = m_Producer.makePI_ModuleTocFinal(this, cmd);
                insertDF(df, pi_node);
            } //---------------------------------------
            // unknow PI, since this is the last list analysis
            else {
                m_Reporter.pushMessage("unknown_pi", cmd.toString());
            }
        }

        //---------------
        // save it
        save();

        // the last thing we do
        if (m_commandAfter != null) {
            m_commandAfter.executeCommand(m_Reporter);
        }
    }

    /**
     * Save this module
     * @return true if it is ok, false otherwise
     */
    public boolean save() {
        if (m_transformation != null) {
            try {
                domer.saveTransformedDom(m_Doc, m_absoluteUri,
                        m_transformation.getabsoluteURI(),
                        m_transParameters);
                m_lastTimeSaved = System.currentTimeMillis();
                return true;
            } catch (Exception ex) {
                m_Reporter.pushMessage("failed_to_transform_and_write", m_absoluteUri.toString());
            }
        } else {
            try {
                String b = m_Definitions.getOption(Definitions.INDENT_OUTPUT);
                if ((b != null) && (b.compareToIgnoreCase("yes") == 0)) {
                    domer.saveDom(m_Doc, m_absoluteUri, null, true, getOutputFormat());
                } else {
                    domer.saveDom(m_Doc, m_absoluteUri, null, false, getOutputFormat());
                }
                m_lastTimeSaved = System.currentTimeMillis();
                return true;
            } catch (Exception e) {
                m_Reporter.pushMessage("cannot_write_module", m_id, m_absoluteUri.toString());
            }
        }
        return false;
    }

    /**
     * Returns the Element in the script that describes this Module.
     * 
     * @return the script element
     */
    public Element getScriptElement() {
        return m_scriptElement;
    }

    /**
     * Returns the absolute LOCATION that is used as reference, anchor for
     * all addresscalculation in this Module.
     * @return the anchor LOCATION
     */
    public String getAnchorCat() {
        return m_anchorCat;
    }

    /**
     * Return the element in the script that defines this module
     * @return The element
     */
    public Element getElement() {
        return m_scriptElement;
    }

    /**
     * Returns the absolute LOCATION for this module.
     * @return the absolute LOCATION
     */
    public URI getAbsoluteUri() {
        if (m_absoluteUri != null) {
            return m_absoluteUri;
        }
        // since we allow non-content modules (to appear as headers)
        // we redirect to first linkable child
        @SuppressWarnings("unchecked")
        Enumeration<Module> modules = this.depthFirstEnumeration();
        while (modules.hasMoreElements()) {
            Module m = modules.nextElement();
            if (m != null) {
                URI link = m.m_absoluteUri;
                if (link != null) {
                    return link;
                }
            }
        }
        return null;
    }

    /**
     * Returns the catalog this module resides in.
     * @return the absolute catalog
     */
    public String getCatalog() {
        return accessutils.removeFilePart(m_absoluteUri).getPath();
    }

    /**
     * Returns the definitions for this module.
     * @return the definitions
     */
    public Definitions getDefinitions() {
        return m_Definitions;
    }

    /**
     * get the author of this module
     * @return The author
     */
    public Fragment getAuthor() {
        return m_author;
    }

    /**
     * Get the references found in this module
     * @return The reference holder
     */
    public Index getReferenceHolder() {
        return m_referenceHolder;
    }

    /**
     * Returns the DOM.
     * @return the DOM
     */
    public Document getDoc() {
        return m_Doc;
    }

    /**
     * Returns the reporter used by this module.
     * @return the reporter
     */
    public reporter getReporter() {
        return m_Reporter;
    }

    /**
     * Returns the scripthandler controlling this module.
     * @return the Scripthandler
     */
    public Scripthandler getScriptHandler() {
        return m_scriptHandler;
    }

    /**
     * Returns the Producer working for this module.
     * @return the Producer
     */
    public Producer getProducer() {
        return m_Producer;
    }

    /**
     * Returns books as a commaseparated string.
     * @return m_books
     */
    public String getBooks() {
        return m_books;
    }

    /**
     * Check if this module should display in lists and should be linkable
     * A module is not linkable if the books is _never
     * @return true if it is linkabel, false otherwise
     */
    public boolean getLinkable() {
        return m_books.trim().indexOf(PIcommand.NEVER) == -1;
    }

    /**
     * Get the list of all content objects introduced in script
     * @return all ScriptContent objects
     */
    public List<ScriptContent> getScriptContent() {
        return m_ScriptContent;
    }

    /**
     * Get the summary fragment for this module.
     * @return A fragment with the summary, null if not set
     */
    public SummaryFragment getSummary() {
        return m_summary;
    }

    /**
     * Find out if this module is target for a second phase build
     * @return true is so is the case, fals otherwise
     */
    public boolean getSecondPhase() {
        return m_doSecondPhase;
    }

    /**
     * Get the desired outputformat, local or option i definitions
     * xml, html or text
     * @return The set output format
     */
    public String getOutputFormat() {
        if (m_outputFormat == null) {
            return m_Definitions.getOption(Definitions.OUTPUT_FORMAT);
        }
        return m_outputFormat;
    }

    /**
     * Should this module be contained in a named book. 
     * @param bookname The book we will investigate
     * @return true if so is the case, otherwise false
     */
    public boolean getContainedInBook(String bookname) {
        return m_books.indexOf("," + bookname + ",") != -1;
    }

    /**
     * Get the contentobject registered with a certain key
     * @param key The key we look for
     * @return The registered Content object, or null if it does not exist
     */
    public Content getRegisteredContent(String key) {
        return m_PIContent.get(key);
    }

    /**
     * Register a new Contentobject
     * @param key The key
     * @param cont The Content
     * @return The earlier registered Content with this key if any
     */
    public Content registerContent(String key, Content cont) {
        return m_PIContent.put(key, cont);
    }

    /**
     * Insert a DocumentFragment that is the result of a PI-production. 
     * If the DocumentFragments first child is comment-node that contains
     * "removeparent", the parent of the PI is removed, otherwise the PI is replaced by the 
     * DodumentFragment
     * @param df The DocumentFragment produced
     * @param piNode The PI that ordered the fragment
     */
    private void insertDF(DocumentFragment df, Node piNode) {
        if ((df.hasChildNodes()) && (df.getFirstChild().getNodeType() == Node.COMMENT_NODE)) {
            String R = df.getFirstChild().getNodeValue();
            if (R.indexOf(PIcommand.REMOVEPARENT) != -1) {
                piNode.getParentNode().getParentNode().replaceChild(df, piNode.getParentNode());
                return;
            }
        }
        piNode.getParentNode().replaceChild(df, piNode);
    }

    /**
     * Get the description as defined as attribute in script.
     * @return the description
     */
    public String getDescription() {
        if (m_description != null) {
            return m_description;
        }
        return getName();
    }

    /**
     * Get the name as defined as attribute in script. 
     * @return the name
     */
    public String getName() {
        if (m_name != null) {
            return m_name;
        }
        return "No name";
    }

    /**
     * Get the id as defined as attribute in script.
     * 
     * @return the name
     */
    public String getID() {
        if (m_id != null) {
            return m_id;
        }
        return getName();
    }

    /**
     * get the template
     * @return the template
     */
    public Template getTemplate() {
        return m_template;
    }

    /**
     * Get the address where this module will be published
     * @return the publish uri if published catalog is set in definitions,
     * otherwise null
     */
    public String getAbsolutePublishAddress() {
        //String absBuildAddr=m_absoluteUri.getPath().substring(1); // loose / in front
        String absBuildAddr = m_absoluteUri.getPath();
        String absPubAnchorCat = m_Definitions.getPublishCatalog();
        if (absPubAnchorCat.endsWith("/")) {
            absPubAnchorCat = absPubAnchorCat.substring(0, absPubAnchorCat.length() - 1);
        }
        String absBuildAnchorCat = m_Definitions.getBuildCatalog().replace('\\', '/');
        if (absPubAnchorCat == null) {
            return null;
        }
        String tmp = absBuildAddr.replace(absBuildAnchorCat, absPubAnchorCat);
        if (tmp.startsWith("/")) {
            return tmp.substring(1);
        } else {
            return tmp;
        }
        //return absBuildAddr.replace(absBuildAnchorCat, absPubAnchorCat);
    }

    /**
     * Get the encoding.
     * 
     * @return A string or null if no encoding from inputprocess
     */
    public String getEncoding() {
        if (m_Doc != null) {
            return m_Doc.getXmlEncoding();
        }
        return m_Definitions.getOption(Definitions.DEFAULT_ENCODING);
    }

    /**
     * Mark this module with no secondphase mark
     */
    public void clearSecondPhaseMark() {
        m_doSecondPhase = false;
    }

    /**
     * Remember an url as a source for this module
     * @param theUri The URI we store
     */
    public void addDependency(URI theUri) {
        m_sourceUris.add(theUri);
    }

    /**
     * Find out if any of the sources have been updated after last save of this module
     * @return true if so is the case, false otherwise
     */
    public boolean shouldBeUpdated() {
        for (URI ur : m_sourceUris) {
            long t = 0;
            try {
                String tmp = ur.getPath();
                if (!tmp.isEmpty()) {
                    File f = new File(tmp);
                    if (f.exists()) {
                        t = f.lastModified();
                    }
                }
            } catch (Exception ex) {
                // forget it
            }
            if (t > m_lastTimeSaved) {
                return true;
            }
        }
        return false;
    }

    /**
     * Make a link from this module to another.
     * 
     * @param to The Module to link to, the target
     * @param element_type The type of element we want (span, div,?)
     * @param css_className The CSS class we want
     * @param css_base_level If positive we use the value to correct CSS-class levels
     * @param bSummary Trur if we want to display the summary, false otherwise
     * @return A prepared element
     */
    public Element makeXLink(Module to, String element_type,
            String css_className, int css_base_level,
            boolean bSummary) {
        String link = "";
        if (!this.equals(to)) {
            try {
                URI relUri = accessutils.makeRelativeURI(getAbsoluteUri(),
                        to.getAbsoluteUri());
                if (relUri != null) {
                    link = relUri.toString();
                }
            } catch (Exception e) {
                getReporter().pushMessage("bad_cross_ref", to.getName());
            }
        }
        // the link innerwrapper element
        Element eElt = getDoc().createElement(element_type);

        // adjust style for level
        // was:
        //if ((css_className!=null)&&(css_base_level != Producer.NO_LEVEL_ADJUST))
        //    css_className = css_className + (to.getLevel() + 1);

        if ((css_className != null) && (css_base_level > -999)) {
            css_className = css_className + (to.getLevel() + 1);
        }

        // link to self as a simple string, textnode
        if (this.equals(to)) {
            if (css_className != null) {
                eElt.setAttribute("class", css_className+" "+PIcommand.WXTSTYLEPREFIX+"selected");
            }
            eElt.appendChild(getDoc().createTextNode(to.getName()));
            return eElt;
        }

        // set style
        if (css_className != null) {
            eElt.setAttribute("class", css_className);
        }

        // find a unique id to avoid mixup of multiple ref on one module
        String useID = getScriptHandler().getANewId(getID());

        // the actual link element
        Element aElt = getDoc().createElement("a");

        // if summary is set we want a popup
        if (bSummary) {
            aElt.setAttribute("onmouseout", "javascript:popunpop('" + useID + "',event);");
            aElt.setAttribute("onmouseover", "javascript:popunpop('" + useID + "',event);");
        } else {
            aElt.setAttribute("title", to.getDescription());
        }

        aElt.setAttribute("href", link);
        aElt.appendChild(getDoc().createTextNode(to.getName()));
        eElt.appendChild(aElt);

        if (bSummary) {
            // this is the element that should pop
            Element fragWrapper = getDoc().createElement("div");
            fragWrapper.setAttribute("class", PIcommand.WXTSTYLEPREFIX + "summary");
            fragWrapper.setAttribute("style", "display: none;");
            fragWrapper.setAttribute("id", useID);

            // produce a header for the pop fragment
            Element headElt = getDoc().createElement("div");
            headElt.setAttribute("class", "header");
            headElt.appendChild(getDoc().createTextNode(to.getName()));
            fragWrapper.appendChild(headElt);

            // get and append the content
            DocumentFragment theFragmentDF = to.getSummary().produceIntextElement(this, null);
            Producer.correctAddressing(theFragmentDF, to, getAbsoluteUri());
            fragWrapper.appendChild(getDoc().importNode(theFragmentDF, true));

            // add it to the package that already contains the link
            eElt.appendChild(fragWrapper);
        }
        return eElt;
    }

    @Override
    public String toString() {
        String tmp = "-no-location-";
        if (m_absoluteUri != null) {
            tmp = m_absoluteUri.toString();
        }
        String s = "\nModule: " + m_name + " at: " + tmp + " with anchor: " + m_anchorCat;

        s += "\nCommands:";

        if (m_commandBefore == null && m_commandAfter == null) {
            s += " none\n";
        } else {
            if (m_commandBefore != null) {
                s += "\n\tbefore: " + m_commandBefore;
            }
            if (m_commandAfter != null) {
                s += "\n\tafter: " + m_commandAfter;
            }
        }

        s += "\nContents:";
        // XMLContent
        for (ScriptContent xc : m_ScriptContent) {
            s += xc.toString();
        }


        // child modules
        if (this.getChildCount() != 0) {
            Module m = (Module) this.getFirstChild();
            while (m != null) {
                s += "\n\t" + m.toString();
                m = (Module) m.getNextSibling();
            }
        }
        return s;
    }

    /**
     * Produce a string suitable for module inspection in a GUI
     * @return The produced string
     */
    public String Inspector() {
        String tdstartcol2 = "<td> <font color=blue>";
        StringBuilder sb = new StringBuilder(1024);

        // attributes
        sb.append("<html>\n");
        sb.append("<table>");

        sb.append("<tr>");
        sb.append("<td>Name</td>").append(tdstartcol2).append(m_name).append("</td>");
        sb.append("</tr>");

        sb.append("<tr>");
        sb.append("<td>Id</td>").append(tdstartcol2).append(m_id).append("</td>");
        sb.append("</tr>");

        sb.append("<tr>");
        sb.append("<td>Description</td>").append(tdstartcol2).append(m_description).append("</td>");
        sb.append("</tr>");

        sb.append("<tr>");
        if (m_absoluteUri != null) {
            sb.append("<td>Location</td>").append(tdstartcol2).append(m_absoluteUri.toString()).append("</td>");
        } else {
            sb.append("<td>Location</td>").append(tdstartcol2).append("null</td>");
        }
        sb.append("</tr>");

        sb.append("<tr>");
        if (m_template != null) {
            sb.append("<td>Template</td>").append(tdstartcol2).append(m_template.getabsoluteURI().toString()).append("</td>");
        } else {
            sb.append("<td>Template</td>").append(tdstartcol2).append("-</td>");
        }
        sb.append("</tr>");

        sb.append("<tr>");
        if (m_transformation != null) {
            sb.append("<td>Transformation</td>").append(tdstartcol2).append(m_transformation.getabsoluteURI().toString()).append("</td>");
        } else {
            sb.append("<td>Transformation</td>").append(tdstartcol2).append("-</td>");
        }
        sb.append("</tr>");

        sb.append("<tr>");
        if (m_anchorCat != null) {
            sb.append("<td>Anchor</td>").append(tdstartcol2).append(m_anchorCat.toString()).append("</td>");
        } else {
            sb.append("<td>Anchor</td>").append(tdstartcol2).append("-</td>");
        }
        sb.append("</tr>");

        sb.append("<tr>");
        if (m_books != null) {
            sb.append("<td>Books</td>").append(tdstartcol2).append(m_books).append("</td>");
        } else {
            sb.append("<td>Books</td>").append(tdstartcol2).append("-</td>");
        }
        sb.append("</tr>");

        sb.append("<tr>");
        if (m_outputFormat != null) {
            sb.append("<td>Output Format</td>").append(tdstartcol2).append(m_outputFormat).append("</td>");
        } else {
            sb.append("<td>Output Format</td>").append(tdstartcol2).append("-</td>");
        }
        sb.append("</tr>");

        sb.append("<tr>");
        if (m_commandBefore != null) {
            sb.append("<td>Run Before</td>").append(tdstartcol2).append(m_commandBefore.getCommandLine()).append("</td>");
        } else {
            sb.append("<td>Run Before</td>").append(tdstartcol2).append("-</td>");
        }
        sb.append("</tr>");

        sb.append("<tr>");
        if (m_commandAfter != null) {
            sb.append("<td>Run After</td>").append(tdstartcol2).append(m_commandAfter.getCommandLine()).append("</td>");
        } else {
            sb.append("<td>Run After</td>").append(tdstartcol2).append("-</td>");
        }
        sb.append("</tr>");



        // summary
        tdstartcol2 = "<td> <font color=red>";

        sb.append("<tr>");
        sb.append("<td>Summary</td>").append(tdstartcol2).append(getSummary().getFragmentAsString()).append("</td>");
        sb.append("</tr>");



        // content
        tdstartcol2 = "<td> <font color=green>";

        for (int ix = 0; ix < m_ScriptContent.size(); ix++) {
            if (!(m_ScriptContent.get(ix).getType().compareTo(Module.XMLCONTENT) == 0)) {
                continue;
            }
            String bup = "";
            if (m_ScriptContent.get(ix).usingBackup()) {
                bup = "(backup)";
            }
            sb.append("<tr>");
            sb.append("<td>XMLContent").append(bup).append("</td>").append(tdstartcol2).append(m_ScriptContent.get(ix).getAbsoluteEffectiveUri().toString()).append("</td>");
            sb.append("</tr>");
        }
        for (int ix = 0; ix < m_ScriptContent.size(); ix++) {
            if (!(m_ScriptContent.get(ix).getType().compareTo(Module.TXTCONTENT) == 0)) {
                continue;
            }
            String bup = "";
            if (m_ScriptContent.get(ix).usingBackup()) {
                bup = "(backup)";
            }
            sb.append("<tr>");
            sb.append("<td>TXTContent").append(bup).append("</td>").append(tdstartcol2).append(m_ScriptContent.get(ix).getAbsoluteEffectiveUri().toString()).append("</td>");
            sb.append("</tr>");
        }

        sb.append("</table>");
        return sb.toString();
    }
}

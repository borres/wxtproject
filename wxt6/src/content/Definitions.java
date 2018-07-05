package content;

import Formulas.Formula;
import Formulas.FormulaUtils;
import commands.CommandLine;
import fragments.AuthorFragment;
import fragments.Frag;
import fragments.Fragment;
import indexing.Index;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import references.Reference;
import reporting.reporter;
import reporting.validationErrorHandler;
import utils.accessutils;
import wimages.Image;
import words.Word;
import wxt.Options;
import wxt.Scripthandler;
import wxtresources.resourceHandler;
import xmldom.ITidyXML;
import xmldom.domer;

/**
 * Hold all definitions for a script.
 * <p>
 * These definitions are collected from Element <strong>definitions</strong> in the script.
 * <p> 
 * Element definitions must be a direct child of the rootnode of the script
 * @author borres
 */
public class Definitions {
    
    /**names in script*/
    public static final String REFERENCES = "references";
    public static final String LOCATION = "location";
    public static final String ID = "id";
    public static final String VALUE = "value";
    public static final String ALTERNATIVE = "alternative";
    public static final String FORMULAS = "formulas";
    public static final String ODTFORMULAS = "odtformulas";
    public static final String TEXFORMULAS = "texformulas";
    public static final String IMAGES = "images";
    public static final String ANCHOR = "anchor";
    public static final String PUBANCHOR = "pubanchor";
    public static final String FRAGMENT = "fragment";
    public static final String PATHFRAGMENT = "pathfragment";
    public static final String FRAGMENTS = "fragments";
    public static final String TEMPLATE = "template";
    public static final String TRANSFORMATION = "transformation";
    public static final String OPTION = "option";
    public static final String COMMAND = "command";
    public static final String LINE = "line";
    public static final String WAIT = "wait";
    public static final String LOGFILE = "logfile";
    public static final String MAXLOG = "maxlog";
    public static final String AUTHOR = "author";
    public static final String AUTHORS = "authors";
    public static final String DEFAULT = "_default";
    public static final String DEFAULTAUTHOR = "_defaultauthor";
    public static final String SITEAUTHOR = "Site author";
    public static final String SHORT = "short";
    public static final String GOOGLEBACKUP = "googleFormulas";
    public static final String JLATEXBACKUP = "jlatexmath";
    public static final String MATHMLBACKUP = "mathmlFormulas";
    
    /** added to javas default tempcatalog as in property: java.io.tmpdir*/
    private static final String TEMP_WXT_CAT = "WXT_TMP";
    /** ref to the actual scripthandler that sets up this Definitions object */
    Scripthandler m_scriptHandler;
    /** the Element in the script that holds the definition element*/
    Element m_definitionElement;
    /** the reporter to use*/
    reporter m_reporter;
    /** a string with a catalogpath.*/
    private String m_anchorCat;
    /** the catalog where the site is published*/
    private String m_publishedCat;
    /** pathfragments */
    HashMap<String, String> m_pathfragments;
    /** xml fragments */
    HashMap<String, Fragment> m_fragments;
    /** authors */
    HashMap<String, Fragment> m_authors;
    /** templates */
    HashMap<String, Template> m_templates;
    /** transformations */
    HashMap<String, Transformation> m_transformations;
    /** Commands */
    HashMap<String, CommandLine> m_commands;
    /** All removables references in reference-file elements in script */
    HashMap<String, Reference> m_references;
    /** holding used references as we build */
    Index m_usedReferences;
    /** holding used images as we build */
    Index m_usedImages;
    /** holding used formulas as we build */
    Index m_usedFormulas;
    /** holding all used indexable words */
    Index m_usedWords;
    /** holding all used authors */
    Index m_usedAuthors;
    /** Candidates for address corrections when a fragment is moved 
    key is tagname, value is attribute, ie key:a  value:href */
    HashMap<String, String> m_addressing;
    /** holding images*/
    HashMap<String, Image> m_images;
    /** holding LaTextfomulas*/
    HashMap<String, Formula> m_formulas;
    /** holding all registered indexable word*/
    HashMap<String, Word> m_words;
    /** holding a OldTidyEngine*/
    ITidyXML m_tidyTool;
    /** temporary dir*/
    String m_tempDir;


    /**
     * Constructor that set up a definition object with only defaults.
     * 
     * @param sh The Scriphandler that owns the script
     */
    public Definitions(Scripthandler sh) {
        m_scriptHandler = sh;
        m_reporter = m_scriptHandler.getReporter();
        m_anchorCat = m_scriptHandler.getScriptCatalog();
        m_publishedCat = null;
        m_pathfragments = new HashMap<>();
        m_fragments = new HashMap<>();
        m_authors = new HashMap<>();
        m_templates = new HashMap<>();
        m_transformations = new HashMap<>();
        m_commands = new HashMap<>();
        m_definitionElement = null;
        m_addressing = new HashMap<>();
        m_references = new HashMap<>();
        m_usedReferences = new Index();
        m_usedImages = new Index();
        m_usedFormulas = new Index();
        m_usedAuthors = new Index();
        m_formulas = new HashMap<>();
        m_images = new HashMap<>();
        m_words = new HashMap<>();
        m_usedWords = new Index();
        m_tidyTool = null;
        m_tempDir = null;
        // make sure we have a temp directory
        setTempCatalog(null);

    }



    /**
     * Collect all data from the script element and set up all definitions
     */
    public void collectFromScript(Element defelt) {
        m_definitionElement = defelt;
        // even if we have no children in the 
        // definitions - element we must set up defaults
        /*if (!m_definitionElement.hasChildNodes()) {
            return;
        }*/

        // we check if we have an attribute, anchor, that
        // change the reference from scriptpaths catalog
        if (m_definitionElement.hasAttribute(ANCHOR)) {
            m_anchorCat = m_definitionElement.getAttribute(ANCHOR);
        }

        // we cehck if we have an attribute, published, that
        // names the catalog where the site will be published
        if (m_definitionElement.hasAttribute(PUBANCHOR)) {
            m_publishedCat = m_definitionElement.getAttribute(PUBANCHOR);
        }


        // -----------------------------------------------------------
        // we must do this collection in this sequence
        //------------------------------------------------------------


        //-------------------------------------------------
        // ------ pathfragments first since we need these in other adressing
        // set up defaults
        String s = m_scriptHandler.getScriptCatalog();
        String stmp = m_scriptHandler.getScriptAbsoluteUri().toString();
        m_pathfragments.put("_scriptcatalog", m_scriptHandler.getScriptCatalog());
        //m_pathfragments.put("_scriptpath", m_scriptHandler.getScriptCatalog());
        m_pathfragments.put("_scripturi", m_scriptHandler.getScriptAbsoluteUri().toString());
        if (m_anchorCat != null) {
            m_pathfragments.put("_scriptanchor", m_anchorCat);
        }
        if (m_publishedCat != null) {
            m_pathfragments.put("_pubanchor", m_publishedCat);
        }

        NodeList nlist = m_definitionElement.getElementsByTagName(PATHFRAGMENT);
        for (int ix = 0; ix < nlist.getLength(); ix++) {
            Element elt = (Element) nlist.item(ix);
            if ((elt.hasAttribute(ID)) && (elt.hasAttribute(VALUE))) {
                String val = elt.getAttribute(VALUE);

                if (elt.hasAttribute(ALTERNATIVE)) {
                    // which pathfragment to choose depends on the OS 
                    val = accessutils.selectPathValue(val, elt.getAttribute(ALTERNATIVE));
                }
                val = substituteFragments(val);
                m_pathfragments.put(elt.getAttribute(ID), val);
            } else {
                m_reporter.pushMessage("bad_pathfragment_element");
            }
        }

        //-------------------------------------------------
        // -- options

        //  override from script
        nlist = m_definitionElement.getElementsByTagName(OPTION);
        for (int ix = 0; ix < nlist.getLength(); ix++) {
            Element elt = (Element) nlist.item(ix);
            String tmpname = elt.getAttribute("name").toLowerCase();
            String tmpvalue = elt.getAttribute("value");
            if (tmpvalue.length() > 1) {
                tmpvalue = tmpvalue.toLowerCase();
                if(!m_scriptHandler.setScriptOption(tmpname, tmpvalue)){
                    m_reporter.pushMessage("bad_option",tmpname);
                }
            }
        }
 
        // -- set default encoding in accesutils and domer
        // use the updated scriptoption
        String defaultEncoding=m_scriptHandler.getScriptOption(Options.DEFAULT_ENCODING);
        accessutils.setDefaultEncoding(defaultEncoding);
        domer.setDefaultEncoding(defaultEncoding);
        // or:
        
        

        // -- tidy engine
        // we set up the necessary tidy object anyhow
        // options will control usage
        m_tidyTool=new xmldom.JSoupTidy(defaultEncoding);
        //or:
        // null is sent as parameterfile, menas we used default see OldTidyEngine
        //m_tidyEngine = new OldTidyEngine(defaultEncoding, null, m_reporter);


        //---------------------------------------
        // -- addressing elements
        // set up default addressing
        m_addressing.put("a", "href");
        m_addressing.put("link", "href");
        m_addressing.put("img", "src");
        m_addressing.put("script", "src");
        nlist = m_definitionElement.getElementsByTagName("addressing");
        for (int ix = 0; ix < nlist.getLength(); ix++) {
            Element elt = (Element) nlist.item(ix);
            String tmpname = elt.getAttribute("tag");
            String tmpvalue = elt.getAttribute("attribute");
            if (elt.hasAttribute("cancel") && (elt.getAttribute("cancel").compareToIgnoreCase("yes") == 0)) {
                m_addressing.remove(tmpname);
            } else {
                m_addressing.put(tmpname, tmpvalue);
            }
        }
        /*
        // key none clears all, also the defaults
        if (m_addressing.get("none") != null) {
        m_addressing.clear();
        }
         */



        //-------------------------------------------
        // ---- single fragments
        nlist = m_definitionElement.getElementsByTagName(FRAGMENT);
        // static function in Frag handles this
        Frag.makeFragments(nlist, this, FRAGMENT);

        //----------------------------------------
        // ----  files with multiple fragments ---------
        nlist = m_definitionElement.getElementsByTagName(FRAGMENTS);
        for (int ix = 0; ix < nlist.getLength(); ix++) {
            Element elt = (Element) nlist.item(ix);
            if (!elt.hasAttribute(LOCATION)) {
                m_reporter.pushMessage("missing_location_in_fragments_element");
                continue;
            }
            String location = elt.getAttribute(LOCATION);
            location = substituteFragments(location);

            try {
                unWrapFragmentsFromFragmentFile(location, FRAGMENT);
            } catch (Exception e) {
                m_reporter.pushMessage("bad_fragments_file", e.getMessage());
            }
        }


        //-------------------------------------------
        // --- single authors
        nlist = m_definitionElement.getElementsByTagName(AUTHOR);
        // static function in Frag handles this
        Frag.makeFragments(nlist, this, AUTHOR);

        //----------------------------------------
        // ----  files with multiple authors (fragments) ---------
        nlist = m_definitionElement.getElementsByTagName(AUTHORS);
        for (int ix = 0; ix < nlist.getLength(); ix++) {
            Element elt = (Element) nlist.item(ix);
            if (!elt.hasAttribute(LOCATION)) {
                m_reporter.pushMessage("missing_location_in_fragments_element");
                continue;
            }
            String location = elt.getAttribute(LOCATION);
            location = substituteFragments(location);

            try {
                unWrapFragmentsFromFragmentFile(location, AUTHOR);
            } catch (Exception e) {
                m_reporter.pushMessage("bad_fragments_file", e.getMessage());
            }

        }


        //------- after all fragmententries, set default author if not done -----------
        // thus all modules will have an author
        if (!m_authors.containsKey(DEFAULTAUTHOR)) {
            Element elt = m_definitionElement.getOwnerDocument().createElement(AUTHOR);
            elt.setAttribute(ID, DEFAULTAUTHOR);
            elt.setAttribute(SHORT, SITEAUTHOR);
            elt.appendChild(m_definitionElement.getOwnerDocument().createTextNode(SITEAUTHOR));
            m_definitionElement.appendChild(elt);
            try {
                m_authors.put(DEFAULTAUTHOR, new AuthorFragment(elt, null, this));
            } catch (Exception ex) {
            }
        }

        //-------------------------------------------
        // ----  templates -----
        nlist = m_definitionElement.getElementsByTagName(TEMPLATE);
        for (int ix = 0; ix < nlist.getLength(); ix++) {
            Element elt = (Element) nlist.item(ix);
            try {
                String tmp = elt.getAttribute(LOCATION);
                tmp = substituteFragments(tmp);
                URI tmpuri = accessutils.makeAbsoluteURI(tmp, m_anchorCat);
                Template t = new Template(elt.getAttribute(ID), tmpuri);
                m_templates.put(elt.getAttribute(ID), t);
            } catch (Exception e) {
                m_reporter.pushMessage("bad_template_element", e.getMessage());
            }
        }


        //-----------------------------
        // ----  transformations -----
        nlist = m_definitionElement.getElementsByTagName(TRANSFORMATION);
        for (int ix = 0; ix < nlist.getLength(); ix++) {
            Element elt = (Element) nlist.item(ix);
            try {
                String tmp = elt.getAttribute(LOCATION);
                tmp = substituteFragments(tmp);
                URI tmpuri = accessutils.makeAbsoluteURI(tmp, m_anchorCat);
                Transformation t = new Transformation(elt.getAttribute(ID), tmpuri);
                m_transformations.put(elt.getAttribute(ID), t);
            } catch (Exception e) {
                m_reporter.pushMessage("bad_transformation_element", e.getMessage());
            }
        }


        //-----------------------------
        // --- commands -----
        nlist = nlist = m_definitionElement.getElementsByTagName(COMMAND);
        for (int ix = 0; ix < nlist.getLength(); ix++) {
            Element elt = (Element) nlist.item(ix);
            try {
                String cid = elt.getAttribute(ID);
                String line = elt.getAttribute(LINE);
                line = substituteFragments(line);
                if (elt.hasAttribute(WAIT) && elt.getAttribute(WAIT).equalsIgnoreCase(Options.NO)) {
                    m_commands.put(cid, new CommandLine(cid, line, false));
                } else {
                    m_commands.put(cid, new CommandLine(cid, line, true));
                }

            } catch (Exception e) {
                m_reporter.pushMessage("bad_command_element", e.getMessage());
            }
        }


        //----------------------------------
        // --- reference-lists ---
        nlist = m_definitionElement.getElementsByTagName(REFERENCES);
        for (int ix = 0; ix < nlist.getLength(); ix++) {
            Element elt = (Element) nlist.item(ix);
            try {
                String location = elt.getAttribute(LOCATION);
                location = substituteFragments(location);
                URI uri = accessutils.makeAbsoluteURI(location, m_anchorCat);
                Document refdoc = domer.makeDomFromUri(uri,false, null);

                // validate the file
                String schemaString = resourceHandler.getReferenceSchema();
                validationErrorHandler eHandler = new validationErrorHandler(true);
                String result = domer.validateDomFromString(refdoc, schemaString, eHandler);
                String rep = eHandler.getReport();
                if (result != null) {
                    m_reporter.pushSimpleMessage("Reference validation failed:" + location + "\n");
                } else {
                    m_reporter.pushSimpleMessage("Reference validation: " + location + "\n" + rep + "\n");
                }


                Node n_doc = refdoc.getDocumentElement();
                Element e_doc = (Element) n_doc;


                Node n = e_doc.getFirstChild();
                while (n != null) {
                    if (n.getNodeType() == Node.ELEMENT_NODE) {

                        Element refelt = (Element) n;
                        try {
                            // we sore all as simple references
                            // how to display them are determined when we do it.
                            Reference r = new Reference(refelt, m_reporter);
                            String id = r.getid();
                            if (m_references.containsKey(id)) {
                                m_reporter.pushMessage("reference_collision", r.getid());
                            } else {
                                m_references.put(id, r);
                            }

                        } catch (Exception e) {
                            m_reporter.pushMessage("bad_reflist_element", e.getMessage());
                        }
                    }
                    n = n.getNextSibling();
                }
            } catch (Exception e) {
                //System.out.println(e);
                m_reporter.pushMessage("bad_reflist_file", e.getMessage());

            }
        }



        //----------------------------------
        //------ odtformulas------
        nlist = m_definitionElement.getElementsByTagName(ODTFORMULAS);
        for (int ix = 0; ix < nlist.getLength(); ix++) {
            Element elt = (Element) nlist.item(ix);
            URI sourceUri = null;
            try {
                String location = elt.getAttribute(LOCATION);
                location = substituteFragments(location);
                sourceUri = accessutils.makeAbsoluteURI(location, m_anchorCat);
                FormulaUtils.makeMathMlFormulasFromOdt(m_reporter, sourceUri, getTempCatalog(), this);
            } catch (Exception e) {
                //System.out.println(e);
                m_reporter.pushMessage("bad_formula_file", sourceUri.toString() + " : " + e.getMessage());
            }
        }

        //----------------------------------
        //------ latex,img,mathml formulas------
        nlist = m_definitionElement.getElementsByTagName(FORMULAS);
        for (int ix = 0; ix < nlist.getLength(); ix++) {
            Element elt = (Element) nlist.item(ix);
            URI sourceUri = null;
            try {
                String location = elt.getAttribute(LOCATION);
                location = substituteFragments(location);
                sourceUri = accessutils.makeAbsoluteURI(location, m_anchorCat);
                FormulaUtils.addFormulasFromFormulaFile(m_reporter, sourceUri, m_scriptHandler, this);
            } catch (Exception e) {
                //System.out.println(e);
                m_reporter.pushMessage("bad_formula_file", sourceUri.toString() + " : " + e.getMessage());
            }
        }

        //--------------------------
        //--------  images -------
        nlist = m_definitionElement.getElementsByTagName(IMAGES);
        for (int ix = 0; ix < nlist.getLength(); ix++) {
            Element elt = (Element) nlist.item(ix);
            if (!elt.hasAttribute(LOCATION)) {
                m_reporter.pushMessage("missing_location_in_imageelement");
                continue;
                //throw new Exception(reporter.getBundleString("missing_location_in_imageelement"));
            }
            String location = elt.getAttribute(LOCATION);
            location = substituteFragments(location);

            try {
                unWrapImagesFromImageFile(location);
            } catch (Exception e) {
                m_reporter.pushMessage("bad_image_file", e.getMessage());
            }
        }
        nlist = m_definitionElement.getElementsByTagName(LOGFILE);
        if (nlist.getLength() > 0) {
            Element elt = (Element) nlist.item(0);
            long maxSize = -1;
            if (elt.hasAttribute(MAXLOG)) {
                try {
                    maxSize = Long.parseLong(elt.getAttribute(MAXLOG));
                } catch (Exception lex) {
                    m_reporter.pushMessage("bad_maxlog_in_logfileelement");
                }
            }
            if (elt.hasAttribute(LOCATION)) {
                try {
                    String location = elt.getAttribute(LOCATION);
                    location = substituteFragments(location);
                    URI theUri = accessutils.makeAbsoluteURI(location, m_anchorCat);
                    m_reporter.setLogFileURI(theUri);
                    if (maxSize >= 0) {
                        m_reporter.setLogFileMaxlength(maxSize);
                    }
                } catch (Exception uex) {
                    m_reporter.pushMessage("bad_location_in_logfileelement");
                }
            } else {
                m_reporter.pushMessage("missing_location_in_logfileelement");
            }
        }
    }

    /**
     * Load a file with imagedefinitions and create images
     * @param location The location of the file
     * @throws java.lang.Exception when we dont succeed
     */
    private void unWrapImagesFromImageFile(String location)
            throws Exception {
        URI sourceUri = accessutils.makeAbsoluteURI(location, m_anchorCat);
        Document imDoc = domer.makeDomFromUri(sourceUri, false,null);
        //validation goes here
        String schemaString = resourceHandler.getImagesSchema();
        validationErrorHandler eHandler = new validationErrorHandler(true);
        String result = domer.validateDomFromString(imDoc, schemaString, eHandler);
        String rep = eHandler.getReport();
        if (result != null) {
            m_reporter.pushSimpleMessage("Images validation failed:" + location + "\n");
        } else {
            m_reporter.pushSimpleMessage("Images validation:" + location + "\n" + rep + "\n");
        }

        // we look for a catalog element giving the
        // root and anchor for all images listed
        // if not removables we assume that the file is in same catalog as images
        if (imDoc.getDocumentElement().hasAttribute("catalog")) {
            // we use this removables catalog as anchor
            // in stead of the catalog in sourceUri
            String srcCatStr = imDoc.getDocumentElement().getAttribute("catalog");
            srcCatStr = substituteFragments(srcCatStr);
            //srcCatStr=srcCatStr.replace('\\', '/');
            //srcCatStr=srcCatStr.replaceAll(" ", "%20");
            // we must try to make sourceUri from the scrCatStr
            try {
                sourceUri = accessutils.makeAbsoluteURI(srcCatStr, sourceUri.getPath());
            } catch (Exception lex) {
                // anything we can do about this ?
                throw lex;
            }
        }

        NodeList imlist = imDoc.getElementsByTagName("image");
        for (int imix = 0; imix < imlist.getLength(); imix++) {
            try {
                Image im = new Image((Element) imlist.item(imix), sourceUri, this);
                registerNewImage(im);
            } catch (Exception imex) {
                //String t=imex.getMessage();
                m_reporter.pushMessage(imex.getMessage());
            }
        }
    }

    /**
     * register a new image
     * @param im The image to register
     */
    public void registerNewImage(Image im) {
        if (m_images.containsKey(im.getID())) {
            m_reporter.pushMessage("duplicate_image_id", im.getID());
        }

        m_images.put(im.getID(), im);
    }

    /**
     * register a new texFormula
     * @param tf The formula to register
     */
    public void registerNewFormula(Formula tf) {
        String id = tf.getId();
        if (m_formulas.containsKey(id)) {
            m_reporter.pushMessage("duplicate_formula_id", id);
        }

        m_formulas.put(id, tf);
    }

    /**
     * register a indexable word
     * @param w 
     */
    public void registerNewWord(Word w) {
        String id = w.getWord();
        if (!m_words.containsKey(id)) {
            m_words.put(id, w);
        }
    }

    /**
     * Register a fragment
     * @param f The fragment to register
     */
    public void registerNewFragment(Fragment f) {
        String id = f.getId();
        if (m_fragments.containsKey(id) || m_authors.containsKey(id)) {
            m_reporter.pushMessage("duplicate_fragment_id", id);
        }
        m_fragments.put(id, f);
    }

    /**
     * Register an author
     * @param f The fragment to register
     */
    public void registerNewAuthor(Fragment f) {
        String id = f.getId();
        if (m_authors.containsKey(id) || m_fragments.containsKey(id)) {
            m_reporter.pushMessage("duplicate_fragment_id", id);
        }
        m_authors.put(id, f);
    }

    /** 
     * Substitute all pathfragments in a string
     * Old style fragments bracketed in {}
     * <p>
     * An old pathfragment is reckognized as enclosed in {}.
     * 
     * @param loc The string to work on
     * @return The modified string
     */
    private String oldSubstituteFragments(String loc) {
        // any fragment bracket at all
        if (loc.indexOf('{') == -1) {
            return loc;
        }


        // must check them all       
        Set<String> keys = m_pathfragments.keySet();
        for (Iterator<String> k = keys.iterator(); k.hasNext();) {
            String ks = k.next();
            String sub = m_pathfragments.get(ks);

            // to make \ survive
            sub = sub.replace("\\", "\\\\");
            String ktmp = "\\Q{" + ks + "}\\E";
            loc = loc.replaceAll(ktmp, sub);
            if (loc.indexOf('{') == -1) {
                return loc;
            }
        }
        return loc;
    }

    /**
     * Substitute all pathfragments in a string
     * New style fragments bracketed in ${}
     * <p>
     * A pathfragment is reckognized as enclosed in ${}.
     *
     * @param loc The string to work on
     * @return The modified string
     */
    public String substituteFragments(String loc) {
        // cannot have old and new in same string
        // any fragment bracket at all (old or new)
        if (loc.indexOf('{') == -1) {
            return loc;
        }
        // handle old deprecated version with {} brackets
        if (loc.indexOf("${") == -1) {
            return oldSubstituteFragments(loc);
        }


        // must check them all
        Set<String> keys = m_pathfragments.keySet();
        for (Iterator<String> k = keys.iterator(); k.hasNext();) {
            String ks = k.next();
            String sub = m_pathfragments.get(ks);

            // to make \ survive
            sub = sub.replace("\\", "\\\\");
            String ktmp = "\\Q${" + ks + "}\\E";
            loc = loc.replaceAll(ktmp, sub);
            if (loc.indexOf("${") == -1) {
                return loc;
            }
        }
        if (loc.indexOf("${") != -1) {
            // an unknown pathfragment
            m_reporter.pushMessage("unknown_patfragment", loc);
        }
        return loc;
    }

    /** 
     * Add a template.
     * 
     * @param id The id, key for the template
     * @param t The Template
     */
    public void addTemplate(String id, Template t) {
        m_templates.put(id, t);
    }

    /** 
     * Get a template.
     * 
     * @param id The id, key for the template
     * @return The Template if it exists, otherwise null
     */
    public Template getTemplate(String id) {
        if (m_templates.containsKey(id)) {
            return m_templates.get(id);
        }
        return null;
    }

    /** 
     * Get a Command.
     *  
     * @param id
     * @return The Commans if it exists, otherwise null 
     */
    public CommandLine getCommand(String id) {
        if (m_commands.containsKey(id)) {
            return m_commands.get(id);
        }
        return null;
    }

    /** 
     * Get a transformation.
     * 
     * @param id The id, key for the transformation
     * @return The Transformation if it exists, otherwise null
     */
    public Transformation getTransformation(String id) {
        if (m_transformations.containsKey(id)) {
            return m_transformations.get(id);
        }
        return null;
    }

    /**
     * Get the scripthandler
     * @return The actual scripthandler
     */
    public Scripthandler getScriptHandler() {
        return m_scriptHandler;
    }

    /**
     * 
     * @return the OldTidyEngine object if it exists
     */
    public ITidyXML getTidyTool() {
        // may be null, if tidy option is no
        return m_tidyTool;
    }

    /**
     * References used so far
     * @return the used refernces
     */
    public Index getReferenceHolder() {
        return m_usedReferences;
    }

    /**
     * All images
     * @return the used images
     */
    public Index getImageHolder() {
        return m_usedImages;
    }

    /**
     * Indexable words used so far
     * @return the used words
     */
    public Index getWordHolder() {
        return m_usedWords;
    }

    /**
     * Formulas
     * @return the registered formulas
     */
    public Index getFormulaHolder() {
        return m_usedFormulas;
    }

    /**
     * Authors 
     * @return the registered authors
     */
    public Index getAuthorHolder() {
        return m_usedAuthors;
    }

    /**
     * get all refrences
     * @return The Map with references
     */
    public HashMap<String, Reference> getReferences() {
        return m_references;
    }

    /**
     * Get a certain reference by id
     * @param id The id of the refrence we want
     * @return The actual reference, null if it does not exist
     */
    public Reference getReference(String id) {
        if (m_references.containsKey(id)) {
            return m_references.get(id);
        }
        return null;
    }

    /**
     * get the publish anchor
     * @return the catalog where we will publish, may be null
     */
    public String getPublishCatalog() {
        return m_publishedCat;
    }

    /**
     * get the build anchor
     * @return the catalog where we are building
     */
    public String getBuildCatalog() {
        return m_anchorCat;
    }

    /** 
     * Add a transformation.
     * 
     * @param id The id, key for the transformation
     * @param t The Transformation
     */
    public void addTransformation(String id, Transformation t) {
        m_transformations.put(id, t);
    }

    /** 
     * Get a Fragments.
     * @param id The id of the fragment
     * @return The fragment, null if not found
     */
    public Fragment getFragment(String id) {
        if (m_fragments.containsKey(id)) {
            return m_fragments.get(id);
        }
        return null;
    }

    /**
     * Get an author
     * @param id The id of the author
     * @return  The fragment, null if not found
     */
    public Fragment getAuthor(String id) {
        if (m_authors.containsKey(id)) {
            return m_authors.get(id);
        }
        return null;
    }

    /**
     * Get a formula by id
     * @param fid The id
     * @return The formula if found, otherwise null
     */
    public Formula getFormula(String fid) {
        if (m_formulas.containsKey(fid)) {
            return m_formulas.get(fid);
        }
        return null;
    }

    /**
     * Get a image by id
     * @param fid The id
     * @return The image if found, otherwise null
     */
    public Image getImage(String imid) {
        if (m_images.containsKey(imid)) {
            return m_images.get(imid);
        }
        return null;
    }

    /**
     * Get a word by id
     * @param fid The id
     * @return The word if found, otherwise null
     */
    public Word getWord(String id) {
        if (m_words.containsKey(id)) {
            return m_words.get(id);
        }
        return null;

    }

    /**
     * Get all words
     * @return The words as a map
     */
    public HashMap<String, Word> getAllWords() {
        return m_words;
    }



    /**
     * Setting the catalog for temporary files
     * @param s The catalogpath, or null which will use "java.io.tmpdir"
     */
    private void setTempCatalog(String s) {
        if (s == null) {
            s = System.getProperty("java.io.tmpdir");
        }
        if (!(s.endsWith("/") || s.endsWith("\\"))) {
            s = s + System.getProperty("file.separator");
        }
        s = s + TEMP_WXT_CAT + System.getProperty("file.separator");
        accessutils.makeCatalog(s + "tmp.txt");
        m_tempDir = s;
    }

    /**
     * Remove the catalog for temporary files
     */
    public void removeTempCatalog() {
        if (m_tempDir != null) {
            // we must make sure that we dont delete something wrong
            String tmp = System.getProperty("file.separator") + TEMP_WXT_CAT + System.getProperty("file.separator");
            if (m_tempDir.endsWith(tmp)) {
                accessutils.deleteDir(new File(m_tempDir));
            }
            m_tempDir = null;
        }
    }

    /**
     * Get the catalog for temporary files
     * @return the catalog path
     */
    public String getTempCatalog() {
        if (m_tempDir == null) {
            setTempCatalog(null);
        }
        return m_tempDir;
    }

    /**
     * get the active reporter
     * @return The reporter
     */
    public reporter getReporter() {
        return m_reporter;
    }

    /**
     * Get all tag-attribute pairs.
     * @return The HasMap
     */
    public HashMap<String, String> getAddressMap() {
        return m_addressing;
    }

    /**
     * Empty referenceholder
     */
    public void initUsedReferences() {
        m_usedReferences = new Index();
    }

    /**
     * Empty imageholder and remove all images made on the fly
     * ,that is from commands
     */
    public void removeImagesMadeOntheFly() {
        m_usedImages = new Index();
        // cannot since I also kills images from images files:
        // m_images=new HashMap<String,Imagex>();

        // attempt to remove all images made on the fly
        if (m_images == null) {
            return;
        }
        try {
            Iterator<String> it = m_images.keySet().iterator();
            List<String> removables = new ArrayList<String>();
            while (it.hasNext()) {
                String key = it.next();
                Image im = m_images.get(key);
                if (im.madeOnTheFly()) {
                    removables.add(key);
                }
            }
            for (int ix = removables.size() - 1; ix >= 0; ix--) {
                m_images.remove(removables.get(ix));
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }


    }

    /**
     * Empty imageholder and remove all images made on the fly
     * ,that is from commands
     */
    public void removeFormulasMadeOntheFly() {
        m_usedFormulas = new Index();
        // cannot since I also kills images from images files:
        // m_formulas=new HashMap<String,LaTexFormula>();

        // attempt to remove all texformulas made on the fly
        if (m_formulas == null) {
            return;
        }
        try {
            Iterator<String> it = m_formulas.keySet().iterator();
            List<String> removables = new ArrayList<String>();
            while (it.hasNext()) {
                String key = it.next();
                Formula im = m_formulas.get(key);
                if (im.madeOnTheFly()) {
                    removables.add(key);
                }
            }
            for (int ix = removables.size() - 1; ix >= 0; ix--) {
                m_formulas.remove(removables.get(ix));
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }



    }

    /**
     * Clean words, all and used
     */
    public void removeWords() {
        m_usedWords = new Index();
        m_words = new HashMap<String, Word>();
    }

    /**
     * Authors are sorted by name
     * @return An object array
     */
    public Fragment[] getAllSortedAuthors() {
        // get all fragments
        Fragment[] frags = m_authors.values().toArray(new Fragment[0]);
        Arrays.sort(frags, new compareFragments());
        return frags;
    }

    /**
     * Load a file with fragment or author definitions and create fragments
     * @param location The location of the file
     * @param type AUTHOR or FRAGMENT
     * @throws java.lang.Exception when we dont succeed
     */
    private void unWrapFragmentsFromFragmentFile(String location, String type)
            throws Exception {
        URI sourceUri = accessutils.makeAbsoluteURI(location, m_anchorCat);
        Document imDoc = domer.makeDomFromUri(sourceUri,false, null);
        //validation goes here
        String schemaString = resourceHandler.getFragmentsSchema();
        validationErrorHandler eHandler = new validationErrorHandler(true);
        String result = domer.validateDomFromString(imDoc, schemaString, eHandler);
        String rep = eHandler.getReport();
        if (result != null) {
            m_reporter.pushSimpleMessage("Fragments validation failed:" + location + "\n");
        } else {
            m_reporter.pushSimpleMessage("Fragments validation:" + location + "\n" + rep + "\n");
        }

        // we look for a catalog element giving the
        // root and anchor for all images listed
        // if not removables we assume that the file is in same catalog as images
        if (imDoc.getDocumentElement().hasAttribute("catalog")) {
            // we use this removables catalog as anchor
            // in stead of the catalog in sourceUri
            String srcCatStr = imDoc.getDocumentElement().getAttribute("catalog");
            srcCatStr = substituteFragments(srcCatStr);
            //srcCatStr=srcCatStr.replace('\\', '/');
            //srcCatStr=srcCatStr.replaceAll(" ", "%20");
            // we must try to make sourceUri from the scrCatStr
            try {
                sourceUri = accessutils.makeAbsoluteURI(srcCatStr, sourceUri.getPath());
            } catch (Exception lex) {
                // anything we can do about this ?
                throw lex;
            }
        }

        NodeList flist = imDoc.getElementsByTagName("fragment");

        // use this:
        if (type.equals(FRAGMENT)) {
            Frag.makeFragments(flist, this, type);
        } else {
            Frag.makeFragments(flist, this, type);
        }
    }

    @Override
    public String toString() {

        String s = "\nDefinitions: anchored at: " + m_anchorCat;

        if (m_publishedCat != null) {
            s += "\npublished at: " + m_publishedCat;
        }

        // pathfragments
        s += "\nPathfragments:";
        for (String ks : m_pathfragments.keySet()) {
            String sub = m_pathfragments.get(ks);
            // regex for quoting
            s += "\n\t" + ks + " : " + sub;
        }

        // xmlfragments
        s += "\nFragments:";
        for (String ks : m_fragments.keySet()) {
            Fragment sub = m_fragments.get(ks);
            // regex for quoting
            s += "\n\t" + ks + " : " + sub;
        }

        // templates
        s += "\nTemplates:";
        for (String ks : m_templates.keySet()) {
            s += "\n\t" + m_templates.get(ks).toString();
        }

        // transformations
        s += "\nTransformations:";
        for (String ks : m_transformations.keySet()) {
            s += "\n\t" + m_transformations.get(ks).toString();
        }

        // commands 
        s += "\nCommands: ";
        for (String ks : m_commands.keySet()) {
            s += "\n\t" + m_commands.get(ks).toString();
        }

        // referencelists: 
        /*
        s += "\nReference lists: ";
        for (String ks : m_refLists.keySet()) {
        s += "\n\t" + m_refLists.get(ks).toString();
        }
         */
        // options 
        s += "\nOptions: \n";
        s+=m_scriptHandler.getScriptOptions().toString();

        return s;
    }

    //------------------------------
    // for comparing fragments by name
    class compareFragments implements Comparator<Fragment> {

        @Override
        public int compare(Fragment o1, Fragment o2) {
            return o1.getShort().compareTo(o2.getShort());
        }
    }
}

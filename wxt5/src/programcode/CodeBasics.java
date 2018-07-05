package programcode;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.domer;

/**
 * Static library handling colorcoding basics for both Google and WXT codeformatter
 * @author bs
 */
public class CodeBasics {

    /**define allowed code types according to old strategy*/
    public static final String XMLCODE = "xmlcode";
    public static final String MLCODE = "mlcode";
    public static final String JAVACODE = "javacode";
    public static final String CODE = "code";
    public static final String JAVASCRIPTCODE = "javascriptcode";
    public static final String CCODE = "ccode";
    public static final String CPPCODE = "cppcode";
    public static final String CSHARPCODE = "csharpcode";
    public static final String PYTHONCODE = "pythoncode";
    public static final String XSLTCODE = "xsltcode";
    public static final String ASPXCODE = "aspxcode";
    public static final String CS3CODE = "cs3code";
    public static final String MATLABCODE = "matlabcode";
    public static final String HTMLCODE = "htmlcode";
    public static final String CSSCODE = "csscode";
    /**definitions acccording to new strategy*/
    public static final String PRETTYPRINT = "prettyprint";
    public static final String READYANDPRETTY = "readyandpretty";
    public static final String LANG_XML = "lang-xml";
    public static final String LANG_J = "lang-j";
    public static final String LANG_JS = "lang-js";
    public static final String LANG_C = "lang-c";
    public static final String LANG_CPP = "lang-cpp";
    public static final String LANG_CSH = "lang-cs";
    public static final String LANG_PY = "lang-py";
    public static final String LANG_XSLT = "lang-xslt";
    public static final String LANG_ASPX = "lang-aspx";
    public static final String LANG_CS3 = "lang-cs3";
    public static final String LANG_MAL = "lang-mal";
    public static final String LANG_SQL = "lang-sql";
    public static final String LANG_HTML = "lang-html";
    public static final String LANG_CSS = "lang-css";
    public static final String LANG = "lang";
    public static final String SPACESFORTAB = "    ";
    public static final String[] ALL_OLD_CODE_TYPES = {
        XMLCODE, MLCODE, JAVACODE, CODE,
        JAVASCRIPTCODE, CCODE, CPPCODE, CSHARPCODE,
        PYTHONCODE, XSLTCODE, ASPXCODE, CS3CODE,
        MATLABCODE, CSSCODE, HTMLCODE
    };
    public static final String[] ALL_CODE_TYPES = {
        LANG_XML, LANG_JS, LANG_J, LANG_CPP,
        LANG_PY, LANG_XSLT, LANG_ASPX, LANG_CS3,
        LANG_MAL, LANG_SQL, LANG_HTML, LANG_CSS, LANG_CSH, LANG_C, LANG
    };

    /**
     * Check if a code parameter is legal. Either old style or
     * some lang- solution
     * @param v the code to check
     * @return true if it is ok, false otherwise
     */
    public static boolean legalCodeValue(String v) {
        return ((v.startsWith(LANG) ||//((v.startsWith(LANG+"-")||
                v.compareTo(CODE) == 0)
                || (v.compareTo(XMLCODE) == 0)
                || (v.compareTo(MLCODE) == 0)
                || (v.compareTo(JAVACODE) == 0)
                || (v.compareTo(JAVASCRIPTCODE) == 0)
                || (v.compareTo(CCODE) == 0)
                || (v.compareTo(CPPCODE) == 0)
                || (v.compareTo(CSHARPCODE) == 0)
                || (v.compareTo(PYTHONCODE) == 0)
                || (v.compareTo(XSLTCODE) == 0)
                || (v.compareTo(ASPXCODE) == 0)
                || (v.compareTo(CS3CODE) == 0)
                || (v.compareTo(HTMLCODE) == 0)
                || (v.compareTo(CSSCODE) == 0)
                || (v.compareTo(MATLABCODE) == 0));
    }

    /**
     * Translating from old codename to new
     * @param c Old code name
     * @return New codename or self if no match in old codes
     */
    public static String translateCodeClass(String c) {
        if (c.compareToIgnoreCase(XMLCODE) == 0) {
            return LANG_XML;
        }
        if (c.compareToIgnoreCase(MLCODE) == 0) {
            return LANG;
        }
        if (c.compareToIgnoreCase(JAVACODE) == 0) {
            return LANG_J;
        }
        if (c.compareToIgnoreCase(JAVASCRIPTCODE) == 0) {
            return LANG_JS;
        }
        if (c.compareToIgnoreCase(CCODE) == 0) {
            return LANG_C;
        }
        if (c.compareToIgnoreCase(CPPCODE) == 0) {
            return LANG_CPP;
        }
        if (c.compareToIgnoreCase(CSHARPCODE) == 0) {
            return LANG_CSH;
        }
        if (c.compareToIgnoreCase(PYTHONCODE) == 0) {
            return LANG_PY;
        }
        if (c.compareToIgnoreCase(XSLTCODE) == 0) {
            return LANG_XML;
        }
        if (c.compareToIgnoreCase(ASPXCODE) == 0) {
            return LANG_ASPX;
        }
        if (c.compareToIgnoreCase(CS3CODE) == 0) {
            return LANG_CS3;
        }
        if (c.compareToIgnoreCase(MATLABCODE) == 0) {
            return LANG_MAL;
        }
        if (c.compareToIgnoreCase(HTMLCODE) == 0) {
            return LANG_HTML;
        }
        if (c.compareToIgnoreCase(CSSCODE) == 0) {
            return LANG_CSS;
        }
        return c;
    }

    /**
     * Find correct codetype from classvalue
     * classvalue contains codetype, among other things
     * @param classvalue The class value
     * @return The selected class or "lang" if no match
     */
    public static String getNewCodeType(String classvalue) {
        String[] values = classvalue.split(" ");
        for (String v : values) {
            for (String t : ALL_CODE_TYPES) {
                if (v.trim().compareTo(t) == 0) {
                    return t;
                }
            }
        }
        // return neutral
        return LANG;
    }

    /**
     * Clean double eol, tab , align left
     * @param theText The text to clean
     * @return The cleaned text
     */
    public static String cleanString(String theText) {
        // only single eol
        theText = theText.replaceAll("\r\n", "\n");
        theText = theText.replaceAll("\r", "\n");
        theText = theText.replaceAll("\n\n", "\n");
        // spaces for tabs
        theText = theText.replaceAll("\t", SPACESFORTAB);

        // left justify
        int minspaces = 100;
        String[] lines = theText.split("\n");
        // measure
        for (String s : lines) {
            int i = 0;
            if (s.length() < 2) {
                continue;
            }
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
                i++;
            }
            if (i < minspaces) {
                minspaces = i;
            }
            if (minspaces < 2) {
                return theText;
            }
        }
        // reconstruct
        StringBuilder sb = new StringBuilder(theText.length());
        theText = "";
        for (String s : lines) {
            if (s.length() < 2) {
                sb.append(s);
            } else {
                sb.append(s.substring(minspaces)).append("\n");
            }
        }
        String S = sb.toString().trim();

        return S;

    }

    /**
     * Analyze a DOM and find all pre-tags with old code
     * and cange it to new style "prettyprint lang-xx"
     * @param doc
     */
    public static void transformToNewForm(Document doc) {
        //---------------------------------
        // scan for old codetypes and translate
        // to be backward compatible
        NodeList nlist = null;
        for (int six = 0; six < ALL_OLD_CODE_TYPES.length; six++) {
            String codetype = ALL_OLD_CODE_TYPES[six];
            String xp = "//pre[@class='" + codetype + "']";
            try {
                nlist = domer.performXPathQuery(doc, xp);
            } catch (Exception te) {
                System.out.println("expandCodeFragments:" + te.getMessage());
                continue;
            }
            // run the list and translate
            for (int ix = 0; ix < nlist.getLength(); ix++) {
                if (nlist.item(ix).getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element eltItem = (Element) nlist.item(ix);
                String curClass = eltItem.getAttribute("class");
                String newClass = PRETTYPRINT + " " + translateCodeClass(curClass);
                eltItem.setAttribute("class", newClass);
            }
        }
    }

    /**
     * Find the first CDATAnode that is child of an element
     * @param elt The element we investigate
     * @return The first CDATA node if it exists, otherwise null
     */
    public static Node getFirstCDATANode(Element elt) {
        if (!elt.hasChildNodes()) {
            return null;
        }
        Node n = elt.getFirstChild();
        while (n != null) {
            if (n.getNodeType() == org.w3c.dom.Node.CDATA_SECTION_NODE) {
                return n;
            } else {
                n = n.getNextSibling();
            }
        }
        return null;
    }
}

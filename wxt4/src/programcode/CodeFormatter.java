package programcode;

import content.Module;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.io.IOException;
//import org.w3c.dom.Element;
import org.w3c.dom.DocumentFragment;
//import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import utils.PIcommand;
import utils.accessutils;
import utils.domer;
import reporting.reporter;

/**
 * Purpose is to produce color coded sourcecode.
 * <p>Two public functions:</p>
 * expandCodeFragments<br>
 * expands already placed pre elements with code
 * </p>
 * <p>
 * produceCodeFragmentFromString<br>
 * returns a DocumentFragment with a &lt;pre&gt;-child
 * </p>
 * We can handle xmlcode,mlcode,javacode,javascriptcode,ccode,cppcode,
 * csharpcode,pythoncode,xsltcode,aspxcode,cs3code,matlabcode
 * </p>
 * <p>
 * If spesific language parsing fails, we escape with a general
 * &lt;pre class="code"&gt; &lt;/pre&gt; - wrapper
 * </p>
 *
 * <p>Using the following styleclasses:
 * </p>
 * <ul>
 * <li>.mlcode</li>
 * <li>.code</li>
 * <li>.javacode</li>
 * <li>.javascriptcode</li>
 * <li>.pythoncode</li>
 * <li>.ccode</li>
 * <li>.cppcode</li>
 * <li>.csharpcode</li>
 * <li>.xsltcode</li>
 * <li>.aspxcode</li>
 * <li>.cs3code</li>
 * <li>.literal,.word,.comment</li>
 * </ul>
 * <p>And for xml
 * <ul>xmlcode, as wrapper
 * <li>.tagname</li>
 * <li>.attname</li>
 * <li>.attvalue</li>
 * <li>.process</li>
 * </ul>
 */
public class CodeFormatter {

    static final String WORD = "kwd";
    static final String COM = "com";
    static final String LITERAL = "str";
    static final String TAGNAME = "tag";
    static final String ATTNAME = "atn";
    static final String ATTVALUE = "atv";
    public static final String EOL = "\n";//System.getProperty("line.separator");
    static final String TAB_WIDTH = "  ";

    /** Creates a new instance of codeFormat */
    public CodeFormatter() {
    }
    /** Reserved words in JavaScript */
    static final String javascriptwords[] = new String[]{"abstract", "as", "boolean", "break", "byte",
        "case", "catch", "char", "class", "const", "continue", "debugger", "default", "delete", "do", "double",
        "else", "enum", "export", "extends", "false", "final", "finally", "float", "for", "function", "goto",
        "if", "implements", "import", "in", "instanceof", "int", "interface", "is", "long",
        "namespace", "native", "new", "null", "package", "private", "protected", "public", "return", "short", "static",
        "super", "switch", "synchronized", "this", "throw", "throws", "transient", "true", "try", "typeof",
        "use", "void", "volatile", "var", "while", "with"};
    /** Reserved words in Java */
    static final String javawords[] = new String[]{"abstract", "assert", "boolean", "break", "byte",
        "case", "catch", "char", "class", "const", "continue", "default", "do", "double", "else", "enum", "extends", "final",
        "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long",
        "native", "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp",
        "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while"};
    /** Reserved words in Python */
    static final String pythonwords[] = new String[]{"and", "assert", "break", "class", "continue", "def", "del", "elif", "else", "except", "exec", "finally",
        "for", "from", "global", "if", "import", "in", "is", "lambda", "not", "or", "pass", "print", "raise", "return", "try"};
    /** Reserved words in C# */
    static final String csharpwords[] = new String[]{
        "as", "base", "bool", "break", "byte",
        "case", "catch", "char", "checked", "class", "const", "continue", "decimal", "default", "delegate", "do", "double",
        "else", "enum", "event", "explicit", "extern", "false", "finally", "fixed", "float", "for", "foreach", "goto",
        "if", "implicit", "in", "int", "interface", "internal", "is", "lock", "long", "namespace", "new", "null", "object",
        "operator", "out", "override", "params", "private", "protected", "public", "readonly", "ref", "return", "sbyte",
        "sealed", "short", "sizeof", "stackalloc", "static", "string", "struct", "switch", "this", "throw", "true", "try",
        "typeof", "uint", "ulong", "unchecked", "unsafe", "ushort", "using", "virtual", "void", "volatile", "while"};
    /** Reserved words in C */
    static final String cwords[] = new String[]{
        "auto", "break", "case", "char", "const", "continue", "default", "do",
        "double", "else", "enum", "extern", "float", "for", "goto", "if", "int", "long", "register", "return", "short",
        "signed", "sizeof", "static", "struct", "switch", "typedef", "union", "unsigned", "void", "volatile", "while"};
    /** Reserved words in C++ */
    static final String cppwords[] = new String[]{
        "auto", "break", "case", "catch", "char", "class", "const", "continue", "default", "delete", "do", "double",
        "else", "enum", "extern", "float", "for", "friend", "goto", "if", "int", "long", "mutable", "new", "operator",
        "private", "protected", "public", "register", "return", "short", "signed", "sizeof", "static", "struct", "switch",
        "template", "this", "throw", "typedef", "union", "unsigned", "virtual", "void", "volatile", "while"};
    /** reserved words in cs3 */
    static final String cs3words[] = new String[]{
        "add", "and", "break", "case", "catch", "class", "continue", "default", "delete", "do", "dynamic", "else", "eq", "extends",
        "false", "finally", "for", "function", "ge", "get", "gt", "if", "ifFrameLoaded", "implements", "import", "in", "instanceof",
        "interface", "intrinsic", "le", "it", "ne", "new", "not", "null", "on", "onClipEvent", "or", "private", "public", "return",
        "set", "static", "super", "switch", "tellTarget", "this", "throw", "try", "typeof", "undefined", "var", "void", "while",
        "with", "as", "abstract", "Boolean", "bytes", "char", "const", "debugger", "double", "enum", "export", "final", "float",
        "goto", "is", "long", "namespace", "native", "package", "protected", "short", "synchronized", "throws", "transient", "use", "volatile"};
    /** reserved words in matlab */
    static final String matlabwords[] = new String[]{
        "break", "case", "catch", "classdef", "continue", "else", "elseif", "end", "for", "function", "global",
        "if", "otherwise", "parfor", "persistent", "return", "switch", "try", "while"
    };
    /* reserved words in SQL*/
    static final String sqlwords[] = new String[]{
        "ADD", "ALL", "ALTER", "AND", "ANY", "AS", "ASC", "AUTHORIZATION", "BACKUP", "BEGIN", "BETWEEN", "BREAK", "BROWSE",
        "BULK", "BY", "CASCADE", "CASE", "CHECK", "CHECKPOINT", "CLOSE", "CLUSTERED", "COALESCE", "COLLATE", "COLUMN",
        "COMMIT", "COMPUTE", "CONSTRAINT", "CONTAINS", "CONTAINSTABLE", "CONTINUE", "CONVERT", "CREATE", "CROSS", "CURRENT",
        "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "CURSOR", "DATABASE", "DBCC", "DEALLOCATE",
        "DECLARE", "DEFAULT", "DELETE", "DENY", "DESC", "DISK", "DISTINCT", "DISTRIBUTED", "DOUBLE", "DROP", "DUMMY", "DUMP",
        "ELSE", "END", "ERRLVL", "ESCAPE", "EXCEPT", "EXEC", "EXECUTE", "EXISTS", "EXIT", "FETCH", "FILE", "FILLFACTOR",
        "FOR", "FOREIGN", "FREETEXT", "FREETEXTTABLE", "FROM", "FULL", "FUNCTION", "GOTO", "GRANT", "GROUP", "HAVING",
        "HOLDLOCK", "IDENTITY", "IDENTITYCOL", "IDENTITY_INSERT", "IF", "IN", "INDEX", "INNER", "INSERT", "INTERSECT",
        "INTO", "IS", "JOIN", "KEY", "KILL", "LEFT", "LIKE", "LINENO", "LOAD", "NATIONAL", "NOCHECK", "NONCLUSTERED",
        "NOT", "NULL", "NULLIF", "OF", "OFF", "OFFSETS", "ON", "OPEN", "OPENDATASOURCE", "OPENQUERY", "OPENROWSET",
        "OPENXML", "OPTION", "OR", "ORDER", "OUTER", "OVER", "PERCENT", "PLAN", "PRECISION", "PRIMARY", "PRINT", "PROC",
        "PROCEDURE", "PUBLIC", "RAISERROR", "READ", "READTEXT", "RECONFIGURE", "REFERENCES", "REPLICATION", "RESTORE",
        "RESTRICT", "RETURN", "REVOKE", "RIGHT", "ROLLBACK", "ROWCOUNT", "ROWGUIDCOL", "RULE", "SAVE", "SCHEMA", "SELECT",
        "SESSION_USER", "SET", "SETUSER", "SHUTDOWN", "SOME", "STATISTICS", "SYSTEM_USER", "TABLE", "TEXTSIZE",
        "THEN", "TO", "TOP", "TRAN", "TRANSACTION", "TRIGGER", "TRUNCATE", "TSEQUAL", "UNION", "UNIQUE", "UPDATE",
        "UPDATETEXT", "USE", "USER", "VALUES", "VARYING", "VIEW", "WAITFOR", "WHEN", "WHERE", "WHILE", "WITH", "WRITETEXT"};
    /** Reserved words in CSS */
    static final String csswords[] = new String[]{
        "background", "background-attachment", "background-color", "background-image", "background-position", "background-repeat",
        "border", "border-bottom", "border-bottom-color", "border-bottom-style", "border-bottom-width", "border-color",
        "border-collapse", "border-left", "border-left-color", "border-left-style", "border-left-width", "border-right",
        "border-right-color", "border-right-style", "border-right-width", "border-spacing", "border-style", "border-top",
        "border-top-color", "border-top-style", "border-top-width", "border-width", "bottom", "caption-side", "clear",
        "clip", "color", "content", "counter-increment", "counter-reset", "cursor", "direction", "display",
        "empty-cells", "float", "font", "font-family", "font-size", "font-style", "font-variant", "font-weight",
        "height", "left", "letter-spacing", "line-height", "list-style", "list-style-image", "list-style-position",
        "list-style-type", "margin", "margin-bottom", "margin-left", "margin-right", "margin-top", "max-height",
        "max-width ", "min-height", "min-width", "orphans", "outline", "outline-color", "outline-style", "outline-width",
        "overflow", "padding", "padding-bottom", "padding-left", "padding-right", "padding-top", "page-break-after",
        "page-break-before", "page-break-inside", "position", "quotes", "right", "table-layout", "text-align",
        "text-decoration", "text-indent", "text-shadow", "text-transform", "top", "unicode-bidi", "vertical-align",
        "visibility", "white-space", "widows", "width", "word-spacing", "z-index", "active", "after", "before",
        "first-child", "first-letter", "first-line", "focus", "hover", "lang", "link", "visited"};
    /** Reserved words in (X)HTML */
    static final String htmltags[] = new String[]{
        "!DOCTYPE", "a", "abbr", "acronym", "address", "applet", "area", "b", "base",
        "basefont", "bdo", "big", "blockquote", "body", "br", "button", "caption",
        "center", "cite", "code", "col", "colgroup", "dd", "del", "dir", "div",
        "dfn", "dl", "dt", "em", "fieldset", "font", "form", "frame", "frameset",
        "h1", "h2", "h3", "h4", "h5", "h6", "head", "hr", "html", "i", "iframe",
        "img", "input", "ins", "isindex", "kbd", "label", "legend", "li", "link",
        "map", "menu", "meta", "noframes", "noscript", "object", "ol", "optgroup",
        "option", "p", "param", "pre", "q", "s", "samp", "script", "select", "small",
        "span", "strike", "strong", "style", "sub", "sup", "table", "tbody", "td",
        "textarea", "tfoot", "th", "thead", "title", "tr", "tt", "u", "ul", "var", "xmp"};

    /**
     * Find out if w is a reserved Java word
     * @param w The word to test
     * @return w formatted in span-element if Java word, w if not
     */
    static protected String isJavaWord(String w) {
        for (int ix = 0; ix < javawords.length; ix++) {
            if (javawords[ix].compareTo(w) == 0) {
                return "<span class=\"" + WORD + "\">" + w + "</span>";
            }
        }
        return w;
    }

    /**
     * Find out if w is a reserved JavaScript word
     * @param w The word to test
     * @return w formatted in span-element if Java word, w if not
     */
    static protected String isJavaScriptWord(String w) {
        for (int ix = 0; ix < javascriptwords.length; ix++) {
            if (javascriptwords[ix].compareTo(w) == 0) {
                return "<span class=\"" + WORD + "\">" + w + "</span>";
            }
        }
        return w;
    }

    /**
     * Find out if w is a reserved Python word
     * @param w The word to test
     * @return w formatted in span-element if Python word, w if not
     */
    static protected String isPythonWord(String w) {
        for (int ix = 0; ix < pythonwords.length; ix++) {
            if (pythonwords[ix].compareTo(w) == 0) {
                return "<span class=\"" + WORD + "\">" + w + "</span>";
            }
        }
        return w;
    }

    /**
     * Find out if w is a reserved C# word
     * @param w The word to test
     * @return w formatted in span-element if C# word, w if not
     */
    static protected String isCSharpWord(String w) {
        for (int ix = 0; ix < csharpwords.length; ix++) {
            if (csharpwords[ix].compareTo(w) == 0) {
                return "<span class=\"" + WORD + "\">" + w + "</span>";
            }
        }
        return w;
    }

    /**
     * Find out if w is a reserved C++ word
     * @param w The word to test
     * @return w formatted in span-element if C++ word, w if not
     */
    static protected String isCPPWord(String w) {
        for (int ix = 0; ix < cppwords.length; ix++) {
            if (cppwords[ix].compareTo(w) == 0) {
                return "<span class=\"" + WORD + "\">" + w + "</span>";
            }
        }
        return w;
    }

    /**
     * Find out if w is a reserved C word
     * @param w The word to test
     * @return w formatted in span-element if C word, w if not
     */
    static protected String isCWord(String w) {
        for (int ix = 0; ix < cwords.length; ix++) {
            if (cwords[ix].compareTo(w) == 0) {
                return "<span class=\"" + WORD + "\">" + w + "</span>";
            }
        }
        return w;
    }

    /**
     * Find out if w is a reserved CS3 word
     * @param w The word to test
     * @return w formatted in span-element if CS3 word, w if not
     */
    static protected String isCS3Word(String w) {
        for (int ix = 0; ix < cs3words.length; ix++) {
            if (cs3words[ix].compareTo(w) == 0) {
                return "<span class=\"" + WORD + "\">" + w + "</span>";
            }
        }
        return w;
    }

    /**
     * Find out if w is a reserved sql word
     * @param w The word to test
     * @return w formatted in span-element if sql word, w if not
     */
    static protected String isSQLWord(String w) {
        for (int ix = 0; ix < sqlwords.length; ix++) {
            if (sqlwords[ix].compareToIgnoreCase(w) == 0) {
                return "<span class=\"" + WORD + "\">" + w + "</span>";
            }
        }
        return w;
    }

    /**
     * Find out if w is a reserved Matlab word
     * @param w The word to test
     * @return w formatted in span-element if Matlab word, w if not
     */
    static protected String isMatlabWord(String w) {
        for (int ix = 0; ix < matlabwords.length; ix++) {
            if (matlabwords[ix].compareTo(w) == 0) {
                return "<span class=\"" + WORD + "\">" + w + "</span>";
            }
        }
        return w;
    }

    /**
     * Find out if w is a reserved CSS word
     * @param w The word to test
     * @return w formatted in span-element if CSS word, w if not
     */
    static protected String isCSSWord(String w) {
        for (int ix = 0; ix < csswords.length; ix++) {
            if (csswords[ix].compareTo(w) == 0) {
                return "<span class=\"" + WORD + "\">" + w + "</span>";
            }
        }
        return w;
    }

    /**
     * Find out if w is a (X)HTML tag
     * @param w The word to test
     * @return w formatted in span-element if HTMLtag, w if not
     */
    static protected String isHTMLTag(String w) {
        for (int ix = 0; ix < htmltags.length; ix++) {
            if (htmltags[ix].compareToIgnoreCase(w) == 0) {
                return "<span class=\"" + TAGNAME + "\">" + w + "</span>";
            }
        }
        return w;
    }

    /**
     * Find out if w is a reserved word of a certain type (language)
     * @param w The word to test
     * @param type The type given
     * @return w formatted in span-element if a legal code word, w if not
     */
    static protected String checkWord(String w, String type) {
        if (type.compareToIgnoreCase(CodeBasics.LANG_J) == 0) {
            return isJavaWord(w);
        } else if (type.compareToIgnoreCase(CodeBasics.LANG_C) == 0) {
            return isCWord(w);
        } else if (type.compareToIgnoreCase(CodeBasics.LANG_CPP) == 0) {
            return isCPPWord(w);
        } else if (type.compareToIgnoreCase(CodeBasics.LANG_CSH) == 0) {
            return isCSharpWord(w);
        } else if (type.compareToIgnoreCase(CodeBasics.LANG_PY) == 0) {
            return isPythonWord(w);
        } else if (type.compareToIgnoreCase(CodeBasics.LANG_JS) == 0) {
            return isJavaScriptWord(w);
        } else if (type.compareToIgnoreCase(CodeBasics.LANG_CS3) == 0) {
            return isCS3Word(w);
        } else if (type.compareToIgnoreCase(CodeBasics.LANG_MAL) == 0) {
            return isMatlabWord(w);
        } else if (type.compareToIgnoreCase(CodeBasics.LANG_SQL) == 0) {
            return isSQLWord(w);
        } else if (type.compareToIgnoreCase(CodeBasics.LANG_HTML) == 0) {
            return isHTMLTag(w);
        } else if (type.compareToIgnoreCase(CodeBasics.LANG_CSS) == 0) {
            return isCSSWord(w);
        } else {
            return w;
        }
    }

    /**
     * Produce a simple, unparsed code segment wrapped in a pre-tag
     * @param txt The text we consider as code
     * @param encoding The encoding we want
     * @param type The codetype we want, CSS-class
     * @param theReporter Where we report errors
     * @return The produced Documentfragment, none if we cannot make it
     */
    static protected String produceSimpleFragmentFromString(String txt, String encoding, String type, reporter theReporter) {

        txt = txt.replaceAll("&", "&amp;");
        txt = txt.replaceAll("<", "&lt;");
        txt = txt.replaceAll(">", "&gt;");
        txt = txt.replaceAll("\"", "&quot;");
        txt = txt.replaceAll("'", "&apos;");

        return txt;

    }

    /**Produces a documentfragment whith formatted sourcecode.
     * Reserved words, comments and text literals are hilited.
     * Can handle Java, Javascript, C, C++, C#, Python, XSLT
     *@param txt The string to parse
     *@param type The codetype to expect (javacode,ccode,cppcode,csharpcode,pythoncode
     *@param encoding The encoding to use when we make the DocumentFragment
     *@param theReporter The reporter object that handles error reports
     *@return The produced Documentfragment, none if we cannot make it
     */
    static private DocumentFragment produceCodeFragmentFromString(String txt, String type, String encoding, Module mod) {

        reporter theReporter = mod.getReporter();
        // the formatted text as text
        String preparedText = "";

        preparedText = produceCodedStringFromString(txt, type, encoding, mod);

        // return fragment
        try {
            return domer.produceDocFragmentFromString(preparedText, encoding, theReporter);
        } catch (Exception e) {
            // we cant make it so we fall back to anonymous code
            theReporter.pushMessage("trouble_parsing_code", type, e.getMessage());
            preparedText = produceSimpleFragmentFromString(txt, encoding, type, theReporter);
            try {
                return domer.produceDocFragmentFromString(preparedText, encoding, theReporter);
            } catch (Exception e2) {
                return null;
            }
        }
    }

    /**Produces a documentfragment whith formatted sourcecode as text.
     * Reserved words, comments and text literals are hilited.
     * Can handle Java, Javascript, C, C++, C#, Python, XSLT
     *@param txt The string to parse
     *@param type The codetype to expect (javacode,ccode,cppcode,csharpcode,pythoncode
     *@param encoding The encoding to use when we make the DocumentFragment
     *@param mod The Module requesting this
     * @return The produced Documentfragment, none if we cannot make it
     */
    static protected String produceCodedStringFromString(String txt, String type, String encoding, Module mod) {
        reporter theReporter = mod.getReporter();
        // the formatted text as text
        String preparedText = "";

        // fix tabs to 4 spaces for all known and unknown codetypes except xml and html
        if ((type.compareToIgnoreCase(CodeBasics.LANG_XML) != 0) || (type.compareToIgnoreCase(CodeBasics.LANG_HTML) != 0)) {
            txt = txt.replaceAll("\t", TAB_WIDTH);
        }

        // make unix form

        txt = txt.replaceAll("\r", "\n");
        txt = txt.replaceAll("\n\n", "\n");
        txt = txt.replaceAll("\n", EOL);

        if (encoding == null) {
            encoding = "utf-8";
        }

        String inserted_header = null;

        // treat XSLT as XML if xml-header is set
        if (type.compareTo(CodeBasics.LANG_XSLT) == 0) {
            if (accessutils.trimLeft(txt).startsWith("<?xml")) {
                type = CodeBasics.LANG_XML;
            }
        }

        // treat HTML as XML if xml-header is set
        if (type.compareToIgnoreCase(CodeBasics.LANG_HTML) == 0) {
            if (accessutils.trimLeft(txt).startsWith("<?xml")) {
                type = CodeBasics.LANG_XML;
            }
        }

        if (type.compareToIgnoreCase(CodeBasics.LANG_HTML) == 0) {
            // teat as XML and set header if doctype is given
            txt = accessutils.trimLeft(txt);
            if (txt.trim().startsWith("<DOCTYPE")) {
                type = CodeBasics.LANG_XML;
                inserted_header = "<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>";
                txt = inserted_header + EOL + txt;
            }
        }


        if ((type.compareToIgnoreCase(CodeBasics.LANG_XML) == 0)) {
            txt = accessutils.trimLeft(txt);
            if (!txt.startsWith("<?xml")) {
                inserted_header = "<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>";
                txt = inserted_header + EOL + txt;
            }

            try {
                XMLEncoder xe = new XMLEncoder(type.compareToIgnoreCase(CodeBasics.LANG_HTML) == 0);
                preparedText = xe.parseText(txt, encoding, theReporter);
                if (preparedText == null) {
                    throw new Exception();
                }
                if (inserted_header != null) {
                    preparedText = preparedText.replaceFirst("\\Q" + inserted_header + "\\E", "");
                }
            } catch (Exception xex) {
                theReporter.pushMessage("Could_not_parse_XMLCode");
                preparedText = produceSimpleFragmentFromString(txt, encoding, type, theReporter);
            }
        } else if (type.compareTo(CodeBasics.LANG_HTML) == 0) {
            preparedText = produceHTMLCodeFragmentFromString(txt, encoding, theReporter);
        } else if (type.compareTo(CodeBasics.LANG) == 0) {
            preparedText = produceSimpleFragmentFromString(txt, encoding, type, theReporter);
        } else if (type.compareTo(CodeBasics.LANG_PY) == 0) {
            preparedText = producePythonCodeFragmentFromString(txt, encoding, theReporter);
        } else if (type.compareTo(CodeBasics.LANG_XSLT) == 0) {
            preparedText = produceXSLTCodeFragmentFromString(txt, encoding, theReporter);
        } else if (type.compareTo(CodeBasics.LANG_ASPX) == 0) {
            preparedText = produceASPXCodeFragmentFromString(txt, encoding, theReporter);
        } else if (type.compareTo(CodeBasics.LANG_MAL) == 0) {
            preparedText = produceMatlabCodeFragmentFromString(txt, encoding, theReporter);
        } else if (type.compareTo(CodeBasics.LANG_SQL) == 0) {
            preparedText = produceSQLCodeFragmentFromString(txt, encoding, theReporter);
        } else if (type.compareTo(CodeBasics.LANG_CSS) == 0) {
            preparedText = produceCSSCodeFragmentFromString(txt, encoding, theReporter);
        } else if ((type.compareTo(CodeBasics.LANG_C) == 0)
                || (type.compareTo(CodeBasics.LANG_CPP) == 0)
                || (type.compareTo(CodeBasics.LANG_CSH) == 0)
                || (type.compareTo(CodeBasics.LANG_J) == 0)
                || (type.compareTo(CodeBasics.LANG_JS) == 0)
                || (type.compareTo(CodeBasics.LANG_CS3) == 0)) {
            preparedText = produceLanguageCodeFragmentFromString(type, txt, encoding, theReporter);
        } else {
            preparedText = produceSimpleFragmentFromString(txt, encoding, type, theReporter);
        }


        preparedText = CodeBasics.cleanString(preparedText);

        preparedText = "<pre class=\"" + type + " " + CodeBasics.READYANDPRETTY + " " + PIcommand.SKIPTRANSLATE + "\">\n"
                + preparedText + "</pre>";

        return preparedText;

        /* // return result. try to align left
        try{
        preparedText=AlignLeft(preparedText);
        return preparedText;
        }
        catch(Exception x)
        {
        return preparedText;
        }
         */
    }

    /**
     * Produces a documentfragment whith formatted sourcecode for Java,javaScript,C,C++,CSharp.
     * Reserved words, comments and text literals are hilited.
     * Can handle Python only
     * @param type The language to parse, ie.:javacode
     * @param txt The string to parse
     * @param encoding The encoding to use when we make the DocumentFragment
     * @param theReporter The reporter object that handles errorreports
     * @return The produced Documentfragment, none if we cannot make it
     */
    static protected String produceLanguageCodeFragmentFromString(String type, String txt, String encoding, reporter theReporter) {
        // do on with Java,javaScript,C,C++,CSharp
        // flags
        boolean quote_is_on = false;
        boolean apos_is_on = false;
        boolean escape_is_on = false;
        boolean escape_is_just_set = false;

        boolean slash_slash_is_on = false;
        boolean slash_star_is_on = false;

        // make sure we are able to finnish  one-line comments
        if (!txt.endsWith(EOL)) {
            txt = txt + EOL;
        }


        StringBuffer newText = new StringBuffer(2048);
        try {
            StringReader rd = new StringReader(txt);
            StreamTokenizer st = new StreamTokenizer(rd);
            st.resetSyntax();
            st.wordChars('A', 'Z');
            st.wordChars('a', 'z');
            st.wordChars('0', '9');
            st.wordChars('_', '_');

            st.wordChars('*', '*');
            st.wordChars('/', '/');

            st.eolIsSignificant(true);

            int token = st.nextToken();
            while (token != StreamTokenizer.TT_EOF) {
                switch (token) {
                    case StreamTokenizer.TT_WORD:
                        String w = st.sval;
                        if (slash_star_is_on && w.endsWith("*/")) {
                            newText.append(w).append("</span>");
                            slash_star_is_on = false;
                        } else if (apos_is_on || quote_is_on || escape_is_on || slash_slash_is_on || slash_star_is_on) {
                            newText.append(st.sval);
                        } else if (w.startsWith("//")) {
                            newText.append("<span class=\"" + COM + "\">").append(w);
                            slash_slash_is_on = true;
                        } else if (w.startsWith("/*")) {
                            if (w.endsWith("*/")) {
                                newText.append("<span class=\"" + COM + "\">").append(w).append("</span>");
                            } else {
                                newText.append("<span class=\"" + COM + "\">").append(w);
                                slash_star_is_on = true;
                            }

                        } else {
                            newText.append(checkWord(st.sval, type));
                        }
                        escape_is_on = false;
                        break;

                    case StreamTokenizer.TT_EOL:
                        if (slash_slash_is_on) {
                            newText.append("</span>");
                        }
                        slash_slash_is_on = false;
                        newText.append(EOL);
                        escape_is_on = false;
                        break;

                    case StreamTokenizer.TT_EOF:
                        // End of file has been reached
                        escape_is_on = false;
                        break;

                    default:
                        // An ordinary character was found, check it out
                        char ch = (char) st.ttype;
                        switch (ch) {
                            case '\\':
                                newText.append(ch);
                                escape_is_on = !escape_is_on;
                                break;
                            case '"':
                                newText.append(ch);
                                if ((!escape_is_on) && (!slash_slash_is_on) && (!slash_star_is_on) && (!apos_is_on))//apos_is_on ?
                                {
                                    if (!quote_is_on) {
                                        newText.append("<span class=\"" + LITERAL + "\">");
                                    } else {
                                        newText.append("</span>");
                                    }
                                    quote_is_on = !quote_is_on;
                                }
                                escape_is_on = false;
                                break;
                            case '\'':
                                newText.append(ch);
                                if ((!escape_is_on) && (!slash_slash_is_on) && (!slash_star_is_on) && (!quote_is_on))//quote_is_on ?
                                {
                                    if (!apos_is_on) {
                                        newText.append("<span class=\"" + LITERAL + "\">");
                                    } else {
                                        newText.append("</span>");
                                    }
                                    apos_is_on = !apos_is_on;
                                }
                                escape_is_on = false;
                                break;
                            case '>':
                                newText.append("&gt;");
                                escape_is_on = false;
                                break;
                            case '<':
                                newText.append("&lt;");
                                escape_is_on = false;
                                break;
                            case '&':
                                newText.append("&amp;");
                                escape_is_on = false;
                                break;
                            default:
                                newText.append(ch);
                                escape_is_on = false;
                        }

                        break;
                }
                token = st.nextToken();
            }
        } catch (IOException e) {
            System.out.println("codeformatter:produceCodeFragmentFromString: " + e.getMessage());
        }

        // what to do if this is not properly closed
        // the ambition is not to do a syntax report
        // just attempt a soft landing
        if ((quote_is_on) || (apos_is_on) || (slash_slash_is_on) || (slash_star_is_on)) {
            theReporter.pushMessage("unclosed_element_in_file", type);
        }
        if (quote_is_on) {
            newText.append("</span>");
        }
        if (apos_is_on) {
            newText.append("</span>");
        }
        if (slash_slash_is_on) {
            newText.append("</span>");
        }
        if (slash_star_is_on) {
            newText.append("</span>");
        }


        String S = newText.toString();
        return S;
    }

    /**
     * Produces a documentfragment whith formatted Python sourcecode.
     * Reserved words, comments and text literals are hilited.
     * Can handle Python only
     * @param txt The string to parse
     * @param encoding The encoding to use when we make the DocumentFragment
     * @param theReporter The reporter object that handles errorreports
     * @return The produced Documentfragment, none if we cannot make it
     */
    static protected String producePythonCodeFragmentFromString(String txt, String encoding, reporter theReporter) {
        // flags
        boolean quote_is_on = false;
        boolean apos_is_on = false;
        boolean escape_is_on = false;
        boolean triple_is_on = false;

        boolean grid_is_on = false;


        // handle triple-quotes specially
        // reuse tab, since we have replaced it with spaces
        txt = txt.replaceAll("\"\"\"", "\t");


        StringBuilder newText = new StringBuilder(2048);
        try {
            StringReader rd = new StringReader(txt);
            StreamTokenizer st = new StreamTokenizer(rd);
            st.resetSyntax();
            st.wordChars('A', 'Z');
            st.wordChars('a', 'z');
            st.wordChars('0', '9');
            st.wordChars('_', '_');

            st.eolIsSignificant(true);

            int token = st.nextToken();
            while (token != StreamTokenizer.TT_EOF) {
                switch (token) {
                    case StreamTokenizer.TT_WORD:
                        String w = st.sval;
                        if (apos_is_on || quote_is_on || escape_is_on || grid_is_on || triple_is_on) {
                            newText.append(st.sval);
                        } else {
                            newText.append(checkWord(st.sval, "pythoncode"));
                        }
                        escape_is_on = false;
                        break;

                    case StreamTokenizer.TT_EOL:
                        if (grid_is_on) {
                            newText.append("</span>");
                        }
                        grid_is_on = false;
                        newText.append(EOL);
                        escape_is_on = false;
                        break;

                    case StreamTokenizer.TT_EOF:
                        // End of file has been reached
                        escape_is_on = false;
                        break;

                    default:
                        // An ordinary character was found, check it out
                        char ch = (char) st.ttype;
                        switch (ch) {
                            case '\\':
                                newText.append(ch);
                                escape_is_on = !escape_is_on;
                                break;
                            case '#':
                                if ((!escape_is_on) && (!grid_is_on) && (!triple_is_on) && (!apos_is_on) && (!quote_is_on)) {
                                    newText.append("<span class=\"" + COM + "\">#");
                                    grid_is_on = true;
                                } else {
                                    newText.append(ch);
                                }
                                escape_is_on = false;
                                break;
                            case '\t':
                                //newText.append("\"\"\"");
                                if (!triple_is_on) {
                                    newText.append("\"\"\"");
                                    newText.append("<span class=\"" + LITERAL + "\">");
                                } else {
                                    newText.append("</span>");
                                    newText.append("\"\"\"");
                                }
                                triple_is_on = !triple_is_on;
                                escape_is_on = false;
                                break;
                            case '"':
                                newText.append(ch);
                                if ((!escape_is_on) && (!triple_is_on) && (!apos_is_on) && (!grid_is_on)) {
                                    if (!quote_is_on) {
                                        newText.append("<span class=\"" + LITERAL + "\">");
                                    } else {
                                        newText.append("</span>");
                                    }
                                    quote_is_on = !quote_is_on;
                                }
                                escape_is_on = false;
                                break;
                            case '\'':
                                newText.append(ch);
                                if ((!escape_is_on) && (!triple_is_on) && (!quote_is_on) && (!grid_is_on)) {
                                    if (!apos_is_on) {
                                        newText.append("<span class=\"" + LITERAL + "\">");
                                    } else {
                                        newText.append("</span>");
                                    }
                                    apos_is_on = !apos_is_on;
                                }
                                escape_is_on = false;
                                break;
                            case '>':
                                newText.append("&gt;");
                                escape_is_on = false;
                                break;
                            case '<':
                                newText.append("&lt;");
                                escape_is_on = false;
                                break;
                            case '&':
                                newText.append("&amp;");
                                escape_is_on = false;
                                break;
                            default:
                                newText.append(ch);
                                escape_is_on = false;
                        }

                        break;
                }
                token = st.nextToken();
            }
        } catch (IOException e) {
            System.out.println("codeformatter:produceCodeFragmentFromString: " + e.getMessage());
        }

        // what to do if this is not properly closed
        // the ambition is not to do a syntax report
        // just attempt a soft landing
        if ((quote_is_on) || (apos_is_on) || (grid_is_on) || (triple_is_on)) {
            theReporter.pushMessage("unclosed_element_in_file", "pycode");
        }
        if (quote_is_on) {
            newText.append("</span>");
        }
        if (apos_is_on) {
            newText.append("</span>");
        }
        if (grid_is_on) {
            newText.append("</span>");
        }
        //if(escape_is_on)
        //    newText.append("</span>");
        if (triple_is_on) {
            newText.append("</span>");
        }


        String S = newText.toString();
        return S;
    }

    /**
     * Produces a documentfragment whith formatted XSLT sourcecode.
     * Reserved words, comments and text literals are hilited.
     * Can handle XSLT only
     * @param txt The string to parse
     * @param encoding The encoding to use when we make the DocumentFragment
     * @param theReporter The reporter object that handles error reports
     * @return The produced Documentfragment, none if we cannot make it
     */
    static protected String produceXSLTCodeFragmentFromString(String txt, String encoding, reporter theReporter) {
        // flags
        boolean quote_is_on = false;
        boolean apos_is_on = false;
        boolean escape_is_on = false;
        boolean comment_is_on = false;

        // handle comment quotes specially
        // we use tabs since we have removed them earlier
        txt = txt.replaceAll("<!--", "\t");
        txt = txt.replaceAll("-->", "\t");

        StringBuilder newText = new StringBuilder(2048);
        try {
            StringReader rd = new StringReader(txt);
            StreamTokenizer st = new StreamTokenizer(rd);
            st.resetSyntax();
            st.wordChars('A', 'Z');
            st.wordChars('a', 'z');
            st.wordChars('0', '9');
            st.wordChars('-', '-');
            st.wordChars(':', ':');
            st.wordChars('/', '/');

            st.eolIsSignificant(true);


            int token = st.nextToken();
            while (token != StreamTokenizer.TT_EOF) {
                switch (token) {
                    case StreamTokenizer.TT_WORD:
                        String w = st.sval;
                        if (apos_is_on || quote_is_on || escape_is_on || comment_is_on) {
                            newText.append(w);
                        } else { // use TAGNAME for WORD  
                            if (w.startsWith("xsl:")) {
                                w = "<span class=\"" + TAGNAME + "\">" + w + "</span>";//WORD
                            } else if (w.startsWith("/xsl:")) {
                                w = "<span class=\"" + TAGNAME + "\">" + w + "</span>";//WORD
                            }
                            newText.append(w);
                        }
                        escape_is_on = false;
                        break;

                    case StreamTokenizer.TT_EOL:
                        newText.append(EOL);
                        escape_is_on = false;
                        break;

                    case StreamTokenizer.TT_EOF:
                        // End of file has been reached
                        escape_is_on = false;
                        break;

                    default:
                        // An ordinary character was found, check it out
                        char ch = (char) st.ttype;
                        switch (ch) {
                            case '\\':
                                newText.append(ch);
                                escape_is_on = !escape_is_on;
                                break;
                            case '\t':
                                if (comment_is_on) {
                                    newText.append("--&gt;</span>");
                                } else {
                                    newText.append("<span class=\"" + COM + "\">&lt;!--");
                                }
                                comment_is_on = !comment_is_on;
                                escape_is_on = false;
                                break;
                            case '"':
                                if ((!escape_is_on) && (!comment_is_on) && (!apos_is_on)) {
                                    if (!quote_is_on) {
                                        newText.append(ch);
                                        newText.append("<span class=\"" + LITERAL + "\">");
                                    } else {
                                        newText.append("</span>");
                                        newText.append(ch);
                                    }
                                    quote_is_on = !quote_is_on;
                                } else {
                                    newText.append(ch);
                                }
                                escape_is_on = false;
                                break;
                            case '\'':
                                newText.append(ch);
                                if ((!escape_is_on) && (!comment_is_on) && (!quote_is_on)) {
                                    if (!apos_is_on) {
                                        newText.append("<span class=\"" + LITERAL + "\">");
                                    } else {
                                        newText.append("</span>");
                                    }
                                    apos_is_on = !apos_is_on;
                                }

                                escape_is_on = false;
                                break;

                            case '>':
                                newText.append("&gt;");
                                escape_is_on = false;
                                break;

                            case '<':
                                newText.append("&lt;");
                                escape_is_on = false;
                                break;

                            case '&':
                                newText.append("&amp;");
                                escape_is_on = false;
                                break;
                            default:
                                newText.append(ch);
                                escape_is_on = false;
                        }

                        break;
                }
                token = st.nextToken();
            }
        } catch (IOException e) {
            System.out.println("codeformatter:produceCodeFragmentFromString: " + e.getMessage());
        }

        String S = newText.toString();
        return S;
    }

    /**
     * Produces a documentfragment whith formatted ASPX sourcecode.
     * Reserved words, comments and text literals are hilited.
     * Can handle ASPX and ASP
     * @param txt The string to parse
     * @param encoding The encoding to use when we make the DocumentFragment
     * @param theReporter The reporter object that handles error reports
     * @return The produced Documentfragment, none if we cannot make it
     */
    static protected String produceASPXCodeFragmentFromString(String txt, String encoding, reporter theReporter) {
        // flags
        boolean quote_is_on = false;
        boolean apos_is_on = false;
        boolean escape_is_on = false;
        boolean comment_is_on = false;

        // handle comment quotes specially
        // we use tabs since we have removed them earlier
        txt = txt.replaceAll("<!--", "\t");
        txt = txt.replaceAll("-->", "\t");

        StringBuilder newText = new StringBuilder(2048);
        try {
            StringReader rd = new StringReader(txt);
            StreamTokenizer st = new StreamTokenizer(rd);
            st.resetSyntax();
            st.wordChars('A', 'Z');
            st.wordChars('a', 'z');
            st.wordChars('0', '9');
            st.wordChars('-', '-');
            st.wordChars(':', ':');
            st.wordChars('/', '/');

            st.eolIsSignificant(true);


            int token = st.nextToken();
            while (token != StreamTokenizer.TT_EOF) {
                switch (token) {
                    case StreamTokenizer.TT_WORD:
                        String w = st.sval;
                        if (apos_is_on || quote_is_on || escape_is_on || comment_is_on) {
                            newText.append(w);
                        } else {
                            if (w.startsWith("asp:")) {
                                w = "<span class=\"" + WORD + "\">" + w + "</span>";
                            } else if (w.startsWith("/asp:")) {
                                w = "<span class=\"" + WORD + "\">" + w + "</span>";
                            }
                            newText.append(w);
                        }
                        escape_is_on = false;
                        break;

                    case StreamTokenizer.TT_EOL:
                        newText.append(EOL);
                        escape_is_on = false;
                        break;

                    case StreamTokenizer.TT_EOF:
                        // End of file has been reached
                        escape_is_on = false;
                        break;

                    default:
                        // An ordinary character was found, check it out
                        char ch = (char) st.ttype;
                        switch (ch) {
                            case '\\':
                                newText.append(ch);
                                escape_is_on = !escape_is_on;
                                break;
                            case '\t':
                                if (comment_is_on) {
                                    newText.append("--&gt;</span>");
                                } else {
                                    newText.append("<span class=\"" + COM + "\">&lt;!--");
                                }
                                comment_is_on = !comment_is_on;
                                escape_is_on = false;
                                break;
                            case '"':
                                if ((!escape_is_on) && (!comment_is_on) && (!apos_is_on)) {
                                    if (!quote_is_on) {
                                        newText.append(ch);
                                        newText.append("<span class=\"" + LITERAL + "\">");
                                    } else {
                                        newText.append("</span>");
                                        newText.append(ch);
                                    }
                                    quote_is_on = !quote_is_on;
                                } else {
                                    newText.append(ch);
                                }
                                escape_is_on = false;
                                break;
                            case '\'':
                                newText.append(ch);
                                if ((!escape_is_on) && (!comment_is_on) && (!quote_is_on)) {
                                    if (!apos_is_on) {
                                        newText.append("<span class=\"" + LITERAL + "\">");
                                    } else {
                                        newText.append("</span>");
                                    }
                                    apos_is_on = !apos_is_on;
                                }

                                escape_is_on = false;
                                break;

                            case '>':
                                newText.append("&gt;");
                                escape_is_on = false;
                                break;

                            case '<':
                                newText.append("&lt;");
                                escape_is_on = false;
                                break;

                            case '&':
                                newText.append("&amp;");
                                escape_is_on = false;
                                break;
                            default:
                                newText.append(ch);
                                escape_is_on = false;
                        }

                        break;
                }
                token = st.nextToken();
            }
        } catch (IOException e) {
            System.out.println("codeformatter:produceCodeFragmentFromString: " + e.getMessage());
        }

        String S = newText.toString();
        return S;
    }

    /**
     * Produces a documentfragment whith formatted Matlab sourcecode.
     * Reserved words, comments and text literals are hilited.
     * Can handle Matlab only
     * @param txt The string to parse
     * @param encoding The encoding to use when we make the DocumentFragment
     * @param theReporter The reporter object that handles errorreports
     * @return The produced Documentfragment, none if we cannot make it
     */
    static protected String produceMatlabCodeFragmentFromString(String txt, String encoding, reporter theReporter) {
        // flags
        boolean quote_is_on = false;
        boolean apos_is_on = false;
        boolean escape_is_on = false;

        boolean comment_is_on = false;
        boolean block_comment_is_on = false;


        StringBuilder newText = new StringBuilder(2048);
        try {
            StringReader rd = new StringReader(txt);
            StreamTokenizer st = new StreamTokenizer(rd);
            st.resetSyntax();
            st.wordChars('A', 'Z');
            st.wordChars('a', 'z');
            st.wordChars('0', '9');
            st.wordChars('_', '_');

            st.eolIsSignificant(true);

            int token = st.nextToken();
            while (token != StreamTokenizer.TT_EOF) {
                switch (token) {
                    case StreamTokenizer.TT_WORD:
                        String w = st.sval;
                        if (block_comment_is_on && w.endsWith("%}")) {
                            newText.append(w).append("</span>");
                            block_comment_is_on = false;
                        } else if (apos_is_on || quote_is_on || escape_is_on || comment_is_on || block_comment_is_on) {
                            newText.append(st.sval);
                        } else if (w.startsWith("%{")) {
                            if (w.endsWith("%}")) {
                                newText.append("<span class=\"" + COM + "\">").append(w).append("</span>");

                            } else {
                                newText.append("<span class=\"" + COM + "\">").append(w);
                                block_comment_is_on = true;
                            }
                        } else {
                            newText.append(checkWord(st.sval, "matlabcode"));
                        }
                        escape_is_on = false;
                        break;

                    case StreamTokenizer.TT_EOL:
                        if (comment_is_on) {
                            newText.append("</span>");
                        }
                        comment_is_on = false;
                        newText.append(EOL);
                        escape_is_on = false;
                        break;

                    case StreamTokenizer.TT_EOF:
                        // End of file has been reached
                        escape_is_on = false;
                        break;

                    default:
                        // An ordinary character was found, check it out
                        char ch = (char) st.ttype;
                        switch (ch) {
                            case '\\':
                                newText.append(ch);
                                escape_is_on = !escape_is_on;
                                break;
                            case '%':
                                if ((!escape_is_on) && (!comment_is_on) && (!block_comment_is_on) && (!apos_is_on) && (!quote_is_on)) {
                                    newText.append("<span class=\"" + COM + "\">%");
                                    comment_is_on = true;
                                }
                                escape_is_on = false;
                                break;
                            case '\'':
                                newText.append(ch);
                                if ((!escape_is_on) && (!quote_is_on) && (!comment_is_on) && (!block_comment_is_on)) {
                                    if (!apos_is_on) {
                                        newText.append("<span class=\"" + LITERAL + "\">");
                                    } else {
                                        newText.append("</span>");
                                    }
                                    apos_is_on = !apos_is_on;
                                }
                                //escape_is_on=false;
                                break;
                            case '>':
                                newText.append("&gt;");
                                escape_is_on = false;
                                break;
                            case '<':
                                newText.append("&lt;");
                                //escape_is_on=false;
                                break;
                            case '&':
                                newText.append("&amp;");
                                //escape_is_on=false;
                                break;
                            default:
                                newText.append(ch);
                                escape_is_on = false;
                        }

                        break;
                }
                token = st.nextToken();
            }
        } catch (IOException e) {
            System.out.println("codeformatter:produceCodeFragmentFromString: " + e.getMessage());
        }

        String S = newText.toString();
        return S;
    }

    /**
     * Produces a documentfragment whith formatted CSS sourcecode.
     * Reserved words, comments and text literals are hilited.
     * Can handle CSS only
     * @param txt The string to parse
     * @param encoding The encoding to use when we make the DocumentFragment
     * @param theReporter The reporter object that handles errorreports
     * @return The produced Documentfragment, none if we cannot make it
     */
    static protected String produceCSSCodeFragmentFromString(String txt, String encoding, reporter theReporter) {
        // flags
        boolean quote_is_on = false;
        //boolean apos_is_on=false;
        boolean escape_is_on = false;

        boolean class_is_on = false;
        boolean id_is_on = false;

        boolean slash_star_is_on = false;

        // make sure we are able to finnish  one-line comments
        if (!txt.endsWith(EOL)) {
            txt = txt + EOL;
        }


        StringBuilder newText = new StringBuilder(2048);
        try {
            StringReader rd = new StringReader(txt);
            StreamTokenizer st = new StreamTokenizer(rd);
            st.resetSyntax();
            st.wordChars('A', 'Z');
            st.wordChars('a', 'z');
            st.wordChars('0', '9');
            //st.wordChars('_', '_');
            st.wordChars('-', '-');



            st.wordChars('*', '*');
            st.wordChars('/', '/');

            st.eolIsSignificant(true);

            int token = st.nextToken();
            while (token != StreamTokenizer.TT_EOF) {
                switch (token) {
                    case StreamTokenizer.TT_WORD:
                        String w = st.sval;
                        if (slash_star_is_on && w.endsWith("*/")) {
                            newText.append(w).append("</span>");
                            slash_star_is_on = false;
                        } else if (quote_is_on || escape_is_on || slash_star_is_on) {
                            newText.append(st.sval);
                        } else if (w.startsWith("/*")) {
                            if (w.endsWith("*/")) {
                                newText.append("<span class=\"" + COM + "\">").append(w).append("</span>");
                            } else {
                                newText.append("<span class=\"" + COM + "\">").append(w);
                                slash_star_is_on = true;
                            }

                        } else {
                            // want to pick up both css words and html-tags
                            String wd = st.sval;
                            if ((!class_is_on) && (!id_is_on)) {
                                String tmp = isCSSWord(wd);
                                if (wd.compareTo(tmp) != 0) {
                                    newText.append(tmp);
                                } else {
                                    tmp = isHTMLTag(wd);
                                    if (wd.compareTo(tmp) != 0) {
                                        newText.append(tmp.replaceAll(TAGNAME, ATTNAME));
                                    } else {
                                        newText.append(wd);
                                    }
                                }
                            } else {
                                //newText.append(checkWord(wd, "csscode"));
                                newText.append(wd);
                            }
                        }
                        escape_is_on = false;
                        class_is_on = false;
                        break;

                    case StreamTokenizer.TT_EOL:
                        newText.append(EOL);
                        escape_is_on = false;
                        break;

                    case StreamTokenizer.TT_EOF:
                        // End of file has been reached
                        escape_is_on = false;
                        class_is_on = false;
                        id_is_on = false;
                        break;

                    default:
                        // An ordinary character was found, check it out
                        class_is_on = false;
                        id_is_on = false;
                        char ch = (char) st.ttype;
                        switch (ch) {
                            case '\\':
                                newText.append(ch);
                                escape_is_on = !escape_is_on;
                                break;
                            case '"':
                                newText.append(ch);
                                if ((!escape_is_on) && (!slash_star_is_on))//apos_is_on ?
                                {
                                    if (!quote_is_on) {
                                        newText.append("<span class=\"" + LITERAL + "\">");
                                    } else {
                                        newText.append("</span>");
                                    }
                                    quote_is_on = !quote_is_on;
                                }
                                escape_is_on = false;
                                break;

                            case '>':
                                newText.append("&gt;");
                                escape_is_on = false;
                                break;
                            case '<':
                                newText.append("&lt;");
                                escape_is_on = false;
                                break;
                            case '&':
                                newText.append("&amp;");
                                escape_is_on = false;
                                break;
                            case ':':
                                newText.append(ch);
                                break;
                            case ';':
                                newText.append(ch);
                                break;
                            case '}':
                                newText.append(ch);
                                break;
                            case '.':
                                newText.append(ch);
                                class_is_on = true;
                                break;
                            case '#':
                                newText.append(ch);
                                id_is_on = true;
                                break;
                            default:
                                newText.append(ch);
                                escape_is_on = false;
                        }

                        break;
                }
                token = st.nextToken();
            }
        } catch (IOException e) {
            System.out.println("codeformatter:produceCodeFragmentFromString: " + e.getMessage());
        }

        // what to do if this is not properly closed
        // the ambition is not to do a syntax report
        // just attempt a soft landing
        if (quote_is_on) {
            newText.append("</span>");
        }


        if (slash_star_is_on) {
            newText.append("</span>");
        }

        String S = newText.toString();
        return S;
    }

    /**
     * Produces a documentfragment whith formatted CSS sourcecode.
     * Reserved words, comments and text literals are hilited.
     * Can handle CSS only
     * @param txt The string to parse
     * @param encoding The encoding to use when we make the DocumentFragment
     * @param theReporter The reporter object that handles errorreports
     * @return The produced Documentfragment, none if we cannot make it
     */
    static protected String produceSQLCodeFragmentFromString(String txt, String encoding, reporter theReporter) {
        // flags
        boolean quote_is_on = false;
        boolean apos_is_on = false;
        //boolean escape_is_on=false;

        boolean slash_slash_is_on = false;
        boolean id_is_on = false;

        boolean slash_star_is_on = false;

        // make sure we are able to finnish  one-line comments
        if (!txt.endsWith(EOL)) {
            txt = txt + EOL;
        }


        StringBuilder newText = new StringBuilder(2048);
        try {
            StringReader rd = new StringReader(txt);
            StreamTokenizer st = new StreamTokenizer(rd);
            st.resetSyntax();
            st.wordChars('A', 'Z');
            st.wordChars('a', 'z');
            st.wordChars('0', '9');
            //st.wordChars('_', '_');
            st.wordChars('-', '-');



            st.wordChars('*', '*');
            st.wordChars('/', '/');

            st.eolIsSignificant(true);

            int token = st.nextToken();
            while (token != StreamTokenizer.TT_EOF) {
                switch (token) {
                    case StreamTokenizer.TT_WORD:
                        String w = st.sval;
                        if (slash_star_is_on && w.endsWith("*/")) {
                            newText.append(w).append("</span>");
                            slash_star_is_on = false;
                        } else if (quote_is_on || slash_star_is_on) {
                            newText.append(st.sval);
                        } else if (w.startsWith("/*")) {
                            if (w.endsWith("*/")) {
                                newText.append("<span class=\"" + COM + "\">").append(w).append("</span>");
                            } else {
                                newText.append("<span class=\"" + COM + "\">").append(w);
                                slash_star_is_on = true;
                            }

                        } else if (w.startsWith("--")) {
                            StringBuilder append = newText.append("<span class=\"" + COM + "\">" + w);
                            slash_slash_is_on = true;
                        } else {
                            // want to pick up  sql
                            String wd = st.sval;
                            if ((!slash_slash_is_on) && (!id_is_on) && (!slash_star_is_on)) {
                                String tmp = isSQLWord(wd);
                                newText.append(tmp);
                            } else {
                                newText.append(wd);
                            }
                        }

                        break;

                    case StreamTokenizer.TT_EOL:
                        if (slash_slash_is_on) {
                            newText.append("</span>");
                        }
                        if (id_is_on) {
                            newText.append("</span>");
                        }
                        newText.append(EOL);

                        slash_slash_is_on = false;
                        id_is_on = false;
                        break;

                    case StreamTokenizer.TT_EOF:
                        // End of file has been reached

                        slash_slash_is_on = false;
                        id_is_on = false;

                        break;

                    default:
                        // An ordinary character was found, check it out

                        //id_is_on=false;
                        char ch = (char) st.ttype;
                        switch (ch) {
                            case '\\':
                                newText.append(ch);
                                break;
                            case '"':
                            case '\'':
                                newText.append(ch);
                                if ((!id_is_on) && (!slash_star_is_on) && (!slash_slash_is_on)) {
                                    if (!quote_is_on) {
                                        newText.append("<span class=\"" + LITERAL + "\">");
                                    } else {
                                        newText.append("</span>");
                                    }
                                    quote_is_on = !quote_is_on;
                                }
                                break;

                            case '>':
                                newText.append("&gt;");
                                break;
                            case '<':
                                newText.append("&lt;");
                                break;
                            case '&':
                                newText.append("&amp;");
                                break;

                            case '#':
                                newText.append("<span class=\"" + COM + "\">").append(ch);
                                id_is_on = true;
                                break;


                            default:
                                newText.append(ch);
                        }

                        break;
                }
                token = st.nextToken();
            }
        } catch (IOException e) {
            System.out.println("codeformatter:produceCodeFragmentFromString: " + e.getMessage());
        }

        // what to do if this is not properly closed
        // the ambition is not to do a syntax report
        // just attempt a soft landing
        if (quote_is_on) {
            newText.append("</span>");
        }


        if (slash_star_is_on) {
            newText.append("</span>");
        }

        String S = newText.toString();
        return S;
    }

    /**
     * Produces a documentfragment whith simple HTML sourcecode.
     * Reserved words, comments and text literals are hilited.
     * Can handle HTML only
     * @param txt The string to parse
     * @param encoding The encoding to use when we make the DocumentFragment
     * @param theReporter The reporter object that handles errorreports
     * @return The produced Documentfragment, none if we cannot make it
     */
    static protected String produceHTMLCodeFragmentFromString(String txt, String encoding, reporter theReporter) {
        // flags
        boolean quote_is_on = false;
        boolean escape_is_on = false;
        boolean comment_is_on = false;

        // replace tab so we can reuse it for comments
        txt = txt.replaceAll("\t", TAB_WIDTH);
        txt = txt.replaceAll("<!--", "\t");
        txt = txt.replaceAll("-->", "\t");


        // make sure we are able to finnish  one-line comments
        if (!txt.endsWith(EOL)) {
            txt = txt + EOL;
        }


        StringBuilder newText = new StringBuilder(2048);
        try {
            StringReader rd = new StringReader(txt);
            StreamTokenizer st = new StreamTokenizer(rd);
            st.resetSyntax();
            st.wordChars('A', 'Z');
            st.wordChars('a', 'z');
            st.wordChars('0', '9');

            st.eolIsSignificant(true);

            int token = st.nextToken();
            while (token != StreamTokenizer.TT_EOF) {
                switch (token) {
                    case StreamTokenizer.TT_WORD:
                        String w = st.sval;
                        if (quote_is_on) {
                            newText.append(st.sval);
                        } else {// want to pick up html-tags
                            newText.append(checkWord(st.sval, CodeBasics.LANG_HTML));
                        }
                        escape_is_on = false;

                        break;

                    case StreamTokenizer.TT_EOL:
                        newText.append(EOL);
                        escape_is_on = false;
                        break;

                    case StreamTokenizer.TT_EOF:
                        // End of file has been reached
                        escape_is_on = false;
                        break;

                    default:
                        // An ordinary character was found, check it out
                        char ch = (char) st.ttype;
                        switch (ch) {
                            case '\\':
                                newText.append(ch);
                                escape_is_on = !escape_is_on;
                                break;
                            case '"':
                                newText.append(ch);
                                if (!escape_is_on)//apos_is_on ?
                                {
                                    if (!quote_is_on) {
                                        newText.append("<span class=\"" + LITERAL + "\">");
                                    } else {
                                        newText.append("</span>");
                                    }
                                    quote_is_on = !quote_is_on;
                                }
                                escape_is_on = false;
                                break;


                            case '>':
                                newText.append("&gt;");
                                escape_is_on = false;
                                break;
                            case '<':
                                newText.append("&lt;");
                                escape_is_on = false;
                                break;

                            case '&':
                                newText.append("&amp;");
                                escape_is_on = false;
                                break;
                            case ':':
                                newText.append(ch);
                                break;
                            case ';':
                                newText.append(ch);
                                break;
                            case '}':
                                newText.append(ch);
                                break;
                            case '\t':
                                if (!comment_is_on) {
                                    newText.append("&lt;!--<span class=\"" + COM + "\">");
                                } else {
                                    newText.append("</span>--&gt;");
                                }
                                comment_is_on = !comment_is_on;
                                break;
                            default:
                                newText.append(ch);
                                escape_is_on = false;
                        }

                        break;
                }
                token = st.nextToken();
            }
        } catch (IOException e) {
            System.out.println("codeformatter:produceCodeFragmentFromString: " + e.getMessage());
        }

        // what to do if this is not properly closed
        // the ambition is not to do a syntax report
        // just attempt a soft landing
        // could a warning
        if (quote_is_on) {
            theReporter.pushMessage("unclosed_element_in_file", "html");
            newText.append("</span>");
        }
        if (comment_is_on) {
            theReporter.pushMessage("unclosed_element_in_file", "html");
            newText.append("</span>");
        }
        String S = newText.toString();
        return S;
    }

    /**
     * Expand preformatted codefragments.
     * Identifies and expands pre tags with class:
     * ALL_OLD_CODE_TYPES
     * @param doc The document we investigate
     * @param encoding The encoding we want
     * @param mod The Module requesting this
     * @return The document with expanded code
     */
    public static Document expandCodeFragments(Document doc, String encoding, Module mod) {
        reporter theReporter = mod.getReporter();
        NodeList nlist = null;
        String xp = null;

        // pick up old format and make new
        CodeBasics.transformToNewForm(doc);
        //--------------------------------
        // acess the actual nodes again. Pick up those set according
        // to new Google pretty originally
        xp = "//pre[contains(@class,'" + CodeBasics.PRETTYPRINT + "')]";
        try {
            nlist = domer.performXPathQuery(doc, xp);
        } catch (Exception te) {
            System.out.println("expandCodeFragments:" + te.getMessage());
            theReporter.pushMessage("failed_to_expand_code", "preformatting", te.getMessage());
            return doc;
        }
        // run the list and do the job
        for (int ix = 0; ix < nlist.getLength(); ix++) {
            if (nlist.item(ix).getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element eltItem = (Element) nlist.item(ix);

            eltItem.normalize();
            // if no children, there is nothing we can do
            if (!eltItem.hasChildNodes()) {
                continue;
            }

            // mark as not translate
            String cstmp = eltItem.getAttribute("class");
            if (cstmp.indexOf(PIcommand.SKIPTRANSLATE) == -1) {
                eltItem.setAttribute("class", cstmp + " " + PIcommand.SKIPTRANSLATE);
            }
            if (cstmp.indexOf(CodeBasics.PRETTYPRINT) == -1) {
                eltItem.setAttribute("class", cstmp.replace(CodeBasics.PRETTYPRINT, CodeBasics.READYANDPRETTY));
            }

            // containg tags because it is already expanded or what ?
            if (eltItem.getElementsByTagName("*").getLength() != 0) {
                continue;
            }

            // if we have a CDATA section we use it, the first and
            // assumed only one
            Node cdataNode = CodeBasics.getFirstCDATANode(eltItem);
            String content = null;
            if (cdataNode != null) {
                content = cdataNode.getTextContent();
            } else {
                // we pick up the first text node
                // assuming that this is the only one
                content = eltItem.getFirstChild().getNodeValue();
            }
            // what is the code type ?
            String codetype = CodeBasics.getNewCodeType(eltItem.getAttribute("class"));
            try {
                DocumentFragment df = produceCodeFragmentFromString(content, codetype, encoding, mod);
                if (df != null) {
                    Node impN = doc.importNode(df, true);
                    eltItem.getParentNode().replaceChild(impN, eltItem);
                }
            } catch (DOMException dex) {
                theReporter.pushMessage("failed_to_expand_code", codetype, dex.getMessage());
            }

        }
        return doc;
    }
}

package utils;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.imageio.ImageIO;

/**
 * This is a library with utility methods for
 * fileaccess, path and URI manipulation etc.
 * Only static methods
 */
public class accessutils {

    /** default encoding */
    static private String default_encoding = "UTF-8";
    /** constant for error in parse name value pairs */
    static public String NONAME = "_noName_";

    /** set default encoding
     * @param s The new encoding
     */
    public static void setDefaultEncoding(String s) {
        default_encoding = s;
    }

    /**
     * get default encoding
     * @return the encoding
     */
    public static String getDefaultEncoding() {
        return default_encoding;
    }

    /**
     * Check if a path is absolute dependent on operating system .
     *@param path The path we will check
     *@return true if it is absolute, false otherwise
     */
    public static boolean isAbsoluteAddress(String path) {
        /* Cases to consider:
        C:\folder\file   
        /something/something 
        http://www.something
        file://something
         */
        try {
            File f = new File(path);
            // handles driver:\folder\file (if win) and /folder/file (if linux)
            if (f.isAbsolute()) {
                return true;
            }
            path = path.trim();
            if (path.startsWith("http") || path.startsWith("file")) {
                return f.toURI().isAbsolute();
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /** 
     *  Try to make necessary catalogs if the theUri (file) does not exist.
     * 
     * @param theUri The URI for the file
     * @return true if the catalogpath exists when we leave, false otherwise
     */
    public static boolean makeCatalog(URI theUri) {
        try {
            File f = new File(theUri);
            if (!f.exists()) {
                new File(f.getParent()).mkdirs();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** 
     *  Try to make necessary catalogs if the file does not exist.
     * 
     * @param path The path for the file
     * @return true if the catalogpath exists when we leave, false otherwise
     */
    public static boolean makeCatalog(String path) {
        try {
            File f = new File(path);
            if (!f.exists()) {
                new File(f.getParent()).mkdirs();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get the digital start of a string, only unsigned positive integers
     * @param s The string we will examine
     * @return the integer part or -1 if not integer start
     */
    public static int getNumericStart(String s) {
        s = s.trim();
        if (!Character.isDigit(s.charAt(0))) {
            return -1;
        }

        if (Character.isDigit(s.charAt(s.length() - 1))) {
            return Integer.parseInt(s);
        }

        int ix = 0;
        while ((ix < s.length()) && (Character.isDigit(s.charAt(ix)))) {
            ix++;
        }
        String tmp = s.substring(0, ix);
        return Integer.parseInt(tmp);
    }

    /**
     * Remove filepart from an URI.
     * 
     * @param theUri The URI we will work on
     * @return the URI with removed filepart, i.e. a catalog
     */
    public static URI removeFilePart(URI theUri) {
        URI resUri = null;
        String tmp = removeFilePartFromPathstring(theUri.toString());
        try {
            //resUri=new URI(tmp);
            resUri = makeUri(tmp);
            return resUri;
        } catch (Exception e) {
            return theUri;
        }
    }

    /**
     * Remove filepart from a path.
     * 
     * @param pt The path we will work on
     * @return the reduced path if file was identified, otherwise the path as received
     */
    public static String removeFilePartFromPathstring(String pt) {
        pt = pt.replace('\\', '/');
        int lastslash = pt.lastIndexOf('/');
        int lastdot = pt.lastIndexOf('.');
        if (lastdot > lastslash) {
            return pt.substring(0, lastslash);
        }
        return pt;
    }

    /**
     * Access the filepart without extension from a path.
     *
     * @param pt The path we will work on
     * @return the filename, otherwise the path as received
     */
    public static String getFileNameFromPathString(String pt) {
        pt = pt.replace('\\', '/');
        int lastslash = pt.lastIndexOf('/');
        int lastdot = pt.lastIndexOf('.');
        if (lastdot > lastslash) {
            return pt.substring(lastslash + 1, lastdot);
        }
        return pt;
    }

    /**
     * Return the extension (string behind last .) of the file with uri uristr
     * @param uristr The string where we try to locate the extension
     * @return The extension. return null if no extenison is found
     */
    public static String getFileExtension(String uristr) {
        int pos = uristr.lastIndexOf('.');
        if (pos != -1) {
            return uristr.substring(pos + 1);
        }
        return null;
    }

    /**
     * Build an URI which refers from one absolute URI to another absolute URI.
     * 
     * @param from_uri The URI we refer from
     * @param to_uri The URI we refer to
     * @return The relative URI, or to_uri if not both are absolute
     */
    public static URI makeRelativeURI(URI from_uri, URI to_uri) {
        // both are assumed to be complete absolute addresses of two files
        // we want to find out how we can reference to_uri from from_uri
        //@TODO : this method should be revised



        // if either is relative we return to_uri
        if ((!to_uri.isAbsolute()) || (!from_uri.isAbsolute())) {
            return to_uri;
        }

        // if they are equal, we try to return the filepath
        if (from_uri.toString().compareTo(to_uri.toString()) == 0) {
            String thepath = to_uri.getPath();
            int pos = thepath.lastIndexOf('/');
            try {
                //return new URI(thepath.substring(pos+1));
                return makeUri(thepath.substring(pos + 1));
            } catch (Exception se) {
                return to_uri;
            }
        }

        // ok both are absolute and they are different
        // remove filepart, if necessary
        URI from_uri_cat = removeFilePart(from_uri);
        URI relUri = from_uri_cat.relativize(to_uri);

        if (!relUri.isAbsolute()) {
            return relUri;
        }

        // we may not be happy with this if we get an absolute URI 
        // we must resolve the case where the result should start with ../
        String fromHost = from_uri.getHost();
        String toHost = to_uri.getHost();
        if ((fromHost != null) && (toHost != null)) {
            if (from_uri.getHost().compareToIgnoreCase(to_uri.getHost()) != 0) {
                return relUri;
            }
        }

        String fromScheme = from_uri.getScheme();
        String toScheme = to_uri.getScheme();
        if (fromScheme.compareTo(toScheme) != 0) {
            return to_uri;
        }
        String fromPath = from_uri.getPath();
        String toiPath = to_uri.getPath();
        String toAuhtority = to_uri.getAuthority();
        String toQuery = to_uri.getQuery();
        String toFragment = to_uri.getFragment();

        String theNewPath = "";

        // start reducing the paths
        String from_list[] = fromPath.split("/");
        String to_list[] = toiPath.split("/");
        int pos = 0;
        while ((pos < to_list.length)
                && (pos < from_list.length)
                && (from_list[pos].compareTo(to_list[pos]) == 0)) {
            pos++;
        }
        if ((pos == from_list.length) && (pos == to_list.length)) {
            try {
                // in same catalog
                return new URI(null, null, to_list[pos - 1], toQuery, toFragment);
            } catch (URISyntaxException e) {
                return to_uri;
            }
        }

        for (int ix = 0; ix < (from_list.length - pos - 1); ix++) {
            theNewPath += "../";
        }
        for (int ix = pos; ix < (to_list.length); ix++) {
            theNewPath += to_list[ix] + "/";
        }
        theNewPath = theNewPath.substring(0, theNewPath.length() - 1);

        try {
            URI retUri = new URI(null, null, theNewPath, toQuery, toFragment);
            return retUri;
        } catch (URISyntaxException e) {
            System.out.println("accessUtils:getRelativeUri: " + e.getMessage());
            return to_uri;
        }
    }

    /**
     * Attempts to make an URI from a string. Spaces are replaced by %20
     * @param source The string we will interpret as an URI
     * @return The prepared UIR
     * @throws java.lang.Exception
     */
    public static URI makeUri(String source)
            throws Exception {
        source = source.trim();
        source = source.replace('\\', '/');
        source = source.replaceAll(" ", "%20");
        if (source.startsWith("http")) {
            try {
                //must locate and urlencode querypart ?
                int pos = source.indexOf('?');
                if (pos != -1) {
                    String p1 = source.substring(0, pos);
                    String p2 = source.substring(pos);
                    //p2=p2.replaceAll("=", "%3D");
                    //p2=p2.replaceAll(",", "%2C");
                    p2 = p2.replaceAll("&", "&amp;");
                    String tmp = p1 + p2;
                    URI tstUri = new URI(tmp);
                    return tstUri;
                }
            } catch (Exception e) {
                return new URI(source);
            }

        }
        return new URI(source);
    }

    /**
     * Build an absolute URI from a directorypath and a filepath.
     * The dirpath is only used if the filepath is relative.
     * 
     * @return An absolute URI
     * @param filepath The absolute or relative filepath
     * @param dirpath The absolute directory path
     * @throws java.lang.Exception if we dont succeed
     */
    public static URI makeAbsoluteURI(String filepath, String dirpath)
            throws Exception {
        // this is hazardiuos business
        //@TODO: Address calculation for all seasons
        // what about ?:
        //    samba access without mapping to drive: \\
        //    implisit: http://wwww , as browsers guess addresses
        //    [scheme:][//authority][path][?query][#fragment], with authority part

        URI theUri = null;
        URI fileUri = null;
        URI catUri = null;
        URI tstUri = null;

        if ((dirpath == null) || (filepath == null)) {
            throw new Exception();
        }

        filepath = filepath.trim();
        dirpath = dirpath.trim();




        dirpath = dirpath.replace('\\', '/');
        filepath = filepath.replace('\\', '/');



        if (!dirpath.endsWith("/")) {
            dirpath = removeFilePartFromPathstring(dirpath);
            if (!dirpath.endsWith("/")) {
                dirpath += "/";
            }
        }

        try {
            // work on the directory path
            // must set scheme file if catalog is a file URI
            if (dirpath.startsWith("/")) {
                // this must be an absolute URI (unix style)
                // with no scheme, that is a file
                dirpath = "file://" + dirpath;
            }
            //catUri=new URI(dirpath);
            //catUri=new URI(dirpath.replaceAll(" ","%20"));
            catUri = makeUri(dirpath);

            String scheme = catUri.getScheme();

            if ((scheme != null) && (scheme.length() < 2)) {
                // this probably is a windows absolute filepath 
                // starting with drive-letter
                //catUri=new URI("file:///"+catUri.toString());
                catUri = makeUri("file:///" + catUri.toString());
            }

            // work on the filepath

            //tstUri=new URI(filepath);
            //tstUri=new URI(filepath.replaceAll(" ","%20"));
            tstUri = makeUri(filepath);


            if (tstUri.isAbsolute()) {
                String tstscheme = tstUri.getScheme();
                if ((tstscheme != null) && (tstscheme.length() < 2)) {
                    // this probably is a windows absolute filepath 
                    // starting with drive-letter
                    //fileUri=new URI("file:///"+tstUri.toString());
                    fileUri = makeUri("file:///" + tstUri.toString());
                } else {
                    fileUri = tstUri;
                }
            } else {
                fileUri = tstUri;
            }
            //fileUri=new URI(filepath);


            theUri = catUri.resolve(fileUri);
            theUri = accessutils.fixUriParameterEntities(theUri);




            return theUri.normalize();
        } catch (Exception e) {
            String m = e.getMessage();
            throw e;
        }

    }

    /**
     * Append to a text file.
     * 
     * @param theUri The absolute URI to the file
     * @param text The text to append
     * @param maxFileSize When file reach this size in bytes it is reset, -1 keeps it going forever
     * @return true if save goes ok, false otherwise
     */
    public static boolean appendToTextFile(URI theUri, String text, long maxFileSize) {
        try {
            File f = new File(theUri);
            try {
                if (!f.exists()) {
                    return saveTextFile(f, text, false); // not append

                }
                if ((maxFileSize >= 0) && (f.length() > maxFileSize)) {
                    return saveTextFile(f, text, false); // not append
                }
                return saveTextFile(f, text, true); // append

            } catch (Exception ioe) {
                throw new Exception(ioe.getMessage());
            }
        } catch (Exception ex) {
            System.out.println("AccessUtils:appendToTextFile, Not written: " + theUri.toString());
            return false;
        }
    }

    /**
     * Save a text file.
     * 
     * @param bAppend true if we want to append to the file
     * @param f A file object
     * @param text The string to write
     * @return true if save goes ok, false otherwise
     */
    private static boolean saveTextFile(File f, String text, boolean bAppend) {
        FileWriter fwriter = null;
        try {
            if (!f.exists()) {
                try {
                    //URI theUri = makeAbsoluteURI(f.getAbsolutePath(), null);
                    URI theUri = f.toURI();
                    if (!makeCatalog(theUri)) {
                        throw new Exception("could not make catalog");
                    }
                } catch (Exception e) {
                    throw new Exception(e.getMessage());
                }
            }
            fwriter = new FileWriter(f, bAppend);
            fwriter.write(text);
            fwriter.close();
            return true;
        } catch (Exception ex) {
            System.out.println("AccessUtils:saveTextFile, Not written: " + f.getAbsolutePath());
            return false;
        } finally {
            try {
                if (fwriter != null) {
                    fwriter.close();
                }
            } catch (IOException ioe) {
            }
        }
    }

    /** Save a text file.
     * 
     * @param theUri The absolute URI to the file
     * @param text The text to write
     * @param encoding The encoding to use
     * @return true if save goes ok, false otherwise
     */
    public static boolean saveTFile(URI theUri, String text, String encoding) {
        File f;
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        try {
            f = new File(theUri);
            if (!f.exists()) {
                try {
                    //URI theUri = makeAbsoluteURI(f.getAbsolutePath(), null);
                    if (!makeCatalog(theUri)) {
                        throw new Exception("could not make catalog");
                    }
                } catch (Exception e) {
                    throw new Exception(e.getMessage());
                }
            }

            fos = new FileOutputStream(f);
            if (encoding != null) {
                osw = new OutputStreamWriter(fos, encoding);
            } else {
                osw = new OutputStreamWriter(fos);
            }
            osw.write(text);
            osw.close();
            return true;
        } catch (Exception ex) {
            System.out.println("AccessUtils:saveTextFile, Not written: " + theUri.toString());
            return false;
        }

    }

    /**
     * Trim left of a string.
     * @param s The string we will trim
     * @return the trimmed string
     */
    public static String trimLeft(String s) {
        int i = 0;
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
            i++;
        }
        return s.substring(i);
    }

    /**
     * Trim right of a string.
     * @param s The string we will trim
     * @return the trimmed string
     */
    public static String trimRight(String s) {
        int i = s.length() - 1;
        while (i > 0 && Character.isWhitespace(s.charAt(i))) {
            i--;
        }
        return s.substring(0, i + 1);
    }

    /**
     * Finds out if a string is in a string[].
     * 
     * @param name The name we are searching for
     * @param namelist The list we are searching
     * @return the index of the found word if found, else -1
     */
    public static int indexOfNameInList(String name, String[] namelist) {
        for (int ix = 0; ix < namelist.length; ix++) {
            if (namelist[ix].compareTo(name) == 0) {
                return ix;
            }
        }
        return -1;
    }

    /**
     * Finds out if one of the strings in a string[] is contained in another string[].
     * 
     * @param names The candidate names
     * @param namelist The list we are searching in
     * @return true if found, false otherwise
     */
    public static boolean isAnyNameInList(String[] names, String[] namelist) {
        for (int ix = 0; ix < names.length; ix++) {
            if (indexOfNameInList(names[ix], namelist) != -1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Produce a list of integers from a commaseparated string.
     * 
     * @param S The string we will parse
     * @return A list of integers, or null if S is unparsable
     */
    public static List<Integer> getIntegerList(String S) {
        String[] slist = S.split(",");
        List<Integer> list = new ArrayList<Integer>(slist.length);
        for (String s : slist) {
            try {
                Integer i = Integer.parseInt(s);
                list.add(i);
            } catch (Exception e) {
                return null;
            }
        }
        return list;
    }

    /**
     * Produce a list of integers from a python-like slice expression.
     * 
     * @param slice The expression
     * @param limit Max number of items. Only effective for slices
     * @return A list of integers, or null if slice is unparsable
     */
    public static List<Integer> getIntegerList(String slice, int limit) {
        List<Integer> list = new ArrayList<Integer>();
        // parse the slice
        // expect one of the following
        // a,b,c,  the named indexes 
        // [a:b]   [a..b>
        // [a:]    [a..limit]
        // [:b]    [0..b>
        // [:-b]   [limit-b..limit]
        slice = slice.trim();
        if (slice.startsWith("[")) {
            slice = slice.substring(1).trim();
        }
        if (slice.endsWith("]")) {
            slice = slice.substring(0, slice.length() - 1).trim();
        }

        if (slice.indexOf(":") == -1) {
            return getIntegerList(slice);
        }

        int lo = 0;
        int hi = limit + 1;
        try {
            if (slice.startsWith(":")) {
                hi = Integer.parseInt(slice.substring(1));
                if (hi < 0) {
                    lo = limit + hi + 1;
                    hi = limit + 1;
                } else {
                    hi = hi + 1;
                }
            } else if (slice.endsWith(":")) {
                lo = Integer.parseInt(slice.substring(0, slice.length() - 1));
            } else {
                String[] parts = slice.split(":");
                if (parts.length == 2) {
                    lo = Integer.parseInt(parts[0]);
                    hi = Integer.parseInt(parts[1]);
                }
            }
        } catch (Exception e) {
            return null;
        }
        for (int ix = lo; ix < hi; ix++) {
            list.add(ix);
        }

        return list;
    }

    /**
     * Parse for name-value pairs in a string and return them in a HashMap.
     * Pairs may be , separated, values must be quoted
     * @param data The string to parse
     * @param quoteMark How values are quoted: ' or "
     * @return a HashMap with names as keys
     */
    public static HashMap<String, String> parseNameValues(String data, char quoteMark) {
        /* Expected input variants:
         * (name1='value1',name2='value2')
         * name1='value1',name2='value2'
         * name1='value1' name2='value2'
         * name1="value1" name2="value2"
         */
        HashMap<String, String> pMap = new HashMap<String, String>();
        if ((data == null) || (data.length() < 3)) {
            return pMap;
        }
        data = data.trim();

        // we dont want to bother about linebreaks or tabs
        data = data.replaceAll("\r\n", " ");
        data = data.replaceAll("\n", " ");
        data = data.replaceAll("\t", " ");


        // get rid of opening and closing brackets if any
        // no check for wellfomredness
        if (data.startsWith("(")) {
            data = data.substring(1);
        }
        if (data.endsWith(")")) {
            data = data.substring(0, data.length() - 1);
        }

        boolean val_is_on = false;
        boolean nam_is_on = true;
        String currentName = NONAME;
        StringBuffer nam = new StringBuffer(32);
        StringBuffer val = new StringBuffer(256);

        for (int pos = 0; pos < data.length(); pos++) {
            char c = data.charAt(pos);
            if (val_is_on) {
                // the only way to finish a value is a quotemark
                if (c == quoteMark) {
                    if (val.toString().length() > 0) {
                        pMap.put(currentName, val.toString());
                    }
                    val = new StringBuffer(10);
                    val_is_on = false;
                    nam_is_on = true; // since commas are ignored as part of a name
                    currentName = NONAME;
                } // we accept everything else as part of a value
                else {
                    val.append(c);
                }
            } else if (nam_is_on) {
                // the only way to finish a name is =
                if (c == '=') {
                    currentName = nam.toString().trim();
                    nam = new StringBuffer(10);
                    nam_is_on = false;
                } // we ignore commas as potential part of name 
                else if (c == ',') {
                    continue;
                } // accept everything else
                else {
                    nam.append(c);
                }
            } // the only way to start a value is a quotemark
            else if (c == quoteMark) {
                val_is_on = true;
            }
        }
        return pMap;
    }

    /**
     * Parse a string with a transformation with parameters.
     * <p>
     * Forms: T(name='Ole',address='Halden').
     *   
     * @param ts The string to unpack
     * @return a hashmap with parameter name as key, and  value as value
     * @throws Exception when we cannot parse the parameterlist
     */
    public static HashMap<String, String> unpackTransformationParameters(String ts)
            throws Exception {
        HashMap<String, String> result = null;
        // first we find out if we have parameters
        int paramstartIx = ts.indexOf('(');
        int paramstoppIx = ts.lastIndexOf(')');
        if ((paramstartIx != -1)
                && (paramstoppIx == ts.length() - 1)
                && (paramstoppIx - paramstartIx > 1)) // allow () and ( ) without generating error
        {
            String tmp = ts.substring(paramstartIx);
            result = parseNameValues(tmp, '\'');
            if (result == null) {
                throw new Exception("unknown transformation");
            }
        }
        return result;
    }

    /**
     * Control if today is between two dates. 
     * @param firstdate First legal day ,yyy:mm:dd
     * @param lastdate Last legal day ,yyy:mm:dd
     * @return true if today is within span
     * @throws Exception when the dateformats is wrong. Should be:yyyy-mm-dd
     */
    static public boolean legalDating(String firstdate, String lastdate)
            throws Exception {
        // REMEMBER that the month is 0-based
        Calendar toDay = new GregorianCalendar();

        // allow some different separators
        if (firstdate != null) {
            firstdate = firstdate.trim();
            firstdate = firstdate.replace('-', ':');
            firstdate = firstdate.replace('_', ':');
            firstdate = firstdate.replace('/', ':');
            String[] parts = firstdate.split(":");
            if (parts.length != 3) {
                return false;
            }
            Calendar firstDATE = new GregorianCalendar();
            firstDATE.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
            if (firstDATE.after(toDay)) {
                return false;
            }
        }
        if (lastdate != null) {
            lastdate = lastdate.trim();
            lastdate = lastdate.replace('-', ':');
            lastdate = lastdate.replace('_', ':');
            lastdate = lastdate.replace('/', ':');
            String[] parts = lastdate.split(":");
            if (parts.length != 3) {
                return false;
            }
            Calendar lastDATE = new GregorianCalendar();
            lastDATE.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
            if (lastDATE.before(toDay)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Read a String from an URI.
     * 
     * @param uri The URI to read from
     * @param encoding The encoding
     * @return the string read
     * @throws Exception when the text cannot be loaded or the URI is not wellformed
     */
    public static String getTextFile(URI uri, String encoding)
            throws Exception {
        BufferedReader in = null;
        try {
            URL url = uri.toURL();
            if (encoding == null) {
                encoding = default_encoding;
            }
            // while testing: System.out.println(url.toString());
            in = new BufferedReader(new InputStreamReader(url.openStream(), encoding));
            StringBuffer result = new StringBuffer();
            String str;
            //String eol=System.getProperty("line.separator");
            String eol = "\n";
            while ((str = in.readLine()) != null) {
                result = result.append(str).append(eol);
            }
            in.close();

            return result.toString();
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * Reading a textfile. Identify and remove BOM-marks
     * Convert encoding if necessary
     * @param uri The URI we want to read
     * @param encoding The encoding we want
     * @return The found, encoded, string
     * @throws java.lang.Exception
     */
    public static String getBOMSafeTextFile(URI uri, String encoding)
            throws Exception {
        // modified from:http://koti.mbnet.fi/akini/java/java_utf8_xml/
        // unicode reader from:http://koti.mbnet.fi/akini/java/unicodereader/UnicodeReader.java.txt
        // http://koti.mbnet.fi/akini/java/unicodereader/
        String s = System.getProperty("file.encoding");
        //System.setProperty("file.encoding", "UTF8");

        BufferedReader reader = null;
        CharArrayWriter writer = null;
        URL url = uri.toURL();
        UnicodeReader ur = new UnicodeReader(url.openStream(), encoding);
        char[] buffer = new char[16 * 1024];   // 16k buffer
        int read;
        try {
            reader = new BufferedReader(ur);
            writer = new CharArrayWriter();

            while ((read = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, read);
            }
            writer.flush();

            // convert
            String foundEncoding = ur.getEncoding();
            String defaultEncoding = ur.getDefaultEncoding();
            if (defaultEncoding != null
                    && foundEncoding != null
                    && !encoderutils.sameEncoding(defaultEncoding, foundEncoding)) {
                String in = writer.toString();
                return encoderutils.reEncode(in, foundEncoding, defaultEncoding, false);
            } else {
                return writer.toString();
            }
        } catch (IOException ex) {
            throw ex;
        } finally {
            try {
                writer.close();
                reader.close();
                ur.close();
            } catch (Exception ex) {
            }
        }

    }

    /**
     * Produce a list of files with a certain suffix from within a catalog.
     * 
     * @param sourcecat The catalog we will investigate
     * @param filesuf The suffix of the files we are interested in
     * @return a list of filenames
     */
    public static List<String> getFileList(String sourcecat, String filesuf) {
        List<String> vs = new ArrayList<String>();
        try {
            File f = new File(sourcecat);
            String[] tmp = f.list();
            for (int ix = 0; ix < tmp.length; ix++) {
                if (tmp[ix].endsWith("." + filesuf)) {
                    vs.add(tmp[ix]);
                }
            }
            return vs;
        } catch (Exception e) {
            return vs;
        }
    }

    /**
     * Produce a list of sorted files with a certain suffix from within a catalog.
     *
     * @param sourcecat The catalog we will investigate
     * @param filesuf The suffix of the files we are interested in
     * @return a list of filenames
     */
    public static List<String> getSortedFileList(String sourcecat, String filesuf) {
        List<String> list = getFileList(sourcecat, filesuf);
        if (list.size() > 1) {
            Collections.sort(list);
        }
        return list;
    }

    /**
     * Changing & to entity &amp&; in string
     * @param S The string we will work on
     * @return The fixed string
     */
    public static String fixUriParameterEntities(String S) {
        int pos = S.indexOf('&');
        while (pos != -1) {
            if (!S.substring(pos + 1).startsWith("amp;")) {
                S = S.substring(0, pos + 1) + "amp;" + S.substring(pos + 1);
            }
            pos = S.indexOf("&", pos + 1);
        }
        return S;
    }

    /**
     * Changing & to entity &amp; in URI
     * @param theUri The URI we will work on
     * @return The fixed URI
     */
    public static URI fixUriParameterEntities(URI theUri) {
        String theUriS = theUri.toString();
        String fixed = fixUriParameterEntities(theUriS);
        try {
            return new URI(fixed);
        } catch (Exception ex) {
            return theUri;
        }
    }

    /**
     * testing if the resource described by an URI exists
     * @param absUri The address we will test
     * @return true if we can open a steam, otherwise false
     */
    public static boolean resourceExists(URI absUri) {
        try {
            // stream or connection ?? see case in producer:makePI_ReferenceTest
            InputStream s = absUri.toURL().openStream();
            s.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Make entities for all std entities
     * @param S The string we will work on
     * @return The result
     */
    public static String setCommonEntities(String S) {
        int p1 = S.indexOf("&");
        while (p1 > -1) {
            if ((!S.substring(p1).startsWith("&amp;"))
                    && (!S.substring(p1).startsWith("&lt;"))
                    && (!S.substring(p1).startsWith("&gt;"))
                    && (!S.substring(p1).startsWith("&apos;"))
                    && (!S.substring(p1).startsWith("&quote;"))) {
                S = S.substring(0, p1) + "&amp;" + S.substring(p1 + 1);
            }
            p1 = S.indexOf("&", p1 + 1);
        }
        S = S.replaceAll("<", "&lt;");
        S = S.replaceAll(">", "&gt;");
        S = S.replaceAll("'", "&apos;");
        S = S.replaceAll("\"", "&quot;");
        return S;
    }

    /**
     * Recursively removing a directory
     * @param dir The directory to remove
     * @return true if we succed, otherwise false
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                File nF = new File(dir, children[i]);
                boolean success = deleteDir(nF);
                if (!success) {
                    return false;
                }
            }
        }
        // The directory is now empty so delete it
        return dir.delete();
    }

    /**
     * Cleans a string from everything but letters and digits.
     * Others are replaced by _
     * Removes Schema, in case it is an uri
     * @param S The string to clean, may be a simple string or an URI
     * @return The cleaned String
     */
    public static String cleanStringForUseAsFilePath(String S) {
        S = S.replace("http://", "");
        S = S.replace("file://", "");

        StringBuilder result = new StringBuilder(50);
        for (int ix = 0; ix < S.length(); ix++) {
            char c = S.charAt(ix);
            if ((!Character.isDigit(c))
                    && (!Character.isLetter(c))
                    && (c != '.')) {
                result.append('_');
            } else {
                result.append(c);
            }
        }
        String s = result.toString();
        s = s.replace('.', '_');
        return s;
    }

    /**
     * Clean a filepath
     * @param S The path
     * @return The cleaned string
     */
    public static String cleanFilePath(String S) {
        // take care of extension
        int pos = S.lastIndexOf('.');
        if (pos == -1) {
            return cleanStringForUseAsFilePath(S);
        }
        if (S.substring(pos).length() > 4) {
            return cleanStringForUseAsFilePath(S);
        }

        String res = cleanStringForUseAsFilePath(S.substring(0, pos)) + S.substring(pos);
        return res;
    }

    /**
     * Copy an image from an URI to a local file
     * @param from The address to get the image
     * @param to The address to store the image
     * @return true if we succed, false otherwise
     */
    public static boolean copyImageFromURI(String from, String to) {
        try {
            File f = new File(to);
            if (!f.exists()) {
                String formatname = "png";
                int pos = from.lastIndexOf('.');
                // found and within reasonable size
                if ((pos != -1) && (from.length() - pos < 5)) {
                    formatname = from.substring(pos + 1);
                }
                URL url = new URL(from);
                String contentType = null;
                contentType = getUrlHeaderContentType(url);
                if ((contentType == null) || (contentType.indexOf("image") == -1)) {
                    throw new Exception("not an image");
                }
                BufferedImage bi = ImageIO.read(url);
                f.mkdirs();
                ImageIO.write(bi, formatname, new File(to));
            }
            return true;
        } catch (Exception ex) {
            String tmp = "Image copy from: " + from + " to: " + to + " because: " + ex.getMessage();

            System.out.println(tmp);
            return false;
        }
    }

    /**
     *  get contenttype from an urlconnection
     * @param url
     * @return
     */
    private static String getUrlHeaderContentType(URL url) {
        try {
            URLConnection conn = url.openConnection();
            return conn.getContentType();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Unzips an OpenOffice document to a folder.
     * @param sourceUri Address of the ODT-file
     * @param outputDirectory Path to folder in witch to save the extracted files.
     * @return The filecatalog where we put the unzipped material, null on failure
     */
    public static File UnzipODT(URI sourceUri, String outputDirectory) {
        ZipInputStream in;
        OutputStream out = null;
        File result = null;

        try {
            //Creates the folder
            result = new File(outputDirectory);
            result.mkdirs();
            if (sourceUri.getScheme().startsWith("file")) {
                in = new ZipInputStream(new FileInputStream(sourceUri.getPath()));
            } else {
                // we have to deal with an absolute http source
                // download it
                URLConnection con = sourceUri.toURL().openConnection();
                in = new ZipInputStream(con.getInputStream());
            }

            ZipEntry entry = in.getNextEntry();

            // Gets directories(subdirectories)/files
            while (entry != null) {
                String outFilename = outputDirectory + File.separatorChar + String.valueOf(entry.getName());
                outFilename = outFilename.replace('\\', '/');
                try {
                    File tempFile = new File(outFilename);
                    // can do delete on exit if we want to
                    //mark some files for delete on exit
                    //tempFile.deleteOnExit();

                    tempFile = tempFile.getParentFile();
                    tempFile.mkdirs();

                    //System.out.println(outFilename);

                    out = new FileOutputStream(outFilename);
                } catch (FileNotFoundException fnfe) {
                    System.err.println(fnfe.getLocalizedMessage());
                }

                new File(outFilename).mkdirs();

                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                entry = in.getNextEntry();
                out.close();

            }
            in.close();
            try {
                //URI tmpUri=new URI("file:///"+result.getPath().replace('\\', '/')+"/readme.txt");
                URI tmpUri = makeUri("file:///" + result.getPath() + "/readme.txt");
                String T = "This directory is used (and reused) for storing unwrapped ODT-stuff. \n"
                        + "WXT will produce a catalog for each ODT-file that is used as acontentprovider\n"
                        + "The structure of the file is found in content.xml\n"
                        + "Pictures are referenced directly from the built Module and should not be removed";
                accessutils.saveTFile(tmpUri, T, "utf-8");
            } catch (Exception uex) {
                //should not happen
                System.out.println(uex.getMessage());
            }
            return result;
        } catch (IOException e) {
            System.out.print(e);
            //e.printStackTrace();
            System.out.print("Could not unzip file.");
            return null;
        }
    }

    /**
     * Pick one of two paths that seems to match the current op sys
     * Using two clues :
     * 1 : as second char to identfy windows abs path
     * 2  \ or / existance
     * @param val1 path1
     * @param val2 path1
     * @return The selected path, prepared for copy into an URI-string
     */
    public static String selectPathValue(String val1, String val2) {
        // get current os
        String os = System.getProperty("os.name").toUpperCase();
        String selected = null;
        if ((val1 == null) && (val2 == null)) {
            return "";
        }
        if (val2 == null) {
            selected = val1;
        } else if (val1 == null) {
            selected = val2;
        } else {
            // neither is null
            // and we must find which of the two that match the op sys
            val1 = val1.trim();
            val2 = val2.trim();
            if (os.indexOf("WINDOWS") != -1) {
                // first we go for an absolute path
                if (val1.indexOf(':') == 1) {
                    // This is obviously a Windows abs path
                    selected = val1;
                } else if (val2.indexOf(':') == 1) {
                    // This is obviously a Windows abs path
                    selected = val2;
                } else if (val1.indexOf('\\') != -1) {
                    selected = val1;
                } else if (val2.indexOf('\\') != -1) {
                    selected = val2;
                } else {
                    // we have no clue, may be it is just a name
                    selected = val1;
                }
            } else {
                // any thing but windows needs a unix path
                if (val1.startsWith("/")) {
                    selected = val1;
                } else if (val2.startsWith("/")) {
                    selected = val2;
                } else if (val1.indexOf('/') != -1) {
                    selected = val1;
                } else if (val2.indexOf('/') != -1) {
                    selected = val2;
                } else {
                    // we dont have a clue
                    selected = val1;
                }
            }
        }
        return selected;
    }

    /**
     * Find out if one string in a csv is contained in another csv
     * @param s1 One csv string
     * @param s2 One csv string
     * @return True only if a value in s1 is contained in s2, or vica versa
     */
    public static boolean compareCSV(String s1, String s2) {
        if ((s1 == null) || (s1.length() == 0)) {
            return false;
        }
        if ((s2 == null) || (s2.length() == 0)) {
            return false;
        }
        s2 = "," + s2 + ",";
        String[] list1 = s1.split(",");
        for (int ix = 0; ix < list1.length; ix++) {
            String sx = "," + list1[ix] + ",";
            if (s2.indexOf(sx) != -1) {
                return true;
            }
        }
        return false;
    }
}

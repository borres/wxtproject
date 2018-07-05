package utils;

import java.nio.charset.Charset;

/**
 * Checking and changing encoding as needed.
 * <p>
 * Very temporary,
 */
public class encoderutils {

    static final String NOT_FOUND = "not found";
    static public final String UTF8_BOM_MARK = "ï»¿";

    /**
     * Constructor
     */
    public encoderutils() {
    }

    /**
     * Look for encoding information in a string.
     * <p>
     * Only look for utf8 BOM.
     * 
     * @param T The string to investigate
     * @return utf-8 if known BOM, otherwise NOT_FOUND
     */
    private static String parseTextEncoding(String T) {
        // look for BOM-marking
        // EF BB BF : ï»¿ : utf-8       
        if (T == null) {
            return NOT_FOUND;
        }
        if (T.trim().startsWith(UTF8_BOM_MARK)) {
            return "UTF-8";
        }

        return NOT_FOUND;
    }

    /**
     * Remove BOM mark from a string
     * @param s The string
     * @return The cleaned string
     */
    public static String removeBOM(String s) {
        // 0xEF,0xBB,0xBF
        if ((s == null) || (s.length() < 4)) {
            return s;
        }
        try {
            byte[] bom = s.getBytes("UTF-8");

            if (bom.length < 4) {
                return s;
            }

            if ((bom[0] == (byte) 0xEF) && (bom[1] == (byte) 0xBB)
                    && (bom[2] == (byte) 0xBF)) {
                String t = new String(bom, 3, bom.length - 3, "UTF-8");
                return t;
            } else {
                return s;
            }
        } catch (Exception ex) {
            return s;
        }

    }

    /**
     * Try to pick up the encoding from an XML-string.
     * 
     * @param T The string to investigate
     * @return The found encoding, or NOT_FOUND
     */
    private static String getTagEncoding(String T) {
        int pos = 0;
        int pos2 = 0;
        String s = "encoding=\"";
        pos = T.indexOf(s);
        if (pos != -1) {
            pos += s.length();
            pos2 = T.indexOf("\"", pos);
            if (pos2 != -1) {
                return T.substring(pos, pos2);
            }
        }
        return NOT_FOUND;
        // alternative is to parse the string, build doc and access
        // doc.getXmlEncoding()
        // safer but costs more
    }

    /**
     * Set or remove the BOM from an utf-8 string.
     * 
     * @param T The string to investigate
     * @param mark mark or unmark the string
     * @return The fixed string
     */
    public static String setBOMUTF8(String T, boolean mark) {
        if (mark && (!T.startsWith(UTF8_BOM_MARK))) {
            T = UTF8_BOM_MARK + T;
            return T;
        }

        if ((!mark) && T.startsWith(UTF8_BOM_MARK)) {
            T = T.substring(UTF8_BOM_MARK.length());
        }

        return T;
    }

    /**
     * Compare two encodings.
     * 
     * @param enc1 Encoding 1
     * @param enc2 Encoding 2
     * @return true if same encoding, false otherwise
     */
    public static boolean sameEncoding(String enc1, String enc2) {
        // if either is null there is nothing we can do
        // but assume they are the same
        if ((enc1 == null) || (enc2 == null)) {
            return true;
        }

        if (enc1.compareToIgnoreCase(enc2) == 0) {
            return true;
        }
        // TODO any encodings which is same, but has different names
        return false;
    }

    /**
     * Re encode a string.
     * 
     * @param T The string to re encode
     * @param fromEnc Current encoding
     * @param toEnc Wanted encoding
     * @param useBOM Do we want to include BOM-mark (utf-8)
     * @return The reencoded string if we succed, otherwise the original string
     */
    public static String reEncode(String T, String fromEnc, String toEnc, boolean useBOM) {
        // we dont try to re encode if any of the codesets does not exist
        String tmp = getLegalEncoding(fromEnc);
        if (tmp != null) {
            fromEnc = tmp;
        } else {
            return T;
        }

        tmp = getLegalEncoding(toEnc);
        if (tmp != null) {
            toEnc = tmp;
        } else {
            return T;
        }

        // --------------
        // ok we can do it, from http://www.jorendorff.com/articles/unicode/java.html
        if (useBOM && (toEnc.compareToIgnoreCase("utf-8") == 0)) {
            T = setBOMUTF8(T, true);
        }

        if (toEnc.compareToIgnoreCase("utf-8") != 0) {
            T = setBOMUTF8(T, false);
        }

        try {
            // First, decode the data using the source encoding.
            byte[] data = T.getBytes();
            String str = new String(data, fromEnc);

            // Next, encode the data using the target encoding.
            byte[] resultbytes = str.getBytes(toEnc);
            //String result=new String(resultbytes);
            String result = new String(resultbytes, toEnc);

            return result;
        } catch (Exception ex) {
            return T;
        }

    }

    /**
     * Check if an encoding is known
     * @param ec Suggested encoding
     * @return the match for the key ec if it exists, otherwise null
     */
    public static String getLegalEncoding(String ec) {
        java.util.SortedMap<String, Charset> cm = java.nio.charset.Charset.availableCharsets();
        if (cm.containsKey(ec.toLowerCase())) {
            return ec.toLowerCase();
        }
        if (cm.containsKey(ec.toUpperCase())) {
            return ec.toUpperCase();
        }
        return null;

    }
}

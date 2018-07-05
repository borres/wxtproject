package converter;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 *
 * from:
 * http://stackoverflow.com/questions/469695/decode-base64-data-in-java
 * Handling images from base64 strings
 * 
 * @author bs
 */
public class Base64ToImage {
    //@todo: Handle imageformats ?

    private static final char[] ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
    private static int[] toInt = new int[128];

    static {
        for (int i = 0; i < ALPHABET.length; i++) {
            toInt[ALPHABET[i]] = i;
        }
    }

    /**
     * Translates the specified Base64 string into a buffered image.
     *
     * @param base64Str the Base64 string (not null)
     * @return the buffered image, 
     * @throws Exception if we fail
     */
    public static BufferedImage getImageFromString(String base64Str)
            throws Exception {
        int delta = base64Str.endsWith("==") ? 2 : base64Str.endsWith("=") ? 1 : 0;
        byte[] buffer = new byte[base64Str.length() * 3 / 4 - delta];
        int mask = 0xFF;
        int index = 0;
        InputStream in = null;
        for (int i = 0; i < base64Str.length(); i += 4) {
            int c0 = toInt[base64Str.charAt(i)];
            int c1 = toInt[base64Str.charAt(i + 1)];
            buffer[index++] = (byte) (((c0 << 2) | (c1 >> 4)) & mask);
            if (index >= buffer.length) {
                return ImageIO.read(new ByteArrayInputStream(buffer));
                // or throw exception ?
            }
            int c2 = toInt[base64Str.charAt(i + 2)];
            buffer[index++] = (byte) (((c1 << 4) | (c2 >> 2)) & mask);
            if (index >= buffer.length) {
                return ImageIO.read(new ByteArrayInputStream(buffer));
                // or throw exception ?
            }
            int c3 = toInt[base64Str.charAt(i + 3)];
            buffer[index++] = (byte) (((c2 << 6) | c3) & mask);
        }

        return ImageIO.read(new ByteArrayInputStream(buffer));

    }
    /**
     * NOT USED
     * Translates the specified byte array into Base64 string.
     *
     * @param buf the byte array (not null)
     * @return the translated Base64 string (not null)
     */
    /*
    public static String encode(byte[] buf){
    int size = buf.length;
    char[] ar = new char[((size + 2) / 3) * 4];
    int a = 0;
    int i=0;
    while(i < size){
    byte b0 = buf[i++];
    byte b1 = (i < size) ? buf[i++] : 0;
    byte b2 = (i < size) ? buf[i++] : 0;
    
    int mask = 0x3F;
    ar[a++] = ALPHABET[(b0 >> 2) & mask];
    ar[a++] = ALPHABET[((b0 << 4) | ((b1 & 0xFF) >> 4)) & mask];
    ar[a++] = ALPHABET[((b1 << 2) | ((b2 & 0xFF) >> 6)) & mask];
    ar[a++] = ALPHABET[b2 & mask];
    }
    switch(size % 3){
    case 1: ar[--a]  = '=';
    case 2: ar[--a]  = '=';
    }
    return new String(ar);
    }
    
    
     */
}

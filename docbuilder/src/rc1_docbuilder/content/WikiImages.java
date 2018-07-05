
package rc1_docbuilder.content;

import com.sun.org.apache.xpath.internal.XPathAPI;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.imageio.ImageIO;
import org.w3c.dom.*;

/**
 * Class to manage image sources in documents.
 * @author silje
 */
public class WikiImages {

    private static String fileSeparator = File.separator; // System path sepperator
    private static String documents = "Documents";        // Name of folder to save documents
    private static String html_documents = "html_documents";  // Name of folder to save generated html documents
    private static String imgFolder = "img";                  // Name of folder to save images
    private static String folder = documents + fileSeparator + html_documents + fileSeparator + imgFolder + fileSeparator; // Folder path to save images

    /**
     * Saves images from the document to a local file on disk.
     * The image source is then changed to point to the new location.
     * @param docf DocumentFragment to process.
     * @param doc The parrent document.
     * @param url URL to original source of Wiki page or odt document.
     * @return A new document fragment with local image paths.
     */
    public static DocumentFragment downloadImages(DocumentFragment docf, Document doc, String url) {

        String xpath = "//img"; // Select all img tags

        Element element = null;   // Element to create new node

        if (docf.hasChildNodes()) {

            try {
                NodeList nodeList = XPathAPI.selectNodeList(docf, xpath);  // Make node list of all image tags

                for (int i = 0; i < nodeList.getLength(); i++) {

                    element = (Element) nodeList.item(i);

                    if (element.hasAttribute("src")) {    // Test for src attribute

                        String src = element.getAttributes().getNamedItem("src").getTextContent();    // get old document path

                        // Skipping the "magnify glass" image on wiki pages.
                        if (getImageName(src).equalsIgnoreCase("magnify-clip")) {
                            /* The images are sourounded by a link tag so we have to remove this too. */
                            Node node_a = element.getParentNode(); // Find node a
                            Node node_div = node_a.getParentNode(); // Find div

                            node_a.removeChild(element); // Remove img tag
                            node_div.removeChild(node_a); // Remove the a tag

                        } else {
                            src = getFullImageSrc(url, src); // Calculates the full path of the image.
                            String newPath = saveImage(src, folder); // Saves the image to image folder. Returns this path.

                            element.setAttribute("src", newPath); // update src attribute with the new path

                            Node anode = doc.createTextNode(element.getTextContent());    // Creates a new node to be inserted in the Document Fragment

                            docf.appendChild(doc.adoptNode(anode)); // Inserts the node

                        } // end if else
                    } // end if
                } // end for
            } catch (Exception ex) {
                System.out.println("");
                ex.printStackTrace();
            } // end try catch
        } // end if
        return docf;
    } // end downloadImages

    /**
     * Saves an image located on the url to given folder.
     * @param url Path to image file
     * @param folder Path to folder where the images will be saved.
     * @return New path to where the image are stored.
     * @throws java.io.IOException
     */
    private static String saveImage(String url, String folder) throws IOException {

        BufferedImage image = getImage(url); // Retrives an image from a path
        if (image == null) {
            return "ImageNotFound"; // Fjerne bilder som ikke blir hentet ut?
        } else {

            String fileType = getFileExtension(url);  // Find image filetype
            String filename = getImageName(url);      //  Resolve base name of the image
            String path = folder + filename + "." + fileType;   // Construct path and name to where the image will be saved

            if (!new File(folder).exists()) {    // Check if image folder exists
                new File(folder).mkdir();
            }

            File imageFile = new File(path);  // Create file
            FileOutputStream fout = new FileOutputStream(imageFile);
            fout.close();
            ImageIO.write(image, fileType, imageFile);  // Write image to file

            String result = imgFolder + fileSeparator + filename + "." + fileType; // Constructs the new path were the image exists
            return result;  // Return the path
        } // end if else
    } // end saveImage

    /**
     * Retrives an image from a path and returns it as a BufferedImage objeck.
     * @param path Path where the image is located.
     * @return A new BufferedImage object
     */
    private static BufferedImage getImage(String path) {

        BufferedImage image = null;
        try {
            // Read from a file
            File sourceimage = new File(path);

            // Tries to read the file as a local file first
            if (sourceimage.canRead()) {
                image = ImageIO.read(sourceimage);   // Read from file
            } // If it could not be read locally, it tries to read it from an URL
            else {
                URL url = new URL(path);
                image = ImageIO.read(url);     // Read from a URL
            }

        } catch (IOException e) {
            System.out.println("Could not read image from " + path);
        } // end try catch

        return image;
    } // end getImage

    /**
     * Resolve an image name from a path
     * @param url Path to image file
     * @return Base name of image without format extension.
     */
    private static String getImageName(String url) {
        url = url.replaceAll("\\\\", "/");
        String[] s = url.split("\\.");
        String type = s[s.length - 2];
        s = type.split("/");
        String fileName = s[s.length - 1];
        return fileName;
    }

    /**
     * Get the file extension from a file name or path.
     * @param file Path to file or just the file name
     * @return The extension of the file.
     */
    private static String getFileExtension(String file) {
        String[] s = file.split("\\.");
        String type = s[s.length - 1];
        return type;
    }

    /**
     * If the image url lacks a scheme a full path is constructed by combining the host source with the image source.
     * @param documentPath Path of original document.
     * @param imageSrc Value of an img src attribute.
     * @return Full path to an image.
     */
    private static String getFullImageSrc(String documentPath, String imageSrc) {

        URI pathUri = null;
        try {
            documentPath = documentPath.replaceAll(" ", "%20");
            pathUri = new URI(documentPath);

        } catch (URISyntaxException ex) {
            System.out.println(ex.getMessage());
        } // end try catch

        if (imageSrc.startsWith("http://")) { // Test if it has a http scheme
            return imageSrc;
        } else if (imageSrc.startsWith("/")) {
            return "http://" + pathUri.getHost() + imageSrc;
        } else {
            String result = System.getProperty("user.dir") + System.getProperty("file.separator") + pathUri.resolve(imageSrc).toString();
            result = result.replaceAll("/", "\\\\");
            return result;
        }
    } // end getFullImageSrc

    /**
     * Switch image source in a document to full path. Use when converting to pdf with Prince to avoid
     * "File not found" errors. Prince can only read full paths if the document is stored locally.
     * @param doc A html document
     * @return The same document with full pathnames for image sources.
     */
    public static Document switchToFullPathNames(Document doc) {

        String xpath = "//img";
        Element element = null;

        if (doc.hasChildNodes()) {

            try {
                NodeList nodeList = XPathAPI.selectNodeList(doc, xpath);

                for (int i = 0; i < nodeList.getLength(); i++) {

                    element = (Element) nodeList.item(i);
                    if (element.hasAttribute("src")) {

                        String src = element.getAttributes().getNamedItem("src").getTextContent();    // get old documentPath
                        if (!(src.startsWith("http:") || src.startsWith("/") || src.startsWith("file:"))) { // Check if it needs to be switched

                            // Constructs the new path
                            String newPath =
                                    System.getProperty("user.dir") + fileSeparator + documents + fileSeparator + html_documents + fileSeparator + src;

                            newPath = newPath.replaceAll("\\\\", "/").replaceAll(" ", "%20");
                            URL url = new URL("file", "/", newPath); // Creates an URL with file scheme

                            element.setAttribute("src", url.toString());   // Updates documentPath

                            Node anode = element.cloneNode(true);            // Create a new Node from the element

                            element.getParentNode().replaceChild(doc.adoptNode(anode), nodeList.item(i));   // Replaces the old node with the updatet one.
                        } // end if

                    } // end if
                } // end for
            } catch (Exception ex) {
                System.out.println("Replace path names failed. \n" + ex.getMessage());
                ex.printStackTrace();
            } // end try catch

        } // end if
        return doc;
    } // end switchToFullPathNames
}


package rc1_docbuilder.content;

import java.io.*;
import java.util.zip.*;

/**
 * Class to generate odt files to html
 * @author silje
 */
public class odtbuilder {

    private int folderHash;
    private String stylesheetXslt = "Documents/xsltfile.xslt"; // The XSLT file used in the transformation.
    private String outFolder; // The odt files ar eunzipped here.
    private String contentXml;   // the content.xml file local path
    private String outputHtml;  // name of the html output file locl path

    public odtbuilder() {
    }

    /**
     * Transforms an odt document to a  html file with Wiki spesific ellements.
     * It unzipz the document and transforms the content.xml. The unzipped files and teh generated file
     * are stored in Documents/tmp.
     *
     * @param url The path to the OpenOffice document.
     * @return String with pathname to the generated html file.
     */
    public String transformToWiki(String url) {
    
        folderHash = url.hashCode();  // Create uniqe folder name

        outFolder = "Documents/tmp/" + folderHash; // unzip path
        contentXml = outFolder + "/content.xml";           // path to content.xml
        outputHtml = outFolder + "/wiki_content.html";  // path to generated content


         UnzipODT(url, outFolder); // Unzip odt document

        transformXml(contentXml, stylesheetXslt, outputHtml); // Does the XSLT transformation. This creates the file wiki_content.html

        return outputHtml;      // Returns the path to a transfomed html document.
    }

    /**
     * Unzips an OpenOffice document to a folder.
     * @param inFilename Path to odt file.
     * @param outputDirectory Path to folder in witch to save the files.
     */
    private void UnzipODT(String inFilename, String outputDirectory) {
        try {
            //Creates the folder
            (new File(outputDirectory)).mkdirs();

            ZipInputStream in = new ZipInputStream(new FileInputStream(
                    inFilename));

            ZipEntry entry = in.getNextEntry();
            OutputStream out = null;

            // Gets directories(subdirectories)/files
            while (entry != null) {
                String outFilename = outputDirectory + File.separatorChar + String.valueOf(entry.getName());
                try {
                    File tempFile = new File(outFilename);
                    tempFile = tempFile.getParentFile();
                    tempFile.mkdirs();
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
            }

            out.close();
            in.close();
        } catch (IOException e) {
            System.out.print(e);
            e.printStackTrace();
            System.out.print("Could not unzip file.");

        }
    } // end UnzipODT


    /**
     * This method tranforms a XML file with the given XSLT file. It is stores in the spesified output
     * @param xmlFilePath The file to transform
     * @param xsltFilePath The XSLT file to use.
     * @param outputPath Filename and path to store the result
     */
    private void transformXml(String xmlFilePath, String xsltFilePath, String outputPath) {
        try {
            // Creates new File objects
            File xmlFile = new File(xmlFilePath);   // the xml file

            File xsltFile = new File(xsltFilePath); // the xslt file

            File resultFile = new File(outputPath); // the output file

            // Creates Stream Sources
            javax.xml.transform.Source xmlSource = new javax.xml.transform.stream.StreamSource(xmlFile);    // the xml file
            javax.xml.transform.Source xsltSource = new javax.xml.transform.stream.StreamSource(xsltFile);  // the xslt file
            javax.xml.transform.Result result = new javax.xml.transform.stream.StreamResult(resultFile);     // the output file

            // create an instance of TransformerFactory
            javax.xml.transform.TransformerFactory transFact =
                    javax.xml.transform.TransformerFactory.newInstance();

            // Creates a transformer from the XSLT file to do the transformation
            javax.xml.transform.Transformer trans =
                    transFact.newTransformer(xsltSource);

            // Use the transformer on the xml file. Store result in variable result.
            trans.transform(xmlSource, result);

        } catch (javax.xml.transform.TransformerException e) {
            System.out.println("Error in XSLT transformation.");
            e.printStackTrace();
        } // end try catch

    } // end transformXml
}

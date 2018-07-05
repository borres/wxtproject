

package rc1_docbuilder.content;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import rc1_docbuilder.utils.accessutils;
import rc1_docbuilder.utils.domer;
/**
 * This class is used to parse WIKI pages and return dataFromPI from the site.
 * The class has a sett of funtions used to rerieve dataFromPI from WIKIPAGEs
 * @author Ali Yusuf Hussein & Enis Dunic
 */
public class Wikibuilder
{

   //A static string containing a pattern in all WIKI webpages
    private final static String HEADLINE="mw-headline";
    //A static string containing a pattern in all WIKI webpages
    private final static String h2Wikitag = "h2";
    //This global string contains the URI adress of the wikipage
    private static String URISOURCE=null;
   
     

    public Wikibuilder()
    {
          
          
    }

    /**
     * Headlines in a Wiki page are packed into <h2> to <h6> based on the size of the headlines.
     * A wiki headline can look like this : <h2><span class="mw-headline">Description</span></h2>.
     * <h*> can contain more than one <span> tag.If <h*> has a <span> childnode with attribute class="mw-headline"
     * then this method will retrieve the <span> tag.
     * This private function is used to retrieve <span> tags that are spantaglist of <h2>-<h6> tags.
     *
     * @param node a Node that reperesents <h*> tagg.
     * @return A NodeList of <span> tags
     * @throws java.lang.Exception
     * 
     */



    private static NodeList getChildren(Node node) throws Exception
    {
        //create a NodeList that will contain <span> tags
        NodeList spantaglist = null;

        try
        {
            /*All Wiki headlines must have spantaglist. Continue if <h*> has spantaglist*/
            if(node.hasChildNodes())
            {
                //Add the spantaglist of <h*> tag to the list
                spantaglist = node.getChildNodes();
            }

        }

        catch(Exception e)
            {
                throw new Exception("Error returning childnodes of <h*> tag: "+e.getMessage());
            }



        return spantaglist;
    }

    /**
     * This function returns headline text of a wiki headline. The headline text is
     * in a childnode <span> tag of <h*> tag. the <span> tag has pattern equal to:<span class="mw-headline">Headline</span>.
     * This function looks for a <span> tag with attribute class="mw-headline". if the <span> tag exist, the headLine fo the tag will
     * be returned.
     * @param nodelist A list of <span> tags.
     * @return String that contains headLine of a <span> tagg that has the attribute class="mw-headline".
     * @throws java.lang.Exception
     * 
     */
   private static String getHeader(NodeList nodelist) throws Exception
    {
       //Create a element node that we will use to loop through th attributes
        Element attribute=null;
        //The string that will contain the headline
        String headLine=null;
        try
        {
             /*Loop through the list of <span> tags*/
                    for(int ix=0; ix<nodelist.getLength(); ix++)
                    {
                        //Checks if the <span> tag has attributes
                        if(nodelist.item(ix).hasAttributes())
                        {
                            //Retrieve the here the <span> tag
                            attribute = (Element)nodelist.item(ix);
                            /*Check if the <span> tag has a attribute class="mw-headline*/
                            if(attribute.getAttribute("class").equals(HEADLINE))
                             {
                                /*Retrieve the textcontent of the <span> tag*/
                                headLine =attribute.getTextContent();
                                /*For debuging:
                                System.out.println("Testing from Wikibuilder.getHeaders "+attribute.getTextContent());
                                */
                              }
                         }
                    }
        }
        catch(Exception e)
        {
            throw new Exception("Eror retrieving wiki headline "+e.getMessage());
        }

        return headLine;
    }

/**
 *A simple function used to return a list of all the headlines in a wiki document
 * @param source A dom object representing the wikipage
 * @param tagname A specific tagname of the wikipage. only folowing tags are allowed:
 * <h2>,<h3>,<h4>,<h5>,<h6>
 * @return ArrayList containing the headers
 * @throws java.lang.Exception
 */

    public static ArrayList getWikiHeadLines(Document source,String tagname) throws Exception
	{
        /*Initialize the ArrayList fo string*/
        ArrayList<String> headlines = new ArrayList<String>();

		try
		{   /*Create a list of nodes that contains <h*> tags*/
			NodeList list = source.getElementsByTagName(tagname);
            /*Create a list of nodes that represents <span> tags*/
			NodeList spantaglist = null;
            /*Start looping through <h*> elements*/
			for(int ix=0; ix<list.getLength(); ix++)
				{
                    /*retrieve a list of <span> tags from each <h*>tag*/
					spantaglist = getChildren(list.item(ix));
                            /*Continue only if there are some headlines */
                            if(getHeader(spantaglist)!=null)
                            {
                                /*Add the headline to the ArrayList*/
                                headlines.add(getHeader(spantaglist));
                            }
				}
		}


		catch(Exception e)
		{
			throw new Exception("Error creating list of headlines: "+e.getMessage(),e.getCause());
            
		}

		return headlines;


	 }

    /**
     * This function takes the url of the page as uri and returns a cleaned DOM object of the site
     * @param uri The URI of the WIKI page
     * @return
     * @throws java.lang.Exception
     * @throws org.w3c.dom.DOMException
     */
    public static Document getWikiPage(URI uri) throws Exception
    {
        Document doc;
        try
        {
            doc= domer.makeDomFromUri(uri);
            //Lett other methods access uri adress
            URISOURCE = uri.toString(); //Append the full wikiURI to global variable
        }
        catch(Exception e)
        {
            throw new Exception("Could not get the Wepage "+uri.toString());
        }


       
        return doc;

    }
/**
 * A function used to read from local
 * @param file local file
 * @param encoding the encoding of the document
 * @return A string reprensenting the file
 * @throws java.lang.Exception
 * @throws java.io.IOException
 */
    public static  String readFile(File file,String encoding) throws IOException, Exception
        {
            String data="";

            if(file.exists()==false)
            {
                throw new FileNotFoundException("The file: "+file.getName()+ " does not exists");
            }
            if(file.canRead()==false)
            {
                throw new IOException("The file cannot be read");
            }
                FileInputStream fileinput=new FileInputStream(file);
                InputStreamReader ir=new InputStreamReader(fileinput,encoding);
                BufferedReader br=new BufferedReader(ir);
                String object= "";
                while((object=br.readLine())!=null)
                {
                    data+= object;
                }
                br.close();

            
            if(data.isEmpty()==true)
            {
                throw new Exception("File is empty");
            }
            return data;


        }
    
    /**
     * A simple function used to write DOM object to local.
     * @param doc The document the will be saved to local
     * @param savefile The locaton where the document should be saved
     * @throws java.lang.Exception
     */
     public static void saveDOM(Document doc, File savefile) throws Exception
    {
        try
        {
            //creata a instance of DOMsource
            DOMSource xmlsourse = new DOMSource(doc);
            //create a instance of StreamResult
            StreamResult result = new StreamResult(new FileOutputStream(savefile));
            //create a instance of transformer
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            //Transform the document and save
            transformer.transform(xmlsourse, result);
        }
        catch(Exception e)
        {
            throw new Exception("Could not save document "+e.getMessage(),e.getCause());
        }
    }
     /**
      * This function replaces PI tags in the master document with a documentfragment from a wiki page.It also performs some filtering process
      * to make sure that the document is clean.
      * The Pi tags has this form: <?Docbuilder type::heading::source::option one::option two?>
      * Based on the type of the PI tags this method replaces the PI tag with the documetnfragment the PI is refrering to.
      * @param master The master document the user is working on
      * @param PItarget The target of the PI tags
      * @throws java.lang.Exception
      */

     public static void generateDocument(Document master, String PItarget) throws Exception
    {
         try
         {
                 /*Create Vector that will contain all PI tags in the master document*/
                 Vector<Node> PIS = domer.getPIs(master,PItarget);
                 /*Create A instace of ProcessingInstriction that will contain the PI tags in the master document*/
                 ProcessingInstruction instruction=null;
                 //This string will contain the data of each PI tag
                 String PIdata=null;
                 //This string is used to split the data from PI tags
                 String [] dataFromPI;
                 //This string is used to retrieve the linkoption from the PI tag
                 String [] linkopption;
                     /*Start looping through each PI*/
                     for(int ix=0; ix<PIS.size(); ix++)
                     {

                                /*Create DOM object that will contain the source document*/
                                Document source;
                                /*Create PI object of each PI tag in the master document*/
                                 instruction  = (ProcessingInstruction)PIS.elementAt(ix);
                                 /*Get date of each PI tag*/
                                    PIdata = instruction.getData();
                                    /*Check and continue only if PI tag has some data*/
                                    if(PIdata!=null)
                                    {
                                        /*Splitt the data with after (::) */
                                        dataFromPI = PIdata.split("::");
                                       /***********The folowing codes are for PI tags that manages ODT files ***********/
                                        //URI string of conventer odt file
                                        String odt2wikiUrl="";
                                        //check wether PI tags are for ODT files
                                        if(PIdata.startsWith("importodt"))
                                        {
                                            //Create a intace of Odtbuilder
                                            odtbuilder odt=new odtbuilder();
                                            /*Perform ODTtoWiki transformation using the URI adress in the PI tags*/
                                            odt2wikiUrl=odt.transformToWiki(dataFromPI[2]);
                                            //retrieve the converter odt file as a DOM object
                                            source=getWikiPage(new URI(odt2wikiUrl));

                                        }
                                        /***********The folowing codes are for PI tags that manages WIKI pages ***********/
                                        //check wether PI tags are for WIKI pages
                                        else if(PIdata.startsWith("importwiki"))
                                        {    //Get the URI adress from PI tags and create DOM object of the webpage
                                             source = getWikiPage(new URI(dataFromPI[2]));
                                             /*Retrieve the URI adress for later use*/
                                            odt2wikiUrl=dataFromPI[2];
                                        }
                                        else{
                                            source=null; // Bør ha en fornuftig output til bruker?
                                            //System.out.println("Source not found"); // For debugging
                                            throw new Exception("The source not found");
                                        }


                                        /* Retrieve the user option of wiki href links*/
                                       linkopption=dataFromPI[3].split(":");

                                      //Create a document fragment object that will contain the retrieved wiki content
                                       DocumentFragment wikifragment = getContent(source, master, dataFromPI[1]);
                                       DocumentFragment fragment=null;
                                       //check if the user choosed to keep the links
                                       if(linkopption[1].equalsIgnoreCase("Yes"))
                                       {
                                           //Perform the linkoption
                                           fragment = links(wikifragment, master, true);
                                       }
                                       //Check wether the user choose to not keep the links
                                       if(linkopption[1].equalsIgnoreCase("No"))
                                       {
                                           //Perform the linkoption
                                          fragment = links(wikifragment, master, false);
                                       }



                                       // Sjekke om optionet for DownloadImg er satt.
                                       //Check wether the user added Image options
                                       if(dataFromPI.length>=5)
                                       {
                                           //Get the user option
                                         String[] imageOption=dataFromPI[4].split(":");

                                         //Check wether the user choosed to download the images in the wikipage
                                           if(imageOption[0].equalsIgnoreCase("DownloadImg")&&imageOption[1].equalsIgnoreCase("Yes"))
                                           {
                                            fragment=WikiImages.downloadImages(fragment, master, odt2wikiUrl);
                                           }
                                       }
                                       else
                                       {
                                           //Download the images anyway in case the wiki document was a converted document from ODT file.
                                           fragment=WikiImages.downloadImages(fragment, master, odt2wikiUrl);
                                       }
                                      if(fragment!=null)
                                      {
                                          //Perfrom the replace process and replace the PI tags with the documentfragment from WIKI document
                                       instruction.getParentNode().replaceChild(fragment, instruction);
                                      }
                                    }
                            }
                         /*Start filetering the document*/
                         documentFiltering(master);
         }
         catch(Exception e)
         {
             throw new Exception("Could not generate Document "+e.getMessage());
         }

    }

/**
 * This function changes the links in the document according to the user choice. If the user chooses to keep the links
 * then all the links will be changed to absolute links. in the other hand if the user chooses to remove the links then
 * the linktag <a> will be replaced with the textcontent of the <a> tag.
 * This function does not remove the  images in the document. the images links will only be made into absolute linkpath.
 * @param doc The document
 * @param keeplinks true to keep the links or false to remove the links. NB! Does not effect images links
 * @throws java.lang.Exception
 */



    private static  void manageLinks(Document doc, boolean keeplinks) throws Exception
    {
        try
        {
                //create a instace of URI
                URI uri = new URI(URISOURCE);
                //This URI instace will be used to perfom some test on the URI adress
                URI urichecker=null;
               //Get all the link tags in the WIKI document
               NodeList links = doc.getElementsByTagName("a");//gett all links in the document
               //This element will contain the <a> elements
              Element linktag =null;
              //This Attr object will be containing the attribute elements of the <a> tag
              Attr attributes=null;

              Node anode =null;
                    //Loop through the each <a> tag.
                    for(int ix=links.getLength()-1;ix >-1; ix--)
                    {
                        //check that the <a> tag has attributes
                        if((links!=null) && links.item(ix).hasAttributes())
                        {
                            //get the <a> tag
                            linktag =(Element) links.item(ix);
                            /*The folowing codes removes the <a> if it contains some a attribute name*/
                             if(linktag.hasAttribute("name"))
                              {
                                  linktag.getParentNode().removeChild(linktag);
                              }
                            /*The folowing code will find if the link is a image used as enlarge button. sicen we wouldn't need it we will delete it from the
                             document. THe enlarge button is a image tag with attribute class. the class atrribute has value=internal then wr will remove it*/
                            if(linktag.hasAttribute("class"))//fjerne enlarge knappen
                            {
                                Attr enlarge =linktag.getAttributeNode("class");
                                if(enlarge.getValue().equalsIgnoreCase("internal"))
                                {
                                    linktag.getParentNode().removeChild(linktag);
                                }
                            }
                                //get the attribute of <a> tag if it is a href attribute
                               attributes=linktag.getAttributeNode("href");
                               String imagetagname="";
                               /*****THe folowing codes are for <img> tags in the <a> tag**********/
                                 if((linktag.hasChildNodes()))
                                 {
                                    //get the <img> in the the <a> tagg.
                                       NodeList imagelinks=  linktag.getElementsByTagName("img");
                                     for(int i=0; i<imagelinks.getLength(); i++)
                                     {
                                         Element imagetag = (Element)imagelinks.item(i);//get the imagetag in the linktag
                                         Attr imageattribute = imagetag.getAttributeNode("src");//get the src node in the image node
                                         if(imageattribute!=null)
                                         {
                                             urichecker = new URI(imageattribute.getValue());
                                             /*Check* wether the uri of the image is absolute or relative. if relative then change it to absolute*/
                                             if(urichecker.isAbsolute())
                                             {
                                                 /*Sett src of the image to be absolute*/
                                                 imageattribute.setValue(urichecker.toString());

                                              //   System.out.println(imageattribute.getValue());
                                             }
                                             else
                                             {
                                                 /*If the link of the image is a relative link then change it to absolute*/
                                                 imageattribute.setValue("http://"+uri.getHost()+imageattribute.getValue());

                                                 //System.out.println(imageattribute.getValue());

                                             }
                                         }
                                         //retrive the the name of the <img> for later use
                                        imagetagname = imagelinks.item(i).getNodeName();
                                     }
                                 }
                                     /*The folowing code for <a> tagg manupilation if the user want to keep the links*/
                                           if((attributes!=null) && (keeplinks==true) )
                                           {
                                              //check if the href tagg in the <a> is a absolute or relative
                                               urichecker = new URI(attributes.getValue());
                                                   if(urichecker.isAbsolute())
                                                   {
                                                        attributes.setValue(urichecker.toString());
                                                   }
                                                   else
                                                   {
                                                            //Change it to abosolute if its not
                                                            attributes.setValue("http://"+uri.getHost()+attributes.getValue());
                                                    }
                                           }
                                            /*The folowing code for <a> tagg manupilation if the user doesn't want to keep the links*/
                                           if((keeplinks==false) &&(linktag.hasAttribute("href")) && (imagetagname.equalsIgnoreCase("img")==false) &&(linktag.hasAttribute("title")) )
                                           {
                                                try
                                                {   //replace the <a> tag with the textcontent of the <a> tag
                                                    anode=doc.createTextNode(linktag.getTextContent());
                                                    linktag.getParentNode().replaceChild(anode , linktag);
                                                }
                                                catch(NullPointerException nulpc)
                                                {

                                                }

                                           }



                        }
                    }

        }
        catch(Exception e)
        {
            throw new Exception("Could not ManageLinks "+e.getMessage());
        }
    }

    /**
     * This function will excute the MangaLinks method on the documentfragment from a Wiki document.
     * @param wikifragment the documentfragment from a wiki file
     * @param master The master document that will recieve the documentfragment
     * @param keeplinks yes to keep the links or No to remove them
     * @return A documentframgnet that has been changed
     * @throws java.lang.Exception
     */
    private static DocumentFragment links(DocumentFragment wikifragment, Document master, boolean keeplinks) throws Exception
    {
        DocumentFragment wikifrag;
        try
        {
            //Createa a new document to retain the documentfragmnet
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            //Create a new documentfragment that we will be returning
            DocumentFragment fr = doc.createDocumentFragment();
            //Import a documentfragment from the WIKI page to the new created documentfragment
            fr.appendChild(doc.importNode(wikifragment, true));
            //Create a element that will be containing the documentfragment
            Element root = doc.createElement("span");
            //Add the documentfragment to the element
            root.appendChild(fr);
            //Add the element in to the document
            doc.appendChild(root);
            //Perform the operation to change the links
           manageLinks(doc, keeplinks);
           //Get the root element in the document
           Element fragm = doc.getDocumentElement();
           //Create a new documentfragment from the master document
            wikifrag = master.createDocumentFragment();
           //We add the documentfragment that has a new linkoption to the master document
            wikifrag.appendChild(master.importNode(fragm, true));
        }
        catch(Exception e)
        {
            throw new Exception("Could not apply to linkopption "+e.getMessage());
        }
            //return the wikifragment
            return wikifrag;
    }
/**
 * This method is used to clean the document using JTedy and return a encoded document
 * @param document the document
 * @param encoding type of encoding
 * @return Cleaned and encoded document
 * @throws java.lang.Exception
 */
    private static Document cleanAndMakeDocument(String document,String encoding) throws Exception
    {
        Document doc;
        try
        {
         doc = domer.makeDomFromString( accessutils.tidyAndMakeXML(readFile(new File(document), encoding), encoding));
        }
        catch(Exception e)
        {
            throw new Exception("Cound not clean the document "+e.getMessage());
        }
         return doc;
    }



    /**
     * This function creates a DefaultMutableTreeNode object that will be used to create a treeview
     * @param source the source document
     * @return DefaultMutableTreeNode that can be used to create a treeview
     * @throws java.lang.Exception
     */

   
    public static DefaultMutableTreeNode generateTree(Document source) throws Exception
    {
         //Create Defualtmutable tree node
         DefaultMutableTreeNode h1 = new DefaultMutableTreeNode();
         h1.setAllowsChildren(true); //allow children
         DefaultMutableTreeNode h2=null;
         DefaultMutableTreeNode h3=null;
         DefaultMutableTreeNode h4=null;
         DefaultMutableTreeNode h5=null;
         DefaultMutableTreeNode h6 =null;



         try
         {

                //Start looping the source and get h1,h2,h3,h4,h5,h6
                NodeList title = source.getElementsByTagName("h1");
                //get the title of the wiki document
                for(int ix=0; ix<title.getLength(); ix++)
                {
                    //Check if the h1 tag has any attributes
                    if(title.item(ix).hasAttributes())
                    {
                        //Get the element
                        Element elementwithattribute = (Element)title.item(ix);
                         //Check if the element has attributes with specified attributename and value
                        if(elementwithattribute.getAttribute("class").equals("firstHeading"))
                        {

                            //Add the textcontent of the node into DefaultMutableTreeNode
                            h1.setUserObject(elementwithattribute.getTextContent());
                        }
                    }
                }

                //Get each h2 tag and create spantaglist of h3, h4,h5,h6
                NodeList h2list = source.getElementsByTagName("h2");
                //start looping
                Node looper =null;
                for(int ix=0; ix<h2list.getLength(); ix++)
                {

                    looper =  h2list.item(ix);//get the h2 tag

                     NodeList h2children = getChildren(looper);
                     //System.out.println("h2 :"+getHeader(h2children));
                     if(getHeader(h2children)!=null)
                         //add the textcontent of <h2> headlines to the h2 DefaultMutableTreeNode
                     h2 = new DefaultMutableTreeNode(getHeader(h2children));
                     if(h2!=null)
                         //add h2 treenode to h1 treenode
                     h1.add(h2);

                     looper = looper.getNextSibling();//get next
                     while((looper!=null) && (looper.getNodeName().equals("h2")==false))
                     {
                         if((looper.getNodeName().equals("h3")==true))
                         {
                             NodeList h3children = getChildren(looper);
                             if(getHeader(h3children)!=null)
                                 //add the textcontent of <h3> headlines to the h3 DefaultMutableTreeNode
                             h3= new DefaultMutableTreeNode(getHeader(h3children));
                             if(h3!=null)
                                 //add h3 treenode to h2 treenode
                             h2.add(h3);



                         }
                         if((looper.getNodeName().equals("h4")==true))
                         {
                             NodeList h4children = getChildren(looper);
                             if(getHeader(h4children)!=null)
                                 //add the textcontent of <h4> headlines to the h4 DefaultMutableTreeNode
                              h4 = new DefaultMutableTreeNode(getHeader(h4children));
                             if(h4!=null)
                                 //add h4 treenode to h3 treenode
                             h3.add(h4);


                         }
                         if(looper.getNodeName().equals("h5")==true)
                         {
                              NodeList h5children = getChildren(looper);
                              if(getHeader(h5children)!=null)
                                  //add the textcontent of <h5> headlines to the h5 DefaultMutableTreeNode
                              h5 = new DefaultMutableTreeNode(getHeader(h5children));
                              if(h5!=null)
                                  //add h5 treenode to h4 treenode
                             h4.add(h5);
                         }

                         if(looper.getNodeName().equals("h6")==true)
                         {
                              NodeList h6children = getChildren(looper);
                              if(getHeader(h6children)!=null)
                                  //add the textcontent of <h6> headlines to the h6 DefaultMutableTreeNode
                              h6 = new DefaultMutableTreeNode(getHeader(h6children));
                              if(h6!=null)
                                  //add h6 treenode to h5 treenode
                              h5.add(h6);
                         }
                         looper =looper.getNextSibling();
                     }
                }
         }
         catch(Exception e)
         {
             throw new Exception("Could not create Treenode "+e.getMessage());
         }
        //return h1 treenode
        return h1;
    }
/**
 * This function returns a fragment of a wiki page
 * @param source Source document which is the wiki page
 * @param master The master document that will inharit the documentfragment
 * @param headline The wiki headline that represents the documentfragment
 * @return A fragment of a wiki page
 * @throws java.lang.Exception
 */
    public static DocumentFragment getContent(Document source,Document master, String headline) throws Exception
     {
        //Create the documentfragment
         DocumentFragment df = master.createDocumentFragment();
         try
         {
                 //splitt the headline in case it is a combination of many headlines.
                 String [] headlinedata  = headline.split(",");
                 String elementtag =null;
                 //incase the headline is singe
                 if(headlinedata.length==1)
                  elementtag="h1";
                 //incase the headline is combination of two headlines
                 if(headlinedata.length==2)
                 elementtag="h2";
                  //incase the headline is combination of three headlines
                 if(headlinedata.length==3)
                   elementtag = "h3";
                  //incase the headline is combination of four headlines
                 if(headlinedata.length==4)
                     elementtag="h4";
                 //incase the headline is combination of five headlines
                 if(headlinedata.length==5)
                     elementtag="h5";
                 //incase the headline is combination of six headlines
                 if(headlinedata.length==6)
                     elementtag="h6";
                 Element root=null;
                 Element span =null;

                 if(elementtag!=null)
                 {
                     //add the <h*> headline to the root element
                    root = master.createElement(elementtag);
                    //Create a element that will contain the rest of the fragment
                    span = master.createElement("span");


                 }
                 NodeList childnodes = null;
                 Node looper = null;
                 NodeList list=null;

                 list = source.getElementsByTagName(elementtag);
                 //loop through the <h*>
                 for(int ix=0; ix<list.getLength(); ix++)
                 {

                     looper = list.item(ix);

                          if(elementtag.equals("h1")==false)
                              //Get the <span> tags in the <h*> element
                          childnodes = getChildren(looper);
                         //incase its a <h1> tag the title of the wiki document
                     if((elementtag.equals("h1")))
                     {
                         //get the title
                         String title =getTitle(looper);
                         //compare the title with the title the user requested
                                 if((headlinedata[0].equals(title)))
                                 {   if(span!=null )

                                     span.setTextContent(headlinedata[0]);
                                     if(root!=null)
                                     root.appendChild(span);
                                     //add the title to the documentframgent
                                     df.appendChild(root);
                                 }

                             looper = looper.getNextSibling();
                             //get all the content under the headline. it stopps when it comes to another headline
                             while((looper!=null)/*&& (headlinedata[0].equals(title)) && (keepGoing(elementtag, looper))==true */)
                             {
                                 //import all the content under the headline
                                    df.appendChild(master.importNode(looper, true));
                                     looper = looper.getNextSibling();
                             }
                     }
                     //incase its a <h2> headline tag
                     if(elementtag.equals("h2"))
                     {          //compare and add the headline to the documentfragment
                                 if((headlinedata[1].equals(getHeader(childnodes))))
                                 {   if(span!=null)
                                     span.setTextContent(headlinedata[1]);
                                      if(root!=null)
                                     root.appendChild(span);
                                     df.appendChild(root);
                                 }

                             looper = looper.getNextSibling();
                             //get all the content under the headline. it stopps when it comes to another headline
                             while((looper!=null)&&(keepGoing(elementtag, looper)==true)  && (headlinedata[1].equals(getHeader(childnodes))))
                             {
                                 df.appendChild(master.importNode(looper, true));
                                 looper = looper.getNextSibling();
                             }
                     }
                     //incase its is a <h3> type headline do the same process ass other document fragment
                     if(elementtag.equals("h3"))
                     {
                                 if((headlinedata[2].equals(getHeader(childnodes))))
                                 {   if(span!=null)
                                     span.setTextContent(headlinedata[2]);
                                      if(root!=null)
                                     root.appendChild(span);
                                     df.appendChild(root);
                                 }

                             looper = looper.getNextSibling();

                             while((looper!=null) &&(keepGoing(elementtag, looper)==true)  && (headlinedata[2].equals(getHeader(childnodes))))
                             {

                                    df.appendChild(master.importNode(looper, true));


                                 looper = looper.getNextSibling();
                             }
                     }
                     if(elementtag.equals("h4"))
                     {

                                 if((headlinedata[3].equals(getHeader(childnodes))))
                                 {
                                     if(span!=null)
                                     span.setTextContent(headlinedata[3]);
                                      if(root!=null)
                                     root.appendChild(span);
                                     df.appendChild(root);
                                 }

                             looper = looper.getNextSibling();

                             while((looper!=null) &&(keepGoing(elementtag, looper)==true)&& (headlinedata[3].equals(getHeader(childnodes))))
                             {

                                 df.appendChild(master.importNode(looper, true));


                                 looper = looper.getNextSibling();
                             }
                     }
                     if(elementtag.equals("h5"))
                     {
                                 if((headlinedata[4].equals(getHeader(childnodes))))
                                 {   if(span!=null)
                                     span.setTextContent(headlinedata[4]);
                                      if(root!=null)
                                     root.appendChild(span);
                                     df.appendChild(root);
                                 }

                             looper = looper.getNextSibling();

                             while((looper!=null) &&(keepGoing(elementtag, looper)==true) && (headlinedata[4].equals(getHeader(childnodes))))
                             {
                                 df.appendChild(master.importNode(looper, true));
                                 looper = looper.getNextSibling();
                             }
                     }
                     if(elementtag.equals("h6"))
                     {
                                 if((headlinedata[5].equals(getHeader(childnodes))))
                                 {   if(span!=null)
                                     root.setTextContent(headlinedata[5]);
                                      if(root!=null)
                                     root.appendChild(span);
                                     df.appendChild(root);
                                 }

                             looper = looper.getNextSibling();

                             while((looper!=null)&&(keepGoing(elementtag, looper)==true)  && (headlinedata[5].equals(getHeader(childnodes))))
                             {
                                 df.appendChild(master.importNode(looper, true));
                                 looper = looper.getNextSibling();
                             }
                     }

                 }
         }
         catch(Exception e)
         {
             throw new Exception("Could not get Wikicontent "+e.getMessage());
         }

         return df;
     }
/**
 * This function gets the textconent of the <h1> tag that is the title.
 * @param titlenode <h1> node
 * @return A string representing the Title of the wikipage
 * @throws java.lang.Exception
 */
    private static String getTitle(Node titlenode) throws Exception
     {
        String title =null;
        try
        {
             

             if(titlenode.hasAttributes())
                {
                    //Get the element
                    Element elementwithattribute = (Element)titlenode;
                     //Check if the element has attributes with specified attributename and value
                    if(elementwithattribute.getAttribute("class").equals("firstHeading"))
                    {


                       title = elementwithattribute.getTextContent();
                    }
             }
        }
        catch(Exception e)
        {
            throw new Exception("Could not get the title of the page "+e.getMessage());
        }
         
         return title;
     }
    /**
     * A test method used in the getContent method.
     * @param elementtag <h*> tag
     * @param looper the current node
     * @return true if the will add the node to the documentfragment or false to ignore
     * @throws java.lang.Exception
     */
    private static boolean keepGoing(String elementtag,Node looper) throws Exception
    {
        

                /*   if(elementtag.equalsIgnoreCase("h1"))
                {
                      if((looper.getNodeName().equals("h2")) ||(looper.getNodeName().equals("h1")))
                      {
                         return false;
                      }
                }*/
                if(elementtag.equalsIgnoreCase("h2"))
                {

                      if((looper.getNodeName().equals("h2")) ||(looper.getNodeName().equals("h1")))
                      {
                         return false;

                      }
                }
                if(elementtag.equalsIgnoreCase("h3"))
                {

                      if((looper.getNodeName().equals("h3")) || (looper.getNodeName().equals("h2")) ||(looper.getNodeName().equals("h1")))
                      {
                         return false;

                      }

                }
                if(elementtag.equalsIgnoreCase("h4"))
                {

                      if((looper.getNodeName().equals("h4")) ||(looper.getNodeName().equals("h3")) || (looper.getNodeName().equals("h2")) ||(looper.getNodeName().equals("h1")))
                      {
                         return false;

                      }
                }

                   if(elementtag.equalsIgnoreCase("h5"))
                {

                      if((looper.getNodeName().equals("h5")) ||(looper.getNodeName().equals("h4")) ||(looper.getNodeName().equals("h3")) || (looper.getNodeName().equals("h2")) ||(looper.getNodeName().equals("h1")))
                      {
                         return false;

                      }


                }
                       if(elementtag.equalsIgnoreCase("h6"))
                {

                      if((looper.getNodeName().equals("h6")) ||(looper.getNodeName().equals("h5")) ||(looper.getNodeName().equals("h4")) ||(looper.getNodeName().equals("h3")) || (looper.getNodeName().equals("h2")) ||(looper.getNodeName().equals("h1")))
                      {
                         return false;

                      }


                }
        
        

        return true;
    }

    /**
     * A function used to filter the master document and remove unimportent elements
     * @param master the master document
     * @throws java.lang.Exception
     */
    private static void documentFiltering(Document master) throws Exception
    {
        try
        {
                /**********************************************************************
                 * Starting with removing the [EDIT] button in the most wikipage
                 */
                /*retrieve all <span> tags in the document*/
                NodeList spantaglist = master.getElementsByTagName("span");
                Element spantag=null;
                    /*Start looping thrrough each <span> tag*/
                   for(int ix=0; ix<spantaglist.getLength(); ix++)
                   {
                       if(spantaglist.item(ix).hasAttributes())
                        {
                             spantag = (Element)spantaglist.item(ix);
                             if(spantag.getAttribute("class").equals("editsection"))
                             {
                                 spantag.getParentNode().removeChild(spantag);
                             }

                         }
                   }
                /***************************************************************************
                 * End of removing [Edit] button
                 */


                /**************************************************************************
                 * Starting of removing other unnaccessay html tags like the nagavigation link
                 */

                NodeList h3list = master.getElementsByTagName("h3");
                Element h3tag=null;
                for(int ix=0; ix<h3list.getLength(); ix++)
                {

                    if(h3list.item(ix).hasAttributes())
                    {
                        h3tag = (Element) h3list.item(ix);
                        if(h3tag.getAttribute("id").equals("siteSub"))
                        {
                            h3tag.getParentNode().removeChild(h3tag);
                        }
                    }
                }

                /************************Start removing the tableofcontent**********************************************/
                 NodeList divlist = master.getElementsByTagName("div");
                Element divtag=null;
                for(int ix=0; ix<divlist.getLength(); ix++)
                {

                    if(divlist.item(ix).hasAttributes())
                    {
                        divtag = (Element) divlist.item(ix);
                        if(divtag.getAttribute("id").equals("jump-to-nav"))
                        {
                            divtag.getParentNode().removeChild(divtag);
                        }
                        if(divtag.getAttribute("id").equals("toctitle"))
                        {
                            System.out.println("i am in");
                            //divtag.getParentNode().removeChild(divtag);
                            System.out.println(divtag.getAttribute("id"));
                        }
                    }
                }
                    NodeList tablelist = master.getElementsByTagName("table");
                    Element tabletag=null;
                    for(int ix=0; ix<tablelist.getLength(); ix++)
                    {
                        tabletag = (Element)tablelist.item(ix);
                        if(tabletag.getAttribute("class").equals("toc") && tabletag.getAttribute("id").equals("toc"))
                        {
                            tabletag.getParentNode().removeChild(tabletag);
                        }
                    }
        }
        catch(Exception e)
        {
            throw new Exception("Errir filtering document "+e.getMessage());
        }

        }
}


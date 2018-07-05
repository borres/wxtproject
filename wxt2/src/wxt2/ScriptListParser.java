package wxt2;

import java.net.URI;
import java.util.Vector;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import utils.accessutils;

/**
 * This is a utility class which simplifies the script for a great variety of projects.
 * <p>
 * The class houses three functions<br/>
 * - parseModuleLists<br/>
 * - parseXMLContentLists<br/>
 * - parseXMLContentLists<br/>
 * These methods walks the script and expands modulelist, xmlcontentlis and 
 * txtcontentlist to modules, xmlcontent and txtcontent respectively.<br/>
 * This is done before the modulestructure is established<br/>
 * This  is not adding any core functionality to wxt.
 */
public class ScriptListParser {
    
    public ScriptListParser()
    {
        
    }
    
    /**
     * Walk all children of a node (module) and produce modules for modulelists.
     * <p>
     * The modules produced replaces the modulelist.
     * 
     * @param root The node whos children we will investigate
     */
    public static void parseModuleLists(Node root,Scripthandler scriptH)
     {
        Node n=null;
        if(root==null)
            n = scriptH.getScriptDoc().getDocumentElement().getFirstChild();
        else
            n=root.getFirstChild();
        
        while (n != null) {
            if ((n.getNodeType() == Node.ELEMENT_NODE) &&
                (n.getNodeName().compareToIgnoreCase("ModuleList") == 0)) 
            {
                // establish Module tree                                      
                try {
                    DocumentFragment df=makeModulesFromAList((Element)n,scriptH);
                    n.getParentNode().replaceChild(df, n);
                } catch (Exception e) {
                    scriptH.getReporter().pushMessage("root_module_failed");                    
                }
            }
            else if ((n.getNodeType() == Node.ELEMENT_NODE) &&
                (n.getNodeName().compareToIgnoreCase("module") == 0))
            {
                parseModuleLists(n,scriptH);
            }
            n = n.getNextSibling();
        }        
     }
     
     /**
      * Produce a list of modules and replace a modulelist element with this list
      * @param elt The element describing the list
      * @return A fragment with all the produced modules
      */
      private static DocumentFragment makeModulesFromAList(Element elt,Scripthandler scriptH)
     {
         DocumentFragment df=scriptH.getScriptDoc().createDocumentFragment();
         // pick up values: sourcecatalog, targetcatalog, template
         if(!elt.hasAttribute("catalog"))
         {
             scriptH.getReporter().pushMessage("missing_catalog_in_modulelist");
             return df;
         }
         String catalog=elt.getAttribute("catalog");
         // assume this catalog is absolute or relative to scriptcatalog         
         try{
            catalog=scriptH.getDefinitions().substituteFragments(catalog);
            String dummyfilename="myfile.txt";
            String dummyfile=catalog+"/"+dummyfilename;
            URI theUri=accessutils.makeAbsoluteURI(dummyfile, scriptH.getScriptCatalog());
            String pth=theUri.getPath();
            catalog=pth.substring(0, pth.lastIndexOf(dummyfilename));

        }
         catch(Exception e)
         {
             scriptH.getReporter().pushMessage("cannot_do_module_list",e.getMessage());
             return df;
         }
         
         String sourcesuf=elt.getAttribute("sourcesuffix");
         if(sourcesuf.isEmpty())
             sourcesuf="xml";
         if(sourcesuf.startsWith("."))
             sourcesuf=sourcesuf.substring(1);
         
         String targetsuf=elt.getAttribute("targetsuffix");
         if(targetsuf.isEmpty())
             targetsuf="html";
         if(targetsuf.startsWith("."))
             targetsuf=targetsuf.substring(1);
         
         // cannot have same suffix
         if(sourcesuf.compareToIgnoreCase(targetsuf)==0)
         {
             scriptH.getReporter().pushMessage("cannot_have_same_suffix");
             return df;           
         }
         String template=elt.getAttribute("template");
         String book=elt.getAttribute("books");
         // if no template, source will be template
         // if no target, target will be source with html-extension
         
         // find all source files
         Vector<String> sourcelist=accessutils.getSortedFileList(catalog,sourcesuf);
         
         if(sourcelist.size()==0)
         {
             // error/warning
             scriptH.getReporter().pushMessage("no_modules_to_modulelist");
         }

         // make module elements
         for(String s:sourcelist)
         {
             String modname=s.replaceAll("\\Q."+sourcesuf+"\\E", "");
             
             if(!catalog.endsWith("/"))
                 catalog=catalog+"/";           
             s=catalog+s;
             Element melt=scriptH.getScriptDoc().createElement("module");
             if(template.isEmpty())
                 melt.setAttribute("template", s);
             else
             {
                 melt.setAttribute("template", template);
                 // append ontent, xml content
                 Element celt=scriptH.getScriptDoc().createElement("xmlcontent");
                 celt.setAttribute("location", s);
                 melt.appendChild(celt);
             }             
             melt.setAttribute("location",s.replaceAll("\\Q."+sourcesuf+"\\E", "."+targetsuf));
             melt.setAttribute("name", modname);
             if(!book.isEmpty())
                 melt.setAttribute("books", book);
             df.appendChild(melt);
         }                  
         return df;
     }
      
      
     /**
      * Walk all children of a node (module) and produce xmlcontent for xmlcontentlists.
      * <p>
      * The xmlcontent elements produced are replacing the xmlcontentlist.
      * 
      * @param root The element whos children we will investigate
      */
      public static void parseXMLContentLists(Node root,Scripthandler scriptH)
     {
        Node n=null;
        if(root==null)
            n = scriptH.getScriptDoc().getDocumentElement().getFirstChild();
        else
            n=root.getFirstChild();
        
        while (n != null) {
            if ((n.getNodeType() == Node.ELEMENT_NODE) &&
                (n.getNodeName().compareToIgnoreCase("XMLContentlist") == 0)) 
            {
                                                    
                try {
                    DocumentFragment df=makeXMLContentsFromAList((Element)n,scriptH);
                    n.getParentNode().replaceChild(df, n);
                } 
                catch (Exception e) 
                {
                    scriptH.getReporter().pushMessage("root_module_failed");                    
                }
            }
            else if ((n.getNodeType() == Node.ELEMENT_NODE) &&
                (n.getNodeName().compareToIgnoreCase("module") == 0))
            {
                parseXMLContentLists(n,scriptH);
            }
            n = n.getNextSibling();
        }        
     }
     
     /**
      * Produce all xmlcontent elements from list
      * @param elt The element which will be replaced
      * @return A fragment with xmlcontent elements
      */
      private static DocumentFragment makeXMLContentsFromAList(Element elt,Scripthandler scriptH)
     {
        DocumentFragment df=scriptH.getScriptDoc().createDocumentFragment();
        // pick up the one and only attribute: catalog
        if(!elt.hasAttribute("catalog"))
         {
             scriptH.getReporter().pushMessage("missing_catalog_inxmlcontentlist");
             return df;
         }
         String catalog=elt.getAttribute("catalog");
         String sourcesuf=elt.getAttribute("sourcesuffix");
         if(sourcesuf.isEmpty())
             sourcesuf="xml";
         if(sourcesuf.startsWith("."))
             sourcesuf=sourcesuf.substring(1);
         // assume this catalog is absolute or relative to scriptcatalog         
         try{
            catalog=scriptH.getDefinitions().substituteFragments(catalog);
            String dummyfilename="myfile.txt";
            if(!catalog.endsWith("/"))
                catalog=catalog+"/";
            String dummyfile=catalog+dummyfilename;
            URI theUri=accessutils.makeAbsoluteURI(dummyfile, scriptH.getScriptCatalog());
            String pth=theUri.getPath();
            catalog=pth.substring(0, pth.lastIndexOf(dummyfilename));

        }
         catch(Exception e)
         {
             scriptH.getReporter().pushMessage("cannot_do_xmlcontent_list",e.getMessage());
             return df;
         }
         // find all source files
         Vector<String> sourcelist=accessutils.getSortedFileList(catalog,sourcesuf);
         
         if(sourcelist.size()==0)
         {
             // error/warning
             scriptH.getReporter().pushMessage("no_content_to_xmlcontentlist");
         }
          // make XMLContent elements
         for(String s:sourcelist)
         {             
             if(!catalog.endsWith("/"))
                 catalog=catalog+"/";           
             s=catalog+s;
             Element conelt=scriptH.getScriptDoc().createElement("xmlcontent");
             conelt.setAttribute("location", s);
             df.appendChild(conelt);
         }
         return df;
     }    

     /**
      * Walk all children of a node (module) and produce txtcontent for txtcontentlists.
      * <p>
      * The txtcontent elements produced are replacing the txtcontentlist.
      * 
      * @param root The element whos children we will investigate
      */
      public static void parseTXTContentLists(Node root,Scripthandler scriptH)
     {
        Node n=null;
        if(root==null)
            n = scriptH.getScriptDoc().getDocumentElement().getFirstChild();
        else
            n=root.getFirstChild();
        
        while (n != null) {
            if ((n.getNodeType() == Node.ELEMENT_NODE) &&
                (n.getNodeName().compareToIgnoreCase("txtcontentlist") == 0)) 
            {
                                                    
                try {
                    DocumentFragment df=makeTXTContentsFromAList((Element)n,scriptH);
                    n.getParentNode().replaceChild(df, n);
                } 
                catch (Exception e) 
                {
                    scriptH.getReporter().pushMessage("root_module_failed");                    
                }
            }
            else if ((n.getNodeType() == Node.ELEMENT_NODE) &&
                (n.getNodeName().compareToIgnoreCase("module") == 0))
            {
                parseTXTContentLists(n,scriptH);
            }
            n = n.getNextSibling();
        }        
     }
     
     /**
      * Produce all xmlcontent elements from list
      * @param elt The element which will be replaced
      * @return A fragment with xmlcontent elements
      */
      private static DocumentFragment makeTXTContentsFromAList(Element elt,Scripthandler scriptH)
     {
        DocumentFragment df=scriptH.getScriptDoc().createDocumentFragment();
        // pick up the one and only attribute: catalog
        if(!elt.hasAttribute("catalog"))
         {
             scriptH.getReporter().pushMessage("missing_catalog_intxtcontentlist");
             return df;
         }
         String catalog=elt.getAttribute("catalog");
         String sourcesuf=elt.getAttribute("sourcesuffix");
         if(sourcesuf.isEmpty())
             sourcesuf="txt";
         if(sourcesuf.startsWith("."))
             sourcesuf=sourcesuf.substring(1);
         // assume this catalog is absolute or relative to scriptcatalog         
         try{
            catalog=scriptH.getDefinitions().substituteFragments(catalog);
            String dummyfilename="myfile.txt";
            if(!catalog.endsWith("/"))
                catalog=catalog+"/";
            String dummyfile=catalog+dummyfilename;
            URI theUri=accessutils.makeAbsoluteURI(dummyfile, scriptH.getScriptCatalog());
            String pth=theUri.getPath();
            catalog=pth.substring(0, pth.lastIndexOf(dummyfilename));

        }
         catch(Exception e)
         {
             scriptH.getReporter().pushMessage("cannot_do_txtcontent_list",e.getMessage());
             return df;
         }
         // find all source files
         Vector<String> sourcelist=accessutils.getSortedFileList(catalog,sourcesuf);
         
         if(sourcelist.size()==0)
         {
             // error/warning
             scriptH.getReporter().pushMessage("no_content_to_txtcontentlist");
         }
          // make XMLContent elements
         for(String s:sourcelist)
         {             
             if(!catalog.endsWith("/"))
                 catalog=catalog+"/";           
             s=catalog+s;
             Element conelt=scriptH.getScriptDoc().createElement("txtcontent");
             conelt.setAttribute("location", s);
             df.appendChild(conelt);
         }
         return df;
     }
}

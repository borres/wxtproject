package programcode;

import content.Module;

import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import utils.PIcommand;
import utils.domer;
import reporting.reporter;

/**
 * Purpose is to produce color coded sourcecode.
 * Prepare for Google to prettyprint when page is loaded
 * Only used when option preformat-language is set to no (default)
*/
public class CodeGoogleFormatter {

    /** Creates a new instance of codeFormat */
    public CodeGoogleFormatter() {
    }


    /**
      * Expand preformatted codefragments and prepare for googlejob.
      * Identifies and expands pre tags with class containing PRETTYPRINT:
      * @param doc The document we investigate
      * @param encoding The encoding we want
      * @return The document with expanded code
      */
     public static Document expandGoogleCodeFragments(Document doc,String encoding,Module mod)
     {
         reporter theReporter=mod.getReporter();
         NodeList nlist=null;
         String xp=null;

         //---------------------------
         // run all, pick up old format and make new
         CodeBasics.transformToNewForm(doc);

         //-----------------------------------
         // access the actual nodes again. Pick up those set according
         // to new Google pretty (originally or transformed above)
         xp="//pre[contains(@class,'"+CodeBasics.PRETTYPRINT+"')]";
         try{
             nlist=domer.performXPathQuery(doc, xp);
         }
         catch(Exception te){
              System.out.println("expandGoogleFormatter:"+te.getMessage());
              theReporter.pushMessage("failed_to_expand_code","google",te.getMessage());
              return doc;
         }
         // run the list and do the job
         for (int ix=0;ix<nlist.getLength();ix++)
         {
                if(nlist.item(ix).getNodeType()!=Node.ELEMENT_NODE)
                    continue;
                Element eltItem=(Element)nlist.item(ix);
                // must clean and see if we have a CDATA
                eltItem.normalize();
                // if no children, there is nothing we can do
                if(!eltItem.hasChildNodes())
                     continue;

                // containg tags because it is already expanded or what ?
                if(eltItem.getElementsByTagName("*").getLength()!=0)
                    continue;

                // if we have a CDATA section we use it, the first and
                // assumed only one
                Node cdataNode=CodeBasics.getFirstCDATANode(eltItem);
                String content=null;
                if(cdataNode!=null)
                {
                    eltItem.replaceChild(doc.createTextNode(cdataNode.getTextContent()), cdataNode);
                }
                // set id
                String newID=mod.getScriptHandler().getANewId(mod.getID());
                // use if already set, like in popup
                if(eltItem.hasAttribute("id"))
                    newID=eltItem.getAttribute("id");

                eltItem.setAttribute("id",newID );

                // mark as not translate
                String cstmp=eltItem.getAttribute("class");
                if(cstmp.indexOf(PIcommand.SKIPTRANSLATE)==-1)
                    eltItem.setAttribute("class",cstmp+" "+PIcommand.SKIPTRANSLATE);
         }
         return doc;
     }

}

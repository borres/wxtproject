package content;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.PIcommand;

/**
 * Constructing a documentfragment according to a dpath expression
 * @author bs
 */
public class DPath
{
    // when filtering:
    /** tags to remove*/
    String[] remove_tags;
    /** tags to use */
    String[] add_tags;
    /** use all tags */
    static boolean use_All;

    // locally usefull contstants
    static final String NO_VALUE="_NO_VALUE";
    static final String ROOT="root";

    // wiki dependant tags
    static final String MW_HEADLINE="mw-headline";
    // the match track
    String matcher="0000000";

    // the components in the dpath
    List<String>dpath=null;
    // where we find places to start collection
    List<Element> starters=null;

    /**
     * Constructor
     * Parse, analyse and prepare pathdata from a dpath expression
     * @param path
     * @throws Exception if syntaxerror in dpath
     */
    public DPath(String path)
    throws Exception{
        if(!setUpDpath(path))
                throw new Exception();
        doSpesificationPart(path);
    }

    /**
     * Do the actual extract according to dpath
     * @param wDoc The domtree to extract from
     * @param mod The Module requesting the fragment
     * @param cmd The command describing the request
     * @return A DocumentFragment
     */
    public DocumentFragment getExtract(Document wDoc,Module mod,PIcommand cmd)
    {
        // assuming that the dpath is legal and that the globals describing
        // the performance is ok

        // prepare a fragment we will collect extracts into
        DocumentFragment df=mod.getDoc().createDocumentFragment();

        // find headers in "flat document-sequence"
        wDoc.normalize();
        NodeList elist=wDoc.getElementsByTagName("*");
        // and locate starters
        starters=new ArrayList<Element>();
        for(int ix=0;ix<elist.getLength();ix++)
        {
            Element e=(Element)elist.item(ix);
            String ename=e.getTagName();
            //System.out.println(ename);
            if(((ename.startsWith("h")||(ename.startsWith("H"))) && (ename.length()==2)))
            {
                int level=0;
                try{level=Integer.parseInt(ename.substring(1));}
                catch(Exception ex){continue;}
                
                String content=e.getTextContent().trim();
                content=content.replace('\n', ' ');
                //System.out.println(""+level+" "+content);

                // compare to legal dpath
                if(level < dpath.size())
                {
                    while(matcher.length()<level) matcher+="0";

                    String T=dpath.get(level);
                    if((T.compareTo(NO_VALUE)==0)||
                       (T.compareTo(content)==0))
                    {
                        matcher=matcher.substring(0, level)+"0";
                        if((level==dpath.size()-1)&&(matcher.indexOf("X")==-1))
                            starters.add(e);
                    }
                    else
                        matcher=matcher.substring(0, level)+"X";
                }
            }
        }

        // we have the starters and are ready to do extracting
        if(starters.isEmpty())
        {
            // no starters and we retun an empty fragment
            mod.getReporter().pushMessage("empty_dpath_extract", cmd.getValue(PIcommand.DPATH));
            return df;
        }

        for(int ix=0;ix<starters.size();ix++)
        {
            Node targetNode=starters.get(ix);
           
            String tmp=((Element)targetNode).getTagName().substring(1);
            int starterLevel=Integer.parseInt(tmp);
            
            boolean more=true;
            while(more)
            {
                
                //if(isWanted(targetNode))
                //    df.appendChild(mod.getDoc().importNode(targetNode,true));

                df.appendChild(selectFrom(targetNode,mod));
               
                targetNode=targetNode.getNextSibling();
                if(targetNode!=null)
                {
                    if(targetNode.getNodeType()==Node.ELEMENT_NODE)
                    {
                        String tName=((Element)targetNode).getTagName();
                        if((tName.length()==2)&&
                           ("123456".indexOf(tName.charAt(1))!=-1)&&
                           ((tName.startsWith("h")) || (tName.startsWith("H"))))
                        {
                            int targetLevel=Integer.parseInt(tName.substring(1));
                            if(targetLevel <= starterLevel)
                                more=false;
                        }
                    }
                }
                else
                    more=false;
            }
             
        }
            
        return df;
    }
    
    /**
     * Can we use this node or someone below in the tree
     * @param n The node to investigae
     * @param mod The module that owns the stuff
     * @return A documentfragment with contents or not
     */
    private DocumentFragment selectFrom(Node n,Module mod)
    {
        DocumentFragment local_df=mod.getDoc().createDocumentFragment();
        if(isWanted(n))
        {
            
            local_df.appendChild(mod.getDoc().importNode(n.cloneNode(true),true));
            if(local_df.getLastChild().getNodeName().compareToIgnoreCase("img")==0)
            {
                // we make this a default wxtimage or not 
                //((Element)local_df.getLastChild()).setAttribute("class","wxtimage");
            }
            return local_df;
        }

        if(n.hasChildNodes())
        {
            Node child=n.getFirstChild();
            while(child!=null)
            {
                local_df.appendChild(selectFrom(child,mod));
                child=child.getNextSibling();
            }

        }
        return local_df;
    }

    /**
     * Check if this node is on the wanted-list
     * @param n The node
     * @return true if we want it, false otherwise
     */
    private boolean isWanted(Node n)
    {
        if(use_All)
            return true;
        String name=n.getNodeName();
        for(int ix=0;ix< remove_tags.length;ix++)
            if(name.compareTo(remove_tags[ix])==0)
                return false;
        for(int ix=0;ix < add_tags.length;ix++)
            if(name.compareTo(add_tags[ix])==0)
                return true;
        return false;
    }

   /**
     * Parse the dpath string and establish a list of targets
     * @param S The dpath string
     * @return true if it is ok, otherwise false
     */
    private boolean setUpDpath(String S)
    {
        if(S==null)
            return false;
        S=S.trim();

        // remove the specification/filter part if found
        S=removeSpesificationPart(S);

        //--------------------------
        // the we go for the path
        S=S.trim();

        // do special cases
        if(S.compareTo("*")==0)
            S=NO_VALUE;
        if(S.compareTo("/")==0)
            S=NO_VALUE+"/"+NO_VALUE;
        if(S.startsWith("/"))
            S=NO_VALUE+S;

        dpath=new Vector<String>(6);
        dpath.add(ROOT); // as level 0
        if(S.isEmpty())
            dpath.add(NO_VALUE);
        // introduce space to make sure we count correct
        S=S.replaceAll("\\Q//\\E","/ /");
        String[] parts=S.split("/");
        for(int ix=0;ix<parts.length;ix++)
        {
            if(parts[ix].trim().isEmpty())
                dpath.add(NO_VALUE);
            else
                dpath.add(parts[ix].trim());
        }
        return true;
    }


        /**
     * Remove filter from dpath string
     * @param S The dpath
     * @return the cleaned dpath without filter
     */
    static private String removeSpesificationPart(String S)
    {
        if(S.endsWith("]"))
        {
            int pos=S.lastIndexOf("[");
            if(pos!=-1)
            {
                return S.substring(0, pos);
            }
        }
        return S;
    }

    /**
     * Analyze the filter part of a dpath
     * Set filtervariables accordingly
     * @param S The complete dpath
     * @return The path without the filter part
     */
    private String doSpesificationPart(String S)
    {
        // a dpath has the form:[a,b,c] or [*]
        // [*]: include everything, same as no filter
        // [a,b,c]: include a and b and c
        // possible values are:
        //     h:all headers, p:paragraphs, t:tables, l:lists, f:formulas, i:images


        S=S.trim();
        //-------------------------
        // initiate filters
        add_tags=new String[0];
        remove_tags=new String[0];
        use_All=true;
        //---------------------------
        // do we have a filter at all
        if(S.endsWith("]"))
        {
            int pos=S.lastIndexOf("[");
            if(pos!=-1)
            {
                String filter=S.substring(pos+1,S.length()-1);
                // prepare the pathpart to return:
                S=S.substring(0, pos);
                //--------------------
                // here we go

                // use nothing ?
                if(filter.isEmpty())
                {
                    use_All=false;
                    return S;
                }
                // use everything ?
                if(filter.trim().compareTo("*")==0)
                {
                    use_All=true;
                    return S;
                }
                // starts with everything, and reduce with -?
                /*
                 if(filter.startsWith("*"))
                {
                    use_All=true;
                    remove_tags=filter.substring(1).trim().split(",");
                    for(int ix=0;ix<remove_tags.length;ix++)
                    {
                        String tmp=remove_tags[ix].trim();
                        tmp=tmp.replaceFirst("-", "").trim();
                        remove_tags[ix]=tmp;
                    }

                    return S;
                }
                 */
                // we have a strait add situation
                use_All=false;
                add_tags=filter.split(",");
                for(int ix=0;ix<add_tags.length;ix++)
                {
                    String tmp=add_tags[ix].trim();
                    add_tags[ix]=tmp;
                }
                return S;
            }
        }
        return S;

    }

}

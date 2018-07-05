package content;

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
    static String[] remove_tags;
    /** tags to use */
    static String[] add_tags;
    /** use all tags */
    static boolean use_All;

    // locally usefull contstants
    static final String NO_VALUE="_NO_VALUE";
    static final String ROOT="root";

    // wiki dependant tags
    static final String MW_HEADLINE="mw-headline";
    // the match track
    static String matcher="0000000";

    // the components in the dpath
    static Vector<String>dpath=null;
    // where we find places to start collection
    static Vector<Element> starters=null;

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
        NodeList elist=wDoc.getElementsByTagName("*");
        // and locate starters
        starters=new Vector<Element>();
        for(int ix=0;ix<elist.getLength();ix++)
        {
            Element e=(Element)elist.item(ix);
            String ename=e.getTagName();
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

                    String T=dpath.elementAt(level);
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
        if(starters.size()==0)
        {
            // no starters and we retun an empty fragment
            mod.getReporter().pushMessage("empty_dpath_extract", cmd.getValue(PIcommand.DPATH));
            return df;
        }


        for(int ix=0;ix<starters.size();ix++)
        {
            Node targetNode=starters.elementAt(ix);
            String tmp=((Element)targetNode).getTagName().substring(1);
            int starterLevel=Integer.parseInt(tmp);
            boolean more=true;
            while(more)
            {
                df.appendChild(mod.getDoc().importNode(targetNode,true));
                targetNode=targetNode.getNextSibling();
                if(targetNode!=null)
                {
                    if(targetNode.getNodeType()==Node.ELEMENT_NODE)
                    {
                        String tName=((Element)targetNode).getTagName();
                        if((tName.length()==2)&&((tName.startsWith("h")) || (tName.startsWith("H"))))
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
     * Parse the dpath string and establish a list of targets
     * @param S The dpath string
     * @return true if it is ok, otherwise false
     */
    static private boolean setUpDpath(String S)
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
     * Remove predicate from dpath string
     * @param S The dpath
     * @return the cleaned dpath without predicate
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
     * Analyze the predicate part of a dpath
     * Set filtervariables accordingly
     * @param S The complete dpath
     * @return The path without the predicate part
     */
    static private String doSpesificationPart(String S)
    {
        // a dpath has the form:[a,b,c] or [*]
        // [*]: include everything, same as no predicate
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
        // do we have a predicate at all
        if(S.endsWith("]"))
        {
            int pos=S.lastIndexOf("[");
            if(pos!=-1)
            {
                String predicate=S.substring(pos+1,S.length()-1);
                // prepare the pathpart to return:
                S=S.substring(0, pos);
                //--------------------
                // here we go

                // use nothing ?
                if(predicate.isEmpty())
                {
                    use_All=false;
                    return S;
                }
                // use everything ?
                if(predicate.trim().compareTo("*")==0)
                {
                    use_All=true;
                    return S;
                }
                // starts with everything, and reduce with -?
                if(predicate.startsWith("*"))
                {
                    use_All=true;
                    remove_tags=predicate.substring(1).trim().split(",");
                    for(int ix=0;ix<remove_tags.length;ix++)
                    {
                        String tmp=remove_tags[ix].trim();
                        tmp=tmp.replaceFirst("-", "").trim();
                        remove_tags[ix]=tmp;
                    }

                    return S;
                }
                // we have a strait add situation
                use_All=false;
                add_tags=predicate.split(",");
                for(int ix=0;ix<add_tags.length;ix++)
                {
                    String tmp=add_tags[ix].trim();
                    //tmp=tmp.replaceFirst("-", "").trim();
                    add_tags[ix]=tmp;
                }
                return S;
            }
        }
        return S;

    }

}

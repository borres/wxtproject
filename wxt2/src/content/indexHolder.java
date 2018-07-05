package content;

import java.net.URI;
import java.util.Vector;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import utils.PIcommand;
import utils.accessutils;

/**
 * Handle all indexes
 * 
 */
public class indexHolder {
    public static String NO_WORD="_no_word";
    public static String NO_COMMENT="_no_comment";
    private Vector<ixEntry> m_indexes;

    public indexHolder()
    {
        m_indexes=new Vector<ixEntry>();
    }
    
    /**
     * Set in a word that will be indexed
     * @param m The module that makes the request to insert and that owns the word
     * @param cmd The cmd (ixword)
     * @param id A value that anchors the word on the module
     */
    public void insertWord(Module m,PIcommand cmd,String id)
    {
        // id is "" when we have not left a mark(name tag) on module
        if(m==null)
            return;
        ixEntry ie=new ixEntry(m,cmd,id);
        // insert in word - order
        if(m_indexes.size()>0)
        {
            ixEntry cmp=m_indexes.firstElement();
            int ix=0;
            while((ix<m_indexes.size()) && 
                  (m_indexes.elementAt(ix).m_word.compareToIgnoreCase(ie.m_word) < 0))
                ix++;
            // we do not want to set in same word with same id again
            if((ix<m_indexes.size())&&
               (m_indexes.elementAt(ix).m_word.compareToIgnoreCase(ie.m_word) == 0)&&
               (m_indexes.elementAt(ix).m_id.compareToIgnoreCase(ie.m_id) == 0))
                return;

            if(ix < m_indexes.size())
                m_indexes.insertElementAt(ie, ix);
            else
                m_indexes.add(ie);                    
        }
        else
            m_indexes.add(ie);


    }
    
    /**
     * Make a reference line in the indexlist
     * @param m The module
     * @param cmd The comand
     * @param ie The index entry
     * @return A a-element with ref to the indexed word on the indexed module
     */
    private Element makeRefLine(Module m,PIcommand cmd,ixEntry ie)
    {
        Element aElt=m.getDoc().createElement("a");
        URI relUri;
        try{
            relUri=accessutils.makeRelativeURI(m.getAbsoluteUri(), ie.m_holder.getAbsoluteUri());
        }
        catch(Exception e)
        {
            relUri=ie.m_holder.getAbsoluteUri();
        }
        if(!ie.m_id.isEmpty())
            aElt.setAttribute("href", relUri.toString()+"#"+ie.m_id);
        else
            aElt.setAttribute("href", relUri.toString());
        aElt.appendChild(m.getDoc().createTextNode(ie.m_holder.getName()));
        return aElt;
        
    }
    
    /**
     * Make a comment line in the indexlist
     * @param m The module
     * @param cmd The comand
     * @param ie The index entry
     * @return a span element
     */
    private Element makeCommentLine(Module m,PIcommand cmd,ixEntry ie)
    {
       Element commentElt=m.getDoc().createElement("span");
       commentElt.setAttribute("class","comment");       
       if(ie.m_comment.length() > 0)
       {
            commentElt.appendChild(m.getDoc().createTextNode(ie.m_comment));
        }
        return commentElt;
    }
    
    /**
     * Make an index list
     * @param m The module
     * @param cmd The comand
     * @return A vector with all lines in the list, as elements
     */
    public Vector<Element> makeIxElementList(Module m,PIcommand cmd)
    {
        DocumentFragment df=m.getDoc().createDocumentFragment();
        String cat="_all";

        String rootId="_root";
        if(cmd.paramExist(PIcommand.CATEGORY))
            cat=cmd.getValue(PIcommand.CATEGORY);
        if(cmd.paramExist(PIcommand.ROOT))
        {
            rootId=cmd.getValue(PIcommand.ROOT);
            if(m.getScriptHandler().getModuleById(rootId)==null)
            {
                rootId="_root";
            }
        }

        
        // we have all the information we need
        // make elements for each entry
        Vector<Element> result=new Vector<Element>();
        // dummies - init loop
        String lastword="jhbjhvkhv";
        Element thisElt=null;
        
        for(ixEntry ie : m_indexes)
        {
            // correct for locale ?
            String thisword=ie.m_word.toLowerCase();
            if(thisword.compareTo(lastword) != 0)
            {
                // possible new word
                // wrap up the old one, if any
                if(thisElt!=null)
                {
                    result.add(thisElt);
                    thisElt=null;
                }
                // should we use this word
                if( (accessutils.isAnyNameInList(cat.split(","), ie.m_categories.split(",")))
                    &&
                    ((rootId.compareTo("_root")==0) || (ie.m_holder.isNodeAncestor(m.getScriptHandler().getModuleById(rootId))))
                  )
                {
                    thisElt=m.getDoc().createElement("div");
                    if(ie.m_word.compareTo(NO_WORD)!=0)
                    {
                        Element wordElt=m.getDoc().createElement("span");
                        wordElt.setAttribute("class","word");
                        wordElt.appendChild(m.getDoc().createTextNode(ie.m_word));
                        thisElt.appendChild(wordElt);
                    }
                    thisElt.appendChild(makeRefLine(m,cmd,ie));
                    if(ie.m_comment.compareTo(NO_COMMENT)!=0)
                        thisElt.appendChild(makeCommentLine(m,cmd,ie));
                    lastword=thisword;
                }                
            }
            else
            {
                // should we use this word
                if( (thisElt!=null)
                     &&
                     (accessutils.isAnyNameInList(cat.split(","), ie.m_categories.split(",")))
                    &&
                    ((rootId.compareTo("_root")==0) || (ie.m_holder.isNodeAncestor(m.getScriptHandler().getModuleById(rootId))))
                  )
                {
                    thisElt.appendChild(makeRefLine(m,cmd,ie)); 
                    if(ie.m_comment.compareTo(NO_COMMENT)!=0)
                        thisElt.appendChild(makeCommentLine(m,cmd,ie));
                }                
                
            }
        }    
        if(thisElt!=null)
            result.add(thisElt);
        
        return result;
    }
    

    
    private class ixEntry{
        public Module m_holder;
        public String m_word;
        public String m_categories;
        public String m_comment;
        public String m_id;

        public ixEntry(Module m,PIcommand cmd,String id)
        {
            m_holder=m;
            m_word=NO_WORD;
            if(cmd.paramExist(PIcommand.WORD))
                m_word=cmd.getValue(PIcommand.WORD);
            m_categories="_all";
            if(cmd.paramExist(PIcommand.CATEGORY))
                m_categories=cmd.getValue(PIcommand.CATEGORY);
            m_comment=NO_COMMENT;
            if(cmd.paramExist(PIcommand.COMMENT))
                m_comment=cmd.getValue(PIcommand.COMMENT);

            m_id=id;
        }        
    }

}

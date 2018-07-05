package wxt4utils;

import javax.swing.tree.DefaultMutableTreeNode;
import org.w3c.dom.Element;

/**
 *
 * @author Administrator
 */
public class treeNode extends DefaultMutableTreeNode 
{
    static public final String NO_MODULES_FOUND="No Modules Found";
    /** the xml element owned by this treeNoe*/
    Element m_ownedElement=null;
    
    public treeNode(Element elt)
    {
        m_ownedElement=elt;
    }
    
    public String getId()
    {
        if(m_ownedElement==null)
            return "SCRIPT";
        if(m_ownedElement.hasAttribute("id"))
            return m_ownedElement.getAttribute("id");
        return getName();
    }
    
    public String getName()
    {
        if(m_ownedElement==null)
            return "SCRIPT";
        return m_ownedElement.getAttribute("name");
    }
    
    public Element getElement()
    {
        return m_ownedElement;
    }
    
    @Override
    public String toString()
    {
        if((m_ownedElement==null) && (getChildCount()==0))
            return NO_MODULES_FOUND;
        if(m_ownedElement==null)
            return getName();
        if (m_ownedElement.getNodeName().compareTo("modulelist")==0)
            return m_ownedElement.getAttribute("catalog")+" is modulelist";
        else
            return getName();
    }
}

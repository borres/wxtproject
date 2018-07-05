package faceutils;

import javax.swing.tree.DefaultTreeModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Administrator
 */
public class TreeMaker {

    //nodenames in script that we want to insert in tree
    static final String MODULE="module";
    static final String MODULELIST="modulelist";
    
    Document m_Doc=null;
    public TreeMaker(Document dom)
    {
        m_Doc=dom;

    }
    
    
    private void fillTree(TreeNode baseN)
    {
       // make a root
       NodeList nlist=baseN.m_ownedElement.getChildNodes();
        for(int ix=0;ix<nlist.getLength();ix++)
        {
           Node tmp=nlist.item(ix);
           if((tmp.getNodeType()==tmp.ELEMENT_NODE)&&
               (((tmp.getNodeName().equals(MODULE))) ||
                       (tmp.getNodeName().equals(MODULELIST))))
           {
           TreeNode tn=new TreeNode((Element)tmp);
           baseN.add(tn);
           fillTree(tn);
           }
       }
    }

    public DefaultTreeModel getModel() {
        // make a root
        TreeNode root=new TreeNode(null);
        NodeList nlist=m_Doc.getDocumentElement().getChildNodes();
        for(int ix=0;ix<nlist.getLength();ix++)
        {
           Node tmp=nlist.item(ix);
           if((tmp.getNodeType()==tmp.ELEMENT_NODE)&&
               (((tmp.getNodeName().equals(MODULE))) ||
                         (tmp.getNodeName().equals(MODULELIST))))
           {
               TreeNode tn=new TreeNode((Element)tmp);
               root.add(tn);
               fillTree(tn);
           }
        } 
               
        return new DefaultTreeModel(root);

    }
    
    public static DefaultTreeModel getEmptyTree()
    {
        TreeNode root=new TreeNode(null);
        return new DefaultTreeModel(root);
    }
    
    

}
   


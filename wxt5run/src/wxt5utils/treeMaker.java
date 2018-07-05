package wxt5utils;

import javax.swing.tree.DefaultTreeModel;
import org.w3c.dom.Document;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Administrator
 */
public class treeMaker {
    Document m_Doc=null;
    public treeMaker(Document dom)
    {
        m_Doc=dom;

    }
    
    
    private void fillTree(treeNode baseN)
    {
       // make a root
       NodeList nlist=baseN.m_ownedElement.getChildNodes();
        for(int ix=0;ix<nlist.getLength();ix++)
        {
           Node tmp=nlist.item(ix);
           if((tmp.getNodeType()==tmp.ELEMENT_NODE)&&
               (((tmp.getNodeName().compareTo("module")==0)) ||
                       (tmp.getNodeName().compareTo("modulelist")==0)))
           {
           treeNode tn=new treeNode((Element)tmp);
           baseN.add(tn);
           fillTree(tn);
           }
       }
    }

    public DefaultTreeModel getModel() {
        // make a root
        treeNode root=new treeNode(null);
        NodeList nlist=m_Doc.getDocumentElement().getChildNodes();
        for(int ix=0;ix<nlist.getLength();ix++)
        {
           Node tmp=nlist.item(ix);
           if((tmp.getNodeType()==tmp.ELEMENT_NODE)&&
               (((tmp.getNodeName().compareTo("module")==0)) ||
                         (tmp.getNodeName().compareTo("modulelist")==0)))
           {
               treeNode tn=new treeNode((Element)tmp);
               root.add(tn);
               fillTree(tn);
           }
        } 
               
        return new DefaultTreeModel(root);

    }
    
    public static DefaultTreeModel getEmptyTree()
    {
        treeNode root=new treeNode(null);
        return new DefaultTreeModel(root);
    }
    
    

}

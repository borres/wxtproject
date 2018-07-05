package soup;

import java.util.ArrayList;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeVisitor;

/**
 * Usage is:
 * 1  make it
 * 2  involve it
 * 3  ask for getNodes
 * @author Administrator
 */
public class basicNodeVisitor implements NodeVisitor{
    /** all nodes as we vist them*/
    protected ArrayList<Node> nList;    
    
    /**
     * consructor
     */
    public basicNodeVisitor(){
       nList=new ArrayList<Node>();         
    }
    
    /**
     * Get all nodes visited
     * @return list of all visited nodes
     */
    public ArrayList getNodes(){return nList;}   
    
  
    @Override
    public void head(Node node, int depth) {
        nList.add(node);
           
        }
    
    @Override
    public void tail(Node node, int depth) {
        //System.out.println("Exiting tag: " + node.nodeName());
    }
    
}

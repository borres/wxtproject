package soup;

import org.jsoup.nodes.Node;

/**
 *
 * @author Administrator
 */
public class PINodeVisitor extends basicNodeVisitor {
    
    public PINodeVisitor(){
        super();
    }
    
    /**
     * Find all PI's which are hidden as <!--<??>-->
     * Dont do anything with them, just notice them
     * @param node The node opened
     * @param depth depth in the tree
     */
    @Override
    public void head(Node node, int depth) {
        if(node.nodeName().equals("#comment")){
            String T=node.toString().trim();
            if(T.startsWith("<!--?_wxt")){
                nList.add(node);
            }           
        }
     }
    
    
    @Override
    public void tail(Node node, int depth) {
        //System.out.println("Exiting tag: " + node.nodeName());
    }

}

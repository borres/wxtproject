package indexing;

import content.Module;
import java.util.List;
import org.w3c.dom.DocumentFragment;
import utils.PIcommand;

/**
 * IndexItems can be handled in indexes
 * Serves as a link between an indexable item and a module
 * @author bs
 */
public class IndexItem implements Comparable<IndexItem> {

    /** the indexable item held by this indexitem*/
    indexable m_item;
    /** the id identifying this item*/
    String m_id;
    /** the module holding this item */
    Module m_mod;
    /** May be used to differentiate references to same indexable item*/
    String m_category;
    /** May be used to differentiate references to same indexable item*/
    String m_comment;

    /**
     * Constructor
     * @param item The Item we refer
     * @param id The id of this indexitem
     * @param mod The Moduleowning this indexitem
     */
    public IndexItem(indexable item, String id, Module mod) {
        m_item = item;
        m_id = id;
        m_mod = mod;
        m_category = PIcommand.NO_CATEGORY;
        m_comment = PIcommand.NO_COMMENT;
    }

    /**
     * Set the id
     * @param id Teh id
     */
    public void setId(String id) {
        m_id = id;
    }

    /**
     * Set Module
     * @param m The module
     */
    public void setMod(Module m) {
        m_mod = m;
    }

    /**
     * Set the referenced item
     * @param item The item
     */
    public void setItem(indexable item) {
        m_item = item;
    }

    /**
     * Set category
     * @param cat The category
     */
    public void setCategory(String cat) {
        m_category = cat;
    }

    /**
     * Set comment
     * @param com The comment
     */
    public void setComment(String com) {
        m_comment = com;
    }

    /**
     * Get the id
     * @return the id
     */
    public String getId() {
        return m_id;
    }

    /**
     * Get module
     * @return the module
     */
    public Module getMod() {
        return m_mod;
    }

    /**
     * Get item
     * @return the item
     */
    public indexable getItem() {
        return m_item;
    }

    /**
     * get comment
     * @return The comment
     */
    public String getComment() {
        return m_comment;
    }

    /**
     * Get category
     * @return the category
     */
    public String getCategory() {
        return m_category;
    }

    @Override
    public String toString() {
        String t = "IndexItem (" + m_item.getClass() + ") \tid=" + m_id + " \tMod:" + m_mod.getName();
        return t;
    }

    @Override
    public int compareTo(IndexItem o) {
        return m_item.compareTo(o.getItem());
    }

    //-------------------------------------
    // not used
    // should/could we access production here ?
    /**
     * 
     * @param mod
     * @param cmd
     * @return
     */
    public DocumentFragment showInText(Module mod, PIcommand cmd) {
        return m_item.produceIntextElement(mod, cmd);
    }

    /**
     * 
     * @param mod
     * @param cmd
     * @param users
     * @return
     */
    public DocumentFragment showInList(Module mod, PIcommand cmd, List<IndexItem> users) {
        return m_item.produceListElement(mod, cmd, users);
    }
}

package indexing;

import content.Module;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An index of IndexItems, which in turn links to an indexable object
 * Can be used in many ways 
 * @author bs
 */
public class Index {

    /**The list of indexed items*/
    List<IndexItem> Items;

    /**
     * Constructor
     */
    public Index() {
        Items = new ArrayList<IndexItem>();

    }

    /**
     * Attempt to add an indexable item to this indexholder
     * If the id exists it will not be added
     * @param in The indexable item
     * @param id The id of the item
     * @param mod The module owning the item
     * @return The sequence number, index 
     */
    public int add(indexable in, String id, Module mod) {
        // does it exist already in this index ?
        // both id and module must be different if we should add it
        for (int ix = 0; ix < Items.size(); ix++) {
            if ((Items.get(ix).getId().compareTo(id) == 0)
                    && (Items.get(ix).getMod().equals(mod))) {
                return ix;
            }
        }

        // it does not, add it
        Items.add(new IndexItem(in, id, mod));
        return Items.size() - 1;
    }

    /**
     * Access an indexed item
     * @param ix The index
     * @return The Item, or null if out of range
     */
    public IndexItem getItemAt(int ix) {
        if (ix < Items.size() && ix >= 0) {
            return Items.get(ix);
        }
        return null;
    }

    /**
     * Get a list of all items with a certain id
     * @param id The id we are looking for
     * @return The list of items with a certain id
     */
    public List<IndexItem> getAllItemsUsingID(String id) {
        List<IndexItem> ret = new ArrayList<IndexItem>();
        for (IndexItem r : Items) {
            if (r.getId().compareTo(id) == 0) {
                ret.add(r);
            }
        }
        return ret;

    }

    /**
     * Get a list of ids of all items ids
     * @return The list of ids of all used items
     */
    public List<String> getAllReferenceIds() {
        // make a copy to avoid sort troubble
        List<String> ret = new ArrayList<String>(Items.size());
        for (IndexItem r : Items) {
            ret.add(r.getId());
        }
        return ret;
    }

    /**
     * Get all modules using an item with a certain id
     * @param id The id
     * @return a list of modules
     */
    public List<Module> getAllModulesUsing(String id) {
        List<Module> res = new ArrayList<Module>(2);
        for (int ix = 0; ix < Items.size(); ix++) {
            if (Items.get(ix).getId().compareTo(id) == 0) {
                // this is the item
                Module mod = Items.get(ix).getMod();
                // new
                if (!res.contains(mod)) {
                    res.add(mod);
                }

                //old
                /*boolean found=false;
                for(Module m:res)
                found=found || (m==mod);
                if(!found)
                res.add(Items.get(ix).getMod());*/
            }
        }
        return res;
    }

    /**
     * Find all items with a certain module
     * @param m The Module we are looking for
     * @return A list of IndexItems
     */
    public List<IndexItem> getAllItemsForModule(Module m) {
        List<IndexItem> result = new ArrayList<IndexItem>();
        for (IndexItem ix : Items) {
            if (ix.getMod().equals(m)) {
                result.add(ix);
            }
        }
        return result;
    }

    /**
     * Get first module using an item with a certain id
     * @param id The id
     * @return the module with first indexholderentry, null if none
     */
    public Module getFirstModuleUsing(String id) {
        for (int ix = 0; ix < Items.size(); ix++) {
            if (Items.get(ix).getId().compareTo(id) == 0) {
                return Items.get(ix).getMod();
            }
        }
        return null;
    }

    /**
     * Find where a certain item is indexed
     * @param r The item we are looking for
     * @return The index, or -1 if not found
     */
    public int getIndexOfItem(indexable r) {
        for (int ix = 0; ix < Items.size(); ix++) {
            indexable thisr = Items.get(ix).getItem();
            if (thisr.equals(r)) {
                return ix;
            }
        }
        return -1;
    }

    /**
     * Find the module that owns an item with a certain index
     * @param ix The index
     * @return The module, or null if index out of range
     */
    public Module getModuleAtIndex(int ix) {
        try {
            return Items.get(ix).getMod();
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Sort index  before returning ids for each
     * @return The sorted index
     */
    public List<String> getSortedIndexIds() {
        List<String> tmp = getAllReferenceIds();
        Collections.sort(tmp);
        return tmp;
        // old:
        //Collections.sort(Items);
        //return getAllReferenceIds();
    }

    /**
     * Make a sorted copy of the indexed items
     * @return A sorted index
     */
    public List<IndexItem> getSortedIndex() {
        List<IndexItem> tmp = new ArrayList<IndexItem>();
        tmp.addAll(Items);
        Collections.sort(tmp);
        return tmp;
    }

    @Override
    public String toString() {
        String t = "Index: size=" + Items.size();
        for (IndexItem it : Items) {
            t += "\n" + it;
        }
        return t;
    }
}

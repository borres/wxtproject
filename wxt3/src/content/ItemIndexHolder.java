package content;

import java.util.ArrayList;
import java.util.List;

/**
 * Holding an index for used items images and texformulas
 * @author bs
 */
public class ItemIndexHolder
{

    /** the index*/
    private List<oneReferenceIndex> m_references;

    /**
     * Construct an indexholder
     */
    public ItemIndexHolder()
    {
        m_references= new ArrayList<oneReferenceIndex>(10);
    }

    /**
     * Attempt to index a reference  in this indexholder
     * @param id The id of the reference
     * @return the sequence number 
     */
    public int addItemReference(String id,Module m)
    {
        // it is in the reference collection
        // does it exist already in this index ?
        for(int ix=0;ix<m_references.size();ix++)
            if((m_references.get(ix).m_id.compareTo(id)==0)
            &&(m_references.get(ix).m_mod==m))
                return ix;

        // it does not, add it
        m_references.add(new oneReferenceIndex(id,m));
        return m_references.size()-1;
    }


    /**
     * Get a list of ids to all references
     * @return The list of all used reference ids
     */
    public List<String> getAllReferenceIds()
    {
        List<String>ret=new ArrayList<String>(m_references.size());
        for(oneReferenceIndex r: m_references)
            ret.add(r.getId());

        return ret;
    }
    
    /**
     * Get all modules using an image with a certain id
     * @param id The id
     * @return a vector og modules
     */
    public List<Module> getAllModulesUsing(String id)
    {
        List<Module> res=new ArrayList<Module>(2);
        for(int ix=0;ix<m_references.size();ix++)
        {
            if(m_references.get(ix).getId().compareTo(id)==0)
            {
                // this is the image
                Module mod=m_references.get(ix).getModule();
                boolean found=false;
                for(Module m:res)
                    found=found || (m==mod);
                if(!found)
                    res.add(m_references.get(ix).getModule());
            }
        }
        return res;
    }
    /**
     * Get first module using an image with a certain id
     * @param id The id
     * @return the module with forst indexholderentry, null if none
     */
    public Module getFirstModuleUsing(String id)
    {
        for(int ix=0;ix<m_references.size();ix++)
        {
            if(m_references.get(ix).getId().compareTo(id)==0)
                return m_references.get(ix).getModule();
        }
        return null;
    }

    /**
     * Find the module that owns reference with a certain index
     * @param ix The index
     * @return The module
     */
    public Module getModuleAtIndex(int ix)
    {
        try{
            return m_references.get(ix).getModule();
        }
        catch(Exception ex)
        {
            return null;
        }
    }




    //------------------------
    /**
     * Holding one reference index.
     *
     */
    private class oneReferenceIndex
    {
        /** the id that identifies this reference in global reference map
         as defined in definitions*/
        private String m_id;
        /** the module that use the reference. This is not necessary in
         module-local referenceindexes, but necessary in the global referenceindex
         defined in definitions*/
        private Module m_mod;

        public oneReferenceIndex(String id,Module m)
        {
            m_id=id;
            m_mod=m;
        }
        public String getId()
        { return m_id;}
        public Module getModule()
        { return m_mod;}
    }

}

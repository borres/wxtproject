package content;

import java.util.Vector;

/**
 * Holding used formulas
 * 
 * @author bs
 */
public class FormulaIndexHolder
{
   /**The actual definitions*/
    private Definitions m_def;
    /** the index*/
    private Vector<oneReferenceIndex> m_references;

    /**
     * Construct an indexholder
     * @param d The actual definitions object
     */
    public FormulaIndexHolder(Definitions d)
    {
        m_def=d;
        m_references= new Vector<oneReferenceIndex>(10);
    }

    /**
     * Attempt to index a reference  in this indexholder
     * @param id The id of the reference
     * @return the sequence number or -1 if the reference does not exist
     */
    public int addFormulaReference(String id,Module m)
    {

        if(m_def.getFormula(id)==null)
            return -1;

        // it is in the reference collection
        // does it exist already in this index ?
        for(int ix=0;ix<m_references.size();ix++)
            if((m_references.elementAt(ix).m_id.compareTo(id)==0)
            &&(m_references.elementAt(ix).m_mod==m))
                return ix;

        // it does not, add it
        m_references.add(new oneReferenceIndex(id,m));
        return m_references.size()-1;
    }


    /**
     * Get a list of ids to all references
     * @return The list of all used reference ids
     */
    public Vector<String> getAllReferenceIds()
    {
        Vector<String>ret=new Vector<String>(m_references.size());
        for(oneReferenceIndex r: m_references)
            ret.add(r.getId());

        return ret;
    }

    /**
     * Find where a certain reference is indexed
     * @param r The refrerence we are looking for
     * @return The index, or -1 if not found
     */
    public int theIndexOfImage(Formula fo,int startix)
    {
        for(int ix=startix;ix<m_references.size();ix++)
        {
            Imagex thisIm=m_def.getImage((m_references.elementAt(ix).getId()));
            if (thisIm.getID().compareTo(fo.getID())==0)
                return ix;
        }
        return -1;
    }

    /**
     * Get all modules using a formula with a certain id
     * @param id The id
     * @return a vector og modules
     */
    public Vector<Module> getAllModulesUsing(String id)
    {
        Vector<Module> res=new Vector<Module>(2);
        for(int ix=0;ix<m_references.size();ix++)
        {
            if(m_references.elementAt(ix).getId().compareTo(id)==0)
            {
                // this is the formula
                Module mod=m_references.elementAt(ix).getModule();
                boolean found=false;
                for(Module m:res)
                    found=found || (m==mod);
                if(!found)
                    res.add(m_references.elementAt(ix).getModule());
            }
        }
        return res;
    }
    /**
     * Get first module using a formula with a certain id
     * @param id The id
     * @return the module with forst indexholderentry, null if none
     */
    public Module getFirstModuleUsing(String id)
    {
        for(int ix=0;ix<m_references.size();ix++)
        {
            if(m_references.elementAt(ix).getId().compareTo(id)==0)
                return m_references.elementAt(ix).getModule();
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
            return m_references.elementAt(ix).getModule();
        }
        catch(Exception ex)
        {
            return null;
        }
    }


    /**
     * Sort index according to type.
     * Harvard means sorted on key
     * IEEE and SIMPLE means that we keep the order the indexes was registered
     * that is the order they were encountered in the text
     * @return The sorted index
     */
    public Vector<String> getUniqueReferenceIds()
    {
        Vector<String>ret=new Vector<String>(m_references.size());
        for(oneReferenceIndex r: m_references)
        {
            boolean found=false;
            for(String s:ret)
                found=found ||(r.getId().compareTo(s)==0);
            if (!found)
                ret.add(r.getId());
        }
        return ret;
    }


    //------------------------
    /**
     * Holding one reference index.
     *
     */
    public class oneReferenceIndex
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
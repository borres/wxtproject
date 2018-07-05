package content;

import java.util.Vector;

/**
 * Holding a list of reference indexes
 * as keys (id) to global referencemap in definitions
 * Used to hold globally used references 
 * and locally (module) used references
 * @author borres
 */
public class ReferenceIndexHolder 
{
    /**The actual definitions*/
    private Definitions m_def;
    /** the index*/
    private Vector<oneReferenceIndex> m_references;

    /**
     * Construct an indexholder
     * @param d The actual difinitions object
     */
    public ReferenceIndexHolder(Definitions d)
    {
        m_def=d;
        m_references= new Vector<oneReferenceIndex>(10);
    }
    
    /**
     * Attempt to index a reference  in this indexholder
     * @param id The id of the reference
     * @return the sequence number or -1 if the reference does not exist
     */
    public int addReference(String id,Module m)
    {

        if(m_def.getReference(id)==null)
            return -1;
        
        // it is in the reference collection
        // does it exist already in this index ?
        for(int ix=0;ix<m_references.size();ix++)
            if(m_references.elementAt(ix).m_id.compareTo(id)==0)
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
    public int theIndexOfReference(Reference r,int startix)
    {
        for(int ix=startix;ix<m_references.size();ix++)
        {
            Reference thisr=m_def.getReference(m_references.elementAt(ix).getId());
            if (thisr.getid().compareTo(r.getid())==0)
                return ix;    
        }
        return -1;
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
    public Vector<String> getSortedReferenceIds()
    {
       // if not harvard the sequence is ok as it is registered
       if(m_def.getOption(Definitions.REFERENCE_FORM).compareTo(Definitions.HARVARD)!=0)
           return getAllReferenceIds();

       // if harvard we must sort according to reference comparison
       Vector<String>ret=getAllReferenceIds();

       // todo: some smarter sort
       boolean ok=false;
        while(!ok)
        {
            ok=true;
            for(int ix=0;ix<ret.size()-1;ix++)
            {
                Reference r1=m_def.getReference(ret.elementAt(ix));
                Reference r2=m_def.getReference(ret.elementAt(ix+1));
                if(r1.compareTo(r2)>0)
                {
                    // swap them
                    ok=false;
                    String tmp=ret.elementAt(ix);
                    ret.add(ix+2, tmp);
                    ret.removeElementAt(ix);
                }
            }
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

package content;

import org.w3c.dom.Element;

/**
 * This class wraps a an Element with a level. It is used
 * when breaking a list of elements into columns
 * 
 */
public class elementWrapper {
    
    /** the wrapped elements */
    private Element m_elt;
    /** the level */
    private int m_level;
    /** flagging if we should break column before this element*/
    private boolean m_columnBreakBefore=false;
    
    /**
     * Construct an elementwrapper.
     * 
     * @param elt The Element
     * @param level The level
     */
    public elementWrapper(Element elt,int level)
    {
        m_elt=elt;
        m_level=level;
    }
    
    /** 
     * Get the Element.
     * 
     * @return The wrapped element
     */
    public Element getElement()
    {
        return m_elt;
    }
    
    /**
     * Get the level of this element.
     * 
     * @return The level
     */
    public int getlevel()
    {
        return m_level;
    }
    
    
    /**
     * Find out if we should have a column break before this element.
     * 
     * @return True if we should break, false otherwise
     */
    public boolean getBreakBefore()
    {
        return m_columnBreakBefore;
    }
    
     /**
      * Set the break condition.
      * 
      * @param b True if we want a break, false otherwise
      */
     public  void setBreakBefore(boolean b)
    {
        m_columnBreakBefore=b;
    }

}

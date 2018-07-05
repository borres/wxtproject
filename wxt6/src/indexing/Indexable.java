package indexing;

import content.Module;
import java.util.List;
import org.w3c.dom.DocumentFragment;
import utils.PIcommand;

/**
 * All objects that will appear in indexes must implement this interface
 * Current implementations: 
 * Image
 * SimpleReference, IEEEReference, HarvardReference 
 * Formula, GoogleTexFormula, LaTexFormula, MathMLFormula, ImageFormula
 * Word 
 * Frag, Fragment, AuthorFragment, SummaryFragment
 * @author bs
 */
public interface Indexable extends Comparable<Indexable> {

    /**
     * Produce a documentfragment describing this Indexable item in the running text
     * @param mod The Module requesting this
     * @param cmd The PIcommand describlig the request
     * @return A documentFragment, always
     */
    public DocumentFragment produceIntextElement(Module mod, PIcommand cmd);

    /**
     * Produce a documentfragment describing this Indexable item in a list
     * @param mod The Module requesting this
     * @param cmd The PIcommand describlig the request
     * @param users The using IndexItems
     * @return A documentFragment, always
     */
    public DocumentFragment produceListElement(Module mod, PIcommand cmd, List<IndexItem> users);

    /**
     * Is this item made during build or as definition
     * @return True if made during build, false otherwise
     */
    public boolean madeOnTheFly();

    /**
     * Get the id
     * @return the id
     */
    public String getId();

    /**
     * Get the compare value we can use
     * @return The comparevalue
     */
    public String getCompareValue();
}

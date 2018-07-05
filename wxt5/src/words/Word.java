package words;

import content.Module;
import indexing.IndexItem;
import indexing.indexable;
import java.net.URI;
import java.util.List;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import utils.PIcommand;
import utils.accessutils;

/**
 * An indexable word
 * @author bs
 */
public class Word implements indexable {

    /** used to make a local htmlpagfe-address, a fragment*/
    public static String FRAGMENTMARKER = "ixword";
    /** the word to index*/
    private String m_word;
    /** possible category */
    private String m_category;

    /**
     * Constructor
     * @param m The module reporting the word
     * @param cmd The PIcommand describing the word
     */
    public Word(Module m, PIcommand cmd) {
        m_word = PIcommand.NO_WORD;


        if (cmd.paramExist(PIcommand.WORD)) {
            m_word = cmd.getValue(PIcommand.WORD);
        }
        m_category = PIcommand.NO_CATEGORY;
        if (cmd.paramExist(PIcommand.CATEGORY)) {
            m_category = cmd.getValue(PIcommand.CATEGORY);
        }
    }

    /**
     * Get the word
     * @return the word
     */
    public String getWord() {
        return m_word;
    }

    /**
     * get the category
     * @return the category
     */
    public String getCategory() {
        return m_category;
    }

    @Override
    public String getId() {
        return m_word;
    }

    /**
     * Produce this word in running text
     * @param m The module that has requested this
     * @param cmd The PIcommand describing the display
     * @return An XML element
     */
    @Override
    public DocumentFragment produceIntextElement(Module m, PIcommand cmd) {
        DocumentFragment df = m.getDoc().createDocumentFragment();
        String word = " ";
        if (cmd.paramExist(PIcommand.WORD)) {
            word = cmd.getValue(PIcommand.WORD);
        }

        String markWord = word;
        if (cmd.paramExist(PIcommand.MARKWORD)) {
            markWord = cmd.getValue(PIcommand.MARKWORD);
        }

        if (markWord.compareToIgnoreCase(PIcommand.NONE) != 0) {

            // produce the element holding the word marked with id
            Element welt = m.getDoc().createElement("span");
            welt.setAttribute("id", FRAGMENTMARKER + m_word);
            welt.setAttribute("class", cmd.getCommandStyleName());
            welt.appendChild(m.getDoc().createTextNode(markWord));
            df.appendChild(welt);
            return df;
            //return welt;
        } else {
            Element empty = m.getDoc().createElement("span");
            df.appendChild(empty);
            return df;
            //return empty;
        }
    }

    /**
     * Produce this word in a list, an indextable
     * will produce the word and a (sub)list of all occurances
     * @param mod The module that has requested this
     * @param cmd The PIcommand describing the display
     * @param users The list of indexItems that will appear in the sublist
     * @return An XML element
     */
    @Override
    public DocumentFragment produceListElement(Module mod, PIcommand cmd, List<IndexItem> users) {
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        Element wrapper = mod.getDoc().createElement("div");
        if (m_word.compareTo(PIcommand.NO_WORD) != 0) {
            Element wordElt = mod.getDoc().createElement("span");
            wordElt.setAttribute("class", "word");
            wordElt.appendChild(mod.getDoc().createTextNode(m_word));
            wrapper.appendChild(wordElt);
        }
        for (IndexItem ie : users) {
            //System.out.println("Users: "+ie);
            Word w = (Word) ie.getItem();
            //System.out.println(w);
            wrapper.appendChild(makeRefLine(mod, cmd, ie));
            if (ie.getComment().compareTo(PIcommand.NO_COMMENT) != 0) {
                wrapper.appendChild(makeCommentLine(mod, cmd, ie));
            }
        }
        df.appendChild(wrapper);
        return df;
        //return wrapper;
    }

    /**
     * Make a reference line in the indexlist
     * @param m The module
     * @param cmd The comand
     * @param ie The index entry
     * @return A a-element with ref to the indexed word on the indexed module
     */
    private Element makeRefLine(Module m, PIcommand cmd, IndexItem ie) {
        Element aElt = m.getDoc().createElement("a");
        URI relUri;
        try {
            relUri = accessutils.makeRelativeURI(m.getAbsoluteUri(), ie.getMod().getAbsoluteUri());
        } catch (Exception e) {
            relUri = ie.getMod().getAbsoluteUri();
        }
        if (!ie.getId().isEmpty()) {
            aElt.setAttribute("href", relUri.toString() + "#" + FRAGMENTMARKER + m_word);
        } else {
            aElt.setAttribute("href", relUri.toString());
        }
        aElt.setAttribute("title", ie.getMod().getDescription());
        String refName = ie.getMod().getName();
        aElt.appendChild(m.getDoc().createTextNode(refName));
        return aElt;

    }

    /**
     * Make a comment line in the indexlist
     * @param m The module
     * @param cmd The comand
     * @param ie The index entry
     * @return a span element
     */
    private Element makeCommentLine(Module m, PIcommand cmd, IndexItem ie) {
        Element commentElt = m.getDoc().createElement("span");
        commentElt.setAttribute("class", "comment");
        if (ie.getComment().compareTo(PIcommand.NO_COMMENT) != 0) {
            commentElt.appendChild(m.getDoc().createTextNode(ie.getComment()));
        }
        return commentElt;
    }

    /**
     * Indexed words are allways made on the fly, as they are detected
     * @return true
     */
    @Override
    public boolean madeOnTheFly() {
        return true;
    }

    @Override
    public int compareTo(indexable o) {
        return getCompareValue().compareTo(o.getCompareValue());
    }

    @Override
    public String toString() {
        String t = "Word: " + m_word + " \tcategory: " + m_category;
        return t;
    }

    @Override
    public String getCompareValue() {
        return m_word;
    }
}

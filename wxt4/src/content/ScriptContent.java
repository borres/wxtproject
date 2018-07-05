package content;

import java.net.URI;
import java.util.HashMap;
import org.w3c.dom.Element;
import utils.PIcommand;
import utils.accessutils;

/**
 * This class describes a content element as included in the script:
 * XMLContent,TextContent,ODFContent,WIKIContent or DBContent
 * @author borres
 */
public class ScriptContent {

    /** the actual type*/
    String m_type;
    /** The absolute location for this content file*/
    protected URI m_absoluteUri;
    /** An absolute location for alternativ source*/
    protected URI m_absoluteBackupUri;
    /** The effective location, one of the above*/
    protected URI m_absoluteEffectiveUri;
    /** The scriptelement where it is described*/
    protected Element m_scriptElement;
    /** The owning module */
    protected Module m_Owner;
    /** Transformation string as in attribute in element */
    protected String m_transformationString;
    /** Possible available transformation, used before extraction */
    protected Transformation m_transformation;
    /** parameters used in the transformation */
    protected HashMap<String, String> m_transParameters;
    /** id, used when importing to template to filter PI production*/
    protected String m_id;
    /** only for db */
    protected String m_connectionString;

    /**
     * Constructing a ScriptContent with empty, initialized fields
     */
    public ScriptContent() {
        m_type = null;
        m_absoluteUri = null;
        m_absoluteBackupUri = null;
        m_absoluteEffectiveUri = null;
        m_scriptElement = null;
        m_Owner = null;
        m_transformation = null;
        m_transParameters = new HashMap<String, String>();
        m_transformationString = null;
        m_id = "_NO_ID";
        m_connectionString = null;
    }

    /**
     * Constructing a ScriptContent object from a script element.
     * Called when a module is established.
     * Note that the element is also subject to validation as part of the script
     * with location as the only required attribute
     *
     * @param celt The script element describing the object
     * @param owner The Module that owns this content
     * @throws Exception  When construction fails
     */
    public ScriptContent(Element celt, Module owner)
            throws Exception {
        this();
        m_scriptElement = celt;
        m_Owner = owner;

        //-------------------------
        // which type
        m_type = celt.getTagName().trim();
        if ((m_type.compareTo(Module.TXTCONTENT) != 0)
                && (m_type.compareTo(Module.XMLCONTENT) != 0)
                && (m_type.compareTo(Module.WIKICONTENT) != 0)
                && (m_type.compareTo(Module.ODFCONTENT) != 0)
                && (m_type.compareTo(Module.DBCONTENT) != 0)) {
            // should not happen
            m_Owner.getReporter().pushSimpleMessage("\tWrong content type: " + m_type);
            throw new Exception("should not happen");
        }

        //--------------------------
        // location , which is mandatory i all but DBContent
        if (m_scriptElement.hasAttribute(PIcommand.LOCATION)) {
            String tmp = m_scriptElement.getAttribute(PIcommand.LOCATION);
            tmp = m_Owner.getDefinitions().substituteFragments(tmp);
            tmp = accessutils.fixUriParameterEntities(tmp);
            try {
                m_absoluteUri = accessutils.makeAbsoluteURI(tmp, m_Owner.getAnchorCat());
            } catch (Exception e) {
                m_Owner.getReporter().pushMessage("cannot_make_abs_uri", e.getMessage());
                throw new Exception(e.getMessage());
            }
        } else if ((m_type.compareTo(Module.DBCONTENT) != 0)) {
            // should not happen
            throw new Exception("missing location");
        }

        //-------------------------
        // backup location is optional
        if (m_scriptElement.hasAttribute(PIcommand.BACKUPLOCATION)) {
            String tmp = m_scriptElement.getAttribute(PIcommand.BACKUPLOCATION);
            tmp = m_Owner.getDefinitions().substituteFragments(tmp);
            tmp = accessutils.fixUriParameterEntities(tmp);
            try {
                m_absoluteBackupUri = accessutils.makeAbsoluteURI(tmp, m_Owner.getAnchorCat());
            } catch (Exception e) {
                // report attempt to  build an alternative uri failed
                m_Owner.getReporter().pushMessage("cannot_make_abs_uri", e.getMessage());
                m_absoluteBackupUri = null;
            }
        }

        //--------------------------
        // possible transformation
        if (m_scriptElement.hasAttribute(PIcommand.TRANSFORMATION)) {
            String tmp = m_scriptElement.getAttribute(PIcommand.TRANSFORMATION).trim();
            tmp = m_Owner.getDefinitions().substituteFragments(tmp);
            m_transformationString = tmp;
            m_transformation = Transformation.findTransformation(tmp, m_Owner);
            if (m_transformation == null) {
                // unknown and uproducable transformatuin
                m_Owner.getReporter().pushMessage("bad_transformation", tmp);
                throw new Exception();
            }
            try {
                m_transParameters = accessutils.unpackTransformationParameters(tmp);
            } catch (Exception e) {
                m_Owner.getReporter().pushMessage("bad_transformation", tmp);
                throw new Exception();
            }
        }

        //---------------------
        // possible connectionstring
        if (m_scriptElement.hasAttribute(PIcommand.CONNECT)) {
            m_connectionString = m_scriptElement.getAttribute(PIcommand.CONNECT);
        }

        //---------------------------
        // possible id
        if (m_scriptElement.hasAttribute(PIcommand.ID)) {
            m_id = m_scriptElement.getAttribute(PIcommand.ID);
        }

        //------------------------------
        // handle backup and efficient URL
        if (accessutils.resourceExists(m_absoluteUri)) {
            m_absoluteEffectiveUri = m_absoluteUri;
            return;
        }
        if ((m_absoluteBackupUri != null) && accessutils.resourceExists(m_absoluteBackupUri)) {
            m_absoluteEffectiveUri = m_absoluteBackupUri;
            m_Owner.getReporter().pushMessage("using_backup", m_absoluteBackupUri.toString());
            return;
        }
        if (m_absoluteUri != null) {
            m_Owner.getReporter().pushMessage("no_source_available", m_absoluteUri.toString());
        }
    }

    /**
     * Get the type of content
     * @return The type
     */
    public String getType() {
        return m_type;
    }

    /**
     * Get the id
     * @return The id
     */
    public String getId() {
        return m_id;
    }

    /**
     * Get the transformation if any
     * @return The transformation or null
     */
    public Transformation getTransformation() {
        return m_transformation;
    }

    /**
     * Get the complete transformation string
     * @return Return the transformation with parameters
     */
    public String getTransformationString() {
        return m_transformationString;
    }

    /**
     * Get the parameters of the transformation
     * @return Theparameteres as a map
     */
    public HashMap<String, String> getTransformationParams() {
        return m_transParameters;
    }

    /**
     * Get the location
     * @return the absolute location as an uri
     */
    public URI getAbsoluteUri() {
        return m_absoluteUri;
    }

    /**
     * Get backup if any
     * @return The backup location
     */
    public URI getAbsoluteBackupUri() {
        return m_absoluteBackupUri;
    }

    /**
     * get the effective uri
     * @return The uri to use, possible backup
     */
    public URI getAbsoluteEffectiveUri() {
        return m_absoluteEffectiveUri;
    }

    /**
     * Will we use backup
     * @return Tru if backup, false otherwise
     */
    public boolean usingBackup() {
        return m_absoluteBackupUri == m_absoluteEffectiveUri;
    }

    /**
     * Get connection string if dbcontent
     * @return A possible connection string, null otherwise
     */
    public String getConnectionString() {
        return m_connectionString;
    }
}

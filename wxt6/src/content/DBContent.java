package content;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.NodeList;
import reporting.reporter;
import utils.PIcommand;
import utils.accessutils;
import utils.encoderutils;
import wxtresources.resourceHandler;
import xmldom.domer;

/**
 * producing content from a database
 * @author  bs
 * 
 */
public class DBContent extends Content {
    // in case we need them
    //static final String MYSQLDRIVERCLASS="org.gjt.mm.mysql.Driver";
    //static final String POSTGRESSDRIVERCLASS="org.postgresql.Driver";

    /** defining the root-element of the resulting  XML*/
    // this is also a name(constant) in the std transformation to table
    static final String ROOT = "root";
    /** defining the rootnode for one sqlString- select query*/
    static final String QUERY = "query";
    /** defining a node for another sqlString job*/
    static final String JOB = "job";
    /** defining a record*/
    static final String RECORD = "record";
    /** the connection string */
    String m_ConnectionString = null;

    /** Constructing a DBContent
     * @param owner The module that request the content
     * @param cmd The PIcommand describing the request
     * @throws an exception id no connectionstring
     */
    DBContent(Module owner, PIcommand cmd)
            throws Exception {
        m_Cmd = cmd;
        m_Owner = owner;
        m_encoding = owner.getEncoding();
        if (cmd.paramExist(PIcommand.ENCODING)) {
            m_encoding = cmd.getValue(PIcommand.ENCODING);
        }

        // pick up mandatory connection string
        m_ConnectionString = null;
        if (cmd.paramExist(PIcommand.CONNECT)) {
            m_ConnectionString = cmd.getValue(PIcommand.CONNECT);
        }
        if (m_ConnectionString == null) {
            throw new Exception(reporter.getBundleString("missing_connect_string"));
        }
    }

    /**
     * Establish and extract content according to parameters in PIcommand
     * 
     * @param mod The module that has requested this content
     * @param cmd The PIcommand that describes what we want
     * @return a DocumentFragment with the extracted content as a textnode or a list of nodes
     */
    @Override
    public DocumentFragment getContent(Module mod, PIcommand cmd) {
        DocumentFragment df = mod.getDoc().createDocumentFragment();
        // get parameters: connection, transformation, targeturi, sqlString, xpath
        /*
        <?_wxt importdb 
        connection="jdbc:mysql://frigg.hiof.no/vin?user=student&password=student" 
        sqlString="use vin;
        select type,name,dice from wines where country = 'Spania';
        select type,description,dice from wines where country = 'Frankrike';"
        encoding="ISO-8859-1"
        targetlocation="dbdump.xml" 
        xpath="//div[@id='root']"
        transformation="vindb1(dice='1')"?>
         */

        String targeturi = null;
        String sqlString = null;
        URI sqlUri = null;
        String trans = null;
        String xpath = null;
        String source_encoding = mod.getEncoding();
        String target_encoding = null;
        boolean parse = false;

        if (cmd.paramExist(PIcommand.TARGETLOCATION)) {
            targeturi = cmd.getValue(PIcommand.TARGETLOCATION);
        }

        if (cmd.paramExist(PIcommand.TRANSFORMATION)) {
            trans = cmd.getValue(PIcommand.TRANSFORMATION);
        }
        if (cmd.paramExist(PIcommand.XPATH)) {
            xpath = cmd.getValue(PIcommand.XPATH);
        }
        if ((cmd.paramExist(PIcommand.PARSE))
                && (cmd.getValue(PIcommand.PARSE).compareTo(PIcommand.YES) == 0)) {
            parse = true;
        }
        if (cmd.paramExist(PIcommand.ENCODING)) {
            source_encoding = cmd.getValue(PIcommand.ENCODING);
        }

        target_encoding = mod.getEncoding();


        if (cmd.paramExist(PIcommand.SQL)) {
            sqlString = cmd.getValue(PIcommand.SQL);
        }
        // if not attribute sqlString set, we use sqlfile
        if ((sqlString == null) && (cmd.paramExist(PIcommand.SQLFILE))) {
            String sqlfile = cmd.getValue(PIcommand.SQLFILE);
            sqlfile = mod.getDefinitions().substituteFragments(sqlfile);
            try {
                sqlUri = accessutils.makeAbsoluteURI(sqlfile, mod.getCatalog());
                //sqlString=accessutils.getTextFile(sqlUri,source_encoding);
                sqlString = accessutils.getBOMSafeTextFile(sqlUri, source_encoding);
            } catch (Exception ex) {
                mod.getReporter().pushMessage("cannot_read_sql_file", cmd.getOriginalData());
                return df;
            }
        }

        // we need connectionstring and sql to do anything at all
        if ((m_ConnectionString == null) || (sqlString == null)) {
            mod.getReporter().pushMessage("bad_processing_instruction", cmd.getOriginalData(), " missing connect");
            return df;
        }

        // find the transformation
        Transformation transObj = null;
        HashMap<String, String> transParameters = null;

        if ((trans != null) && (trans.compareToIgnoreCase(PIcommand.TABLE) == 0)) {
            // it is a std table transformation
            // we will use the standard to-table transformation that
            // is distributed with wxt
            String transString = resourceHandler.getDBaseToTable();
            try {
                String tmp = mod.getDefinitions().getTempCatalog();
                Transformation to = new Transformation(PIcommand.TABLE, transString, tmp);
                // register it
                mod.getDefinitions().addTransformation(PIcommand.TABLE, to);
            } catch (Exception tx) {
                mod.getReporter().pushMessage("could_not_do_table", cmd.getOriginalData());
                trans = null;
                return df;
            }
        }

        if (trans != null) {
            // we have a transformation, handmade or std to-table
            transObj = Transformation.findTransformation(trans, mod);
            if (transObj == null) {
                mod.getReporter().pushMessage("bad_transformation", trans);
                return df;
            }

            try {
                transParameters = accessutils.unpackTransformationParameters(trans);
            } catch (Exception e) {
                mod.getReporter().pushMessage("bad_transformation", trans);
                return df;
            }
        }


        //------------------------------------------------- 
        // we have what we need, start working
        // do the database access and receive the result as a formatted string
        String xmlTxt = accessDatabase(m_ConnectionString, sqlString, mod.getReporter(), parse);
        if ((xmlTxt == null) || (xmlTxt.isEmpty())) {
            mod.getReporter().pushMessage("no_result_from_query");
            return df;
        }
        // should we reencode the text ????
        // or is it done automatically when we move fragment ???

        // prepare the result as XML with ROOT as documentnode if
        // it is not already complete XML.
        // ROOT=root is a constant in std totabel transformation
        // May be the case if access a xml-fragment for parse
        if (!xmlTxt.trim().startsWith("<?xml")) {
            xmlTxt = "<?xml version=\"1.0\" encoding=\"" + target_encoding + "\"?>\n<" + ROOT + ">" + xmlTxt + "\n</" + ROOT + ">";
        }

        // save the xml so we can inspect it
        if (targeturi != null) {
            // correct the targeturi
            targeturi = mod.getDefinitions().substituteFragments(targeturi);
            URI theUri = null;
            try {
                theUri = accessutils.makeAbsoluteURI(targeturi, mod.getCatalog());
            } catch (Exception e) {
                mod.getReporter().pushMessage("bad_processing_instruction", cmd.getOriginalData(), e.getMessage());
                return df;
            }

            if ((source_encoding != null) && (!encoderutils.sameEncoding(source_encoding, target_encoding))) {
                xmlTxt = encoderutils.reEncode(xmlTxt, source_encoding, target_encoding, false);
            }

            accessutils.saveTFile(theUri, xmlTxt, target_encoding);
        }


        Document resDom = null;
        // transformation
        if (transObj != null) {
            try {
                resDom = domer.makeTransformedDomFromString(xmlTxt,false, transObj.getabsoluteURI(),
                        transParameters, target_encoding);
            } catch (Exception ex) {
                mod.getReporter().pushMessage("error_producing_db_content", ex.getMessage());
            }
        } else if (parse) // parse it 
        {
            try {
                // dont block tidy in the attempts
                resDom = domer.makeDomFromString(xmlTxt, false, target_encoding);
            } catch (Exception ex) {
                mod.getReporter().pushMessage("error_producing_db_content", ex.getMessage());
            }
        }


        // pick out what we want
        if (resDom != null) {
            NodeList nlist = null;
            try {
                if (xpath == null) {
                    nlist = resDom.getDocumentElement().getChildNodes();
                } else {
                    nlist = domer.performXPathQuery(resDom, xpath);
                }

                // fill up the fragment
                for (int ix = 0; ix < nlist.getLength(); ix++) {
                    df.appendChild(mod.getDoc().importNode(nlist.item(ix), true));
                }
            } catch (Exception ex) {
                mod.getReporter().pushMessage("error_producing_db_content", ex.getMessage());
            }
        } else {
            df.appendChild(mod.getDoc().createTextNode(xmlTxt));
        }

        // return whatever
        return df;
    }

    /**
     * Prepare an XML-fragment describing the result of a query.
     * Query results are considered as text and are wrapped in a XML-structre
     * 
     * @param rSet The resultset
     * @param sql The SQL statement executed
     * @param count The index of the query
     * @return The string produced
     */
    protected String appendResult(ResultSet rSet, String sql, int count) {
        StringBuffer restable = new StringBuffer("");

        try {
            int col = (rSet.getMetaData()).getColumnCount();

            while (rSet.next()) {
                restable.append("\n\t<" + RECORD + ">");
                for (int pos = 1; pos <= col; pos++) {
                    //String elementType=rs.getMetaData().getColumnTypeName(pos);
                    String elementName = rSet.getMetaData().getColumnLabel(pos);
                    // or getColumnName?

                    // since this is going to be target for
                    // transformation, we replace the common enities:&,<,>,',"                  
                    String tmp = rSet.getString(pos);
                    tmp = accessutils.setCommonEntities(tmp);
                    // use it
                    restable.append("\n\t\t<").append(elementName).append(">").append(tmp).append("</").append(elementName).append(">");
                }
                restable.append("\n\t</" + RECORD + ">");
            }
        } catch (Exception e) {
            restable = new StringBuffer("\n\t<" + RECORD + "/>");
        }
        String resID = "" + count;
        return "\n<" + QUERY + " id=\"" + resID + "\">"
                + "\n<sql>" + accessutils.setCommonEntities(sql) + "</sql>\n"
                + restable.toString() + "\n"
                + "</" + QUERY + ">";
    }

    /**
     * Prepare an XML-fragment describing the result of a query.
     * Query results are parsable, that is xml-code
     * The fragments fetched are simply concatenated
     * 
     * @param rSet The resultset
     * @return The string produced
     */
    protected String appendParsableResult(ResultSet rSet) {
        StringBuffer restable = new StringBuffer("");

        try {
            int col = (rSet.getMetaData()).getColumnCount();
            while (rSet.next()) {
                for (int pos = 1; pos <= col; pos++) {
                    restable.append(rSet.getString(pos));
                }
            }
        } catch (Exception e) {
            restable = new StringBuffer("\n\t<" + RECORD + "/>");
        }
        return restable.toString();
    }

    /**
     * Produce report to be appended to the overall reportstring,
     * as a simple comment
     * 
     * @param sql The SQL executed
     * @param result Reported result from the query
     * @return The string built
     */
    protected String appendSimpleResult(String sql, String result) {
        return "\n<" + JOB + ">" + "\n<!-- Result from: " + sql + "  -->\n"
                + "result:" + result + "\n"
                + "</" + JOB + ">";

    }

    /** 
     * Access the database and perform all SQL sentences.
     * Build resulting XML-fragments as we go.
     * 
     * @param connection The connection string
     * @param sql The SQL-sentence
     * @param bparse Wheter we should parse result or not
     * @param rep The reporter
     * @return the number of successfull queries, -1 is no connection
     */
    protected String accessDatabase(String connection, String sql,
            reporter rep, boolean bparse) {
        // Creating a new connection object to the database 
        Connection dbConn = null;
        Statement query = null;
        ResultSet recordSet = null;
        // try to extract databasetype from a complete connectionstring:
        // jdbc:mysql://localhost/football?user=root&password=hemmelig"

        try {
            dbConn = DriverManager.getConnection(connection);
        } catch (Exception e) {
            rep.pushMessage("could_not_connect_to_db", e.getMessage());
            return null;
        }
        String resultTxt = "";

        // do the querys and get results 
        String SQLList[] = sql.split(";");
        int index = 1;
        String thisSQL = null;
        try {
            //Creating a statement object from the connection object dbConn.
            //Then creating a resultset from the statement object.      

            // split the SQL-query into sentences and run them separately
            for (int i = 0; i < SQLList.length; i++) {
                thisSQL = SQLList[i].trim();
                if ((thisSQL == null) || (thisSQL.isEmpty())) {
                    continue;
                }
                query = dbConn.createStatement();
                if (thisSQL.toUpperCase().startsWith("SELECT")) {
                    recordSet = query.executeQuery(thisSQL);
                    if (bparse) {
                        resultTxt += appendParsableResult(recordSet);
                    } else {
                        resultTxt += appendResult(recordSet, thisSQL, index++);
                    }
                } else if ((thisSQL.toUpperCase().startsWith("UPDATE"))
                        || (thisSQL.toUpperCase().startsWith("INSERT"))
                        || (thisSQL.toUpperCase().startsWith("DELETE"))) {
                    int resultcount = query.executeUpdate(thisSQL);
                    rep.pushSimpleMessage("" + resultcount + " rows affected");
                } else {
                    query.execute(thisSQL);
                }

            }
            return resultTxt;
        } catch (SQLException se) {
            rep.pushMessage("sql_exception", se.getMessage() + "\n" + thisSQL);
            //se.printStackTrace(System.out);
            return null;
        } finally {
            try {
                if (recordSet != null) {
                    recordSet.close();
                }
                if (query != null) {
                    query.close();
                }
                if (dbConn != null) {
                    dbConn.close();
                }
            } catch (SQLException sex) {
            }
        }
    }

    @Override
    public String toString() {
        return "\n\tDBContent";
    }
}

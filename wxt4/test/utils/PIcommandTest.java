/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utils;

import content.Definitions;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author borres
 */
public class PIcommandTest {

    public PIcommandTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getCommand method, of class PIcommand.
     */
    @Test
    public void testGetCommand() {
        System.out.println("getCommand");
        PIcommand instance;
        try{
            instance = new PIcommand("importxml location=\"hallo.xml\" xpath=\"\\root\"",new Definitions());
        }
        catch(Exception e)
        {
            fail("Could not establish PIcommand");
            return;
        }
        assertEquals("importxml", instance.getCommand());

    }

    /**
     * Test of getValue method, of class PIcommand.
     */
    @Test
    public void testGetValue() {
        System.out.println("getValue");
        PIcommand instance;
        try{
            instance = new PIcommand("importxml location=\"hallo.xml\" xpath=\"\\root\"",new Definitions());
        }
        catch(Exception e)
        {
            fail("Could not establish PIcommand");
            return;
        }
        assertEquals("hallo.xml", instance.getValue("location"));
        assertEquals("\\root", instance.getValue("xpath"));
    }

    /**
     * Test of getValueAsInteger method, of class PIcommand.
     */
    @Test
    public void testGetValueAsInteger() {
        System.out.println("getValueAsInteger");
        String n = "";
        PIcommand instance = null;
        try{
            instance = new PIcommand("modulemap cols=\"3\"",new Definitions());
        }
        catch(Exception e)
        {
            fail("Could not establish PIcommand");
            return;
        }
        assertEquals(3, instance.getValueAsInteger("cols"));
    }

    /**
     * Test of paramExist method, of class PIcommand.
     */
    @Test
    public void testParamExist() {
        System.out.println("paramExist");
        String n = "";
        PIcommand instance = null;
        try{
            instance = new PIcommand("modulemap cols=\"3\"",new Definitions());
        }
        catch(Exception e)
        {
            fail("Could not establish PIcommand");
            return;
        }
        assertEquals(true, instance.paramExist("cols"));
    }

    /**
     * Test of parse method, of class PIcommand.
     */
    @Test
    public void testParse() {
        System.out.println("parse");
        String data = "";
        PIcommand instance = null;
        try{
            instance = new PIcommand("modulemap cols=\"3\"",new Definitions());
        }
        catch(Exception e)
        {
            fail("Could not establish PIcommand");
            return;
        }
        assertEquals(true, instance.parse(instance.getOriginalData()));
    }


}
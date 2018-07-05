/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wxt2;

import wxt3.Scripthandler;
import content.Module;
import java.util.HashMap;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.w3c.dom.Document;

/**
 *
 * @author maa
 */
public class ScripthandlerTest {

    Scripthandler instance;

    public ScripthandlerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        try {
            HashMap <String,String>hp=new HashMap<String,String>();
            hp.put("s", "/home/maa/svn/obama/obama/testscript.xml");
            instance = new Scripthandler(hp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getScriptDoc method, of class Scripthandler.
     */
    @Test
    public void testGetScriptDoc() {
        System.out.println("getScriptDoc");
        Scripthandler instance = null;
        Document expResult = null;
        Document result = instance.getScriptDoc();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getRootModules method, of class Scripthandler.
     */
    @Test
    public void testGetRootModules() {
        System.out.println("getRootModules");
        
        
        List<Module> expResult = null;
        List<Module> result = instance.getRootModules();

        
        assertEquals(expResult, result);        
    }



    /**
     * Test of getReport method, of class Scripthandler.
     */
    @Test
    public void testGetReport() {
        System.out.println("getReport");

        String expResult = "";
        String result = instance.getReport();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class Scripthandler.
     */
    @Test
    public void testToString() {
        System.out.println("toString");

        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
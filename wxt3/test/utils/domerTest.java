/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utils;

import java.net.URI;
import javax.xml.parsers.DocumentBuilder;
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
public class domerTest {

    public domerTest() {
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
     * Test of makeDocBuilder method, of class domer.
     */
    @Test
    public void testMakeDocBuilder() throws Exception {
        System.out.println("makeDocBuilder");
        DocumentBuilder expResult = null;
        DocumentBuilder result = domer.makeDocBuilder(false);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of makeDomFromString method, of class domer.
     */
    @Test
    public void testMakeDomFromString() throws Exception {
        System.out.println("makeDomFromString");
        String S = "";
        Document expResult = null;
        Document result = domer.makeDomFromString(S);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of makeDomFromUri method, of class domer.
     */
    @Test
    public void testMakeDomFromUri() throws Exception {
        System.out.println("makeDomFromUri");
        URI theUri = null;
        Document expResult = null;
        Document result = domer.makeDomFromUri(theUri);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


   

    /**
     * Test of makeDocBuilder method, of class domer.
     */
    @Test
    public void makeDocBuilder() throws Exception {
        System.out.println("makeDocBuilder");
        DocumentBuilder expResult = null;
        DocumentBuilder result = domer.makeDocBuilder(false);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of makeDomFromString method, of class domer.
     */
    @Test
    public void makeDomFromString() throws Exception {
        System.out.println("makeDomFromString");
        String S = "";
        Document expResult = null;
        Document result = domer.makeDomFromString(S);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of makeDomFromUri method, of class domer.
     */
    @Test
    public void makeDomFromUri() throws Exception {
        System.out.println("makeDomFromUri");
        URI theUri = null;
        Document expResult = null;
        Document result = domer.makeDomFromUri(theUri);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

 
    

}
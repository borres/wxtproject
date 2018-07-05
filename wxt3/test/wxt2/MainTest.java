/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wxt2;

import wxt3.Main;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author maa
 */
public class MainTest {

    public MainTest() {
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
     * Test of main method, of class Main.
     */
    @Test
    public void testMain() {
        System.out.println("main");
        String[] args = null;
        Main.main(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of showUsage method, of class Main.
     */
    @Test
    public void testShowUsage() {
        System.out.println("showUsage");
        Main.showUsage();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
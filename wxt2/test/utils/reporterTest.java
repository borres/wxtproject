/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utils;

import java.net.URI;
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
public class reporterTest {

    public reporterTest() {
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
     * Test of setLocale method, of class reporter.
     */
    @Test
    public void testSetLocale() {
        System.out.println("setLocale");
        String lang = "";
        reporter.setLocale(lang);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setBundle method, of class reporter.
     */
    @Test
    public void testSetBundle() {
        System.out.println("setBundle");
        String bundle = "";
        reporter.setBundle(bundle);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLogFileUri method, of class reporter.
     */
    @Test
    public void testGetLogFileUri() {
        System.out.println("getLogFileUri");
        reporter instance = new reporter();
        URI expResult = null;
        URI result = instance.getLogFileUri();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setLogFileURI method, of class reporter.
     */
    @Test
    public void testSetLogFileURI() {
        System.out.println("setLogFileURI");
        URI theUri = null;
        reporter instance = new reporter();
        instance.setLogFileURI(theUri);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLogFileMaxlength method, of class reporter.
     */
    @Test
    public void testGetLogFileMaxlength() {
        System.out.println("getLogFileMaxlength");
        reporter instance = new reporter();
        long expResult = 0L;
        long result = instance.getLogFileMaxlength();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setLogFileMaxlength method, of class reporter.
     */
    @Test
    public void testSetLogFileMaxlength() {
        System.out.println("setLogFileMaxlength");
        long m = 0L;
        reporter instance = new reporter();
        instance.setLogFileMaxlength(m);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of markStack method, of class reporter.
     */
    @Test
    public void testMarkStack() {
        System.out.println("markStack");
        reporter instance = new reporter();
        instance.markStack();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getStackSize method, of class reporter.
     */
    @Test
    public void testGetStackSize() {
        System.out.println("getStackSize");
        reporter instance = new reporter();
        int expResult = 0;
        int result = instance.getStackSize();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of clearMessages method, of class reporter.
     */
    @Test
    public void testClearMessages() {
        System.out.println("clearMessages");
        reporter instance = new reporter();
        instance.clearMessages();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of pushSimpleMessage method, of class reporter.
     */
    @Test
    public void testPushSimpleMessage() {
        System.out.println("pushSimpleMessage");
        String m = "";
        reporter instance = new reporter();
        instance.pushSimpleMessage(m);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of pushMessage method, of class reporter.
     */
    @Test
    public void testPushMessage_String() {
        System.out.println("pushMessage");
        String m = "";
        reporter instance = new reporter();
        instance.pushMessage(m);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of pushMessage method, of class reporter.
     */
    @Test
    public void testPushMessage_String_String() {
        System.out.println("pushMessage");
        String m = "";
        String t1 = "";
        reporter instance = new reporter();
        instance.pushMessage(m, t1);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of pushMessage method, of class reporter.
     */
    @Test
    public void testPushMessage_3args() {
        System.out.println("pushMessage");
        String m = "";
        String t1 = "";
        String t2 = "";
        reporter instance = new reporter();
        instance.pushMessage(m, t1, t2);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of popMessage method, of class reporter.
     */
    @Test
    public void testPopMessage() {
        System.out.println("popMessage");
        reporter instance = new reporter();
        String expResult = "";
        String result = instance.popMessage();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of clearLogFile method, of class reporter.
     */
    @Test
    public void testClearLogFile() {
        System.out.println("clearLogFile");
        reporter instance = new reporter();
        instance.clearLogFile();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of writeLogFile method, of class reporter.
     */
    @Test
    public void testWriteLogFile() {
        System.out.println("writeLogFile");
        String text = "";
        String scriptpath = "";
        reporter instance = new reporter();
        instance.writeLogFile(text, scriptpath);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getReport method, of class reporter.
     */
    @Test
    public void testGetReport() {
        System.out.println("getReport");
        String scriptpath = "";
        reporter instance = new reporter();
        String expResult = "";
        String result = instance.getReport(scriptpath);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getBundleString method, of class reporter.
     */
    @Test
    public void testGetBundleString_String() {
        System.out.println("getBundleString");
        String S = "";
        String expResult = "";
        String result = reporter.getBundleString(S);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getBundleString method, of class reporter.
     */
    @Test
    public void testGetBundleString_String_String() {
        System.out.println("getBundleString");
        String S = "";
        String t1 = "";
        String expResult = "";
        String result = reporter.getBundleString(S, t1);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getBundleString method, of class reporter.
     */
    @Test
    public void testGetBundleString_3args() {
        System.out.println("getBundleString");
        String S = "";
        String t1 = "";
        String t2 = "";
        String expResult = "";
        String result = reporter.getBundleString(S, t1, t2);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of setLocale method, of class reporter.
     */
    @Test
    public void setLocale() {
        System.out.println("setLocale");
        String lang = "";
        reporter.setLocale(lang);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setBundle method, of class reporter.
     */
    @Test
    public void setBundle() {
        System.out.println("setBundle");
        String bundle = "";
        reporter.setBundle(bundle);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLogFileUri method, of class reporter.
     */
    @Test
    public void getLogFileUri() {
        System.out.println("getLogFileUri");
        reporter instance = new reporter();
        URI expResult = null;
        URI result = instance.getLogFileUri();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setLogFileURI method, of class reporter.
     */
    @Test
    public void setLogFileURI() {
        System.out.println("setLogFileURI");
        URI theUri = null;
        reporter instance = new reporter();
        instance.setLogFileURI(theUri);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLogFileMaxlength method, of class reporter.
     */
    @Test
    public void getLogFileMaxlength() {
        System.out.println("getLogFileMaxlength");
        reporter instance = new reporter();
        long expResult = 0L;
        long result = instance.getLogFileMaxlength();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setLogFileMaxlength method, of class reporter.
     */
    @Test
    public void setLogFileMaxlength() {
        System.out.println("setLogFileMaxlength");
        long m = 0L;
        reporter instance = new reporter();
        instance.setLogFileMaxlength(m);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of markStack method, of class reporter.
     */
    @Test
    public void markStack() {
        System.out.println("markStack");
        reporter instance = new reporter();
        instance.markStack();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getStackSize method, of class reporter.
     */
    @Test
    public void getStackSize() {
        System.out.println("getStackSize");
        reporter instance = new reporter();
        int expResult = 0;
        int result = instance.getStackSize();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of clearMessages method, of class reporter.
     */
    @Test
    public void clearMessages() {
        System.out.println("clearMessages");
        reporter instance = new reporter();
        instance.clearMessages();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of pushSimpleMessage method, of class reporter.
     */
    @Test
    public void pushSimpleMessage() {
        System.out.println("pushSimpleMessage");
        String m = "";
        reporter instance = new reporter();
        instance.pushSimpleMessage(m);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of pushMessage method, of class reporter.
     */
    @Test
    public void pushMessage() {
        System.out.println("pushMessage");
        String m = "";
        reporter instance = new reporter();
        instance.pushMessage(m);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of popMessage method, of class reporter.
     */
    @Test
    public void popMessage() {
        System.out.println("popMessage");
        reporter instance = new reporter();
        String expResult = "";
        String result = instance.popMessage();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of clearLogFile method, of class reporter.
     */
    @Test
    public void clearLogFile() {
        System.out.println("clearLogFile");
        reporter instance = new reporter();
        instance.clearLogFile();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of writeLogFile method, of class reporter.
     */
    @Test
    public void writeLogFile() {
        System.out.println("writeLogFile");
        String text = "";
        String scriptpath = "";
        reporter instance = new reporter();
        instance.writeLogFile(text, scriptpath);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getReport method, of class reporter.
     */
    @Test
    public void getReport() {
        System.out.println("getReport");
        String scriptpath = "";
        reporter instance = new reporter();
        String expResult = "";
        String result = instance.getReport(scriptpath);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getBundleString method, of class reporter.
     */
    @Test
    public void getBundleString() {
        System.out.println("getBundleString");
        String S = "";
        String expResult = "";
        String result = reporter.getBundleString(S);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


}
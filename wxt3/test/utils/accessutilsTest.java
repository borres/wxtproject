/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;


import java.util.List;
import java.io.File;
import java.net.URI;

import java.util.HashMap;
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
public class accessutilsTest {

    static String opsys;

    public accessutilsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        opsys = System.getProperty("os.name");
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

     
    @Test
    public void testRemoveFilePart(){
        URI theURI; 
        
        // test
        try{
            theURI = new URI("file://localhost/home/bilde.png");        
            assertEquals("file://localhost/home", accessutils.removeFilePart(theURI).toString());
        }catch(Exception e){
            fail(e.getMessage());
        }
        
        
    }

    @Test
    public void testgetFileNameFromPathString()
    {
        try{assertEquals("c",accessutils.getFileNameFromPathString("a/b/c.gif"));}
        catch(Exception e){fail(e.getMessage());}
        try{assertEquals("c",accessutils.getFileNameFromPathString("a\\b\\c.gif"));}
        catch(Exception e){fail(e.getMessage());}
        try{assertEquals("a/b/c",accessutils.getFileNameFromPathString("a\\b\\c"));}
        catch(Exception e){fail(e.getMessage());}
        try{assertEquals("c",accessutils.getFileNameFromPathString("c.gif"));}
        catch(Exception e){fail(e.getMessage());}

    }

    @Test
    public void testgetNumericStart()
    {
        try{assertEquals(14,accessutils.getNumericStart("14"));}
        catch(Exception e){fail(e.getMessage());}

        try{assertEquals(14,accessutils.getNumericStart("14px"));}
        catch(Exception e){fail(e.getMessage());}

        try{assertEquals(14,accessutils.getNumericStart("14%"));}
        catch(Exception e){fail(e.getMessage());}

        try{assertEquals(140,accessutils.getNumericStart("140%"));}
        catch(Exception e){fail(e.getMessage());}

        try{assertEquals(-1,accessutils.getNumericStart("px"));}
        catch(Exception e){fail(e.getMessage());}
    }
    /**
     * test of compareCSVI method
     */
    @Test
    public void testcompareCSV()
    {
        //----------
        try {assertEquals(true, accessutils.compareCSV("_never,xml", "all,xml"));}
        catch (Exception e) { fail(e.getMessage());}
        //----------
        try {assertEquals(false, accessutils.compareCSV("_never,xml", "all,xsml"));}
        catch (Exception e) { fail(e.getMessage()); }
        //----------
        try {assertEquals(false, accessutils.compareCSV("_never,xml,xslt", "all,xsml"));}
        catch (Exception e) { fail(e.getMessage()); }
        //----------
        try {assertEquals(false, accessutils.compareCSV("_never,xml,xslt", "all,txml"));}
        catch (Exception e) { fail(e.getMessage()); }  
        //----------
        try {assertEquals(true, accessutils.compareCSV("_never,xml,xslt", "all,xml"));}
        catch (Exception e) { fail(e.getMessage()); }   
        //----------
        try {assertEquals(true, accessutils.compareCSV("_never,xml,xslt", "xml"));}
        catch (Exception e) { fail(e.getMessage()); }
        //----------
        try {assertEquals(true, accessutils.compareCSV("xml","_never,xml,xslt" ));}
        catch (Exception e) { fail(e.getMessage()); }


    }
    /**
     * test of makeRelativeURI method
     */
    @Test
    public void testMakeRelativeURI(){
        URI theUri;
        URI fromU;
        URI toU;
        
      //----------
         try {
            fromU=new URI("file://c:/mypage.html");
            toU=new URI(  "file://d:/bilde.png");

            theUri = accessutils.makeRelativeURI(fromU,toU);
            assertEquals("file://d:/bilde.png", theUri.toString());
        } catch (Exception e) {
            fail(e.getMessage());
        } 
     //----------
         try {
            fromU=new URI("file://c:/top/mypage.html");
            toU=new URI(  "file://c:/top/b/bilde.png");

            theUri = accessutils.makeRelativeURI(fromU,toU);
            assertEquals("b/bilde.png", theUri.toString());
        } catch (Exception e) {
            fail(e.getMessage());
        } 

        //----------
         try {
            fromU=new URI("file://c:/mypage.html");
            toU=new URI(  "file://c:/top/b/bilde.png");

            theUri = accessutils.makeRelativeURI(fromU,toU);
            assertEquals("top/b/bilde.png", theUri.toString());
        } catch (Exception e) {
            fail(e.getMessage());
        } 
       //----------
         try {
            fromU=new URI("file://c:/mypage.html");
            toU=new URI(  "file://c:/bilde.png");

            theUri = accessutils.makeRelativeURI(fromU,toU);
            assertEquals("bilde.png", theUri.toString());
        } catch (Exception e) {
            fail(e.getMessage());
        } 
 
         //----------
         try {
            fromU=new URI("file:///C:/top/a/mypage.html");
            toU=new URI(  "file:///C:/top/b/bilde.png");

            theUri = accessutils.makeRelativeURI(fromU,toU);
            assertEquals("../b/bilde.png", theUri.toString());
        } catch (Exception e) {
            fail(e.getMessage());
        } 

        // ----------
         try {
            fromU=new URI("http://www.ia.hiof.no/a/b/mod.html");
            toU=new URI(  "http://www.ia.hiof.no/a/b/c/bilde.png");

            theUri = accessutils.makeRelativeURI(fromU,toU);
            assertEquals("c/bilde.png", theUri.toString());
        } catch (Exception e) {
            fail(e.getMessage());
        }
        
        //----------
         try {
            fromU=new URI("file:/c:/top/a/b/mod.html");
            toU=new URI(  "file:/c:/top/a/b/c/bilde.png");

            theUri = accessutils.makeRelativeURI(fromU,toU);
            assertEquals("c/bilde.png", theUri.toString());
        } catch (Exception e) {
            fail(e.getMessage());
        }
        //----------
         try {
            fromU=new URI("file:/c:/top/a/b/");
            toU=new URI(  "file:/c:/top/a/b/c/bilde.png");

            theUri = accessutils.makeRelativeURI(fromU,toU);
            assertEquals("c/bilde.png", theUri.toString());
        } catch (Exception e) {
            fail(e.getMessage());
        }
       //----------
         try {
            fromU=new URI("file:/c:/top/a/b/mod.html#herogder");
            toU=new URI(  "file:/c:/top/a/b/c/bilde.png");

            theUri = accessutils.makeRelativeURI(fromU,toU);
            assertEquals("c/bilde.png", theUri.toString());
        } catch (Exception e) {
            fail(e.getMessage());
        }
       //----------
         try {
            fromU=new URI("file:/c:/top/a/b/mod.html#herogder");
            toU=new URI(  "http://www.ia.hiof.no/a/b/c/bilde.png");

            theUri = accessutils.makeRelativeURI(fromU,toU);
            assertEquals("http://www.ia.hiof.no/a/b/c/bilde.png", theUri.toString());
        } catch (Exception e) {
            fail(e.getMessage());
        } 
     }
    
    /**
     * Test of isAbsoluteAddress method, of class accessutils.
     */
    @Test
    public void testIsAbsoluteAddress() {
        //TODO test this under Linux 

        System.out.println("isAbsolute");


        if (opsys.toLowerCase().contains("linux")) {
            assertTrue(accessutils.isAbsoluteAddress("/home/maa/svn"));
            assertFalse(accessutils.isAbsoluteAddress("c:\\folder\\subfolder\\file"));
            assertFalse(accessutils.isAbsoluteAddress("\\\\folder\\subfolder\\file"));
        } else if (opsys.toLowerCase().contains("win")) {
            assertFalse(accessutils.isAbsoluteAddress("/home/maa/svn"));
            assertTrue(accessutils.isAbsoluteAddress("c:\\folder\\subfolder\\file"));
            assertTrue(accessutils.isAbsoluteAddress("\\\\folder\\subfolder\\file"));
        }

        assertTrue(accessutils.isAbsoluteAddress("file:///C:/articles/common/p_template.xml"));
        assertTrue(accessutils.isAbsoluteAddress("file://C:/articles/common/"));
        assertFalse(accessutils.isAbsoluteAddress("home/maa/svn"));
        assertFalse(accessutils.isAbsoluteAddress("\\home\\maa\\svn"));
        assertTrue(accessutils.isAbsoluteAddress("http://www.it.hiof.no/~maa/index.html"));
        assertFalse(accessutils.isAbsoluteAddress("c:folder\\subfolder\\file"));
    }

    /**
     * Test of makeCatalog method, of class accessutils.
     */
    @Test
    public void testMakeCatalog() {
        URI theUri = null; 
        if (opsys.toLowerCase().contains("windows")) {
            try {
                theUri = new URI("file:///c:/testMakeCatalog/f.xml");
                accessutils.makeCatalog(theUri);
                File f = new File("c:\\testMakeCatalog\\");
                assertTrue( f.isDirectory());
                f.delete();
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }
        if(opsys.toLowerCase().contains("linux")){
            try{
                String home = System.getenv("HOME");                
                theUri = new URI("file://" + home + "/testMakeCatalog/f.xml");
                accessutils.makeCatalog(theUri);
                File f = new File(home + "/testMakeCatalog/"); 
                assertTrue(f.isDirectory()); 
                f.delete();
            }catch(Exception e){
                fail(e.getMessage());
            }            
        }        
    }

    /**
     * Test of makeAbsoluteURI method, of class accessutils.
     */
    @Test
    public void testMakeAbsoluteURI() throws Exception {
        System.out.println("MakeAbsoluteURI");
        URI theUri = null;
        String fp = null;
        String dp = null;



        // ************** Linux ************** 
        if (System.getProperty("os.name").contains("Linux")) {

            //test
            fp = "b/f.xml";
            dp = "/a/b";
            try {

                theUri = accessutils.makeAbsoluteURI(fp, dp);
                assertEquals("file:/a/b/b/f.xml", theUri.toString());
            } catch (Exception e) {
                fail(e.getMessage());
            }

            //test
            fp = "b/f.xml";
            dp = "/a/b/";
            try {

                theUri = accessutils.makeAbsoluteURI(fp, dp);
                assertEquals("file:/a/b/b/f.xml", theUri.toString());
            } catch (Exception e) {
                fail(e.getMessage());
            }
            //test
            fp = "b/f.xml";
            dp = "c:\\a\\b\\";
            try {

                theUri = accessutils.makeAbsoluteURI(fp, dp);

            } catch (Exception e) {
                assertTrue(true);
            }

            fp = "b/f.xml";
            dp = null;
            try {

                theUri = accessutils.makeAbsoluteURI(fp, dp);

            } catch (Exception e) {
                assertTrue(true);
            }

          // ************** Windows  **************   
        } else if (System.getProperty("os.name").contains("Win")) {
            
            //test            
            fp = "file://c:/~b/c/d.xml";
            dp = "http://www.ia.hiof.no/~b/c/";
            try {
                theUri = accessutils.makeAbsoluteURI(fp, dp);
                assertEquals("file://c:/~b/c/d.xml", theUri.toString());
            } catch (Exception e) {
                fail(e.getMessage());
            }
            //test 
            fp = "http://www.ia.hiof.no/~b/c/d.xml";
            dp = "http://www.ia.hiof.no/~b/c/";
            try {
                theUri = accessutils.makeAbsoluteURI(fp, dp);
                assertEquals("http://www.ia.hiof.no/~b/c/d.xml", theUri.toString());
            } catch (Exception e) {
                fail(e.getMessage());
            }


            fp = "d.xml";
            dp = "http://www.ia.hiof.no/~b/c/";
            try {
                theUri = accessutils.makeAbsoluteURI(fp, dp);
                assertEquals("http://www.ia.hiof.no/~b/c/d.xml", theUri.toString());
            } catch (Exception e) {
                fail(e.getMessage());
            }
            
            
            // test
            fp = "f.xml";
            dp = "c:\\a\\b\\";
            try {
                theUri = accessutils.makeAbsoluteURI(fp, dp);
                assertEquals("file:/c:/a/b/f.xml", theUri.toString());
            } catch (Exception e) {
                fail(e.getMessage());
            }

            // test  ---------------------
            fp = "../d/f.xml";
            dp = "c:/a/b/";
            try {
                theUri = accessutils.makeAbsoluteURI(fp, dp);
                assertEquals("file:/c:/a/d/f.xml", theUri.toString());
            } catch (Exception e) {
                fail(e.getMessage());
            }
            
            // test  ---------------------
            fp = "../../f.xml";
            dp = "c:/a/b/d/";
            try {
                theUri = accessutils.makeAbsoluteURI(fp, dp);
                assertEquals("file:/c:/a/f.xml", theUri.toString());
            } catch (Exception e) {
                fail(e.getMessage());
            }

            // test  ---------------------
            fp = "f.xml";
            dp = "c:/a/b";
            try {
                theUri = accessutils.makeAbsoluteURI(fp, dp);
                assertEquals("file:/c:/a/b/f.xml", theUri.toString());
            } catch (Exception e) {
                fail(e.getMessage());
            }


            // test  ---------------------
            fp = "c:/a/b/f.xml";
            dp = null;
            try {
                theUri = accessutils.makeAbsoluteURI(fp, dp);
                assertEquals("file:///c:/a/b/f.xml", theUri.toString());
            } catch (Exception e) {
                assertTrue(true);
            }


            // test  ---------------------
            fp = "c:\\a\\b\\f.xml";
            dp = null;
            try {
                theUri = accessutils.makeAbsoluteURI(fp, dp);
                assertEquals("file:///c:/a/b/f.xml", theUri.toString());
            } catch (Exception e) {
                assertTrue(true);
            }

            // test  ---------------------
            fp = "c:\\a\\b\\f.xml";
            dp = "c:\\a\\d\\";
            try {
                theUri = accessutils.makeAbsoluteURI(fp, dp);
                assertEquals("file:///c:/a/b/f.xml", theUri.toString());
            } catch (Exception e) {
                fail(e.getMessage());
            }
            
            // test  ---------------------
            fp = "c:\\a\\b\\f x.xml";
            dp = "c:\\a\\d\\";
            try {
                theUri = accessutils.makeAbsoluteURI(fp, dp);
                assertEquals("file:///c:/a/b/f%20x.xml", theUri.toString());
            } catch (Exception e) {
                fail(e.getMessage());
            }
           
 



            // test  ---------------------  
            // Directory path not absolute
            fp = "f.xml";
            dp = "a/b";
            try {
                theUri = accessutils.makeAbsoluteURI(fp, dp);
            } catch (Exception e) {
                assertTrue(true);
            }
            
         }


        // test  ---------------------
        fp = "http://www.x/f.xml";
        dp = null;
        try {
            theUri = accessutils.makeAbsoluteURI(fp, dp);
            assertEquals(fp, theUri.toString());
        } catch (Exception e) {
            assertTrue(true);
        }
                // test  ---------------------
        fp = "f.xml";
        dp = "http://www.x.y/~borres/q/";
        try {
            theUri = accessutils.makeAbsoluteURI(fp, dp);
            assertEquals("http://www.x.y/~borres/q/f.xml", theUri.toString());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        // test  ---------------------
        fp = "../f.xml";
        dp = "http://www.x.y/~borres/q/r/";
        try {
            theUri = accessutils.makeAbsoluteURI(fp, dp);
            assertEquals("http://www.x.y/~borres/q/f.xml", theUri.toString());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        // test  ---------------------
        fp = "file:/c:/a/f.xml";
        dp = null;
        try {
            theUri = accessutils.makeAbsoluteURI(fp, dp);
            assertEquals(fp, theUri.toString());
        } catch (Exception e) {
            assertTrue(true);
        }
    }
    
    @Test
    public void testgetIntegerList()
    {
        String s = "1,2,3,4";
        int limit=100;
        try {
            List<Integer> v = accessutils.getIntegerList(s,limit);
            assertEquals(4, v.size());
        } catch (Exception e) {
            fail(e.getMessage());
        }
        
        s="[1:7]";
        limit=-1;
        try {
            List<Integer> v = accessutils.getIntegerList(s,limit);
            assertEquals(6, v.size());
            assertEquals(1, (int)v.get(0));
            assertEquals(6, (int)v.get(v.size()-1));
        } catch (Exception e) {
            fail(e.getMessage());
        }

        s="[1:]";
        limit=100;
        try {
            List<Integer> v = accessutils.getIntegerList(s,limit);
            assertEquals(100, v.size());
            assertEquals(1, (int)v.get(0));
            assertEquals(100, (int)v.get(v.size()-1));
        } catch (Exception e) {
            fail(e.getMessage());
        }

        s="[:7]";
        limit=10;
        try {
            List<Integer> v = accessutils.getIntegerList(s,limit);
            assertEquals(8, v.size());
            assertEquals(0, (int)v.get(0));
            assertEquals(7, (int)v.get(v.size()-1));
        } catch (Exception e) {
            fail(e.getMessage());
        }

        s="[:-7]";
        limit=10;
        try {
            List<Integer> v = accessutils.getIntegerList(s,limit);
            assertEquals(7, v.size());
            assertEquals(4, (int)v.get(0));
            assertEquals(10, (int)v.get(v.size()-1));
        } catch (Exception e) {
            fail(e.getMessage());
        }

    }
            
    
    /**
     * Testing parse of transformation string
     */
    @Test
    public void testUnpackTransformationParameters(){
        String TP="T(a='b',c='d')";
        try{
            HashMap<String,String> map =accessutils.unpackTransformationParameters(TP);
            assertEquals(2,map.keySet().toArray().length);
        }
        catch(Exception e)
        {
            fail(e.getMessage());
        }
        //--------------
        TP="T(a='b' c='d')";
        try{
            HashMap<String,String> map =accessutils.unpackTransformationParameters(TP);
            assertEquals(2,map.keySet().toArray().length);
        }
        catch(Exception e)
        {
            fail(e.getMessage());
        }
       
        //--------------
        TP="T1";
        try{
            HashMap<String,String> map =accessutils.unpackTransformationParameters(TP);
            assertEquals(null,map);
        }
        catch(Exception e)
        {
            fail(e.getMessage());
        }
       //--------------
        TP="T1(alder=12";
        try{
            HashMap<String,String> map =accessutils.unpackTransformationParameters(TP);
            assertEquals(null,map);
        }
        catch(Exception e)
        {
            assertTrue(true);
        }
      //--------------
        TP="T1()";
        try{
            HashMap<String,String> map =accessutils.unpackTransformationParameters(TP);
            assertEquals(null,map);
        }
        catch(Exception e)
        {
            assertTrue(false);
        }
   }
    
    @Test
    public void testresourceExists(){
        try{
           URI turi=new URI("http://www.ia.hiof.no/~borres/self/bs1.gif");
           boolean res=accessutils.resourceExists(turi);
           assertEquals(true,res);
        }
        catch(Exception e)
        {
            fail(e.getMessage());
        }        

        try{
           URI turi=new URI("http://www.ia.hiof.no/~borres/self/bs100.gif");
           boolean res=accessutils.resourceExists(turi);
           assertEquals(false,res);
        }
        catch(Exception e)
        {
            fail(e.getMessage());
        }        
       //--------------- linux ---------------
        if(System.getProperty("os.name").contains("Linux"))
        {
        } 
       //-------------------- windows -------------
        else if (System.getProperty("os.name").contains("Win")) 
        {
         
           try{
               URI turi=new URI("file:///C:/wxtproject/wxt2/manifest.mf");
               boolean res=accessutils.resourceExists(turi);
               assertEquals(true,res);
            }
            catch(Exception e)
            {
                fail(e.getMessage());
            }
           try{
               URI turi=new URI("file:///C:/wxtproject/wxt2/juletrefest.mf");
               boolean res=accessutils.resourceExists(turi);
               assertEquals(false,res);
            }
            catch(Exception e)
            {
                fail(e.getMessage());
            }
        }
    }

    
    @Test
    public void testfixUriParameterEntities(){
       try{
           String inS="http://myserver/a/&hei";
           String outS= accessutils.fixUriParameterEntities(inS);
           assertEquals("http://myserver/a/&amp;hei",outS);
        }
        catch(Exception e)
        {
            fail(e.getMessage());
        }
      try{
           String inS="http://myserver/a/&hei/&heisann";
           String outS= accessutils.fixUriParameterEntities(inS);
           assertEquals("http://myserver/a/&amp;hei/&amp;heisann",outS);
        }
        catch(Exception e)
        {
            fail(e.getMessage());
        }
      try{
           String inS="http://myserver/a/&amp;hei/&heisann";
           String outS= accessutils.fixUriParameterEntities(inS);
           assertEquals("http://myserver/a/&amp;hei/&amp;heisann",outS);
        }
        catch(Exception e)
        {
            fail(e.getMessage());
        }
    }

     @Test
    public void testsetCommonEntities(){
       try{
           String inS="olsson&brothers";
           String outS= accessutils.setCommonEntities(inS);
           assertEquals("olsson&amp;brothers",outS);
        }
        catch(Exception e)
        {
            fail(e.getMessage());
        }
      try{
           String inS="hanson&amp;olsson&brothers";
           String outS= accessutils.setCommonEntities(inS);
           assertEquals("hanson&amp;olsson&amp;brothers",outS);
        }
        catch(Exception e)
        {
            fail(e.getMessage());
        }
      try{
           String inS="hanson&amp;'olsson'&brothers";
           String outS= accessutils.setCommonEntities(inS);
           assertEquals("hanson&amp;&apos;olsson&apos;&amp;brothers",outS);
        }
        catch(Exception e)
        {
            fail(e.getMessage());
        }


    }

 

}

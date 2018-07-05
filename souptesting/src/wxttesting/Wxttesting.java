package wxttesting;
//http://jsoup.org/apidocs/
//http://jsoup.org/

import filing.readwrite;
import java.io.FileOutputStream;
import xmldom.domer;



public class Wxttesting {
    private static String cat=System.getProperty("user.dir");
    
    public static void testSelect(){
        String t;
   
       // t=testHTML.findElementsWithSelector(cat+"\\soupdata\\dw.html", "a.external");
       t=testHTML.findElementsWithSelector(cat+"\\soupdata\\dw.html", "p");
       readwrite.writeTextFile(t, cat+"\\soupresult\\test.html");      
    }
    
    public static void testClean(){
        String T=readwrite.getTextFile(cat+"\\soupdata\\incomplete.xml");
        //String T=readwrite.getTextFile(cat+"\\soupdata\\vin.xml");
        //String T=readwrite.getTextFile(cat+"\\soupdata\\dw.html");
        try{
            org.w3c.dom.Document doc=domer.makeDomFromStringSomeHow(T, "UTF-8");
            System.out.println("OK");
            org.w3c.dom.NodeList divs=doc.getElementsByTagName("p");
            System.out.println(divs.getLength());
            domer.printDocument(doc, new FileOutputStream(cat+"\\soupresult\\test.xml"));
            //domer.printDocument(doc, System.out);
            
        }
        catch(Exception ex){
            System.out.println(ex.getMessage());
        }
    }
    
    public static void main(String[] args) {
        //testSelect();
        testClean();
      }
}

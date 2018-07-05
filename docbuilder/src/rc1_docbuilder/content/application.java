/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rc1_docbuilder.content;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import javax.swing.JOptionPane;

/**
 *
 * @author HP_Eier
 */
public class application {

    public static String readText(String filename)
    {

        String content=null;
        try
        {

            File f = new File(filename);
            FileInputStream fis=new FileInputStream(f);
            InputStreamReader isr=new InputStreamReader(fis,"UTF-8");
            //FileReader reader = new FileReader(f);
            BufferedReader br = new BufferedReader(isr);
            String object = "";
            content="";
            while((object=br.readLine())!=null)
            {
                content += object+"\n";
            }

            br.close();



        }

        catch(IOException ioe)
        {
            System.out.println("Reading  file failed:  "+ioe.getMessage());
            JOptionPane.showMessageDialog(null, "The file :"+filename+ " could not be read");
        }


        return content;
    }


    public static String readPrincePath(String filename)
    {

        String content=null;
        try
        {

            File f = new File(filename);
            FileReader reader = new FileReader(f);
            BufferedReader br = new BufferedReader(reader);
            String object = "";
            content="";
            while((object=br.readLine())!=null)
            {
                content += object+"\n";
            }
            

            br.close();
            


        }

        catch(IOException ioe)
        {
            System.out.println("Reading  file failed:  "+ioe.getMessage());
            JOptionPane.showMessageDialog(null, "The file :"+filename+ " could not be read");
        }


        return content;
    }



    public static void saveTextToFile(String text,String filename,String directory)
    {
        try
        {
            File f=new File(directory+"/"+filename);
            //FileWriter fw=new FileWriter(f);
            FileOutputStream fos=new FileOutputStream(f);
            OutputStreamWriter osw=new OutputStreamWriter(fos,"UTF-8");
            BufferedWriter bw=new BufferedWriter(osw);

            bw.write(text);
            bw.flush();

        }
        catch(IOException ioe)
        {
            System.out.println("Error writing to file " +ioe.getMessage());
        }
    }


    public static boolean deleteDirectory(File path)
    {
        
       
        
        if(path.exists())
        {
            System.out.println("Filen eksisterer...");
            File[] files=path.listFiles();
            for(int i=0; i<files.length; i++)
            {
               
                    files[i].deleteOnExit();
              
            }
        }
        return (path.delete());
    }
   



    

}

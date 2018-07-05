package snippets;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;

/**
 * Making codesnippets for Adobe Dreamweaver CS4, csn-files
 * Read textfiles according to own format from dist-catalog:
 *   defdefs.txt,moduledefs.txt,pidefs.txt,scriptdef.txt,scropttemplate.txt
 * and read master for a snippet according to what is found in DW csn-files
 *   template.txt
 *
 * Produce snippets according to template and build the following catalogstructure:
 * wxt
 *   scripttemplate.csn
 *   |-pi
 *   |-script
 *      |-scripttag.csn
 *      |-definitions
 *      |-module
 *
 * Input format for  file:
 *     snippet#snippet#snippet
 * each snippet:
 *     name;description;value
 * See for instance: pidefs.txt
 *
 * @author maa
 */
public class SnippetsCreator {

    /** templatevariables */
    private final String[] VARS = {"_NAME_", "_DESCRIPTION_", "_PI_"};
    /**  path to file containing the definitions as we have written them*/
    private String m_pathSource;
    /** The template-document as a string */
    private String m_template;
    /** the catalog where we will save the snippets when ready in DW-format*/
    private String m_catTarget;


    public SnippetsCreator(String pathSource, 
                           String pathTemplate,
                           String pathSnippets)
    {
        m_pathSource = pathSource;
        m_catTarget=pathSnippets;
        m_template=readFile(pathTemplate);
        createSnippets();
    }
    
    

    public void createSnippets() {

        String T=readFile(m_pathSource);
        if(T==null)
            return;
        // split on resultSnippet separator
        String[] snippets=T.split("#");
        for(int ix=0;ix<snippets.length;ix++)
        {
            // split a resultSnippet definition
            String oneSnippet=snippets[ix];
            // loose empty or too short snippets
            if(oneSnippet.trim().length()<3)
                continue;
            String[] parts=oneSnippet.split(";");
            if(parts.length!=VARS.length)
            {
                System.out.println("bad snippet: "+oneSnippet);
                continue;
            }
            String resultSnippet = m_template;
            for (int i = 0; i < VARS.length; i++) {
                resultSnippet = resultSnippet.replaceAll(VARS[i], parts[i].trim());
            }
            saveSnippet(resultSnippet, parts[0].trim());
        }
    }

    private void saveSnippet(String snippet, String name) {
        try {
            //hack to allow space in filepath
            String fileName=m_catTarget+"\\"+name+".csn";
            fileName=fileName.replace('\\', '/');
            File f = new File( new URI("file:///"+fileName.replaceAll(" ", "%20")));
            
            // make sure we have the catalog(s)
            f.getParentFile().mkdirs();

            FileWriter fw = new FileWriter(f);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(snippet);
            bw.close();

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
   
    private String readFile(String filename)
    {
         try {
                
                BufferedReader in = new BufferedReader(new FileReader(filename));
                StringBuffer result=new StringBuffer();
                String str;
                String eol="\n";
                while ((str = in.readLine()) != null) {
                    if(!str.trim().startsWith("//"))
                            result=result.append(str+eol);
                }
                in.close();
                return result.toString();
         } 
         catch (IOException e) {
             System.out.println("could not read: "+filename);
             return null;
         }
       
    }

    public static void main(String[] params) {
 
        //Default resultSnippet catalog:

        //Directly to Dreamweaver:
        //String snippetCatalog="C:/Program Files/Adobe/Adobe Dreamweaver CS4/configuration/Snippets/wxt/";
        //or
        //Make it here and move wxt-catalog by hand or by export/import plugin:
        String snippetCatalog="C:/tmp/dw-snippets/wxt/";
      
        //snippetCatalog ="C:/users/maa/AppData/Roaming/Adobe/Dreamweaver CS4/en_US/Configuration/Snippets/wxt_pi";
        //snippetCatalog="C:/users/maa/snippets";
 
        // we attempt to read the snippetCatalog from params
        // we have only one functional param:
        // the catalog we want to save the resultSnippet structure to
        // since this may contain spaces, we concatenate all we find
        if(params.length>0)
        {
            String par="";
            for(int ix=0;ix<params.length;ix++)
                par+=" "+params[ix];
            par=par.trim();
            if((par.compareToIgnoreCase("help")==0) || (par.compareToIgnoreCase("?")==0))
            {
                System.out.println("usage: snippets.jar targetcatalog");
                System.exit(0);
            }
            snippetCatalog=par.trim();
        }
        
        String templatefile = "template.txt";
        
        // catalogstructure:
        //wxt
        // |-scripttemplate.csn
        // |-pi
        // |-script
        //   |-scripttag.csn
        //   |-definitions
        //   |-module


        SnippetsCreator sc=null;
        System.out.println("doing: "+snippetCatalog);
        sc =new SnippetsCreator("scripttemplate.txt",templatefile,snippetCatalog);
        System.out.println("doing: "+snippetCatalog+"pi/");
        sc =new SnippetsCreator("pidefs.txt",templatefile,snippetCatalog+"pi/");
        System.out.println("doing: "+snippetCatalog+"script/");
        sc =new SnippetsCreator("scriptdef.txt",templatefile,snippetCatalog+"script/");
        System.out.println("doing: "+snippetCatalog+"script/module/");
        sc =new SnippetsCreator("moduledefs.txt",templatefile,snippetCatalog+"script/module/");
        System.out.println("doing: "+snippetCatalog+"script/defintions/");
        sc =new SnippetsCreator("defdefs.txt",templatefile,snippetCatalog+"script/defintions/");
        System.out.println("done");
    }
}

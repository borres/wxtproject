package wxt5;
import java.util.HashMap;
import java.util.Set;

/**
 * Main
 * 
 */
public class Main {

    /** organizing the arguments*/
    static HashMap<String,String>arguments;
    
    /**
     * Reading and interpreting arguments.
     * <p>
     * Setting up and using a {@link Scripthandler}
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {

        arguments=new HashMap<String,String>();
        // arguments will be organized such:
        // the first argument should always be the script
        // script: the script to build
       // -m ids will produce modules : build modules with this id list
        // samples:
        // java -jar C:\wxt\dist\wxt.jar -h
        // java -jar C:\wxt\dist\wxt.jar c:\book\myscript.xml
        // java -jar C:\wxt\dist\wxt.jar c:\book\myscript.xml -m page1,page2
        // java -jar C:\wxt\dist\wxt.jar c:\book\myscript.xml -o expand-all=yes -o default-encoding=utf-8


        //-----------------------------------------
        // go for the arguments
        if((args==null) || (args.length==0))
        {
            System.out.println("Missing script");
            showUsage();
            return;
        }
        else
        {
            if(args.length > 0)
            {                
                int ix=0;
                while(ix<args.length)
                {
                    if(args[ix].startsWith("-"))
                    {
                        String opt=args[ix].substring(1);
                        if((opt.compareToIgnoreCase("h")==0)
                                ||(opt.compareToIgnoreCase("help")==0))
                        {
                            // handle help, and ignore the rest
                            showUsage();
                        }
                        else if((opt.compareToIgnoreCase("m")==0))
                        {
                            // handle module idlist
                            if(ix+1 < args.length)
                            {
                                // clean spaces surrounding commas
                                String[] mods=args[ix+1].split(",");
                                String s="";
                                for(int mix=0;mix < mods.length;mix++)
                                    s+=","+mods[mix].trim();
                                if(!s.isEmpty())
                                {
                                    s=s.substring(1);
                                    arguments.put("modules", s);
                                }
                                //arguments.put("modules", args[ix+1]);
                                ix++;
                            }

                        }
                        else if(opt.compareToIgnoreCase("o")==0)
                        {
                            // -o option=value
                            // -o default-encoding=utf-8
                            if(ix+1 < args.length)
                            {
                                String[] tmp=args[ix+1].split("=");
                                if(tmp.length==2)
                                    arguments.put(tmp[0], tmp[1]);
                                ix++;
                            }
                        }
                        else
                        {
                            System.out.println("Cannot interpret commandline. Argument "+ix+": "+args[ix]);
                            showUsage();
                            ix+=1000;
                        }
                    }
                    else
                    {
                        if(ix==0)
                        {
                            // this must be interpreted as the script
                            String scriptadr=args[ix];
                            // space in scriptpath ?
                            ix++;
                            while((ix<args.length)&&
                                    ((!args[ix].startsWith("-"))))
                            {
                                scriptadr+=" "+args[ix];
                                ix++;
                            }
                            ix--;
                            scriptadr=scriptadr.replace('\\', '/');
                            scriptadr=scriptadr.replace("//", "/");
                            

                            arguments.put("script", scriptadr);
                        }
                        else
                        {
                            // dont accept script as any argument but the first
                            System.out.println("Cannot interpret commandline. Argument "+ix+": "+args[ix]);
                            showUsage();
                            ix+=1000;                           
                        }
                    }
                    ix++;
                }               
            }
        }       
        // we must have a script
        if(!arguments.containsKey("script"))
        {
            System.out.println("Missing script");
            showUsage();
        }
        else
        {
        
            // we use same arguments for both constructor
            // and builModules.
            // they will know what they want to use.
            // Constructor will use "script" and options
            // buildModules will use "modules" and options
           try{
                Scripthandler sh=new Scripthandler(arguments);
                System.out.println(sh.getReport());

                //when debugging: System.out.println(sh);

                sh.buildModules(arguments);
                System.out.println(sh.getReport());

                // debugging repeated builds with same structure
                //sh.buildModules(arguments);
                //System.out.println(sh.getReport());
                // eo debugging

            }
            catch(Exception e){
                System.out.println(args[0]+" -> "+e.getMessage());
                //e.printStackTrace();
            }
        }
    }    
    
    /**
     * Print the expected parameterlist
     */
    public static void showUsage()
    {
        System.out.println("------ Usage: -------");
        System.out.println(" ");
        System.out.println("script [options][-m]");
        System.out.println(" ");
        System.out.println("script is full path to the script to be built");
        System.out.println("options:");
        System.out.println("  -h\tshow this message");
        System.out.println("  -o\toption=value");
        System.out.println("Some options are:");
        System.out.println("\texpand-all'. Values: yes or no");
        System.out.println("\tindent-output. Values: yes or no");
        System.out.println("\toutput-format. Values: xml, html, text or xhtml ");
        System.out.println("\tdefault-encoding. Default is utf-8");
        System.out.println("\tConsult the documentation for ");
        System.out.println("\texplanation of these and a few more options");
        System.out.println(" ");
        System.out.println("modules:");
        System.out.println("-m id[,id]");
        System.out.println("\tbuild all modules with these ids");
        System.out.println("---------------------");

    }
    
   /**
    * Just for debugging
    */
    private static void showArguments()
    {
       Set<String> keys=arguments.keySet();
       for(String k:keys)
           System.out.println(k+"  -> "+arguments.get(k));      
    }


}

package tidytester;

/**
 *
 * @author Administrator
 * 
 * 
 * 
 */
public class Tidytester 
{

    /**
     * Print the expected parameterlist
     */
    public static void showUsage()
    {
        System.out.println("------ Usage: -------");
        System.out.println(" ");
        System.out.println("[attributefile] fileIn fileOut");
        System.out.println("---------------------");

    }    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String inFileName=null;
        String outFileName=null;
        String attFileName=null;

        // arguments will be organized such:
        // java -jar C:\temp\in.xml C:\temp\out.xml


        //-----------------------------------------
        // go for the arguments
        if((args==null) || (args.length < 2))
        {
            System.out.println("Missing arguments");
            showUsage();
            return;
        }
        else if (args.length ==3)
        {
            attFileName=args[0];
            inFileName=args[1];
            outFileName=args[2];                    
        }
        else
        {
            inFileName=args[0];
            outFileName=args[1];
        }       
        tidying tider=new tidying(attFileName);
        tider.doTheJob(inFileName, outFileName);
    }
}



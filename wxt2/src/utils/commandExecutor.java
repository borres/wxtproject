package utils;

import java.io.*;

/**
 * Runs a program with exec.
 * <p>
 * May be set to wait for this new process to finnish.
 * <p>
 * Routes output from the new process to mainthreads output.
 */
public class commandExecutor {
    /**the argument(s), space separated elements in commandline*/   
    String[] arguments=null;
    /** the command line as found*/
    String cmdline=null;
    /**if we should wait for the process we start*/
    boolean waitForProcess=false;
    /**the reporter we will use*/
    reporter m_reporter=null;
    /**default retun value*/
    private int returnValue = Integer.MIN_VALUE;
   
    /**
     * Constructing a new commandExecutor.
     * 
     *@param comline the "line" we want to execute
     *@param bWait true if we will wait for the new thread to finnish
     *@param rep the reporter we will use
     */
    public commandExecutor(String comline,boolean bWait,reporter rep){
     cmdline=comline;
     arguments=comline.split(" ");
     waitForProcess=bWait;
     m_reporter=rep;
   }
    

     /**
      * Gets the returnValue achieved in this commandExecutor.
      * 
      * @return The returnValues value
      */
     public int getReturnValue() {
          return returnValue;
     }

     /**
      * Main processing method for a commandExecutor object
      */
     public void doit() {
          try {
               if (arguments.length < 1) {
                    // System.out.println("Usage: java commandExecutor <command string>");
                    return;
               }
               Process pro = null;
               if (arguments.length > 1) 
                    pro = Runtime.getRuntime().exec(arguments);
               else 
                    pro = Runtime.getRuntime().exec(arguments[0]);

               InputStream error = pro.getErrorStream();
               InputStream output = pro.getInputStream();
               Thread err = new Thread(new OutErrReader(error,m_reporter));
               Thread out = new Thread(new OutErrReader(output,m_reporter));
               
               out.start();
               err.start();
               
               if(waitForProcess)
               {
                   m_reporter.pushSimpleMessage("Start command: "+cmdline+", and wait");
                   returnValue = pro.waitFor();
               }
               else
               {
                   m_reporter.pushSimpleMessage("Start command: "+cmdline);
                   returnValue=0;
               }

          }
          catch (java.io.IOException e) {
              //System.out.println(e.getMessage());
              // error 193 - some security problem ?
              m_reporter.pushMessage("command_failed",e.getMessage());
              return;
              // e.printStackTrace();
          }

           catch (java.lang.InterruptedException e) {
              m_reporter.pushMessage("command_interrupted",arguments[0]);
              return;
              // e.printStackTrace();
          }

     }


     /**
      *  Will receive errors
      */
     class OutErrReader implements Runnable {
         /** input stream */
         InputStream is;
         /** the reporter we will use*/
         reporter m_reporter=null;

          /**
           * Constructor for the OutErrReader object
           * @param rep the reporter we will use
           * @param is Description of the Parameter
           */
          public OutErrReader(InputStream is,reporter rep) {
               this.is = is;
               m_reporter=rep;
          }

        /**
        *  Main processing method for the OutErrReader object
        */
        @Override
        public void run() 
        {
           try {
                BufferedReader in = new BufferedReader(new InputStreamReader(is));
                String temp = null;
                while ((temp = in.readLine()) != null) 
                {
                     System.out.println(temp);
                     m_reporter.pushSimpleMessage(temp);
                }
                is.close();
           }
           catch (Exception e) {
                e.printStackTrace();
           }
        }
     }
}

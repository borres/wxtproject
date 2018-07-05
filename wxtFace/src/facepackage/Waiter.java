package facepackage;

import javax.swing.JProgressBar;

/**
 * Updating a progressbar in a separate thread
 * @author Administrator
 */
public class Waiter extends Thread{
    
    JProgressBar bar;
    boolean goOn;

    public Waiter(JProgressBar b){
        bar=b;
        goOn=false;
    }
    
    public void stopIt(){
        goOn=false;
    }
     
    @Override
    public void run() {
        goOn=true;
        bar.setBorder(null);
        int count=0;
        int dcount=10;
        bar.setMaximum(bar.getWidth());
        bar.setMinimum(0);
        while(goOn){
            try{
                Thread.sleep(100);
                bar.setValue(count+=dcount);
                if(bar.getValue() >= bar.getMaximum()-dcount-1) {
                    count=0;
                    bar.setValue(bar.getMaximum()-1);
                    //System.out.println("changed to 0");
                 }
                 bar.paintImmediately(0, 0, bar.getWidth(), bar.getHeight());
                 //System.out.println("*");
            }
            catch(Exception ex){
                
            }
       }
       bar.setValue(0);
    }
    
}

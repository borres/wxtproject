public class wxtError {

     /**
     * Produce a system.out msg and/or a visible errorelement
     * @param doc The doc that will own the message
     * @param cmd The cmd causing the error
     * @param msg A message
     * @return A span element
     */ 
    public static Element makeErrorMsgForDebugging(Document doc,PIcommand cmd,String msg)
    {
        if(msg==null)
            msg=" ";
        String cmds=" ";
        if(cmd!=null)
            cmds=cmd.getOriginalData();
        String all="Internal WXTError: "+msg+" in "+cmds;
        
        // print to system.out always
        System.out.println(all);
        
        // return an element if doc exists
        if(doc!=null)
        {
            Element erElt=doc.createElement("span");
            erElt.setAttribute("class", "wxt-error");
            erElt.appendChild(doc.createTextNode(all));
            return erElt;
        }
        else
            return null;
    }
}



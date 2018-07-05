package reporting;

import content.Definitions;
import content.Module;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import utils.PIcommand;

/**
 * Handle internal WXT errors.
 * "should not happen" errors
 * @author bs
 */
public class wxtError {

    /**
     * Produce a system.out msg and/or a visible errorelement
     * @param mod The module involved
     * @param cmd The cmd causing the error
     * @param msg A message
     * @return A span element
     */
    public static Element makeErrorMsgForDebugging(Module mod, PIcommand cmd, String msg) {
        if (msg == null) {
            msg = " ";
        }
        String cmds = " ";
        if (cmd != null) {
            cmds = cmd.getOriginalData();
        }
        String all = "Internal WXTError: " + msg + " in " + cmds;

        Document doc = null;

        // print to system.out always
        System.out.println(all);

        if (mod != null) {
            doc = mod.getDoc();
            if (mod.getDefinitions().getOption(Definitions.VERBOSE).compareTo(Definitions.YES) == 0) {
                mod.getReporter().pushSimpleMessage(all);
            }
        }

        // return an element if doc exists
        if (doc != null) {
            Element erElt = doc.createElement("span");
            erElt.setAttribute("class", "wxt-error");
            erElt.appendChild(doc.createTextNode(all));
            return erElt;
        } else {
            return null;
        }

    }

    /**
     * Report notices about empty or failed PIs
     * @param mod The emodule involved
     * @param msg The command we try to implement
     */
    static public void reportNotice(Module mod, String msg) {
        if (mod.getDefinitions().getOption(Definitions.VERBOSE).compareTo(Definitions.YES) != 0) {
            return;
        }
        // drop template ison messages, they are internal to wxt
        if (msg.indexOf(PIcommand.TEMPLATE) == -1) {
            mod.getReporter().pushSimpleMessage(msg);
            System.out.println(msg);
        }
    }
}

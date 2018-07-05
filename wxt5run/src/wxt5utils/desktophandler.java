package wxt5utils;

import java.awt.Desktop;
import java.net.URI;

/**
 *
 * @author Administrator
 */
public class desktophandler {
    public static String launchUri(URI theUri)
    {
        if(Desktop.isDesktopSupported())
        {
            Desktop dt=Desktop.getDesktop();
            try{
                dt.browse(theUri);
                return null;
            }
            catch(Exception ex)
            {
                return ex.getMessage();
            }
        }
        return "Launcher not supported";
    }

}

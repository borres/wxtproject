/*
 * wxt2runApp.java
 */

package wxt2run;

import java.awt.SplashScreen;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class wxt2runApp extends SingleFrameApplication {

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup()
    {

        show(new wxt2runView(this));
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of wxt2runApp
     */
    public static wxt2runApp getApplication() {
        return Application.getInstance(wxt2runApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(wxt2runApp.class, args);
    }
}

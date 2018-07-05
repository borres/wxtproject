/*
 * wxt3runApp.java
 */

package wxt3run;

import java.awt.SplashScreen;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class wxt3runApp extends SingleFrameApplication {

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup()
    {

        show(new wxt3runView(this));
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
     * @return the instance of wxt3runApp
     */
    public static wxt3runApp getApplication() {
        return Application.getInstance(wxt3runApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(wxt3runApp.class, args);
    }
}

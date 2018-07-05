/*
 * RC1_DocbuilderApp.java
 */

package rc1_docbuilder;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class RC1_DocbuilderApp extends SingleFrameApplication {

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        show(new RC1_DocbuilderView(this));
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
     * @return the instance of RC1_DocbuilderApp
     */
    public static RC1_DocbuilderApp getApplication() {
        return Application.getInstance(RC1_DocbuilderApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(RC1_DocbuilderApp.class, args);
    }
}

package engine;

import java.net.URL;
import java.net.URLClassLoader;

/**
 *
 * @author Administrator
 */
public class JarFileLoader extends URLClassLoader {
     
    public JarFileLoader (URL[] urls)
      {
        super (urls);
      }
    
    public void addFile (String path) throws Exception
    {
        String urlPath = "jar:file:///" + path + "!/";
        urlPath=urlPath.replace('\\', '/');
        System.out.println(urlPath);
        addURL (new URL (urlPath));
    }
    
}

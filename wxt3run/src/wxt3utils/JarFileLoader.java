package wxt3utils;

/**
 *
 * @author Administrator
 */
import java.net.URL;
import java.net.URLClassLoader;
 
public class JarFileLoader extends URLClassLoader
{
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



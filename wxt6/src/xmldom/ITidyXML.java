package xmldom;

import java.net.URI;
import reporting.reporter;

/**
 *
 * @author Administrator
 */
public interface ITidyXML {
    public  String tidyAndMakeXML(URI theuri,String encoding,reporter theReporter)
            throws Exception;
    public  String tidyAndMakeXML(String source,String encoding,reporter theReporter)
            throws Exception;

}

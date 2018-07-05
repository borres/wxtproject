/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rc1_docbuilder.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Vector;
import org.w3c.tidy.Configuration;
import org.w3c.tidy.Tidy;



/**
 * This is a library with utility methods for
 * fileaccess, path and URI manipulation etc.
 * Only static methods
 */
public class accessutils {

/** default encoding */
static private String default_encoding="UTF-8";

/** set default encoding
 * 
 * @param s The new encoding
 */
public static void setDefaultEncoding(String s)
{default_encoding=s;}

/**
 * get default encoding
 * 
 * @return the encoding
 */
public static String getDefaultEncoding()
{return default_encoding;}

    /**
     * Check if a path is absolute dependent on operating system .
     * Cases to consider:
     * <p>
     * C:\folder\file   
     * <p>     
     * /something/something 
     * <p>
     * http://www.something
     * <p>
     * file://something
     *      
     *@param path The path we will check
     *@return true if it is absolute, false otherwise
     */
    public static boolean isAbsoluteAddress(String path) {                
        try {                
            File f = new File(path);  
            // handles driver:\folder\file (if win) and /folder/file (if linux)
            if (f.isAbsolute())
                return true; 
            path = path.trim(); 
            if (path.startsWith("http") || path.startsWith("file")){                
                return f.toURI().isAbsolute();                
            }             
            return false; 
        } 
        catch (Exception e) 
        {            
            return false;
        }                                
    }

    /** 
     *  Try to make necessary catalogs if the theUri (file) does not exist.
     * 
     * @param theUri The URI for the file
     * @return true if the catalogpath exists when we leave, false otherwise
     */
    public static boolean makeCatalog(URI theUri) {
        try {
            File f = new File(theUri);
            if (!f.exists()) {
                new File(f.getParent()).mkdirs();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    
    /**
     * Remove filepart from an URI.
     * 
     * @param theUri The URI we will work on
     * @return the URI with removed filepart, i.e. a catalog
     */
    public static URI removeFilePart(URI theUri)
    {
        URI resUri=null;
        String tmp=removeFilePartFromPathstring(theUri.toString());
        try{
            resUri=new URI(tmp);
            return resUri;            
        }
        catch(Exception e)
        {
            return theUri;
        }
    }
     /**
     * Remove filepart from a path.
      * 
     * @param pt The path we will work on
     * @return the reduced path if file was identified, otherwise the path as received
     */  
    public static String removeFilePartFromPathstring(String pt)
    {
        int lastslash=pt.lastIndexOf('/');
        int lastdot=pt.lastIndexOf('.');
        if(lastdot > lastslash) 
            return pt.substring(0,lastslash);
        return pt;     
    }
    
    /**
     * Return the extension (string behind last .) of the file with uri uristr
     * @param uristr The string where we try to locate the extension
     * @return The extension. return null if no extenison is found
     */
    public static String getFileExtension(String uristr)
    {
        int pos=uristr.lastIndexOf('.');
        if(pos != -1)
        {
            return uristr.substring(pos+1);
        }
        return null;
    }
    
    
    /**
     * Build an URI which refers from one absolute uri to another absolute uri.
     * 
     * @param from_uri The URI we refer from
     * @param to_uri The URI we refer to
     * @return The relative URI
     */
    public static URI makeRelativeURI(URI from_uri,URI to_uri){
        // both are assumed to be complete absolute addresses of two files
        // we want to find out how we can reference to_uri from from_uri
        //@TODO : this method should be revised
        
        
        
        // if either is relative we return to_uri
        if((!to_uri.isAbsolute())||(!from_uri.isAbsolute()))
            return to_uri;
        
        // if they are equal, we try to return the filepath
        if(from_uri.toString().compareTo(to_uri.toString())==0)
        {
            String thepath=to_uri.getPath();
            int pos=thepath.lastIndexOf('/');
            try{ return new URI(thepath.substring(pos+1)); }
            catch(URISyntaxException se){return to_uri; }
        }

        // ok both are absolute and they are different
        // remove filepart, if necessary
        URI from_uri_cat=removeFilePart(from_uri);
        URI relUri=from_uri_cat.relativize(to_uri);
        
        if(!relUri.isAbsolute())
            return relUri;
        
        // we may not be happy with this if we get an absolute URI 
        // we must resolve the case where the result should start with ../
        String fromHost=from_uri.getHost();
        String toHost=to_uri.getHost();
        if((fromHost!=null)&&(toHost!=null))
             if(from_uri.getHost().compareToIgnoreCase(to_uri.getHost())!=0)
                  return relUri;

        String fromScheme=from_uri.getScheme();
        String toScheme=to_uri.getScheme();
        if(fromScheme.compareTo(toScheme)!=0)
            return to_uri;
        String fromPath=from_uri.getPath();
        String toiPath=to_uri.getPath();
        String toAuhtority=to_uri.getAuthority();
        String toQuery=to_uri.getQuery();
        String toFragment=to_uri.getFragment();
        String theNewPath="";
        
        // start reducing the paths
        String from_list[]=fromPath.split("/");
        String to_list[]=toiPath.split("/");
        int pos=0;
        while((pos < to_list.length)&&
              (pos < from_list.length)&&
              (from_list[pos].compareTo(to_list[pos])==0))
                    pos++;
        if((pos==from_list.length)&&(pos==to_list.length)){
            try{
                // in same catalog
                return new URI(null,null,to_list[pos-1],toQuery,toFragment); 
            }
            catch(URISyntaxException e){
                return to_uri;
            }
        }
        
        for(int ix=0; ix < (from_list.length-pos-1);ix++)
            theNewPath+="../";
        for(int ix=pos; ix < (to_list.length);ix++)
            theNewPath+=to_list[ix]+"/";
        theNewPath=theNewPath.substring(0,theNewPath.length()-1);        
        
        try{
            URI retUri=new URI(null,null,theNewPath,toQuery,toFragment);
            return retUri;
        }
        catch(URISyntaxException e){
            System.out.println("accessUtils:getRelativeUri: "+e.getMessage());
            return to_uri;
        }
    }
    
    


    /**
     * Build an absolute URI from a directorypath and a filepath.
     * The dirpath is only used if the filepath is relative.
     * 
     * @return An absolute URI
     * @param filepath The absolute or relative filepath
     * @param dirpath The absolute directory path
     * @throws java.lang.Exception if we dont succeed
     */
    public static URI makeAbsoluteURI(String filepath, String dirpath)
            throws Exception {
        // this is hazardiuos business
        //@TODO: Address calculation for all seasons
        // what about ?:
        //    samba access without mapping to drive: \\
        //    implisit: http://wwww , as browsers guess addresses
        //    [scheme:][//authority][path][?query][#fragment], with authority part
        
        URI theUri = null;
        URI fileUri = null;
        URI catUri = null;
        URI tstUri = null;
        
        if((dirpath==null) || (filepath==null))
            throw new Exception();        
        
        dirpath=dirpath.replace('\\','/');
        filepath=filepath.replace('\\','/');
        
        if (!dirpath.endsWith("/"))
        {
            dirpath=removeFilePartFromPathstring(dirpath);
            if(!dirpath.endsWith("/"))
                dirpath+="/";
        }
        
        try{        
            // work on the directory path
            // must set scheme file if catalog is a file URI
            if(dirpath.startsWith("/"))
            {
                // this must be an absolute URI (unix style)
                // with no scheme, that is a file
                dirpath="file://"+dirpath;
            }
            catUri=new URI(dirpath);
            String scheme=catUri.getScheme();
           
            if((scheme!=null)&&(scheme.length() < 2))
            {
                // this probably is a windows absolute filepath 
                // starting with drive-letter
                catUri=new URI("file:///"+catUri.toString());
            }
            
            // work on the filepath
            tstUri=new URI(filepath);
            if(tstUri.isAbsolute())
            {
                String tstscheme=tstUri.getScheme();
                if((tstscheme!=null)&&(tstscheme.length() < 2))
                {
                    // this probably is a windows absolute filepath 
                    // starting with drive-letter
                    fileUri=new URI("file:///"+tstUri.toString());
                }
                else
                    fileUri=tstUri;
            }
            else
                fileUri=new URI(filepath);
            
            theUri=catUri.resolve(fileUri);
            theUri=accessutils.fixUriParameterEntities(theUri);
            return theUri.normalize();
        }
        catch(Exception e)
        {
            String m=e.getMessage();
            throw e;
        }

    }

    /**
     * Append to a text file.
     * 
     * @param theUri The absolute URI to the file
     * @param text The text to append
     * @param maxFileSize When file reach this size in bytes it is reset, -1 keeps it going forever
     * @return true if save goes ok, false otherwise
     */
    public static boolean appendToTextFile(URI theUri, String text, long maxFileSize) {
        try {
            File f = new File(theUri);
            try {
                if (!f.exists()) {
                    return saveTextFile(f, text, false); // not append

                }
                if ((maxFileSize > 0) && (f.length() > maxFileSize)) {
                    return saveTextFile(f, text, false); // not append

                }
                return saveTextFile(f, text, true); // append

            } catch (Exception ioe) {
                throw new Exception(ioe.getMessage());
            }
        } catch (Exception ex) {
            System.out.println("AccessUtils:appendToTextFile, Problems writing: " + theUri.toString());
            return false;
        }
    }

    /**
     * Save a text file.
     * 
     * @param bAppend true if we want to append to the file
     * @param f A file object
     * @param text The string to write
     * @return true if save goes ok, false otherwise
     */
    private static boolean saveTextFile(File f, String text, boolean bAppend) {
        FileWriter fwriter = null;
        try {
            if (!f.exists()) {
                try {
                    //URI theUri = makeAbsoluteURI(f.getAbsolutePath(), null);
                    URI theUri = f.toURI();
                    makeCatalog(theUri);
                } catch (Exception e) {
                    throw new Exception(e.getMessage());
                }
            }
            fwriter = new FileWriter(f, bAppend);
            fwriter.write(text);
            fwriter.close();
            return true;
        } 
        catch (Exception ex) {
            System.out.println("AccessUtils:saveTextFile, Problems writing: " + f.getAbsolutePath());
            return false;
        } finally {
            try {
                if (fwriter != null) {
                    fwriter.close();
                }
            } catch (IOException ioe) {
            }
        }
    }
    
 

    /** Save a text file.
     * 
     * @param theUri The absolute URI to the file
     * @param text The text to write
     * @param encoding The encoding to use
     * @return true if save goes ok, false otherwise
     */
     public static boolean saveTFile(URI theUri, String text, String encoding)
    {
        File f;
        FileOutputStream fos=null;
        OutputStreamWriter osw=null;
        try {
            f = new File(theUri);
            if (!f.exists()) {
                try {
                    //URI theUri = makeAbsoluteURI(f.getAbsolutePath(), null);
                    makeCatalog(theUri);
                } 
                catch (Exception e) 
                {
                    throw new Exception(e.getMessage());
                }
            }

            fos=new FileOutputStream(f);
            if(encoding!=null)
                osw=new OutputStreamWriter(fos,encoding);
            else
                osw=new OutputStreamWriter(fos);
            osw.write(text);
            osw.close();
            return true;
        } 
        catch (Exception ex) 
        {
            System.out.println("AccessUtils:saveTextFile, Problems writing: " + theUri.toString());
            return false;
        }

    }

    /**
     * Finds out if a string is in a stringarray.
     * 
     * @param name The name we are searching for
     * @param namelist The list we are searching
     * @return the index of the found word if found, else -1
     */
    public static int indexOfNameInList(String name,String[] namelist)
    {
        for(int ix=0;ix<namelist.length;ix++)
            if(namelist[ix].compareTo(name)==0)
                return ix;
        return -1;
    }
     
    /**
     * Finds out if one of the strings in a stringarray is contained in another stringarray.
     * 
     * @param names The candidate names
     * @param namelist The list we are searching in
     * @return true if found, false otherwise
     */
    public static boolean isAnyNameInList(String[] names,String[] namelist)
    {
        for(int ix=0;ix<names.length;ix++)
        {
            if (indexOfNameInList(names[ix],namelist)!= -1)
                return true;
        }
        return false;
    }
    
    /**
     * Produce a list of integers from a commaseparated string.
     * 
     * @param S The string we will parse
     * @return A list of integers, or null if S is unparsable
     */
    public static Vector<Integer> getIntegerList(String S)
    {       
        String[]slist=S.split(",");
        Vector<Integer> list=new Vector<Integer>(slist.length);
        for(String s:slist)
        {
            try{
                Integer i=Integer.parseInt(s);
                list.add(i);
            }
            catch(Exception e){
                return null;
            }
         }
        return list;
    }
     
    /**
     * Produce a list of integers from a python-like slice expression.
     * 
     * @param slice The expression
     * @param limit Max number of items. Only effective for slices
     * @return A list of integers, or null if slice is unparsable
     */ 
     public static Vector<Integer> getIntegerList( String slice,int limit)
     {
         Vector<Integer> list=new Vector<Integer>();
         // parse the slice
         // expect one of the following
         // a,b,c,  the named indexes 
         // [a:b]   [a..b>
         // [a:]    [a..limit]
         // [:b]    [0..b>
         // [:-b]   [limit-b..limit]
         slice=slice.trim();
         if(slice.startsWith("["))
             slice=slice.substring(1).trim();
         if(slice.endsWith("]"))
             slice=slice.substring(0,slice.length()-1).trim();
         
         if(slice.indexOf(":")==-1)
             return getIntegerList(slice);
         
         int lo=0;
         int hi=limit+1;
         try{
             if(slice.startsWith(":"))
             {
                 hi=Integer.parseInt(slice.substring(1));
                 if(hi <0)
                 {
                     lo=limit+hi+1;
                     hi=limit+1;
                 }
                 else
                     hi=hi+1;
             }
             else if(slice.endsWith(":"))
                 lo=Integer.parseInt(slice.substring(0,slice.length()-1));
             else
             {
                String[] parts=slice.split(":");
                if(parts.length==2) 
                {
                    lo=Integer.parseInt(parts[0]);
                    hi=Integer.parseInt(parts[1]);
                }
             }
         }
         catch(Exception e)
         {
             return null;
         }
         for(int ix=lo;ix< hi;ix++)
             list.add(ix);
         
         return list;
     }
    
    
    /**
     * Parse for name-value pairs in a string and return them in a HashMap.
     * <p>
     * <p>Expected input variants:</p>
     * <p>(name1='value1',name2='value2')</p>
     * <p>name1='value1',name2='value2'</p>
     * <p>name1='value1' name2='value2'</p>
     * <p>name1="value1" name2="value2"</p>
     * @param data The string to parse
     * @param quoteMark How values are quoted: ' or "
     * @return a HashMap with names as keys
     */
    public static HashMap<String,String> parseNameValues(String data, char quoteMark){
        HashMap<String,String> pMap=new HashMap<String,String>();
        if((data==null)||(data.length()<3))
            return pMap;
        data=data.trim();
        
        // we dont want to bother about linebreaks or tabs
        data=data.replaceAll("\r\n"," ");
        data=data.replaceAll("\n"," ");
        data=data.replaceAll("\t"," ");

        // to identify missing name-value balance
        final String NONAME="baretull";
        
        // get rid of opening and closing brackets if any
        // no check for wellfomredness
        if(data.startsWith("(")) data=data.substring(1);
        if(data.endsWith(")")) data=data.substring(0,data.length()-1);
        
        boolean val_is_on=false;
        boolean nam_is_on=true;
        String currentName=NONAME;
        StringBuffer nam=new StringBuffer(32);
        StringBuffer val=new StringBuffer(256);
        
        for(int pos=0;pos<data.length();pos++){
            char c=data.charAt(pos);
            if(val_is_on){
                // the only way to finish a value is a quotemark
                if(c==quoteMark){
                    pMap.put(currentName,val.toString());
                    val=new StringBuffer(10);
                    val_is_on=false;
                    nam_is_on=true; // since commas are ignored as part of a name
                    currentName=NONAME;
                }
                // we accept everything else as part of a value
                else
                    val.append(c);
            }
            else if (nam_is_on){
                // the only way to finish a name is =
                if(c=='='){
                    currentName=nam.toString().trim();
                    nam=new StringBuffer(10);
                    nam_is_on=false;
                }
                // we ignore commas as potential part of name 
                else if(c==',')
                    continue;
                // accept everything else
                else
                    nam.append(c);
            }
            // the only way to start a value is a quotemark
            else if (c==quoteMark)
                val_is_on=true;
        }
        return pMap;
    }

     /**
     * Parse a string with a transformation with parameters.
     * <p>
     * Forms: T(name='Ole',address='Halden').
      *   
     * @param ts The string to unpack
     * @return a hashmap with parameter name as key, and  value as value
     * @throws Exception when we cannot parse the parameterlist
     */
    public static HashMap<String,String> unpackTransformationParameters(String ts)
    throws Exception{
       HashMap<String,String> result=null;
       // first we find out if we have parameters
      int paramstartIx=ts.indexOf('(');
      int paramstoppIx=ts.lastIndexOf(')');
      if((paramstartIx != -1)&&
         (paramstoppIx==ts.length()-1) &&
         (paramstoppIx-paramstartIx >1)) // allow () and ( ) without generating error
      {
          String tmp=ts.substring(paramstartIx);
          result=parseNameValues(tmp,'\'');
          if(result==null)
          {
              throw new Exception("unknown transformation");                 
          }
      }
      return result;
   }
    
     /**
     * Control if today is between two dates.
      * 
     * @param firstdate First legal day
     * @param lastdate Last legal day
     * @return true if today is within span
     * @throws NumberFormatException when the dateformats is wrong. Should be:yyyy-mm-dd
     */
    static public boolean legalDating(String firstdate,String lastdate)
    throws Exception{
        // REMEMBER that the month is 0-based
        Calendar toDay=new GregorianCalendar();

        // allow some different separators
        if(firstdate!=null)
        {
            firstdate=firstdate.replace('-', ':');
            firstdate=firstdate.replace('_', ':');
            firstdate=firstdate.replace('/', ':');
            String[] parts=firstdate.split(":");
            Calendar firstDATE=new GregorianCalendar();
            firstDATE.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1])-1, Integer.parseInt(parts[2]));
            if(firstDATE.after(toDay))
                return false;
        }
        if(lastdate!=null)
        {
            lastdate=lastdate.replace('-', ':');
            lastdate=lastdate.replace('_', ':');
            lastdate=lastdate.replace('/', ':');
            String[] parts=lastdate.split(":");
            Calendar lastDATE=new GregorianCalendar();
            lastDATE.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1])-1, Integer.parseInt(parts[2]));
            if(lastDATE.before(toDay))
                return false;
        }
        return true;
    }
    
     /**
      * Read a String from an URI.
      * 
     * @param uri The URI to read from
     * @return the string read
     * @throws Exception when the text cannot be loaded or the URI is not wellformed
     */
    public static String getTextFile(URI uri,String encoding)
    throws Exception{
        URL url=uri.toURL();
        if(encoding == null)
            encoding="utf-8";
        // while testing: System.out.println(url.toString());
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(),encoding));
        StringBuffer result=new StringBuffer();
        String str;
        //String eol=System.getProperty("line.separator");
        String eol="\n";
        while ((str = in.readLine()) != null) {
            result=result.append(str+eol);
        }
        in.close();
        
        return result.toString();
    }
    
    
    
    /**
     * Produce a list of files with a certain suffix from within a catalog.
     * 
     * @param sourcecat The catalog we will investigate
     * @param filesuf The suffix of the files we are interested in
     * @return a list of filenames
     */
    public static Vector<String> getFileList(String sourcecat,String filesuf)
    {
        Vector<String> vs=new Vector<String>();
        try{
            File f=new File(sourcecat);
            String[] tmp=f.list();
            for(int ix=0;ix<tmp.length;ix++)
                if(tmp[ix].endsWith("."+filesuf))
                    vs.add(tmp[ix]);
            return vs;
        }
        catch(Exception e)
        {
            return vs;
        }
    }
    
    /**
     * Changing & to entity &amp; in string
     * @param S The string we will work on
     * @return The fixed string
     */
    public static String fixUriParameterEntities(String S)
    {
        int pos=S.indexOf('&');
        while(pos!=-1)
        {
            if(!S.substring(pos+1).startsWith("amp;"))
                S=S.substring(0, pos+1)+"amp;"+S.substring(pos+1);
            pos=S.indexOf("&", pos+1);
        }
        return S;
    }
     
   /**
     * Changing & to entity &amp; in URI
     * @param theUri The URI we will work on
     * @return The fixed URI
     */
     public static URI fixUriParameterEntities(URI theUri)
     {
         String theUriS=theUri.toString();
         String fixed=fixUriParameterEntities(theUriS);
         try{
             return new URI(fixed);
         }
         catch(Exception ex)
         {
             return theUri;
         }
     }
     
     /**
      * Tidy a string 
      * @param source The string to tidy
      * @param encoding The encoding we use
      * @param theReporter The reporter we will report to
      * @return The cleaned (tidied) string
      * @throws java.lang.Exception when cannot do it
      */
     public static String tidyAndMakeXML(String source,String encoding)
     throws Exception
     {
         // http://jtidy.sourceforge.net/howto.html
         if(encoding==null)
             encoding=default_encoding;
        source=source.trim();
        while( (!source.startsWith("<")) && (source.length()>10) )
              source=source.substring(1);           //StringBufferInputStream sbis=new StringBufferInputStream(source);
        ByteArrayInputStream bis=new ByteArrayInputStream(source.getBytes());
        ByteArrayOutputStream bos=new ByteArrayOutputStream();

        Tidy tidy=new Tidy();
        tidy.getOnlyErrors();
        //tidy.setXmlOut(true);
        tidy.setXHTML(true);
        tidy.setCharEncoding(Configuration.DOCTYPE_STRICT);
        
        
        boolean reportFromTidy=false;
                
        if(reportFromTidy)
        {
            StringWriter sw=new StringWriter();
            PrintWriter pr=new PrintWriter(sw);        
            tidy.setErrout(pr);
            tidy.setQuiet(true);
            
            tidy.parse(bis, bos);
            
            String report=sw.toString();
            String[]lines=report.split("\n");
            for(int ix=0;ix < lines.length;ix++)
            {
                String line=lines[ix];
                if((line.indexOf("Warning")!=-1) ||(line.indexOf("Error")!=-1))
                    System.out.println("something went wrong in tidy and xml");
            }

        }
        else
        {
            tidy.parse(bis, bos); 
        }
        source=bos.toString();
        

        // add xml prolog if necessary
        if(!source.startsWith("<?xml"))
        {
            String pro="<?xml version=\"1.0\" encoding=\""+encoding+"\"?>\n";
            source=pro+source;
        }
        return source;
     }
     
       /**
      * testing if the resource described by an URI exists
      * @param absUri The address we will test
      * @return true if we can open a steam, otherwise false
      */
     public static boolean resourceExists(URI absUri)
     {
        try{
            InputStream s=absUri.toURL().openStream();
            s.close();
            return true;
        }
        catch(Exception ex)
        {
            return false;
        }
     }
     
     /**
      * Make entities for all std entities
      * @param S The string we will work on
      * @return The result
      */
     public static String setCommonEntities(String S)
     {
        int p1=S.indexOf("&");
        while(p1>-1)
        {
            if((!S.substring(p1).startsWith("&amp;"))&&
               (!S.substring(p1).startsWith("&lt;"))&&
               (!S.substring(p1).startsWith("&gt;"))&&
               (!S.substring(p1).startsWith("&apos;"))&&
               (!S.substring(p1).startsWith("&quote;")))
                    S=S.substring(0,p1)+"&amp;"+S.substring(p1+1);
            p1=S.indexOf("&",p1+1);
        }
        S=S.replaceAll("<", "&lt;");
        S=S.replaceAll(">", "&gt;");
        S=S.replaceAll("'", "&apos;");
        S=S.replaceAll("\"", "&quot;");  
        return S;
     }
     
    
}

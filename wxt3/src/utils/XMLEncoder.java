package utils;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.xml.sax.ext.DefaultHandler2;

/**
 * Generates colordcoded XML-code by parsing the string
 * Styles:
 * xmlcode, as wrapper
 * tagname
 * attname
 * attvalue
 * process
 * ------------------------
 * parseText is the only entrypoint
 */
public class XMLEncoder extends DefaultHandler2
{
    static final int START=1;
    static final int END=2;
    static final int CHARACTER=3;
    static final int PI=4;
    static final int START_CDATA=5;
    static final int END_CDATA=6;
    static final int COMMENT=7;
    static final int DTD=8;
    static final int END_DTD=9;
    static final int START_ENTITY=10;
    static final int END_ENTITY=11;

    static final String WORD="kwd";
    static final String COM="com";
    static final String LITERAL="str";
    static final String TAGNAME="tag";
    static final String ATTNAME="atn";
    static final String ATTVALUE="atv";

    static int num=1;// testing puposes

    // VERY primitive, but it works until I find out
    // a little more about entity resolving
    static final String _AMP_="_AMP_REPLACER_";
    static final String _QUOT_="_QUOT_REPLACER_";
    static final String _APOS_="_APOS_REPLACER_";

    static final String EOL=codeFormatter.EOL;

    // keep track of element depth, lines and indentation
    static final String TAB_WIDTH="  ";
    static final int MAX_LINEWIDTH=80;

    private String result="";

    private StringBuilder bResult=null;

    private String xmlheader="";
    private int lastDone=0;
    int elementDepth=0;
    String elementIndent="";

    // if we want to discriminated XML and HTML
    // which we does not at the moment
    boolean isHTML=false;

    /**
     * constructor
     */
    public XMLEncoder(boolean makehtml)
    {
	isHTML=makehtml;
    }

    /**
     * Dividing very long attribute values to make them printable
     * @param v The attribute value
     * @param maxlength
     * @param indent Indentation before parts
     * @return The returned string
     */
    public static String chopUpAttributeValue(String v,int maxlength,int indent)
    {
        // attempts to break on doublespace
        String breaker="  ";
        while(v.indexOf("   ")!=-1)
            v=v.replaceAll("   ", "  ");
        // no double, we try single
        if(v.indexOf(breaker)==-1)
            breaker=" ";
        if(v.indexOf(breaker)!=-1)
        {
            StringBuffer b=new StringBuffer(EOL);
            for(int ix=0;ix<indent;ix++)b.append(' ');
            String ret=v.replaceAll(breaker, EOL+b.toString());
            return ret;
        }
        // no space what shall we do

        return v;
    }

    /**
     * Make the heading, before document-element, root
     * and possible connections to styleshets etc
     * @param source
     * @return true if meaningfull, false otherwise
     */
    private boolean produceHeader(String source,String encoding)
    {
        source=source.trim();
        int pos1=source.indexOf("<?xml ");
        if(pos1!=0)
        {
            // source does not start with normal xml header
            return false;
        }
        int pos2=source.indexOf("?>",pos1);
        if(pos2==-1)
        {
            // source does not start with normal xml header
            return false;
        }

        xmlheader=source.substring(0, pos2+2);
        xmlheader=xmlheader.replace("<", "&lt;");
        xmlheader=xmlheader.replace(">", "&gt;");
        lastDone=PI; // prepare for lineshift before new PI
        return true;
    }

    /**
     * Parse the string and produce colorcoded xhtml
     * @param source The string to parse
     * @param enc The actual encoding
     * @return The prepared string,or null if we encounter problems
     */
    public String parseText(String source,String enc,reporter theReporter)
    {

        source=source.replaceAll("\t", "  ");
        // want these std entities to survive
        source=source.replaceAll("\\Q&amp;\\E",_AMP_);
        source=source.replaceAll("\\Q&quot;\\E",_QUOT_);
        source=source.replaceAll("\\Q&apos;\\E",_APOS_);


        try {
            Boolean ok=produceHeader(source,enc);
            if(!ok)
                throw new Exception();
            //get a factory
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setFeature("http://xml.org/sax/features/namespaces", true);
            spf.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
            spf.setFeature("http://xml.org/sax/features/use-entity-resolver2",false);
            // necessary for speed:
            spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",false);
            spf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar",false);
            //spf.setFeature("http://apache.org/xml/features/dom/include-ignorable-whitespace",true);
            //spf.setFeature("http://xml.org/sax/features/lexical-handler/parameter-entities",true);

            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            // need this to pick up doctype (and entities ?)
            sp.setProperty ("http://xml.org/sax/properties/lexical-handler", this);


            //parse the string and also register this class for call backs
            source=source.trim();
            InputSource inSource= new InputSource(new StringReader(source));

            elementDepth=0;

            // set global collector
            bResult=new StringBuilder(source.length()*20);

            sp.parse(inSource, this);

            /*
             int start=0;
            while((start=bResult.indexOf(_AMP_, start))!=-1)
                bResult.replace(start, start+5, "&amp;");
            start=0;
            while((start=bResult.indexOf(_QUOT_, start))!=-1)
                bResult.replace(start, start+6, "&quot;");
            start=0;
            while((start=bResult.indexOf(_APOS_, start))!=-1)
                bResult.replace(start, start+6, "&apos;");
            */
            result=bResult.toString();

            result=result.replaceAll("\\Q"+_AMP_+"\\E", "&amp;");
            result=result.replaceAll("\\Q"+_QUOT_+"\\E", "&quot;");
            result=result.replaceAll("\\Q"+_APOS_+"\\E", "&apos;");
            

            String S=result;
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

            result=S;

            return result;

        }
        catch(SAXException se) {
                System.out.println(se.getMessage());
                theReporter.pushSimpleMessage(se.getMessage());
                return null;
            }
        catch(ParserConfigurationException pce) {
                    System.out.println(pce.getMessage());
                    theReporter.pushSimpleMessage(pce.getMessage());
                    return null;
            }
        catch (IOException ie) {
                    System.out.println(ie.getMessage());
                    theReporter.pushSimpleMessage(ie.getMessage());
                    return null;
            }
        catch(Exception nex){
                    System.out.println(nex.getMessage());//nex.printStackTrace();
                    theReporter.pushSimpleMessage(nex.getMessage());
                    return null;
            }


	}



	//Event Handlers
        @Override
	public void startElement(String uri, String localName, String qName, Attributes attributes)
        throws SAXException {
		String eltString="";
        setIndent();

        int onelineLength=qName.length()+3+elementIndent.length();
        if(attributes!=null)
        {
            for(int ix=0;ix < attributes.getLength();ix++)
            {
                String name=attributes.getLocalName(ix);
                String name2=attributes.getQName(ix);
                if(name.isEmpty())
                    name=name2;

                String v=attributes.getValue(ix);
                // if this is too long we should attempt to break it
                if((v!=null) && (v.length() > (MAX_LINEWIDTH+elementIndent.length()+qName.length())))
                    v=chopUpAttributeValue(v, MAX_LINEWIDTH,elementIndent.length()+qName.length());

                String newattribute=" <span class=\""+ATTNAME+"\">"+name+"</span>=\"<span class=\""+ATTVALUE+"\">"+v+"</span>\"";
                int thislength=name.length()+v.length()+3;
                if((onelineLength + thislength) >MAX_LINEWIDTH)
                {
                    eltString+=EOL+elementIndent;
                    int tix=-1;
                    while(tix++ <localName.length())
                        eltString+=" ";
                    onelineLength=tix+elementIndent.length()+thislength;
                }
                else
                    onelineLength+=thislength;

                eltString+=newattribute;
            }
        }
        if(lastDone!=CHARACTER)
            bResult.append(EOL);//result+=EOL;
	bResult.append("&lt;<span class=\""+TAGNAME+"\">"+qName+"</span>"+eltString+"&gt;");//result += "&lt;<span class=\""+TAGNAME+"\">"+qName+"</span>"+eltString+"&gt;";
        lastDone=START;

        elementDepth+=1;
	}



    @Override
	public void processingInstruction(String target, String data)
        {
            if(lastDone==PI)
                bResult.append(EOL);//result+=EOL;
            bResult.append("&lt;?<span class=\""+ATTNAME+"\">"+target+"</span> <span class=\""+ATTVALUE+"\">"+data+"</span>?&gt;");//result+="&lt;?<span class=\""+ATTNAME+"\">"+target+"</span> <span class=\""+ATTVALUE+"\">"+data+"</span>?&gt;";
            lastDone=PI;
        }

    @Override
	public void characters(char[] ch, int start, int length)
        throws SAXException {
		String tmp=new String(ch,start,length);

        if(tmp.indexOf("<")!=-1)
        {
            tmp=tmp.replaceAll("<", "&lt;");
            tmp=tmp.replaceAll(">", "&gt;");
            tmp="&lt;![CDATA["+tmp+"]]&gt;";
        }

        bResult.append(tmp);//result += tmp;
        lastDone=CHARACTER;
	}
    @Override
    public void comment(char[] ch, int start, int length)
    throws SAXException {
        String t=new String(ch,start,length);
        bResult.append("&lt;!--<span class=\""+COM+"\">"+t+"</span>--&gt;");//result+="&lt;!--<span class=\""+COM+"\">"+t+"</span>--&gt;";
        lastDone=COMMENT;
    }

    @Override
     public void startCDATA()
     {
         //result+="&lt;![CDATA[";
         lastDone=START_CDATA;
     }

    @Override
    public void endCDATA()
    {
        //result+="]]&gt;";
        lastDone=END_CDATA;
    }

    @Override
	public void endElement(String uri, String localName, String qName)
        throws SAXException {
		//reset
		if(lastDone==START)
                {
                    bResult.replace(bResult.length()-4, bResult.length(), "/&gt;");
                    //result=result.substring(0,result.length()-4)+"/&gt;";
                }
        else
        {
            if (lastDone==END)
                bResult.append(EOL);//result+=EOL;
            bResult.append( "&lt;/<span class=\""+TAGNAME+"\">"+qName+"</span>&gt;");//result += "&lt;/<span class=\""+TAGNAME+"\">"+qName+"</span>&gt;";
        }
        lastDone=END;
        elementDepth-=1;

	}

    @Override
    public void startDocument()
    {
        bResult.append(xmlheader+EOL);//result=xmlheader+EOL;
    }

    @Override
    public void endDocument()
    {
        //;
    }

    @Override
    public void startDTD(String name, String publicId, String systemId)
    {
        // this code may need a little bit correcting
        bResult.append("\n&lt;!DOCTYPE " +name);//result+="\n&lt;!DOCTYPE " +name;
        if(publicId!=null)
        {
            bResult.append(" PUBLIC "+"\""+publicId+"\"");//result=result+" PUBLIC "+"\""+publicId+"\"";
            if(systemId!=null)
                bResult.append(EOL+"            "+"\""+publicId+"\"");//result=result+EOL+"            "+"\""+publicId+"\"";
        }
        else if(systemId!=null)
            bResult.append(" SYSTEM "+"\""+systemId+"\"");//result=result+" SYSTEM "+"\""+systemId+"\"";

        bResult.append("&gt;"+EOL);//result=result+"&gt;"+EOL;

        lastDone=DTD;
    }

    @Override
    public void endDTD()
    {
        lastDone=END_DTD;
    }


    @Override
    public void startEntity(String name)
    {
        lastDone=START_ENTITY;
    }

    @Override
    public void endEntity(String name)
    {
        lastDone=END_ENTITY;
    }


    private void setIndent()
    {
        elementIndent="";
        for(int ix=0;ix<elementDepth;ix++)
            elementIndent+=TAB_WIDTH;
    }
}





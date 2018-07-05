/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utils;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.net.URI;

/**
 *
 * @author Administrator
 */
public class useJEuclid
{

    static public Dimension buildImage(URI absMmlLocation,URI absImageLocation,PIcommand cmd)
            throws Exception
    {
        Color foreColor=null;
        Color backColor=null;
        float fontSize=0.0f;
        // use cmd to pick up:fontsize,color,backgroundcolor etc ?
        if(cmd.paramExist(PIcommand.COLOR))
        {
            String tmp=cmd.getValue(PIcommand.COLOR);
            foreColor=javaColor(tmp);
        }
        if(cmd.paramExist(PIcommand.BACKCOLOR))
        {
            String tmp=cmd.getValue(PIcommand.BACKCOLOR);
            backColor=javaColor(tmp);
        }
        if(cmd.paramExist(PIcommand.FONTSIZE))
        {
            try{
                fontSize=Float.parseFloat(cmd.getValue(PIcommand.FONTSIZE));
            }
            catch(Exception ex)
            {
                throw (new Exception("bad format in fontsize"));
            }
        }
        Dimension d=null;
        // do as in project c:\projects\mathmldata\jeuclidtest

        /*
        Converter con=Converter.getInstance();
        MutableLayoutContext params = new LayoutContextImpl(LayoutContextImpl.getDefaultLayoutContext());
        if(fontSize > 0.0f)
            params.setParameter(Parameter.MATHSIZE, 20f);
        if(foreColor != null)
            params.setParameter(Parameter.MATHCOLOR , foreColor);
        if(backColor!=null)
            params.setParameter(Parameter.MATHBACKGROUND , backColor);

        File in=new File(absMmlLocation);
        File out=new File(absImageLocation); // must be png

        try{
               d= con.convert(in,out,"image/png",params);
        }
        catch(Exception ex2)
        {
            System.out.println(ex2.getMessage());
            throw new Exception(ex2.getMessage());
        }
         */
       return d;
    }

    static Color javaColor(String CssColor)
    {
        try{
            if((CssColor.startsWith("#") && CssColor.length()==7))
            {

                Integer.parseInt(CssColor.substring(1,3), 16);
                return new Color(Integer.parseInt(CssColor.substring(1,3), 16),
                                 Integer.parseInt(CssColor.substring(3,5), 16),
                                 Integer.parseInt(CssColor.substring(5),16)
                                 ,0);
            }
            return Color.getColor(CssColor); // may be null
        }
        catch(Exception ex)
        {
            return null;
        }
    }

}

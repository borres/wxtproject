package converter;
/*
From:
http://forge.scilab.org/index.php/p/scidoc/source/tree/af93aac84dc67af12da7990e86fceb02ccaceb04/src/org/scilab/forge/scidoc/image/MathMLImageConverter.java
 */

/*
 * Scilab ( http://www.scilab.org/ ) - This file is part of Scilab
 * Copyright (C) 2010 - Calixte DENIZET
 *
 * This file must be used under the terms of the CeCILL.
 * This source file is licensed as described in the file COPYING, which
 * you should have received as part of this distribution.  The terms
 * are also available at
 * http://www.cecill.info/licences/Licence_CeCILL_V2-en.txt
 *
 */
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


import java.net.URI;
import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import net.sourceforge.jeuclid.MathMLParserSupport;
import net.sourceforge.jeuclid.layout.JEuclidView;
import net.sourceforge.jeuclid.context.LayoutContextImpl;
import net.sourceforge.jeuclid.context.Parameter;

import utils.PIcommand;
import utils.accessutils;

/**
 * A MathML to PNG converter
 * @author bs
 */
public class mathmlToImage {

    private static final Graphics2D TEMPGRAPHIC =
            new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();

    private mathmlToImage() {
    }

    /**
     * @param mathmlStr the string to convert
     * @param fontSize the font size
     * @return a bufferede image
     */
    public static BufferedImage convertMathML(String mathmlStr, int fontSize) {

        Document doc = null;
        try {
            doc = MathMLParserSupport.parseString(mathmlStr);
        } catch (final SAXException e) {
            return null;
        } catch (final ParserConfigurationException e) {
            return null;
        } catch (final IOException e) {
            return null;
        }

        LayoutContextImpl parameters = new LayoutContextImpl(LayoutContextImpl.getDefaultLayoutContext());
        parameters.setParameter(Parameter.MATHSIZE, fontSize);
        JEuclidView jev = new JEuclidView((Node) doc, parameters, TEMPGRAPHIC);

        int width = (int) Math.ceil(jev.getWidth());
        int ascent = (int) Math.ceil(jev.getAscentHeight());
        int height = (int) Math.ceil(jev.getDescentHeight()) + ascent;

        if (width <= 0 || height <= 0) {
            return null;
        }

        BufferedImage bimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = bimg.createGraphics();
        
        g2d.setColor(new Color(255, 255, 255, 0));
        g2d.fillRect(0, 0, width, height);
        
        jev.draw(g2d, 0, ascent);
        //jev.draw(g2d, width, height);
        g2d.dispose();
        return bimg;
    }

    /**
     * Make a image from a mahtml string
     * @param absMmlLocation Location of the mathml-string
     * @param absImageLocation Location of the image
     * @param cmd The PIcommand describing the request
     * @return The size of the image
     * @throws Exception 
     */
    static public Dimension buildImage(URI absMmlLocation, URI absImageLocation, PIcommand cmd)
            throws Exception {
        
         String MathMLStr=accessutils.getTextFile(absMmlLocation, "utf-8");
         Document doc = null;
         try {
            doc = MathMLParserSupport.parseString(MathMLStr);
        } catch (final SAXException e) {
            return null;
        } catch (final ParserConfigurationException e) {
            return null;
        } catch (final IOException e) {
            return null;
        }
       
        
        Color foreColor = Color.BLACK;
        Color backColor = Color.WHITE;
        float fontSize = 16.0f;
        
        if(cmd!=null){
            // use cmd to pick up:fontsize,color,backgroundcolor etc ?
            if (cmd.paramExist(PIcommand.COLOR)) {
                String tmp = cmd.getValue(PIcommand.COLOR);
                foreColor = javaColor(tmp);
                if (foreColor == null) {
                    foreColor = Color.BLACK;
                }
            }
            if (cmd.paramExist(PIcommand.BACKCOLOR)) {
                String tmp = cmd.getValue(PIcommand.BACKCOLOR);
                backColor = javaColor(tmp);
                if (backColor == null) {
                    backColor = Color.WHITE;
                }
            }
            if (cmd.paramExist(PIcommand.FONTSIZE)) {
                try {
                    fontSize = Float.parseFloat(cmd.getValue(PIcommand.FONTSIZE));
                } catch (Exception ex) {
                    throw (new Exception("bad format in fontsize"));
                }
            }
        }
        LayoutContextImpl parameters = new LayoutContextImpl(LayoutContextImpl.getDefaultLayoutContext());
        parameters.setParameter(Parameter.MATHSIZE, fontSize);
        JEuclidView jev = new JEuclidView((Node) doc, parameters, TEMPGRAPHIC);

        int width = (int) Math.ceil(jev.getWidth());
        int ascent = (int) Math.ceil(jev.getAscentHeight());
        int height = (int) Math.ceil(jev.getDescentHeight()) + ascent;

        if (width <= 0 || height <= 0) {
            return null;
        }

        BufferedImage bimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = bimg.createGraphics();
        
        g2d.setColor(new Color(255, 255, 255, 0));
        // and backcolor and fontsize
        g2d.fillRect(0, 0, width, height);
        
        jev.draw(g2d, 0, ascent);


        File outputfile=new File(absImageLocation);
        ImageIO.write(bimg, "png", outputfile);
        g2d.dispose();
        return new Dimension(width,height);
         
    }
    

    static private Color javaColor(String CssColor) {
        try {
            if ((CssColor.startsWith("#") && CssColor.length() == 7)) {

                Integer.parseInt(CssColor.substring(1, 3), 16);
                return new Color(Integer.parseInt(CssColor.substring(1, 3), 16),
                        Integer.parseInt(CssColor.substring(3, 5), 16),
                        Integer.parseInt(CssColor.substring(5), 16), 0);
            }
            return Color.getColor(CssColor); // may be null
        } catch (Exception ex) {
            return null;
        }
    }

}

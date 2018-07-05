<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="xml" 
     version="1.0" 
     encoding="ISO-8859-1" 
     indent="yes" 
     omit-xml-declaration="yes"
     doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"  
     doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"/>
     
     <xsl:param name="OL" select="'Barcelona'"/>
     <xsl:param name="DISTANCE" select="'100m'"/>

<xsl:template match="/">
    <html>
      <head>
        <link rel="STYLESHEET" href="olymp5.css"/>
         <title>Olympics</title>
       </head>
       <body>
         <xsl:apply-templates select="IOC/OlympicGame[@place=$OL]">
         <xsl:sort select="@year" order="ascending"/>
         </xsl:apply-templates>
        </body>
    </html>
</xsl:template>
	
<xsl:template match="OlympicGame">
	<h3><xsl:value-of select="@place"/></h3>
	<xsl:apply-templates select="event[@track=$DISTANCE]"/>
</xsl:template>
	
<xsl:template match="//event">
	<table>
	<tr colspan="3">
		<th><xsl:value-of select="@track"/></th>
	</tr>
	<xsl:apply-templates select="athlet">
		<xsl:sort  data-type="number" select="result"/>
	</xsl:apply-templates>
	</table> 
</xsl:template>
	
<xsl:template match="athlet">
	<tr>
		<td><xsl:value-of select="name"/></td>
		<td><xsl:value-of select="nation"/></td>
		<td><xsl:value-of select="result"/></td>
	</tr>
</xsl:template>
</xsl:stylesheet>
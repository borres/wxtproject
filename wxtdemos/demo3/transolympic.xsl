<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:fo="http://www.w3.org/1999/XSL/Format">
<xsl:output method="html" 
				doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"
				doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"
				 encoding="UTF-8"/>

<xsl:param name="place" select="'Barcelona'"/>
<xsl:param name="distance" select="'100m'"/>

<xsl:template match="/">
	<xsl:copy-of select="/html/head"/>
	<xsl:apply-templates select="//body"/>
</xsl:template>

<xsl:template match="//body">
	<body>
		<table>
		<tr>
			<xsl:apply-templates select="table/tr/td[position()='1']"/>		
		<td valign="top" class="maincontent">
			<xsl:apply-templates select="//IOC"/>
		</td>
		</tr>
		</table>
	</body>
</xsl:template>

<xsl:template match="td">
	<xsl:copy-of select="."/>
</xsl:template>

<xsl:template match="//IOC">
	<h2>Resultat</h2>
	<h3><xsl:value-of select="$place"/> - <xsl:value-of select="$distance"/></h3>
	<xsl:apply-templates select="OlympicGame[@place=$place]"/>
</xsl:template>

<xsl:template match="//OlympicGame">
	<xsl:apply-templates select="event[@track=$distance]"/>
</xsl:template>

<xsl:template match="//event">
<table>
		<tr>
			<th>Name</th><th>Nation</th><th>Result</th>
		</tr>
		<xsl:apply-templates select="athlet"/>
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

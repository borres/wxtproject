<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:fo="http://www.w3.org/1999/XSL/Format">
<xsl:output method="xml" 
				 encoding="UTF-8"/>

<xsl:param name="place" select="'Barcelona'"/>
<xsl:param name="distance" select="'100m'"/>

<xsl:template match="/">
	<root>
	<xsl:apply-templates select="//IOC"/>
	</root>
</xsl:template>


<xsl:template match="//IOC">
	<body>
	<h2>Resultat</h2>
	<h3><xsl:value-of select="$place"/> - <xsl:value-of select="$distance"/></h3>
	<xsl:apply-templates select="OlympicGame[@place=$place]"/>
	</body>
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

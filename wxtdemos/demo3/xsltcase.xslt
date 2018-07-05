<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" 
						xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" 
					 version="1.0" 
					 encoding="UTF-8" 
					 indent="yes"/>
<xsl:template match="/">
<geonames>
	<xsl:apply-templates select="//country"/>
</geonames>
</xsl:template>
<!-- for each country -->
<xsl:template match="country">
	<xsl:element name="country">
		<xsl:attribute name="geonameId">
			<xsl:value-of select="geonameId"/>
		</xsl:attribute>
		<xsl:attribute name="countryName">
			<xsl:value-of select="countryName"/>
		</xsl:attribute>
	</xsl:element>
</xsl:template>
</xsl:stylesheet>

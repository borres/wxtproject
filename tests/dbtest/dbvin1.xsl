<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" 
    encoding="UTF-8" 
    indent="yes" />

<xsl:param name="dice"  select="'6'" />
<xsl:param name="type"  select="'red'" />
    
<xsl:template match="/">
  <div id="root">
    <h1>Wines from France</h1>
    <p style="font-weight:bold"><xsl:value-of select="$type"/> wines with dice <xsl:value-of select="$dice"/></p>
    <ul>
		<xsl:apply-templates select="//query[@id='1']/record[$dice=dice and $type=type]"/>
	</ul>
   </div>
</xsl:template>   

   
<xsl:template match="//record">
  <li><xsl:value-of select="name"/></li>
</xsl:template>

</xsl:stylesheet>


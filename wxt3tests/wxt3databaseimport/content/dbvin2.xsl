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
    <h1>Wines</h1>
    <p>(Norwegian text)</p>
    <dl>
	<xsl:apply-templates select="//query[@id='2']/record[$dice=dice and $type=type]"/>
  </dl>
  </div>
</xsl:template>   

   
<xsl:template match="//record">
  <dt><xsl:value-of select="name"/></dt>
  <dd><xsl:value-of select="description"/></dd>
</xsl:template>

</xsl:stylesheet>


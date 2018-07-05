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
    <h1>Viner</h1>
    <xsl:apply-templates select="//query[@id='2']/record[$dice=dice and $type=type]"/>
   </div>
</xsl:template>   

   
<xsl:template match="//record">
 <div>
  <xsl:value-of select="description"/>
  </div>
</xsl:template>

</xsl:stylesheet>


<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" 
    encoding="UTF-8" 
    indent="yes" />

<xsl:param name="dice"  select="'6'" />
    
<xsl:template match="/">
  <div id="root">
    <h1>Viner</h1>
    <xsl:apply-templates select="//query[@id='1']/record[$dice=dice]"/>
   </div>
</xsl:template>   

   
<xsl:template match="//record">
 <div>
  <xsl:value-of select="name"/>
  </div>
</xsl:template>

</xsl:stylesheet>


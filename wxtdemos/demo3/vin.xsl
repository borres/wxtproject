<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" 
    encoding="ISO-8859-1" 
    indent="yes" />

<xsl:param name="country"  select="'Portugal'" />
<xsl:param name="dice"  select="'6'" />
<xsl:param name="type"  select="'red'" />
    
<xsl:template match="/">
  <div id="root">
    <xsl:comment>start</xsl:comment>
    <h1>Vinliste</h1>
    <xsl:call-template name="ingress"/>
    <xsl:apply-templates select="//wine[$type=type  and $dice=dice  and $country=country]">
     <xsl:sort select="type" order="ascending"/>
     <xsl:sort select="dice" order="descending"/>
     <xsl:sort select="price/volume" order="descending"/>
     </xsl:apply-templates>
     <xsl:comment>stop</xsl:comment>
   </div>
</xsl:template>   

<xsl:template name="ingress">
  <p>
  <xsl:value-of select="$type"/> wines from <xsl:value-of select="$country"/>
  </p>
  <hr size="1"/>
</xsl:template>
    
<xsl:template match="//wine">
 <div style="margin-top:20px">
  <div style="float:left">
  <xsl:element name="img">
   <xsl:attribute name="style">margin:10px</xsl:attribute>
    <xsl:attribute name="src">http://www.ia.hiof.no/~borres/commondata/vin/<xsl:value-of select="type"/>
    <xsl:value-of select="dice"/>.gif</xsl:attribute>
    <xsl:attribute name="alt">dice</xsl:attribute>
  </xsl:element>
  </div>
  <div>
   <xsl:value-of select="name"/>
  </div>
  <div>
   <span>
     kr <xsl:value-of select="price"/> / <xsl:value-of select="volume"/>cl
   </span>
  </div>
  <div style="clear:left"/>
   <div >
    <xsl:value-of select="description"/>
   </div>
  </div>
</xsl:template>

</xsl:stylesheet>


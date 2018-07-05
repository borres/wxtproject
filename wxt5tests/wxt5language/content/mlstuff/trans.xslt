<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>
  <xsl:param name="theCountry" select="'Frankrike'"/>
  <xsl:param name="theType" select="'red'"/>
  <xsl:template match="/">
    "viner":[
    <xsl:apply-templates select="//wine[country=$theCountry and type=$theType]">
      <xsl:sort order="descending" select="dice"/>
    </xsl:apply-templates>
    ]
  </xsl:template>
  <xsl:template match="//wine">
    {"terning":"<xsl:value-of select="dice"/>",
     "navn":"<xsl:value-of select="name"/>",
    "beskrivelse":"<xsl:value-of select="description"/>"},
  </xsl:template>
</xsl:stylesheet> 
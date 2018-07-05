<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" >

<xsl:output method="xml" 
            encoding="UTF-8" 
            indent="yes" 
            standalone="yes" 
            doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" />

<xsl:template match="/">
    <div class="dbtables">
        <xsl:apply-templates select="/root/query"/>
    </div>
</xsl:template>

<xsl:template match="//query">
<div class="dbresult">
    <table>
    <xsl:call-template name="tableheader">
        <xsl:with-param name="nodelist" select="record[position()=1]/child::*"/>
    </xsl:call-template>
    <tbody>
        <xsl:apply-templates select="record"/>
    </tbody>
    </table>
</div>
</xsl:template>

<xsl:template name="tableheader">
    <xsl:param name="nodelist"/>	
    <thead>
    <tr>
        <xsl:for-each select="$nodelist">
            <th><xsl:value-of select="local-name()"/></th>
        </xsl:for-each>
    </tr>
    </thead>
</xsl:template>

<xsl:template match="//record">
    <tr>
        <xsl:for-each select="child::*">
            <td><xsl:value-of select="."/></td>
        </xsl:for-each>
    </tr>
</xsl:template>

</xsl:stylesheet>

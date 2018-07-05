<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">
<xsl:output method="html" encoding="utf-8" />

<xsl:template match="/">
<table>
	<tbody>
		<xsl:call-template name="header"/>
		<xsl:apply-templates select="resultset/row"/>
	</tbody>
</table>
</xsl:template>

<xsl:template match="row">
<tr>
	<xsl:apply-templates select="field"/>
</tr>
</xsl:template>

<xsl:template match="field">
<td>
	<xsl:value-of select="."/>
</td>
</xsl:template>

<xsl:template name="header">
	<tr>
		<xsl:for-each select="//row[position()=1]/field">
		<th><xsl:value-of select="@name"/></th>
		</xsl:for-each>
	</tr>
</xsl:template>

</xsl:stylesheet>

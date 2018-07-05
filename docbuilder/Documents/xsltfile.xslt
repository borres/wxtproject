<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
        xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0"
        xmlns:draw="urn:oasis:names:tc:opendocument:xmlns:drawing:1.0"
        xmlns:xlink="http://www.w3.org/1999/xlink"
        xmlns="http://www.w3.org/1999/xhtml"
        xml:lang="en"
	>

<xsl:output version="1.0" method="xml" encoding="UTF-8" omit-xml-declaration="yes" />

    <!-- main -->
    <xsl:template match="/">
        <html>
	  <head>
                <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<meta http-equiv="Content-Style-Type" content="text/css" />
                <title>Odt2Wiki - generated document</title>
            </head>
            <body class="mediawiki">
                <div id="globalWrapper">
                    <div id="column-content">
                        <xsl:apply-templates/>
                    </div>
                </div>
            </body>
        </html>
    </xsl:template>
<!-- end main -->


<!-- lists -->
    <xsl:template match="text:list">
        <ul><xsl:apply-templates/></ul>
    </xsl:template>

<!-- list items-->
    <xsl:template match="text:list-item">
        <li><xsl:apply-templates/></li>
    </xsl:template>
<!-- end lists -->


<!-- Headings -->
    <xsl:template name="text1" match="text:h">
	<!-- h1 -->
	<xsl:if test="@text:outline-level='1'">
		<h1 class="firstHeading"><xsl:apply-templates/></h1>
	</xsl:if>
	<!-- h2 -->
	<xsl:if test="@text:outline-level='2'">
		<h2><span class="mw-headline"><xsl:apply-templates/></span></h2>
	</xsl:if>
	<!-- h3 -->
	<xsl:if test="@text:outline-level='3'">
		<h3><span class="mw-headline"><xsl:apply-templates/></span></h3>
	</xsl:if>
	<!-- h4 -->
	<xsl:if test="@text:outline-level='4'">
		<h4><span class="mw-headline"><xsl:apply-templates/></span></h4>
	</xsl:if>
	<!-- h5 -->
	<xsl:if test="@text:outline-level='5'">
		<h5><span class="mw-headline"><xsl:apply-templates/></span></h5>
	</xsl:if>
	<!-- h6 -->
	<xsl:if test="@text:outline-level='6'">
		<h6><span class="mw-headline"><xsl:apply-templates/></span></h6>
        </xsl:if>
    </xsl:template>
<!-- end headings -->


<!-- Tables -->
<xsl:template match="table:table">
    <table>
        <xsl:apply-templates/>
    </table>
</xsl:template>
<!-- Tables: row -->
<xsl:template match="table:table-row">
    <tr>
        <xsl:apply-templates/>
    </tr>
</xsl:template>
<!-- Tables: cell -->
<xsl:template match="table:table-cell">
    <td>
        <xsl:apply-templates/>
    </td>
</xsl:template>
<!-- end tables -->


<!-- Images-->
<xsl:template match="draw:image" name="image">
    <img>
        <xsl:attribute name="src"><xsl:value-of select="@xlink:href"/></xsl:attribute>
        <xsl:attribute name="class">image</xsl:attribute>
        <xsl:attribute name="title"><xsl:value-of select="../@draw:name" /></xsl:attribute>
	<xsl:attribute name="alt"><xsl:value-of select="../@draw:name" /></xsl:attribute>
    </img>
</xsl:template>

<xsl:template match="draw:a"><!-- Image links-->
    <a>
    <xsl:attribute name="href"><xsl:value-of select="@xlink:href"/></xsl:attribute>
    <xsl:apply-templates/>
    </a>
</xsl:template>
<!-- end images -->


<!-- Links -->
<xsl:template match="text:a">
    <a><xsl:attribute name="href"><xsl:value-of select="@xlink:href"/></xsl:attribute>
    <xsl:attribute name="title"><xsl:value-of select="."/></xsl:attribute>
    <xsl:value-of select="."/>
    </a>
</xsl:template>
<!-- end links -->


<!-- bookmarks -->
<xsl:template match="text:bookmark">
	<a>
	<xsl:attribute name="name"><xsl:value-of select="@text:name"/></xsl:attribute>
	<xsl:attribute name="id"><xsl:value-of select="@text:name"/></xsl:attribute>
	</a>
</xsl:template>
<!-- end bookmarks -->


<!-- break line-->
<xsl:template match="text:line-break">
    <br /><xsl:apply-templates/>
</xsl:template>
<!-- end break line -->

<!-- text paragraphs -->
<xsl:template match="text:p[ .!='' ]">
  <p>
    <xsl:apply-templates/>
    </p>
</xsl:template>
<!-- end text paragraphs -->


</xsl:stylesheet>
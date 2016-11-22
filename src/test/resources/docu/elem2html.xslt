<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="root">
		<html>
		<body>
		<h1><xsl:value-of select="@titel"/></h1>
		<h2><xsl:value-of select="@inhalt"/></h2>
        <xsl:apply-templates select="lib" mode="inhalt"/>
        <xsl:apply-templates select="lib" mode="full"/>
		</body>
		</html>
	</xsl:template>

	<xsl:template match="lib" mode="inhalt">
		<xsl:value-of select="position()"/>.
	    <xsl:element name="a">
			<xsl:attribute name="href">#link<xsl:value-of select="position()"/></xsl:attribute>
	        <xsl:value-of select="@name"/>
	     </xsl:element>
		<br/>
        <xsl:apply-templates select="element" mode="inhalt">
			<xsl:with-param name="number" select="position()"/>
        </xsl:apply-templates>
  	</xsl:template>

	<xsl:template match="element" mode="inhalt">
		<xsl:param name="number" />
		<xsl:value-of select="$number"/>.<xsl:value-of select="position()"/>. <xsl:value-of select="@name"/><br/>
  	</xsl:template>

	<xsl:template match="lib" mode="full">
		<hr/>
	    <h3>
			<xsl:element name="a">
		        <xsl:attribute name="name">link<xsl:value-of select="position()"/></xsl:attribute>
				<xsl:value-of select="position()"/>.
		        <xsl:value-of select="@name"/>
			</xsl:element>
	    </h3>
        <xsl:apply-templates select="element" mode="full"/>
  	</xsl:template>

	<xsl:template match="element" mode="full">
		<hr/>
		<xsl:element name="img">
	        <xsl:attribute name="src">img/<xsl:value-of select="@img"/>.png</xsl:attribute>
	        <xsl:attribute name="align">right</xsl:attribute>
		</xsl:element>
	    <h4><xsl:value-of select="@name"/></h4>
		<p><xsl:value-of select="descr"/></p>
        <xsl:apply-templates select="inputs"/>
        <xsl:apply-templates select="outputs"/>
        <xsl:apply-templates select="attributes"/>
  	</xsl:template>

	<xsl:template match="inputs">
		<h4><xsl:value-of select="@name"/></h4>
		<dl>
		<xsl:apply-templates select="pin"/>
		</dl>
  	</xsl:template>

	<xsl:template match="outputs">
		<h4><xsl:value-of select="@name"/></h4>
		<dl>
		<xsl:apply-templates select="pin"/>
		</dl>
  	</xsl:template>

	<xsl:template match="pin">
		<dt><i><xsl:value-of select="@name"/></i></dt>
		<dd><xsl:value-of select="."/></dd>
  	</xsl:template>

	<xsl:template match="attributes">
		<h4><xsl:value-of select="@name"/></h4>
		<dl>
		<xsl:apply-templates select="attr"/>
		</dl>
  	</xsl:template>

	<xsl:template match="attr">
		<dt><i><xsl:value-of select="@name"/></i></dt>
		<dd><xsl:value-of select="."/></dd>
  	</xsl:template>

</xsl:stylesheet>

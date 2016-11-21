<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
				xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
				xmlns:fo="http://www.w3.org/1999/XSL/Format">

	<xsl:template match="root">
		<fo:root font-family="SansSerif" font-size="11pt">
			<fo:layout-master-set>
				<fo:simple-page-master master-name="DIN-A4"
									   page-height="29.7cm" page-width="21cm"
									   margin-top="2cm"     margin-bottom="1cm"
									   margin-left="2.5cm"  margin-right="2.5cm">
					<fo:region-body
							margin-top="1.5cm" margin-bottom="1.8cm"
							margin-left="0cm"  margin-right="0cm"/>
					<fo:region-before region-name="header" extent="1.3cm"/>
					<fo:region-after  region-name="footer" extent="1.5cm"/>
					<fo:region-start  region-name="left"   extent="0cm"/>
					<fo:region-end    region-name="right"  extent="0cm"/>
				</fo:simple-page-master>
			</fo:layout-master-set>
			<fo:page-sequence master-reference="DIN-A4">
				<fo:static-content flow-name="header">
					<fo:block font-size="14pt" text-align="right" border-bottom-style="solid">
						<xsl:value-of select="@titel"/>
					</fo:block>
				</fo:static-content>
				<fo:static-content flow-name="footer">
                    <fo:block text-align-last="justify" border-top-style="solid">
    					<fo:inline text-align="left" font-size="8pt">
                            <fo:basic-link external-destination="https://github.com/hneemann/Digital" show-destination="new">
                                https://github.com/hneemann/Digital
                            </fo:basic-link>
		    			</fo:inline>
                        <fo:leader/>
                        <fo:inline text-align="right">
                            <fo:page-number/> / <fo:page-number-citation ref-id="LastPage"/>
                        </fo:inline>
                    </fo:block>
				</fo:static-content>
				<fo:flow flow-name="xsl-region-body">
                    <!-- large title -->
					<fo:block font-size="80pt" font-weight="bold">
						<xsl:value-of select="@titel"/>
					</fo:block>
                    <!-- image on title page -->
                    <fo:block margin-top="20mm" text-align="center">
                        <xsl:element name="fo:external-graphic">
                            <xsl:attribute name="src">url('<xsl:value-of select="@titleImage"/>')</xsl:attribute>
                            <xsl:attribute name="width">100%</xsl:attribute>
                            <xsl:attribute name="content-width">scale-to-fit</xsl:attribute>
                        </xsl:element>
                    </fo:block>
                    <!-- table of contents text -->
                    <fo:block page-break-before="always" margin-bottom="5mm" font-size="18pt" font-weight="bold">
						<xsl:value-of select="@inhalt"/>
					</fo:block>
                    <!-- table of contents -->
					<xsl:apply-templates select="lib" mode="inhalt"/>
					<fo:block page-break-before="always"/>
                    <!-- the content -->
					<xsl:apply-templates select="lib" mode="full"/>
					<fo:block id="LastPage"/>
				</fo:flow>
			</fo:page-sequence>
		</fo:root>
	</xsl:template>

	<!-- Creation of the table of content-->
	<xsl:template match="lib" mode="inhalt">
		<fo:block>
		    <xsl:value-of select="position()"/>. <xsl:value-of select="@name"/>
		</fo:block>
        <xsl:apply-templates select="element" mode="inhalt">
			<xsl:with-param name="number" select="position()"/>
        </xsl:apply-templates>
  	</xsl:template>

	<xsl:template match="element" mode="inhalt">
		<xsl:param name="number" />
		<fo:block margin-left="3mm" text-align-last="justify">
			<xsl:element name="fo:basic-link">
				<xsl:attribute name="internal-destination"><xsl:value-of select="$number"/>_<xsl:value-of select="position()"/></xsl:attribute>
				<xsl:attribute name="show-destination">replace</xsl:attribute>
				<xsl:value-of select="$number"/>.<xsl:value-of select="position()"/>. <xsl:value-of select="@name"/>
			</xsl:element>
			<xsl:text> </xsl:text>
			<fo:leader leader-pattern="dots" />
			<xsl:element name="fo:page-number-citation">
				<xsl:attribute name="ref-id"><xsl:value-of select="$number"/>_<xsl:value-of select="position()"/></xsl:attribute>
			</xsl:element>
		</fo:block>
  	</xsl:template>

	<!-- Creation of the text -->
	<xsl:template match="lib" mode="full">
		<fo:block margin-top="4mm" margin-bottom="4mm" font-size="16pt" font-weight="bold">
			<xsl:value-of select="position()"/>. <xsl:value-of select="@name"/>
		</fo:block>
		<xsl:apply-templates select="element" mode="full">
			<xsl:with-param name="number" select="position()"/>
		</xsl:apply-templates>
  	</xsl:template>

	<xsl:template match="element" mode="full">
		<xsl:param name="number" />

		<fo:block keep-together.within-page="always">
			<fo:block margin-top="6mm">
				<fo:inline>
					<xsl:element name="fo:external-graphic">
                	    <xsl:attribute name="src">url('<xsl:value-of select="@img"/>')</xsl:attribute>
						<xsl:attribute name="content-width">20%</xsl:attribute>
						<xsl:attribute name="content-height">20%</xsl:attribute>
						<xsl:attribute name="id"><xsl:value-of select="$number"/>_<xsl:value-of select="position()"/></xsl:attribute>
        	        </xsl:element>
				</fo:inline>
			</fo:block>

			<fo:block margin-top="4mm" margin-bottom="4mm" font-size="12pt" font-weight="bold">
				<xsl:value-of select="$number"/>.<xsl:value-of select="position()"/>. <xsl:value-of select="@name"/>
			</fo:block>
		</fo:block>

		<fo:block>
			<xsl:value-of select="descr"/>
		</fo:block>

        <xsl:apply-templates select="inputs"/>
        <xsl:apply-templates select="outputs"/>
        <xsl:apply-templates select="attributes"/>
  	</xsl:template>

	<xsl:template match="inputs">
		<fo:block margin-top="2mm" margin-bottom="2mm">
			<xsl:value-of select="@name"/>
		</fo:block>
		<xsl:apply-templates select="pin"/>
  	</xsl:template>

	<xsl:template match="outputs">
		<fo:block margin-top="2mm" margin-bottom="2mm">
			<xsl:value-of select="@name"/>
		</fo:block>
		<xsl:apply-templates select="pin"/>
  	</xsl:template>

	<xsl:template match="pin">
		<fo:block  margin-left="6mm">
			<xsl:value-of select="@name"/>
		</fo:block>
		<fo:block margin-left="12mm">
			<xsl:value-of select="."/>
		</fo:block>
  	</xsl:template>

	<xsl:template match="attributes">
		<fo:block margin-top="2mm" margin-bottom="2mm">
			<xsl:value-of select="@name"/>
		</fo:block>
		<xsl:apply-templates select="attr"/>
  	</xsl:template>

	<xsl:template match="attr">
		<fo:block  margin-left="6mm">
			<xsl:value-of select="@name"/>
		</fo:block>
		<fo:block margin-left="12mm">
			<xsl:value-of select="."/>
		</fo:block>
  	</xsl:template>

</xsl:stylesheet>

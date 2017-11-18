<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text"/>

    <xsl:template match="/">
        <!--<xsl:apply-templates select="//tr[@class='GridRow_WebBlue']"/>-->
        <!--<xsl:apply-templates select=""/>-->

        <!--<xsl:call-template name="header">-->
            <!--<xsl:with-param name="columns" select = "/html/body/form/div/div[2]/table/tr[5]/td/div/table/thead/tr/th" />-->
        <!--</xsl:call-template>-->
        <xsl:call-template name="body">
            <xsl:with-param name="rows" select = "/html/body/form/div/div[2]/table/tr[5]/td/div/table/tbody/tr" />
        </xsl:call-template>

        <!--<xsl:with-param name="rows" select = "/html/body/form/div/div[2]/table/tr[5]/td/div/table/tbody/tr/" />-->
    </xsl:template>

    <xsl:template name="header">
        <xsl:param name = "columns" />
        <!--<xsl:value-of select="./td[0]/text()"/>,<xsl:value-of select="./td[1]/text()"/>,<xsl:value-of select="./td[2]/text()"/>,<xsl:value-of select="./td[3]/text()"/>,<xsl:value-of select="./td[4]/text()"/>,<xsl:value-of select="./td[5]/text()"/>-->
        <xsl:for-each select="$columns">"<xsl:value-of select="a/text()"/>",</xsl:for-each>
<xsl:text>
</xsl:text>
    </xsl:template>

    <xsl:template name="body">
        <xsl:param name = "rows" />
        <xsl:for-each select="$rows">
            <xsl:call-template name="row">
                <xsl:with-param name="prow" select = "./child::node()[not(self::text())]" />
            </xsl:call-template>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="row">
        <xsl:param name = "prow" />
        <xsl:for-each select="$prow">"<xsl:value-of select="text()"/>",</xsl:for-each>
<xsl:text>
</xsl:text>
    </xsl:template>

</xsl:stylesheet>




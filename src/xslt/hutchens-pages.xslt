<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text"/>

    <xsl:template match="/">
        <xsl:apply-templates select="//tr[@class='GridPager_WebBlue']" />
    </xsl:template>

    <xsl:template match="tr">
        <!--<xsl:value-of select="." />-->
        <xsl:for-each select="td/a">
            <!--<xsl:value-of select="./@href" />-->
            <xsl:value-of select="substring (./@href, 26, 37)"/><xsl:text>
</xsl:text>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>




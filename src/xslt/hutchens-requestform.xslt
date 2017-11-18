<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text"/>

    <xsl:template match="/">
        <xsl:apply-templates select="/html/body/form" />
    </xsl:template>

    <xsl:template match="form">
        <xsl:for-each select="//input[@name != 'SearchGroup']">
            <xsl:value-of select="./@name" />=<xsl:value-of select="./@value" /><xsl:text>
</xsl:text>
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>




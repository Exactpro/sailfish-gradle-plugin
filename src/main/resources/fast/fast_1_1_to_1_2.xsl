<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  ~ Copyright 2009-2019 Exactpro (Exactpro Systems Limited)
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
-->
<xsl:stylesheet
        version="2.0"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:fast="http://www.fixprotocol.org/ns/fast/td/1.1"
        xmlns="http://www.fixprotocol.org/ns/fast/td/1.1"
        exclude-result-prefixes="fast">

    <xsl:output omit-xml-declaration="yes" indent="yes"/>
    <xsl:strip-space elements="*"/>

    <xsl:template name="processAttributes">
        <xsl:copy-of select="(@*|@name/@*)[contains('|name|id|presence|', concat('|', name(), '|'))]"/>
    </xsl:template>

    <xsl:template match="fast:enum">
        <xsl:element name="uInt32">
            <xsl:call-template name="processAttributes"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="fast:timestamp">
        <xsl:element name="uInt64">
            <xsl:call-template name="processAttributes"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="fast:boolean">
        <xsl:element name="uInt32">
            <xsl:call-template name="processAttributes"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
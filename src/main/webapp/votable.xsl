<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:vtm="http://www.ivoa.net/xml/VOSITables/v1.0"
    xmlns:vot="http://www.ivoa.net/xml/VOTable/v1.3" 
    xmlns="http://www.w3.org/1999/xhtml"
    version="1.0">

    <xsl:output method="xml" 
      doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"
      doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"/>

	<!-- release date column position -->
	<xsl:variable name="rDatePosition" select="40" />

    <!-- Hide everything by default. --> 
    <xsl:template match="text()"/>

	<!--  VO Table -->

    <xsl:template match="vot:TD">
    	<td class="col">
    	<xsl:choose>
    	<xsl:when test="starts-with(text(), 'http')">
			<a>
			  <xsl:attribute name="href">
    			<xsl:value-of select="text()" />
  			  </xsl:attribute>
  			  <xsl:value-of select="text()"/>
			</a>    		
    	</xsl:when>
    	<xsl:otherwise>
    		<xsl:value-of select="text()"/>
    	</xsl:otherwise>
    	</xsl:choose>
    	</td>
    </xsl:template>

    <xsl:template match="vot:TR" mode="obscore">
    	<tr>
             <xsl:attribute name="class">
                 <xsl:choose>
                     <xsl:when test="(position() mod 2) != 1">
                         <xsl:text>even</xsl:text>
                     </xsl:when>
                     <xsl:otherwise>
                         <xsl:text>odd</xsl:text>
                     </xsl:otherwise>
                 </xsl:choose>
             </xsl:attribute>
             <xsl:apply-templates select="vot:TD[position()&lt;13]"/>
             <!-- release date column value -->
             <xsl:apply-templates select="vot:TD[position() = $rDatePosition]"/>
    	</tr>
    </xsl:template>

    <xsl:template match="vot:TR">
    	<tr>
             <xsl:attribute name="class">
                 <xsl:choose>
                     <xsl:when test="(position() mod 2) != 1">
                         <xsl:text>even</xsl:text>
                     </xsl:when>
                     <xsl:otherwise>
                         <xsl:text>odd</xsl:text>
                     </xsl:otherwise>
                 </xsl:choose>
             </xsl:attribute>
             <xsl:apply-templates select="vot:TD"/>
    	</tr>
    </xsl:template>

    <xsl:template match="vot:FIELD">
    	<xsl:choose>
    		<xsl:when test="@name='description'">
    			<th class="tableHeaderDesc">
			    	<xsl:value-of select="@name"/>
				</th>
    		</xsl:when>
    		<xsl:otherwise>
		    	<th>
			    	<xsl:value-of select="@name"/>
			    	<xsl:choose>
						<xsl:when test="@name='authenticated_id_token'">
							<br/>(ID: <xsl:value-of select="@ID" />)
						</xsl:when>				
					</xsl:choose>
				</th>
    		</xsl:otherwise>
		</xsl:choose>
    </xsl:template>

    <xsl:template match="vot:VOTABLE[vot:RESOURCE/vot:INFO/@name='Table Name' and vot:RESOURCE/vot:INFO/@value='ivoa.obscore']">
        <table class="voTable" >
			<colgroup>
				<!-- total number of display column -->
				<xsl:for-each select="vot:RESOURCE/vot:TABLE/vot:FIELD[position()&lt;13]">
					<col />
				</xsl:for-each>
			</colgroup>        
            <tr class="tableHeader">
                <xsl:apply-templates select="vot:RESOURCE/vot:TABLE/vot:FIELD[position()&lt;13]"/>
                <!-- release date column header -->
                <xsl:apply-templates select="vot:RESOURCE/vot:TABLE/vot:FIELD[position() = $rDatePosition]"/>
            </tr>
            <xsl:apply-templates mode="obscore" select="vot:RESOURCE/vot:TABLE/vot:DATA/vot:TABLEDATA/vot:TR"/>
        </table>
    </xsl:template>

	<xsl:template match="vot:RESOURCE[@type='meta']">
		<hr />
		<h3>Service - <xsl:value-of select="@ID" /></h3>
		<b>Details</b>
		<table class="vot:VOTABLE">
			<xsl:apply-templates select="vot:PARAM" />
		</table>
		<xsl:apply-templates select="vot:GROUP" />
	</xsl:template>

	<xsl:template match="vot:PARAM">
		<tr>
             <xsl:attribute name="class">
                 <xsl:choose>
                     <xsl:when test="(position() mod 2) != 1">
                         <xsl:text>even</xsl:text>
                     </xsl:when>
                     <xsl:otherwise>
                         <xsl:text>odd</xsl:text>
                     </xsl:otherwise>
                 </xsl:choose>
             </xsl:attribute>
		
			<xsl:for-each select="@*">
				<td class="col"><b><xsl:value-of select="name()" /> : </b><xsl:value-of select="." /></td>
			</xsl:for-each>
		</tr>
	</xsl:template>

	<xsl:template match="vot:GROUP">
		<b>Params</b>
		<table class="">
			<xsl:apply-templates select="vot:PARAM" />
		</table>
	</xsl:template>

    <xsl:template match="vot:VOTABLE">
        <table class="voTable">
            <tr class="tableHeader">
                <xsl:apply-templates select="vot:RESOURCE/vot:TABLE/vot:FIELD"/>
            </tr>
            <xsl:apply-templates select="vot:RESOURCE/vot:TABLE/vot:DATA/vot:TABLEDATA/vot:TR"/>
        </table>
        <xsl:apply-templates select="vot:RESOURCE[@type='meta']"></xsl:apply-templates>
    </xsl:template>

    <xsl:template match="/">
        <html>
            <head>
              <title><xsl:value-of select="vot:VOTABLE/vot:RESOURCE/@name"/></title>
              <!--  Note: These links are relative to the file being processed, not to this file. -->
              <link rel="stylesheet" type="text/css" href="../css/global.css" />
              <link rel="stylesheet" type="text/css" href="../css/vo_tools.css" />
            </head>
            <body>            	
            	<xsl:choose>
                     <xsl:when test="vot:VOTABLE/vot:RESOURCE/@type='results'">
                         <h2><xsl:value-of select="vot:VOTABLE/vot:RESOURCE/@name"/></h2>
                     </xsl:when>
                     <xsl:otherwise>
                         <h2></h2>
                     </xsl:otherwise>
                </xsl:choose>  
                <xsl:if test="contains(vot:VOTABLE/vot:RESOURCE/@name,'Datalink')">
	                <P>
	                	The SIAP services are primarily intended for use with scripts and programmatic data access. 
	                	Please refer to the <a href="http://www.atnf.csiro.au/observers/data/casdaguide.html" target="_blank">CASDA User Guide</a> for information and examples.
	                </P>               
                </xsl:if>
       
                <xsl:apply-templates select="*"/>
            </body>
        </html>
    </xsl:template>

</xsl:stylesheet>

<?xml version="1.0" encoding="utf-8"?>
<VOTABLE version="1.3" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns="http://www.ivoa.net/xml/VOTable/v1.3" xmlns:stc="http://www.ivoa.net/xml/STC/v1.30"
	xsi:schemaLocation="http://www.ivoa.net/xml/VOTable/v1.3 http://www.ivoa.net/xml/VOTable/v1.3 http://www.ivoa.net/xml/STC/v1.30 http://www.ivoa.net/xml/STC/v1.30">
	<RESOURCE name="CASDA SIAP Result" type="meta">
		<DESCRIPTION>${survey} Simple Image Access Service</DESCRIPTION>
		<INFO name="QUERY_STATUS" value="OK">Successful query</INFO>
		<INFO name="SERVICE_PROTOCOL" value="1.0">SIAP</INFO>

		<!-- Input parameters -->
		<PARAM name="INPUT:POS" value="" datatype="char" arraysize="*">
			<DESCRIPTION>
				The center of the region of interest in the form "ra,dec" where ra and dec are given in 
				decimal degrees in the ICRS coordinate system..
			</DESCRIPTION>
		</PARAM>

		<PARAM name="INPUT:SIZE" value="${defaultSizeDegrees}" datatype="double" unit="deg">
			<DESCRIPTION>
				Size of search region in the RA and Dec. directions in either "ra,dec" or "diameter" format 
				with the value in decimal degrees.
			</DESCRIPTION>
			<VALUES>
				<MIN value="0" />
				<MAX value="${maxSizeDegrees}" />
			</VALUES>
		</PARAM>

		<PARAM name="INPUT:FORMAT" value="ALL" datatype="char"
			arraysize="*">
			<DESCRIPTION>Requested format of images</DESCRIPTION>
			<VALUES>
			<#list formats as format>
				<OPTION value="${format}" />
			</#list>
			</VALUES>
		</PARAM>

		<!-- Output parameters -->
		<TABLE name="results">
			<#list outputFields as field>
			<FIELD name="OUTPUT:${field.fieldName!}" ID="${field.fieldName}" value="" 
				datatype="${field.datatype}" <#if field.arraysize?has_content>arraysize="${field.arraysize}"</#if>
				<#if field.unit?has_content>unit="${field.unit}"</#if> <#if field.ucd?has_content>ucd="${field.ucd}"</#if> >
				<DESCRIPTION>
					${field.description}
				</DESCRIPTION>
			</FIELD>
			</#list>
		</TABLE>
		
		

	</RESOURCE>
</VOTABLE>

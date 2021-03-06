<%@ page contentType="application/xml"%><?xml version="1.0" encoding="UTF-8"?>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<vosi:capabilities
	xmlns:vosi="http://www.ivoa.net/xml/VOSICapabilities/v1.0"
	xmlns:tap="http://www.ivoa.net/xml/TAPRegExt/v1.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:vs="http://www.ivoa.net/xml/VODataService/v1.1"
	xmlns:vr="http://www.ivoa.net/xml/VOResource/v1.0"
	xsi:schemaLocation="http://www.ivoa.net/xml/VOSICapabilities/v1.0 http://www.ivoa.net/xml/VOSICapabilities/v1.0 http://www.ivoa.net/xml/SIA/v1.1 http://www.ivoa.net/xml/SIA/v1.1 http://www.ivoa.net/xml/VODataService/v1.1 http://www.ivoa.net/xml/VODataService/v1.1">
	<capability standardID="ivo://ivoa.net/std/VOSI#capabilities">
	<interface xsi:type="vs:ParamHTTP" version="1.0" role="std">
	<accessURL use="full"> ${capabilitiesURL} </accessURL>
	<queryType>GET</queryType>
	<resultType>application/xml</resultType>
	</interface>
	</capability>
	<capability standardID="ivo://ivoa.net/std/VOSI#availability">
	<interface xsi:type="vs:ParamHTTP" version="1.0" role="std">
	<accessURL use="full"> ${availabilityURL} </accessURL>
	<queryType>GET</queryType>
	<resultType>application/xml</resultType>
	</interface>
	</capability>
	<capability standardID="ivo://ivoa.net/std/VOSI#tables">
	<interface xsi:type="vs:ParamHTTP" version="1.0" role="std">
	<accessURL use="full"> ${tablesURL} </accessURL>
	<queryType>GET</queryType>
	<resultType>application/xml</resultType>
	</interface>
	</capability>
	<capability standardID="ivo://ivoa.net/std/TAP"
		xsi:type="tap:TableAccess">
	<interface xsi:type="vs:ParamHTTP" version="1.0" role="std">
	<accessURL use="base">${tapURL}</accessURL>
	<queryType>GET</queryType>
	<resultType>application/xml</resultType>
	</interface>
	<c:if test="${obscoreVersion == '1.0'}">
	<dataModel ivo-id="ivo://ivoa.net/std/ObsCore/v1.0">ObsCore-1.0</dataModel>
	</c:if>
	<c:if test="${obscoreVersion == '1.1'}">
	<dataModel ivo-id="ivo://ivoa.net/std/ObsCore#core-1.1">ObsCore-1.1</dataModel>
	</c:if>
	<language> <name>${languageName}</name> <version
		ivo-id="ivo://ivoa.net/std/${languageName}#v${languageVersion}">${languageVersion}</version>
	<description>${languageDesc}</description> </language>
	<outputFormat> <mime>${outputFormatMime}</mime> <alias>${outputFormatAlias}</alias>
	</outputFormat>
	<c:if test="${uploadEnabled}">
	<uploadMethod ivo-id="ivo://ivoa.net/std/TAPRegExt#upload-https"/>
	<uploadMethod ivo-id="ivo://ivoa.net/std/TAPRegExt#upload-inline"/>
    <uploadMethod ivo-id="ivo://ivoa.net/std/TAPRegExt#upload-http"/>
	</c:if>
	<retentionPeriod> <default>${retentionPeriodDefault}</default>
	<hard>${retentionPeriodHard}</hard> </retentionPeriod>
	<executionDuration> <default>${execDurationDefault}</default>
	<hard>${execDurationHard}</hard> </executionDuration>
	<outputLimit> <hard unit="row">${outputLimitHard}</hard>
	</outputLimit>
	<c:if test="${uploadEnabled}">
	<uploadLimit>
	<hard unit="byte">${uploadLimit}</hard>
	</uploadLimit>
	</c:if>
	</capability>
	<c:if test="${not empty tapExamplesUrl}">
		<capability standardID="ivo://ivoa.net/std/DALI#examples">
			<interface xsi:type="vr:WebBrowser" role="std"> 
				<accessURL use="full">${tapExamplesUrl}</accessURL> 
			</interface> 
		</capability>
	</c:if>
</vosi:capabilities>
